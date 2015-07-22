package com.bina.lrsim.simulator;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.Pair;
import org.apache.log4j.Logger;

import com.bina.lrsim.bioinfo.Context;
import com.bina.lrsim.bioinfo.EnumBP;
import com.bina.lrsim.bioinfo.HPIterator;
import com.bina.lrsim.bioinfo.Heuristics;
import com.bina.lrsim.h5.bax.BaxH5Writer;
import com.bina.lrsim.h5.pb.PBReadBuffer;
import com.bina.lrsim.h5.pb.PBSpec;
import com.bina.lrsim.interfaces.RandomSequenceGenerator;
import com.bina.lrsim.simulator.samples.SamplesDrawer;
import com.bina.lrsim.util.Monitor;

/**
 * Created by bayo on 5/11/15.
 */
public class Simulator {
  private final static Logger log = Logger.getLogger(Simulator.class.getName());
  private final long[] base_counter_ = new long[EnumEvent.values().length];
  private final long[] event_counter_ = new long[EnumEvent.values().length];
  private RandomSequenceGenerator seqGen_;

  /**
   * Constructor
   * 
   * @param seqGen a random sequence generator
   */
  public Simulator(RandomSequenceGenerator seqGen) {
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
  public int simulate(String path, String movie_name, int firsthole, SamplesDrawer drawer, int total_bases, PBSpec spec, RandomGenerator gen) {
    BaxH5Writer writer = new BaxH5Writer(spec);
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

      // draw a sequence according to max insert length, make RC if belt is long enough
      ArrayList<byte[]> fw_rc = new ArrayList<byte[]>(2);
      fw_rc.add(seqGen_.getSequence(max_len, gen));
      if (insert_lengths.length > 1) {
        final byte[] fw = fw_rc.get(0);
        final byte[] rc = new byte[fw.length];
        for (int pos = 0, tgt = fw.length - 1; pos < fw.length; ++pos, --tgt) {
          rc[tgt] = EnumBP.ascii_rc(fw[pos]);
        }
        fw_rc.add(rc);
      }

      ArrayList<Integer> section_ends = new ArrayList<Integer>(2 * insert_lengths.length - 1);
      boolean skipIfShort = false;
      for (int ins_idx = 0; ins_idx < insert_lengths.length; ++ins_idx) {
        final int insert_length = insert_lengths[ins_idx];
        final int begin = ins_idx == 0 ? max_len - insert_length : 0;
        final int end = (ins_idx + 1 == insert_lengths.length) ? insert_length : max_len;
        final boolean isShort = insert_length < Heuristics.SMRT_INSERT_FRACTION * max_len && ins_idx != 0 && ins_idx + 1 != insert_lengths.length;
        if (!isShort || !skipIfShort) {
          if (ins_idx != 0) {
            //prepend with a "perfect" adaptor sequence
            read.addASCIIBases(Heuristics.SMRT_ADAPTOR_STRING, Heuristics.SMRT_ADAPTOR_STRING, Heuristics.SMRT_ADAPTOR_SCORE);
            section_ends.add(read.size());
          }
          for (Iterator<Context> itr = new HPIterator(fw_rc.get(ins_idx % 2), begin, end, drawer.left_flank(), drawer.right_flank(), drawer.hp_anchor()); itr.hasNext();) {
            final Context con = itr.next();
            if (null != con) {
              long[] change_counters = drawer.appendTo(read, con, gen);
              for (int ii = 0; ii < change_counters.length; ++ii) {
                base_counter_[ii] += change_counters[ii];
              }
            }
          }
          section_ends.add(read.size());
          if (isShort) {
            skipIfShort = true;
          }
        }
        if (!isShort) {
          skipIfShort = false;
        }
      }

      writer.addLast(read, section_ends, len_score.getSecond());
      num_bases += read.size();
      if (writer.size() % 10000 == 1) {
        log.info(toString());
      }
    }
    log.info(toString());
    log.info("generated " + writer.size() + " reads.");
    log.info("Memory usage: " + Monitor.PeakMemoryUsage());
    writer.write(path + "/" + movie_name + spec.getSuffix(), movie_name, firsthole);
    log.info("Memory usage: " + Monitor.PeakMemoryUsage());
    return writer.size();
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("simulated statistics\n");
    sb.append(EnumEvent.getPrettyStats(base_counter_));
    sb.append("\n");
    sb.append(EnumEvent.getPrettyStats(event_counter_));
    return sb.toString();
  }
}
