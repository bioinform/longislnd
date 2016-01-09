package com.bina.lrsim.pb.h5;

/**
 * Created by bayo on 5/3/15.
 */

import ncsa.hdf.hdf5lib.H5;

import ncsa.hdf.hdf5lib.HDF5Constants;
import ncsa.hdf.hdf5lib.exceptions.HDF5Exception;
import ncsa.hdf.object.FileFormat;
import ncsa.hdf.object.h5.H5File;
import org.apache.log4j.Logger;

import java.io.IOException;

public class H5Append {
  public static void run(String filename) {
    oo(filename);
  }
  public static void oo(String filename) {
    H5File oh5 = new H5File(filename, FileFormat.CREATE);
    H5AppendableByteArray a = new H5AppendableByteArray(oh5, "oo", 10000);
    byte[] c1 = new byte[] {1, 3, 5, 7, 9, 11, 13};
    byte[] c2 = new byte[] {2, 4, 6, 8, 10, 12, 14, 16, 18, 20, 22};
    a.add(c1);
    a.add(c2);
    try {
      a.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    try {
      oh5.close();
    } catch (HDF5Exception e) {
      e.printStackTrace();
    }

  }
  public static void mod(String filename) {
    try {
      H5File oh5 = new H5File(filename, FileFormat.CREATE);
      int file  = oh5.open();

      log.info(file);
      log.info(oh5.open());
      log.info(oh5.open());

      int file_space = H5.H5Screate_simple(1, new long[] {0}, new long[]{HDF5Constants.H5S_UNLIMITED});

      int plist = H5.H5Pcreate(HDF5Constants.H5P_DATASET_CREATE);
      H5.H5Pset_layout(plist, HDF5Constants.H5D_CHUNKED);
      final int bayo_chunk_size = 10000;
      H5.H5Pset_chunk(plist, 1, new long[] {bayo_chunk_size});

      int type = HDF5Constants.H5T_NATIVE_UCHAR;

      int dset = H5.H5Dcreate(file, "dset1", type, file_space, HDF5Constants.H5P_DEFAULT, plist, HDF5Constants.H5P_DEFAULT);

      H5.H5Pclose(plist);
      H5.H5Sclose(file_space);

      byte[] c1 = new byte[] {1, 3, 5, 7, 9, 11, 13};
      byte[] c2 = new byte[] {2, 4, 6, 8, 10, 12, 14, 16, 18, 20, 22};
      int mem_space = H5.H5Screate_simple(1, new long[] {c1.length+c2.length}, null);
      {
        log.info(H5.H5Dget_storage_size(dset));
        H5.H5Dset_extent(dset, new long[] {c1.length});
        file_space = H5.H5Dget_space(dset);
        H5.H5Sselect_hyperslab(file_space, HDF5Constants.H5S_SELECT_SET, new long[]{0}, null, new long[]{c1.length}, null);

        H5.H5Sset_extent_simple(mem_space, 1, new long[]{c1.length}, null);
        H5.H5Dwrite(dset, type, mem_space, file_space, HDF5Constants.H5P_DEFAULT, c1);

        H5.H5Sclose(file_space);
        log.info(H5.H5Dget_storage_size(dset));
      }
      {
        log.info(H5.H5Dget_storage_size(dset));
        H5.H5Dset_extent(dset, new long[] {c1.length + c2.length});
        file_space = H5.H5Dget_space(dset);
        H5.H5Sselect_hyperslab(file_space, HDF5Constants.H5S_SELECT_SET, new long[] {c1.length}, null, new long[]{c2.length}, null);

        H5.H5Sset_extent_simple(mem_space, 1, new long[]{c2.length}, null);
        H5.H5Dwrite(dset, type, mem_space, file_space, HDF5Constants.H5P_DEFAULT, c2);

        H5.H5Sclose(file_space);
        log.info(H5.H5Dget_storage_size(dset));
      }

      H5.H5Sclose(mem_space);
      H5.H5Dclose(dset);
//      H5.H5Fclose(file);
      oh5.close();
    } catch (HDF5Exception e) {
      e.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  public static void example(String filename) {
    try {
      int file = H5.H5Fcreate(filename, HDF5Constants.H5F_ACC_TRUNC, HDF5Constants.H5P_DEFAULT, HDF5Constants.H5P_DEFAULT);

      long[] max_dims = new long[] {HDF5Constants.H5S_UNLIMITED};
      int file_space = H5.H5Screate_simple(1, new long[] {0}, max_dims);

      int plist = H5.H5Pcreate(HDF5Constants.H5P_DATASET_CREATE);
      H5.H5Pset_layout(plist, HDF5Constants.H5D_CHUNKED);
      final int bayo_chunk_size = 10000;
      H5.H5Pset_chunk(plist, 1, new long[] {bayo_chunk_size});

      int type = HDF5Constants.H5T_NATIVE_UCHAR;

      int dset = H5.H5Dcreate(file, "dset1", type, file_space, HDF5Constants.H5P_DEFAULT, plist, HDF5Constants.H5P_DEFAULT);

      H5.H5Pclose(plist);
      H5.H5Sclose(file_space);

      byte[] c1 = new byte[] {1, 3, 5, 7, 9, 11, 13};
      byte[] c2 = new byte[] {2, 4, 6, 8, 10, 12, 14, 16, 18, 20, 22};
      {
        log.info(H5.H5Dget_storage_size(dset));
        H5.H5Dset_extent(dset, new long[] {c1.length});
        file_space = H5.H5Dget_space(dset);
        H5.H5Sselect_hyperslab(file_space, HDF5Constants.H5S_SELECT_SET, new long[] {0}, null, new long[] {c1.length}, null);

        int mem_space = H5.H5Screate_simple(1, new long[] {c1.length}, null);
        H5.H5Dwrite(dset, type, mem_space, file_space, HDF5Constants.H5P_DEFAULT, c1);
        H5.H5Sclose(mem_space);

        H5.H5Sclose(file_space);
        log.info(H5.H5Dget_storage_size(dset));
      }
      {
        log.info(H5.H5Dget_storage_size(dset));
        H5.H5Dset_extent(dset, new long[] {c1.length + c2.length});
        file_space = H5.H5Dget_space(dset);
        H5.H5Sselect_hyperslab(file_space, HDF5Constants.H5S_SELECT_SET, new long[] {c1.length}, null, new long[] {c2.length}, null);

        int mem_space = H5.H5Screate_simple(1, new long[] {c2.length}, null);
        H5.H5Dwrite(dset, type, mem_space, file_space, HDF5Constants.H5P_DEFAULT, c2);
        H5.H5Sclose(mem_space);

        H5.H5Sclose(file_space);
        log.info(H5.H5Dget_storage_size(dset));
      }

      H5.H5Dclose(dset);
      H5.H5Fclose(file);
    } catch (HDF5Exception e) {
      e.printStackTrace();
    }
  }

  private final static Logger log = Logger.getLogger(H5Append.class.getName());
}
