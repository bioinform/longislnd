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
  INSERTION("i", Heuristics.SAMPLE_PERIOD_INS, KmerBCPool.class),
  DELETION("d", Heuristics.SAMPLE_PERIOD_DEL, DeletedSingleBCPool.class),
  SUBSTITUTION("s", Heuristics.SAMPLE_PERIOD_SUB, KmerBCPool.class),
  MATCH("m", Heuristics.SAMPLE_PERIOD_MAT, SingleBCPool.class);

  public static final EnumEvent[] values = values();

  public static String getListDescription() {
    StringBuilder sb = new StringBuilder();
    sb.append(values[0]);
    for(int i = 1; i < values.length; i++) {
      sb.append(":" + values[i]);
    }
    return sb.toString();
  }

  public final String description;
  public final int recordEvery;
  public final Class<?> pool;

  EnumEvent(String description, int recordPeriod, Class<?> pool) {
    this.description = description;
    this.recordEvery = recordPeriod;
    this.pool = pool;
  }

  public String toString() {
    return description;
  }

  public static String getPrettyStats(long[] data) {
    if (data.length != values.length) {
      return "invalid data length";
    }
    StringBuilder sb = new StringBuilder();
    long sum = 0;
    for (long entry : data) {
      sum += entry;
    }
    for (EnumEvent ev : EnumEvent.values) {
      long count = data[ev.ordinal()];
      sb.append(" " + ev + " " + count + String.format(" (%5.1f)", 100 * (double) (count) / sum));
    }
    return sb.toString();
  }
}
