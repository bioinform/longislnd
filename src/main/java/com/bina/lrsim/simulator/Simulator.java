package com.bina.lrsim.simulator;

import java.util.Iterator;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.Pair;
import org.apache.log4j.Logger;

import com.bina.lrsim.bioinfo.Context;
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
  public int simulate(String path,
                      String movie_name,
                      int firsthole,
                      SamplesDrawer drawer,
                      int total_bases,
                      PBSpec spec,
                      RandomGenerator gen) {
    BaxH5Writer writer = new BaxH5Writer(spec);
    PBReadBuffer read = new PBReadBuffer(spec);
    log.info("generating reads");

    for (int num_bases = 0; num_bases < total_bases;) {
      read.clear();
      if (read.size() != 0) {
        log.info("couldn't clear buffer");
        throw new RuntimeException("different lengths!");
      }

      Pair<Integer, Integer> len_score = drawer.getRandomLengthScore(gen);

      for (Iterator<Context> itr = seqGen_.getSequence((int)(len_score.getFirst()*1.2),
                                                       drawer.left_flank(),
                                                       drawer.right_flank(),
                                                       drawer.hp_anchor(),
                                                       gen)
          ; read.size() < len_score.getFirst() && itr.hasNext()
          ;) {
        final Context con = itr.next();
        if (null != con) {
          long[] change_counters = drawer.appendTo(read, con, gen);
          for (int ii = 0; ii < change_counters.length; ++ii) {
            base_counter_[ii] += change_counters[ii];
          }
        }
      }

      writer.addLast(read, len_score.getSecond());
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
