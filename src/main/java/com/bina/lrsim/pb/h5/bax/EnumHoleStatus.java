package com.bina.lrsim.pb.h5.bax;

/**
 * Created by bayo on 6/2/15.
 */
public enum EnumHoleStatus {
  SEQUENCING(0), ANTIHOLE(1), FIDUCIAL(2), SUSPECT(3), ANTIMIRROR(4), FDZMW(5), FBZMW(6), ANTIBEAMLET(7), OUTSIDEFOV(8);

  public final byte value;

  public static final String[] names;

  static {
    names = new String[values().length];
    int i = 0;
    for (EnumHoleStatus value : values()) {
      names[i] = value.name();
      i++;
    }
  }

  EnumHoleStatus(int value) {
    this.value = (byte) value;
  }
}
