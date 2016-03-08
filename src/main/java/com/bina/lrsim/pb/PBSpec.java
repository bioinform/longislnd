package com.bina.lrsim.pb;

import com.bina.lrsim.pb.h5.bax.EnumGroups;

import java.util.EnumSet;
import java.util.Set;

/**
 * Created by bayo on 5/25/15.
 */
public abstract class PBSpec {

  public abstract Set<EnumDat> getDataSet();

  public abstract Set<EnumDat> getNonBaseDataSet();

  public abstract String[] getDataDescription();

  public abstract Set<EnumGroups> getGroupSet();

  public abstract boolean writeAdapterInsert();

  public abstract EnumGroups getBaseCallsEnum();

  public abstract EnumGroups getZMWEnum();

  public abstract EnumGroups getZMWMetricsEnum();

  public abstract String getSuffix();
}
