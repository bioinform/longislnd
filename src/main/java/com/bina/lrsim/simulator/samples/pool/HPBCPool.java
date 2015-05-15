package com.bina.lrsim.simulator.samples.pool;

import com.bina.lrsim.bioinfo.Context;
import com.bina.lrsim.h5.pb.PBReadBuffer;
import com.bina.lrsim.simulator.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by bayo on 5/14/15.
 */
public class HPBCPool extends BaseCallsPool{
    private List<List<List<byte[]>>> data_; // data[kmer][hp_len] contains a pool of byte[] base calls

    public HPBCPool(int numKmers, int entryPerKmer) {
        super(numKmers, entryPerKmer);

        data_ = new ArrayList<List<List<byte[]>>>(numKmers_);
        for (int ii = 0; ii < numKmers_; ++ii) {
            data_.add(new ArrayList<List<byte[]>>((entryPerKmer>0)?entryPerKmer:100));
        }
        // data_[kmer] is non-null, but data_[kmer][len] might not have been allocated at this point
    }

    @Override
    public boolean add(Event ev) throws Exception {
        final int kmer = ev.kmer();
        final int hp_len = ev.hp_len();

        while( data_.get(kmer).size() <= hp_len ) {
            data_.get(kmer).add(null);
        }
        if (null == data_.get(kmer).get(hp_len) ) {
            data_.get(kmer).set(hp_len,new ArrayList<byte[]>());
        }

        data_.get(kmer).get(hp_len).add(ev.data_cpy());

        return true;
    }

    @Override
    public boolean appendTo(PBReadBuffer buffer, Context context, Random gen) throws Exception {
        final int kmer = context.kmer();
        final int hp_len = context.hp_len();
        if (hp_len < data_.get(kmer).size() ) {
            List<byte[]> pool = data_.get(kmer).get(hp_len);
            if( null != pool ) {
                final int draw = gen.nextInt(pool.size());
                final byte[] b = pool.get(draw);
                buffer.addLast(b, 0, b.length);
                return true;
            }

        }
        return false;
    }
}
