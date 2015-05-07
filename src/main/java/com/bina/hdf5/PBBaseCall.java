package com.bina.hdf5;

/**
 * Created by bayo on 5/4/15.
 */
public class PBBaseCall {

    public void set(EnumDat e, byte b) {
        data_[e.value()] = b;
    }

    public byte get(EnumDat e) {
        return data_[e.value()];
    }

    private final byte[] data_ = new byte[EnumDat.values().length];
}
