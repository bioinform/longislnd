package com.bina.lrsim;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.bina.lrsim.pb.h5.cmp.CmpH5Reader;
import com.bina.lrsim.pb.PBBaxSampleSpec;
import com.bina.lrsim.pb.PBCcsSpec;
import com.bina.lrsim.pb.Spec;
import com.bina.lrsim.interfaces.EventGroupFactory;
import com.bina.lrsim.sam.SamReader;
import com.bina.lrsim.simulator.samples.SamplesCollector;

/**
 * Created by bayo on 5/11/15.
 */
public class H5Sampler {
  private final static Logger log = Logger.getLogger(H5Sampler.class.getName());

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
    final String out_prefix = args[0];
    final String in_file = args[1];
    final String read_type = args[2];
    final int left_flank = Integer.parseInt(args[3]);
    final int right_flank = Integer.parseInt(args[4]);
    final int min_length = Integer.parseInt(args[5]);
    final int flank_mask = Integer.parseInt(args[6]);
    final int hp_anchor = 2;

    final Spec spec;

    switch (read_type) {
      case "bax":
        spec = Spec.BaxSampleSpec;
        break;
      case "ccs":
        spec = Spec.CcsSpec;
        break;
      default:
        spec = null;
        log.info("read_type must be bax or ccs");
        log.info(usage);
        System.exit(1);
    }


    EventGroupFactory groupFactory = null;
    final boolean writeEvents;
    if (in_file.endsWith(".sam") && args.length > 7) {
      log.info("sam mode");
      groupFactory = new SamReader(in_file, args[7]);
      writeEvents = false;
    } else {
      groupFactory = new CmpH5Reader(in_file, spec);
      writeEvents = true;
    }

/*
    {
      EventGroupsProcessor inspector = new AdHocProcessor(6, 30, 2);
      inspector.process(groupFactory, min_length, flank_mask);
    }
    */

    try (SamplesCollector collector = new SamplesCollector(out_prefix, left_flank, right_flank, hp_anchor, writeEvents)) {
      collector.process(groupFactory, min_length, flank_mask);
//      log.info("\n" + collector.toString() + "\n");
    }
    log.info("finished");
  }
}
