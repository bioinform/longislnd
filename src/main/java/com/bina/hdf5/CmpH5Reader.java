package com.bina.hdf5;


/**
 * Created by bayo on 5/1/15.
 */

import org.apache.log4j.Logger;
import ncsa.hdf.object.h5.H5File;

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
        sb.append("last data alnarray:\n");
        CmpH5AlnData tmp = new CmpH5AlnData(h5_,AlnGroup_.path(161));
        try {
            byte[] bb = tmp.get(CmpH5AlnData.Field.AlnArray);
            sb.append(bb.length);
            for(int ii = 0 ;ii<10;++ii){
                sb.append(" ");
                sb.append(bb[ii]);
            }
            sb.append("\n");
        }
        catch(Exception e){
            log.info(e,e);

        }
        return sb.toString();
    }

    private String filename_ = null;
    private H5File h5_ = null;
    private CmpH5AlnIndex AlnIndex_ = null;
    private CmpH5AlnGroup AlnGroup_ = null;
    private final static Logger log = Logger.getLogger(H5Test.class.getName());
}
