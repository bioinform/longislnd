package com.bina.hdf5.simulator;

import com.bina.hdf5.simulator.pool.EmptyBCPool;
import com.bina.hdf5.simulator.pool.GeneralBCPool;
import com.bina.hdf5.simulator.pool.SingleBCPool;

/**
 * Created by bayo on 5/8/15.
 */
public enum EnumEvent {
    INSERTION(0,"i", GeneralBCPool.class),
    DELETION(1,"d", EmptyBCPool.class),
    SUBSTITUTION(2,"s", SingleBCPool.class),
    MATCH(3,"m", SingleBCPool.class);
    static private EnumEvent[] value2enum_ ={INSERTION,DELETION,SUBSTITUTION,MATCH};

    private int value_;
    private String description_;
    private Class<?> pool_;

    public static EnumEvent value2enum(int i) {
        return value2enum_[i];
    }

    EnumEvent(int value,String d, Class<?> pool) {
        value_ = value;
        description_ = d;
        pool_ = pool;
    }

    public int value() {
        return value_;
    }

    public String toString() {
        return description_;
    }

    public Class<?> pool() {
        return pool_;
    }


}
