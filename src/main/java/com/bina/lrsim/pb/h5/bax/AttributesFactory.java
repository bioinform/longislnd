package com.bina.lrsim.pb.h5.bax;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.EnumMap;
import java.util.Map;

import com.bina.lrsim.pb.RunInfo;
import com.bina.lrsim.pb.h5.Attributes;
import com.bina.lrsim.pb.EnumDat;
import com.bina.lrsim.pb.Spec;

/**
 * Created by bayo on 5/7/15.
 */

class AttributesFactory {

  private Map<EnumDat, Attributes> ofDat = new EnumMap<>(EnumDat.class);
  private Map<EnumGroups, Attributes> ofGrp = new EnumMap<>(EnumGroups.class);

  public AttributesFactory(int numReads, String movieName, Spec spec, RunInfo runInfo) {
    {// for enum dat
      {
        Attributes att = new Attributes();
        att.add(EnumAttributes.DESCRIPTION.fieldName, new String[] {"Called base"}, null, false);
        att.add(EnumAttributes.INDEX_FIELD.fieldName, new String[] {EnumAttributes.NUM_EVENT.fieldName}, null, false);
        ofDat.put(EnumDat.BaseCall, att);
      }
      {
        Attributes att = new Attributes();
        att.add(EnumAttributes.DESCRIPTION.fieldName, new String[] {"Probability of deletion error prior to the current base"}, null, false);
        att.add(EnumAttributes.INDEX_FIELD.fieldName, new String[] {EnumAttributes.NUM_EVENT.fieldName}, null, false);
        att.add(EnumAttributes.UNITS_OR_ENCODING.fieldName, new String[] {EnumAttributes.PHRED_QV.fieldName}, null, false);
        ofDat.put(EnumDat.DeletionQV, att);
      }
      {
        Attributes att = new Attributes();
        att.add(EnumAttributes.DESCRIPTION.fieldName, new String[] {"Likely identity of deleted base"}, null, false);
        att.add(EnumAttributes.INDEX_FIELD.fieldName, new String[] {EnumAttributes.NUM_EVENT.fieldName}, null, false);
        ofDat.put(EnumDat.DeletionTag, att);
      }
      {
        Attributes att = new Attributes();
        att.add(EnumAttributes.DESCRIPTION.fieldName, new String[] {"Probability that the current base is an insertion"}, null, false);
        att.add(EnumAttributes.INDEX_FIELD.fieldName, new String[] {EnumAttributes.NUM_EVENT.fieldName}, null, false);
        att.add(EnumAttributes.UNITS_OR_ENCODING.fieldName, new String[] {EnumAttributes.PHRED_QV.fieldName}, null, false);
        ofDat.put(EnumDat.InsertionQV, att);
      }
      {
        Attributes att = new Attributes();
        att.add(EnumAttributes.DESCRIPTION.fieldName, new String[] {"Probability of merged-pulse error at the current base"}, null, false);
        att.add(EnumAttributes.INDEX_FIELD.fieldName, new String[] {EnumAttributes.NUM_EVENT.fieldName}, null, false);
        att.add(EnumAttributes.UNITS_OR_ENCODING.fieldName, new String[] {EnumAttributes.PHRED_QV.fieldName}, null, false);
        ofDat.put(EnumDat.MergeQV, att);
      }
      {
        Attributes att = new Attributes();
        att.add(EnumAttributes.DESCRIPTION.fieldName, new String[] {"Probability of basecalling error at the current base"}, null, false);
        att.add(EnumAttributes.INDEX_FIELD.fieldName, new String[] {EnumAttributes.NUM_EVENT.fieldName}, null, false);
        att.add(EnumAttributes.UNITS_OR_ENCODING.fieldName, new String[] {EnumAttributes.PHRED_QV.fieldName}, null, false);
        ofDat.put(EnumDat.QualityValue, att);
      }
      {
        Attributes att = new Attributes();
        att.add(EnumAttributes.DESCRIPTION.fieldName, new String[] {"Probability of substitution error at the current base"}, null, false);
        att.add(EnumAttributes.INDEX_FIELD.fieldName, new String[] {EnumAttributes.NUM_EVENT.fieldName}, null, false);
        att.add(EnumAttributes.UNITS_OR_ENCODING.fieldName, new String[] {EnumAttributes.PHRED_QV.fieldName}, null, false);
        ofDat.put(EnumDat.SubstitutionQV, att);
      }
      {
        Attributes att = new Attributes();
        att.add(EnumAttributes.DESCRIPTION.fieldName, new String[] {"Most likely alternative base"}, null, false);
        att.add(EnumAttributes.INDEX_FIELD.fieldName, new String[] {EnumAttributes.NUM_EVENT.fieldName}, null, false);
        ofDat.put(EnumDat.SubstitutionTag, att);
      }
    }
    {// for enum group_
      {
        Attributes att = new Attributes();
        att.add("ChangeListID", new String[] {"2.1.0.0.126982"}, null, false);
        if (spec.getBaseCallsEnum().equals(EnumGroups.BaseCalls)) {
          att.add("Content", spec.getDataDescription(), new long[] {2, spec.getDataDescription().length / 2}, false);
          att.add("CountStored", new int[] {numReads}, null, true);
          DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SS'+00:00'");
          att.add("DateCreated", new String[] {df.format(Calendar.getInstance().getTime())}, null, false);
          att.add("QVDecoding", new String[] {"Standard Phred encoding: QV = -10 * log10(p) - where p is the probability of error"}, null, false);
          att.add("SchemaRevision", new String[] {"1.0"}, null, false);
        }
        ofGrp.put(EnumGroups.BaseCalls, att);
      }
      {
        Attributes att = new Attributes();
        // This should be refactered to go along with ZMW group
        att.add("SchemaRevision", new String[] {"NumEvent", "HoleNumber", "HoleXY", "HoleStatus", "int32", "uint32", "int16", "uint8"}, new long[] {2, 4}, false);
        ofGrp.put(EnumGroups.ZMW, att);
        ofGrp.put(EnumGroups.CZMW, att);
      }
      {
        Attributes att = new Attributes();
        att.add("ChangeListID", new String[] {"2.3.0.0.140936"}, null, false);
        if (spec.getBaseCallsEnum().equals(EnumGroups.CBaseCalls)) {
          att.add("Content", spec.getDataDescription(), new long[] {2, spec.getDataDescription().length / 2}, false);
          att.add("CountStored", new int[] {numReads}, null, true);
          DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SS'+00:00'");
          att.add("DateCreated", new String[] {df.format(Calendar.getInstance().getTime())}, null, false);
          att.add("QVDecoding", new String[] {"Standard Phred encoding: QV = -10 * log10(p) - where p is the probability of error"}, null, false);
          att.add("SchemaRevision", new String[] {"1.0"}, null, false);
        }
        ofGrp.put(EnumGroups.CBaseCalls, att);
      }
      {
        Attributes att = new Attributes();
        att.add("ChangeListID", new String[] {"126982"}, null, false);
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'+0000'");
        att.add("DateCreated", new String[] {df.format(Calendar.getInstance().getTime())}, null, false);
        att.add("FormatVersion", new String[] {"Springfield 1.1"}, null, false);
        att.add("SoftwareVersion", new String[] {"Otto 2.1.0.0"}, null, false);
        ofGrp.put(EnumGroups.ScanData, att);
      }
      {
        Attributes att = new Attributes();
        att.add("BaseMap", new String[] {"TGAC"}, null, false);
        att.add("Name", new String[] {"NA"}, null, false);
        att.add("NumAnalog", new short[] {4}, null, false);
        ofGrp.put(EnumGroups.DyeSet, att);
      }
      {
        Attributes att = new Attributes();
        att.add("Base", new String[] {"T"}, null, false);
        att.add("CalMovieDate", new String[] {"NA"}, null, false);
        att.add("Label", new String[] {"Ch1-FAB4.F-dT"}, null, false);
        att.add("Nucleotide", new String[] {"dT6P"}, null, false);
        att.add("RecordDate", new String[] {"2013-08-27T00:48:13-0800"}, null, false);
        att.add("RecordName", new String[] {"m130827_184651_638_42213_Channel1_SIA_130827_184651.dye.h5"}, null, false);
        att.add("Type", new String[] {"NA"}, null, false);
        att.add("TypeId", new int[] {0}, null, true);
        att.add("Wavelength", new float[] {433}, null, false);
        ofGrp.put(EnumGroups.A0, att);
      }
      {
        Attributes att = new Attributes();
        att.add("Base", new String[] {"G"}, null, false);
        att.add("CalMovieDate", new String[] {"NA"}, null, false);
        att.add("Label", new String[] {"Ch2-FAB4.F-dG"}, null, false);
        att.add("Nucleotide", new String[] {"dG6P"}, null, false);
        att.add("RecordDate", new String[] {"2013-08-27T00:48:13-0800"}, null, false);
        att.add("RecordName", new String[] {"m130827_191805_577_42213_Channel2_SIA_130827_191805.dye.h5"}, null, false);
        att.add("Type", new String[] {"NA"}, null, false);
        att.add("TypeId", new int[] {0}, null, true);
        att.add("Wavelength", new float[] {488}, null, false);
        ofGrp.put(EnumGroups.A1, att);
      }
      {
        Attributes att = new Attributes();
        att.add("Base", new String[] {"A"}, null, false);
        att.add("CalMovieDate", new String[] {"NA"}, null, false);
        att.add("Label", new String[] {"Ch3-FAB4.F-dA"}, null, false);
        att.add("Nucleotide", new String[] {"dA6P"}, null, false);
        att.add("RecordDate", new String[] {"2013-08-27T00:48:13-0800"}, null, false);
        att.add("RecordName", new String[] {"m130827_194847_892_42213_Channel3_SIA_130827_194848.dye.h5"}, null, false);
        att.add("Type", new String[] {"NA"}, null, false);
        att.add("TypeId", new int[] {0}, null, true);
        att.add("Wavelength", new float[] {538}, null, false);
        ofGrp.put(EnumGroups.A2, att);
      }
      {
        Attributes att = new Attributes();
        att.add("Base", new String[] {"C"}, null, false);
        att.add("CalMovieDate", new String[] {"NA"}, null, false);
        att.add("Label", new String[] {"Ch4-FAB4.F-dC"}, null, false);
        att.add("Nucleotide", new String[] {"dC6P"}, null, false);
        att.add("RecordDate", new String[] {"2013-08-27T00:48:13-0800"}, null, false);
        att.add("RecordName", new String[] {"m130827_201802_353_42213_Channel4_SIA_130827_201805.dye.h5"}, null, false);
        att.add("Type", new String[] {"NA"}, null, false);
        att.add("TypeId", new int[] {0}, null, true);
        att.add("Wavelength", new float[] {633}, null, false);
        ofGrp.put(EnumGroups.A3, att);
      }
      {
        Attributes att = new Attributes();
        att.add("BindingKit", new String[] {runInfo.bindingKit}, null, false);
        att.add("Control", new String[] {""}, null, false);
        att.add("InstrumentId", new int[] {1}, null, false);
        att.add("InstrumentName", new String[] {runInfo.instrumentName}, null, false);
        att.add("IsControlUsed", new String[] {"False"}, null, false);
        att.add("MovieName", new String[] {movieName}, null, false);
        att.add("PlatformId", new int[] {2}, null, false);
        att.add("PlatformName", new String[] {runInfo.platformName}, null, false);
        att.add("RunCode", new String[] {runInfo.runCode}, null, false);
        att.add("RunId", new int[] {1}, null, false);
        att.add("SequencingChemistry", new String[] {runInfo.sequencingChemistry}, null, false);
        att.add("SequencingKit", new String[] {runInfo.sequencingKit}, null, false);
        ofGrp.put(EnumGroups.RunInfo, att);
      }
      {
        Attributes att = new Attributes();
        att.add("AduGain", new float[] {(float) 0.485437}, null, false);
        att.add("CameraGain", new float[] {(float) 1.0}, null, false);
        att.add("CameraType", new int[] {200}, null, true);
        att.add("DOELayout", new String[] {"RS2"}, null, false);
        att.add("FrameRate", new float[] {(float) 75.0}, null, false);
        att.add("HotStartFrame", new int[] {1285}, null, true);
        att.add("HotStartFrameValid", new byte[] {1}, null, false);
        att.add("LaserIntensity", new float[] {(float) 1.0, (float) 1.0}, new long[] {2}, false);
        att.add("LaserOnFrame", new int[] {-1003}, null, true);
        att.add("LaserOnFrameValid", new byte[] {1}, null, false);
        att.add("LaserPower", new float[] {(float) 1.0, (float) 1.0}, new long[] {2}, false);
        att.add("Look", new byte[] {0}, null, false);
        att.add("NumCameras", new byte[] {4}, null, false);
        att.add("NumFrames", new int[] {809900}, null, false);
        att.add("NumLasers", new byte[] {2}, null, false);
        ofGrp.put(EnumGroups.AcqParams, att);
      }

    }

  }

  public Attributes get(EnumDat e) {
    return ofDat.get(e);
  }

  public Attributes get(EnumGroups e) {
    Attributes ret = ofGrp.get(e);
    if (ret != null) return ofGrp.get(e);
    else return new Attributes();
  }
}
