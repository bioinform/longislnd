package com.bina.lrsim.simulator.samples;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import com.bina.lrsim.bioinfo.KmerIntIntCounter;
import com.bina.lrsim.pb.Spec;
import com.bina.lrsim.simulator.samples.pool.AddBehavior;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.Pair;
import org.apache.log4j.Logger;

import com.bina.lrsim.bioinfo.Context;
import com.bina.lrsim.bioinfo.Heuristics;
import com.bina.lrsim.bioinfo.Kmerizer;
import com.bina.lrsim.pb.EnumDat;
import com.bina.lrsim.pb.PBReadBuffer;
import com.bina.lrsim.simulator.EnumEvent;
import com.bina.lrsim.simulator.Event;
import com.bina.lrsim.simulator.samples.pool.AppendState;
import com.bina.lrsim.simulator.samples.pool.BaseCallsPool;
import com.bina.lrsim.simulator.samples.pool.HPBCPool;

/**
 * Created by bayo on 5/10/15.
 * <p/>
 * Class for drawing sequence samples based on sequencing context
 */
public class SamplesDrawer extends Samples {
  private final static Logger log = Logger.getLogger(SamplesDrawer.class.getName());
  final private Map<EnumEvent, BaseCallsPool> kmerEventDrawer = new EnumMap<>(EnumEvent.class);
  private HPBCPool hpEventDrawer;
  private final Spec spec;
  private final long[] customFrequency;

  /**
   * Constructor
   * 
   * @param prefixes a list of prefixes storing sampled data
   * @param spec specification of data fields, etc
   * @param maxSample maximum number of samples stored in compressed way
   * @param customFrequency if not null, override per-kmer event frequency with this
   * @param lenLimits restrict length properties
   * @throws IOException
   */
  public SamplesDrawer(String[] prefixes, Spec spec, int maxSample, long[] customFrequency, boolean artificialCleanIns, LengthLimits lenLimits) throws IOException {
    this(prefixes[0], spec, 0/* must use 0 here */, customFrequency, artificialCleanIns, lenLimits);
    for (int ii = 1; ii < prefixes.length; ++ii) {
      accumulateStats(new SamplesDrawer(prefixes[ii], spec, 0/* must use 0 here */, customFrequency, artificialCleanIns, lenLimits));
    }
//    log.info(this.toString());
    allocateEventDrawer(spec, maxSample);
    loadEvents(prefixes, maxSample, artificialCleanIns);
    super.filterScoreLength(lenLimits);
  }

  /**
   * Private Constructor
   * WARNING: score/length filtering is not done here
   * 
   * @param prefix
   * @param prefix prefix of files storing sampled data
   * @param spec specification of data fields, etc
   * @param maxSample maximum number of samples stored in compressed way
   * @param customFrequency if not null, override per-kmer event frequency with this
   * @param lenLimits restrict length properties
   * @throws IOException
   */
  private SamplesDrawer(String prefix, Spec spec, int maxSample, long[] customFrequency, boolean artificialCleanIns, LengthLimits lenLimits) throws IOException {
    super(prefix);
    this.spec = spec;
    this.customFrequency = customFrequency;
    if (this.customFrequency != null) {
      log.info("using custom event frequencies");
    } else {
      log.info("using sampled event frequencies");
    }
    log.info("loaded bulk statistics from " + prefix);
    allocateEventDrawer(spec, maxSample);
    loadEvents(prefix, maxSample, artificialCleanIns);
//    super.filterScoreLength(lenLimits);
  }

