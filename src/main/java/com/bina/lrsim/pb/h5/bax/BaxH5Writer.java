package com.bina.lrsim.pb.h5.bax;

/**
 * Created by bayo on 5/4/15.
 */

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.bina.lrsim.bioinfo.Locus;
import com.bina.lrsim.pb.RunInfo;
import ncsa.hdf.hdf5lib.exceptions.HDF5Exception;
import ncsa.hdf.object.FileFormat;
import ncsa.hdf.object.HObject;
import ncsa.hdf.object.h5.H5File;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.log4j.Logger;

import com.bina.lrsim.pb.h5.Attributes;
import com.bina.lrsim.pb.h5.H5ScalarDSIO;
import com.bina.lrsim.pb.EnumDat;
import com.bina.lrsim.pb.PBReadBuffer;
import com.bina.lrsim.pb.Spec;

public class BaxH5Writer extends com.bina.lrsim.pb.ReadsWriter {

  private final static Logger log = Logger.getLogger(BaxH5Writer.class.getName());
  private final DataBuffer buffer;
  private final RunInfo runInfo;

  public BaxH5Writer(Spec spec, String filename, String moviename, int firsthole, RunInfo runInfo) {
    super(spec, filename, moviename, firsthole);
    buffer = new DataBuffer(super.spec, 100000);
    this.runInfo = runInfo;
  }

