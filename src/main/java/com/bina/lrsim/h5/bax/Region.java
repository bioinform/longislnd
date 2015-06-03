package com.bina.lrsim.h5.bax;

import java.util.ArrayList;

/**
 * Created by bayo on 5/27/15.
 */
public class Region {
  final int regionScore_;
  final ArrayList<Integer> insertLengths = new ArrayList<Integer>();

  public Region(int[] data, int begin, int end) {
    if (end < begin || (end - begin) % EnumRegionsIdx.values().length != 0) { throw new RuntimeException("unexpected region data"); }

    int hq_start = Integer.MAX_VALUE;
    int hq_end = -1;

    ArrayList<Integer> insert_start = new ArrayList<Integer>(4);
    ArrayList<Integer> insert_end = new ArrayList<Integer>(4);

    int regionScore = -1;
    for (int shift = begin; shift < end; shift += EnumRegionsIdx.values().length) {
      final int loc_start = data[shift + EnumRegionsIdx.RegionStart.value];
      final int loc_end = data[shift + EnumRegionsIdx.RegionEnd.value];
      if (data[shift + EnumRegionsIdx.RegionType.value] == EnumTypeIdx.TypeInsert.value) {
        insert_start.add(loc_start);
        insert_end.add(loc_end);
      } else if (data[shift + EnumRegionsIdx.RegionType.value] == EnumTypeIdx.TypeHQRegion.value) {
        hq_start = Math.max(loc_start, hq_start);
        hq_end = Math.min(loc_end, hq_end);
        regionScore = Math.max(regionScore, data[shift + EnumRegionsIdx.RegionScore.value]);
      }
    }

    for (int ii = 0; ii < insert_start.size(); ++ii) {
      insertLengths.add(Math.min(insert_end.get(ii), hq_end) - Math.max(insert_start.get(ii), hq_start));
    }

    regionScore_ = regionScore;
  }

  public ArrayList<Integer> getInsertLengths() {
    return insertLengths;
  }

  public int getRegionScore() {
    return regionScore_;
  }
}
