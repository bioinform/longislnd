package com.bina.lrsim.simulator;

import com.bina.lrsim.bioinfo.Heuristics;
import com.bina.lrsim.simulator.samples.pool.DeletedSingleBCPool;
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
  INSERTION(0, "i", Heuristics.SAMPLE_PERIOD_INS, KmerBCPool.class),
  DELETION(1, "d", Heuristics.SAMPLE_PERIOD_DEL, DeletedSingleBCPool.class),
  SUBSTITUTION(2, "s", Heuristics.SAMPLE_PERIOD_SUB, KmerBCPool.class),
  MATCH(3, "m", Heuristics.SAMPLE_PERIOD_MAT, SingleBCPool.class);

  private static final EnumEvent[] value2enum_ = {INSERTION, DELETION, SUBSTITUTION, MATCH};
  public static String getListDescription() {
    StringBuilder sb = new StringBuilder();
    sb.append(value2enum(0).toString());
    for(int ii = 1; ii < EnumEvent.values().length; ++ii) {
      sb.append(":"+value2enum(ii).toString());
    }
    return sb.toString();
  }

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
