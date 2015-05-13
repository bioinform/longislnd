package com.bina.lrsim.simulator.samples.pool;

import com.bina.lrsim.h5.pb.PBReadBuffer;
import com.bina.lrsim.simulator.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
            data_.add(new ArrayList<byte[]>((entryPerKmer>0)?entryPerKmer:100));
        }
    }

    @Override
    public boolean add(Event ev) throws Exception {
        if(entryPerKmer_ < 0 || data_.get(ev.kmer()).size() < entryPerKmer_) {
            data_.get(ev.kmer()).add(ev.data_cpy());
            return true;
        }
        return false;
    }

    @Override
    public void appendTo(PBReadBuffer buffer, int kmer, Random gen) throws Exception {
        int draw = gen.nextInt(data_.get(kmer).size());
        final byte[] b = data_.get(kmer).get(draw);
        buffer.addLast(b, 0, b.length);
    }
}
