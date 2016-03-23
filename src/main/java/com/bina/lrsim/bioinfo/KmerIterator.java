package com.bina.lrsim.bioinfo;

import org.apache.log4j.Logger;

import java.util.Iterator;

/**
 * Created by bayo on 5/11/15.
 */
public final class KmerIterator implements Iterator<Context> {
  private final static Logger log = Logger.getLogger(KmerIterator.class.getName());
  private final byte[] seq;
  private int curr;
  private final int end;
  private final int leftFlank;
  private final int rightFlank;

  /**
   * Constructor to iterate the kmer context of through [begin,end) of a ascii stream
   * 
   * @param ascii ascii sequence in fw direction
   * @param begin 0-base begin
   * @param end 0-base end, exclusive
   * @param leftFlank number of bp before the position of interest
   * @param rightFlank number of bp after the position of interest
   */
  KmerIterator(byte[] ascii, int begin, int end, int leftFlank, int rightFlank) {
    this.leftFlank = leftFlank;
    this.rightFlank = rightFlank;
    seq = ascii;
    curr = begin + leftFlank;
    this.end = end - rightFlank;
  }

  @Override
  public boolean hasNext() {
    return curr < end;
  }

  @Override
  public Context next() {
    // there can be a running sum optimization
    KmerContext c = new KmerContext(seq, curr, leftFlank, rightFlank);
    ++curr;
    return c;
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }
}
