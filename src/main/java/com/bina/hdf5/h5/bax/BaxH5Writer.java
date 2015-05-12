package com.bina.hdf5.h5.bax;

/**
 * Created by bayo on 5/4/15.
 */

import com.bina.hdf5.h5.pb.EnumDat;
import com.bina.hdf5.h5.pb.PBReadBuffer;
import com.bina.hdf5.h5.Attributes;
import com.bina.hdf5.h5.H5ScalarDSIO;
import ncsa.hdf.object.FileFormat;
import ncsa.hdf.object.HObject;
import ncsa.hdf.object.h5.H5File;
import org.apache.log4j.Logger;

import java.util.EnumSet;

public class BaxH5Writer {

    public void write(String filename, String moviename, int firsthole) throws Exception {
        log.info("Writing to " + filename + " as movie " + moviename);
        H5File h5 = new H5File(filename, FileFormat.CREATE);
        h5.open();
        AttributesFactory af = new AttributesFactory(size(),moviename);
        writeGroups(h5,af);
        writeBaseCalls(h5,af);
        writeZWM(h5,firsthole);
        writeRegions(h5,firsthole);
        h5.close();
    }

    public void addLast(PBReadBuffer read, int score) {
        buffer_.addLast(read, score);
    }

    private void writeGroups(H5File h5, AttributesFactory af) throws Exception {
        for (EnumGroups e : EnumSet.allOf(EnumGroups.class)) {
            final HObject obj = h5.createGroup(e.path(), null);
            af.get(e).writeTo(obj);
        }
    }

    private void writeBaseCalls(H5File h5, AttributesFactory af) throws Exception {
        long[] dims = new long[]{buffer_.reads().size()};
        for (EnumDat e : EnumDat.getBaxSet()) {
            final HObject obj = H5ScalarDSIO.Write(h5, EnumGroups.BaseCalls.path() + e.path(), buffer_.reads().get(e).data_ref(), dims);
            af.get(e).writeTo(obj);
        }
    }

    public int size() throws Exception {
        return buffer_.length_score().size() / 2;
    }

    private final DataBuffer buffer_ = new DataBuffer(100000);
    private final static Logger log = Logger.getLogger(BaxH5Writer.class.getName());

    private void writeRegions(H5File h5, int firsthole) throws Exception {
        final EnumSet<EnumTypeIdx> typeSet = EnumSet.of(EnumTypeIdx.TypeInsert, EnumTypeIdx.TypeHQRegion);
        int[] buffer = new int[size() * EnumRegionsIdx.values().length * typeSet.size()];
        final int[] length_score = buffer_.length_score().data_ref();
        int shift = 0;
        for (int rr = 0; rr < size(); ++rr) {
            for (EnumTypeIdx e : typeSet) {
                buffer[shift + EnumRegionsIdx.HoleNumber.value()] = firsthole + rr;
                buffer[shift + EnumRegionsIdx.RegionType.value()] = e.value();
                buffer[shift + EnumRegionsIdx.RegionStart.value()] = 0;
                buffer[shift + EnumRegionsIdx.RegionEnd.value()] = length_score[2 * rr];
                buffer[shift + EnumRegionsIdx.RegionScore.value()] = length_score[2 * rr + 1];
                shift += EnumRegionsIdx.values().length;
            }
        }
        long[] dims = new long[]{buffer.length / EnumRegionsIdx.values().length, EnumRegionsIdx.values().length};
        final HObject obj = H5ScalarDSIO.Write(h5, EnumGroups.PulseData.path() + "/Regions", buffer, dims);
        Attributes att = new Attributes();
        att.add("ColumnNames", EnumRegionsIdx.getDescriptionArray(),new long[]{EnumRegionsIdx.values().length});
        att.add("RegionDescriptions", new String[]{"Adapter Hit","Insert Region","High Quality bases region. Score is 1000 * predicted accuracy, where predicted accuray is 0 to 1.0"}, new long[]{3}); //typo foolows pacbio's typo
        att.add("RegionSources", new String[]{"AdapterFinding","AdapterFinding","PulseToBase Region classifer"}, new long[]{3}); // typo follows pacbio's typo
        att.add("RegionTypes", EnumTypeIdx.getDescriptionArray(),new long[]{EnumTypeIdx.values().length});
        att.writeTo(obj);
    }


    public void writeZWM(H5File h5, int firsthole) throws Exception {
        final int[] length_score = buffer_.length_score().data_ref();
        final long[] dims_1 = new long[]{(long) size()};
        final long[] dims_2 = new long[]{(long) size(), (long) 2};

        int[] int_buffer = new int[size()];
        {
            //HoleNumber
            for (int ii = 0; ii < size(); ++ii) {
                int_buffer[ii] = firsthole + ii;
            }
            final HObject obj = H5ScalarDSIO.Write(h5, EnumGroups.ZMW.path() + "/HoleNumber", int_buffer, dims_1);
            Attributes att = new Attributes();
            att.add(AttributesFactory.DESCRIPTION, new String[]{"Number assigned to each ZMW on the chip"}, null);
            att.writeTo(obj);
        }
        {
            //HoleStatus
            byte[] byte_buffer = new byte[size()];
            for (int ii = 0; ii < size(); ++ii) {
                byte_buffer[ii] = 0;
            }
            final HObject obj = H5ScalarDSIO.Write(h5, EnumGroups.ZMW.path() + "/HoleStatus", byte_buffer, dims_1);
            Attributes att = new Attributes();
            att.add(AttributesFactory.DESCRIPTION, new String[]{"Type of ZMW that produced the data_ref"}, null);
            att.add( "LookupTable"
                   , new String[]{"SEQUENCING","ANTIHOLE","FIDUCIAL","SUSPECT","ANTIMIRROR","FDZMW","FBZMW","ANTIBEAMLET","OUTSIDEFOV"}
                   , new long[]{9});
            att.writeTo(obj);
        }
        {
            //NumXY
            short[] short_buffer = new short[size() * 2];
            for (int ii = 0; ii < size(); ++ii) {
                int holenumber = ii + firsthole;
                short_buffer[2 * ii] = (short)(holenumber%2);
                short_buffer[2 * ii + 1] = (short) holenumber;
            }
            final HObject obj = H5ScalarDSIO.Write(h5, EnumGroups.ZMW.path() + "/HoleXY", short_buffer, dims_2);
            Attributes att = new Attributes();
            att.add(AttributesFactory.DESCRIPTION, new String[]{"Grid coordinates assigned to each ZMW on the chip"}, null);
            att.writeTo(obj);
        }
        {
            //NumEvent
            for (int ii = 0; ii < size(); ++ii) {
                int_buffer[ii] = length_score[2 * ii];
            }
            final HObject obj = H5ScalarDSIO.Write(h5, EnumGroups.ZMW.path() + "/NumEvent", int_buffer, dims_1);
            Attributes att = new Attributes();
            att.add(AttributesFactory.DESCRIPTION, new String[]{"ZMW event-stream counts"}, null);
            att.writeTo(obj);
        }
        {
            //ReadScore
            float[] float_buffer = new float[size()];
            for (int ii = 0; ii < size(); ++ii) {
                float_buffer[ii] = (float) (length_score[2 * ii + 1]) / (float) 1001;
            }
            final HObject obj = H5ScalarDSIO.Write(h5, EnumGroups.ZMWMetrics.path() + "/ReadScore", float_buffer, dims_1);
            Attributes att = new Attributes();
            att.add(AttributesFactory.DESCRIPTION, new String[]{"Read raw accuracy prediction"}, null);
            att.writeTo(obj);
        }
    }
}
