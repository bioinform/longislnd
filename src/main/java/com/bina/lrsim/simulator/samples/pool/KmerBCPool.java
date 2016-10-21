package com.bina.lrsim.simulator.samples.pool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.bina.lrsim.pb.EnumDat;
import com.bina.lrsim.pb.Spec;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.log4j.Logger;

import com.bina.lrsim.bioinfo.Context;
import com.bina.lrsim.pb.PBReadBuffer;
import com.bina.lrsim.simulator.Event;

/**
 * Created by bayo on 5/10/15.
 */
public class KmerBCPool extends BaseCallsPool {

  // this might have significat memory overhead
  private final static Logger log = Logger.getLogger(KmerBCPool.class.getName());

  private List<List<byte[]>> baseCallFieldsForAllKmers;

  public KmerBCPool(Spec spec, int numKmers, int entryPerKmer) {
    super(spec, numKmers, entryPerKmer);
    baseCallFieldsForAllKmers = new ArrayList<>(this.numKmers);
    for (int ii = 0; ii < this.numKmers; ++ii) {
      baseCallFieldsForAllKmers.add(new ArrayList<byte[]>((entryPerKmer > 0) ? entryPerKmer : 100));
    }
  }

  @Override
  public boolean add(Event ev, AddBehavior ab) {
    if (entryPerKmer < 0 || baseCallFieldsForAllKmers.get(ev.getKmer()).size() < entryPerKmer) {
      byte[] tmp = ev.dataCpy();
      for (int idx = EnumDat.QualityValue.value; idx < tmp.length; idx += EnumDat.numBytes) {
        tmp[idx] = (byte) ab.newQV(tmp[idx]);
      }
      baseCallFieldsForAllKmers.get(ev.getKmer()).add(tmp);
      return true;
    }
    return false;
  }

  @Override
  public AppendState appendTo(PBReadBuffer buffer, Context context, AppendState previousAppendState, RandomGenerator gen) {
    if (context.getHpLen() != 1) { throw new RuntimeException("memory compression does not make sense for homopolymer"); }
    int draw = gen.nextInt(baseCallFieldsForAllKmers.get(context.getKmer()).size());
    final byte[] b = baseCallFieldsForAllKmers.get(context.getKmer()).get(draw);
    if (previousAppendState != null && (b[EnumDat.QualityValue.value] > previousAppendState.lastEvent[EnumDat.QualityValue.value] || b[EnumDat.DeletionQV.value] > previousAppendState.lastEvent[EnumDat.DeletionQV.value])) {
      final byte[] tmp = Arrays.copyOf(b, b.length);
      tmp[EnumDat.QualityValue.value] = previousAppendState.lastEvent[EnumDat.QualityValue.value];
      tmp[EnumDat.DeletionQV.value] = previousAppendState.lastEvent[EnumDat.DeletionQV.value];
      tmp[EnumDat.DeletionTag.value] = previousAppendState.lastEvent[EnumDat.DeletionTag.value];
      buffer.addLast(tmp, 0, tmp.length);
    } else {
      buffer.addLast(b, 0, b.length);
    }
    return new AppendState(null, true);
  }
}
