package com.bina.lrsim;

import java.io.IOException;
import java.util.Arrays;

import org.apache.log4j.Logger;

import com.bina.lrsim.pb.PBClrBamSpec;
import com.bina.lrsim.simulator.samples.SamplesDrawer;

/**
 * Created by bayo on 5/11/15.
 */
public class LengthInspector {
  private final static Logger log = Logger.getLogger(LengthInspector.class.getName());
  private final static String usage = "parameters: model_prefix ";

  /**
   * check lengths
   * 
   * @param args see log.info
   */
  public static void main(String[] args) throws IOException {
    if (args.length < 1) {
      log.info(usage);
      System.exit(1);
    }
    final String model_prefixes = args[0];

    final SamplesDrawer.LengthLimits len_limits = new SamplesDrawer.LengthLimits(0, Integer.MAX_VALUE, 0, Integer.MAX_VALUE);
    final SamplesDrawer samples = new SamplesDrawer(model_prefixes.split(","), new PBClrBamSpec(), 0, null, false, len_limits);
    for(int ii = 0; ii < samples.getLengthSize(); ++ii) {
      log.info("entry: " + Arrays.toString(samples.getLength(ii)));
    }
  }

}
