package com.bina.lrsim.bioinfo;

import java.util.Arrays;
import java.util.Iterator;

import org.apache.log4j.Logger;

/**
 * Created by bayo on 5/13/15.
 * <p/>
 * An iterator which iterates through homopolymer context sequence.
 * <p/>
 * A valid homopolymer context of length (q-p+1) is centered at a position, p, such that the bp at p-1 is different. The bp from p to q are the same, and the bp
 * of q+1 is different.
 * <p/>
 * CGAAAAAAAAAATCA p q
 * <p/>
 * The iterator for the above situation would output context at
 * <p/>
 * ... (p-1), p, q+1, ......
 */
public final class HPIterator implements Iterator<Context> {
  private final static Logger log = Logger.getLogger(HPIterator.class.getName());
  private final byte[] seq;
  private int curr;
  private int end;
  private final int leftFlank;
  private final int rightFlank;
  private final int hpAnchor;

  /**
   * Constructor to iterate the kmer context of through [begin,end) of a ascii stream
   * 
   * @param ascii ascii sequence in the fw direction
   * @param begin 0-base begin
   * @param end 0-base end, exclusive
   * @param leftFlank number of bp before the position of interest
   * @param rightFlank number of bp after the position of interest
   * @param hpAnchor number of bp to anchor homopolymer
   */
  public HPIterator(byte[] ascii, int begin, int end, int leftFlank, int rightFlank, int hpAnchor) {
    this.leftFlank = leftFlank;
    this.rightFlank = rightFlank;
    this.hpAnchor = hpAnchor;
    seq = ascii;

    curr = begin + leftFlank;
    this.end = end - rightFlank;
    for (; curr > 0 && curr < seq.length && seq[curr] == seq[curr - 1]; ++curr) {}
    for (; 0 <= this.end - 2 && seq[this.end - 1] == seq[this.end - 2]; --this.end) {}
  }

  @Override
  public boolean hasNext() {
    return curr < end;
  }

  @Override
  public Context next() {
    // find the next base which is different
    int firstDifferenceIndex = curr + 1;
    for (; firstDifferenceIndex < seq.length && seq[firstDifferenceIndex] == seq[curr]; ++firstDifferenceIndex) {}

    if (firstDifferenceIndex + rightFlank > seq.length) {
      //TODO: move unconditionally executed statement out of if statement
      curr = firstDifferenceIndex;
      return null;
    } else {
      final byte[] buffer = Arrays.copyOfRange(seq, curr - leftFlank, firstDifferenceIndex + rightFlank);

      curr = firstDifferenceIndex;
      return new HPContext(buffer, leftFlank, rightFlank, hpAnchor);
    }
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }
}
