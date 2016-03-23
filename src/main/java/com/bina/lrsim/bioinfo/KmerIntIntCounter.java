package com.bina.lrsim.bioinfo;

import com.bina.lrsim.util.ArrayUtils;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;

/**
 * Created by bayolau on 1/12/16.
 */
public class KmerIntIntCounter implements Serializable {
  private final static Logger log = Logger.getLogger(KmerIntIntCounter.class.getName());
  public final int k;
  public final int maxKmer;
  public final int max1;
  public final int max2;
  private final long[] data;

  public KmerIntIntCounter(int k, int max1, int max2) {
    this.k = k;
    this.maxKmer = 1 << (2 * k);
    this.max1 = max1;
    this.max2 = max2;
    this.data = new long[maxKmer * max1 * max2];
  }

  public long get(int kmer, int l1, int l2) {
    return data[(kmer * max1 + l1) * max2 + l2];
  }

  public long increment(int kmer, int l1, int l2, long delta) {
    if (kmer >= 0 && kmer < maxKmer && l1 >= 0 && l1 < max1 && l2 >= 0 && l2 < max2) {
      final int index = (kmer * max1 + l1) * max2 + l2;
      data[index] += delta;
      return data[index];
    } else {
      log.warn("out-of-range: " + kmer + " " + l1 + " " + l2);
    }
    return 0l;
  }

  public long increment(int kmer, int l1, int l2) {
    return increment(kmer, l1, l2, 1);
  }

  public void accumulate(KmerIntIntCounter other) {
    if (k != other.k) { throw new RuntimeException("unmatched k " + k + "!=" + other.k); }
    if (max1 != other.max1) { throw new RuntimeException("unmatched max1 " + max1 + "!=" + other.max1); }
    if (max2 != other.max2) { throw new RuntimeException("unmatched max2 " + max2 + "!=" + other.max2); }
    ArrayUtils.axpy(1, other.data, data);
  }

  @Override
  public String toString() {
    return toString("");
  }

  public String toString(String prefix) {
    StringBuilder sb = new StringBuilder();
    for (int kk = 0; kk < maxKmer; ++kk) {
      for (int r = 0; r < max1; ++r) {
        long sum = 0;
        for (int s = 0; s < max2; ++s) {
          sum += get(kk, r, s);
        }
        if (sum == 0) {
          continue;
        }
        String kmer = new String(Kmerizer.toByteArray(kk, k), StandardCharsets.UTF_8);
        sb.append(prefix + kmer + " " + sum + " " + r + ":");
        for (int s = 0; s < max2; ++s) {
          long tmp = get(kk, r, s);
          if (tmp > 0) {
            sb.append(" " + s + "-" + tmp + "-" + String.format("(%.1f)", 100d * tmp / sum));
          }
        }
        sb.append("\n");
      }
    }
    return sb.toString();
  }

  /**
   * create a new instance by deriving new kmers from existing kmer, then accumulate the stats
   *
   * @param beginK begining position of existing kmer
   * @param endK end position of existing kmer (exclusive)
   * @return new instance with statistics of shortened kmer
   */
  public KmerIntIntCounter reduce(int beginK, int endK) {
    final int newK = endK - beginK;
    if (newK < 1 || beginK < 0 || endK > k) return null;
    final KmerIntIntCounter ret = new KmerIntIntCounter(newK, max1, max2);
    for (int kk = 0; kk < maxKmer; ++kk) {
      final int newKmer = Kmerizer.fromASCII(Kmerizer.toByteArray(kk, k), beginK, endK);
      for (int r = 0; r < max1; ++r) {
        for (int s = 0; s < max2; ++s) {
          ret.increment(newKmer, r, s, this.get(kk, r, s));
        }
      }
    }
    return ret;
  }
}
