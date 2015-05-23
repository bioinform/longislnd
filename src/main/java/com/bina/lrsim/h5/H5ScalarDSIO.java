package com.bina.lrsim.h5;

/**
 * Created by bayo on 5/1/15.
 */

import ncsa.hdf.object.Dataset;
import ncsa.hdf.object.h5.H5File;
import ncsa.hdf.object.h5.H5ScalarDS;
import org.apache.log4j.Logger;

import java.io.IOException;

public class H5ScalarDSIO {
  private final static Logger log = Logger.getLogger(H5ScalarDSIO.class.getName());

  public static <T> T Read(H5File h5, String path) throws IOException {
    log.debug("reading from " + path);
    try {
      H5ScalarDS dset = (H5ScalarDS) h5.get(path);
      return (T) dset.read();
    } catch (Exception e) { // H5 API throws this base class
      throw new IOException("failed to read from " + path);
    }
  }

  public static Dataset Write(H5File h5, String path, Object buffer, long[] dims) throws IOException {
    try {
      return h5.createScalarDS(path,
                               null,
                               EnumH5Type.getEnum(buffer.getClass()).getH5Datatype(buffer, dims),
                               dims,
                               null,
                               null,
                               0,
                               buffer);
    } catch (Exception e) { // H5 API throws this base class
      throw new IOException("failed to write to " + path);
    }
  }
}
