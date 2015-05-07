package com.bina.hdf5.h5;

/**
 * Created by bayo on 5/1/15.
 */

import ncsa.hdf.object.h5.H5File;
import ncsa.hdf.object.h5.H5ScalarDS;
import org.apache.log4j.Logger;

public class H5ScalarDSIO {
    private final static Logger log = Logger.getLogger(H5ScalarDSIO.class.getName());

    static public <T> T Read(H5File h5, String path) throws Exception {
        log.info("reading from " + path);
        H5ScalarDS dset = (H5ScalarDS) h5.get(path);
        return (T) dset.read();
    }

    static public void Write(H5File h5, String path, Object buffer, long[] dims) throws Exception {
        h5.createScalarDS(path, null, EnumH5Type.getEnum(buffer.getClass()).getH5Datatype(buffer), dims, null, null, 0, buffer);
    }
}
