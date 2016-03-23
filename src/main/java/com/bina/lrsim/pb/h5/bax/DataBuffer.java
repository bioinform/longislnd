package com.bina.lrsim.pb.h5.bax;

import java.util.ArrayList;
import java.util.List;

import com.bina.lrsim.pb.PBReadBuffer;
import com.bina.lrsim.pb.Spec;

/**
 * Created by bayo on 5/6/15.
 */

class DataBuffer {

  private final List<Integer> score = new ArrayList<>();
  private final List<List<Integer>> readLengths = new ArrayList<>();
  private final PBReadBuffer reads;
  private int numAdapterInsert = 0;

  public DataBuffer(Spec spec, int bufferSize) {
    reads = new PBReadBuffer(spec, bufferSize);
  }

  public void addLast(PBReadBuffer read, List<Integer> readLengths, int score) {
    reads.addLast(read);
    this.score.add(score);
    this.readLengths.add(readLengths);
    numAdapterInsert += readLengths.size();
    if (read.size() != readLengths.get(readLengths.size() - 1)) { throw new RuntimeException("something's wrong with insertion length"); }
  }

  public int getNumReads() {
    return score.size();
  }

  public int getNumAdapterInsert() {
    return numAdapterInsert;
  }

  public PBReadBuffer getReadsRef() {
    return reads;
  }

  public int getLength(int index) {
    final List<Integer> tmp = getReadLengths(index);
    return tmp.get(tmp.size() - 1);
  }

  public List<Integer> getReadLengths(int index) {
    return readLengths.get(index);
  }

  public int getScore(int index) {
    return score.get(index);
  }
}
