package com.bina.lrsim.simulator;

import com.bina.lrsim.simulator.samples.pool.EmptyBCPool;
import com.bina.lrsim.simulator.samples.pool.KmerBCPool;
import com.bina.lrsim.simulator.samples.pool.SingleBCPool;

import java.util.EnumSet;

/**
 * Created by bayo on 5/8/15.
 * <p/>
 * Set of editing events relative to a given reference Also associate memory-efficient
 * implementations of storing those samples
 */
public enum EnumEvent {
  // value, description, recording period, sample pool implementation
  INSERTION(0, "i", 1, KmerBCPool.class), // ~10% insertion, but can be multi-bp, so let's suck it
                                          // up for now
  DELETION(1, "d", -1, EmptyBCPool.class), // skip all deletion samples, emptybcpool has no memory
                                           // overhead
  SUBSTITUTION(2, "s", 1, KmerBCPool.class), // ~1% mismatch so memory is not too bad
  MATCH(3, "m", 10, SingleBCPool.class); // 85% matches, down-sample 10-fold, must use
                                         // SingleBCPoolCompression
  private static final EnumEvent[] value2enum_ = {INSERTION, DELETION, SUBSTITUTION, MATCH};

  public final int value;
  public final String description;
  public final int recordEvery;
  public final Class<?> pool;

  public static EnumEvent value2enum(int i) {
    return value2enum_[i];
  }

  EnumEvent(int value, String description, int recordPeriod, Class<?> pool) {
    this.value = value;
    this.description = description;
    this.recordEvery = recordPeriod;
    this.pool = pool;
  }

  public static int num_logged_events() {
    int out = 0;
    for (EnumEvent ev : EnumSet.allOf(EnumEvent.class)) {
      if (ev.recordEvery > 0) {
        ++out;
      }
    }
    return out;
  }

  public String toString() {
    return description;
  }

  public static String getPrettyStats(long[] data) {
    if (data.length != EnumEvent.values().length) {
      return "invalid data length";
    }
    StringBuilder sb = new StringBuilder();
    long sum = 0;
    for (long entry : data) {
      sum += entry;
    }
    for (EnumEvent ev : EnumSet.allOf(EnumEvent.class)) {
      long count = data[ev.value];
      sb.append(" " + ev.toString() + " " + count + String.format(" (%5.1f)", 100 * (double) (count) / sum));
    }
    return sb.toString();
  }
}
