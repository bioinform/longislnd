package com.bina.hdf5.simulator.pool;

import com.bina.hdf5.h5.pb.PBReadBuffer;
import com.bina.hdf5.simulator.Event;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bayo on 5/10/15.
 */
public class GeneralBCPool extends BaseCallsPool {

    // this might have significat memory overhead

    private List<List<byte[]>> data_;

    public GeneralBCPool(int numKmers, int entryPerKmer) {
        super(numKmers, entryPerKmer);
        data_ = new ArrayList<List<byte[]>>(numKmers_);
        for (int ii = 0; ii < numKmers_; ++ii) {
            data_.add(new ArrayList<byte[]>(entryPerKmer));
        }
    }

    @Override
    public void add(Event ev) throws Exception {
        data_.get(ev.kmer()).add(ev.data_cpy());
    }

    @Override
    public void appendTo(PBReadBuffer buffer, int kmer) throws Exception {
        int draw = 0;
        final byte[] b = data_.get(kmer).get(draw);
        buffer.addLast(b, 0, b.length);
    }
}
