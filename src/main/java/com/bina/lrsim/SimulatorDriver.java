package com.bina.lrsim;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import com.bina.lrsim.bioinfo.Heuristics;
import com.bina.lrsim.bioinfo.ReferenceSequenceDrawer;
import com.bina.lrsim.pb.*;
import com.bina.lrsim.simulator.ParallelSimulator;
import com.bina.lrsim.util.ProgramOptions;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.log4j.Logger;

import com.bina.lrsim.simulator.EnumEvent;
import com.bina.lrsim.simulator.samples.SamplesDrawer;
import com.bina.lrsim.util.Monitor;
import org.kohsuke.args4j.Option;

/**
 * Created by bayo on 5/11/15.
 */
public class SimulatorDriver {
  private final static Logger log = Logger.getLogger(SimulatorDriver.class.getName());
  private final static Set<String> VALID_READ_TYPES = new HashSet<>(Arrays.asList("bax", "ccs", "clrbam", "fastq"));

  /**
   * create a file of simulated reads based on the given FASTA and model
   */
  public static void main(String[] args) throws IOException {
    final ModuleOptions po = ProgramOptions.parse(args, ModuleOptions.class);
    if (po == null) {
      System.exit(1);
    }

    final long[] eventsFrequency = po.getEventsFrequency();

    if (!VALID_READ_TYPES.contains(po.readType)) {
      log.error("valid read types: " + StringUtils.join(VALID_READ_TYPES, ", "));
      System.exit(1);
    }

    final Spec spec = Spec.fromReadType(po.readType);

    final ReferenceSequenceDrawer wr = ReferenceSequenceDrawer.Factory(po.sequencingMode, po.fasta);
    if (wr == null) {
      log.error("failed to set up reference drawer");
      System.exit(1);
    }

    log.info("Memory usage: " + Monitor.PeakMemoryUsage());

    final int targetChunk = (int) Math.min(wr.getNonNCount(), 200000000);
    log.info("each file will have ~" + targetChunk + " bases");

    final String moviePrefix = new SimpleDateFormat("'m'yyMMdd'_'HHmmss'_'").format(Calendar.getInstance().getTime());
    final String movieSuffix = "_c" + po.identifier + "_s1_p0";

    final SamplesDrawer.LengthLimits len_limits = new SamplesDrawer.LengthLimits(po.minFragmentLength, po.maxFragmentLength, po.minNumPasses, po.maxNumPasses);
    final SamplesDrawer samples = new SamplesDrawer(po.modelPrefixes.split(","), spec, po.samplePer, eventsFrequency, Heuristics.ARTIFICIAL_CLEAN_INS, len_limits);
    log.info(samples.toString());
    log.info("Memory usage: " + Monitor.PeakMemoryUsage());

    ParallelSimulator.process(wr, po.outDir, moviePrefix, movieSuffix, samples, targetChunk, po.totalBases, spec, new MersenneTwister(po.seed));

    log.info("finished.");
  }

  public static class ModuleOptions extends ProgramOptions {
    @Option(name = "--outDir", required = true, usage = "output directory")
    private String outDir;

    @Option(name = "--identifier", required = true, usage = "output identifier")
    private String identifier;

    @Option(name = "--readType", required = true, usage = "type of output data")
    private String readType;

    @Option(name = "--sequencingMode", required = true, usage = "sequencing mode")
    private String sequencingMode;

    @Option(name = "--fasta", required = true, usage = "reference sequences for simulation")
    private String fasta;

    @Option(name = "--modelPrefixes", required = true, usage = "a list of model prefixes, joined by comma")
    private String modelPrefixes;

    @Option(name = "--totalBases", required = true, usage = "number of bases to simulate")
    private long totalBases;

    @Option(name = "--samplePer", required = true, usage = "number of events per kmer")
    private int samplePer;

    @Option(name = "--seed", required = true, usage = "seed for random number generator")
    private int seed;

    @Option(name = "--minFragmentLength", required = false, usage = "minimum fragment length")
    private int minFragmentLength = 50;

    @Option(name = "--maxFragmentLength", required = false, usage = "maximum fragment length")
    private int maxFragmentLength = Integer.MAX_VALUE;

    @Option(name = "--minNumPasses", required = false, usage = "minimum number of passes")
    private int minNumPasses = 0;

    @Option(name = "--maxNumPasses", required = false, usage = "maxnimum number of passes")
    private int maxNumPasses = Integer.MAX_VALUE;

    @Option(name = "--eventsFrequency", required = false, usage = "custom event frequency")
    private String eventsFrequency = "";
    long [] getEventsFrequency() {
      long[] ret = null;
      if (eventsFrequency.length() < 1) {
        return ret;
      }
      String[] idsm = eventsFrequency.split(":");
      if (idsm.length != EnumEvent.values().length) {
        log.info("event frequency must be a set of integers " + EnumEvent.getListDescription());
        System.exit(1);
      } else {
        ret = new long[EnumEvent.values().length];
        for (int ii = 0; ii < ret.length; ++ii) {
          ret[ii] = Long.parseLong(idsm[ii]);
        }
        log.info("custom event frequencies: " + Arrays.toString(ret));
      }
      return ret;
    }
  }
}
