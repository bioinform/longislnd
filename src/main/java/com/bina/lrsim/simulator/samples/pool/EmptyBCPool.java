package com.bina.lrsim.simulator.samples.pool;

import com.bina.lrsim.bioinfo.Context;
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
    public boolean appendTo(PBReadBuffer buffer, Context context, Random gen) throws Exception {
        if(context.hp_len() != 1) { throw new Exception("memory compression does not make sense for homopolymer"); }
        return true;
    }

    @Override
    public boolean add(Event ev) throws Exception {
        return false;
    }

}
