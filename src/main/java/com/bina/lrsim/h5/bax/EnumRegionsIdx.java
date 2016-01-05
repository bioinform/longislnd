package com.bina.lrsim.h5.bax;

/**
 * Created by bayo on 5/6/15.
 */
enum EnumRegionsIdx {
  HoleNumber(0, "HoleNumber"),
  RegionType(1, "Region type index"),
  RegionStart(2, "Region start in bases"),
  RegionEnd(3, "Region end in bases"),
  RegionScore(4, "Region score");

  static String[] getDescriptionArray() {
    return new String[] {HoleNumber.description,
                         RegionType.description,
                         RegionStart.description,
                         RegionEnd.description,
                         RegionScore.description};
  }

  EnumRegionsIdx(int value, String description) {
    this.value = value;
    this.description = description;
  }

  public final String description;
  public final int value;
}
