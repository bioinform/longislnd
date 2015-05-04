package com.bina.hdf5.h5;

/**
 * Created by bayo on 5/1/15.
 */

import ncsa.hdf.object.h5.H5File;
import ncsa.hdf.object.h5.H5ScalarDS;

public class H5ScalarDSReader {
    static public <T> T Read(H5File h5, String path) throws Exception{
        H5ScalarDS dset = (H5ScalarDS) h5.get(path);
        return (T) dset.read();
    }
}
