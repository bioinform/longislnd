package com.bina.lrsim.simulator.samples.pool;

import org.apache.log4j.Logger;

/**
 * Created by bayolau on 9/9/15.
 */
public class AddBehavior {
  private final static Logger log = Logger.getLogger(AddBehavior.class.getName());
  private final int delta_q;
  private final int min_q;
  private final int max_q;

  /**
   * constructor
   *
   * @param delta_q how to change q
   * @param min_q minimum of changed q value, ignored if < 0 or larger than original qv
   * @param max_q maximum of changed q value, ignored if < min_q or smaller than original qv
   */
  public AddBehavior(int delta_q, int min_q, int max_q) {
    this.delta_q = delta_q;
    if (max_q >= min_q && min_q >= 0) {
      this.min_q = min_q;
      this.max_q = max_q;
    } else {
      log.warn("min/max q = " + min_q + "/" + max_q + " ignored.");
      this.min_q = 0;
      this.max_q = Integer.MAX_VALUE;
    }
  }

  /**
   * apply qv transform, keeps old qv if it's beyond cap
   *
   * @param old_qv an original quality value
   * @return modified quality value
   */
  public int newQV(int old_qv) {
    final int ma = Math.max(old_qv, max_q);
    final int mi = Math.min(old_qv, min_q);
    return Math.max(mi, Math.min(old_qv + delta_q, ma));
  }

}
