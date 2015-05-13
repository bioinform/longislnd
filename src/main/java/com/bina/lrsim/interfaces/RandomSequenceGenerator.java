package com.bina.lrsim.interfaces;

import com.bina.lrsim.bioinfo.KmerContext;

import java.util.Iterator;
import java.util.Random;

/**
 * Created by bayo on 5/11/15.
 */
public interface RandomSequenceGenerator {
    Iterator<Context> getSequence(int length, int left_flank, int right_flank, Random gen);
}
