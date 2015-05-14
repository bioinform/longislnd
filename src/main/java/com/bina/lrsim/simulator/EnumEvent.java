package com.bina.lrsim.simulator;

import com.bina.lrsim.simulator.samples.pool.EmptyBCPool;
import com.bina.lrsim.simulator.samples.pool.GeneralBCPool;
import com.bina.lrsim.simulator.samples.pool.SingleBCPool;

import java.util.EnumSet;

/**
 * Created by bayo on 5/8/15.
 * <p/>
 * Set of editing events relative to a given reference
 * Also associate memory-efficient implementations of storing those samples
 */
public enum EnumEvent {
    //           value, description, recording period, sample pool implementation
    INSERTION   (0,     "i",         1,                GeneralBCPool.class), // ~10% insertion, but can be multi-bp, so let's suck it up for now
    DELETION    (1,     "d",         -1,               EmptyBCPool.class),   // skip all deletion samples, emptybcpool has no memory overhead
    SUBSTITUTION(2,     "s",         1,                GeneralBCPool.class), // ~1% mismatch so memory is not too bad
    MATCH       (3,     "m",         10,               SingleBCPool.class);  // 85% matches, down-sample 10-fold, must use SingleBCPoolCompression
    static private EnumEvent[] value2enum_ = {INSERTION, DELETION, SUBSTITUTION, MATCH};

    private int value_;
    private String description_;
    private int record_every_;
    private Class<?> pool_;

    public static EnumEvent value2enum(int i) {
        return value2enum_[i];
    }

    EnumEvent(int value, String d, int record_period, Class<?> pool) {
        value_ = value;
        description_ = d;
        record_every_ = record_period;
        pool_ = pool;
    }

    static public int num_logged_events() {
        int out = 0;
        for (EnumEvent ev : EnumSet.allOf(EnumEvent.class)) {
            if (ev.record_every() > 0) {
                ++out;
            }
        }
        return out;
    }

    public int value() {
        return value_;
    }

    public String toString() {
        return description_;
    }

    public int record_every() {
        return record_every_;
    }

    public Class<?> pool() {
        return pool_;
    }

    static public String getPrettyStats(long[] data) {
        if (data.length != EnumEvent.values().length) {
            return "invalid data length";
        }
        StringBuilder sb = new StringBuilder();
        long sum = 0;
        for (long entry : data) {
            sum += entry;
        }
        for (EnumEvent ev : EnumSet.allOf(EnumEvent.class)) {
            long count = data[ev.value()];
            sb.append(" " + ev.toString() + " " + count + String.format(" (%5.1f)", 100 * (double) (count) / sum));
        }
        return sb.toString();
    }
}
