package com.bina.lrsim.interfaces;

import com.bina.lrsim.simulator.Event;

import java.util.Iterator;

/**
 * Created by bayo on 5/8/15.
 */
public interface EventGroup {
    Iterator<Event> getEventIterator(int left_flank, int right_flank, int left_mask, int right_mask, int hp_anchor);
    int seq_length();
    int ref_length();
}
