package com.bina.lrsim.pb.h5.bax;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bayo on 5/27/15.
 */
public class Region {
  private final static Logger log = Logger.getLogger(Region.class.getName());
  final private int regionScore;
  final private float readScore;
  final private byte holeStatus;
  final private List<Integer> insertLengths = new ArrayList<>();
  final private List<Pair<Integer, Integer>> beginEnd = new ArrayList<>();

  public Region(int regionScore, float readScore, byte holeStatus, int insertLength) {
    this.regionScore = regionScore;
    this.readScore = readScore;
    this.holeStatus = holeStatus;
    insertLengths.add(insertLength);
    if (insertLength > 0) {
      beginEnd.add(new ImmutablePair<>(0, insertLength));
    }
  }

  public Region(int[] data, int begin, int end, float score, byte holeStatus) {
    if (end < begin || (end - begin) % EnumRegionsIdx.values().length != 0) { throw new RuntimeException("unexpected region data"); }
    this.readScore = score;
    this.holeStatus = holeStatus;

    int hqStart = Integer.MAX_VALUE;
    int hqEnd = -1;

    List<Integer> insertStart = new ArrayList<>(4);
    List<Integer> insertEnd = new ArrayList<>(4);

    int regionScore = -1;
    for (int shift = begin; shift < end; shift += EnumRegionsIdx.values().length) {
      final int locStart = data[shift + EnumRegionsIdx.RegionStart.value];
      final int locEnd = data[shift + EnumRegionsIdx.RegionEnd.value];
      if (data[shift + EnumRegionsIdx.RegionType.value] == EnumTypeIdx.TypeInsert.value) {
        insertStart.add(locStart);
        insertEnd.add(locEnd);
      } else if (data[shift + EnumRegionsIdx.RegionType.value] == EnumTypeIdx.TypeHQRegion.value) {
        hqStart = Math.min(locStart, hqStart);
        hqEnd = Math.max(locEnd, hqEnd);
        regionScore = Math.max(regionScore, data[shift + EnumRegionsIdx.RegionScore.value]);
      }
    }

    for (int ii = 0; ii < insertStart.size(); ++ii) {
      final int b = Math.max(insertStart.get(ii), hqStart);
      final int e = Math.min(insertEnd.get(ii), hqEnd);
      insertLengths.add(e - b);
      if (e > b) {
        beginEnd.add(new ImmutablePair<>(b, e));
      }
    }

    this.regionScore = regionScore;
  }

  public List<Pair<Integer, Integer>> getBeginEnd() {
    return beginEnd;
  }

  public List<Integer> getInsertLengths() {
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
