package com.bina.lrsim.bioinfo;

import java.util.Arrays;
import java.util.Iterator;

import org.apache.log4j.Logger;

/**
 * Created by bayo on 5/13/15.
 * <p/>
 * An iterator which iterates through homopolymer context sequence.
 * <p/>
 * A valid homopolymer context of length (q-p+1) is centered at a position, p, such that the bp at
 * p-1 is different. The bp from p to q are the same, and the bp of q+1 is different.
 * <p/>
 * CGAAAAAAAAAATCA p q
 * <p/>
 * The iterator for the above situation would output context at
 * <p/>
 * ... (p-1), p, q+1, ......
 */
public final class HPIterator implements Iterator<Context> {
  private final static Logger log = Logger.getLogger(HPIterator.class.getName());
  private final byte[] seq_;
  private int curr_;
  private int end_;
  private final int leftFlank_;
  private final int rightFlank_;
  private final int hpAnchor_;
  private final boolean rc_;

  /**
   * Constructor to iterate the kmer context of through [begin,end) of a ascii stream
   * 
   * @param ascii ascii sequence in the fw direction
   * @param begin 0-base begin
   * @param end 0-base end, exclusive
   * @param leftFlank number of bp before the position of interest
   * @param rightFlank number of bp after the position of interest
   * @param hpAnchor number of bp to anchor homopolymer
   * @param rc if we are doing reverse complement of ascii
   */
  HPIterator(byte[] ascii, int begin, int end, int leftFlank, int rightFlank, int hpAnchor, boolean rc) {
    leftFlank_ = leftFlank;
    rightFlank_ = rightFlank;
    hpAnchor_ = hpAnchor;
    rc_ = rc;
    seq_ = ascii;

    curr_ = begin + leftFlank;
    end_ = end - rightFlank;
    if (rc) {
      curr_ = seq_.length - 1 - curr_;
      end_ = seq_.length - 1 - end_;

      for (; 0 <= curr_ && seq_[curr_] == seq_[curr_ + 1]; --curr_) {}

      for (; end_ + 2 < seq_.length && seq_[end_ + 1] == seq_[end_ + 2]; ++end_) {}

    } else {
      for (; curr_ < seq_.length && seq_[curr_] == seq_[curr_ - 1]; ++curr_) {}
      for (; 0 <= end_ - 2 && seq_[end_ - 1] == seq_[end_ - 2]; --end_) {}
    }
  }

  @Override
  public boolean hasNext() {
    return rc_ ? curr_ > end_ : curr_ < end_;
  }

  @Override
  public Context next() {
    return rc_ ? rc_next() : fw_next();
  }

  private HPContext fw_next() {
    // find the next base which is different
    int diff_pos = curr_ + 1;
    for (; diff_pos < seq_.length && seq_[diff_pos] == seq_[curr_]; ++diff_pos) {}

    if (diff_pos + rightFlank_ > seq_.length) {
      curr_ = diff_pos;
      return null;
    } else {
      final byte[] buffer = Arrays.copyOfRange(seq_, curr_ - leftFlank_, diff_pos + rightFlank_);

      curr_ = diff_pos;
      return new HPContext(buffer, leftFlank_, rightFlank_, hpAnchor_);
    }
  }

  private HPContext rc_next() {
    // find the next base which is different
    int diff_pos = curr_ - 1;
    for (; diff_pos >= 0 && seq_[diff_pos] == seq_[curr_]; --diff_pos) {}

    if (diff_pos - rightFlank_ < -1) {
      curr_ = diff_pos;
      return null;
    } else {
      final byte[] buffer = new byte[leftFlank_ + curr_ - diff_pos + rightFlank_];
      int kk = 0;

      for (int pos = curr_ + leftFlank_; kk < buffer.length; ++kk, --pos) {
        buffer[kk] = EnumBP.ascii_rc(seq_[pos]);
      }

      curr_ = diff_pos;
      return new HPContext(buffer, leftFlank_, rightFlank_, hpAnchor_);
    }
  }


  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }
}
