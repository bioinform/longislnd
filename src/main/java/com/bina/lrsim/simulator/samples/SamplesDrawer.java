package com.bina.lrsim.simulator.samples;

import com.bina.lrsim.h5.pb.PBReadBuffer;
import com.bina.lrsim.simulator.EnumEvent;
import com.bina.lrsim.simulator.Event;
import com.bina.lrsim.simulator.samples.pool.BaseCallsPool;
import org.apache.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Random;

/**
 * Created by bayo on 5/10/15.
 *
 * Class for drawing sequence samples based on sequencing context
 */
public class SamplesDrawer extends Samples {
    private final static Logger log = Logger.getLogger(SamplesDrawer.class.getName());
    final EnumMap<EnumEvent,BaseCallsPool> event_drawer_ = new EnumMap<EnumEvent,BaseCallsPool> (EnumEvent.class);

    public SamplesDrawer(String[] prefixes, int max_sample) throws Exception {
        this(prefixes[0],0);
        for(int ii = 1; ii < prefixes.length; ++ii) {
            accumulateStats(new SamplesDrawer(prefixes[ii],0/*must use 0 here*/));
        }
        loadEvents(prefixes, max_sample);
    }

    /**
     * Constructor
     * @param prefix     prefix of files storing sampled data
     * @param max_sample limit the number of samples per sequencing context
     * @throws Exception
     */
    public SamplesDrawer(String prefix, int max_sample) throws Exception {
        super(prefix);
        log.info("loaded bulk statistics from "+prefix);
        for(EnumEvent event: EnumSet.allOf(EnumEvent.class)){
//            final int cap = (event.equals(EnumEvent.MATCH) ) ? max_sample : -1;
            final int cap = max_sample;
            try {
                event_drawer_.put(
                        event,
                        (BaseCallsPool) event.pool().getDeclaredConstructor(new Class[]{int.class, int.class})
                                                    .newInstance(super.numKmer_, cap));
            } catch (ReflectiveOperationException e) {
                log.info(e,e);
            }
        }
        loadEvents(prefix, max_sample);
    }

    /**
     * Returns a read length drawn from sampled data
     * @param gen random number generator
     * @return    read length
     */
    public int drawLength(Random gen) {
        return lengths_.get(gen.nextInt(lengths_.size()));
    }

    /**
     * For the given sequencing context, append to buffer a sequence drawn from the sampled data
     * @param buffer visitor to take the randomly generated sequence
     * @param kmer   sequencing context
     * @param gen    random number generator
     * @return       type of the randomly selected event
     * @throws Exception
     */
    public EnumEvent appendTo(PBReadBuffer buffer, int kmer, Random gen) throws Exception{
        EnumEvent ev = randomEvent(kmer,gen);
        event_drawer_.get(ev).appendTo(buffer, kmer, gen);
        return ev;
    }

    /**
     * load events from a single file
     * @param prefix file prefix
     * @param max_sample
     * @throws Exception
     */
    private void loadEvents(String prefix, int max_sample) throws Exception {
        loadEvents(new String[]{prefix},max_sample);
    }

    /**
     * Load the sampled events
     * @param prefixes      prefixes of the event files
     * @throws Exception
     */
    private void loadEvents(String[] prefixes, int max_sample) throws Exception {
        log.info("loading events");
        if( max_sample < 1) return;
        final int num_src = prefixes.length;
        DataInputStream[] dis = new DataInputStream[num_src];
        for(int ii = 0 ; ii < num_src ; ++ii) {
            dis[ii] = new DataInputStream(new BufferedInputStream(new FileInputStream(Suffixes.EVENTS.filename(prefixes[ii])),1000000)) ;
        }
        Event buffer = new Event();
        long count = 0;

        long[] event_count = new long[EnumEvent.values().length];
        long[] logged_event_count = new long[EnumEvent.values().length];
        long num_logged_event = 0;
        final long max_logged_event = EnumEvent.num_logged_events() * numKmer_ * (long)max_sample;

        final boolean[] src_done = new boolean[num_src];

        for(int src = 0, n_src_done = 0; n_src_done < num_src && num_logged_event < max_logged_event; src = (src + 1) % num_src) {
            if(dis[src].available()>0) {
                buffer.read(dis[src]);
                if(buffer.event().equals(EnumEvent.DELETION) && buffer.size() !=0)
                    throw new Exception("del with length " + buffer.size());
                else if(buffer.event().equals(EnumEvent.SUBSTITUTION) && buffer.size() !=1)
                    throw new Exception("sub with length " + buffer.size());
                else if(buffer.event().equals(EnumEvent.MATCH) && buffer.size() !=1)
                    throw new Exception("match with length " + buffer.size());
                ++event_count[buffer.event().value()];
                if( event_drawer_.get(buffer.event()).add(buffer)) {
                    ++logged_event_count[buffer.event().value()];
                    ++num_logged_event;
                }
                ++count;
                if(count % 10000000 == 1) {
                    log.info("loaded " + count + " events" + Arrays.toString(logged_event_count) + "/" + Arrays.toString(event_count));
                }

            }
            else if (!src_done[src]) {
               src_done[src] = true;
                ++n_src_done;
            }
        }

        log.info("loaded " + count + " events");
        for(int ii = 0 ; ii < num_src ; ++ii) {
            dis[ii].close();
        }
    }

    /**
     * For a given sequencing context, generate an event
     * @param kmer
     * @param gen
     * @return
     */
    private EnumEvent randomEvent(int kmer, Random gen) throws Exception {
        final int shift = EnumEvent.values().length * kmer;
        long sum = 0;
        for(int ii = 0; ii < EnumEvent.values().length; ++ii){
            sum += kmer_event_count_[shift + ii];
        }
        final double p = gen.nextDouble();
        if(p<0 || p > 1) {
            throw new Exception("bad p="+p);
        }
        double cdf = 0;
        for(int ii = 0; ii < EnumEvent.values().length; ++ii){
            cdf += (double)(kmer_event_count_[shift+ii])/(sum) ;
            if( p <= cdf ) return EnumEvent.value2enum(ii);
        }
        return EnumEvent.MATCH;
    }

}
