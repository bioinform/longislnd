package com.bina.hdf5.simulator.pool;

import com.bina.hdf5.h5.pb.EnumDat;
import com.bina.hdf5.h5.pb.PBReadBuffer;
import com.bina.hdf5.simulator.Event;

/**
 * Created by bayo on 5/10/15.
 */
public class SingleBCPool extends BaseCallsPool {
    private byte[] data_;
    private int[] end_;

    public SingleBCPool(int numKmers, int entryPerKmer) {
        super(numKmers, entryPerKmer);
        data_ = new byte[numKmers_ * entryPerKmer_ * BYTE_PER_BC];
        end_ = new int[numKmers_];
        for (int kk = 0; kk < numKmers_; ++kk) {
            end_[kk] = kk * entryPerKmer_ * BYTE_PER_BC;
        }
    }

    @Override
    public void add(Event ev) throws Exception {
        int shift = end_[ev.kmer()];
        if (ev.size() != 1) {
            throw new Exception("event is too large");
        }
        for (EnumDat e : EnumDat.getBaxSet()) {
            data_[shift + e.value()] = ev.get(0, e);
        }
        end_[ev.kmer()] += BYTE_PER_BC;
    }

    @Override
    public void appendTo(PBReadBuffer buffer, int kmer) throws Exception {
        int shift = kmer * entryPerKmer_ * BYTE_PER_BC;
        buffer.addLast(data_, shift, shift + BYTE_PER_BC);
    }
}
