package com.bina.hdf5.simulator.samples;

import com.bina.hdf5.h5.pb.PBReadBuffer;
import com.bina.hdf5.simulator.EnumEvent;
import com.bina.hdf5.simulator.Event;
import com.bina.hdf5.simulator.samples.pool.BaseCallsPool;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Random;
import java.util.logging.Logger;

/**
 * Created by bayo on 5/10/15.
 *
 * Class for drawing sequence samples based on sequencing context
 */
public class SamplesDrawer extends Samples {
    private final static Logger base_log = Logger.getLogger(SamplesDrawer.class.getName());
    final EnumMap<EnumEvent,BaseCallsPool> event_drawer_ = new EnumMap<EnumEvent,BaseCallsPool> (EnumEvent.class);

    /**
     * Constructor
     * @param prefix     prefix of files storing sampled data
     * @param max_sample limit the number of samples per sequencing context
     * @throws Exception
     */
    public SamplesDrawer(String prefix, int max_sample) throws Exception {
        super(prefix);
        base_log.info("initializing enummap");
        for(EnumEvent event: EnumSet.allOf(EnumEvent.class)){
            try {
                event_drawer_.put(
                        event,
                        (BaseCallsPool) event.pool().getDeclaredConstructor(new Class[]{int.class, int.class})
                                                    .newInstance(super.numKmer_, max_sample));
            } catch (ReflectiveOperationException e) {
                base_log.info(e.getMessage());
            }
        }
        base_log.info("done");
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
        base_log.info("loading events");
        DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(Suffixes.EVENTS.filename(prefix)),1000000000)) ;
        Event buffer = new Event();
        long count = 0;
        while(dis.available() > 0) {
            buffer.read(dis);
            event_drawer_.get(buffer.event()).add(buffer);
            ++count;
            if(count % 10000000 == 1) {
                base_log.info("loaded " + count + " events");
            }
        }
        base_log.info("loaded " + count + " events");
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
