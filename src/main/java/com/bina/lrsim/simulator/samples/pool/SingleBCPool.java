package com.bina.lrsim.simulator.samples.pool;

import java.util.Arrays;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.log4j.Logger;

import com.bina.lrsim.bioinfo.Context;
import com.bina.lrsim.pb.EnumDat;
import com.bina.lrsim.pb.PBReadBuffer;
import com.bina.lrsim.pb.Spec;
import com.bina.lrsim.simulator.Event;

/**
 * Created by bayo on 5/10/15.
 */
public class SingleBCPool extends BaseCallsPool {
  private final static Logger log = Logger.getLogger(SingleBCPool.class.getName());
  protected byte[] baseCallFieldsForAllKmers;
  protected int[] end;

  public SingleBCPool(Spec spec, int numKmers, int entryPerKmer) {
    super(spec, numKmers, entryPerKmer);
    baseCallFieldsForAllKmers = new byte[begin(numKmers)];
    end = new int[this.numKmers];
    for (int kk = 0; kk < this.numKmers; ++kk) {
      end[kk] = begin(kk);
    }
  }

  protected int begin(int kmer) {
    return kmer * entryPerKmer * BYTE_PER_BC;
  }

  @Override
  public boolean add(Event ev, AddedBehavior ab) {
    if (end[ev.getKmer()] - begin(ev.getKmer()) < entryPerKmer * BYTE_PER_BC) {
      int shift = end[ev.getKmer()];
      if (ev.size() != 1) { throw new RuntimeException("event is too large"); }
      for (EnumDat e : spec.getDataSet()) {
        baseCallFieldsForAllKmers[shift + e.value] = ev.get(0, e);
      }
      baseCallFieldsForAllKmers[shift + EnumDat.QualityValue.value] = (byte) ab.newQV(baseCallFieldsForAllKmers[shift + EnumDat.QualityValue.value]);
      end[ev.getKmer()] += BYTE_PER_BC;
      return true;
    }
    return false;
  }

  @Override
  public AppendedState appendTo(PBReadBuffer buffer, Context context, AppendedState previousAppendedState, RandomGenerator gen) {
    if (context.getHpLen() != 1) { throw new RuntimeException("memory compression does not make sense for homopolymer"); }
    final int base = begin(context.getKmer());
    if (base == end[context.getKmer()]) throw new RuntimeException("no sample for " + context.toString());
    final int randomShift = base + gen.nextInt((end[context.getKmer()] - base) / BYTE_PER_BC) * BYTE_PER_BC;
    if (previousAppendedState == null) {
      buffer.addLast(baseCallFieldsForAllKmers, randomShift, randomShift + BYTE_PER_BC);
    } else { // last base deletion
      final byte[] tmp = Arrays.copyOfRange(baseCallFieldsForAllKmers, randomShift, randomShift + BYTE_PER_BC);
      if (tmp[EnumDat.QualityValue.value] > previousAppendedState.lastEvent[EnumDat.QualityValue.value] || tmp[EnumDat.DeletionQV.value] > previousAppendedState.lastEvent[EnumDat.DeletionQV.value]) {
        tmp[EnumDat.QualityValue.value] = previousAppendedState.lastEvent[EnumDat.QualityValue.value];
        tmp[EnumDat.DeletionQV.value] = previousAppendedState.lastEvent[EnumDat.DeletionQV.value];
        tmp[EnumDat.DeletionTag.value] = previousAppendedState.lastEvent[EnumDat.DeletionTag.value];
      }
      buffer.addLast(tmp, 0, BYTE_PER_BC);
    }
    return new AppendedState(null, true);
  }
}
