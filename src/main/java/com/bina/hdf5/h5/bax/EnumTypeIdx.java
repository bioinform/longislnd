package com.bina.hdf5.h5.bax;

/**
 * Created by bayo on 5/6/15.
 */
enum EnumTypeIdx {
    TypeAdapter (0,"Adapter"),
    TypeInsert(1, "Insert"),
    TypeHQRegion(2, "HQRegion");

    public int value() {
        return value_;
    }

    public String description() {
        return description_;
    }

    EnumTypeIdx(int value, String description) {
        value_ = value;
        description_ = description;
    }

    private final String description_;
    private final int value_;
}
