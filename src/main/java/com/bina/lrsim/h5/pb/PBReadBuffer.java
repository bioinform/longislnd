package com.bina.lrsim.h5.pb;

/**
 * Created by bayo on 5/4/15.
 */


import com.bina.lrsim.simulator.BaseCalls;
import com.bina.lrsim.util.ByteBuffer;
import org.apache.log4j.Logger;

import java.util.EnumMap;

public class PBReadBuffer {
    private static final int INITIAL_SIZE = 1000;
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

    public void addLast(BaseCalls other) {
        byte[] buffer = new byte[other.size()];
        for (EnumDat e : EnumDat.getBaxSet()) {
            for (int pp = 0; pp < other.size(); ++pp) {
                buffer[pp]=other.get(pp,e);
            }
            data_.get(e).addLast(buffer);
        }
    }

    public void addLast(PBReadBuffer other) {
        for (EnumDat e : EnumDat.getBaxSet()) {
            data_.get(e).addLast(other.get(e));
        }
    }

    public void addLast(byte[] other, int begin, int end) throws Exception{
        if((end-begin) % EnumDat.getBaxSet().size() != 0) throw new Exception("invalid size" );
        for(int itr=begin; itr<end; itr+=EnumDat.getBaxSet().size()){
            for (EnumDat e : EnumDat.getBaxSet()) {
                data_.get(e).addLast(other[itr+e.value()]);
            }
        }
    }

    public ByteBuffer get(EnumDat e) {
        return data_.get(e);
    }

    private final EnumMap<EnumDat,ByteBuffer> data_  = new EnumMap<EnumDat, ByteBuffer>(EnumDat.class);
}