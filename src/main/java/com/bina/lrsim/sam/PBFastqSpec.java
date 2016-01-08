package com.bina.lrsim.sam;

import java.util.EnumSet;

import com.bina.lrsim.h5.bax.EnumGroups;
import com.bina.lrsim.pb.EnumDat;
import com.bina.lrsim.pb.PBSpec;

/**
 * Created by bayo on 5/25/15.
 */
public class PBFastqSpec extends PBSpec {
  @Override
  public String[] getDataDescription() {
    return new String[] {"Basecall", "QualityValue", "uint8", "uint8"};

  }

  @Override
  public EnumSet<EnumGroups> getGroupSet() {
    return EnumSet.complementOf(EnumSet.of(EnumGroups.CBaseCalls, EnumGroups.CZMW, EnumGroups.CZMWMetrics, EnumGroups.CPasses));
  }

  @Override
  public boolean writeAdapterInsert() {
    return true;
  }

  @Override
  public EnumGroups getBaseCallsEnum() {
    return EnumGroups.BaseCalls;
  }

  @Override
  public EnumGroups getZMWEnum() {
    return EnumGroups.ZMW;
  }

  @Override
  public EnumGroups getZMWMetricsEnum() {
    return EnumGroups.ZMWMetrics;
  }

  @Override
  public String getSuffix() {
    return ".bax.h5";
  }

  @Override
  public EnumSet<EnumDat> getDataSet() {
    return EnumSet.of(EnumDat.BaseCall, EnumDat.QualityValue);
  }

  @Override
  public EnumSet<EnumDat> getNonBaseDataSet() {
    return EnumSet.of(EnumDat.QualityValue);
  }

}
