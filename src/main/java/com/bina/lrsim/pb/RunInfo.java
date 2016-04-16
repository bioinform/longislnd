package com.bina.lrsim.pb;

import com.bina.lrsim.pb.h5.Attributes;
import com.bina.lrsim.pb.h5.bax.EnumGroups;
import ncsa.hdf.object.h5.H5File;

/**
 * Created by bayolau on 4/11/16.
 */
public class RunInfo implements java.io.Serializable {
  public final String bindingKit;
  public final String instrumentName;
  public final String movieName;
  public final String platformName;
  public final String runCode;
  public final String sequencingChemistry;
  public final String sequencingKit;

  public RunInfo(H5File h5) {
    bindingKit = ExtractString(h5, "BindingKit");
    instrumentName = ExtractString(h5, "InstrumentName");
    movieName = ExtractString(h5, "MovieName");
    platformName = ExtractString(h5, "PlatformName");
    runCode = ExtractString(h5, "RunCode");
    sequencingChemistry = ExtractString(h5, "SequencingChemistry");
    sequencingKit = ExtractString(h5, "SequencingKit");
  }

  public RunInfo() {
    bindingKit = "100256000";
    instrumentName = "42213";
    movieName = "unknown";
    platformName = "Springfield";
    runCode = "2013-10-19_NGAT-213_CHM1h-3-Titration-P5C3_40-B01_2";
    sequencingChemistry = "P5-C3";
    sequencingKit = "100254800";
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("bindingKit: ");
    sb.append(bindingKit);
    sb.append(" ");
    sb.append("instrumentName: ");
    sb.append(instrumentName);
    sb.append(" ");
    sb.append("movieName: ");
    sb.append(movieName);
    sb.append(" ");
    sb.append("platformName: ");
    sb.append(platformName);
    sb.append(" ");
    sb.append("runCode: ");
    sb.append(runCode);
    sb.append(" ");
    sb.append("sequencingChemistry: ");
    sb.append(sequencingChemistry);
    sb.append(" ");
    sb.append("sequencingKit: ");
    sb.append(sequencingKit);
    return sb.toString();
  }

  private static String ExtractString(H5File h5, String field) {
    String ret = null;
    try {
      ret = ((String[]) Attributes.extract(h5.get(EnumGroups.RunInfo.path), field))[0];
    } catch (Exception e) {}
    if (ret == null) ret = "";
    return ret;
  }
}
