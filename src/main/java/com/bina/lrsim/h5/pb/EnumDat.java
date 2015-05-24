package com.bina.lrsim.h5.pb;

/**
 * Created by bayo on 5/2/15.
 */

import java.util.EnumSet;

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

  public static String[] getContentDescription() {
    return new String[] {"Basecall",
                         "DeletionQV",
                         "DeletionTag",
                         "InsertionQV",
                         "MergeQV",
                         "QualityValue",
                         "SubstitutionQV",
                         "SubstitutionTag",
                         "uint8",
                         "uint8",
                         "uint8",
                         "uint8",
                         "uint8",
                         "uint8",
                         "uint8",
                         "uint8"};

  }

  public static EnumSet<EnumDat> getBaxSet() {
    return EnumSet.complementOf(EnumSet.of(AlnArray));
  }

  public static EnumSet<EnumDat> getCmpSet() {
    return EnumSet.complementOf(EnumSet.of(BaseCall));
  }

  public static EnumSet<EnumDat> getNonBaseSet() {
    return EnumSet.complementOf(EnumSet.of(BaseCall, AlnArray));
  }

  EnumDat(int value, String path, boolean isSigned) {
    this.value = value;
    this.path = path;
    this.isSigned = isSigned;
  }

  public final int value;
  public final String path;
  public final boolean isSigned;
}
