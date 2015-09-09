package com.bina.lrsim.interfaces;

import com.bina.lrsim.bioinfo.Fragment;
import org.apache.commons.math3.random.RandomGenerator;

/**
 * Created by bayo on 5/11/15.
 */
public interface RandomFragmentGenerator {
  Fragment getFragment(int length, RandomGenerator gen);
}
