package com.bina.lrsim.simulator.samples.pool;

import java.util.ArrayList;
import java.util.List;

import com.bina.lrsim.h5.pb.EnumDat;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.log4j.Logger;

import com.bina.lrsim.bioinfo.Context;
import com.bina.lrsim.h5.pb.PBReadBuffer;
import com.bina.lrsim.h5.pb.PBSpec;
import com.bina.lrsim.simulator.Event;

/**
 * Created by bayo on 5/10/15.
 */
public class KmerBCPool extends BaseCallsPool {

  // this might have significat memory overhead
  private final static Logger log = Logger.getLogger(KmerBCPool.class.getName());

  private List<List<byte[]>> data_;

  public KmerBCPool(PBSpec spec, int numKmers, int entryPerKmer) {
    super(spec, numKmers, entryPerKmer);
    data_ = new ArrayList<List<byte[]>>(numKmers_);
    for (int ii = 0; ii < numKmers_; ++ii) {
      data_.add(new ArrayList<byte[]>((entryPerKmer > 0) ? entryPerKmer : 100));
    }
  }

  @Override
  public boolean add(Event ev, AddBehavior ab) {
    if (entryPerKmer_ < 0 || data_.get(ev.kmer()).size() < entryPerKmer_) {
      byte[] tmp = ev.data_cpy();
      for (int idx = EnumDat.QualityValue.value; idx < tmp.length; idx += EnumDat.numBytes) {
        tmp[idx] += ab.delta_q;
      }
      data_.get(ev.kmer()).add(tmp);
      return true;
    }
    return false;
  }

  @Override
  public AppendState appendTo(PBReadBuffer buffer, Context context, AppendState as, RandomGenerator gen) {
    if (context.hp_len() != 1) { throw new RuntimeException("memory compression does not make sense for homopolymer"); }
    int draw = gen.nextInt(data_.get(context.kmer()).size());
    final byte[] b = data_.get(context.kmer()).get(draw);
    if (as != null && (b[EnumDat.QualityValue.value] > as.last_event[EnumDat.QualityValue.value] || b[EnumDat.DeletionQV.value] > as.last_event[EnumDat.DeletionQV.value])) {
      b[EnumDat.QualityValue.value] = as.last_event[EnumDat.QualityValue.value];
      b[EnumDat.DeletionQV.value] = as.last_event[EnumDat.DeletionQV.value];
      b[EnumDat.DeletionTag.value] = as.last_event[EnumDat.DeletionTag.value];
    }
    buffer.addLast(b, 0, b.length);
    return new AppendState(b, true);
  }
}
