package com.bina.hdf5;

/**
 * Created by bayo on 5/4/15.
 */

import java.util.EnumSet;
import com.bina.hdf5.util.ByteBuffer;

public class PBReadBuffer {

    public int size(){
        return data_[0].size();
    }

    public void reserve(int size){
        for(ByteBuffer entry: data_) {
            entry.reserve(size);
        }
    }

    public void clear() {
        for(ByteBuffer entry: data_) {
            entry.clear();
        }
    }

    public void push_back(PBBaseCall bc){
        for(EnumDat e : EnumSet.allOf(EnumDat.class)){
            data_[e.value()].push_back(bc.get(e));
        }
    }

    public byte[] get(EnumDat e){
        return data_[e.value()].data();
    }

    private final ByteBuffer[] data_ = new ByteBuffer[EnumDat.num_fields.value()];
}