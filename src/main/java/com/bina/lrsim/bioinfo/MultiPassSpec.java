package com.bina.lrsim.bioinfo;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 * Created by bayolau on 3/7/16.
 */
public class MultiPassSpec {
  public final int numPasses;
  public final int fragmentLength;

  public MultiPassSpec(int[] lengths) {
    this.numPasses = lengths.length;
    fragmentLength = EstimateFragmentLength(lengths);
  }

  public int capLength(int length) {
    if (length >= fragmentLength) {
      return fragmentLength;
    } else {
      return length;
    }
  }

  static private int EstimateFragmentLength(int[] lengths) {
    if (lengths.length < 4) { return NumberUtils.max(lengths); }

    DescriptiveStatistics stats = new DescriptiveStatistics();
    for (int ii = 1; ii + 1 < lengths.length; ++ii) {
      stats.addValue(lengths[ii]);
    }
    return (int) (stats.getPercentile(50) + 0.5);
  }
}
