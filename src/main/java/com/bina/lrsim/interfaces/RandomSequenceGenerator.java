package com.bina.lrsim.interfaces;

import org.apache.commons.math3.random.RandomGenerator;

/**
 * Created by bayo on 5/11/15.
 */
public interface RandomSequenceGenerator {
  byte[] getSequence(int length, RandomGenerator gen);
}
