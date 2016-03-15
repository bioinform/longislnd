package com.bina.lrsim.pb;

import java.util.EnumSet;
import java.util.Set;

import com.bina.lrsim.pb.h5.bax.EnumGroups;

/**
 * Created by bayo on 5/25/15.
 */
public class PBBaxSpec extends PBSpec {
  public static final String SUFFIX = ".bax.h5";

  @Override
  public String[] getDataDescription() {
    return new String[] {"Basecall", "DeletionQV", "DeletionTag", "InsertionQV", "MergeQV", "QualityValue", "SubstitutionQV", "SubstitutionTag", "uint8", "uint8", "uint8", "uint8", "uint8", "uint8", "uint8", "uint8"};
  }

  @Override
  public Set<EnumGroups> getGroupSet() {
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
    return SUFFIX;
  }

  @Override
  public Set<EnumDat> getDataSet() {
    return EnumSet.complementOf(EnumSet.of(EnumDat.AlnArray, EnumDat.IDPV1));
  }

  @Override
  public Set<EnumDat> getNonBaseDataSet() {
    return EnumSet.complementOf(EnumSet.of(EnumDat.BaseCall, EnumDat.AlnArray, EnumDat.IDPV1));
  }

}
