package com.bina.lrsim.simulator.samples.pool;

/**
 * Created by bayo on 8/14/15.
 */

/**
 * a class which describe appendTo results
 */
public class AppendState {
  public AppendState(byte[] lastEvent, boolean success) {
    this.lastEvent = lastEvent;
    this.success = success;
  }

  public final boolean success;
  public final byte[] lastEvent;
}
