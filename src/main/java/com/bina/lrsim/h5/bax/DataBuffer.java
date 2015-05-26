package com.bina.lrsim.h5.bax;

import java.util.ArrayList;

import com.bina.lrsim.h5.pb.PBReadBuffer;
import com.bina.lrsim.h5.pb.PBSpec;

/**
 * Created by bayo on 5/6/15.
 */

class DataBuffer {

  private final ArrayList<Integer> length_score_ = new ArrayList<Integer>();
  private final PBReadBuffer reads_;

  public DataBuffer(PBSpec spec, int bufferSize) {
    reads_ = new PBReadBuffer(spec, bufferSize);
  }

  public void addLast(PBReadBuffer read, int score) {
    reads_.addLast(read);
    length_score_.add(read.size());
    length_score_.add(score);

  }

  public int size() {
    return length_score_.size() / 2;
  }

  public PBReadBuffer getReadsRef() {
    return reads_;
  }

  public int getLength(int index) {
    return length_score_.get(2 * index);
  }

  public int getScore(int index) {
    return length_score_.get(2 * index + 1);
  }
}
