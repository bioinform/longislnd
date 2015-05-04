package com.bina.hdf5.h5;
import java.util.EnumSet;
import java.util.Map;
import java.util.HashMap;

import ncsa.hdf.hdf5lib.H5;
import ncsa.hdf.hdf5lib.exceptions.HDF5LibraryException;
import ncsa.hdf.object.HObject;
import ncsa.hdf.hdf5lib.structs.H5O_info_t;
import ncsa.hdf.hdf5lib.exceptions.HDF5Exception;

/**
 * Created by bayo on 5/1/15.
 */
public class H5Traits {
    public final static int UNKONWN = -1;
    public final static int GROUP = 0;
    public final static int DATASET = 1;
    public final static int NAMED_DATA_TYPE = 2;
    public static int Trait(HObject obj) throws HDF5LibraryException{
        return H5.H5Oget_info_by_name(obj.getFID(),obj.getFullName(),0).type;
    }
}
