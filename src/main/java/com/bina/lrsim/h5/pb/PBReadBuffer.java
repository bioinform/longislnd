package com.bina.lrsim.h5.pb;

/**
 * Created by bayo on 5/4/15.
 */


import org.apache.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.util.EnumMap;

public class PBReadBuffer {
    private static final int INITIAL_SIZE = 1000;
    private final static Logger log = Logger.getLogger(PBReadBuffer.class.getName());
    //util.ByteBuffer can save a full copy operation everytime a byte[] is extracted
    private final EnumMap<EnumDat, ByteArrayOutputStream> data_ = new EnumMap<EnumDat, ByteArrayOutputStream>(EnumDat.class);

    public PBReadBuffer() {
        this(INITIAL_SIZE);
    }

    public PBReadBuffer(int reserveSize) {
        for (EnumDat e : EnumDat.getBaxSet()) {
            data_.put(e, new ByteArrayOutputStream(reserveSize));
        }
        reserve(reserveSize);
    }

    public int size() {
        return data_.get(EnumDat.BaseCall).size();
    }

    public void reserve(int size) {
        //ByteArrayOutputStream's ensureCapacity is private, oh well
        /*
        for (EnumDat e : EnumDat.getBaxSet()) {
            data_.get(e).reserve(size);
        }
        */
    }


    // should "templatize" when have time

    public void clear() {
        for (EnumDat e : EnumDat.getBaxSet()) {
            data_.get(e).reset();
        }
    }

    public void addLast(BaseCalls other) {
        for (EnumDat e : EnumDat.getBaxSet()) {
            for (int pp = 0; pp < other.size(); ++pp) {
                data_.get(e).write(other.get(pp, e));
            }
        }
    }

    public void addLast(PBReadBuffer other) {
        for (EnumDat e : EnumDat.getBaxSet()) {
            final byte[] tmp = other.get(e).toByteArray();
            data_.get(e).write(tmp, 0, tmp.length); // the (byte[]) version throws IO exception
        }
    }

    public void addLast(byte[] other, int begin, int end) {
        if ((end - begin) % EnumDat.getBaxSet().size() != 0) throw new RuntimeException("invalid size");
        for (int itr = begin; itr < end; itr += EnumDat.getBaxSet().size()) {
            for (EnumDat e : EnumDat.getBaxSet()) {
                data_.get(e).write(other[itr + e.value]);
            }
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (EnumDat e : EnumDat.getBaxSet()) {
            sb.append("\n");
            sb.append(e.path + "\n");
            if (e.equals(EnumDat.BaseCall) || e.equals(EnumDat.DeletionTag) || e.equals(EnumDat.SubstitutionTag)) {
                for (int ii = 0; ii < data_.get(e).size(); ++ii) {
//                    sb.append((char)data_.get(e).get(ii));
                }
            } else {
                for (int ii = 0; ii < data_.get(e).size(); ++ii) {
//                    sb.append((char)(data_.get(e).get(ii)+33));
                }
            }
        }
        return sb.toString();
    }

    public ByteArrayOutputStream get(EnumDat e) {
        return data_.get(e);
    }
}