  private AddBehavior calculateAddBehavior(long[] customFrequency) {
    if (null == customFrequency) { return new AddBehavior(0, 0, Integer.MAX_VALUE); }
    double custom = 0;
    for (long entry : customFrequency)
      custom += entry;
    if (customFrequency[EnumEvent.MATCH.ordinal()] == custom) { return new AddBehavior(0, 0, Integer.MAX_VALUE); }
    custom = 1.0 - customFrequency[EnumEvent.MATCH.ordinal()] / custom;
    final int customQ = (int) (-10 * Math.log10(custom) + 0.5);

    double intrinsic = 0;
    for (long entry : super.getEventBaseCountRef())
      intrinsic += entry;
    if (super.getEventBaseCountRef()[EnumEvent.MATCH.ordinal()] == intrinsic) { return new AddBehavior(0, 0, Integer.MAX_VALUE); }
    intrinsic = 1.0 - super.getEventBaseCountRef()[EnumEvent.MATCH.ordinal()] / intrinsic;
    final int intrinsicQ = (int) (-10 * Math.log10(intrinsic) + 0.5);

    final int deltaQ = (int) (10 * Math.log10(intrinsic / custom) + 0.5);

    return new AddBehavior(deltaQ, 0, Math.max(customQ, intrinsicQ));
  }

  private void allocateEventDrawer(Spec spec, int maxSample) {
    hpEventDrawer = new HPBCPool(spec, 1 << (2 * (1 + 2 * getHpAnchor())), -1);
    for (EnumEvent event : EnumSet.allOf(EnumEvent.class)) {
      // final int cap = (event.equals(EnumEvent.MATCH) ) ? max_sample : -1;
      final int cap = maxSample;
      try {
        kmerEventDrawer.put(event, (BaseCallsPool) event.pool.getDeclaredConstructor(new Class[] {Spec.class, int.class, int.class}).newInstance(spec, getNumKmer(), cap));
      } catch (ReflectiveOperationException e) {
        log.info(e, e);
      }
    }
  }

  /**
   * Returns a read length/score drawn from sampled data
   *
   * @param gen random number generator
   * @return read length and region score
   */
  public final Pair<int[], Integer> getRandomLengthScore(RandomGenerator gen) {
    final int index = gen.nextInt(getLengthSize());
    return new Pair<>(getLength(index), getScore(index));
  }

  /**
   * For the given sequencing context, append to buffer a sequence drawn from the sampled data
   *
   * @param buffer visitor to take the randomly generated sequence
   * @param context sequencing context
   * @param gen random number generator
   * @param baseCounter base counter for ins/del/sub/mat
   * @return about current state
   */
  public AppendState appendTo(PBReadBuffer buffer, Context context, AppendState deletion, RandomGenerator gen, long[] baseCounter) {
    final int oldLength = buffer.size();
    if (context.getHpLen() == 1) {
      EnumEvent ev = randomEvent(context, gen);
      // ignore the previous event if it's already a deletion
      final int orgLength = buffer.size();
      AppendState result = kmerEventDrawer.get(ev).appendTo(buffer, context, ev.equals(EnumEvent.DELETION) ? null : deletion, gen);

      final boolean badLength;
      switch(ev) {
        case DELETION:
          badLength = buffer.size() != orgLength;
          break;
        case INSERTION:
          badLength = buffer.size() <= orgLength + 1;
          break;
        case MATCH:
        case SUBSTITUTION:
          badLength = buffer.size() != orgLength + 1;
          if (buffer.size() != orgLength + 1) {
            throw new RuntimeException("length increased by " + (buffer.size() - orgLength) + " for " + ev.name());
          }
          break;
        default:
          badLength = false;
      }
      if (badLength) {
        throw new RuntimeException("length increased by " + (buffer.size() - orgLength) + " for " + ev.name());
      }

      ++baseCounter[ev.ordinal()];
      if (ev == EnumEvent.INSERTION) {
        baseCounter[ev.ordinal()] += buffer.size() - oldLength - 2;
      }
      if (!result.success) { throw new RuntimeException("kmer draw"); }
      // return a signal for deletion event
      return (ev == EnumEvent.DELETION && result.lastEvent != null && result.lastEvent.length > 0) ? result : null;
    } else {
      // do not do full hp if custom frequency is provided
      // custom hp drawing is possible improvement
      final boolean hpSampling;
      if (customFrequency == null) {
        final AppendState result = hpEventDrawer.appendTo(buffer, context, deletion, gen);
        hpSampling = result.success;
      } else {
        // might want to do something with remodeling homopolymer indels -- uniform error does not work
        hpSampling = false;
      }
      if (hpSampling) {
        final int differential = buffer.size() - oldLength - context.getHpLen();
        if (differential == 0) {
          baseCounter[EnumEvent.MATCH.ordinal()] += context.getHpLen();
          // log.info("homo match");
        } else if (differential < 0) {
          baseCounter[EnumEvent.DELETION.ordinal()] += -differential;
          // log.info("homo del");

        } else {
          baseCounter[EnumEvent.MATCH.ordinal()] += context.getHpLen() - 1;
          baseCounter[EnumEvent.INSERTION.ordinal()] += differential;
          // log.info("homo ins");
        }
        return null; // homopolyer does not pash deletion tag
      } else {
        int count = 0;
        // decompose extended kmer to kmer
        for (Iterator<Context> itr = context.decompose(getLeftFlank(), getRightFlank()); itr.hasNext();) {
          deletion = this.appendTo(buffer, itr.next(), deletion, gen, baseCounter);
          ++count;
        }
        if (count != context.getHpLen()) {
          log.info(count + " vs " + context.getHpLen());
          throw new RuntimeException("bad decomposition");
        }
        return deletion;
      }
    }
  }

