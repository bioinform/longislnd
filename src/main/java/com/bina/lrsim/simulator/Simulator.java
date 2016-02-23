package com.bina.lrsim.simulator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import com.bina.lrsim.bioinfo.*;
import com.bina.lrsim.pb.ReadsWriter;
import com.bina.lrsim.pb.ReadsWriterFactory;
import com.bina.lrsim.simulator.samples.pool.AppendState;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.Pair;
import org.apache.log4j.Logger;

import com.bina.lrsim.pb.PBReadBuffer;
import com.bina.lrsim.pb.PBSpec;
import com.bina.lrsim.interfaces.RandomFragmentGenerator;
import com.bina.lrsim.simulator.samples.SamplesDrawer;
import com.bina.lrsim.util.Monitor;

/**
 * Created by bayo on 5/11/15.
 */
public class Simulator {
  private final static Logger log = Logger.getLogger(Simulator.class.getName());
  private final long[] base_counter_ = new long[EnumEvent.values().length];
  private final long[] event_counter_ = new long[EnumEvent.values().length];
  final ConcurrentHashMap<String, AtomicLong> name_counter = new ConcurrentHashMap<String, AtomicLong>();
  private RandomFragmentGenerator seqGen_;

  /**
   * Constructor
   * 
   * @param seqGen a random sequence generator
   */
  public Simulator(RandomFragmentGenerator seqGen) {
    seqGen_ = seqGen;
  }

  /**
   * Generate a pacbio h5 file containing reads simulated according to the sampler and reference
   * 
   * @param path output path
   * @param movie_name movie name
   * @param firsthole first hole producing sequence
   * @param drawer an instance from which samples can be drawn
   * @param total_bases minimum number of bp to generate
   * @param gen random number generator
   */
  public int simulate(final String path, final String movie_name, final int firsthole, SamplesDrawer drawer, int total_bases, final PBSpec spec, RandomGenerator gen) throws IOException {
    try (ReadsWriter writer = ReadsWriterFactory.makeWriter(spec, path + "/" + movie_name + spec.getSuffix(), movie_name, firsthole)) {
      PBReadBuffer read = new PBReadBuffer(spec);
      log.info("generating reads");

      for (int num_bases = 0; num_bases < total_bases;) {
        read.clear();
        if (read.size() != 0) {
          log.info("couldn't clear buffer");
          throw new RuntimeException("different lengths!");
        }

        // draw a list of smrt belts
        Pair<int[], Integer> len_score = drawer.getRandomLengthScore(gen);
        final int[] insert_lengths = len_score.getFirst();
        int max_len = -1;
        for (int len : insert_lengths) {
          if (len > max_len) max_len = len;
        }

        final Fragment fragment = seqGen_.getFragment(max_len, gen);
        final Locus locus = fragment.getLocus();
        name_counter.putIfAbsent(locus.getChrom(), new AtomicLong((long) 0));
        name_counter.get(locus.getChrom()).incrementAndGet();
        final byte[] sequence = fragment.getSeq();
        // correct insert lengths if the drawn fragment is shorter, the fractional change might not be realistic, but it avoids crazy coverage in fragment mode
        if (sequence.length < max_len) {
          final float ratio = (float) sequence.length / (float) max_len;
          max_len = sequence.length;
          for (int ii = 0; ii < insert_lengths.length; ++ii) {
            insert_lengths[ii] *= ratio;
          }
        }

        // draw a sequence according to max insert length, make RC if belt is long enough
        ArrayList<byte[]> fw_rc = new ArrayList<byte[]>(2);
        fw_rc.add(sequence);
        if (insert_lengths.length > 1) {
          final byte[] fw = fw_rc.get(0);
          final byte[] rc = new byte[fw.length];
          for (int pos = 0, tgt = fw.length - 1; pos < fw.length; ++pos, --tgt) {
            rc[tgt] = EnumBP.ascii_rc(fw[pos]);
          }
          fw_rc.add(rc);
        }

        List<Locus> clr_loci = new ArrayList<Locus>();

        ArrayList<Integer> section_ends = new ArrayList<Integer>(2 * insert_lengths.length - 1);
        boolean skipIfShort = false;
        for (int ins_idx = 0; ins_idx < insert_lengths.length; ++ins_idx) {
          final int insert_length = insert_lengths[ins_idx];
          final boolean first_clr = ins_idx == 0;
          final boolean last_clr = ins_idx + 1 == insert_lengths.length;
          final int begin = first_clr ? max_len - insert_length : 0;
          final int end = last_clr ? insert_length : max_len;
          final boolean isShort = insert_length < Heuristics.SMRT_INSERT_FRACTION * max_len && !first_clr && !last_clr;
          if (!isShort || !skipIfShort) {
            if (!first_clr) {
              // prepend with a "perfect" adaptor sequence
              read.addASCIIBases(Heuristics.SMRT_ADAPTOR_STRING, Heuristics.SMRT_ADAPTOR_STRING, Heuristics.SMRT_ADAPTOR_SCORE);
              section_ends.add(read.size());
            }
            AppendState deletion = null;
            for (Iterator<Context> itr = new HPIterator(fw_rc.get(ins_idx % 2), begin, end, drawer.left_flank(), drawer.right_flank(), drawer.hp_anchor()); itr.hasNext();) {
              final Context con = itr.next();
              if (null != con) {
                deletion = drawer.appendTo(read, con, deletion, gen, base_counter_);
              }
            }

            //per clr coordinates
            int clr_begin = locus.getBegin0();
            int clr_end = locus.getEnd0();
            final boolean read_is_rc = (ins_idx % 2 == 1) ^ locus.isRc();
            if (first_clr) {
              if(read_is_rc)
                clr_end = locus.getBegin0() + insert_length;
              else
                clr_begin = locus.getEnd0() - insert_length;
            }
            if (last_clr) {
              if(read_is_rc)
                clr_begin = locus.getEnd0() - insert_length;
              else
                clr_end = locus.getBegin0() + insert_length;
            }
            clr_loci.add(new Locus(locus.getChrom(), clr_begin, clr_end, read_is_rc));
            section_ends.add(read.size());
            if (isShort) {
              skipIfShort = true;
            }
          }
          if (!isShort) {
            skipIfShort = false;
          }
        }

        writer.addLast(read, section_ends, len_score.getSecond(), locus, clr_loci);
        num_bases += read.size();
        if (writer.size() % 10000 == 1) {
          log.info(toString());
        }
      }
      log.info(toString());
      log.info("generated " + writer.size() + " reads.");
      log.info("Memory usage: " + Monitor.PeakMemoryUsage());
      log.info("Memory usage: " + Monitor.PeakMemoryUsage());
      return writer.size();
    }
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("simulated statistics\n");
    sb.append(EnumEvent.getPrettyStats(base_counter_));
    sb.append("\n");
    sb.append(EnumEvent.getPrettyStats(event_counter_));
    sb.append("\n");
    for (ConcurrentHashMap.Entry<String, AtomicLong> entry : name_counter.entrySet()) {
      sb.append(entry.getKey());
      sb.append(" ");
      sb.append(entry.getValue().get());
      sb.append("\n");
    }
    return sb.toString();
  }
}
