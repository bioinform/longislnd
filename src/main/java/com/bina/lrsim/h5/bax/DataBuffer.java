package com.bina.lrsim.h5.bax;

import com.bina.lrsim.h5.pb.PBReadBuffer;

import java.util.ArrayList;

/**
 * Created by bayo on 5/6/15.
 */

class DataBuffer {

    public DataBuffer(int bufferSize) {
        reads_.reserve(bufferSize);
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
        return length_score_.get(2*index);
    }

    public int getScore(int index) {
        return length_score_.get(2*index+1);
    }

    private final ArrayList<Integer> length_score_ = new ArrayList<Integer>();
    private final PBReadBuffer reads_ = new PBReadBuffer();
}
