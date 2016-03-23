package com.bina.lrsim.pb.h5.bax;

/**
 * Created by bayo on 6/2/15.
 */
public enum EnumHoleStatus {
  SEQUENCING, ANTIHOLE, FIDUCIAL, SUSPECT, ANTIMIRROR, FDZMW, FBZMW, ANTIBEAMLET, OUTSIDEFOV;

  public static final String[] names;

  static {
    names = new String[values().length];
    int i = 0;
    for (EnumHoleStatus value : values()) {
      names[i] = value.name();
      i++;
    }
  }
}
