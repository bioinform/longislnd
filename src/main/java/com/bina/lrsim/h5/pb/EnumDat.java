package com.bina.lrsim.h5.pb;

/**
 * Created by bayo on 5/2/15.
 */

public enum EnumDat {
  BaseCall(0, "/Basecall", false, false), // for bax
  AlnArray(0, "/AlnArray", false, false), // for cmp
  DeletionQV(1, "/DeletionQV", false, true),
  DeletionTag(2, "/DeletionTag", false, false),
  InsertionQV(3, "/InsertionQV", false, true),
  MergeQV(4, "/MergeQV", false, true),
  QualityValue(5, "/QualityValue", false, true),
  SubstitutionQV(6, "/SubstitutionQV", false, true),
  SubstitutionTag(7, "/SubstitutionTag", false, false);
  public static int numBytes = 8;

  public final int value;
  public final String path;
  public final boolean isSigned;
  public final boolean isScore;

  EnumDat(int value, String path, boolean isSigned, boolean isScore) {
    this.value = value;
    this.path = path;
    this.isSigned = isSigned;
    this.isScore = isScore;
  }
}
