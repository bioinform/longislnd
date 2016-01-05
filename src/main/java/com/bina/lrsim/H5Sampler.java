package com.bina.lrsim;

import com.bina.lrsim.h5.cmp.CmpH5Reader;
import com.bina.lrsim.simulator.samples.SamplesCollector;

import org.apache.log4j.Logger;

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
        if(args.length != 6){
            log.info("parameters: out_prefix in_file left_flank right_flank");
            return;
        }
        final String out_prefix = args[0];
        final String in_file = args[1];
        final int left_flank = Integer.parseInt(args[2]);
        final int right_flank = Integer.parseInt(args[3]);
        final int min_length = Integer.parseInt(args[4]);
        final int flank_mask = Integer.parseInt(args[5]);


        SamplesCollector collector = null;
        try {
            collector = new SamplesCollector(out_prefix,left_flank,right_flank);
            collector.process(new CmpH5Reader(in_file),min_length,flank_mask);
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
