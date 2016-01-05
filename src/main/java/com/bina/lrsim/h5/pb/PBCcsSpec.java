package com.bina.lrsim.h5.pb;

import java.util.EnumSet;

import com.bina.lrsim.h5.bax.EnumGroups;

/**
 * Created by bayo on 5/26/15.
 */
public class PBCcsSpec extends PBSpec {
  @Override
  public String[] getDataDescription() {
    return new String[] {"Basecall", "DeletionQV", "DeletionTag", "InsertionQV", "MergeQV", "QualityValue", "SubstitutionQV", "SubstitutionTag", "uint8", "uint8", "uint8", "uint8", "uint8", "uint8", "uint8", "uint8"};

  }

  @Override
  public EnumSet<EnumGroups> getGroupSet() {
    return EnumSet.complementOf(EnumSet.of(EnumGroups.ZMW, EnumGroups.ZMWMetrics));
  }

  @Override
  public boolean writeAdapterInsert() {
    return false;
  }

  @Override
  public EnumGroups getBaseCallsEnum() {
    return EnumGroups.CBaseCalls;
  }

  @Override
  public EnumGroups getZMWEnum() {
    return EnumGroups.CZMW;
  }

  @Override
  public EnumGroups getZMWMetricsEnum() {
    return EnumGroups.CZMWMetrics;
  }

  @Override
  public String getSuffix() {
    return ".ccs.h5";
  }

  @Override
  public EnumSet<EnumDat> getDataSet() {
    return EnumSet.complementOf(EnumSet.of(EnumDat.AlnArray, EnumDat.MergeQV));
  }

  @Override
  public EnumSet<EnumDat> getNonBaseDataSet() {
    return EnumSet.complementOf(EnumSet.of(EnumDat.BaseCall, EnumDat.AlnArray, EnumDat.MergeQV));
  }
}
