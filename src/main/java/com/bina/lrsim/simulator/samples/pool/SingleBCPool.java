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
  protected byte[] data;
  protected int[] end;

  public SingleBCPool(Spec spec, int numKmers, int entryPerKmer) {
    super(spec, numKmers, entryPerKmer);
    data = new byte[begin(numKmers)];
    end = new int[this.numKmers];
    for (int kk = 0; kk < this.numKmers; ++kk) {
      end[kk] = begin(kk);
    }
  }

  protected int begin(int kmer) {
    return kmer * entryPerKmer * BYTE_PER_BC;
  }

  @Override
  public boolean add(Event ev, AddBehavior ab) {
    if (end[ev.kmer()] - begin(ev.kmer()) < entryPerKmer * BYTE_PER_BC) {
      int shift = end[ev.kmer()];
      if (ev.size() != 1) { throw new RuntimeException("event is too large"); }
      for (EnumDat e : spec.getDataSet()) {
        data[shift + e.value] = ev.get(0, e);
      }
      data[shift + EnumDat.QualityValue.value] = (byte) ab.newQV(data[shift + EnumDat.QualityValue.value]);
      end[ev.kmer()] += BYTE_PER_BC;
      return true;
    }
    return false;
  }

  @Override
  public AppendState appendTo(PBReadBuffer buffer, Context context, AppendState as, RandomGenerator gen) {
    if (context.getHpLen() != 1) { throw new RuntimeException("memory compression does not make sense for homopolymer"); }
    final int base = begin(context.getKmer());
    if (base == end[context.getKmer()]) throw new RuntimeException("no sample for " + context.toString());
    final int shift = base + gen.nextInt((end[context.getKmer()] - base) / BYTE_PER_BC) * BYTE_PER_BC;
    if (as == null) {
      buffer.addLast(data, shift, shift + BYTE_PER_BC);
    } else { // last base deletion
      final byte[] tmp = Arrays.copyOfRange(data, shift, shift + BYTE_PER_BC);
      if (tmp[EnumDat.QualityValue.value] > as.lastEvent[EnumDat.QualityValue.value] || tmp[EnumDat.DeletionQV.value] > as.lastEvent[EnumDat.DeletionQV.value]) {
        tmp[EnumDat.QualityValue.value] = as.lastEvent[EnumDat.QualityValue.value];
        tmp[EnumDat.DeletionQV.value] = as.lastEvent[EnumDat.DeletionQV.value];
        tmp[EnumDat.DeletionTag.value] = as.lastEvent[EnumDat.DeletionTag.value];
      }
      buffer.addLast(tmp, 0, BYTE_PER_BC);
    }
    return new AppendState(null, true);
  }
}
