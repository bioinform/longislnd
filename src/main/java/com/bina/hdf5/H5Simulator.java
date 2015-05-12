package com.bina.hdf5;

import com.bina.hdf5.bioinfo.WeightedReference;
import com.bina.hdf5.simulator.Simulator;
import com.bina.hdf5.simulator.samples.SamplesDrawer;
import org.apache.log4j.Logger;

import java.util.Random;

/**
 * Created by bayo on 5/11/15.
 */
public class H5Simulator {
    private final static Logger log = Logger.getLogger(H5Simulator.class.getName());
    /**
     * create a file of simulated reads based on the given FASTA and model
     * @param args see log.info
     */
    static public void run(String[] args) {
        if(args.length != 6){
            log.info("parameters: out_dir fasta model_prefix total_bases sample_per seed");
            return;
        }
        final String out_dir = args[0];
        final String fasta = args[1];
        final String model_prefix = args[2];
        final int total_bases = Integer.parseInt(args[3]);
        final int sample_per = Integer.parseInt(args[4]);
        final int seed = Integer.parseInt(args[5]);

        try {
            SamplesDrawer samples = new SamplesDrawer(model_prefix,sample_per);
            Simulator sim = new Simulator(new WeightedReference(fasta));
            Random gen = new Random(seed);
            String movie_name = "m000000_000000_11111_cSIMULATED_s0_p0";
            sim.simulate(out_dir,movie_name,0,samples,total_bases,gen);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}