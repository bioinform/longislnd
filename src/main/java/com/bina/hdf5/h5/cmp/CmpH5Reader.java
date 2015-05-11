package com.bina.hdf5.h5.cmp;


/**
 * Created by bayo on 5/1/15.
 */

import com.bina.hdf5.h5.pb.EnumDat;
import com.bina.hdf5.H5Test;
import com.bina.hdf5.interfaces.EventGroupFactory;
import com.bina.hdf5.bioinfo.EnumBP;
import com.bina.hdf5.bioinfo.WeightedReference;
import ncsa.hdf.object.FileFormat;
import ncsa.hdf.object.h5.H5File;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class CmpH5Reader implements EventGroupFactory {
    @Override
    public int size() {
        return AlnIndex_.size();
    }

    @Override
    public CmpH5Alignment getEventGroup(int index) throws Exception {
        String path = AlnGroup_.path(AlnIndex_.get(index,EnumIdx.AlnGroupID));
        if(path == null) return null;
        /*
        AlnData data_ref = path_data_.get(path);
        if (null == data_ref) {
            data_ref = new AlnData(h5_, path);
            path_data_.put(path, data_ref);
        }
        */
        if(last_path_ == null || !path.equals(last_path_)){
            log.info("loading alignment group "+path);
            last_data_ = new AlnData(h5_,path);
            last_path_ = path;
        }
        /*
        CmpH5Alignment aa = new CmpH5Alignment(AlnIndex_.get(index), last_data_);
        byte[] a2r = aa.toRefRead().get(EnumDat.BaseCall).data_ref();
        byte[] r2r = Arrays.copyOfRange(wr_.get(22-(AlnIndex_.get(index, EnumIdx.RefGroupID)-1))
                                       ,AlnIndex_.get(index, EnumIdx.tStart)
                                       ,AlnIndex_.get(index, EnumIdx.tEnd) );
        log.info(a2r.length + " " + r2r.length);
        log.info(22-(AlnIndex_.get(index, EnumIdx.RefGroupID)-1));
        log.info(AlnIndex_.get(index, EnumIdx.tStart));
        log.info(AlnIndex_.get(index, EnumIdx.tEnd));
        log.info(AlnIndex_.get(index, EnumIdx.RCRefStrand));
        StringBuilder sb= new StringBuilder();
        for( int ii = 0 ; ii < r2r.length ; ii+=200){
            sb.append("-\n");
            int end = 200;
            int diff = r2r.length - ii;
            if(diff < end) end = diff;
            for( int jj = 0 ; jj < end; ++jj){
                sb.append((char)(a2r[ii+jj]&0xFF));
            }
            sb.append("\n");
            for( int jj = 0 ; jj < end; ++jj){
                sb.append((char)(r2r[ii+jj]&0xFF));
            }
            sb.append("\n");
        }
        log.info(sb.toString());
        */
        return new CmpH5Alignment(AlnIndex_.get(index), last_data_);
    }


    public CmpH5Reader(String filename) {
        load(filename);
//        wr_ = new WeightedReference("/Users/bayo/Downloads/CHM1htert.fasta");
    }

    public void load(String filename) {
        filename_ = filename;
        h5_ = new H5File(filename, FileFormat.READ);
        AlnIndex_ = new AlnIndex(h5_);
        AlnGroup_ = new AlnGroup(h5_);
        path_data_ = new HashMap<String, AlnData>();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(AlnIndex_.toString());
        sb.append(AlnGroup_.toString());
        sb.append("cmp conversion table:\n");
        sb.append(EnumBP.tableToString());
        sb.append("last data_ref alnarray:\n");
        AlnData tmp = new AlnData(h5_, AlnGroup_.path(2));
        sb.append(AlnGroup_.path(2) + "\n");
        try {
            byte[] bb = tmp.get(EnumDat.AlnArray);
            sb.append(bb.length);
            for (int ii = 0; ii < 10; ++ii) {
                sb.append(" ");
                sb.append(bb[ii] & 0xff);
            }
            sb.append("\n");

            CmpH5Alignment aa = getEventGroup(2);
            sb.append(aa.toString());
            int[] aln = aa.aln();
            sb.append(aln.length + " " + aa.aln_begin() + " " + aa.aln_end() + "\n");
            for (int ii = 0; ii < 10; ++ii) {
                sb.append(" ");
                sb.append(aln[ii]);
            }
            sb.append("\n");
        } catch (Exception e) {
            log.info(e, e);

        }
        return sb.toString();
    }

    private String filename_ = null;
    private H5File h5_ = null;
    private AlnIndex AlnIndex_ = null;
    private AlnGroup AlnGroup_ = null;
    private Map<String, AlnData> path_data_ = null;

    private WeightedReference wr_;

    private String last_path_;
    private AlnData last_data_;
    private final static Logger log = Logger.getLogger(H5Test.class.getName());
}
