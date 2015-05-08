package com.bina.hdf5.simulator;

/**
 * Created by bayo on 5/8/15.
 */
public enum EnumEvent {
    MATCH(0,"m"),
    INSERTION(1,"i"),
    DELETION(2,"d"),
    SUBSTITUTION(3,"s");
    static private EnumEvent[] value2enum_ ={MATCH,INSERTION,DELETION,SUBSTITUTION};

    private int value_;
    private String description_;

    public static EnumEvent value2enum(int i) {
        return value2enum_[i];
    }

    EnumEvent(int value,String d) {
        value_ = value;
        description_ = d;
    }

    public int value() {
        return value_;
    }

    public String toString() {
        return description_;
    }


}
