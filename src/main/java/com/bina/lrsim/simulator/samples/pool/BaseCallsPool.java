package com.bina.lrsim.simulator.samples.pool;

import org.apache.commons.math3.random.RandomGenerator;

import com.bina.lrsim.bioinfo.Context;
import com.bina.lrsim.pb.EnumDat;
import com.bina.lrsim.pb.PBReadBuffer;
import com.bina.lrsim.pb.PBSpec;
import com.bina.lrsim.simulator.Event;

/**
 * Created by bayo on 5/10/15.
 */
public abstract class BaseCallsPool {


  protected static final int BYTE_PER_BC = EnumDat.numBytes;
  protected final int entryPerKmer_;
  protected final int numKmers_;
  protected final PBSpec spec;

  /**
   * constrcutor
   * 
   * @param numKmers number of kmer identifier, typically 1<<(2*num_base)
   * @param entryPerKmer a hint of maximum number of entries per kmer, <1 means some default behavior
   */
  protected BaseCallsPool(PBSpec spec, int numKmers, int entryPerKmer) {
    numKmers_ = numKmers;
    entryPerKmer_ = entryPerKmer;
    this.spec = spec;
  }

  /**
   * given a sequencing context, append some simulated base calls to the read buffer
   * 
   * @param pb read buffer
   * @param context sequencing context
   * @param gen random number generator
   * @return success or not
   */
  public abstract AppendState appendTo(PBReadBuffer pb, Context context, AppendState as, RandomGenerator gen);

  /**
   * add an event to be drawn from later
   * 
   * @param ev a sequencing event
   * @return event has been added to the pool or not
   */
  public abstract boolean add(Event ev, AddBehavior ab);
}
