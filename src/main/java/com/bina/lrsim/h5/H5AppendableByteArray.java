package com.bina.lrsim.h5;

import ncsa.hdf.hdf5lib.H5;
import ncsa.hdf.hdf5lib.HDF5Constants;
import ncsa.hdf.hdf5lib.exceptions.HDF5LibraryException;
import ncsa.hdf.object.h5.H5File;
import org.apache.log4j.Logger;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by bayolau on 11/5/15.
 */

// can be more generic than byte, but hdf5 type systsem is nasty to test thoroughly
public class H5AppendableByteArray implements Closeable {
  private final static Logger log = Logger.getLogger(H5AppendableByteArray.class.getName());
  static final int type = HDF5Constants.H5T_NATIVE_UCHAR; // can be generalized, but too convoluted due to java type -> hdf5 type mapping

  private final String path_;
  private int size_ = 0;
  private int dset = 0;
  private int mem_space;

  public H5AppendableByteArray(H5File h5, String path, final int chunk_size) {
    this.path_ = path;
    this.size_ = 0;
    try {
      final int file = h5.open();

      final int plist = H5.H5Pcreate(HDF5Constants.H5P_DATASET_CREATE);
      H5.H5Pset_layout(plist, HDF5Constants.H5D_CHUNKED);
      H5.H5Pset_chunk(plist, 1, new long[] {chunk_size});

      final int file_space = H5.H5Screate_simple(1, new long[] {0}, new long[] {HDF5Constants.H5S_UNLIMITED});
      this.dset = H5.H5Dcreate(file, this.path_, type, file_space, HDF5Constants.H5P_DEFAULT, plist, HDF5Constants.H5P_DEFAULT);

      H5.H5Pclose(plist);
      H5.H5Sclose(file_space);
      this.mem_space = H5.H5Screate_simple(1, new long[] {1000}, null);
    } catch (Exception e) {
      throw new RuntimeException("failed to generate data set at " + path_);
    }
  }

  public int size() {
    return this.size_;
  }

  public int add(byte[] data) {
    try {
      final int new_size = this.size() + data.length;
      H5.H5Dset_extent(this.dset, new long[] {new_size});
      int file_space = H5.H5Dget_space(dset);
      H5.H5Sselect_hyperslab(file_space, HDF5Constants.H5S_SELECT_SET, new long[] {this.size_}, null, new long[] {data.length}, null);

      H5.H5Sset_extent_simple(mem_space, 1, new long[] {data.length}, null);
      H5.H5Dwrite(dset, type, mem_space, file_space, HDF5Constants.H5P_DEFAULT, data);

      H5.H5Sclose(file_space);
      this.size_ = new_size;
    } catch (HDF5LibraryException e) {
      throw new RuntimeException("failed to append data to " + path_);
    }
    return this.size();
  }

  @Override
  public void close() throws IOException {
    try {
      H5.H5Sclose(mem_space);
    } catch (HDF5LibraryException e) {
      log.warn("failed to close mem_space for " + path_);
    }
    try {
      H5.H5Dclose(dset);
    } catch (HDF5LibraryException e) {
      log.warn("failed to close dset for " + path_);
    }
  }
}
