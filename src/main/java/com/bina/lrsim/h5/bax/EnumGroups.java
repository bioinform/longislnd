package com.bina.lrsim.h5.bax;

/**
 * Created by bayo on 5/3/15.
 */
public enum EnumGroups {
  PulseData("/PulseData"),

  BaseCalls("/PulseData/BaseCalls"),
  ZMW("/PulseData/BaseCalls/ZMW"),
  ZMWMetrics("/PulseData/BaseCalls/ZMWMetrics"),

  CBaseCalls("/PulseData/ConsensusBaseCalls"),
  CZMW("/PulseData/ConsensusBaseCalls/ZMW"),
  CZMWMetrics("/PulseData/ConsensusBaseCalls/ZMWMetrics"),
  CPasses("/PulseData/ConsensusBaseCalls/Passes"),

  ScanData("/ScanData"),
  AcqParams("/ScanData/AcqParams"),
  DyeSet("/ScanData/DyeSet"),
  A0("/ScanData/DyeSet/Analog[0]"),
  A1("/ScanData/DyeSet/Analog[1]"),
  A2("/ScanData/DyeSet/Analog[2]"),
  A3("/ScanData/DyeSet/Analog[3]"),
  RunInfo("/ScanData/RunInfo");

  EnumGroups(String path) {
    this.path = path;
  }

  public final String path;
}
