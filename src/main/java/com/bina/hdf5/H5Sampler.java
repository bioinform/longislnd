package com.bina.hdf5;

import com.bina.hdf5.h5.cmp.CmpH5Reader;
import com.bina.hdf5.simulator.samples.SamplesCollector;

import java.util.logging.Logger;

/**
 * Created by bayo on 5/11/15.
 */
public class H5Sampler {
    private final static Logger log = Logger.getLogger(H5Sampler.class.getName());

    /**
     * collect context-specific samples of reference->read edits from an alignment file
     * @param args see log.info
     */
    static public void run(String[] args) {
        if(args.length != 4){
            log.info("parameters: out_prefix in_file left_flank right_flank");
            return;
        }
        String out_prefix = args[0];
        String in_file = args[1];
        int left_flank = Integer.parseInt(args[2]);
        int right_flank = Integer.parseInt(args[3]);


        SamplesCollector collector = null;
        try {
            collector = new SamplesCollector(out_prefix,left_flank,right_flank);
            collector.process(new CmpH5Reader(in_file));
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally{
            if(collector != null){
                log.info(collector.toString());
                collector.close();
            }
        }

    }
}
