package com.bina.hdf5.h5.pb;

/**
 * Created by bayo on 5/2/15.
 */

import java.util.EnumSet;

public enum EnumDat {
    BaseCall       (0, "/Basecall"), //for bax
    AlnArray       (0, "/AlnArray"), //for cmp
    DeletionQV     (1, "/DeletionQV"),
    DeletionTag    (2, "/DeletionTag"),
    InsertionQV    (3, "/InsertionQV"),
    MergeQV        (4, "/MergeQV"),
    QualityValue   (5, "/QualityValue"),
    SubstitutionQV (6, "/SubstitutionQV"),
    SubstitutionTag(7, "/SubstitutionTag");

    static public String[] getContentDescription(){
        return new String[]
                {"Basecall","DeletionQV","DeletionTag","InsertionQV","MergeQV","QualityValue","SubstitutionQV","SubstitutionTag"
                ,"uint8",   "uint8",     "uint8",      "uint8",      "uint8",  "uint8",       "uint8",         "uint8"};

    }

    static public EnumSet<EnumDat> getBaxSet() {
        return EnumSet.complementOf(EnumSet.of(AlnArray));
    }

    static public EnumSet<EnumDat> getCmpSet() {
        return EnumSet.complementOf(EnumSet.of(BaseCall));
    }

    static public EnumSet<EnumDat> getNonBaseSet() {
        return EnumSet.complementOf(EnumSet.of(BaseCall, AlnArray));
    }

    public int value() {
        return value_;
    }

    public String path() {
        return path_;
    }

    EnumDat(int value, String path) {
        value_ = value;
        path_ = path;
    }

    private final int value_;
    private final String path_;
}