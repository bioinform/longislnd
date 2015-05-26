package com.bina.lrsim.h5.pb;

import java.util.EnumSet;

/**
 * Created by bayo on 5/25/15.
 */
public abstract class PBSpec {

  public abstract EnumSet<EnumDat> getSet();

  public abstract EnumSet<EnumDat> getNonBaseSet();

  public abstract String[] getContentDescription();
}
