package com.bina.lrsim.pb.h5.bax;

import java.io.IOException;
import java.util.*;

import com.bina.lrsim.pb.h5.Attributes;
import htsjdk.samtools.fastq.FastqRecord;
import ncsa.hdf.object.FileFormat;
import ncsa.hdf.object.h5.H5File;

import com.bina.lrsim.pb.h5.H5ScalarDSIO;
import com.bina.lrsim.pb.Spec;
import com.bina.lrsim.interfaces.RegionGroup;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

/**
 * Created by bayo on 5/27/15.
 */
public class BaxH5Reader implements RegionGroup {
  private final static Logger log = Logger.getLogger(BaxH5Reader.class.getName());
  private H5File h5 = null;
  private final Spec spec;
  private String movieName = null;

  public BaxH5Reader(String filename, Spec spec) throws IOException {
    this.load(filename);
    this.spec = spec;
  }

  public void load(String filename) throws IOException {
    h5 = new H5File(filename, FileFormat.READ);
    movieName = null;
    try {
      movieName = ((String[]) Attributes.extract(h5.get(EnumGroups.RunInfo.path), "MovieName"))[0];
    } catch (Exception e) {
      log.error("failed to retrieve movie name from " + filename);
      throw new IOException("failed to retrieve movie name from " + filename);
    }
    if (null == movieName) {
      movieName = "";
    }
  }

  public Iterator<FastqRecord> reads() {
    return new ReadIterator(this.iterator());
  }

  private class ReadIterator implements Iterator<FastqRecord> {
    private final Iterator<Region> regions;
    private final int[] holeNumber;
    private final int[] numEvents;
    private final byte[] seq;
    private final byte[] qual;
    private int shift = 0;
    private int base_shift = 0;
    private Queue<FastqRecord> queue = new LinkedList<>();

    ReadIterator(Iterator<Region> r) {
      regions = r;
      try {
        holeNumber = (int[]) H5ScalarDSIO.Read(h5, spec.getZMWEnum().path + "/HoleNumber");
        numEvents = (int[]) H5ScalarDSIO.Read(h5, spec.getZMWEnum().path + "/NumEvent");
        seq = (byte[]) H5ScalarDSIO.Read(h5, spec.getBaseCallsEnum().path + "/Basecall");
        byte[] tmp = (byte[]) H5ScalarDSIO.Read(h5, spec.getBaseCallsEnum().path + "/QualityValue");
        qual = Arrays.copyOf(tmp, tmp.length); // to be shifted by 33
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public boolean hasNext() {
      while (queue.isEmpty() && regions.hasNext()) {
        Region reg = regions.next();
        if (reg.isSequencing()) {
          for (Pair<Integer, Integer> entry : reg.getBeginEnd()) {
            final int b = entry.getLeft();
            final int e = entry.getRight();
            final int len = e - b;
            for (int ii = b; ii < e; ++ii) {
              qual[base_shift + ii] += 33;
            }
            queue.add(new FastqRecord(new String(movieName + "/" + holeNumber[shift] + "/" + b + "_" + e), new String(seq, base_shift + b, len), null, new String(qual, base_shift + b, len)));
          }
        }
        base_shift += numEvents[shift];
        ++shift;
      }
      return !queue.isEmpty();
    }

    @Override
    public FastqRecord next() {
      return this.hasNext() ? queue.remove() : null;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }

  @Override
  public Iterator<Region> iterator() {
    return (spec.getZMWEnum() == EnumGroups.CZMW) ? new CCSRegionIterator() : new RegionIterator();
  }

  private class CCSRegionIterator implements Iterator<Region> {
    int curr = 0;
    private final float[] holeScore;
    private final byte[] holeStatus;
    private final int[] numEvents;

    CCSRegionIterator() {
      curr = 0;
      try {
        holeStatus = (byte[]) H5ScalarDSIO.Read(h5, spec.getZMWEnum().path + "/HoleStatus");
        numEvents = (int[]) H5ScalarDSIO.Read(h5, spec.getZMWEnum().path + "/NumEvent");
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      float[] fp;
      try {
        fp = (float[]) H5ScalarDSIO.Read(h5, spec.getZMWMetricsEnum().path + "/ReadScore");
      } catch (IOException e) {
        fp = null;
      }
      try {
        if (null == fp) {
          fp = (float[]) H5ScalarDSIO.Read(h5, spec.getZMWMetricsEnum().path + "/PredictedAccuracy");
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
    int minHole = Integer.MAX_VALUE;

    RegionIterator() {
      curr = 0;
      try {
        regionData = (int[]) H5ScalarDSIO.Read(h5, EnumGroups.PulseData.path + "/Regions");
        holeScore = (float[]) H5ScalarDSIO.Read(h5, spec.getZMWMetricsEnum().path + "/ReadScore");
        holeStatus = (byte[]) H5ScalarDSIO.Read(h5, spec.getZMWEnum().path + "/HoleStatus");
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
      int holeNumber = regionData[curr + EnumRegionsIdx.HoleNumber.ordinal()];
      minHole = Math.min(holeNumber, minHole);
      int next = curr + EnumRegionsIdx.values().length;
      for (; next < regionData.length && regionData[next + EnumRegionsIdx.HoleNumber.ordinal()] == holeNumber; next += EnumRegionsIdx.values().length) {}
      Region r = new Region(regionData, curr, next, holeScore[holeNumber - minHole], holeStatus[holeNumber - minHole]);
      curr = next;
      return r;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException("cannot remove elements");
    }
  }
}
