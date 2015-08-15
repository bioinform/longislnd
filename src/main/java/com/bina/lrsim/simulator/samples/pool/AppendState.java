package com.bina.lrsim.simulator.samples.pool;

/**
 * Created by bayo on 8/14/15.
 */

/**
 * a class which describe appendTo results
 */
public class AppendState {
  public AppendState(byte[] last_event, boolean success) {
    this.last_event = last_event;
    this.success = success;
  }

  public final boolean success;
  public final byte[] last_event;
}
