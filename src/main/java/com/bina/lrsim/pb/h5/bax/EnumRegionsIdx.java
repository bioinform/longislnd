package com.bina.lrsim.pb.h5.bax;

/**
 * Created by bayo on 5/6/15.
 */
enum EnumRegionsIdx {
  HoleNumber("HoleNumber"),
  RegionType("Region type index"),
  RegionStart("Region start in bases"),
  RegionEnd("Region end in bases"),
  RegionScore("Region score");

  public static final String[] descriptionArray;

  static {
    descriptionArray = new String[values().length];
    int i = 0;
    for (final EnumRegionsIdx enumRegionsIdx : values()) {
      descriptionArray[i] = enumRegionsIdx.description;
      i++;
    }
  }

  static String[] getDescriptionArray() {
    return descriptionArray;
  }

  EnumRegionsIdx(String description) {
    this.description = description;
  }

  public final String description;
}
