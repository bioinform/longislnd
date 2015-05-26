package com.bina.lrsim.h5.pb;

/**
 * Created by bayo on 5/2/15.
 */

public enum EnumDat {
  BaseCall(0, "/Basecall", false), // for bax
  AlnArray(0, "/AlnArray", false), // for cmp
  DeletionQV(1, "/DeletionQV", false),
  DeletionTag(2, "/DeletionTag", false),
  InsertionQV(3, "/InsertionQV", false),
  MergeQV(4, "/MergeQV", false),
  QualityValue(5, "/QualityValue", false),
  SubstitutionQV(6, "/SubstitutionQV", false),
  SubstitutionTag(7, "/SubstitutionTag", false);
  public static int numBytes = 8;

  public final int value;
  public final String path;
  public final boolean isSigned;

  EnumDat(int value, String path, boolean isSigned) {
    this.value = value;
    this.path = path;
    this.isSigned = isSigned;
  }
}
