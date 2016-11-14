package com.bina.lrsim;

import java.io.File;
import java.io.IOException;
import java.util.EnumSet;
import java.util.Set;

import com.bina.lrsim.util.SuffixFixedFileType;
import com.bina.lrsim.util.ProgramOptions;
;
import org.apache.commons.lang3.StringUtils;
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
public class SamplerDriver {
  private final static Logger log = Logger.getLogger(SamplerDriver.class.getName());

  public static class ModuleOptions extends ProgramOptions {
    private final static Logger log = Logger.getLogger(ModuleOptions.class.getName());
    private final static Set<SuffixFixedFileType.FileType> VALID_READ_TYPES = EnumSet.of(
                                                                          SuffixFixedFileType.FileType.bax,
                                                                          SuffixFixedFileType.FileType.ccs,
                                                                          SuffixFixedFileType.FileType.fastq,
                                                                          SuffixFixedFileType.FileType.clrbam);

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
    private File reference = null;

    @Option(name = "hpAnchor", required = false, hidden = true, usage = "homopolymer anchor")
    private int hpAnchor = 2;

    @Option(name = "--noWrite", required = false, hidden = true, usage = "do not write model")
    private boolean noWrite;

    private EventGroupFactory getGroupFactory() {
      if (readType.equals(SuffixFixedFileType.fastq.type.name())) {
        if (SuffixFixedFileType.fastq.hasLegalOutputSuffix(this.inFile)) {
          if(this.reference == null) {
            log.error("please specify reference file with --reference");
          }
          else if(this.reference.exists()) {
            return new SamReader(new File(this.inFile), this.reference);
          }
          else {
            log.error("missing reference file: --reference "+ this.reference.toString());
          }
        } else {
          log.error("fastq spec is supported only with SAM/BAM input, please contact developer if more is needed");
        }
        return null;
      } else if (readType.equals(SuffixFixedFileType.bax.type.name())) {
        if (SuffixFixedFileType.bax.hasLegalOutputSuffix(this.inFile)) {
          return new CmpH5Reader(this.inFile, Spec.BaxSampleSpec);
        } else {
          log.error("bax spec is supported only with cmp.h5 input, which contains beyond-fastq quality scores, please contact developer if alternatives are needed");
        }
      } else if (readType.equals(SuffixFixedFileType.ccs.type.name())) {
        if (SuffixFixedFileType.ccs.hasLegalOutputSuffix(this.inFile)) {
          return new CmpH5Reader(this.inFile, Spec.CcsSpec);
        } else {
          log.error("ccs spec is supported only with cmp.h5 input, which contains beyond-fastq quality scores, please contact developer if alternatives are needed");
        }
      } else if (readType.equals(SuffixFixedFileType.clrbam.type.name())) {
        if (SuffixFixedFileType.clrbam.hasLegalOutputSuffix(this.inFile)) {
          return new SamReader(new File(this.inFile), this.reference, Spec.ClrBamSpec);
        } else {
          log.error("pbbam spec is supported only with bam input, please contact developer if more is needed");
        }
      } else {
        log.error("valid --readType: " + StringUtils.join(VALID_READ_TYPES, ", "));
      }
      return null;
    }
  }

  /**
   * collect context-specific samples of reference->read edits from an alignment file
   */
  public static void main(String[] args) throws IOException {
    final ModuleOptions po = ProgramOptions.parse(args, ModuleOptions.class);
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
