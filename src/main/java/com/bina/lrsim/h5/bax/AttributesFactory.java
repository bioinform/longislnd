package com.bina.lrsim.h5.bax;

import com.bina.lrsim.h5.pb.EnumDat;
import com.bina.lrsim.h5.Attributes;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.EnumMap;

/**
 * Created by bayo on 5/7/15.
 */

class AttributesFactory {
    public static final String DESCRIPTION = "Description";

    private EnumMap<EnumDat,Attributes> ofDat_ = new EnumMap<>(EnumDat.class);
    private EnumMap<EnumGroups,Attributes> ofGrp_ = new EnumMap<>(EnumGroups.class);

    public Attributes get(EnumDat e) {
        return ofDat_.get(e);
    }

    public Attributes get(EnumGroups e) {
        Attributes ret = ofGrp_.get(e);
        if(ret != null) return ofGrp_.get(e);
        else return new Attributes();
    }

    public AttributesFactory(int num_reads, String movie_name) {
        {//for enum dat
            final String idxf = "IndexField";
            final String ne = "NumEvent";
            final String uoe = "UnitsOrEncoding";
            final String pqv = "Phred QV";
            {
                Attributes att = new Attributes();
                att.add(DESCRIPTION, new String[]{"Called base"}, null);
                att.add(idxf, new String[]{ne}, null);
                ofDat_.put(EnumDat.BaseCall, att);
            }
            {
                Attributes att = new Attributes();
                att.add(DESCRIPTION, new String[]{"Probability of deletion error prior to the current base"}, null);
                att.add(idxf, new String[]{ne}, null);
                att.add(uoe, new String[]{"Phred QV"}, null);
                ofDat_.put(EnumDat.DeletionQV, att);
            }
            {
                Attributes att = new Attributes();
                att.add(DESCRIPTION, new String[]{"Likely identity of deleted base"}, null);
                att.add(idxf, new String[]{ne}, null);
                ofDat_.put(EnumDat.DeletionTag, att);
            }
            {
                Attributes att = new Attributes();
                att.add(DESCRIPTION, new String[]{"Probability that the current base is an insertion"}, null);
                att.add(idxf, new String[]{ne}, null);
                att.add(uoe, new String[]{"Phred QV"}, null);
                ofDat_.put(EnumDat.InsertionQV, att);
            }
            {
                Attributes att = new Attributes();
                att.add(DESCRIPTION, new String[]{"Probability of merged-pulse error at the current base"}, null);
                att.add(idxf, new String[]{ne}, null);
                att.add(uoe, new String[]{"Phred QV"}, null);
                ofDat_.put(EnumDat.MergeQV, att);
            }
            {
                Attributes att = new Attributes();
                att.add(DESCRIPTION, new String[]{"Probability of basecalling error at the current base"}, null);
                att.add(idxf, new String[]{ne}, null);
                att.add(uoe, new String[]{"Phred QV"}, null);
                ofDat_.put(EnumDat.QualityValue, att);
            }
            {
                Attributes att = new Attributes();
                att.add(DESCRIPTION, new String[]{"Probability of substitution error at the current base"}, null);
                att.add(idxf, new String[]{ne}, null);
                att.add(uoe, new String[]{"Phred QV"}, null);
                ofDat_.put(EnumDat.SubstitutionQV, att);
            }
            {
                Attributes att = new Attributes();
                att.add(DESCRIPTION, new String[]{"Most likely alternative base"}, null);
                att.add(idxf, new String[]{ne}, null);
                ofDat_.put(EnumDat.SubstitutionTag, att);
            }
        }
        {// for enum group_
            {
                Attributes att = new Attributes();
                att.add("ChangeListID", new String[]{"2.1.0.0.126982"}, null);
                att.add("Content", EnumDat.getContentDescription(), new long[]{2,EnumDat.getContentDescription().length/2});
                att.add("CountStored", new int[]{num_reads}, null);
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SS'+00:00'");
                att.add("DateCreated", new String[]{df.format(Calendar.getInstance().getTime())}, null);
                att.add("QVDecoding", new String[]{"Standard Phred encoding: QV = -10 * log10(p) - where p is the probability of error"}, null);
                att.add("SchemaRevision", new String[]{"1.0"}, null);
                ofGrp_.put(EnumGroups.BaseCalls, att);
            }
            {
                Attributes att = new Attributes();
                //This should be refactered to go along with ZMW group
                att.add("SchemaRevision", new String[]{"NumEvent","HoleNumber","HoleXY","HoleStatus","int32","uint32","int16","uint8"}, new long[]{2,4});
                ofGrp_.put(EnumGroups.ZMW, att);
            }
            {
                Attributes att = new Attributes();
                att.add("ChangeListID", new String[]{"126982"}, null);
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'+0000'");
                att.add("DateCreated", new String[]{df.format(Calendar.getInstance().getTime())}, null);
                att.add("FormatVersion", new String[]{"Springfield 1.1"}, null);
                att.add("SoftwareVersion", new String[]{"Otto 2.1.0.0"}, null);
                ofGrp_.put(EnumGroups.ScanData, att);
            }
            {
                Attributes att = new Attributes();
                att.add("BaseMap", new String[]{"TGAC"}, null);
                att.add("Name", new String[]{"NA"}, null);
                att.add("NumAnalog", new short[]{4}, null);
                ofGrp_.put(EnumGroups.DyeSet, att);
            }
            {
                Attributes att = new Attributes();
                att.add("Base", new String[]{"T"}, null);
                att.add("CalMovieDate", new String[]{"NA"}, null);
                att.add("Label", new String[]{"Ch1-FAB4.F-dT"}, null);
                att.add("Nucleotide", new String[]{"dT6P"}, null);
                att.add("RecordDate", new String[]{"2013-08-27T00:48:13-0800"},null);
                att.add("RecordName", new String[]{"m130827_184651_638_42213_Channel1_SIA_130827_184651.dye.h5"},null);
                att.add("Type", new String[]{"NA"}, null);
                att.add("TypeId", new int[]{0}, null);
                att.add("Wavelength", new float[]{433}, null);
                ofGrp_.put(EnumGroups.A0, att);
            }
            {
                Attributes att = new Attributes();
                att.add("Base", new String[]{"G"}, null);
                att.add("CalMovieDate", new String[]{"NA"}, null);
                att.add("Label", new String[]{"Ch2-FAB4.F-dG"}, null);
                att.add("Nucleotide", new String[]{"dG6P"}, null);
                att.add("RecordDate", new String[]{"2013-08-27T00:48:13-0800"},null);
                att.add("RecordName", new String[]{"m130827_191805_577_42213_Channel2_SIA_130827_191805.dye.h5"},null);
                att.add("Type", new String[]{"NA"}, null);
                att.add("TypeId", new int[]{0}, null);
                att.add("Wavelength", new float[]{488}, null);
                ofGrp_.put(EnumGroups.A1, att);
            }
            {
                Attributes att = new Attributes();
                att.add("Base", new String[]{"A"}, null);
                att.add("CalMovieDate", new String[]{"NA"}, null);
                att.add("Label", new String[]{"Ch3-FAB4.F-dA"}, null);
                att.add("Nucleotide", new String[]{"dA6P"}, null);
                att.add("RecordDate", new String[]{"2013-08-27T00:48:13-0800"},null);
                att.add("RecordName", new String[]{"m130827_194847_892_42213_Channel3_SIA_130827_194848.dye.h5"},null);
                att.add("Type", new String[]{"NA"}, null);
                att.add("TypeId", new int[]{0}, null);
                att.add("Wavelength", new float[]{538}, null);
                ofGrp_.put(EnumGroups.A2, att);
            }
            {
                Attributes att = new Attributes();
                att.add("Base", new String[]{"C"}, null);
                att.add("CalMovieDate", new String[]{"NA"}, null);
                att.add("Label", new String[]{"Ch4-FAB4.F-dC"}, null);
                att.add("Nucleotide", new String[]{"dC6P"}, null);
                att.add("RecordDate", new String[]{"2013-08-27T00:48:13-0800"},null);
                att.add("RecordName", new String[]{"m130827_201802_353_42213_Channel4_SIA_130827_201805.dye.h5"},null);
                att.add("Type", new String[]{"NA"}, null);
                att.add("TypeId", new int[]{0}, null);
                att.add("Wavelength", new float[]{633}, null);
                ofGrp_.put(EnumGroups.A3, att);
            }
            {
                Attributes att = new Attributes();
                att.add("BindingKit", new String[]{"100256000"}, null);
                att.add("Control", new String[]{""}, null);
                att.add("InstrumentId", new int[]{1}, null);
                att.add("InstrumentName", new String[]{"42213"}, null);
                att.add("IsControlUsed", new String[]{"False"}, null);
                att.add("MovieName", new String[]{movie_name}, null);
                att.add("PlatformId", new int[]{2}, null);
                att.add("PlatformName", new String[]{"Springfield"}, null);
                att.add("RunCode", new String[]{"2013-10-19_NGAT-213_CHM1h-3-Titration-P5C3_40-B01_2"}, null);
                att.add("RunId", new int[]{1}, null);
                att.add("SequencingChemistry", new String[]{"P5-C3"}, null);
                att.add("SequencingKit", new String[]{"100254800"}, null);
                ofGrp_.put(EnumGroups.RunInfo, att);
            }
            {
                Attributes att = new Attributes();
                att.add("AduGain", new float[]{(float)0.485437}, null);
                att.add("CameraGain", new float[]{(float)1.0}, null);
                att.add("CameraType", new int[]{200}, null);
                att.add("DOELayout", new String[]{"RS2"}, null);
                att.add("FrameRate", new float[]{(float)75.0}, null);
                att.add("HotStartFrame", new int[]{1285}, null);
                att.add("HotStartFrameValid", new byte[]{1}, null);
                att.add("LaserIntensity", new float[]{(float)1.0,(float)1.0}, new long[]{2});
                att.add("LaserOnFrame", new int[]{-1003}, null);
                att.add("LaserOnFrameValid", new byte[]{1}, null);
                att.add("LaserPower", new float[]{(float)1.0,(float)1.0}, new long[]{2});
                att.add("Look", new byte[]{0}, null);
                att.add("NumCameras", new byte[]{4}, null);
                att.add("NumFrames", new int[]{809900}, null);
                att.add("NumLasers", new byte[]{2}, null);
                ofGrp_.put(EnumGroups.AcqParams, att);
            }

        }

    }
}
