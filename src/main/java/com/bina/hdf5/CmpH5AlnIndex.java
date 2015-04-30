package com.bina.hdf5;

/**
 * Created by bayo on 5/1/15.
 */
import ncsa.hdf.object.h5.H5File;
import ncsa.hdf.object.h5.H5ScalarDS;
import org.apache.log4j.Logger;

public class CmpH5AlnIndex {

    //named according to pacbio's pdf
    public enum Column{
        AlnID       (0),
        AlnGroupID  (1),
        MovieId     (2),
        RefGroupID  (3),
        tStart      (4),
        tEnd        (5),
        RCRefStrand (6),
        HoleNumber  (7),
        SetNumber   (8),
        StrobeNumber(9),
        MoleculeID  (10),
        rStart      (11),
        rEnd        (12),
        MapQV       (13),
        nM          (14),
        nMM         (15),
        nIns        (16),
        nDel        (17),
        offset_begin(18),
        offset_end  (19),
        nBackRead   (20),
        nBackOverlap(21),
        num_columns (22);

        Column(int value){ value_=value; }
        public int value() {return value_;}
        private final int value_;
    }

    public CmpH5AlnIndex(H5File h5){load(h5);}

    public int get(int alignment_index, Column c){
        return data_[ alignment_index*num_cols_+c.value()];
    }

    public boolean load(H5File h5){
        try{
            H5ScalarDS obj = (H5ScalarDS) h5.get("/AlnInfo/AlnIndex");
            obj.init();

            long[] dims = obj.getDims();
            if (dims.length != 2) throw new Exception("bad AlnIndex dimension");
            final int nr = (int)dims[0];
            final int nc = (int)dims[1];
            if( nc!=22 ) throw new Exception("bad AlinIndex num_col");

            int[] d = (int[])obj.getData();
            if (d.length != nr*nc) throw new Exception("bad AlnIndex data");

            data_=d;
            num_rows_=nr;
            num_cols_=nc;
        }
        catch(Exception e){
            log.info(e,e);
            log.info(e.toString());
            return true;
        }
        return false;
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();
        for(int row = 0 ; row < num_rows_ ; ++row){
            sb.append(row);
            sb.append("\t");
            for(int col = 0 ; col < num_cols_ ; ++col){
                sb.append(" ");
                sb.append(data_[row*num_cols_+col]);
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    private final static Logger log = Logger.getLogger(CmpH5AlnIndex.class.getName());
    private int[] data_= null;
    private int num_rows_;
    private int num_cols_;
}
