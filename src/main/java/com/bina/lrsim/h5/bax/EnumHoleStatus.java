package com.bina.lrsim.h5.bax;

/**
 * Created by bayo on 6/2/15.
 */
public enum EnumHoleStatus {
  SEQUENCING(0), ANTIHOLE(1), FIDUCIAL(2), SUSPECT(3), ANTIMIRROR(4), FDZMW(5), FBZMW(6), ANTIBEAMLET(7), OUTSIDEFOV(8);

  public final byte value;

  EnumHoleStatus(int value) {
    this.value = (byte) value;
  }
}
