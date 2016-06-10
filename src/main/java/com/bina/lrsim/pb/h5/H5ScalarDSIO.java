package com.bina.lrsim.pb.h5;

/**
 * Created by bayo on 5/1/15.
 */

import com.bina.lrsim.util.H5Wrapper;
import ncsa.hdf.object.Dataset;
import ncsa.hdf.object.h5.H5File;
import ncsa.hdf.object.h5.H5ScalarDS;
import org.apache.log4j.Logger;

import java.io.IOException;

public class H5ScalarDSIO {
  private final static Logger log = Logger.getLogger(H5ScalarDSIO.class.getName());

  public static Object Read(H5File h5, String path) throws IOException {
    log.debug("reading from " + path);
    try {
      H5ScalarDS dset = (H5ScalarDS) h5.get(path);
      return dset.read();
    } catch (Exception e) { // H5 API throws this base class
      throw new IOException("failed to read from " + path);
    }
  }

  public static Object Read(H5Wrapper h5, String path) throws IOException {
    return Read(h5.getH5(), path);
  }

  public static Dataset Write(H5File h5, String path, Object buffer, long[] dims, boolean isSigned) throws IOException {
    try {
      return h5.createScalarDS(path, null, EnumH5Type.getH5Datatype(buffer, dims, isSigned), dims, null, null, 0, buffer);
    } catch (Exception e) { // H5 API throws this base class
      throw new IOException("failed to write to " + path);
    }
  }
}