  /**
   * load events from a single file
   *
   * @param prefix file prefix
   * @param maxSample
   * @throws IOException
   */
  private void loadEvents(String prefix, int maxSample, boolean artificialCleanIns) throws IOException {
    loadEvents(new String[] {prefix}, maxSample, artificialCleanIns);
  }

  /**
   * Load the sampled events
   *
   * @param prefixes prefixes of the event files
   * @throws IOException
   */
  private void loadEvents(String[] prefixes, int maxSample, boolean artificialCleanIns) throws IOException {
    final int numSrc = prefixes.length;
    for (Suffixes suf : EnumSet.of(Suffixes.EVENTS, Suffixes.HP)) {
      DataInputStream[] dis = new DataInputStream[numSrc];
      for (int ii = 0; ii < numSrc; ++ii) {
        dis[ii] = new DataInputStream(new BufferedInputStream(new FileInputStream(suf.filename(prefixes[ii])), 1000000));
      }
      loadEvents(dis, maxSample, artificialCleanIns);
      for (int ii = 0; ii < numSrc; ++ii) {
        dis[ii].close();
      }
    }
  }

  /**
   * Load the sampled events
   *
   * @param dis a list of datastream
   * @throws IOException
   */
  private void loadEvents(DataInputStream[] dis, int maxSample, boolean artificialCleanIns) throws IOException {
    // Bottleneck code
    AddBehavior ab = calculateAddBehavior(this.customFrequency);
    if (maxSample < 1) return;
    log.info("loading events");
    final int numSrc = dis.length;
    Event buffer = new Event(spec);
    long count = 0;

    long[] eventCount = new long[EnumEvent.values.length];
    long[] loggedEventCount = new long[EnumEvent.values.length];
    // long num_logged_event = 0;
    // final long max_logged_event = EnumEvent.num_logged_events() * getNumKmer() * (long) max_sample;
    KmerIntIntCounter numLoggedEvents = new KmerIntIntCounter(getK(), EnumEvent.values.length, 1);
    final int minEventIndex = EnumEvent.SUBSTITUTION.ordinal(); // assumes substitution is the rarest events
    final long[] minEventThreshold = new long[getNumKmer()]; // -1 for done, 0 or 1 for not done
    for (int kk = 0; kk < getNumKmer(); ++kk) {
      minEventThreshold[kk] = getKmerEventCountRef()[kk * EnumEvent.values.length + minEventIndex] == 0 ? 0 : 1;
    }
    int nKmerDone = 0;

    long numHpEvents = 0;

    long rawIns = 0;
    long modIns = 0;

    final boolean[] srcDone = new boolean[numSrc];

    for (int src = 0, nSrcDone = 0; nSrcDone < numSrc && nKmerDone < getNumKmer() /* && num_logged_event < max_logged_event */; src = (src + 1) % numSrc) {
      if (dis[src].available() > 0) {
        buffer.read(dis[src]);
        final int bufferKmer = buffer.getKmer();

        if (buffer.getHpLen() == 1) {
          final EnumEvent bufferEvent = buffer.getEvent();
          if (buffer.size() > 1 && bufferEvent != EnumEvent.INSERTION) {
            throw new RuntimeException(bufferEvent.name() + " with length " + buffer.size());
          }

          final byte centerBase = Kmerizer.getKmerByte(bufferKmer, getLeftFlank() + 1 + getRightFlank(), getLeftFlank());
          switch(bufferEvent) {
            case DELETION:
              break;
            case SUBSTITUTION:
              if (centerBase == buffer.get(0, EnumDat.BaseCall)) {
                throw new RuntimeException("matching base for " + bufferEvent.name() + " event");
              }
              break;
            case MATCH:
              if (centerBase != buffer.get(0, EnumDat.BaseCall)) {
                throw new RuntimeException("mismatching base for " + bufferEvent.name() + " event");
              }
              break;
            case INSERTION:
              if (artificialCleanIns) {
                final byte nextBase = Kmerizer.getKmerByte(bufferKmer, getLeftFlank() + 1 + getRightFlank(), getLeftFlank() + 1);
                final int midPoint = (buffer.size() + 1) / 2;
                final byte midPointBase = buffer.get(midPoint, EnumDat.BaseCall);
                final boolean midPointDifferent = midPointBase != centerBase && midPointBase != nextBase;
                boolean changed = false;
                for (int index = 0; index < buffer.size(); ++index) {
                  final byte base = buffer.get(index, EnumDat.BaseCall);
                  if (base != centerBase && base != nextBase) {
                    buffer.set(index, EnumDat.BaseCall, centerBase);
                    changed = true;
                  }
                }
                ++rawIns;
                if (buffer.size() % 2 == 0 && midPointDifferent) {
                  buffer.set(midPoint, EnumDat.BaseCall, ThreadLocalRandom.current().nextBoolean() ? centerBase : nextBase);
                  changed = true;
                }
                if (changed) {
                  ++modIns;
                  // log.info("bayo changed: " + new String(kmer_sequence) + " " + buffer.toString());
                }
              }
              if (buffer.size() - 1 > Heuristics.MAX_INS_LENGTH) {
                int hpLength = 1;
                final byte[] kmerSequence = Kmerizer.toByteArray(bufferKmer, getLeftFlank() + 1 + getRightFlank());
                for (int pos = getLeftFlank() + 1; pos < kmerSequence.length && kmerSequence[pos] == centerBase; ++pos, ++hpLength) {
                }
                for (int pos = getLeftFlank() - 1; pos >= 0 && kmerSequence[pos] == centerBase; --pos, ++hpLength) {
                }
                if (hpLength <= Math.min(getLeftFlank(), getRightFlank())) {
                  buffer.resize(Heuristics.MAX_INS_LENGTH + 1);
                }
              }
          }

          ++eventCount[bufferEvent.ordinal()];
          if (kmerEventDrawer.get(bufferEvent).add(buffer, ab)) {
            final int eventIndex = bufferEvent.ordinal();
            ++loggedEventCount[eventIndex];
            numLoggedEvents.increment(bufferKmer, eventIndex, 0);
            if (minEventThreshold[bufferKmer] >= 0 && numLoggedEvents.get(bufferKmer, minEventIndex, 0) >= minEventThreshold[bufferKmer]) {
              boolean done = true;
              long sum = 0;
              long sumPossible = 0;
              for (EnumEvent event : EnumEvent.values) {
                final long loc = numLoggedEvents.get(bufferKmer, event.ordinal(), 0);
                final long loc_logged_total = getKmerEventCountRef()[bufferKmer * EnumEvent.values.length + event.ordinal()];
                final long loc_possible = Math.max(loc_logged_total / event.recordEvery, loc_logged_total > 0 ? 1 : 0);
                done = done && (loc > 0 || loc_possible == 0);
                sum += loc;
                sumPossible += loc_possible;
              }
              done = done && (sum >= maxSample || sum >= sumPossible);
              if (done) {
                ++nKmerDone;
                minEventThreshold[bufferKmer] = -1;
              }
            }
            // ++num_logged_event;
          }
        } else {
          if (Heuristics.DISCARD_DIRTY_HOMOPOLYMER_SAMPLES) {
            final byte centerBase = Kmerizer.getKmerByte(buffer.getKmer(), 2 * getHpAnchor() + 1, getHpAnchor());
            int match = 0;
            for (int pos = 0; pos < buffer.size(); ++pos) {
              if (buffer.get(pos, EnumDat.BaseCall) == centerBase) {
                ++match;
              }
            }
            if (2 * match >= buffer.size()) {
              hpEventDrawer.add(buffer, ab);
              ++numHpEvents;
            }
          } else {
            hpEventDrawer.add(buffer, ab);
            ++numHpEvents;
          }
        }
        ++count;
        if (count % 10000000 == 1) {
          log.info("loaded " + count + " events" + Arrays.toString(loggedEventCount) + "/" + Arrays.toString(eventCount));
          log.info("loaded " + numHpEvents + " hp events");
          log.info("pruning criterions " + nKmerDone + "/" + getNumKmer());
        }
      } else if (!srcDone[src]) {
        srcDone[src] = true;
        ++nSrcDone;
      }
    }
    if(nKmerDone >= getNumKmer()) {
      log.info("load has been pruned");
    }
    log.info("loaded " + count + " events");
    log.info("loaded " + numHpEvents + " hp events");
    log.info("modified ins: " + modIns + "/" + rawIns);
  }

