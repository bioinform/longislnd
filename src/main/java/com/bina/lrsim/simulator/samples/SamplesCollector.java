package com.bina.lrsim.simulator.samples;

import java.io.*;
import java.util.Arrays;
import java.util.Iterator;

import com.bina.lrsim.bioinfo.Heuristics;
import org.apache.log4j.Logger;

import com.bina.lrsim.interfaces.EventGroup;
import com.bina.lrsim.interfaces.EventGroupFactory;
import com.bina.lrsim.simulator.EnumEvent;
import com.bina.lrsim.simulator.Event;

/**
 * Created by bayo on 5/8/15.
 * <p/>
 * Class for going through, eg, alignment data to collect samples of sequence context-based output
 */
public class SamplesCollector extends Samples implements Closeable, com.bina.lrsim.interfaces.EventGroupsProcessor {
  private final static Logger log = Logger.getLogger(SamplesCollector.class.getName());
  private final String outPrefix;
  private final DataOutputStream eventOut;
  private final DataOutputStream hpOut;

  /**
   * Constructor
   * 
   * @param outPrefix prefix of output files
   * @param leftFlank number of bp preceding the base of interest in the construction of sequencing
   *        context
   * @param rightFlank number of bp after the base of interest in the construction of sequencing
   *        context
   * @throws IOException
   */
  public SamplesCollector(String outPrefix, int leftFlank, int rightFlank, int hpAnchor, boolean writeEvents) throws IOException {
    super(leftFlank, rightFlank, hpAnchor);
    this.outPrefix = outPrefix;
    eventOut = (writeEvents) ? new DataOutputStream(new BufferedOutputStream(new FileOutputStream(Suffixes.EVENTS.filename(this.outPrefix)))) : null;
    hpOut = (writeEvents) ? new DataOutputStream(new BufferedOutputStream(new FileOutputStream(Suffixes.HP.filename(this.outPrefix)))) : null;
    Arrays.fill(getEventBaseCountRef(), 0);
    Arrays.fill(getEventCountRef(), 0);
    log.info("flanks=(" + getLeftFlank() + "," + getRightFlank() + ") k=" + getK() + " num_kmers=" + getNumKmer());
  }

  /**
   * Collect events based on sequencing context
   *
   * @param itr iterator of a group of events, eg an alignment
   * @throws IOException
   */
  public void process(Iterator<Event> itr) throws IOException {
    while (itr.hasNext()) {
      Event event = itr.next();
      if (null == event) {
        continue;
      }

      if (event.getHpLen() == 1) {
        final long current_count = getKmerEventCountRef()[EnumEvent.values().length * event.getKmer() + event.getEvent().ordinal()];
        if (null!= eventOut && event.getEvent().recordEvery > 0
                && current_count % event.getEvent().recordEvery == 0 && current_count / event.getEvent().recordEvery <= Heuristics.MAX_KMER_EVENT_SAMPLES) {
          event.write(eventOut);
        }
        final int idx = event.getEvent().ordinal();

        ++getEventCountRef()[idx];
        ++getEventBaseCountRef()[idx];
        if (event.getEvent().equals(EnumEvent.INSERTION)) {
          getEventBaseCountRef()[idx] += event.size() - 2;
        }
        ++getKmerEventCountRef()[EnumEvent.values().length * event.getKmer() + event.getEvent().ordinal()];
      } else {
        if (event.getHpLen() < getMaxRlen() && event.size() < getMaxSlen()) {
          addKmerRlenSlenCount(event.getKmer(), event.getHpLen(), event.size());
          if (null != hpOut) event.write(hpOut);
        }
      }
    }
  }

  /**
   * Collect events based on sequencing context
   *
   * @param groups a collection of event groups, eg alignments
   * @throws IOException
   */
  @Override
  public void process(EventGroupFactory groups, int minLength, int flankMask) throws IOException {
    int ii = 0;
    for (EventGroup group : groups) {
      if (ii % 10000 == 0 && ii != 0) {
        log.info("processing group " + ii);
      }
      if (null == group) {
        log.info("failed to retrieve group " + ii);
        continue;
      }
      if (group.getSeqLength() < minLength) {
        continue;
      }
      // super.lengths_ref().add(group.getSeqLength());
      process(group.iterator(getLeftFlank(), getRightFlank(), flankMask, flankMask, getHpAnchor()));
      ++ii;
    }
    log.info("processed " + ii + " groups");
  }

  /**
   * finish sampling and write to file
   */
  @Override
  public void close() throws IOException {
    if (null != eventOut) {
      eventOut.close();
    }
    if (null != hpOut) {
      hpOut.close();
    }
    writeSummary(outPrefix);
    writeStats(outPrefix);
    writeIdx(outPrefix);
  }

}
