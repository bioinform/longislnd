package com.bina.lrsim;

import com.bina.lrsim.bioinfo.WeightedReference;
import com.bina.lrsim.simulator.Simulator;
import com.bina.lrsim.simulator.samples.SamplesDrawer;
import com.bina.lrsim.util.Monitor;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.log4j.Logger;

import java.io.IOException;

/**
 * Created by bayo on 5/11/15.
 */
public class H5Simulator {
    private final static Logger log = Logger.getLogger(H5Simulator.class.getName());

    /**
     * create a file of simulated reads based on the given FASTA and model
     *
     * @param args see log.info
     */
    public static void main(String[] args) throws IOException {
        if (args.length != 6) {
            log.info("parameters: out_dir fasta model_prefix total_bases sample_per seed");
            System.exit(1);
        }
        final String out_dir = args[0];
        final String fasta = args[1];
        final String model_prefixes = args[2];
        final int total_bases = Integer.parseInt(args[3]);
        final int sample_per = Integer.parseInt(args[4]);
        final int seed = Integer.parseInt(args[5]);


        final SamplesDrawer samples = new SamplesDrawer(model_prefixes.split(","), sample_per);

        log.info("Memory usage: " + Monitor.PeakMemoryUsage());

        final Simulator sim = new Simulator(new WeightedReference(fasta));
        log.info("Memory usage: " + Monitor.PeakMemoryUsage());

        final RandomGenerator gen = new org.apache.commons.math3.random.MersenneTwister(seed);
        log.info("Memory usage: " + Monitor.PeakMemoryUsage());

        final String movie_name = "m000000_000000_11111_cSIMULATED_s0_p0";
        sim.simulate(out_dir, movie_name, 0, samples, total_bases, gen);

        log.info("finished.");
    }

}