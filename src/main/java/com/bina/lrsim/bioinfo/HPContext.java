package com.bina.lrsim.bioinfo;


import java.util.Iterator;

/**
 * Created by bayo on 5/11/15.
 */
public final class HPContext extends Context {
  private final byte[] ascii;

  private static int constructor_kmerizer(byte[] asciiArray, int leftFlank, int rightFlank, int hp_anchor) {
    //when array length is equal to left flank + right flank + 1, no homopolymer
    if (asciiArray.length == 1 + leftFlank + rightFlank) {
      return Kmerizer.fromASCII(asciiArray);
    } else {
      byte[] tmp = new byte[2 * hp_anchor + 1];
      int k = 0;
      for (int pos = leftFlank - hp_anchor; pos <= leftFlank; ++pos, ++k) {
        tmp[k] = asciiArray[pos];
      }
      for (int pos = asciiArray.length - rightFlank; pos < asciiArray.length - rightFlank + hp_anchor; ++pos, ++k) {
        tmp[k] = asciiArray[pos];
      }
      return Kmerizer.fromASCII(tmp);
    }
  }

  HPContext(byte[] asciiArray, int leftFlank, int rightFlank, int hp_anchor) {
    super(constructor_kmerizer(asciiArray, leftFlank, rightFlank, hp_anchor), asciiArray.length - leftFlank - rightFlank);
    this.ascii = asciiArray;
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
