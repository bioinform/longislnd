package com.bina.lrsim.simulator.samples.pool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.bina.lrsim.pb.EnumDat;
import com.bina.lrsim.pb.Spec;
import org.apache.commons.math3.random.RandomGenerator;

import com.bina.lrsim.bioinfo.Context;
import com.bina.lrsim.bioinfo.Heuristics;
import com.bina.lrsim.pb.PBReadBuffer;
import com.bina.lrsim.simulator.Event;
import org.apache.log4j.Logger;

/**
 * Created by bayo on 5/14/15.
 */
public class HPBCPool extends BaseCallsPool {
  private final static Logger log = Logger.getLogger(HPBCPool.class.getName());

  private List<List<List<byte[]>>> data; // data[kmer][hp_len] contains a pool of byte[] base calls

  public HPBCPool(Spec spec, int numKmers, int entryPerKmer) {
    super(spec, numKmers, entryPerKmer);

    data = new ArrayList<>(this.numKmers);
    for (int ii = 0; ii < this.numKmers; ++ii) {
      data.add(new ArrayList<List<byte[]>>((entryPerKmer > 0) ? entryPerKmer : Heuristics.MIN_HP_SAMPLES));
    }
    // data_[kmer] is non-null, but data_[kmer][len] might not have been allocated at this point
  }

  @Override
  public boolean add(Event ev, AddedBehavior ab) {
    final int kmer = ev.getKmer();
    final int hpLen = ev.getHpLen();

    while (data.get(kmer).size() <= hpLen) {
      data.get(kmer).add(null);
    }
    if (null == data.get(kmer).get(hpLen)) {
      data.get(kmer).set(hpLen, new ArrayList<byte[]>());
    }

    byte[] tmp = ev.dataCpy();

    for (int idx = EnumDat.QualityValue.value; idx < tmp.length; idx += EnumDat.numBytes) {
      tmp[idx] = (byte) ab.newQV(tmp[idx]);
    }

    data.get(kmer).get(hpLen).add(tmp);

    return true;
  }

  @Override
  public AppendState appendTo(PBReadBuffer buffer, Context context, AppendState as, RandomGenerator gen) {
    final int kmer = context.getKmer();
    final int hpLen = context.getHpLen();
    if (hpLen < data.get(kmer).size()) {
      List<byte[]> pool = data.get(kmer).get(hpLen);
      if (null != pool && pool.size() >= Heuristics.MIN_HP_SAMPLES) {
        final byte[] b = pool.get(gen.nextInt(pool.size()));
        if (as != null && b.length > 0 && (b[EnumDat.QualityValue.value] > as.lastEvent[EnumDat.QualityValue.value] || b[EnumDat.DeletionQV.value] > as.lastEvent[EnumDat.DeletionQV.value])) {
          final byte[] tmp = Arrays.copyOf(b, b.length);
          tmp[EnumDat.QualityValue.value] = as.lastEvent[EnumDat.QualityValue.value];
          tmp[EnumDat.DeletionQV.value] = as.lastEvent[EnumDat.DeletionQV.value];
          tmp[EnumDat.DeletionTag.value] = as.lastEvent[EnumDat.DeletionTag.value];
          buffer.addLast(tmp, 0, tmp.length);
        }
        else {
          buffer.addLast(b, 0, b.length);
        }
        return new AppendState(null, true);
      }
    }
    return new AppendState(null, false);
  }
}
