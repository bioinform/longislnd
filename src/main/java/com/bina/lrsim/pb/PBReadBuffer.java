package com.bina.lrsim.pb;

/**
 * Created by bayo on 5/4/15.
 */


import java.io.ByteArrayOutputStream;
import java.util.EnumMap;
import java.util.Map;

import org.apache.log4j.Logger;

public class PBReadBuffer {
  private static final int INITIAL_SIZE = 1000;
  private final static Logger log = Logger.getLogger(PBReadBuffer.class.getName());
  // util.ByteBuffer can save a full copy operation everytime a byte[] is extracted
  private final Map<EnumDat, ByteArrayOutputStream> data2OutputStream = new EnumMap<>(EnumDat.class);
  private final Spec spec;

  public PBReadBuffer(Spec spec) {
    this(spec,INITIAL_SIZE);
  }

  public PBReadBuffer(Spec spec, int reserveSize) {
    this.spec = spec;
    for (EnumDat e : spec.getDataSet()) {
      data2OutputStream.put(e, new ByteArrayOutputStream(reserveSize));
    }
    reserve(reserveSize);
  }

  /**
   * return count of base calls stored in the buffer
   * @return
   */
  public int size() {
    return data2OutputStream.get(EnumDat.BaseCall).size();
  }

  public void reserve(int size) {
    // ByteArrayOutputStream's ensureCapacity is private, oh well
    /*
     * for (EnumDat e : EnumDat.getBaxSet()) { data2OutputStream.get(e).reserve(size); }
     */
  }


  // should "templatize" when have time

  public void clear() {
    for (EnumDat e : spec.getDataSet()) {
      data2OutputStream.get(e).reset();
    }
    if (this.size() != 0) {
      log.error("couldn't clear buffer");
      throw new RuntimeException("different lengths!");
    }
  }

  /**
   * take 3 arrays of data, base calls, scores, and other fields, store them in buffer
   * @param asciiSeq
   * @param defaultSeq
   * @param defaultScores
   */
  public void addASCIIBases(byte[] asciiSeq, byte[] defaultSeq, byte[] defaultScores) {
    for (EnumDat e : spec.getDataSet()) {
      final byte[] buffer;
      if (e == EnumDat.BaseCall) {
        buffer = asciiSeq;
      } else if (e.isScore) {
        buffer = defaultScores;
      } else {
        //a question is, for all data types other than score and base call, values will be identical?
        buffer = defaultSeq;
      }
      //assume all input arrays are of same lengths
      data2OutputStream.get(e).write(buffer, 0, asciiSeq.length);
    }
  }

  public void addLast(BaseCalls other) {
    for (EnumDat e : spec.getDataSet()) {
      for (int pp = 0; pp < other.size(); ++pp) {
        data2OutputStream.get(e).write(other.get(pp, e));
      }
    }
  }

  /**
   * append an input buffer's data to current buffer
   * @param other
   */
  public void addLast(PBReadBuffer other) {
    for (EnumDat e : spec.getDataSet()) {
      final byte[] tmp = other.get(e).toByteArray();
      data2OutputStream.get(e).write(tmp, 0, tmp.length); // the (byte[]) version throws IO exception
    }
  }

  /**
   * append an array of base call data (all deletion, insertion scores and other
   * fields are stored in one array, accessible by careful indexing) to current
   * buffer
   *
   * end - begin must be multiples of total number of data types emitted from
   * a base call (refer to @EnumDat).
   *
   * @param baseCallFieldsForAllKmers
   * @param begin
   * @param end
   */
  public void addLast(byte[] baseCallFieldsForAllKmers, int begin, int end) {
    if ((end - begin) % EnumDat.numBytes != 0) throw new RuntimeException("invalid size! Index range is not multiple of length of base call fields.");
    for (int itr = begin; itr < end; itr += EnumDat.numBytes) {
      for (EnumDat baseCallDataType : spec.getDataSet()) {
        data2OutputStream.get(baseCallDataType).write(baseCallFieldsForAllKmers[itr + baseCallDataType.value]);
      }
    }
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (EnumDat e : spec.getDataSet()) {
      sb.append("\n");
      sb.append(e.path + "\n");
      //TODO: consider removing or uncommenting this part
      if (e == EnumDat.BaseCall || e == EnumDat.DeletionTag || e == EnumDat.SubstitutionTag) {
        for (int ii = 0; ii < data2OutputStream.get(e).size(); ++ii) {
          // sb.append((char)data2OutputStream.get(e).get(ii));
        }
      } else {
        for (int ii = 0; ii < data2OutputStream.get(e).size(); ++ii) {
          // sb.append((char)(data2OutputStream.get(e).get(ii)+33));
        }
      }
    }
    return sb.toString();
  }

  /**
   * get output byte stream for a particular data type of a base call
   * @param e
   * @return
   */
  public ByteArrayOutputStream get(EnumDat e) {
    return data2OutputStream.get(e);
  }
}
