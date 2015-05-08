package com.bina.hdf5.util;


/**
 * Created by bayo on 5/6/15.
 */

import java.util.Arrays;

public class IntBuffer {
    private int[] data_;
    private int size_;

    public IntBuffer() {
        this(64 / 4);
    }

    public IntBuffer(int reserve) {
        data_ = new int[Math.abs(reserve) + 1];
        size_ = 0;
    }

    public void addLast(int i) {
        if (size_ >= data_.length) {
            reserve(size_ * 2 + 1000);
        }
        data_[size_] = i;
        ++size_;
    }

    public void addLast(int[] other) {
        final int newSize = size_ + other.length;
        if (newSize >= data_.length) {
            reserve(newSize * 2 + 1000);
        }
        // there's probably something like std::copy in java?
        for (int ii = 0; ii < other.length; ++ii, ++size_) {
            data_[size_] = other[ii];
        }
    }

    public void reserve(int new_size) {
        if (new_size > data_.length) {
            int[] new_data = Arrays.copyOf(data_, new_size);
            for(int ii = 0 ; ii < size_ ; ++ii){
                new_data[ii] = data_[ii];
            }
            data_ = new_data;
        }
    }

    public int[] data() {
        return data_;
    }

    public int size() {
        return size_;
    }

    public void clear() {
        size_ = 0;
    }
}
