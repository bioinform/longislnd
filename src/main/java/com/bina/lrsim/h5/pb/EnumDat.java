package com.bina.lrsim.h5.pb;

/**
 * Created by bayo on 5/2/15.
 */

import java.util.EnumSet;

public enum EnumDat {
  BaseCall(0, "/Basecall"), // for bax
  AlnArray(0, "/AlnArray"), // for cmp
  DeletionQV(1, "/DeletionQV"),
  DeletionTag(2, "/DeletionTag"),
  InsertionQV(3, "/InsertionQV"),
  MergeQV(4, "/MergeQV"),
  QualityValue(5, "/QualityValue"),
  SubstitutionQV(6, "/SubstitutionQV"),
  SubstitutionTag(7, "/SubstitutionTag");

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

  EnumDat(int value, String path) {
    this.value = value;
    this.path = path;
  }

  public final int value;
  public final String path;
}
