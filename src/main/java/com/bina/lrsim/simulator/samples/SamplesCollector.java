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
  private final String outPrefix_;
  private final DataOutputStream eventOut_;
  private final DataOutputStream hpOut_;

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
  public SamplesCollector(String outPrefix, int leftFlank, int rightFlank, int hp_anchor, boolean writeEvents) throws IOException {
    super(leftFlank, rightFlank, hp_anchor);
    outPrefix_ = outPrefix;
    eventOut_ = (writeEvents) ? new DataOutputStream(new BufferedOutputStream(new FileOutputStream(Suffixes.EVENTS.filename(outPrefix_)))) : null;
    hpOut_ = (writeEvents) ? new DataOutputStream(new BufferedOutputStream(new FileOutputStream(Suffixes.HP.filename(outPrefix_)))) : null;
    Arrays.fill(event_base_count_ref(), 0);
    Arrays.fill(event_count_ref(), 0);
    log.info("flanks=(" + left_flank() + "," + right_flank() + ") k=" + k() + " num_kmers=" + num_kmer());
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

      if (event.hp_len() == 1) {
        final long current_count = kmer_event_count_ref()[EnumEvent.values().length * event.kmer() + event.event().value];
        if (null!=eventOut_ && event.event().recordEvery > 0
                && current_count % event.event().recordEvery == 0 && current_count / event.event().recordEvery <= Heuristics.MAX_KMER_EVENT_SAMPLES) {
          event.write(eventOut_);
        }
        final int idx = event.event().value;

        ++event_count_ref()[idx];
        ++event_base_count_ref()[idx];
        if (event.event().equals(EnumEvent.INSERTION)) {
          event_base_count_ref()[idx] += event.size() - 2;
        }
        ++kmer_event_count_ref()[EnumEvent.values().length * event.kmer() + event.event().value];
      } else {
        if (event.hp_len() < max_rlen() && event.size() < max_slen()) {
          add_kmer_rlen_slen_count(event.kmer(), event.hp_len(), event.size());
          if (null != hpOut_) event.write(hpOut_);
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
  public void process(EventGroupFactory groups, int min_length, int flank_mask) throws IOException {
    int ii = 0;
    for (EventGroup group : groups) {
      if (ii % 10000 == 0 && ii != 0) {
        log.info("processing group " + ii);
      }
      if (null == group) {
        log.info("failed to retrieve group " + ii);
        continue;
      }
      if (group.seq_length() < min_length) {
        continue;
      }
      // super.lengths_ref().add(group.seq_length());
      process(group.iterator(left_flank(), right_flank(), flank_mask, flank_mask, hp_anchor()));
      ++ii;
    }
    log.info("processed " + ii + " groups");
  }

  /**
   * finish sampling and write to file
   */
  @Override
  public void close() throws IOException {
    if (null != eventOut_) {
      eventOut_.close();
    }
    if (null != hpOut_) {
      hpOut_.close();
    }
    writeSummary(outPrefix_);
    writeStats(outPrefix_);
    writeIdx(outPrefix_);
  }

}
