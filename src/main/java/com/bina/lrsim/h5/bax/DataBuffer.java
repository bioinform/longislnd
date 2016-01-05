package com.bina.lrsim.h5.bax;

import java.util.ArrayList;

import com.bina.lrsim.h5.pb.PBReadBuffer;
import com.bina.lrsim.h5.pb.PBSpec;

/**
 * Created by bayo on 5/6/15.
 */

class DataBuffer {

  private final ArrayList<Integer> score_ = new ArrayList<Integer>();
  private final ArrayList<ArrayList<Integer>> read_lengths_ = new ArrayList<>();
  private final PBReadBuffer reads_;
  private int numAdapterInsert = 0;

  public DataBuffer(PBSpec spec, int bufferSize) {
    reads_ = new PBReadBuffer(spec, bufferSize);
  }

  public void addLast(PBReadBuffer read, ArrayList<Integer> readLengths, int score) {
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
    final ArrayList<Integer> tmp = getReadLengths(index);
    return tmp.get(tmp.size() - 1);
  }

  public ArrayList<Integer> getReadLengths(int index) {
    return read_lengths_.get(index);
  }

  public int getScore(int index) {
    return score_.get(index);
  }
}
