package com.bina.hdf5.h5.cmp;


/**
 * Created by bayo on 5/1/15.
 */

import java.util.HashMap;
import java.util.Map;

import com.bina.hdf5.Alignment;
import com.bina.hdf5.H5Test;
import org.apache.log4j.Logger;
import ncsa.hdf.object.h5.H5File;
import ncsa.hdf.object.FileFormat;

public class CmpH5Reader {

    Alignment getAlignment(int index) throws Exception{
        String path = AlnGroup_.path(index);
        AlnData data = path_data_.get(path);
        if( null == data ){
            data = new AlnData(h5_,path);
            path_data_.put(path,data);
        }
        return new Alignment(AlnIndex_.get(index),data);
    }

    public CmpH5Reader(String filename){ load(filename); }

    public void load(String filename){
        filename_ = filename;
        h5_ = new H5File(filename,FileFormat.READ);
        AlnIndex_ = new AlnIndex(h5_);
        AlnGroup_ = new AlnGroup(h5_);
        path_data_ = new HashMap<String,AlnData>();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(AlnIndex_.toString());
        sb.append(AlnGroup_.toString());
        sb.append("cmp conversion table:\n");
        sb.append(EnumBP.tableToString());
        sb.append("last data alnarray:\n");
        AlnData tmp = new AlnData(h5_,AlnGroup_.path(2));
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
    private AlnIndex AlnIndex_ = null;
    private AlnGroup AlnGroup_ = null;
    private Map<String,AlnData> path_data_ = null;
    private final static Logger log = Logger.getLogger(H5Test.class.getName());
}
