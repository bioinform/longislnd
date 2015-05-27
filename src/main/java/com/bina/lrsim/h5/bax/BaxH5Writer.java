package com.bina.lrsim.h5.bax;

/**
 * Created by bayo on 5/4/15.
 */

import java.io.IOException;
import java.util.EnumSet;

import com.bina.lrsim.h5.pb.PBSpec;
import ncsa.hdf.hdf5lib.exceptions.HDF5Exception;
import ncsa.hdf.object.FileFormat;
import ncsa.hdf.object.HObject;
import ncsa.hdf.object.h5.H5File;

import org.apache.log4j.Logger;

import com.bina.lrsim.h5.Attributes;
import com.bina.lrsim.h5.H5ScalarDSIO;
import com.bina.lrsim.h5.pb.EnumDat;
import com.bina.lrsim.h5.pb.PBReadBuffer;

public class BaxH5Writer {

  private final static Logger log = Logger.getLogger(BaxH5Writer.class.getName());
  private final DataBuffer buffer_;
  private final PBSpec spec;

  public BaxH5Writer(PBSpec spec) {
    this.spec = spec;
    buffer_ = new DataBuffer(spec,100000);
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

  public void addLast(PBReadBuffer read, int score) {
    buffer_.addLast(read, score);
  }

  private void writeGroups(H5File h5, AttributesFactory af) throws IOException {
    for (EnumGroups e : spec.getGroupSet() ) {
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
  }

  public int size() {
    return buffer_.size();
  }

  private void writeRegions(H5File h5, int firsthole) throws IOException {
    final EnumSet<EnumTypeIdx> typeSet = spec.getTypeIdx();
    int[] buffer = new int[size() * EnumRegionsIdx.values().length * typeSet.size()];
    int shift = 0;
    for (int rr = 0; rr < size(); ++rr) {
      for (EnumTypeIdx e : typeSet) {
        buffer[shift + EnumRegionsIdx.HoleNumber.value] = firsthole + rr;
        buffer[shift + EnumRegionsIdx.RegionType.value] = e.value;
        buffer[shift + EnumRegionsIdx.RegionStart.value] = 0;
        buffer[shift + EnumRegionsIdx.RegionEnd.value] = buffer_.getLength(rr);
        buffer[shift + EnumRegionsIdx.RegionScore.value] = buffer_.getScore(rr);
        shift += EnumRegionsIdx.values().length;
      }
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
        byte_buffer[ii] = 0;
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
      // Productivit
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
