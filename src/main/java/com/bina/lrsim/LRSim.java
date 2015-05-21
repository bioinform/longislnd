package com.bina.lrsim;

import java.util.Arrays;

/**
 * Created by bayo on 4/30/15.
 */
public class LRSim {

    private static String VERSION = "LRSim " + LRSim.class.getPackage().getImplementationVersion();
    private static String usage = "java -jar LRSim.jar <mode> [parameters] \n"
            + "       mode    -- simulate/sample\n"
            + "       parameters -- see mode description by not specifying parameters\n";

    public static void main(String[] args) {

        if (args.length < 1) {
            System.err.println(VERSION);
            System.err.println(usage);
            System.exit(1);
        }

        final String[] pass_args = Arrays.copyOfRange(args, 1, args.length);


        switch (args[0]) {
            case "simulate": // run simulator
                H5Simulator.main(pass_args);
                break;

            case "sample": //run sampling
                H5Sampler.main(pass_args);
                break;

            default:
                System.err.println(usage);
        }
    }

}
