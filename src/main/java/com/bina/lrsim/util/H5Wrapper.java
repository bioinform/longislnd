package com.bina.lrsim.util;

import ncsa.hdf.hdf5lib.exceptions.HDF5Exception;
import ncsa.hdf.object.h5.H5File;
import org.apache.log4j.Logger;

import java.io.Closeable;

/**
 * Created by bayolau on 6/10/16.
 */
public class H5Wrapper implements Closeable {
  private final static Logger log = Logger.getLogger(H5Wrapper.class.getName());
  private final H5File h5;
  private final String name;

  public H5Wrapper(String name, int mode) {
    this.name = name;
    this.h5 = new H5File(this.name, mode);
  }

  public H5File getH5() {
    return h5;
  }

  @Override
  public void close() {
    try {
      h5.close();
    } catch (HDF5Exception e) {
      log.error("Failed to close h5 " + this.name);
    }
  }
}
