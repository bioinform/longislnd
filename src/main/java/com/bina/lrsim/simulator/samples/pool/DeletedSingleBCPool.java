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
  public AppendState appendTo(PBReadBuffer buffer, Context context, AppendState as, RandomGenerator gen) {
    if (context.hp_len() != 1) { throw new RuntimeException("memory compression does not make sense for homopolymer"); }
    final int base = begin(context.kmer());
    if (base == end_[context.kmer()]) throw new RuntimeException("no sample");
    final int shift = base + gen.nextInt((end_[context.kmer()] - base) / BYTE_PER_BC) * BYTE_PER_BC;
    return new AppendState(Arrays.copyOfRange(data_, shift, shift + BYTE_PER_BC), true);
  }
}
