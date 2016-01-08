package com.bina.lrsim;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Arrays;

import com.bina.lrsim.bioinfo.Heuristics;
import com.bina.lrsim.bioinfo.ReferenceSequenceDrawer;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.log4j.Logger;

import com.bina.lrsim.pb.PBBaxSpec;
import com.bina.lrsim.pb.PBCcsSpec;
import com.bina.lrsim.pb.PBSpec;
import com.bina.lrsim.simulator.EnumEvent;
import com.bina.lrsim.simulator.Simulator;
import com.bina.lrsim.simulator.samples.SamplesDrawer;
import com.bina.lrsim.util.Monitor;

/**
 * Created by bayo on 5/11/15.
 */
public class H5Simulator {
  private final static Logger log = Logger.getLogger(H5Simulator.class.getName());
  private final static String usage = "parameters: out_dir movie_id read_type sequencing_mode fasta model_prefix total_bases sample_per seed [min fragment length ] [max fragment length] [min passes] [max passes] [" + EnumEvent.getListDescription() + "]";

  /**
   * create a file of simulated reads based on the given FASTA and model
   * 
   * @param args see log.info
   */
  public static void main(String[] args) throws IOException {
    if (args.length < 9) {
      log.info(usage);
      System.exit(1);
    }
    final String out_dir = args[0];
    final String identifier = args[1].trim();
    final String read_type = args[2];
    final String sequencing_mode = args[3];
    final String fasta = args[4];
    final String model_prefixes = args[5];
    final long total_bases = Long.parseLong(args[6]);
    final int sample_per = Integer.parseInt(args[7]);
    final int seed = Integer.parseInt(args[8]);

    final int min_fragment_length = (args.length > 9) ? Integer.parseInt(args[9]) : 0;

    final int max_fragment_length = (args.length > 10) ? Integer.parseInt(args[10]) : Integer.MAX_VALUE;
    if (max_fragment_length < 1) {
      log.info("maximum fragment length cannot be non-positive");
      System.exit(1);
    }

    final int min_num_passes = (args.length > 11) ? Integer.parseInt(args[11]) : 0;

    final int max_num_passes = (args.length > 12) ? Integer.parseInt(args[12]) : Integer.MAX_VALUE;

    long[] events_frequency = null;
    if (args.length > 13) {
      String[] idsm = args[13].split(":");
      if (idsm.length != EnumEvent.values().length) {
        log.info(usage);
        log.info("event frequency must be a set of integers " + EnumEvent.getListDescription());
        System.exit(1);
      } else {
        events_frequency = new long[EnumEvent.values().length];
        for (int ii = 0; ii < events_frequency.length; ++ii) {
          events_frequency[ii] = Long.parseLong(idsm[ii]);
        }
        log.info("custom event frequencies: " + Arrays.toString(events_frequency));
      }
    }

    final PBSpec spec;

    switch (read_type) {
      case "bax":
        spec = new PBBaxSpec();
        break;
      case "ccs":
        spec = new PBCcsSpec();
        break;
      default:
        spec = null;
        log.info("read_type must be bax or ccs");
        log.info(usage);
        System.exit(1);
    }


    final ReferenceSequenceDrawer wr = ReferenceSequenceDrawer.Factory(sequencing_mode, fasta);
    if (wr == null) System.exit(1);

    final Simulator sim = new Simulator(wr);
    log.info("Memory usage: " + Monitor.PeakMemoryUsage());

    final RandomGenerator gen = new org.apache.commons.math3.random.MersenneTwister(seed);
    log.info("Memory usage: " + Monitor.PeakMemoryUsage());

    int current_file_index = 0;
    int simulated_reads = 0;
    final int target_chunk = (int) Math.min(wr.num_non_n(), 200000000);
    log.info("each file will have ~" + target_chunk + " bases");

    final String movie_prefix = new SimpleDateFormat("'m'yyMMdd'_'HHmmss'_'").format(Calendar.getInstance().getTime());

    final SamplesDrawer.LengthLimits len_limits = new SamplesDrawer.LengthLimits(min_fragment_length, max_fragment_length, min_num_passes, max_num_passes);
    final SamplesDrawer samples = new SamplesDrawer(model_prefixes.split(","), spec, sample_per, events_frequency, Heuristics.ARTIFICIAL_CLEAN_INS, len_limits);
    log.info("Memory usage: " + Monitor.PeakMemoryUsage());

    // the following can be parallelized
    for (long simulated_bases = 0; simulated_bases <= total_bases; ++current_file_index) {
      final String movie_name = movie_prefix + String.format("%05d", current_file_index) + "_c" + identifier + "_s1_p0";
      final int target = (int) Math.min(target_chunk, Math.max(0, total_bases - simulated_bases));
      log.info("simulating roughly " + target_chunk + " for " + movie_name);
      simulated_reads += sim.simulate(out_dir, movie_name, simulated_reads, samples, target, spec, gen);
      log.info("total number of reads is " + simulated_reads);
      simulated_bases += target + 1;
    }


    log.info("finished.");
  }

}
