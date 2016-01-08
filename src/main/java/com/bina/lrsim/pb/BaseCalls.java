package com.bina.lrsim.pb;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import com.bina.lrsim.bioinfo.EnumBP;
import com.google.common.primitives.Bytes;

/**
 * Created by bayo on 5/8/15.
 */
public class BaseCalls {
  private static final int bytePerBase = EnumDat.numBytes;
  // util.ByteBuffer can save a full copy operation everytime a byte[] is extracted from get()
  private final ArrayList<Byte> data_ = new ArrayList<Byte>(bytePerBase);
  private final PBSpec spec; // maybe there's a way to templatize this like in c++

  public BaseCalls(PBSpec spec) {
    this.spec = spec;
  }

  public BaseCalls(PBSpec spec, int size) {
    this(spec);
    resize(size);
  }

  public final void clear() {
    data_.clear();
  }

  public final int size() {
    return data_.size() / bytePerBase;
  }

  public final void push_back() {
    resize(size() + 1);
  }

  public final void resize(int size) {
    if (this.size() > size) {
      while (data_.size() > size * bytePerBase) {
        data_.remove(data_.size() - 1);
      }
    } else if (this.size() < size) {
      this.reserve(size);
      for (int ii = this.size() * bytePerBase; ii < size * bytePerBase; ++ii) {
        data_.add(EnumBP.Invalid.ascii);
      }
    }
  }

  public final void reserve(int size) {
    data_.ensureCapacity(size * bytePerBase);
  }

  public final void set(int pos, EnumDat e, byte b) {
    data_.set(pos * bytePerBase + e.value, b);
  }

  public final byte get(int pos, EnumDat e) {
    return data_.get(pos * bytePerBase + e.value);
  }

  public final byte[] toByteArray() {
    return Bytes.toArray(data_);
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (EnumDat e : spec.getDataSet()) {
      for (int ii = 0; ii < size(); ++ii) {
        if (e.equals(EnumDat.BaseCall) || e.equals(EnumDat.DeletionTag) || e.equals(EnumDat.SubstitutionTag)) {
          sb.append((char) (0xff & this.get(ii, e)));
        } else {
          sb.append(get(ii, e));
        }
        sb.append("\t");
      }
      sb.append(e.path);
      sb.append("\n");
    }
    return sb.toString();
  }

  public void write(DataOutputStream dos) throws IOException {
    dos.writeInt(data_.size());
    for (Byte entry : data_) {
      dos.writeByte(entry);
    }
  }

  public void read(DataInputStream dis) throws IOException {
    this.clear();
    int new_size = dis.readInt();
    for (int ii = 0; ii < new_size; ++ii) {
      data_.add(dis.readByte());
    }
  }
}
