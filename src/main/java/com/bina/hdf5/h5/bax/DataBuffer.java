package com.bina.hdf5.h5.bax;

import com.bina.hdf5.PBReadBuffer;
import com.bina.hdf5.util.IntBuffer;

/**
 * Created by bayo on 5/6/15.
 */

class DataBuffer {

    public DataBuffer(int bufferSize) {
        reads_.reserve(bufferSize);
    }

    public void addLast(PBReadBuffer read, int score) {
        reads_.addLast(read);
        length_score_.addLast(read.size());
        length_score_.addLast(score);

    }

    public PBReadBuffer reads() {
        return reads_;
    }

    public IntBuffer length_score() {
        return length_score_;
    }

    private final IntBuffer length_score_ = new IntBuffer();
    private final PBReadBuffer reads_ = new PBReadBuffer();
}
