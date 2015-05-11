package com.bina.hdf5.util;

/**
 * Created by bayo on 5/5/15.
 */

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class ByteBuffer {
    private byte[] data_;
    private int size_;


    public ByteBuffer() {
        this(64);
    }

    public ByteBuffer(int reserve) {
        data_ = new byte[Math.abs(reserve) + 1];
        size_ = 0;
    }

    public void addLast(byte other) {
        final int newSize = size_ + 1;
        if (newSize >= data_.length) {
            reserve(newSize * 2 + 1000);
        }
        data_[size_] = other;
        ++size_;
    }

    public void addLast(byte[] other) {
        final int newSize = size_ + other.length;
        if (newSize >= data_.length) {
            reserve(newSize * 2 + 1000);
        }
        // there's probably something like std::copy in java?
        for (int ii = 0; ii < other.length; ++ii, ++size_) {
            data_[size_] = other[ii];
        }
    }

    public void addLast(ByteBuffer other) {
        final int newSize = size_ + other.size();
        if (newSize >= data_.length) {
            reserve(newSize * 2 + 1000);
        }
        // there's probably something like std::copy in java?
        for (int ii = 0; ii < other.size(); ++ii, ++size_) {
            data_[size_] = other.data_ref()[ii];
        }
    }

    public void reserve(int new_size) {
        if (new_size > data_.length) {
            byte[] new_data = Arrays.copyOf(data_, new_size);
            for(int ii = 0 ; ii < size_ ; ++ii){
                new_data[ii] = data_[ii];
            }
            data_ = new_data;
        }
    }

    public void resize(int new_size){
        reserve(new_size);
        size_=new_size;
    }

    public byte[] data_ref() {
        return data_;
    }

    public byte[] data_cpy() {
        return Arrays.copyOf(data_,size_);
    }

    public int size() {
        return size_;
    }

    public void clear() {
        size_ = 0;
    }

    public void write(DataOutputStream dos) throws IOException {
        dos.writeInt(size_);
        dos.write(data_, 0, size_);
    }

    public void read(DataInputStream dis) throws IOException {
        size_ = dis.readInt();
        resize(size_);
        dis.read(data_, 0, size_);
    }
}
