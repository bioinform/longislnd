package com.bina.lrsim.simulator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;

import com.bina.lrsim.bioinfo.*;
import com.bina.lrsim.pb.*;
import com.bina.lrsim.simulator.samples.pool.AppendedState;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.Pair;
import org.apache.log4j.Logger;

import com.bina.lrsim.interfaces.RandomFragmentGenerator;
import com.bina.lrsim.simulator.samples.SamplesDrawer;
import com.bina.lrsim.util.Monitor;

/**
 * Created by bayo on 5/11/15.
 */
public class Simulator {
  private final static Logger log = Logger.getLogger(Simulator.class.getName());
  private final AtomicLongArray baseCounter = new AtomicLongArray(EnumEvent.values.length);
  private final AtomicLongArray eventCounter = new AtomicLongArray(EnumEvent.values.length);
  private final ConcurrentHashMap<String, AtomicLong> nameCounter = new ConcurrentHashMap<>();
  private final RandomFragmentGenerator randomFragmentGenerator;

  /**
   * Constructor
   * 
   * @param randomFragmentGenerator a random sequence generator
   */
  public Simulator(RandomFragmentGenerator randomFragmentGenerator) {
    this.randomFragmentGenerator = randomFragmentGenerator;
  }

  /**
   * Generate a pacbio h5 file containing reads simulated according to the sampler and reference
   * 
   * @param path output path
   * @param movieName movie name
   * @param firsthole first hole producing sequence
   * @param samplesDrawer an instance from which samples can be drawn
   * @param totalBases minimum number of bp to generate
   * @param randomNumberGenerator random number generator
   */
  public int simulate(final String path, final String movieName, final int firsthole, SamplesDrawer samplesDrawer, int totalBases, final Spec spec, RandomGenerator randomNumberGenerator) throws IOException {
    try (ReadsWriter writer = ReadsWriterFactory.makeWriter(
            spec, new File(path, movieName + spec.getSuffix()).getPath(),
            movieName, firsthole,
            //it seems only the first set of run infos is used
            samplesDrawer.getNumRunInfo() > 0 ? samplesDrawer.getRunInfo(0) : new RunInfo())) {
      long[] localBaseCounter = new long[baseCounter.length()];
      PBReadBuffer read = new PBReadBuffer(spec);

      /*
      each iteration generates one simulated read
       */
          for (int numBases = 0; numBases < totalBases;) {
            read.clear();

        // draw a list of smrt belts
        Pair<int[], Integer> lenScore = samplesDrawer.getRandomLengthScore(randomNumberGenerator);
        final int[] insertLengths = lenScore.getFirst();
        MultiPassSpec multiPassSpec = new MultiPassSpec(insertLengths);

        final Fragment fragment = randomFragmentGenerator.getFragment(multiPassSpec.fragmentLength, randomNumberGenerator);
        final Locus locus = fragment.getLocus();
        nameCounter.putIfAbsent(locus.getChrom(), new AtomicLong((long) 0));
        nameCounter.get(locus.getChrom()).incrementAndGet();
        final byte[] sampledReferenceSequence = fragment.getSeq();
        // in some sequence drawer, such as fragment mode, the sequence can be much longer than read length, in this case we set the passes to fragment length
        if(multiPassSpec.fragmentLength < Heuristics.READLENGTH_RESCUE_FRACTION * sampledReferenceSequence.length) {
            //why starts from 1 instead of 0?
          for (int ii = 1; ii + 1 < insertLengths.length; ++ii) {
            if (insertLengths[ii] < sampledReferenceSequence.length) {
              insertLengths[ii] = sampledReferenceSequence.length;
            }
          }
        }
        /* correct insert lengths if the drawn fragment is shorter, the fractional change might not be realistic, but it avoids crazy coverage in fragment mode
            * make sure no insert length is longer than sampled reference sequence
            */
        for (int ii = 0; ii < insertLengths.length; ++ii) {
          if(insertLengths[ii] > sampledReferenceSequence.length) {
            insertLengths[ii] = sampledReferenceSequence.length;
          }
        }

        // draw a sequence according to max insert length, make RC if belt is long enough
        final List<byte[]> forwardAndReverseComplementSequences = new ArrayList<>(2);
        forwardAndReverseComplementSequences.add(sampledReferenceSequence);
        if (insertLengths.length > 1) {
          /*the forward sequence might be reverse complement of a reference sequence
          one has to check reverse complement boolean flag of locus for that fragment
           */
          final byte[] forwardSequence = forwardAndReverseComplementSequences.get(0);
          final byte[] reverseComplementSequence = new byte[forwardSequence.length];
          //TODO: wrap reverse complement generation into a method
          for (int pos = 0, tgt = forwardSequence.length - 1; pos < forwardSequence.length; ++pos, --tgt) {
            reverseComplementSequence[tgt] = EnumBP.ascii_rc(forwardSequence[pos]);
          }
          forwardAndReverseComplementSequences.add(reverseComplementSequence);
        }

        //clr stands for continuous long read
        final List<Locus> clrLoci = new ArrayList<>();

        final List<Integer> sectionEnds = new ArrayList<>(2 * insertLengths.length - 1);
        boolean skipIfShort = false;
        for (int insIdx = 0; insIdx < insertLengths.length; ++insIdx) {
          final int currentInsertLength = insertLengths[insIdx];
          final boolean isFirstClr = insIdx == 0;
          final boolean isLastClr = insIdx + 1 == insertLengths.length;
            //why set begin and end like this?
          final int begin = isFirstClr ? sampledReferenceSequence.length - currentInsertLength : 0;
          final int end = isLastClr ? currentInsertLength : sampledReferenceSequence.length;
          final boolean isShort = currentInsertLength < Heuristics.SMRT_INSERT_FRACTION * sampledReferenceSequence.length && !isFirstClr && !isLastClr;
          if (!isShort || !skipIfShort) {
            if (!isFirstClr) {
              // prepend with a "perfect" adaptor sequence
              read.addASCIIBases(Heuristics.SMRT_ADAPTOR_STRING, Heuristics.SMRT_ADAPTOR_STRING, Heuristics.SMRT_ADAPTOR_SCORE);
              sectionEnds.add(read.size());
            }
            AppendedState previousState = null;
            for (Iterator<Context> itr = new HPIterator(forwardAndReverseComplementSequences.get(insIdx % 2), begin, end, samplesDrawer.getLeftFlank(), samplesDrawer.getRightFlank(), samplesDrawer.getHpAnchor()); itr.hasNext();) {
              final Context currentContext = itr.next();
              if (null != currentContext) {
                //this is where the magic of error simulation happens~~~
                previousState = samplesDrawer.appendTo(read, currentContext, previousState, randomNumberGenerator, localBaseCounter);
              }
            }

            //per clr coordinates
            int clrBegin = locus.getBegin0();
            int clrEnd = locus.getEnd0();
            //determine if current sequence is truly reverse complement of original reference
            final boolean isReverseComplement = (insIdx % 2 == 1) ^ locus.isReverseComplement();
            if (isFirstClr) {
              if(isReverseComplement)
                clrEnd = locus.getBegin0() + currentInsertLength;
              else
                clrBegin = locus.getEnd0() - currentInsertLength;
            }
            if (isLastClr) {
              if(isReverseComplement)
                clrBegin = locus.getEnd0() - currentInsertLength;
              else
                clrEnd = locus.getBegin0() + currentInsertLength;
            }
            clrLoci.add(new Locus(locus.getChrom(), clrBegin, clrEnd, isReverseComplement));
            sectionEnds.add(read.size());
            if (isShort) {
              skipIfShort = true;
            }
          }
          if (!isShort) {
            skipIfShort = false;
          }
        }

        writer.addLast(read, sectionEnds, lenScore.getSecond(), locus, clrLoci);
        numBases += read.size();
      }
      for (int index = 0; index < localBaseCounter.length; ++index) {
        baseCounter.getAndAdd(index, localBaseCounter[index]);
      }
      synchronized (log) {
        log.info(toString());
        log.info("generated " + writer.size() + " reads.");
        log.info("Memory usage: " + Monitor.PeakMemoryUsage());
      }
      return writer.size();
    }
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("simulated statistics\n");
    long[] tmp = new long[baseCounter.length()];
    for (int index = 0; index < tmp.length; ++index) {
      tmp[index] = baseCounter.get(index);
    }
    sb.append(EnumEvent.getPrettyStats(tmp));
    sb.append("\n");
    for (int index = 0; index < tmp.length; ++index) {
      tmp[index] = eventCounter.get(index);
    }
    sb.append(EnumEvent.getPrettyStats(tmp));
    sb.append("\n");
    for (ConcurrentHashMap.Entry<String, AtomicLong> entry : nameCounter.entrySet()) {
      sb.append(entry.getKey());
      sb.append(" ");
      sb.append(entry.getValue().get());
      sb.append("\n");
    }
    return sb.toString();
  }
}
