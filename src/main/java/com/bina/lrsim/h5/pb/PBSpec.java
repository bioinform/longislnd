package com.bina.lrsim.h5.pb;

import com.bina.lrsim.h5.bax.EnumGroups;
import com.bina.lrsim.h5.bax.EnumTypeIdx;

import java.util.EnumSet;

/**
 * Created by bayo on 5/25/15.
 */
public abstract class PBSpec {

  public abstract EnumSet<EnumDat> getDataSet();

  public abstract EnumSet<EnumDat> getNonBaseDataSet();

  public abstract String[] getDataDescription();

  public abstract EnumSet<EnumGroups> getGroupSet();

  public abstract EnumSet<EnumTypeIdx> getTypeIdx();

  public abstract EnumGroups getBaseCallsEnum();

  public abstract EnumGroups getZMWEnum();

  public abstract EnumGroups getZMWMetricsEnum();

  public abstract String getSuffix();
}
