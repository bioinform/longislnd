package com.bina.hdf5.h5.bax;

/**
 * Created by bayo on 5/3/15.
 */
public enum EnumGroups {
    PulseData("/PulseData"),
    BaseCalls("/PulseData/BaseCalls"),
    ZMW("/PulseData/BaseCalls/ZMW"),
    ZMWMetrics("/PulseData/BaseCalls/ZMWMetrics"),
    ScanData("/ScanData"),
    AcqParams("/ScanData/AcqParams"),
    DyeSet("/ScanData/DyeSet"),
    RunInfo("/ScanData/RunInfo");

    EnumGroups(String path) {
        path_ = path;
    }

    String path() {
        return path_;
    }

    private final String path_;
}
