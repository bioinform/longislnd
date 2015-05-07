package com.bina.hdf5.h5.bax;

/**
 * Created by bayo on 5/4/15.
 */

import com.bina.hdf5.EnumDat;
import com.bina.hdf5.PBReadBuffer;
import com.bina.hdf5.h5.H5ScalarDSIO;
import ncsa.hdf.object.FileFormat;
import ncsa.hdf.object.h5.H5File;
import org.apache.log4j.Logger;

import java.util.EnumSet;

public class BaxH5Writer {

    public BaxH5Writer(String filename) {
        filename_ = filename;
        h5_ = new H5File(filename_, FileFormat.CREATE);
    }

    public void addLast(PBReadBuffer read, int score) {
        buffer_.addLast(read, score);
    }

    public void writeGroups() throws Exception {
        for (EnumGroups e : EnumSet.allOf(EnumGroups.class)) {
            h5_.createGroup(e.path(), null);
        }
    }

    public void writeBaseCalls() throws Exception {
        long[] dims = new long[]{buffer_.reads().size()};
        for (EnumDat e : EnumDat.getBaxSet()) {
            H5ScalarDSIO.Write(h5_, EnumGroups.BaseCalls.path() + e.path(), buffer_.reads().get(e).data(), dims);
        }
    }

    public int size() throws Exception {
        return buffer_.length_score().size() / 2;
    }

    public void close() throws Exception {
        h5_.close();
    }

    private String filename_;
    private H5File h5_ = null;
    private final DataBuffer buffer_ = new DataBuffer(100000);
    private final static Logger log = Logger.getLogger(BaxH5Writer.class.getName());

    public void writeRegions() throws Exception {
        final EnumSet<EnumTypeIdx> typeSet = EnumSet.of(EnumTypeIdx.TypeInsert, EnumTypeIdx.TypeHQRegion);
        int[] buffer = new int[size() * EnumRegionsIdx.NumFields.value() * typeSet.size()];
        final int[] length_score = buffer_.length_score().data();
        int shift = 0;
        for (int rr = 0; rr < size(); ++rr) {
            for (EnumTypeIdx e : typeSet) {
                buffer[shift + EnumRegionsIdx.HoleNumber.value()] = rr;
                buffer[shift + EnumRegionsIdx.RegionType.value()] = e.value();
                buffer[shift + EnumRegionsIdx.RegionStart.value()] = 0;
                buffer[shift + EnumRegionsIdx.RegionEnd.value()] = length_score[2 * rr];
                buffer[shift + EnumRegionsIdx.RegionScore.value()] = length_score[2 * rr + 1];
                shift += EnumRegionsIdx.NumFields.value();
            }
        }
        long[] dims = new long[]{buffer.length / EnumRegionsIdx.NumFields.value(), EnumRegionsIdx.NumFields.value()};
        H5ScalarDSIO.Write(h5_, EnumGroups.PulseData.path() + "/Regions", buffer, dims);
    }


    public void writeZWM() throws Exception {
        final int[] length_score = buffer_.length_score().data();
        final long[] dims_1 = new long[]{(long) size()};
        final long[] dims_2 = new long[]{(long) size(), (long) 2};

        int[] int_buffer = new int[size()];
        {
            //HoleNumber
            for (int ii = 0; ii < size(); ++ii) {
                int_buffer[ii] = ii;
            }
            H5ScalarDSIO.Write(h5_, EnumGroups.ZMW.path() + "/HoleNumber", int_buffer, dims_1);
        }
        {
            //HoleStatus
            byte[] byte_buffer = new byte[size()];
            for (int ii = 0; ii < size(); ++ii) {
                byte_buffer[ii] = 0;
            }
            H5ScalarDSIO.Write(h5_, EnumGroups.ZMW.path() + "/HoleStatus", byte_buffer, dims_1);
        }
        {
            //NumEvent
            short[] short_buffer = new short[size() * 2];
            for (int ii = 0; ii < size(); ++ii) {
                short_buffer[2 * ii] = 0;
                short_buffer[2 * ii + 1] = (short) ii;
            }
            H5ScalarDSIO.Write(h5_, EnumGroups.ZMW.path() + "/HoleXY", short_buffer, dims_1);
        }
        {
            //NumEvent
            for (int ii = 0; ii < size(); ++ii) {
                int_buffer[ii] = length_score[2 * ii];
            }
            H5ScalarDSIO.Write(h5_, EnumGroups.ZMW.path() + "/NumEvent", int_buffer, dims_1);
        }
        {
            //ReadScore
            float[] float_buffer = new float[size()];
            for (int ii = 0; ii < size(); ++ii) {
                float_buffer[ii] = (float) (length_score[2 * ii + 1]) / (float) 1000;
            }
            H5ScalarDSIO.Write(h5_, EnumGroups.ZMWMetrics.path() + "/ReadScore", float_buffer, dims_1);
        }
    }
}