  /**
   * For a given sequencing context, generate an event
   *
   * @param context sequencing context
   * @param gen random number generator
   * @return an event type
   */
  private EnumEvent randomEvent(Context context, RandomGenerator gen) {
    if (context.getHpLen() == 1) {
      final int shift = EnumEvent.values.length * context.getKmer();

      final long[] frequencies = Arrays.copyOfRange(getKmerEventCountRef(), shift, shift + EnumEvent.values.length);
      if (null != customFrequency) {
        for (int ii = 0; ii < EnumEvent.values.length; ++ii) {
          if (frequencies[ii] > Heuristics.MIN_KMER_SAMPLES_FOR_NON_ZERO_CUSTOM_FREQUENCY) {
            frequencies[ii] = customFrequency[ii];
          } else {
            frequencies[ii] = 0;
            // log.info("warning: not enough samples for custom sampling frequency. considering more training data or shorter sampling flank to discover such rare event");
          }
        }
      }

      long sum = 0;
      for (int ii = 0; ii < EnumEvent.values.length; ++ii) {
        sum += frequencies[ii];
      }
      final double p = gen.nextDouble();
      if (p < 0 || p > 1) { throw new RuntimeException("bad p=" + p); }
      double cdf = 0;
      for (int ii = 0; ii < EnumEvent.values.length; ++ii) {
        cdf += (double) (frequencies[ii]) / (double) (sum);
        if (p <= cdf) return EnumEvent.values[ii];
      }
      return EnumEvent.MATCH;
    } else {
      throw new UnsupportedOperationException("hp code path not implemented");
    }
  }

}
