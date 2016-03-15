package com.bina.lrsim.simulator;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.bina.lrsim.bioinfo.Context;
import com.bina.lrsim.pb.BaseCalls;
import com.bina.lrsim.pb.EnumDat;
import com.bina.lrsim.pb.Spec;

/**
 * Created by bayo on 5/8/15.
 */

public final class Event {
  private Context context;
  private EnumEvent event;
  private final BaseCalls bc;

  public Event(Spec spec) {
    context = null;
    event = null;
    bc = new BaseCalls(spec);
  }

  public Event(Context c, EnumEvent e, BaseCalls b) {
    context = c;
    event = e;
    bc = b;
  }

  public int size() {
    return bc.size();
  }

  public void resize(int s) {
    bc.resize(s);
  }

  public EnumEvent event() {
    return event;
  }

  public int kmer() {
    return context.kmer();
  }

  public int hp_len() {
    return context.hp_len();
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(context + " " + event + " " + bc.size() + "\n");
    if (null != bc) sb.append(bc.toString());
    return sb.toString();
  }

  public final byte get(int pos, EnumDat e) {
    return bc.get(pos, e);
  }

  public final void set(int pos, EnumDat e, byte b) {
    bc.set(pos, e, b);
  }

  public byte[] data_cpy() {
    return bc.toByteArray();
  }

  // there are 4-byte per 12-byte match event here, which is huge overhead
  // we can save 4 byte by storing 2byte hp-length and 2byte base length if needed
  // we can also save all 4 bytes by writing homopolymer events to a different stream
  // this can be done down the line if we have time
  public void write(DataOutputStream dos) throws IOException {
    dos.writeInt(context.kmer());
    dos.writeInt(EnumEvent.values.length * context.hp_len() + event.ordinal());
    bc.write(dos);
  }

  public void read(DataInputStream dis) throws IOException {
    final int kmer = dis.readInt();
    int tmp = dis.readInt();
    context = new Context(kmer, tmp / EnumEvent.values.length);
    event = EnumEvent.values[tmp % EnumEvent.values.length];
    bc.read(dis);
    for (int ii = 0; ii < size(); ++ii) {
      byte base = get(ii, EnumDat.BaseCall);
      if (base != 'A' && base != 'C' && base != 'T' && base != 'G') {
        throw new RuntimeException("bad event at " + ii + ":" + toString());
      }

    }
  }
}
