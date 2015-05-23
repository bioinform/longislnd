package com.bina.lrsim.simulator.samples;

import com.bina.lrsim.bioinfo.Context;
import com.bina.lrsim.h5.pb.PBReadBuffer;
import com.bina.lrsim.simulator.EnumEvent;
import com.bina.lrsim.simulator.Event;
import com.bina.lrsim.simulator.samples.pool.BaseCallsPool;
import com.bina.lrsim.simulator.samples.pool.HPBCPool;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Iterator;

/**
 * Created by bayo on 5/10/15.
 * <p/>
 * Class for drawing sequence samples based on sequencing context
 */
public class SamplesDrawer extends Samples {
  private final static Logger log = Logger.getLogger(SamplesDrawer.class.getName());
  final EnumMap<EnumEvent, BaseCallsPool> kmer_event_drawer_ = new EnumMap<EnumEvent, BaseCallsPool>(EnumEvent.class);
  HPBCPool hp_event_drawer_;

  public SamplesDrawer(String[] prefixes, int max_sample) throws IOException {
    this(prefixes[0], 0/* must use 0 here */);
    for (int ii = 1; ii < prefixes.length; ++ii) {
      accumulateStats(new SamplesDrawer(prefixes[ii], 0/* must use 0 here */));
    }
    allocateEventDrawer(max_sample);
    loadEvents(prefixes, max_sample);
  }

  /**
   * Constructor
   * 
   * @param prefix prefix of files storing sampled data
   * @param max_sample limit the number of samples per sequencing context
   * @throws IOException
   */
  public SamplesDrawer(String prefix, int max_sample) throws IOException {
    super(prefix);
    log.info("loaded bulk statistics from " + prefix);
    allocateEventDrawer(max_sample);
    loadEvents(prefix, max_sample);
  }

  private void allocateEventDrawer(int max_sample) {
    hp_event_drawer_ = new HPBCPool(1 << (2 * (1 + 2 * hp_anchor())), -1);
    for (EnumEvent event : EnumSet.allOf(EnumEvent.class)) {
      // final int cap = (event.equals(EnumEvent.MATCH) ) ? max_sample : -1;
      final int cap = max_sample;
      try {
        kmer_event_drawer_.put(event,
                               (BaseCallsPool) event.pool.getDeclaredConstructor(new Class[] {int.class, int.class})
                                                         .newInstance(num_kmer(), cap));
      } catch (ReflectiveOperationException e) {
        log.info(e, e);
      }
    }
  }

  /**
   * Returns a read length drawn from sampled data
   *
   * @param gen random number generator
   * @return read length
   */
  public int drawLength(RandomGenerator gen) {
    return lengths_ref().get(gen.nextInt(lengths_ref().size()));
  }

  /**
   * For the given sequencing context, append to buffer a sequence drawn from the sampled data
   *
   * @param buffer visitor to take the randomly generated sequence
   * @param context sequencing context
   * @param gen random number generator
   * @return an array whose EnumEvent.value() element is the number of simulated bp of that event
   */
  public long[] appendTo(PBReadBuffer buffer, Context context, RandomGenerator gen) {
    long[] counters = new long[EnumEvent.values().length];
    final int old_length = buffer.size();
    if (context.hp_len() == 1) {
      EnumEvent ev = randomEvent(context, gen);
      kmer_event_drawer_.get(ev).appendTo(buffer, context, gen);
      ++counters[ev.value];
      if (ev.equals(EnumEvent.INSERTION)) {
        counters[ev.value] += buffer.size() - old_length - 2;
      }
    } else {
      if (hp_event_drawer_.appendTo(buffer, context, gen)) {
        final int differential = buffer.size() - old_length - context.hp_len();
        if (differential == 0) {
          counters[EnumEvent.MATCH.value] += context.hp_len();
          // log.info("homo match");
        } else if (differential < 0) {
          counters[EnumEvent.DELETION.value] += -differential;
          // log.info("homo del");

        } else {
          counters[EnumEvent.MATCH.value] += context.hp_len() - 1;
          counters[EnumEvent.INSERTION.value] += differential;
          // log.info("homo ins");

        }
      } else {
        int count = 0;
        for (Iterator<Context> itr = context.decompose(left_flank(), right_flank()); itr.hasNext();) {
          long[] tmp = this.appendTo(buffer, itr.next(), gen);
          for (int ii = 0; ii < tmp.length; ++ii) {
            counters[ii] += tmp[ii];
          }
          ++count;
        }
        if (count != context.hp_len()) {
          log.info(count + " vs " + context.hp_len());
          throw new RuntimeException("bad decomposition");

        }
      }
    }
    return counters;
  }

