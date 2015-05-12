package com.bina.hdf5.simulator.samples;

import com.bina.hdf5.h5.pb.PBReadBuffer;
import com.bina.hdf5.simulator.EnumEvent;
import com.bina.hdf5.simulator.Event;
import com.bina.hdf5.simulator.samples.pool.BaseCallsPool;
import org.apache.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
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

    /**
     * Constructor
     * @param prefix     prefix of files storing sampled data
     * @param max_sample for match events, limit the number of samples per sequencing context
     * @throws Exception
     */
    public SamplesDrawer(String prefix, int max_sample) throws Exception {
        super(prefix);
        log.info("initializing sample pools");
        for(EnumEvent event: EnumSet.allOf(EnumEvent.class)){
            final int cap = (event.equals(EnumEvent.MATCH) ) ? max_sample : -1;
            try {
                event_drawer_.put(
                        event,
                        (BaseCallsPool) event.pool().getDeclaredConstructor(new Class[]{int.class, int.class})
                                                    .newInstance(super.numKmer_, cap));
            } catch (ReflectiveOperationException e) {
                log.info(e,e);
            }
        }
        log.info("done");
        loadEvents(prefix);
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
     * Load the sampled events
     * @param prefix      prefix of the event file
     * @throws Exception
     */
    private void loadEvents(String prefix) throws Exception {
        log.info("loading events");
        DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(Suffixes.EVENTS.filename(prefix)),1000000000)) ;
        Event buffer = new Event();
        long count = 0;
        while(dis.available() > 0) {
            buffer.read(dis);
            event_drawer_.get(buffer.event()).add(buffer);
            ++count;
            if(count % 10000000 == 1) {
                log.info("loaded " + count + " events");
            }
        }
        log.info("loaded " + count + " events");
        dis.close();
    }

    /**
     * For a given sequencing context, generate an event
     * @param kmer
     * @param gen
     * @return
     */
    private EnumEvent randomEvent(int kmer, Random gen) {
        final int shift = EnumEvent.values().length * kmer;
        double sum = 0;
        for(int ii = 0; ii < EnumEvent.values().length; ++ii){
            sum += kmer_event_count_[shift + ii];
        }
        final double p = gen.nextDouble();
        double cdf = 0;
        for(int ii = 0; ii < EnumEvent.values().length; ++ii){
            cdf += (double)(kmer_event_count_[shift+ii])/(sum) ;
            if( p <= cdf ) return EnumEvent.value2enum(ii);
        }
        return EnumEvent.MATCH;
    }

}
