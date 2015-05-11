package com.bina.hdf5.simulator;

import com.bina.hdf5.h5.pb.EnumDat;

import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * Created by bayo on 5/8/15.
 */

public class Event {
    public Event() {
        kmer_= -1;
        event_ = null;
        bc_ = new BaseCalls();
    }

    public Event(int k, EnumEvent e, BaseCalls b) {
        kmer_ = k;
        event_ = e;
        bc_ = b;
    }

    public int size() {
        return bc_.size();
    }

    public EnumEvent event() {
        return event_;
    }

    public int kmer() {
        return kmer_;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(kmer_ + " " + event_.toString() + "\n");
        if(null!=bc_) sb.append(bc_.toString());
        return sb.toString();
    }

    public final byte get(int pos, EnumDat e) {
        return bc_.get(pos,e);
    }

    public byte[] data_cpy() {
        return bc_.data_cpy();
    }


    private int kmer_;
    private EnumEvent event_;
    private BaseCalls bc_;

    public void write(DataOutputStream dos) throws Exception {
        if (event_.value() >= EnumEvent.values().length) throw new Exception("invalid i/o format");
        dos.writeInt(EnumEvent.values().length * kmer_ + event_.value());
        bc_.write(dos);
    }

    public void read(DataInputStream dis) throws Exception {
        int tmp = dis.readInt();
        kmer_ = tmp / EnumEvent.values().length;
        event_ = EnumEvent.value2enum(tmp % EnumEvent.values().length);
        if(null == bc_) bc_ = new BaseCalls();
        bc_.read(dis);
    }
}