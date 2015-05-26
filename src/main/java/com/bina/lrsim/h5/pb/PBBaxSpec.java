package com.bina.lrsim.h5.pb;

import java.util.EnumSet;

/**
 * Created by bayo on 5/25/15.
 */
public class PBBaxSpec extends PBSpec {
  @Override
  public String[] getContentDescription() {
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

  @Override
  public EnumSet<EnumDat> getSet() {
    return EnumSet.complementOf(EnumSet.of(EnumDat.AlnArray));
  }

  @Override
  public EnumSet<EnumDat> getNonBaseSet() {
    return EnumSet.complementOf(EnumSet.of(EnumDat.BaseCall, EnumDat.AlnArray));
  }
}
