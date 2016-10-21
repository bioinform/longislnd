package com.bina.lrsim.simulator.samples.pool;

import com.bina.lrsim.bioinfo.Context;
import com.bina.lrsim.pb.PBReadBuffer;
import com.bina.lrsim.pb.Spec;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Arrays;

/**
 * Created by bayolau on 10/1/15.
 */
public class DeletedSingleBCPool extends SingleBCPool {
  public DeletedSingleBCPool(Spec spec, int numKmers, int entryPerKmer) {
    super(spec, numKmers, entryPerKmer);
  }

  @Override
  public AppendState appendTo(PBReadBuffer buffer, Context context, AppendState previousAppendState, RandomGenerator gen) {
    if (context.getHpLen() != 1) { throw new RuntimeException("memory compression does not make sense for homopolymer"); }
    final int base = begin(context.getKmer());
    if (base == end[context.getKmer()]) throw new RuntimeException("no sample");
    final int shift = base + gen.nextInt((end[context.getKmer()] - base) / BYTE_PER_BC) * BYTE_PER_BC;
    return new AppendState(Arrays.copyOfRange(baseCallFieldsForAllKmers, shift, shift + BYTE_PER_BC), true);
  }
}