  /**
   * load events from a single file
   *
   * @param prefix file prefix
   * @param max_sample
   * @throws IOException
   */
  private void loadEvents(String prefix, int max_sample) throws IOException {
    loadEvents(new String[] {prefix}, max_sample);
  }

  /**
   * Load the sampled events
   *
   * @param prefixes prefixes of the event files
   * @throws IOException
   */
  private void loadEvents(String[] prefixes, int max_sample) throws IOException {
    log.info("loading events");
    if (max_sample < 1) return;
    final int num_src = prefixes.length;
    DataInputStream[] dis = new DataInputStream[num_src];
    for (int ii = 0; ii < num_src; ++ii) {
      dis[ii] = new DataInputStream(new BufferedInputStream(new FileInputStream(Suffixes.EVENTS.filename(prefixes[ii])),
                                                            1000000));
    }
    Event buffer = new Event();
    long count = 0;

    long[] event_count = new long[EnumEvent.values().length];
    long[] logged_event_count = new long[EnumEvent.values().length];
//    long num_logged_event = 0;
//    final long max_logged_event = EnumEvent.num_logged_events() * num_kmer() * (long) max_sample;

    long num_hp_events = 0;

    final boolean[] src_done = new boolean[num_src];

    for (int src = 0, n_src_done = 0; n_src_done < num_src /*&& num_logged_event < max_logged_event*/; src = (src + 1) % num_src) {
      if (dis[src].available() > 0) {
        buffer.read(dis[src]);

        if (buffer.hp_len() == 1) {
          /*
           * if (buffer.event().equals(EnumEvent.DELETION) && buffer.size() != 0) throw new
           * Exception("del with length " + buffer.size()); else if
           * (buffer.event().equals(EnumEvent.SUBSTITUTION) ) { if(buffer.size() != 1) throw new
           * Exception("sub with length " + buffer.size());
           * if(Kmerizer.toByteArray(buffer.kmer(),left_flank
           * ()+1+right_flank())[left_flank()]==buffer.get(0, EnumDat.BaseCall)) { throw new
           * Exception("matching base for substitution event"); } } else if
           * (buffer.event().equals(EnumEvent.MATCH) ) { if( buffer.size() != 1) throw new
           * Exception("match with length " + buffer.size());
           * if(Kmerizer.toByteArray(buffer.kmer(),left_flank
           * ()+1+right_flank())[left_flank()]!=buffer.get(0, EnumDat.BaseCall)) { throw new
           * Exception("unmatching base for match event"); } }
           */

          ++event_count[buffer.event().value];
          if (kmer_event_drawer_.get(buffer.event()).add(buffer)) {
            ++logged_event_count[buffer.event().value];
//            ++num_logged_event;
          }
        } else {
          hp_event_drawer_.add(buffer);
          ++num_hp_events;
        }
        ++count;
        if (count % 10000000 == 1) {
          log.info("loaded " + count
                   + " events"
                   + Arrays.toString(logged_event_count)
                   + "/"
                   + Arrays.toString(event_count));
          log.info("loaded " + num_hp_events + " hp events");
        }

      } else if (!src_done[src]) {
        src_done[src] = true;
        ++n_src_done;
      }
    }

    log.info("loaded " + count + " events");
    log.info("loaded " + num_hp_events + " hp events");
    for (int ii = 0; ii < num_src; ++ii) {
      dis[ii].close();
    }
  }

  /**
   * For a given sequencing context, generate an event
   *
   * @param context sequencing context
   * @param gen random number generator
   * @return
   */
  private EnumEvent randomEvent(Context context, RandomGenerator gen) {
    if (context.hp_len() == 1) {
      final int shift = EnumEvent.values().length * context.kmer();
      long sum = 0;
      for (int ii = 0; ii < EnumEvent.values().length; ++ii) {
        sum += kmer_event_count_ref()[shift + ii];
      }
      final double p = gen.nextDouble();
      if (p < 0 || p > 1) {
        throw new RuntimeException("bad p=" + p);
      }
      double cdf = 0;
      for (int ii = 0; ii < EnumEvent.values().length; ++ii) {
        cdf += (double) (kmer_event_count_ref()[shift + ii]) / (sum);
        if (p <= cdf) return EnumEvent.value2enum(ii);
      }
      return EnumEvent.MATCH;
    } else {
      throw new UnsupportedOperationException("hp code path not implemented");
    }
  }

}
