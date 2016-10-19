package com.bina.lrsim.simulator.samples.pool;

import com.bina.lrsim.pb.Spec;
import org.apache.commons.math3.random.RandomGenerator;

import com.bina.lrsim.bioinfo.Context;
import com.bina.lrsim.pb.PBReadBuffer;
import com.bina.lrsim.simulator.Event;

/**
 * Created by bayo on 5/10/15.
 */
//TODO: remove unused class
public class EmptyBCPool extends BaseCallsPool {
  public EmptyBCPool(Spec spec, int numKmers, int entryPerKmer) {
    super(spec, numKmers, entryPerKmer);
  }

  @Override
  public AppendedState appendTo(PBReadBuffer buffer, Context context, AppendedState previousAppendedState, RandomGenerator gen) {
    if (context.getHpLen() != 1) { throw new RuntimeException("memory compression does not make sense for homopolymer"); }
    return new AppendedState(null, true);
  }

  @Override
  public boolean add(Event ev, AddedBehavior ab) {
    return false;
  }

}
