package com.bina.lrsim.util;

import org.apache.commons.math3.random.MersenneTwister;

/**
 * Created by bayolau on 3/31/16.
 */
public class ThreadLocalResources {
  private static final ThreadLocal<MersenneTwister> gen = new ThreadLocal<MersenneTwister>() {
    public MersenneTwister initialValue() {
      return new MersenneTwister();
    }
  };

  // because java.util.concurrent.ThreadLocalRandom does not allow reseting of seed
  public static MersenneTwister random() {
    return gen.get();
  }
}
