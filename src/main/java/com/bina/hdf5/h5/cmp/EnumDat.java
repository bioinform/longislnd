package com.bina.hdf5.h5.cmp;

/**
 * Created by bayo on 5/2/15.
 */

public enum EnumDat {
    AlnArray       (0,"/AlnArray"),
    DeletionQV     (1,"/DeletionQV"),
    DeletionTag    (2,"/DeletionTag"),
    InsertionQV    (3,"/InsertionQV"),
    MergeQV        (4,"/MergeQV"),
    QualityValue   (5,"/QualityValue"),
    SubstitutionQV (6,"/SubstituionQV"),
    SubstitutionTag(7,"/SubstituionTag"),
    num_fields     (8,"");

    public int value() {return value_;}
    public String path() {return path_;}

    EnumDat(int value, String path){ value_=value; path_=path;}
    private final int value_;
    private final String path_;
}