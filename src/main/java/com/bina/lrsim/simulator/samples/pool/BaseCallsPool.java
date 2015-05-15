package com.bina.lrsim.simulator.samples.pool;

import com.bina.lrsim.bioinfo.Context;
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

    /**
     * constrcutor
     * @param numKmers      number of kmer identifier, typically 1<<(2*num_base)
     * @param entryPerKmer  a hint of maximum number of entries per kmer, <1 means some default behavior
     */
    protected BaseCallsPool(int numKmers, int entryPerKmer) {
        numKmers_ = numKmers;
        entryPerKmer_ = entryPerKmer;
    }

    /**
     * given a sequencing context, append some simulated base calls to the read buffer
     * @param pb       read buffer
     * @param context  sequencing context
     * @param gen      random number generator
     * @return         success or not
     * @throws Exception
     */
    public abstract boolean appendTo(PBReadBuffer pb, Context context, Random gen) throws Exception;

    /**
     * add an event to be drawn from later
     * @param  ev a sequencing event
     * @return event has been added to the pool or not
     * @throws Exception
     */
    public abstract boolean add(Event ev) throws Exception;
}
