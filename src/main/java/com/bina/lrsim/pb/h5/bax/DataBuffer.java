package com.bina.lrsim.pb.h5.bax;

import java.util.ArrayList;
import java.util.List;

import com.bina.lrsim.pb.PBReadBuffer;
import com.bina.lrsim.pb.Spec;

/**
 * Created by bayo on 5/6/15.
 */

class DataBuffer {

  private final List<Integer> score_ = new ArrayList<>();
  private final List<List<Integer>> read_lengths_ = new ArrayList<>();
  private final PBReadBuffer reads_;
  private int numAdapterInsert = 0;

  public DataBuffer(Spec spec, int bufferSize) {
    reads_ = new PBReadBuffer(spec, bufferSize);
  }

  public void addLast(PBReadBuffer read, List<Integer> readLengths, int score) {
    reads_.addLast(read);
    score_.add(score);
    read_lengths_.add(readLengths);
    numAdapterInsert += readLengths.size();
    if (read.size() != readLengths.get(readLengths.size() - 1)) { throw new RuntimeException("something's wrong with insertion length"); }
  }

  public int getNumReads() {
    return score_.size();
  }

  public int getNumAdapterInsert() {
    return numAdapterInsert;
  }

  public PBReadBuffer getReadsRef() {
    return reads_;
  }

  public int getLength(int index) {
    final List<Integer> tmp = getReadLengths(index);
    return tmp.get(tmp.size() - 1);
  }

  public List<Integer> getReadLengths(int index) {
    return read_lengths_.get(index);
  }

  public int getScore(int index) {
    return score_.get(index);
  }
}
