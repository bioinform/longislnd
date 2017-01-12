package com.bina.lrsim.simulator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;

import com.bina.lrsim.bioinfo.*;
import com.bina.lrsim.pb.*;
import com.bina.lrsim.simulator.samples.pool.AppendState;
import com.bina.lrsim.util.ArrayUtils;
import com.bina.lrsim.util.CircularArrayList;
import org.apache.commons.lang3.NotImplementedException;
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
  private final RandomFragmentGenerator seqGen;

  /**
   * Constructor
   * 
   * @param seqGen a random sequence generator
   */
  public Simulator(RandomFragmentGenerator seqGen) {
    this.seqGen = seqGen;
  }

  /**
   * wrapper for two implementations of simulate()
   */
  public int simulate(final String path, final String movieName, final int firsthole,
                      SamplesDrawer drawer, int totalBases, final Spec spec,
                      RandomGenerator gen) throws IOException {
    if (spec.isPolymeraseReadFlag()) {
      return simulateWithNoisyAdapter(path, movieName, firsthole, drawer, totalBases,
              spec, gen);
    } else {
      return simulateWithPerfectAdapter(path, movieName, firsthole, drawer, totalBases,
              spec, gen);
    }
  }

  /**
   * Generate a pacbio h5 file containing reads simulated according to the sampler and reference
   * 
   * @param path output path
   * @param movieName movie name
   * @param firsthole first hole producing sequence
   * @param drawer an instance from which samples can be drawn
   * @param totalBases minimum number of bp to generate
   * @param gen random number generator
   */
  private int simulateWithPerfectAdapter(final String path, final String movieName, final int firsthole, SamplesDrawer drawer, int totalBases, final Spec spec, RandomGenerator gen) throws IOException {
    try (ReadsWriter writer = ReadsWriterFactory.makeWriter(spec, new File(path, movieName + spec.getSuffix()).getPath(), movieName, firsthole, drawer.getNumRunInfo() > 0 ? drawer.getRunInfo(0) : new RunInfo())) {
      long[] localBaseCounter = new long[baseCounter.length()];
      PBReadBuffer read = new PBReadBuffer(spec);

      for (int numBases = 0; numBases < totalBases;) {
        read.clear();
        if (read.size() != 0) {
          log.error("couldn't clear buffer");
          throw new RuntimeException("different lengths!");
        }

        // draw a list of smrt belts
        Pair<int[], Integer> lenScore = drawer.getRandomLengthScore(gen);
        final int[] insertLengths = lenScore.getFirst();
        MultiPassSpec multiPassSpec = new MultiPassSpec(insertLengths);

        final Fragment fragment = seqGen.getFragment(multiPassSpec.fragmentLength, gen);
        final Locus locus = fragment.getLocus();
        nameCounter.putIfAbsent(locus.getChrom(), new AtomicLong((long) 0));
        nameCounter.get(locus.getChrom()).incrementAndGet();
        final byte[] sequence = fragment.getSeq();
        // in some sequence drawer, such as fragment mode, the sequence can be much longer than read length, in this case we set the passes to fragment length
        if(multiPassSpec.fragmentLength < Heuristics.READLENGTH_RESCUE_FRACTION * sequence.length) {
          for (int ii = 1; ii + 1 < insertLengths.length; ++ii) {
            if (insertLengths[ii] < sequence.length) {
              insertLengths[ii] = sequence.length;
            }
          }
        }
        // correct insert lengths if the drawn fragment is shorter, the fractional change might not be realistic, but it avoids crazy coverage in fragment mode
        for (int ii = 0; ii < insertLengths.length; ++ii) {
          if(insertLengths[ii] > sequence.length) {
            insertLengths[ii] = sequence.length;
          }
        }

        // draw a sequence according to max insert length, make RC if belt is long enough
        final List<byte[]> fwRc = new ArrayList<>(2);
        fwRc.add(sequence);
        if (insertLengths.length > 1) {
          final byte[] fw = fwRc.get(0);
          final byte[] rc = new byte[fw.length];
          for (int pos = 0, tgt = fw.length - 1; pos < fw.length; ++pos, --tgt) {
            rc[tgt] = EnumBP.ascii_rc(fw[pos]);
          }
          fwRc.add(rc);
        }

        final List<Locus> clrLoci = new ArrayList<>();

        final List<Integer> sectionEnds = new ArrayList<>(2 * insertLengths.length - 1);
        boolean skipIfShort = false;
        for (int insIdx = 0; insIdx < insertLengths.length; ++insIdx) {
          final int insertLength = insertLengths[insIdx];
          final boolean firstClr = insIdx == 0;
          final boolean lastClr = insIdx + 1 == insertLengths.length;
          final int begin = firstClr ? sequence.length - insertLength : 0;
          final int end = lastClr ? insertLength : sequence.length;
          final boolean isShort = insertLength < Heuristics.SMRT_INSERT_FRACTION * sequence.length && !firstClr && !lastClr;
          if (!isShort || !skipIfShort) {
            if (!firstClr) {
              // prepend with a "perfect" adaptor sequence
              read.addASCIIBases(Heuristics.SMRT_ADAPTOR_STRING, Heuristics.SMRT_ADAPTOR_STRING, Heuristics.SMRT_ADAPTOR_SCORE);
              sectionEnds.add(read.size());
            }
            AppendState deletion = null;
            for (Iterator<Context> itr = new HPIterator(fwRc.get(insIdx % 2), begin, end, drawer.getLeftFlank(), drawer.getRightFlank(), drawer.getHpAnchor()); itr.hasNext();) {
              final Context con = itr.next();
              if (null != con) {
                deletion = drawer.appendTo(read, con, deletion, gen, localBaseCounter);
              }
            }

            //per clr coordinates
            int clrBegin = locus.getBegin0();
            int clrEnd = locus.getEnd0();
            final boolean readIsRc = (insIdx % 2 == 1) ^ locus.isRc();
            if (firstClr) {
              if(readIsRc)
                clrEnd = locus.getBegin0() + insertLength;
              else
                clrBegin = locus.getEnd0() - insertLength;
            }
            if (lastClr) {
              if(readIsRc)
                clrBegin = locus.getEnd0() - insertLength;
              else
                clrEnd = locus.getBegin0() + insertLength;
            }
            clrLoci.add(new Locus(locus.getChrom(), clrBegin, clrEnd, readIsRc));
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

  public int simulateWithNoisyAdapter(final String path, final String movieName, final int firsthole,
                      SamplesDrawer samplesDrawer, int totalBases,
                      final Spec spec, RandomGenerator randomNumberGenerator) throws IOException {
    boolean outputPolymeraseRead = spec.isPolymeraseReadFlag();
    try (ReadsWriter writer = ReadsWriterFactory.makeWriter(
            spec, new File(path, movieName + spec.getSuffix()).getPath(),
            movieName, firsthole,
            //it seems only the first set of run infos is used
            samplesDrawer.getNumRunInfo() > 0 ? samplesDrawer.getRunInfo(0) : new RunInfo());
         FileOutputStream adapaterEndWriter = new FileOutputStream(
                 new File(path, movieName + spec.getSuffix() + ".adapter"))
    ) {
      long[] localBaseCounter = new long[baseCounter.length()];
      PBReadBuffer read = new PBReadBuffer(spec);

      /*
      each iteration generates one simulated read
       */
      for (int numBases = 0; numBases < totalBases;) {
        read.clear();

        // draw a list of smrt belts
        Pair<int[], Integer> lenScore = samplesDrawer.getRandomLengthScore(randomNumberGenerator);
        //TODO: for polymerase mode, we should probably apply polymerase length distribution
        //for now, we just stick with subread length distribution model
        final int[] insertLengths = lenScore.getFirst();
        MultiPassSpec multiPassSpec = new MultiPassSpec(insertLengths);

        final Fragment fragment = seqGen.getFragment(multiPassSpec.fragmentLength, randomNumberGenerator);
        final Locus locus = fragment.getLocus();
        nameCounter.putIfAbsent(locus.getChrom(), new AtomicLong((long) 0));
        nameCounter.get(locus.getChrom()).incrementAndGet();
        final byte[] sampledReferenceSequence = fragment.getSeq();
        //clr stands for continuous long read
        final List<Locus> clrLoci = new ArrayList<>();
        final List<Integer> sectionEnds = new ArrayList<>(2 * insertLengths.length - 1);
        /*
         *
         *
         * SMRTBell scheme
         * both bell-shape adapters go in forward direction (5'->3')
         * the insert sequences alternate between forward and reverse-complement states.
         * so overall polymerase looks like
         *
         * fa-rc-fa-f-fa...
         * or
         * fa-f-fa-rc-fa...
         *
         * fa: forward adapter
         * f: forward insert
         * rc: reverse complement of insert
         *
         *    _                       _
         *  /   \5'_______________3'/   \
         * |     3'_______________5'     |
         *  \ _ /                   \ _ /
         *
         *
         */
        // in some sequence drawer, such as fragment mode, the sequence can be much longer than read length, in this case we set the passes to fragment length
        if (multiPassSpec.fragmentLength < Heuristics.READLENGTH_RESCUE_FRACTION * sampledReferenceSequence.length) {
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
          if (insertLengths[ii] > sampledReferenceSequence.length) {
            insertLengths[ii] = sampledReferenceSequence.length;
          }
        }

        // draw a sequence according to max insert length, make RC if belt is long enough
        final List<byte[]> forwardAndReverseComplementSequences = new ArrayList<>(2);
        forwardAndReverseComplementSequences.add(sampledReferenceSequence);
        //if (insertLengths.length > 1) { //this if statement is unnecessary as later on loops will be bounded by insertLengths.length
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
        //}

        /*****************************************************************************************
         ******************************************************************************************
         *polymerase read mode*
         ******************************************************************************************
         ******************************************************************************************
         ******************************************************************************************
         */
        if (outputPolymeraseRead) {
          /*a polymerase read has to start in the loop of a bell-shaped adapter
          for now we ignore the fine structure of an adapter
          and assume it starts anywhere completely randomly (if sequencing starts
          in an adapter).
          */
          int randomStartPositionInAdapter = randomNumberGenerator.nextInt(spec.getAdapterSequence().length);
          byte[] adapterNucleotideArray = spec.getAdapterSequence();
          /*
          if our model is built from H5 input, then we can sum up insert lengths to get the total
          length of a polymerase read minus adapter length.
          for n full passes, we add back n - 1 adapter sequences
           */
          /*
          construct an artificial polymerase read without errors
          partial fa - f - fa - rc - fa ... - partial fa
          or
          partial fa - rc - fa - f - fa ... - partial fa

          partial fa: partial forward adapter
          f: forward insert
          fa: forward adapter
          rc: reverse complement of insert

          a good news is, because the sampled reference could be forward or reverse complement (we need to check locus to determine)
          we do not have to explicitly simulate strand of first insert in a polymerase read
           */
          final Pair<byte[], String[]> noErrorPolymeraseReadPair = populateNoErrorPolymeraseRead(insertLengths, adapterNucleotideArray,
                  forwardAndReverseComplementSequences, randomStartPositionInAdapter, movieName + "/" + writer.size());
          adapaterEndWriter.write(ArrayUtils.join(noErrorPolymeraseReadPair.getSecond(), "\t").getBytes());
          final byte[] noErrorPolymeraseRead = noErrorPolymeraseReadPair.getFirst();
          final int polymeraseReadLength = noErrorPolymeraseRead.length;
          final int begin = 0;
          final int end = polymeraseReadLength; //exclusive end that will be used in HPIterator
          AppendState previousState = null;
          /*
           * the first few bp equal to length of flank will be skipped
           * if first context we have contains homopolymer, more bp will be skipped.
           */
          for (Iterator<Context> itr = new HPIterator(noErrorPolymeraseRead, begin, end, samplesDrawer.getLeftFlank(),
                  samplesDrawer.getRightFlank(), samplesDrawer.getHpAnchor());
               itr.hasNext();) {
            final Context currentContext = itr.next();
            if (null != currentContext) {
              //this is where the magic of error simulation happens~~~
              previousState = samplesDrawer.appendTo(read, currentContext, previousState, randomNumberGenerator, localBaseCounter);
            }
          }

          //we should have adjusted begin and end by flank, but this is trivial, ignored for now.
          int clrBegin = locus.getBegin0();
          int clrEnd = locus.getEnd0();
          //for polymerase read, strand of its origin will always be positive regardless.
          boolean isReverseComplement = true;
          //for polymerase read, we record full region of sampled fragment as its origin
          //unless number of passes is smaller than 1
          if (insertLengths.length == 1) {
            isReverseComplement = locus.isRc();
            //we need 0-based start and 1-based end for BED
            if (isReverseComplement) {
              //update start
              clrBegin  = locus.getEnd0() - insertLengths[0];
            } else {
              //update end
              clrEnd = locus.getBegin0() + insertLengths[0];
            }
          }
          clrLoci.add(new Locus(locus.getChrom(), clrBegin, clrEnd, isReverseComplement));
          sectionEnds.add(read.size());
          writer.addLast(read, sectionEnds, lenScore.getSecond(), locus, clrLoci);
          numBases += read.size();
          continue;
        } else {
          throw new NotImplementedException("not implemented.");
          //TODO: remove the following code in comment, as simulateWithPerfectAdapter can replace it

        /*****************************************************************************************
         ******************************************************************************************
         *subread mode*
         ******************************************************************************************
         ******************************************************************************************
         ******************************************************************************************
         */

        /*
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
            AppendState previousState = null;
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
            final boolean isReverseComplement = (insIdx % 2 == 1) ^ locus.isRc();
            if (isFirstClr) {
              if(isReverseComplement)
                clrBegin = locus.getEnd0() - currentInsertLength;
              else
                clrEnd = locus.getBegin0() + currentInsertLength;
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
        */
        }
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
    } catch(Exception e) {
      e.printStackTrace();
      log.error(e.getMessage());
      log.error("simulate() failed");
    }
    return -1;
  }

  /**
   * calculate length of polymerase read given an array of
   * insert lengths and adapter length, and start position in adapter
   *
   * if first insert is not full length, then sequencing starts in an insert
   * otherwise in an adapter
   *
   * assume no adapter at the end
   * if we know true length (without indel errors) of each insert, we can determine whether last
   * insert is full or partial, and add adapter if it is full. However,
   * for now we just assume there is no adapter at the end of a
   * polymerase read. The chance of polymerase stopping in an adapter
   * is low after all.
   *
   * @param insertLengths
   * @param adapaterLength
   * @param startInAdapter
   * @return
   */
  private int calculatePolymeraseReadLength(int[] insertLengths, int adapaterLength, int startInAdapter, int originalInsertLength) {
    if (insertLengths == null || insertLengths.length == 0) {
      return 0;
    }
    int polymeraseReadLength = insertLengths[0];
    if (insertLengths[0] >= originalInsertLength) {
      polymeraseReadLength += adapaterLength - startInAdapter;
    }
    for (int i = 1; i < insertLengths.length; i++) {
      polymeraseReadLength += adapaterLength;
      polymeraseReadLength += insertLengths[i];
    }
    return polymeraseReadLength;
  }
  /**
   * add adapter (assuming forward) to fragment
   * regardless of fragment is forward or reverse complemented, prepend
   * adapater to the fragment
   * @param fragmentSequence
   * @param adapter
   * @return
   */
  private byte[] addAdapter(byte[] fragmentSequence, String adapter) {
    byte[] sequenceWithAdapater = new byte[fragmentSequence.length + adapter.length()];
    int overallIndex = 0;
    for (int i = 0; i < adapter.length(); i++) {
      sequenceWithAdapater[overallIndex++] = (byte) adapter.charAt(i);
    }
    for (int i = 0; i < fragmentSequence.length; i++) {
      sequenceWithAdapater[overallIndex++] = fragmentSequence[i];
    }
    return sequenceWithAdapater;
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

  /**
   * construct a byte array of polymerase read nucleotides for error simulation
   *
   * note that sequencing may start in an adapter or an insert
   *
   * @param insertLengths sampled subread/insert lengths
   * @param adapterNucleotideArray adapter sequence
   * @param forwardReverseComplement forward and reverse complement of insert
   * @param startInAdapter start position in first adapter sequence of the polymerase read
   * @return
   */
  private Pair<byte[], String[]> populateNoErrorPolymeraseRead(int[] insertLengths, byte[] adapterNucleotideArray,
                                               List<byte[]> forwardReverseComplement, int startInAdapter,
                                               String readName) {

    int originalInsertLength = forwardReverseComplement.get(0).length;
    int polymeraseReadLength = calculatePolymeraseReadLength(insertLengths, adapterNucleotideArray.length, startInAdapter, originalInsertLength);
    byte[] noErrorPolymeraseRead = new byte[polymeraseReadLength];
    int polymeraseNucleotideIndex = 0;
    if (insertLengths == null || insertLengths.length == 0) {
      return null;
    }
    CircularArrayList<byte[]> circularList = new CircularArrayList<byte[]>();
    /*
    at the end of each adapter, output a position (exclusive). The position is
    normalized by length of polymerase read to 0~1.
     */
    DecimalFormat formatter = new DecimalFormat("#.####");
    List<String> normalizedAdapterLociStrings = new ArrayList<String>();
    normalizedAdapterLociStrings.add(readName);
    normalizedAdapterLociStrings.add(Integer.toString(polymeraseReadLength));
    //first subread is incomplete, so sequencing or at least high quality region starts in insert
    if (insertLengths[0] < originalInsertLength) {
      for (int i = originalInsertLength - insertLengths[0]; i < originalInsertLength && polymeraseNucleotideIndex < polymeraseReadLength; i++) {
        noErrorPolymeraseRead[polymeraseNucleotideIndex++] = forwardReverseComplement.get(0)[i];
      }
    } else {
      //first subread is complete, so sequencing or at least high quality region starts in adapter
      for (int i = startInAdapter; polymeraseNucleotideIndex < polymeraseReadLength && i < adapterNucleotideArray.length; i++) {
        noErrorPolymeraseRead[polymeraseNucleotideIndex++] = adapterNucleotideArray[i];
      }
      normalizedAdapterLociStrings.add(Integer.toString(polymeraseNucleotideIndex));
      for (int i = 0; i < originalInsertLength && polymeraseNucleotideIndex < polymeraseReadLength; i++) {
        noErrorPolymeraseRead[polymeraseNucleotideIndex++] = forwardReverseComplement.get(0)[i];
      }
    }
    if (polymeraseNucleotideIndex < polymeraseReadLength) {
      circularList.add(adapterNucleotideArray);
      circularList.add(forwardReverseComplement.get(1));
      circularList.add(adapterNucleotideArray);
      circularList.add(forwardReverseComplement.get(0));
      for (byte[] currentSequence : circularList) {
        for (int i = 0; polymeraseNucleotideIndex < polymeraseReadLength && i < currentSequence.length; i++) {
          noErrorPolymeraseRead[polymeraseNucleotideIndex++] = currentSequence[i];
        }
      /*
      if the next sequence has equal length as adapter, then we think
      it is adapter. The assumption here is that the insert sequences
      will have different (larger) lengths.
       */
        if (currentSequence.length == adapterNucleotideArray.length) {
          normalizedAdapterLociStrings.add(Integer.toString(polymeraseNucleotideIndex));
        }
        if (polymeraseNucleotideIndex >= polymeraseReadLength) {
          break;
        }
      }
    }
    normalizedAdapterLociStrings.set(normalizedAdapterLociStrings.size() - 1,
            normalizedAdapterLociStrings.get(normalizedAdapterLociStrings.size() - 1) + "\n");
    return new Pair<byte[], String[]>(noErrorPolymeraseRead, normalizedAdapterLociStrings.toArray(new String[0]));
  }
}
