package com.bina.hdf5;

/**
 * Created by bayo on 5/2/15.
 */
import ncsa.hdf.object.h5.H5File;
public class CmpH5AlnData {
    public enum Field{
        AlnArray       (0,"AlnArray"),
        DeletionQV     (1,"DeletionQV"),
        DeletionTag    (2,"DeletionTag"),
        InsertionQV    (3,"InsertionQV"),
        MergeQV        (4,"MergeQV"),
        QualityValue   (5,"QualityValue"),
        SubstitutionQV (6,"SubstituionQV"),
        SubstitutionTag(7,"SubstituionTag"),
        num_fields     (8,"");

        Field(int value, String path){ value_=value; path_=path;}
        public int value() {return value_;}
        public String path() {return path_;}
        private final int value_;
        private final String path_;
    }

    public CmpH5AlnData(H5File h5, String path){ load(h5,path); }

    public byte[] get(Field f) throws Exception{
        if( null == data_[f.value()] ){
            data_[f.value()] = H5ScalarDSReader.<byte[]>Read(h5_,path_+"/"+f.path());
        }
        return (byte[])data_[f.value()];
    }

    public void load(H5File h5, String path){
        h5_ = h5;
        path_ = path;
        data_ = new Object[Field.num_fields.value()];
    }

    private H5File h5_ = null;
    private String path_ = null;
    private Object[] data_ = null; // they don't have to be in byte, there are int and short
}
