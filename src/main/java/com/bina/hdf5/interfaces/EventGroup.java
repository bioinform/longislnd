package com.bina.hdf5.interfaces;

import com.bina.hdf5.simulator.Event;

import java.util.Iterator;

/**
 * Created by bayo on 5/8/15.
 */
public interface EventGroup {
    Iterator<Event> getEventIterator(int left_flank, int right_flank);
    int seq_length();
    int ref_length();
}
