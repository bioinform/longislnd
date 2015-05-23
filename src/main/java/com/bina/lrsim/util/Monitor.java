package com.bina.lrsim.util;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;

/**
 * Created by bayo on 5/19/15.
 */
public class Monitor {
  public static long PeakMemoryUsage() {
    long out = 0;
    for (MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans()) {
      out += pool.getPeakUsage().getUsed();
    }
    return out;
  }
}
