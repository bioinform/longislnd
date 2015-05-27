package com.bina.lrsim.h5.pb;

import java.util.EnumSet;

import com.bina.lrsim.h5.bax.EnumGroups;
import com.bina.lrsim.h5.bax.EnumTypeIdx;

/**
 * Created by bayo on 5/25/15.
 */
public class PBBaxSpec extends PBSpec {
  @Override
  public String[] getDataDescription() {
    return new String[] {"Basecall", "DeletionQV", "DeletionTag", "InsertionQV", "MergeQV", "QualityValue", "SubstitutionQV", "SubstitutionTag", "uint8", "uint8", "uint8", "uint8", "uint8", "uint8", "uint8", "uint8"};

  }

  @Override
  public EnumSet<EnumGroups> getGroupSet() {
    return EnumSet.complementOf(EnumSet.of(EnumGroups.CBaseCalls, EnumGroups.CZMW, EnumGroups.CZMWMetrics, EnumGroups.CPasses));
  }

  @Override
  public EnumSet<EnumTypeIdx> getTypeIdx() {
    return EnumSet.of(EnumTypeIdx.TypeInsert, EnumTypeIdx.TypeHQRegion);
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
    return EnumSet.complementOf(EnumSet.of(EnumDat.AlnArray));
  }

  @Override
  public EnumSet<EnumDat> getNonBaseDataSet() {
    return EnumSet.complementOf(EnumSet.of(EnumDat.BaseCall, EnumDat.AlnArray));
  }

}
