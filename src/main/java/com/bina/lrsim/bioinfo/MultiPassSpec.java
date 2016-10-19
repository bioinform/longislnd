package com.bina.lrsim.bioinfo;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 * Created by bayolau on 3/7/16.
 */
public class MultiPassSpec {
  //number of passes, i.e., # of insert lengths
  public final int numPasses;
    //estiamted fragment length given an array of lengths
  public final int fragmentLength;

  public MultiPassSpec(int[] lengths) {
    this.numPasses = lengths.length;
    fragmentLength = EstimateFragmentLength(lengths);
  }

  //TODO: remove unused method
  public int capLength(int length) {
    if (length >= fragmentLength) {
      return fragmentLength;
    } else {
      return length;
    }
  }

  /**
   * given an array of lengths, estimate the simulated length
   * either as max of all lengths (for 3 or fewer lengths)
   * or as 50% percentile
   *
   * @param lengths
   * @return
   */
  static private int EstimateFragmentLength(int[] lengths) {
    if (lengths.length < 4) { return NumberUtils.max(lengths); }

    DescriptiveStatistics stats = new DescriptiveStatistics();
    //why exclude first and last?
    for (int ii = 1; ii + 1 < lengths.length; ++ii) {
      stats.addValue(lengths[ii]);
    }
    return (int) (stats.getPercentile(50) + 0.5);
  }
}
