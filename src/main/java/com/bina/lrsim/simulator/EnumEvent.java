package com.bina.lrsim.simulator;

import com.bina.lrsim.simulator.samples.pool.EmptyBCPool;
import com.bina.lrsim.simulator.samples.pool.GeneralBCPool;
import com.bina.lrsim.simulator.samples.pool.SingleBCPool;

/**
 * Created by bayo on 5/8/15.
 *
 * Set of editing events relative to a given reference
 * Also associate memory-efficient implementations of storing those samples
 */
public enum EnumEvent {
    INSERTION   (0,"i", GeneralBCPool.class), // ~10% insertion, but can be multi-bp, so let's suck it up for now
    DELETION    (1,"d", EmptyBCPool.class),   // emptybcpool has no memory overhead
    SUBSTITUTION(2,"s", GeneralBCPool.class), // ~1% mismatch so memory is not too bad
    MATCH       (3,"m", SingleBCPool.class);  // 85% matches, must use SingleBCPoolCompression
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
