package com.bina.lrsim.simulator.samples.pool;

import org.apache.log4j.Logger;

/**
 * Created by bayolau on 9/9/15.
 */
public class AddBehavior {
  private final static Logger log = Logger.getLogger(AddBehavior.class.getName());
  private final int deltaQ;
  private final int minQ;
  private final int maxQ;

  /**
   * constructor
   *
   * @param deltaQ how to change q
   * @param minQ minimum of changed q value, ignored if < 0 or larger than original qv
   * @param maxQ maximum of changed q value, ignored if < minQ or smaller than original qv
   */
  public AddBehavior(int deltaQ, int minQ, int maxQ) {
    this.deltaQ = deltaQ;
    if (maxQ >= minQ && minQ >= 0) {
      this.minQ = minQ;
      this.maxQ = maxQ;
    } else {
      log.warn("min/max q = " + minQ + "/" + maxQ + " ignored.");
      this.minQ = 0;
      this.maxQ = Integer.MAX_VALUE;
    }
  }

  /**
   * apply qv transform, keeps old qv if it's beyond cap
   *
   * @param oldQv an original quality value
   * @return modified quality value
   */
  public int newQV(int oldQv) {
    final int ma = Math.max(oldQv, maxQ);
    final int mi = Math.min(oldQv, minQ);
    return Math.max(mi, Math.min(oldQv + deltaQ, ma));
  }

}
