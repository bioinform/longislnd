package com.bina.lrsim.util;

/**
 * Created by bayo on 5/13/15.
 * 
 * hacked up some reusable snippets until I figure out the proper way of doing so in java
 */
public class ArrayUtils {
  public static void axpy(long a, long[] x, long[] y) {
    if (x.length != y.length) throw new RuntimeException("inconsistent samples");
    for (int ii = 0; ii < x.length; ++ii) {
      y[ii] += a * x[ii];
    }
  }
}
