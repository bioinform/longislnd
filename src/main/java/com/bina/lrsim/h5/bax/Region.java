package com.bina.lrsim.h5.bax;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import java.util.ArrayList;

/**
 * Created by bayo on 5/27/15.
 */
public class Region {
  private final static Logger log = Logger.getLogger(Region.class.getName());
  final private int regionScore;
  final private float readScore;
  final private byte holeStatus;
  final private ArrayList<Integer> insertLengths = new ArrayList<Integer>();
  final private ArrayList<Pair<Integer, Integer>> begin_end = new ArrayList<Pair<Integer, Integer>>();

  public Region(int regionScore, float readScore, byte holeStatus, int insertLength) {
    this.regionScore = regionScore;
    this.readScore = readScore;
    this.holeStatus = holeStatus;
    insertLengths.add(insertLength);
    if (insertLength > 0) {
      begin_end.add(new ImmutablePair<Integer, Integer>(0, insertLength));
    }
  }

  public Region(int[] data, int begin, int end, float score, byte holeStatus) {
    if (end < begin || (end - begin) % EnumRegionsIdx.values().length != 0) { throw new RuntimeException("unexpected region data"); }
    this.readScore = score;
    this.holeStatus = holeStatus;

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
        hq_start = Math.min(loc_start, hq_start);
        hq_end = Math.max(loc_end, hq_end);
        regionScore = Math.max(regionScore, data[shift + EnumRegionsIdx.RegionScore.value]);
      }
    }

    for (int ii = 0; ii < insert_start.size(); ++ii) {
      final int b = Math.max(insert_start.get(ii), hq_start);
      final int e = Math.min(insert_end.get(ii), hq_end);
      insertLengths.add(e - b);
      if (e > b) {
        begin_end.add(new ImmutablePair<Integer, Integer>(b, e));
      }
    }

    this.regionScore = regionScore;
  }

  public ArrayList<Pair<Integer, Integer>> getBeginEnd() {
    return begin_end;
  }

  public ArrayList<Integer> getInsertLengths() {
    return insertLengths;
  }

  public int getRegionScore() {
    return regionScore;
  }

  public float getReadScore() {
    return readScore;
  }

  public boolean isSequencing() {
    return holeStatus == EnumHoleStatus.SEQUENCING.value;
  }
}
