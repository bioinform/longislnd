package com.bina.lrsim;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import com.bina.lrsim.bioinfo.Heuristics;
import com.bina.lrsim.bioinfo.ReferenceSequenceDrawer;
import com.bina.lrsim.pb.*;
import com.bina.lrsim.simulator.ParallelSimulator;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.log4j.Logger;

import com.bina.lrsim.simulator.EnumEvent;
import com.bina.lrsim.simulator.samples.SamplesDrawer;
import com.bina.lrsim.util.Monitor;

/**
 * Created by bayo on 5/11/15.
 */
public class SimulatorDriver {
  private final static Logger log = Logger.getLogger(SimulatorDriver.class.getName());
  private final static String usage = "parameters: out_dir movie_id read_type sequencing_mode fasta model_prefix total_bases sample_per seed [min fragment length ] [max fragment length] [min passes] [max passes] [" + EnumEvent.getListDescription() + "]";
  private final static Set<String> VALID_READ_TYPES = new HashSet<>(Arrays.asList("bax", "ccs", "clrbam", "fastq"));

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
    final String outDir = args[0];
    final String identifier = args[1].trim();
    final String readType = args[2];
    final String sequencingMode = args[3];
    final String fasta = args[4];
    final String modelPrefixes = args[5];
    final long totalBases = Long.parseLong(args[6]);
    final int samplePer = Integer.parseInt(args[7]);
    final int seed = Integer.parseInt(args[8]);

    final int minFragmentLength = (args.length > 9) ? Integer.parseInt(args[9]) : 0;

    final int maxFragmentLength = (args.length > 10) ? Integer.parseInt(args[10]) : Integer.MAX_VALUE;
    if (maxFragmentLength < 1) {
      log.info("maximum fragment length cannot be non-positive");
      System.exit(1);
    }

    final int minNumPasses = (args.length > 11) ? Integer.parseInt(args[11]) : 0;

    final int maxNumPasses = (args.length > 12) ? Integer.parseInt(args[12]) : Integer.MAX_VALUE;

    long[] eventsFrequency = null;
    if (args.length > 13) {
      String[] idsm = args[13].split(":");
      if (idsm.length != EnumEvent.values().length) {
        log.info(usage);
        log.info("event frequency must be a set of integers " + EnumEvent.getListDescription());
        System.exit(1);
      } else {
        eventsFrequency = new long[EnumEvent.values().length];
        for (int ii = 0; ii < eventsFrequency.length; ++ii) {
          eventsFrequency[ii] = Long.parseLong(idsm[ii]);
        }
        log.info("custom event frequencies: " + Arrays.toString(eventsFrequency));
      }
    }

    if (!VALID_READ_TYPES.contains(readType)) {
      log.error("read_type must be fastq, clrbam, bax or ccs");
      log.info(usage);
      System.exit(1);
    }

    final Spec spec = Spec.fromReadType(readType);

    final ReferenceSequenceDrawer wr = ReferenceSequenceDrawer.Factory(sequencingMode, fasta);
    if (wr == null) System.exit(1);

    log.info("Memory usage: " + Monitor.PeakMemoryUsage());

    final int targetChunk = (int) Math.min(wr.getNonNCount(), 200000000);
    log.info("each file will have ~" + targetChunk + " bases");

    final String moviePrefix = new SimpleDateFormat("'m'yyMMdd'_'HHmmss'_'").format(Calendar.getInstance().getTime());
    final String movieSuffix = "_c" + identifier + "_s1_p0";

    final SamplesDrawer.LengthLimits len_limits = new SamplesDrawer.LengthLimits(minFragmentLength, maxFragmentLength, minNumPasses, maxNumPasses);
    final SamplesDrawer samples = new SamplesDrawer(modelPrefixes.split(","), spec, samplePer, eventsFrequency, Heuristics.ARTIFICIAL_CLEAN_INS, len_limits);
    log.info(samples.toString());
    log.info("Memory usage: " + Monitor.PeakMemoryUsage());

    ParallelSimulator.process(wr, outDir, moviePrefix, movieSuffix, samples, targetChunk, totalBases, spec, new MersenneTwister(seed));

    log.info("finished.");
  }
}
