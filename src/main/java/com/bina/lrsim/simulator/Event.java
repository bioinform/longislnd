package com.bina.lrsim.simulator;

import com.bina.lrsim.bioinfo.KmerContext;
import com.bina.lrsim.h5.pb.EnumDat;
import com.bina.lrsim.bioinfo.Context;

import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * Created by bayo on 5/8/15.
 */

public class Event {
    private Context context_;
    private EnumEvent event_;
    private BaseCalls bc_;

    public Event() {
        context_ = null;
        event_ = null;
        bc_ = new BaseCalls();
    }

    public Event(Context c, EnumEvent e, BaseCalls b) {
        context_ = c;
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
        return context_.kmer();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(context_.toString() + " " + event_.toString() + "\n");
        if(null!=bc_) sb.append(bc_.toString());
        return sb.toString();
    }

    public final byte get(int pos, EnumDat e) {
        return bc_.get(pos,e);
    }

    public byte[] data_cpy() {
        return bc_.data_cpy();
    }

    public void write(DataOutputStream dos) throws Exception {
        if(event_.equals(EnumEvent.DELETION)) return;
        if (event_.value() >= EnumEvent.values().length) throw new Exception("invalid i/o format");
        dos.writeInt(EnumEvent.values().length * context_.kmer() + event_.value());
        bc_.write(dos);
    }

    public void read(DataInputStream dis) throws Exception {
        int tmp = dis.readInt();
        context_ = new Context( tmp / EnumEvent.values().length, (short)1 );
        event_ = EnumEvent.value2enum(tmp % EnumEvent.values().length);
        if(null == bc_) bc_ = new BaseCalls();
        bc_.read(dis);
    }
}