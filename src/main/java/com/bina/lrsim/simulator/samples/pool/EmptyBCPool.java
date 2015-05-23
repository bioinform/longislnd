package com.bina.lrsim.simulator.samples.pool;

import com.bina.lrsim.bioinfo.Context;
import com.bina.lrsim.h5.pb.PBReadBuffer;
import com.bina.lrsim.simulator.Event;
import org.apache.commons.math3.random.RandomGenerator;

/**
 * Created by bayo on 5/10/15.
 */
public class EmptyBCPool extends BaseCallsPool {
  public EmptyBCPool(int numKmers, int entryPerKmer) {
    super(numKmers, entryPerKmer);
  }

  @Override
  public boolean appendTo(PBReadBuffer buffer, Context context, RandomGenerator gen) {
    if (context.hp_len() != 1) {
      throw new RuntimeException("memory compression does not make sense for homopolymer");
    }
    return true;
  }

  @Override
  public boolean add(Event ev) {
    return false;
  }

}
