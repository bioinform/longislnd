package com.bina.lrsim.h5.bax;

/**
 * Created by bayo on 5/3/15.
 */
enum EnumGroups {
    PulseData("/PulseData"),
    BaseCalls("/PulseData/BaseCalls"),
    ZMW("/PulseData/BaseCalls/ZMW"),
    ZMWMetrics("/PulseData/BaseCalls/ZMWMetrics"),
    ScanData("/ScanData"),
    AcqParams("/ScanData/AcqParams"),
    DyeSet("/ScanData/DyeSet"),
    A0("/ScanData/DyeSet/Analog[0]"),
    A1("/ScanData/DyeSet/Analog[1]"),
    A2("/ScanData/DyeSet/Analog[2]"),
    A3("/ScanData/DyeSet/Analog[3]"),
    RunInfo("/ScanData/RunInfo");

    EnumGroups(String path) {
        path_ = path;
    }

    String path() {
        return path_;
    }

    private final String path_;
}
