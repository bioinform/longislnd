package com.bina.lrsim.h5.pb;

import com.bina.lrsim.util.ByteBuffer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by bayo on 5/8/15.
 */
public class BaseCalls {
    public BaseCalls(){
    }

    public BaseCalls(int size){
        resize(size);
    }

    public final void clear() {
        data_.clear();
    }

    public final int size() {
        return data_.size()/bytePerBase;
    }

    public final void push_back() {
        resize(size() + 1);
    }

    public final void resize(int size){
        data_.resize(size * bytePerBase);
    }
    public final void reserve(int size){
        data_.reserve(size * bytePerBase);
    }

    public final void set(int pos, EnumDat e, byte b) {
        data_.set(pos*bytePerBase+ e.value, b);
    }

    public final byte get(int pos, EnumDat e) {
        return data_.get(pos*bytePerBase+ e.value);
    }

    public final byte[] data_cpy() {
        return data_.data_cpy();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for(EnumDat e: EnumDat.getBaxSet()){
            for(int ii = 0; ii < size(); ++ii){
                if(e.equals(EnumDat.BaseCall)){
                    sb.append((char)(0xff&this.get(ii,e)));
                }
                else{
                    sb.append(get(ii,e));
                }
                sb.append("\t");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    private static final int bytePerBase = EnumDat.getBaxSet().size();
    final private ByteBuffer data_ = new ByteBuffer(bytePerBase);

    public void write(DataOutputStream dos) throws IOException {
        data_.write(dos);
    }

    public void read(DataInputStream dis) throws IOException {
        data_.read(dis);
    }
}
