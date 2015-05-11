package com.bina.hdf5.interfaces;

import com.bina.hdf5.bioinfo.Context;

import java.util.Iterator;
import java.util.Random;

/**
 * Created by bayo on 5/11/15.
 */
public interface RandomSequenceGenerator {
    Iterator<Context> getSequence(int length, Random gen);
}
