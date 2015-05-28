package com.bina.lrsim.interfaces;

import java.util.Iterator;

import com.bina.lrsim.h5.bax.Region;

/**
 * Created by bayo on 5/27/15.
 */
public interface RegionGroup {
  Iterator<Region> getRegionIterator();
}
