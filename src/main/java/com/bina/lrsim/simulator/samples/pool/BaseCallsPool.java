package com.bina.lrsim.simulator.samples.pool;

import com.bina.lrsim.h5.pb.EnumDat;
import com.bina.lrsim.h5.pb.PBReadBuffer;
import com.bina.lrsim.simulator.Event;

import java.util.Random;

/**
 * Created by bayo on 5/10/15.
 */
public abstract class BaseCallsPool {

    protected static final int BYTE_PER_BC = EnumDat.getBaxSet().size();
    protected int entryPerKmer_;
    protected int numKmers_;

    protected BaseCallsPool(int numKmers, int entryPerKmer) {
        numKmers_ = numKmers;
        entryPerKmer_ = entryPerKmer;
    }

    public abstract void appendTo(PBReadBuffer pb, int kmer, Random gen) throws Exception;

    public abstract boolean add(Event ev) throws Exception;
}
