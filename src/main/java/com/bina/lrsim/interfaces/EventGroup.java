package com.bina.lrsim.interfaces;

import java.util.Iterator;

import com.bina.lrsim.pb.EnumDat;
import com.bina.lrsim.pb.Spec;
import com.bina.lrsim.simulator.Event;

/**
 * Created by bayo on 5/8/15.
 */
public interface EventGroup {
  /**
   * Return an iterator over sequencing events
   * 
   * @param leftFlank associate an event by this number of bp preceeding a position of interest
   * @param rightFlank associate an event by this number of bp after a position of interest
   * @param leftMask the iterator range omits this number of events to begin with
   * @param rightMask the iterator range omits this number of events at the end
   * @param hpAnchor instead of left/right flank, use this number of bp on bothends of homopolymer event
   * @return an iterator over Events
   */
  Iterator<Event> iterator(int leftFlank, int rightFlank, int leftMask, int rightMask, int hpAnchor);

  int getSeqLength();

  int getRefLength();

  byte getData(EnumDat ed, int seqIdx);

  Spec getSpec();

  byte getRef(int index);

  byte getSeq(int index);

  int getSeqDataIndex(int index);

  int size();
}
