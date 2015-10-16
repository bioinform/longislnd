package com.bina.lrsim.h5.bax;

/**
 * Created by bayo on 5/4/15.
 */

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import com.bina.lrsim.bioinfo.Locus;
import ncsa.hdf.hdf5lib.exceptions.HDF5Exception;
import ncsa.hdf.object.FileFormat;
import ncsa.hdf.object.HObject;
import ncsa.hdf.object.h5.H5File;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.log4j.Logger;

import com.bina.lrsim.h5.Attributes;
import com.bina.lrsim.h5.H5ScalarDSIO;
import com.bina.lrsim.h5.pb.EnumDat;
import com.bina.lrsim.h5.pb.PBReadBuffer;
import com.bina.lrsim.h5.pb.PBSpec;

public class BaxH5Writer {

  private final static Logger log = Logger.getLogger(BaxH5Writer.class.getName());
  private final DataBuffer buffer_;
  private final PBSpec spec;
  private final ArrayList<Locus> loci_;

  public BaxH5Writer(PBSpec spec) {
    this.spec = spec;
    buffer_ = new DataBuffer(spec, 100000);
    this.loci_ = new ArrayList<Locus>();
  }

  public void writeLociBed(String prefix, String moviename, int firsthole) {
    try (FileWriter fw = new FileWriter(new File(prefix + ".bed"))) {
      int shift = 0;
      for (Locus entry : this.loci_) {
        fw.write(entry.getChrom());
        fw.write('\t');
        fw.write(String.valueOf(entry.getBegin0()));
        fw.write('\t');
        fw.write(String.valueOf(entry.getEnd0()));
        fw.write('\t');
        fw.write(moviename);
        fw.write('/');
        fw.write(String.valueOf(firsthole + shift));
        fw.write("\t500\t");
        fw.write(entry.isRc() ? '-' : '+');
        fw.write(System.lineSeparator());
        ++shift;
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void write(String filename, String moviename, int firsthole) {
    log.info("Writing to " + filename + " as movie " + moviename);
    H5File h5 = new H5File(filename, FileFormat.CREATE);
    try {
      h5.open();
      AttributesFactory af = new AttributesFactory(size(), moviename, spec);
      writeGroups(h5, af);
      writeBaseCalls(h5, af);
      writeZWM(h5, firsthole);
      writeRegions(h5, firsthole);
      h5.close();
      writeLociBed(filename, moviename, firsthole);
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

  public void addLast(PBReadBuffer read, ArrayList<Integer> readLengths, int score, Locus locus) {
    buffer_.addLast(read, readLengths, score);
    loci_.add(locus);
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
    long[] dims = new long[] {buffer_.getReadsRef().size()};
    for (EnumDat e : spec.getDataSet()) {
      try {
        final HObject obj = H5ScalarDSIO.Write(h5, spec.getBaseCallsEnum().path + e.path, buffer_.getReadsRef().get(e).toByteArray(), dims, e.isSigned);
        af.get(e).writeTo(obj);
      } catch (Exception exception) {
        throw new IOException("failed to write " + e.path);
      }
    }
    if (spec.getGroupSet().contains(EnumGroups.CPasses)) writePasses(h5);
  }

  private void writePasses(H5File h5) throws IOException {
    final int fakeNumPasses = 3;
    final long[] dims_reads = new long[] {(long) size()};
    final long[] dims_passes = new long[] {fakeNumPasses * (long) size()};
    byte[] byte_buffer = new byte[fakeNumPasses * size()];
    int[] int_buffer = new int[fakeNumPasses * size()];
    MersenneTwister gen = new MersenneTwister(1111);
    {
      for (int ii = 0; ii < size(); ++ii) {
        int value = gen.nextBoolean() ? 1 : 0;
        for (int jj = 0; jj < fakeNumPasses; ++jj, value = (value + 1) % 2) {
          byte_buffer[fakeNumPasses * ii + jj] = (byte) value;
        }
      }
      {
        final HObject obj = H5ScalarDSIO.Write(h5, EnumGroups.CPasses.path + "/AdapterHitAfter", byte_buffer, dims_passes, false);
        Attributes att = new Attributes();
        att.add(EnumAttributes.DESCRIPTION.fieldName, new String[] {"Flag indicating if an adapter hit was detected at the end of this pass"}, null, false);
        att.add(EnumAttributes.INDEX_FIELD.fieldName, new String[] {"NumPasses"}, null, false);
        att.writeTo(obj);
      }
      for (int ii = 0; ii < fakeNumPasses * size(); ++ii) {
        byte_buffer[ii] = (byte) ((byte_buffer[ii] + 1) % 2);
      }
      {
        final HObject obj = H5ScalarDSIO.Write(h5, EnumGroups.CPasses.path + "/AdapterHitBefore", byte_buffer, dims_passes, false);
        Attributes att = new Attributes();
        att.add(EnumAttributes.DESCRIPTION.fieldName, new String[] {"Flag indicating if an adapter hit was detected at the beginning of this pass"}, null, false);
        att.add(EnumAttributes.INDEX_FIELD.fieldName, new String[] {"NumPasses"}, null, false);
        att.writeTo(obj);
      }
    }
    {
      for (int ii = 0; ii < size(); ++ii) {
        int_buffer[ii] = fakeNumPasses;
      }
      final HObject obj = H5ScalarDSIO.Write(h5, EnumGroups.CPasses.path + "/NumPasses", int_buffer, dims_reads, true);
      Attributes att = new Attributes();
      att.add(EnumAttributes.DESCRIPTION.fieldName, new String[] {"ZMW event-stream counts"}, null, false);
      att.writeTo(obj);
    }
    {
      for (int ii = 0; ii < size(); ++ii) {
        int value = gen.nextBoolean() ? 1 : 0;
        for (int jj = 0; jj < fakeNumPasses; ++jj, value = (value + 1) % 2) {
          byte_buffer[fakeNumPasses * ii + jj] = (byte) value;
        }
      }
      final HObject obj = H5ScalarDSIO.Write(h5, EnumGroups.CPasses.path + "/PassDirection", byte_buffer, dims_passes, false);
      Attributes att = new Attributes();
      att.add(EnumAttributes.DESCRIPTION.fieldName, new String[] {"Direction of pass across the SMRTbell"}, null, false);
      att.add(EnumAttributes.INDEX_FIELD.fieldName, new String[] {"NumPasses"}, null, false);
      att.writeTo(obj);
    }
    {
      for (int ii = 0; ii < size(); ++ii) {
        for (int jj = 0; jj < fakeNumPasses; ++jj) {
          int_buffer[fakeNumPasses * ii + jj] = buffer_.getLength(ii);
        }
      }
      final HObject obj = H5ScalarDSIO.Write(h5, EnumGroups.CPasses.path + "/PassNumBases", int_buffer, dims_passes, false);
      Attributes att = new Attributes();
      att.add(EnumAttributes.DESCRIPTION.fieldName, new String[] {"Number of bases in circular consensus pass"}, null, false);
      att.add(EnumAttributes.INDEX_FIELD.fieldName, new String[] {"NumPasses"}, null, false);
      att.writeTo(obj);
    }
    {
      for (int ii = 0; ii < size(); ++ii) {
        int_buffer[ii] = 0;
      }
      for (int ii = 0; ii < size(); ++ii) {
        // this is not strictly correct wrt
        final int adapter_length = 50;
        int shift = adapter_length;
        for (int jj = 0; jj < fakeNumPasses; ++jj, shift += buffer_.getLength(ii) + adapter_length) {
          int_buffer[fakeNumPasses * ii + jj] = shift;
        }
      }
      final HObject obj = H5ScalarDSIO.Write(h5, EnumGroups.CPasses.path + "/PassStartBase", int_buffer, dims_passes, false);
      Attributes att = new Attributes();
      att.add(EnumAttributes.DESCRIPTION.fieldName, new String[] {"Index of first base in circular consensus pass"}, null, false);
      att.add(EnumAttributes.INDEX_FIELD.fieldName, new String[] {"NumPasses"}, null, false);
      att.writeTo(obj);
    }
  }

  public int size() {
    return buffer_.getNumReads();
  }

  private static void writeRegions(int[] buffer, int shift, int hole, int type, int start, int end, int score) {
    buffer[shift + EnumRegionsIdx.HoleNumber.value] = hole;
    buffer[shift + EnumRegionsIdx.RegionType.value] = type;
    buffer[shift + EnumRegionsIdx.RegionStart.value] = start;
    buffer[shift + EnumRegionsIdx.RegionEnd.value] = end;
    buffer[shift + EnumRegionsIdx.RegionScore.value] = score;
  }

  private void writeRegions(H5File h5, int firsthole) throws IOException {
    final int numEntries = size() + (spec.writeAdapterInsert() ? buffer_.getNumAdapterInsert() : 0);
    final int[] buffer = new int[numEntries * EnumRegionsIdx.values().length];
    int shift = 0;
    final int ins_score = -1; // insert score seems to be -1 in real data
    for (int rr = 0; rr < size(); ++rr) {
      final int hole = firsthole + rr;
      final int score = buffer_.getScore(rr);
      ArrayList<Integer> read_lengths = buffer_.getReadLengths(rr);
      if (spec.writeAdapterInsert()) {
        writeRegions(buffer, shift, hole, EnumTypeIdx.TypeInsert.value, 0, read_lengths.get(0), ins_score);
        shift += EnumRegionsIdx.values().length;
        for (int ii = 2; ii < read_lengths.size(); ii += 2) {
          writeRegions(buffer, shift, hole, EnumTypeIdx.TypeInsert.value, read_lengths.get(ii - 1), read_lengths.get(ii), ins_score);
          shift += EnumRegionsIdx.values().length;
        }
        for (int ii = 1; ii < read_lengths.size(); ii += 2) {
          writeRegions(buffer, shift, hole, EnumTypeIdx.TypeAdapter.value, read_lengths.get(ii - 1), read_lengths.get(ii), score);
          shift += EnumRegionsIdx.values().length;
        }
      }
      writeRegions(buffer, shift, hole, EnumTypeIdx.TypeHQRegion.value, 0, read_lengths.get(read_lengths.size() - 1), score);
      shift += EnumRegionsIdx.values().length;
    }
    long[] dims = new long[] {buffer.length / EnumRegionsIdx.values().length, EnumRegionsIdx.values().length};
    final HObject obj = H5ScalarDSIO.Write(h5, EnumGroups.PulseData.path + "/Regions", buffer, dims, true);
    Attributes att = new Attributes();
    att.add("ColumnNames", EnumRegionsIdx.getDescriptionArray(), new long[] {EnumRegionsIdx.values().length}, false);
    att.add("RegionDescriptions", new String[] {"Adapter Hit", "Insert Region", "High Quality bases region. Score is 1000 * predicted accuracy, where predicted accuray is 0 to 1.0"}, new long[] {3}, false); // typo
                                                                                                                                                                                                               // foolows
                                                                                                                                                                                                               // pacbio's
                                                                                                                                                                                                               // typo
    att.add("RegionSources", new String[] {"AdapterFinding", "AdapterFinding", "PulseToBase Region classifer"}, new long[] {3}, false); // typo follows pacbio's
                                                                                                                                        // typo
    att.add("RegionTypes", EnumTypeIdx.getDescriptionArray(), new long[] {EnumTypeIdx.values().length}, false);
    att.writeTo(obj);
  }


  public void writeZWM(H5File h5, int firsthole) throws IOException {
    final long[] dims_1 = new long[] {(long) size()};
    final long[] dims_2 = new long[] {(long) size(), (long) 2};

    int[] int_buffer = new int[size()];
    {
      // HoleNumber
      for (int ii = 0; ii < size(); ++ii) {
        int_buffer[ii] = firsthole + ii;
      }
      final HObject obj = H5ScalarDSIO.Write(h5, spec.getZMWEnum().path + "/HoleNumber", int_buffer, dims_1, false);
      Attributes att = new Attributes();
      att.add(EnumAttributes.DESCRIPTION.fieldName, new String[] {"Number assigned to each ZMW on the chip"}, null, false);
      att.writeTo(obj);
    }
    {
      // HoleStatus
      byte[] byte_buffer = new byte[size()];
      for (int ii = 0; ii < size(); ++ii) {
        byte_buffer[ii] = EnumHoleStatus.SEQUENCING.value;
      }
      final HObject obj = H5ScalarDSIO.Write(h5, spec.getZMWEnum().path + "/HoleStatus", byte_buffer, dims_1, false);
      Attributes att = new Attributes();
      att.add(EnumAttributes.DESCRIPTION.fieldName, new String[] {"Type of ZMW that produced the data_ref"}, null, false);
      att.add("LookupTable", new String[] {"SEQUENCING", "ANTIHOLE", "FIDUCIAL", "SUSPECT", "ANTIMIRROR", "FDZMW", "FBZMW", "ANTIBEAMLET", "OUTSIDEFOV"}, new long[] {9}, false);
      att.writeTo(obj);
    }
    {
      // NumXY
      short[] short_buffer = new short[size() * 2];
      for (int ii = 0; ii < size(); ++ii) {
        int holenumber = ii + firsthole;
        short_buffer[2 * ii] = (short) (holenumber % 2);
        short_buffer[2 * ii + 1] = (short) holenumber;
      }
      final HObject obj = H5ScalarDSIO.Write(h5, spec.getZMWEnum().path + "/HoleXY", short_buffer, dims_2, true);
      Attributes att = new Attributes();
      att.add(EnumAttributes.DESCRIPTION.fieldName, new String[] {"Grid coordinates assigned to each ZMW on the chip"}, null, false);
      att.writeTo(obj);
    }
    {
      // NumEvent
      for (int ii = 0; ii < size(); ++ii) {
        int_buffer[ii] = buffer_.getLength(ii);
      }
      final HObject obj = H5ScalarDSIO.Write(h5, spec.getZMWEnum().path + "/NumEvent", int_buffer, dims_1, true);
      Attributes att = new Attributes();
      att.add(EnumAttributes.DESCRIPTION.fieldName, new String[] {"ZMW event-stream counts"}, null, false);
      att.writeTo(obj);
    }
    {
      // Productivity
      byte[] byte_buffer = new byte[size()];
      for (int ii = 0; ii < size(); ++ii) {
        byte_buffer[ii] = 1;
      }
      final HObject obj = H5ScalarDSIO.Write(h5, spec.getZMWMetricsEnum().path + "/Productivity", byte_buffer, dims_1, false);
      Attributes att = new Attributes();
      att.add(EnumAttributes.DESCRIPTION.fieldName, new String[] {"ZMW productivity classification"}, null, false);
      att.add(EnumAttributes.UNITS_OR_ENCODING.fieldName, new String[] {"0:Empty,1:Productive,2:Other,255:NotDefined"}, null, false);
      att.writeTo(obj);
    }
    {
      // ReadScore
      float[] float_buffer = new float[size()];
      for (int ii = 0; ii < size(); ++ii) {
        float_buffer[ii] = (float) (buffer_.getScore(ii)) / (float) 1001;
      }
      final HObject obj = H5ScalarDSIO.Write(h5, spec.getZMWMetricsEnum().path + "/ReadScore", float_buffer, dims_1, false);
      Attributes att = new Attributes();
      att.add(EnumAttributes.DESCRIPTION.fieldName, new String[] {"Read raw accuracy prediction"}, null, false);
      att.writeTo(obj);
    }
  }
}
