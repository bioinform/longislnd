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
        final long total_bases = Long.parseLong(args[3]);
        final int sample_per = Integer.parseInt(args[4]);
        final int seed = Integer.parseInt(args[5]);


        final SamplesDrawer samples = new SamplesDrawer(model_prefixes.split(","), sample_per);

        log.info("Memory usage: " + Monitor.PeakMemoryUsage());

        final Simulator sim = new Simulator(new WeightedReference(fasta));
        log.info("Memory usage: " + Monitor.PeakMemoryUsage());

        final RandomGenerator gen = new org.apache.commons.math3.random.MersenneTwister(seed);
        log.info("Memory usage: " + Monitor.PeakMemoryUsage());

        int current_file_index = 0;
        int simulated_reads = 0;
        final int target_number_of_bases = 200000000;
        // the following can be parallelized
        for( long simulated_bases = 0; simulated_bases < total_bases; ++current_file_index, simulated_bases+=target_number_of_bases) {
            final String movie_name = "m000000_000000_"
                    + String.format("%05d", current_file_index)
                    + "_cFromLRSim_s1_p0";
            log.info("simulating roughly " + target_number_of_bases + " for " + movie_name);
            simulated_reads += sim.simulate(out_dir, movie_name, simulated_reads, samples, target_number_of_bases, gen);
            log.info("total number of reads is " + simulated_reads);
        }


        log.info("finished.");
    }

}