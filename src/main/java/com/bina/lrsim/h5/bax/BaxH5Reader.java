package com.bina.lrsim.h5.bax;

import java.io.IOException;
import java.util.Iterator;

import ncsa.hdf.object.FileFormat;
import ncsa.hdf.object.h5.H5File;

import com.bina.lrsim.h5.H5ScalarDSIO;
import com.bina.lrsim.h5.pb.PBSpec;
import com.bina.lrsim.interfaces.RegionGroup;

/**
 * Created by bayo on 5/27/15.
 */
public class BaxH5Reader implements RegionGroup {
  private H5File h5_ = null;
  private final PBSpec spec;

  public BaxH5Reader(String filename, PBSpec spec) {
    this.load(filename);
    this.spec = spec;
  }

  public void load(String filename) {
    h5_ = new H5File(filename, FileFormat.READ);
  }

  @Override
  public Iterator<Region> iterator() {
    return (spec.getZMWEnum().equals(EnumGroups.CZMW)) ? new CCSRegionIterator() : new RegionIterator();
  }

  private class CCSRegionIterator implements Iterator<Region> {
    int curr = 0;
    private final float[] holeScore;
    private final byte[] holeStatus;
    private final int[] numEvents;

    CCSRegionIterator() {
      curr = 0;
      try {
        holeStatus = H5ScalarDSIO.<byte[]>Read(h5_, spec.getZMWEnum().path + "/HoleStatus");
        numEvents = H5ScalarDSIO.<int[]>Read(h5_, spec.getZMWEnum().path + "/NumEvent");
      }  catch (IOException e) {
        throw new RuntimeException(e);
      }
      float[] fp;
      try {
        fp = H5ScalarDSIO.<float[]>Read(h5_, spec.getZMWMetricsEnum().path + "/ReadScore");
      } catch (IOException e) {
        fp = null;
      }
      try {
        if(null == fp) {
          fp = H5ScalarDSIO.<float[]>Read(h5_, spec.getZMWMetricsEnum().path + "/PredictedAccuracy");
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      holeScore = fp;
    }

    @Override
    public boolean hasNext() {
      return curr < numEvents.length;
    }

    @Override
    public Region next() {
      Region ret = new Region((int) (1000 * holeScore[curr]), holeScore[curr], holeStatus[curr], numEvents[curr]);
      ++curr;
      return ret;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException("cannot remove elements");
    }
  }

  private class RegionIterator implements Iterator<Region> {
    int curr = 0;
    private int[] regionData;
    private float[] holeScore;
    private byte[] holeStatus;
    int min_hole = Integer.MAX_VALUE;

    RegionIterator() {
      curr = 0;
      try {
        regionData = H5ScalarDSIO.<int[]>Read(h5_, EnumGroups.PulseData.path + "/Regions");
        holeScore = H5ScalarDSIO.<float[]>Read(h5_, spec.getZMWMetricsEnum().path + "/ReadScore");
        holeStatus = H5ScalarDSIO.<byte[]>Read(h5_, spec.getZMWEnum().path + "/HoleStatus");
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public boolean hasNext() {
      return curr < regionData.length;
    }

    @Override
    public Region next() {
      int hole_number = regionData[curr + EnumRegionsIdx.HoleNumber.value];
      min_hole = Math.min(hole_number, min_hole);
      int next = curr + EnumRegionsIdx.values().length;
      for (; next < regionData.length && regionData[next + EnumRegionsIdx.HoleNumber.value] == hole_number; next += EnumRegionsIdx.values().length) {}
      Region r = new Region(regionData, curr, next, holeScore[hole_number - min_hole], holeStatus[hole_number - min_hole]);
      curr = next;
      return r;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException("cannot remove elements");
    }
  }
}
