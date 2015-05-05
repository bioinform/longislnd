package com.bina.hdf5.util;

/**
 * Created by bayo on 5/5/15.
 */

import java.util.Arrays;

public class ByteBuffer {
    private static final int INITIAL_SIZE = 100000;
    private byte[] data_;
    private int size_;

    public ByteBuffer() {
        this(INITIAL_SIZE);
    }

    public ByteBuffer(int reserve) {
        data_ = new byte[Math.abs(reserve) + 1];
        size_ = 0;
    }

    public void push_back(byte b) {
        if (size_ == data_.length) {
            reserve(data_.length * 2 + 1000);
        }
        data_[size_] = b;
        ++size_;
    }

    public void reserve(int new_size) {
        if (new_size > data_.length) {
            byte[] new_data = Arrays.copyOf(data_, new_size);
            data_ = new_data;
        }
    }

    public byte[] data() {
        return data_;
    }

    public int size() {
        return size_;
    }

    public void clear() {
        size_ = 0;
    }
}
