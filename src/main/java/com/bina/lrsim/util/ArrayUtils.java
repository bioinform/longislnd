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

  /**
   * join an array of strings with a delimiter
   * @param a
   */
  public static String join(String[] a, String delimiter) {
    if (a == null) {
      return null;
    }
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < a.length - 1; i++) {
      sb.append(a[i] + delimiter);
    }
    if (a.length >= 1) {
      sb.append(a[a.length - 1]);
    }
    return sb.toString();
  }
}