  @Override
  public void close() throws IOException {
    log.info("Writing to " + this.filename + " as movie " + this.moviename);
    H5File h5 = new H5File(this.filename, FileFormat.CREATE);
    try {
      h5.open();
      AttributesFactory af = new AttributesFactory(size(), this.moviename, spec, runInfo);
      writeGroups(h5, af);
      writeBaseCalls(h5, af);
      writeZWM(h5, this.firsthole);
      writeRegions(h5, this.firsthole);
      h5.close();
      writeLociBed(this.filename, this.moviename, this.firsthole);
    } catch (IOException e) {
      // The HDF5 API throws the base class Exception, so let's just catch them all and rethrow
      // run-time exception
      e.printStackTrace();
      throw new RuntimeException(e);
    } catch (HDF5Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  @Override
  public void addLast(PBReadBuffer read, List<Integer> readLengths, int score, Locus locus, List<Locus> clrLoci) {
    buffer.addLast(read, readLengths, score);
    addLocus(locus);
  }

  private void writeGroups(H5File h5, AttributesFactory af) throws IOException {
    for (EnumGroups e : spec.getGroupSet()) {
      try {
        final HObject obj = h5.createGroup(e.path, null);
        af.get(e).writeTo(obj);
      } catch (Exception exception) {
        throw new IOException("failed to write " + e.path);
      }
    }
  }

  private void writeBaseCalls(H5File h5, AttributesFactory af) throws IOException {
    long[] dims = new long[] {buffer.getReadsRef().size()};
    for (EnumDat e : spec.getDataSet()) {
      try {
        final HObject obj = H5ScalarDSIO.Write(h5, spec.getBaseCallsEnum().path + e.path, buffer.getReadsRef().get(e).toByteArray(), dims, e.isSigned);
        af.get(e).writeTo(obj);
      } catch (Exception exception) {
        throw new IOException("failed to write " + e.path);
      }
    }
    if (spec.getGroupSet().contains(EnumGroups.CPasses)) writePasses(h5);
  }

  private void writePasses(H5File h5) throws IOException {
    final int fakeNumPasses = 3;
    final long[] dimsReads = new long[] {(long) size()};
    final long[] dimsPasses = new long[] {fakeNumPasses * (long) size()};
    byte[] byteBuffer = new byte[fakeNumPasses * size()];
    int[] intBuffer = new int[fakeNumPasses * size()];
    MersenneTwister gen = new MersenneTwister(1111);
    {
      for (int ii = 0; ii < size(); ++ii) {
        int value = gen.nextBoolean() ? 1 : 0;
        for (int jj = 0; jj < fakeNumPasses; ++jj, value = (value + 1) % 2) {
          byteBuffer[fakeNumPasses * ii + jj] = (byte) value;
        }
      }
      {
        final HObject obj = H5ScalarDSIO.Write(h5, EnumGroups.CPasses.path + "/AdapterHitAfter", byteBuffer, dimsPasses, false);
        Attributes att = new Attributes();
        att.add(EnumAttributes.DESCRIPTION.fieldName, new String[] {"Flag indicating if an adapter hit was detected at the end of this pass"}, null, false);
        att.add(EnumAttributes.INDEX_FIELD.fieldName, new String[] {"NumPasses"}, null, false);
        att.writeTo(obj);
      }
      for (int ii = 0; ii < fakeNumPasses * size(); ++ii) {
        byteBuffer[ii] = (byte) ((byteBuffer[ii] + 1) % 2);
      }
      {
        final HObject obj = H5ScalarDSIO.Write(h5, EnumGroups.CPasses.path + "/AdapterHitBefore", byteBuffer, dimsPasses, false);
        Attributes att = new Attributes();
        att.add(EnumAttributes.DESCRIPTION.fieldName, new String[] {"Flag indicating if an adapter hit was detected at the beginning of this pass"}, null, false);
        att.add(EnumAttributes.INDEX_FIELD.fieldName, new String[] {"NumPasses"}, null, false);
        att.writeTo(obj);
      }
    }
    {
      for (int ii = 0; ii < size(); ++ii) {
        intBuffer[ii] = fakeNumPasses;
      }
      final HObject obj = H5ScalarDSIO.Write(h5, EnumGroups.CPasses.path + "/NumPasses", intBuffer, dimsReads, true);
      Attributes att = new Attributes();
      att.add(EnumAttributes.DESCRIPTION.fieldName, new String[] {"ZMW event-stream counts"}, null, false);
      att.writeTo(obj);
    }
    {
      for (int ii = 0; ii < size(); ++ii) {
        int value = gen.nextBoolean() ? 1 : 0;
        for (int jj = 0; jj < fakeNumPasses; ++jj, value = (value + 1) % 2) {
          byteBuffer[fakeNumPasses * ii + jj] = (byte) value;
        }
      }
      final HObject obj = H5ScalarDSIO.Write(h5, EnumGroups.CPasses.path + "/PassDirection", byteBuffer, dimsPasses, false);
      Attributes att = new Attributes();
      att.add(EnumAttributes.DESCRIPTION.fieldName, new String[] {"Direction of pass across the SMRTbell"}, null, false);
      att.add(EnumAttributes.INDEX_FIELD.fieldName, new String[] {"NumPasses"}, null, false);
      att.writeTo(obj);
    }
    {
      for (int ii = 0; ii < size(); ++ii) {
        for (int jj = 0; jj < fakeNumPasses; ++jj) {
          intBuffer[fakeNumPasses * ii + jj] = buffer.getLength(ii);
        }
      }
      final HObject obj = H5ScalarDSIO.Write(h5, EnumGroups.CPasses.path + "/PassNumBases", intBuffer, dimsPasses, false);
      Attributes att = new Attributes();
      att.add(EnumAttributes.DESCRIPTION.fieldName, new String[] {"Number of bases in circular consensus pass"}, null, false);
      att.add(EnumAttributes.INDEX_FIELD.fieldName, new String[] {"NumPasses"}, null, false);
      att.writeTo(obj);
    }
    {
      for (int ii = 0; ii < size(); ++ii) {
        intBuffer[ii] = 0;
      }
      for (int ii = 0; ii < size(); ++ii) {
        // this is not strictly correct wrt
        final int adapterLength = 50;
        int shift = adapterLength;
        for (int jj = 0; jj < fakeNumPasses; ++jj, shift += buffer.getLength(ii) + adapterLength) {
          intBuffer[fakeNumPasses * ii + jj] = shift;
        }
      }
      final HObject obj = H5ScalarDSIO.Write(h5, EnumGroups.CPasses.path + "/PassStartBase", intBuffer, dimsPasses, false);
      Attributes att = new Attributes();
      att.add(EnumAttributes.DESCRIPTION.fieldName, new String[] {"Index of first base in circular consensus pass"}, null, false);
      att.add(EnumAttributes.INDEX_FIELD.fieldName, new String[] {"NumPasses"}, null, false);
      att.writeTo(obj);
    }
  }

  @Override
  public int size() {
    return buffer.getNumReads();
  }

  private static void writeRegions(int[] buffer, int shift, int hole, int type, int start, int end, int score) {
    buffer[shift + EnumRegionsIdx.HoleNumber.ordinal()] = hole;
    buffer[shift + EnumRegionsIdx.RegionType.ordinal()] = type;
    buffer[shift + EnumRegionsIdx.RegionStart.ordinal()] = start;
    buffer[shift + EnumRegionsIdx.RegionEnd.ordinal()] = end;
    buffer[shift + EnumRegionsIdx.RegionScore.ordinal()] = score;
  }

  private void writeRegions(H5File h5, int firsthole) throws IOException {
    final int numEntries = size() + (spec.writeAdapterInsert() ? buffer.getNumAdapterInsert() : 0);
    final int[] buffer = new int[numEntries * EnumRegionsIdx.values().length];
    int shift = 0;
    final int ins_score = -1; // insert score seems to be -1 in real data
    for (int rr = 0; rr < size(); ++rr) {
      final int hole = firsthole + rr;
      final int score = this.buffer.getScore(rr);
      final List<Integer> readLengths = this.buffer.getReadLengths(rr);
      if (spec.writeAdapterInsert()) {
        writeRegions(buffer, shift, hole, EnumTypeIdx.TypeInsert.ordinal(), 0, readLengths.get(0), ins_score);
        shift += EnumRegionsIdx.values().length;
        for (int ii = 2; ii < readLengths.size(); ii += 2) {
          writeRegions(buffer, shift, hole, EnumTypeIdx.TypeInsert.ordinal(), readLengths.get(ii - 1), readLengths.get(ii), ins_score);
          shift += EnumRegionsIdx.values().length;
        }
        for (int ii = 1; ii < readLengths.size(); ii += 2) {
          writeRegions(buffer, shift, hole, EnumTypeIdx.TypeAdapter.ordinal(), readLengths.get(ii - 1), readLengths.get(ii), score);
          shift += EnumRegionsIdx.values().length;
        }
      }
      writeRegions(buffer, shift, hole, EnumTypeIdx.TypeHQRegion.ordinal(), 0, readLengths.get(readLengths.size() - 1), score);
      shift += EnumRegionsIdx.values().length;
    }
    long[] dims = new long[] {buffer.length / EnumRegionsIdx.values().length, EnumRegionsIdx.values().length};
    final HObject obj = H5ScalarDSIO.Write(h5, EnumGroups.PulseData.path + "/Regions", buffer, dims, true);
    Attributes att = new Attributes();
    att.add("ColumnNames", EnumRegionsIdx.descriptionArray);
    att.add("RegionDescriptions", new String[] {"Adapter Hit", "Insert Region", "High Quality bases region. Score is 1000 * predicted accuracy, where predicted accuray is 0 to 1.0"}); // typo foolows pacbio's typo
    att.add("RegionSources", new String[] {"AdapterFinding", "AdapterFinding", "PulseToBase Region classifer"}); // typo follows pacbio's typo
    att.add("RegionTypes", EnumTypeIdx.getDescriptionArray());
    att.writeTo(obj);
  }


  public void writeZWM(H5File h5, int firsthole) throws IOException {
    final long[] dims1 = new long[] {(long) size()};
    final long[] dims2 = new long[] {(long) size(), (long) 2};

    int[] intBuffer = new int[size()];
    {
      // HoleNumber
      for (int ii = 0; ii < size(); ++ii) {
        intBuffer[ii] = firsthole + ii;
      }
      final HObject obj = H5ScalarDSIO.Write(h5, spec.getZMWEnum().path + "/HoleNumber", intBuffer, dims1, false);
      Attributes att = new Attributes();
      att.add(EnumAttributes.DESCRIPTION.fieldName, new String[] {"Number assigned to each ZMW on the chip"}, null, false);
      att.writeTo(obj);
    }
    {
      // HoleStatus
      byte[] byteBuffer = new byte[size()];
      Arrays.fill(byteBuffer, ((byte) EnumHoleStatus.SEQUENCING.ordinal()));

      final HObject obj = H5ScalarDSIO.Write(h5, spec.getZMWEnum().path + "/HoleStatus", byteBuffer, dims1, false);
      Attributes att = new Attributes();
      att.add(EnumAttributes.DESCRIPTION.fieldName, new String[] {"Type of ZMW that produced the data_ref"}, null, false);
      att.add("LookupTable", EnumHoleStatus.names);
      att.writeTo(obj);
    }
    {
      // NumXY
      short[] shortBuffer = new short[size() * 2];
      for (int ii = 0; ii < size(); ++ii) {
        int holenumber = ii + firsthole;
        shortBuffer[2 * ii] = (short) (holenumber % 2);
        shortBuffer[2 * ii + 1] = (short) holenumber;
      }
      final HObject obj = H5ScalarDSIO.Write(h5, spec.getZMWEnum().path + "/HoleXY", shortBuffer, dims2, true);
      Attributes att = new Attributes();
      att.add(EnumAttributes.DESCRIPTION.fieldName, new String[] {"Grid coordinates assigned to each ZMW on the chip"}, null, false);
      att.writeTo(obj);
    }
    {
      // NumEvent
      for (int ii = 0; ii < size(); ++ii) {
        intBuffer[ii] = buffer.getLength(ii);
      }
      final HObject obj = H5ScalarDSIO.Write(h5, spec.getZMWEnum().path + "/NumEvent", intBuffer, dims1, true);
      Attributes att = new Attributes();
      att.add(EnumAttributes.DESCRIPTION.fieldName, new String[] {"ZMW event-stream counts"}, null, false);
      att.writeTo(obj);
    }
    {
      // Productivity
      byte[] byteBuffer = new byte[size()];
      for (int ii = 0; ii < size(); ++ii) {
        byteBuffer[ii] = 1;
      }
      final HObject obj = H5ScalarDSIO.Write(h5, spec.getZMWMetricsEnum().path + "/Productivity", byteBuffer, dims1, false);
      Attributes att = new Attributes();
      att.add(EnumAttributes.DESCRIPTION.fieldName, new String[] {"ZMW productivity classification"}, null, false);
      att.add(EnumAttributes.UNITS_OR_ENCODING.fieldName, new String[] {"0:Empty,1:Productive,2:Other,255:NotDefined"}, null, false);
      att.writeTo(obj);
    }
    {
      // ReadScore
      float[] floatBuffer = new float[size()];
      for (int ii = 0; ii < size(); ++ii) {
        floatBuffer[ii] = (float) (buffer.getScore(ii)) / (float) 1001;
      }
      final HObject obj = H5ScalarDSIO.Write(h5, spec.getZMWMetricsEnum().path + "/ReadScore", floatBuffer, dims1, false);
      Attributes att = new Attributes();
      att.add(EnumAttributes.DESCRIPTION.fieldName, new String[] {"Read raw accuracy prediction"}, null, false);
      att.writeTo(obj);
    }
  }

}
