package com.bina.hdf5;

import ncsa.hdf.object.h5.H5File;

/**
 * Created by bayo on 5/1/15.
 */
public class CmpH5Reader {

    public CmpH5Reader(String filename){ load(filename); }

    public void load(String filename){
        filename_ = filename;
        h5_ = new H5File(filename);
        AlnIndex_ = new CmpH5AlnIndex(h5_);
        AlnGroup_ = new CmpH5AlnGroup(h5_);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("AlnIndex:\n");
        sb.append(AlnIndex_.toString());
        sb.append("AlnGroup:\n");
        sb.append(AlnGroup_.toString());
        return sb.toString();
    }

    private String filename_ = null;
    private H5File h5_ = null;
    private CmpH5AlnIndex AlnIndex_ = null;
    private CmpH5AlnGroup AlnGroup_ = null;
}
