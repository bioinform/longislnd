package com.bina.lrsim;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import htsjdk.samtools.util.IOUtil;
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
  private final static Set<String> VALID_READ_TYPES = new HashSet<>(Arrays.asList("bax", "ccs"));
  private final static String usage = "parameters: out_prefix in_file read_type left_flank right_flank min_length flank_mask";

  /**
   * collect context-specific samples of reference->read edits from an alignment file
   * 
   * @param args see log.info
   */
  public static void main(String[] args) throws IOException {
    if (args.length < 7) {
      log.info(usage);
      System.exit(1);
    }
    final String outPrefix = args[0];
    final String inFile = args[1];
    final String readType = args[2];
    final int leftFlank = Integer.parseInt(args[3]);
    final int rightFlank = Integer.parseInt(args[4]);
    final int minLength = Integer.parseInt(args[5]);
    final int flankMask = Integer.parseInt(args[6]);
    final int hpAnchor = 2;

    if (!VALID_READ_TYPES.contains(readType)) {
      log.error("read_type must be bax or ccs");
      log.info(usage);
      System.exit(1);
    }

    final Spec spec = readType.equals("bax") ? Spec.BaxSampleSpec : Spec.CcsSpec;

    EventGroupFactory groupFactory = null;
    final boolean writeEvents;
    if (inFile.endsWith(IOUtil.SAM_FILE_EXTENSION) && args.length > 7) {
      log.info("sam mode");
      groupFactory = new SamReader(inFile, args[7]);
      writeEvents = false;
    } else {
      groupFactory = new CmpH5Reader(inFile, spec);
      writeEvents = true;
    }

/*
    {
      EventGroupsProcessor inspector = new AdHocProcessor(6, 30, 2);
      inspector.process(groupFactory, min_length, flank_mask);
    }
    */

    try (SamplesCollector collector = new SamplesCollector(outPrefix, leftFlank, rightFlank, hpAnchor, writeEvents)) {
      collector.process(groupFactory, minLength, flankMask);
//      log.info("\n" + collector.toString() + "\n");
    }
    log.info("finished");
  }
}
