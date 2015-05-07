package com.bina.hdf5;

/**
 * Created by bayo on 5/4/15.
 */

import com.bina.hdf5.util.ByteBuffer;
import org.apache.log4j.Logger;

public class PBReadBuffer {
    private static final int INITIAL_SIZE = 100000;
    private final static Logger log = Logger.getLogger(PBReadBuffer.class.getName());

    public PBReadBuffer() {
        this(INITIAL_SIZE);
    }

    public PBReadBuffer(int reserveSize) {
        for (int ii = 0; ii < EnumDat.NumFields.value(); ++ii) {
            data_[ii] = new ByteBuffer(reserveSize);
        }
        reserve(reserveSize);
    }

    public int size() {
        return data_[0].size();
    }

    public void reserve(int size) {
        for (ByteBuffer entry : data_) {
            entry.reserve(size);
        }
    }

    public void clear() {
        for (ByteBuffer entry : data_) {
            entry.clear();
        }
    }


    // should "templatize" when have time

    public void addLast(PBBaseCall bc) {
        for (EnumDat e : EnumDat.getBaxSet()) {
            data_[e.value()].addLast(bc.get(e));
        }
    }

    public void addLast(PBReadBuffer other) {
        for (EnumDat e : EnumDat.getBaxSet()) {
            data_[e.value()].addLast(other.get(e));
        }
    }

    public ByteBuffer get(EnumDat e) {
        return data_[e.value()];
    }

    private final ByteBuffer[] data_ = new ByteBuffer[EnumDat.NumFields.value()];
}