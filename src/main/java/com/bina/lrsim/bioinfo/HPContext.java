package com.bina.lrsim.bioinfo;


import java.util.Iterator;

/**
 * Created by bayo on 5/11/15.
 */
public final class HPContext extends Context {
  private final byte[] ascii;

  private static int constructor_kmerizer(byte[] ascii, int leftFlank, int rightFlank, int hp_anchor) {
    if (ascii.length == 1 + leftFlank + rightFlank) {
      return Kmerizer.fromASCII(ascii);
    } else {
      byte[] tmp = new byte[2 * hp_anchor + 1];
      int k = 0;
      for (int pos = leftFlank - hp_anchor; pos <= leftFlank; ++pos, ++k) {
        tmp[k] = ascii[pos];
      }
      for (int pos = ascii.length - rightFlank; pos < ascii.length - rightFlank + hp_anchor; ++pos, ++k) {
        tmp[k] = ascii[pos];
      }
      return Kmerizer.fromASCII(tmp);
    }
  }

  HPContext(byte[] ascii, int leftFlank, int rightFlank, int hp_anchor) {
    super(constructor_kmerizer(ascii, leftFlank, rightFlank, hp_anchor), ascii.length - leftFlank - rightFlank);
    this.ascii = ascii;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (byte entry : ascii) {
      sb.append((char) entry);
    }
    sb.append(" ");
    sb.append(String.valueOf(getHpLen()));
    sb.append(" ");
    sb.append(String.valueOf(getKmer()));
    return sb.toString();
  }

  /**
   * decompose a possibly complicated context into a series of simpler contexts
   * 
   * @return an iterator of simpler contexts
   */
  @Override
  public Iterator<Context> decompose(int leftFlank, int rightFlank) {
    return new KmerIterator(ascii, 0, ascii.length, leftFlank, rightFlank);
  }

}
