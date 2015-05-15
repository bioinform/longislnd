package com.bina.lrsim.simulator.samples.pool;

import com.bina.lrsim.bioinfo.Context;
import com.bina.lrsim.h5.pb.EnumDat;
import com.bina.lrsim.h5.pb.PBReadBuffer;
import com.bina.lrsim.simulator.Event;
import org.apache.log4j.Logger;

import java.util.Random;

/**
 * Created by bayo on 5/10/15.
 */
public class SingleBCPool extends BaseCallsPool {
    private final static Logger log = Logger.getLogger(SingleBCPool.class.getName());
    private byte[] data_;
    private int[] end_;

    private int begin(int kmer) {
        return kmer*entryPerKmer_*BYTE_PER_BC;
    }

    public SingleBCPool(int numKmers, int entryPerKmer) {
        super(numKmers, entryPerKmer);
        data_ = new byte[begin(numKmers)];
        end_ = new int[numKmers_];
        for (int kk = 0; kk < numKmers_; ++kk) {
            end_[kk] = begin(kk);
        }
    }

    @Override
    public boolean add(Event ev) throws Exception {
        if(end_[ev.kmer()] - begin(ev.kmer()) < entryPerKmer_*BYTE_PER_BC ) {
            int shift = end_[ev.kmer()];
            if (ev.size() != 1) {
                throw new Exception("event is too large");
            }
            for (EnumDat e : EnumDat.getBaxSet()) {
                data_[shift + e.value()] = ev.get(0, e);
            }
            end_[ev.kmer()] += BYTE_PER_BC;
            return true;
        }
        return false;
    }

    @Override
    public boolean appendTo(PBReadBuffer buffer, Context context, Random gen) throws Exception {
        if(context.hp_len() != 1) { throw new Exception("memory compression does not make sense for homopolymer"); }
        final int base = begin(context.kmer());
        if(base == end_[context.kmer()]) throw new Exception("no sample");
        final int shift = base + gen.nextInt((end_[context.kmer()]-base)/BYTE_PER_BC) * BYTE_PER_BC;
        buffer.addLast(data_, shift, shift + BYTE_PER_BC);
        return true;
    }
}
