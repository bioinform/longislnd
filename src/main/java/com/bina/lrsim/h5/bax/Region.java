package com.bina.lrsim.h5.bax;

/**
 * Created by bayo on 5/27/15.
 */
public class Region {
  final int maxInsertLength_;
  final int regionScore_;

  public Region(int[] data, int begin, int end) {
    if (end < begin || (end - begin) % EnumRegionsIdx.values().length != 0) { throw new RuntimeException("unexpected region data"); }

    int hqLength = -1;
    int insertLength = -1;
    int regionScore = -1;
    for (int shift = begin; shift < end; shift += EnumRegionsIdx.values().length) {
      int loc = data[shift + EnumRegionsIdx.RegionEnd.value] - data[shift + EnumRegionsIdx.RegionStart.value];
      if (data[shift + EnumRegionsIdx.RegionType.value] == EnumTypeIdx.TypeInsert.value) {
        insertLength = Math.max(insertLength, loc);
      } else if (data[shift + EnumRegionsIdx.RegionType.value] == EnumTypeIdx.TypeHQRegion.value) {
        hqLength = Math.max(hqLength, loc);
        regionScore = Math.max(regionScore, data[shift + EnumRegionsIdx.RegionScore.value]);
      }
    }
    if (insertLength < 0) {
      insertLength = hqLength;
    }
    maxInsertLength_ = insertLength;
    regionScore_ = regionScore;
  }

  public int getMaxInsertLength() {
    return maxInsertLength_;
  }

  public int getRegionScore() {
    return regionScore_;
  }
}
