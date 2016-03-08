package com.bina.lrsim.simulator.samples.pool;

import java.util.ArrayList;
import java.util.List;

import com.bina.lrsim.pb.EnumDat;
import org.apache.commons.math3.random.RandomGenerator;

import com.bina.lrsim.bioinfo.Context;
import com.bina.lrsim.bioinfo.Heuristics;
import com.bina.lrsim.pb.PBReadBuffer;
import com.bina.lrsim.pb.PBSpec;
import com.bina.lrsim.simulator.Event;
import org.apache.log4j.Logger;

/**
 * Created by bayo on 5/14/15.
 */
public class HPBCPool extends BaseCallsPool {
  private final static Logger log = Logger.getLogger(HPBCPool.class.getName());

  // this is a hack until we have a proper full-blown homopolyer error rate scaling
  private final static int MIN_POOL_SIZE = 20;

  private List<List<List<byte[]>>> data_; // data[kmer][hp_len] contains a pool of byte[] base calls

  public HPBCPool(PBSpec spec, int numKmers, int entryPerKmer) {
    super(spec, numKmers, entryPerKmer);

    data_ = new ArrayList<>(numKmers_);
    for (int ii = 0; ii < numKmers_; ++ii) {
      data_.add(new ArrayList<List<byte[]>>((entryPerKmer > 0) ? entryPerKmer : Heuristics.MIN_HP_SAMPLES));
    }
    // data_[kmer] is non-null, but data_[kmer][len] might not have been allocated at this point
  }

  @Override
  public boolean add(Event ev, AddBehavior ab) {
    final int kmer = ev.kmer();
    final int hp_len = ev.hp_len();

    while (data_.get(kmer).size() <= hp_len) {
      data_.get(kmer).add(null);
    }
    if (null == data_.get(kmer).get(hp_len)) {
      data_.get(kmer).set(hp_len, new ArrayList<byte[]>());
    }

    byte[] tmp = ev.data_cpy();

    for (int idx = EnumDat.QualityValue.value; idx < tmp.length; idx += EnumDat.numBytes) {
      tmp[idx] = (byte) ab.newQV(tmp[idx]);
    }

    data_.get(kmer).get(hp_len).add(tmp);

    return true;
  }

  @Override
  public AppendState appendTo(PBReadBuffer buffer, Context context, AppendState as, RandomGenerator gen) {

    final int kmer = context.kmer();
    final int hp_len = context.hp_len();
    if (hp_len < data_.get(kmer).size()) {
      List<byte[]> pool = data_.get(kmer).get(hp_len);
      if (null != pool && pool.size() >= Heuristics.MIN_HP_SAMPLES) {
        final byte[] b = pool.get(gen.nextInt(pool.size()));
        if (as != null && b.length > 0 && (b[EnumDat.QualityValue.value] > as.last_event[EnumDat.QualityValue.value] || b[EnumDat.DeletionQV.value] > as.last_event[EnumDat.DeletionQV.value])) {
          b[EnumDat.QualityValue.value] = as.last_event[EnumDat.QualityValue.value];
          b[EnumDat.DeletionQV.value] = as.last_event[EnumDat.DeletionQV.value];
          b[EnumDat.DeletionTag.value] = as.last_event[EnumDat.DeletionTag.value];
        }
        buffer.addLast(b, 0, b.length);
        return new AppendState(null, true);
      }
    }
    return new AppendState(null, false);
  }
}
