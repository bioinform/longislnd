package com.bina.hdf5;


/**
 * Created by bayo on 5/1/15.
 */

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import ncsa.hdf.object.h5.H5File;

public class CmpH5Reader {

    Alignment getAlignment(int index) throws Exception{
        String path = AlnGroup_.path(index);
        CmpH5AlnData data = path_data_.get(path);
        if( null == data ){
            data = new CmpH5AlnData(h5_,path);
            path_data_.put(path,data);
        }
        return new Alignment(AlnIndex_.get(index),data);
    }

    public CmpH5Reader(String filename){ load(filename); }

    public void load(String filename){
        filename_ = filename;
        h5_ = new H5File(filename);
        AlnIndex_ = new CmpH5AlnIndex(h5_);
        AlnGroup_ = new CmpH5AlnGroup(h5_);
        path_data_ = new HashMap<String,CmpH5AlnData>();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("AlnIndex:\n");
//        sb.append(AlnIndex_.toString());
        sb.append("AlnGroup:\n");
//        sb.append(AlnGroup_.toString());
        sb.append("cmp conversion table:\n");
        sb.append(EnumBP.tableToString());
        sb.append("last data alnarray:\n");
        CmpH5AlnData tmp = new CmpH5AlnData(h5_,AlnGroup_.path(2));
        sb.append(AlnGroup_.path(2)+"\n");
        try {
            byte[] bb = tmp.get(EnumDat.AlnArray);
            sb.append(bb.length);
            for(int ii = 0 ;ii<10;++ii){
                sb.append(" ");
                sb.append(bb[ii]&0xff);
            }
            sb.append("\n");

            Alignment aa = getAlignment(2);
            sb.append(aa.toString());
            int[] aln = aa.aln();
            sb.append(aln.length+" "+aa.aln_begin()+" "+aa.aln_end()+"\n");
            for(int ii = 0 ;ii<10;++ii){
                sb.append(" ");
                sb.append(aln[ii]);
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
    private Map<String,CmpH5AlnData> path_data_ = null;
    private final static Logger log = Logger.getLogger(H5Test.class.getName());
}
