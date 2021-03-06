package com.bina.lrsim.pb.h5.cmp;

/**
 * Created by bayo on 5/1/15.
 */

import com.bina.lrsim.pb.h5.H5ScalarDSIO;
import ncsa.hdf.object.h5.H5File;
import ncsa.hdf.object.h5.H5ScalarDS;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Arrays;

class AlnIndex {

  private final static Logger log = Logger.getLogger(AlnIndex.class.getName());
  private int[] data = null;
  private int numRows;
  private int numCols;

  public AlnIndex() {
    numRows = 0;
    numCols = EnumIdx.values().length;
    data = new int[numCols * 1000];
  }

  public AlnIndex(H5File h5) {
    load(h5);
  }

  public void add(int[] single) {
    while (data.length < (numRows + 1) * numCols) { // just to be safe, but no sane situation will do it twice
      data = Arrays.copyOf(data, (data.length + numCols) * 2);
    }
    final int shift = numCols * numRows;
    for (int cc = 0; cc < numCols; ++cc) {
      data[shift + cc] = single[cc];
    }
    ++numRows;
  }

  public void save(H5File h5, String path) throws IOException {
    final long[] dims = new long[] {(long) numRows, (long) numCols};
    H5ScalarDSIO.Write(h5, path, data, dims, false);
  }

  public int size() {
    return numRows;
  }

  public int get(int alignmentIndex, EnumIdx c) {
    return data[alignmentIndex * numCols + c.ordinal()];
  }

  public int[] get(int alignmentIndex) {
    final int begin = alignmentIndex * numCols;
    return Arrays.copyOfRange(data, begin, begin + numCols);
  }


  public boolean load(H5File h5) {
    try {
      H5ScalarDS obj = (H5ScalarDS) h5.get("/AlnInfo/AlnIndex");
      obj.init();

      long[] dims = obj.getDims();
      log.debug("/AlnInfo/AlnIndex dimensions: " + dims[0] + " " + dims[1]);
      if (dims.length != 2) throw new RuntimeException("bad AlnIndex dimension");
      final int nr = (int) dims[0];
      final int nc = (int) dims[1];
      if (nc != 22) throw new RuntimeException("bad AlinIndex num_col");

      int[] d = (int[]) obj.getData();
      if (d.length != nr * nc) throw new RuntimeException("bad AlnIndex data_ref");

      data = Arrays.copyOf(d, d.length);
      numRows = nr;
      numCols = nc;
    } catch (Exception e) {
      log.info(e, e);
      log.info(e.toString());
      return true;
    }
    long nIns = 0;
    long nDel = 0;
    long nRef = 0;
    for (int ii = 0; ii < size(); ++ii) {
      nIns += get(ii, EnumIdx.nIns);
      nDel += get(ii, EnumIdx.nDel);
      nRef += get(ii, EnumIdx.tEnd) - get(ii, EnumIdx.tStart);
    }
    log.info("alignment with " + nIns
             + "("
             + (double) nIns
             / (double) nRef
             + ") ins and "
             + nDel
             + "("
             + (double) nDel
             / (double) nRef
             + ") del "
             + nRef
             + " ref");
    return false;
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (int row = 0; row < numRows; ++row) {
      sb.append("AlnIdx " + row + "\t");
      for (int col = 0; col < numCols; ++col) {
        sb.append(" ");
        sb.append(data[row * numCols + col]);
      }
      sb.append("\n");
    }
    return sb.toString();
  }
}
