package com.bina.lrsim.interfaces;

import com.bina.lrsim.bioinfo.Context;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Iterator;

/**
 * Created by bayo on 5/11/15.
 */
public interface RandomSequenceGenerator {
    Iterator<Context> getSequence(int length, int left_flank, int right_flank, int hp_anchor, RandomGenerator gen);
}
