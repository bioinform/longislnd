package com.bina.lrsim;

import java.io.IOException;
import java.util.Arrays;

/**
 * Created by bayo on 4/30/15.
 */
public class LRSim {

  private static String VERSION = "LRSim " + LRSim.class.getPackage().getImplementationVersion();
  private static String usage = "java -jar LRSim.jar <mode> [parameters] \n" + "       mode    -- simulate/sample/region\n" + "       parameters -- see mode description by not specifying parameters\n";

  public static void main(String[] args) throws IOException {

    if (args.length < 1) {
      System.err.println(VERSION);
      System.err.println(usage);
      System.exit(1);
    }

    final String[] pass_args = Arrays.copyOfRange(args, 1, args.length);


    switch (args[0]) {
      case "simulate": // run simulator
        SimulatorDriver.main(pass_args);
        break;

      case "sample": // run sampling
        H5Sampler.main(pass_args);
        break;

      case "region": // run sampling of regions
        H5RegionSampler.main(pass_args);
        break;

      case "h5fastq": // h5 to fastq conversion
        H5Fastq.main(pass_args);
        break;

      case "fastqh5": // fastq to h5 conversion
        FastqH5.main(pass_args);
        break;

      case "samh5": // sam to h5 conversion
        SamH5.main(pass_args);
        break;

      case "length": // length inspection
        LengthInspector.main(pass_args);
        break;

      default:
        System.err.println(usage);
    }
  }

}
