package com.bina.lrsim;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import htsjdk.samtools.util.IOUtil;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.apache.log4j.Logger;

import com.bina.lrsim.pb.h5.cmp.CmpH5Reader;
import com.bina.lrsim.pb.Spec;
import com.bina.lrsim.interfaces.EventGroupFactory;
import com.bina.lrsim.sam.SamReader;
import com.bina.lrsim.simulator.samples.SamplesCollector;

/**
 * Created by bayo on 5/11/15.
 */
public class H5Sampler {
  private final static Logger log = Logger.getLogger(H5Sampler.class.getName());

  private static class ProgramOptions {
    private final static Logger log = Logger.getLogger(ProgramOptions.class.getName());
    private final static Set<String> VALID_READ_TYPES = new HashSet<>(Arrays.asList("bax", "ccs", "fastq"));

    @Option(name = "--help", aliases = "-h", usage = "print help message")
    private boolean help;

    @Option(name = "--outPrefix", required = true, usage = "prefix of output model files")
    private String outPrefix;

    @Option(name = "--inFile", required = true, usage = "input file name")
    private String inFile;

    @Option(name = "--readType", required = true, usage = "type of input data")
    private String readType;

    @Option(name = "--leftFlank", required = true, usage = "number of bp on the left flank")
    private int leftFlank;

    @Option(name = "--rightFlank", required = true, usage = "number of bp on the right flank")
    private int rightFlank;

    @Option(name = "--minLength", required = true, usage = "minimum read length")
    private int minLength;

    @Option(name = "--flankMask", required = true, usage = "discard this many bp from the beginning and end of the read")
    private int flankMask;

    @Option(name = "--reference", required = false, usage = "path to reference file")
    private String reference = "";

    @Option(name = "hpAnchor", required = false, hidden = true, usage = "homopolymer anchor")
    private int hpAnchor = 2;

    @Option(name = "--noWrite", required = false, hidden = true, usage = "do not write model")
    private boolean noWrite;

    EventGroupFactory getGroupFactory() {
      if (readType.equals("fastq")) {
        if (this.inFile.endsWith(IOUtil.SAM_FILE_EXTENSION)) {
          return new SamReader(this.inFile, this.reference);
        } else {
          log.error("fastq spec is supported only with SAM/BAM input, please contact developer if more is needed");
          return null;
        }
      } else if (readType.equals("bax")) {
        if (this.inFile.endsWith("cmp.h5")) {
          return new CmpH5Reader(this.inFile, Spec.BaxSampleSpec);
        } else {
          log.error("bax spec is supported only with cmp.h5 input, which contains beyond-fastq quality scores, please contact developer if alternatives are needed");
          return null;
        }
      } else if (readType.equals("ccs")) {
        if (this.inFile.endsWith("cmp.h5")) {
          log.error("bax spec is supported only with cmp.h5 input, which contains beyond-fastq quality scores, please contact developer if alternatives are needed");
          return new CmpH5Reader(this.inFile, Spec.CcsSpec);
        } else {
          return null;
        }
      } else {
        log.error("valid --readType: " + StringUtils.join(VALID_READ_TYPES, ", "));
        return null;
      }
    }

    private ProgramOptions() {}

    static private ProgramOptions parse(String[] args) {
      ProgramOptions ret = new ProgramOptions();
      CmdLineParser parser = new CmdLineParser(ret);
      try {
        parser.parseArgument(args);
      } catch (CmdLineException e) {
        log.error(e.getMessage());
        ret.help = true;
      }
      if (ret.help) {
        System.err.println("parameters for " + H5Sampler.class.getName() + " module");
        parser.printUsage(System.err);
        return null;
      } else {
        return ret;
      }
    }
  }

  /**
   * collect context-specific samples of reference->read edits from an alignment file
   */
  public static void main(String[] args) throws IOException {
    final ProgramOptions po = ProgramOptions.parse(args);
    if (po == null) {
      System.exit(1);
    }

    final EventGroupFactory groupFactory = po.getGroupFactory();
    if (groupFactory == null) {
      System.exit(1);
    }

/*
    {
      EventGroupsProcessor inspector = new AdHocProcessor(6, 30, 2);
      inspector.process(groupFactory, min_length, flank_mask);
    }
    */

    try (SamplesCollector collector = new SamplesCollector(po.outPrefix, po.leftFlank, po.rightFlank, po.hpAnchor, !po.noWrite)) {
      collector.process(groupFactory, po.minLength, po.flankMask);
    }
    log.info("finished");
  }
}
