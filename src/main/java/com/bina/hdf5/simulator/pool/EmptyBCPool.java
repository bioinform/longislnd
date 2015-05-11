package com.bina.hdf5.simulator.pool;

import com.bina.hdf5.h5.pb.PBReadBuffer;
import com.bina.hdf5.simulator.Event;

/**
 * Created by bayo on 5/10/15.
 */
public class EmptyBCPool extends BaseCallsPool {
    public EmptyBCPool(int numKmers, int entryPerKmer) {
        super(numKmers, entryPerKmer);
    }

    @Override
    public void appendTo(PBReadBuffer buffer, int kmer) throws Exception {
    }

    @Override
    public void add(Event ev) throws Exception {
    }

}
