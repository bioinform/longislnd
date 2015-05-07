package com.bina.hdf5;

/**
 * Created by bayo on 5/4/15.
 */


import java.util.EnumMap;
import com.bina.hdf5.util.ByteBuffer;
import org.apache.log4j.Logger;

public class PBReadBuffer {
    private static final int INITIAL_SIZE = 100000;
    private final static Logger log = Logger.getLogger(PBReadBuffer.class.getName());

    public PBReadBuffer() {
        this(INITIAL_SIZE);
    }

    public PBReadBuffer(int reserveSize) {
        for(EnumDat e: EnumDat.getBaxSet()){
            data_.put(e,new ByteBuffer(reserveSize));
        }
        reserve(reserveSize);
    }

    public int size() {
        return data_.get(EnumDat.BaseCall).size();
    }

    public void reserve(int size) {
        for (EnumDat e : EnumDat.getBaxSet()) {
            data_.get(e).reserve(size);
        }
    }

    public void clear() {
        for (EnumDat e : EnumDat.getBaxSet()) {
            data_.get(e).clear();
        }
    }


    // should "templatize" when have time

    public void addLast(PBBaseCall other) {
        for (EnumDat e : EnumDat.getBaxSet()) {
            data_.get(e).addLast(other.get(e));
        }
    }

    public void addLast(PBReadBuffer other) {
        for (EnumDat e : EnumDat.getBaxSet()) {
            data_.get(e).addLast(other.get(e));
        }
    }

    public ByteBuffer get(EnumDat e) {
        return data_.get(e);
    }

    private final EnumMap<EnumDat,ByteBuffer> data_  = new EnumMap<EnumDat, ByteBuffer>(EnumDat.class);
}