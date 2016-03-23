package com.bina.lrsim.pb.h5.cmp;

/**
 * Created by bayo on 5/2/15.
 */

import com.bina.lrsim.pb.h5.H5ScalarDSIO;
import com.bina.lrsim.pb.EnumDat;
import ncsa.hdf.object.h5.H5File;

class AlnData {

  private H5File h5 = null;
  private String path = null;
  private Object[] data = null; // they don't have to be in byte, there are int and short

  public AlnData(H5File h5, String path) {
    load(h5, path);
  }

  // these might not be byte for other fields
  // still trying to figure out how to do associate class type with enum then cast and generic
  public byte[] get(EnumDat f) {
    try {
      if (null == data[f.value]) {
        data[f.value] = f.mapper.execute(H5ScalarDSIO.Read(h5, path + f.path));
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return (byte[]) data[f.value];
  }

  public void load(H5File h5, String path) {
    this.h5 = h5;
    this.path = path;
    data = new Object[EnumDat.values().length];
  }
}
