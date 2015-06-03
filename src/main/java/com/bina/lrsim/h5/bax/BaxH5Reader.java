package com.bina.lrsim.h5.bax;

import java.util.Iterator;

import com.bina.lrsim.interfaces.RegionGroup;
import ncsa.hdf.object.FileFormat;
import ncsa.hdf.object.h5.H5File;

import com.bina.lrsim.h5.H5ScalarDSIO;

/**
 * Created by bayo on 5/27/15.
 */
public class BaxH5Reader implements RegionGroup{
  private H5File h5_ = null;

  public BaxH5Reader(String filename) {
    this.load(filename);
  }

  public void load(String filename) {
    h5_ = new H5File(filename, FileFormat.READ);
  }

  @Override
  public Iterator<Region> getRegionIterator() {
    return new RegionIterator();
  }

  private class RegionIterator implements Iterator<Region> {
    int curr = 0;
    private int[] region_data;
    private float[] hole_score;

    RegionIterator() {
      curr = 0;
      try {
        region_data = H5ScalarDSIO.<int[]>Read(h5_, EnumGroups.PulseData.path + "/Regions");
        hole_score = H5ScalarDSIO.<float[]>Read(h5_, EnumGroups.CZMWMetrics.path + "/ReadScore");
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public boolean hasNext() {
      return curr < region_data.length;
    }

    @Override
    public Region next() {
      int hole_number = region_data[curr + EnumRegionsIdx.HoleNumber.value];
      int next = curr + EnumRegionsIdx.values().length;
      for (; next < region_data.length && region_data[next + EnumRegionsIdx.HoleNumber.value] == hole_number; next += EnumRegionsIdx.values().length) {}
      Region r = new Region(region_data, curr, next, hole_score[hole_number]);
      curr = next;
      return r;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException("cannot remove elements");
    }
  }
}
