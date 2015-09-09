package com.bina.lrsim.simulator.samples.pool;

import org.apache.commons.math3.random.RandomGenerator;

import com.bina.lrsim.bioinfo.Context;
import com.bina.lrsim.h5.pb.PBReadBuffer;
import com.bina.lrsim.h5.pb.PBSpec;
import com.bina.lrsim.simulator.Event;

/**
 * Created by bayo on 5/10/15.
 */
public class EmptyBCPool extends BaseCallsPool {
  public EmptyBCPool(PBSpec spec, int numKmers, int entryPerKmer) {
    super(spec, numKmers, entryPerKmer);
  }

  @Override
  public AppendState appendTo(PBReadBuffer buffer, Context context, AppendState as, RandomGenerator gen) {
    if (context.hp_len() != 1) { throw new RuntimeException("memory compression does not make sense for homopolymer"); }
    return new AppendState(null, true);
  }

  @Override
  public boolean add(Event ev, AddBehavior ab) {
    return false;
  }

}
