package com.bina.lrsim.pb;

import com.bina.lrsim.h5.bax.EnumGroups;

import java.util.EnumSet;

/**
 * Created by bayolau on 1/8/16.
 */
public class PBClrBamSpec extends PBSpec {
  @Override
  public String[] getDataDescription() {
    return new String[] {"Basecall", "DeletionQV", "DeletionTag", "InsertionQV", "MergeQV", "QualityValue", "SubstitutionQV", "SubstitutionTag", "IDPV1", "uint8", "uint8", "uint8", "uint8", "uint8", "uint8", "uint8", "uint8", "uint8"};
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
    return ".bam";
  }

  @Override
  public EnumSet<EnumDat> getDataSet() {
    return EnumSet.complementOf(EnumSet.of(EnumDat.AlnArray));
  }

  @Override
  public EnumSet<EnumDat> getNonBaseDataSet() {
    return EnumSet.complementOf(EnumSet.of(EnumDat.BaseCall, EnumDat.AlnArray));
  }
}
