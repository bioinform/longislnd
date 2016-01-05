package com.bina.lrsim.simulator.samples.pool;

import java.util.Arrays;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.log4j.Logger;

import com.bina.lrsim.bioinfo.Context;
import com.bina.lrsim.h5.pb.EnumDat;
import com.bina.lrsim.h5.pb.PBReadBuffer;
import com.bina.lrsim.h5.pb.PBSpec;
import com.bina.lrsim.simulator.Event;

/**
 * Created by bayo on 5/10/15.
 */
public class SingleBCPool extends BaseCallsPool {
  private final static Logger log = Logger.getLogger(SingleBCPool.class.getName());
  protected byte[] data_;
  protected int[] end_;

  public SingleBCPool(PBSpec spec, int numKmers, int entryPerKmer) {
    super(spec, numKmers, entryPerKmer);
    data_ = new byte[begin(numKmers)];
    end_ = new int[numKmers_];
    for (int kk = 0; kk < numKmers_; ++kk) {
      end_[kk] = begin(kk);
    }
  }

  protected int begin(int kmer) {
    return kmer * entryPerKmer_ * BYTE_PER_BC;
  }

  @Override
  public boolean add(Event ev, AddBehavior ab) {
    if (end_[ev.kmer()] - begin(ev.kmer()) < entryPerKmer_ * BYTE_PER_BC) {
      int shift = end_[ev.kmer()];
      if (ev.size() != 1) { throw new RuntimeException("event is too large"); }
      for (EnumDat e : spec.getDataSet()) {
        data_[shift + e.value] = ev.get(0, e);
      }
      data_[shift + EnumDat.QualityValue.value] = (byte) ab.newQV(data_[shift + EnumDat.QualityValue.value]);
      end_[ev.kmer()] += BYTE_PER_BC;
      return true;
    }
    return false;
  }

  @Override
  public AppendState appendTo(PBReadBuffer buffer, Context context, AppendState as, RandomGenerator gen) {
    if (context.hp_len() != 1) { throw new RuntimeException("memory compression does not make sense for homopolymer"); }
    final int base = begin(context.kmer());
    if (base == end_[context.kmer()]) throw new RuntimeException("no sample");
    final int shift = base + gen.nextInt((end_[context.kmer()] - base) / BYTE_PER_BC) * BYTE_PER_BC;
    if (as == null) {
      buffer.addLast(data_, shift, shift + BYTE_PER_BC);
    } else { // last base deletion
      final byte[] tmp = Arrays.copyOfRange(data_, shift, shift + BYTE_PER_BC);
      if (tmp[EnumDat.QualityValue.value] > as.last_event[EnumDat.QualityValue.value] || tmp[EnumDat.DeletionQV.value] > as.last_event[EnumDat.DeletionQV.value]) {
        tmp[EnumDat.QualityValue.value] = as.last_event[EnumDat.QualityValue.value];
        tmp[EnumDat.DeletionQV.value] = as.last_event[EnumDat.DeletionQV.value];
        tmp[EnumDat.DeletionTag.value] = as.last_event[EnumDat.DeletionTag.value];
      }
      buffer.addLast(tmp, 0, BYTE_PER_BC);
    }
    return new AppendState(null, true);
  }
}
