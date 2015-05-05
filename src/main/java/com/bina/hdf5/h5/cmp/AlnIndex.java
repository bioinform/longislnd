package com.bina.hdf5.h5.cmp;

/**
 * Created by bayo on 5/1/15.
 */

import ncsa.hdf.object.h5.H5File;
import ncsa.hdf.object.h5.H5ScalarDS;
import org.apache.log4j.Logger;

import java.util.Arrays;

public class AlnIndex {

    public AlnIndex(H5File h5) {
        load(h5);
    }

    public int get(int alignment_index, EnumIdx c) {
        return data_[alignment_index * num_cols_ + c.value()];
    }

    public int[] get(int alignment_index) {
        final int begin = alignment_index * num_cols_;
        return Arrays.copyOfRange(data_, begin, begin + num_cols_);
    }

    public boolean load(H5File h5) {
        try {
            H5ScalarDS obj = (H5ScalarDS) h5.get("/AlnInfo/AlnIndex");
            obj.init();

            long[] dims = obj.getDims();
            if (dims.length != 2) throw new Exception("bad AlnIndex dimension");
            final int nr = (int) dims[0];
            final int nc = (int) dims[1];
            if (nc != 22) throw new Exception("bad AlinIndex num_col");

            int[] d = (int[]) obj.getData();
            if (d.length != nr * nc) throw new Exception("bad AlnIndex data");

            data_ = d;
            num_rows_ = nr;
            num_cols_ = nc;
        } catch (Exception e) {
            log.info(e, e);
            log.info(e.toString());
            return true;
        }
        return false;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int row = 0; row < num_rows_; ++row) {
            sb.append("AlnIdx " + row + "\t");
            for (int col = 0; col < num_cols_; ++col) {
                sb.append(" ");
                sb.append(data_[row * num_cols_ + col]);
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    private final static Logger log = Logger.getLogger(AlnIndex.class.getName());
    private int[] data_ = null;
    private int num_rows_;
    private int num_cols_;
}
