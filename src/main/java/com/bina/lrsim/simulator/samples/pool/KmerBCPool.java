package com.bina.lrsim.simulator.samples.pool;

import com.bina.lrsim.bioinfo.Context;
import com.bina.lrsim.h5.pb.PBReadBuffer;
import com.bina.lrsim.simulator.Event;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bayo on 5/10/15.
 */
public class KmerBCPool extends BaseCallsPool {

  // this might have significat memory overhead

  private List<List<byte[]>> data_;

  public KmerBCPool(int numKmers, int entryPerKmer) {
    super(numKmers, entryPerKmer);
    data_ = new ArrayList<List<byte[]>>(numKmers_);
    for (int ii = 0; ii < numKmers_; ++ii) {
      data_.add(new ArrayList<byte[]>((entryPerKmer > 0) ? entryPerKmer : 100));
    }
  }

  @Override
  public boolean add(Event ev) {
    if (entryPerKmer_ < 0 || data_.get(ev.kmer()).size() < entryPerKmer_) {
      data_.get(ev.kmer()).add(ev.data_cpy());
      return true;
    }
    return false;
  }

  @Override
  public boolean appendTo(PBReadBuffer buffer, Context context, RandomGenerator gen) {
    if (context.hp_len() != 1) {
      throw new RuntimeException("memory compression does not make sense for homopolymer");
    }
    int draw = gen.nextInt(data_.get(context.kmer()).size());
    final byte[] b = data_.get(context.kmer()).get(draw);
    buffer.addLast(b, 0, b.length);
    return true;
  }
}
