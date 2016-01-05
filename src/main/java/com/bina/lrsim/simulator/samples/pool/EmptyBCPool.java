package com.bina.lrsim.simulator.samples.pool;

import com.bina.lrsim.h5.pb.PBReadBuffer;
import com.bina.lrsim.simulator.Event;

import java.util.Random;

/**
 * Created by bayo on 5/10/15.
 */
public class EmptyBCPool extends BaseCallsPool {
    public EmptyBCPool(int numKmers, int entryPerKmer) {
        super(numKmers, entryPerKmer);
    }

    @Override
    public void appendTo(PBReadBuffer buffer, int kmer, Random gen) throws Exception {
    }

    @Override
    public void add(Event ev) throws Exception {
    }

}
