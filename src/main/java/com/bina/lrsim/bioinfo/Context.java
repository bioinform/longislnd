package com.bina.lrsim.bioinfo;

import java.util.Iterator;

/**
 * Created by bayo on 5/13/15.
 * <p/>
 * a sequencing Context, eg a base call at position p plus flanking sequences
 * <p/>
 * kmer, hpLen are just common names of integers with unique mapping to a sequencing context
 */
public class Context {
  private final int kmer;
  private final int hpLen;

  public Context(int kmer, int hp_len) {
    this.kmer = kmer;
    hpLen = hp_len;
  }

  public final int getKmer() {
    return kmer;
  }

  public final int getHpLen() {
    return hpLen;
  }

  public String toString() {
    return String.valueOf(getKmer()) + " " + String.valueOf(getHpLen());
  }


  /**
   * decompose a possibly complicated context into a series of simpler contexts
   * 
   * @param leftFlank left flank of the resulting iterator
   * @param rightFlank right flank of the resulting iterator
   * @return
   */
  public Iterator<Context> decompose(int leftFlank, int rightFlank) {
    throw new UnsupportedOperationException("cannot find a simpler decomposition");
  }
}
