package com.bina.hdf5.simulator;

import com.bina.hdf5.h5.pb.PBReadBuffer;
import com.bina.hdf5.simulator.pool.BaseCallsPool;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.EnumMap;
import java.util.EnumSet;

/**
 * Created by bayo on 5/10/15.
 */
public class SampleDrawer extends Sampler {
    public SampleDrawer(String prefix,int max_sample) throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        super(prefix);
        for(EnumEvent event: EnumSet.allOf(EnumEvent.class)){
            event_drawer_.put(
                    event,
                    (BaseCallsPool)event.pool().getDeclaredConstructor(new Class[]{int.class,int.class})
                                               .newInstance(super.numKmer_, max_sample));
        }
    }

    public void appendTo(PBReadBuffer buffer, int kmer) throws Exception{
        event_drawer_.get(randomEvent(kmer)).appendTo(buffer,kmer);
    }

    private void loadEvents(String prefix) throws Exception {
        DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(Suffixes.EVENTS.filename(prefix)))) ;
        Event buffer = new Event();
        while(dis.available() > 0) {
            buffer.read(dis);
            event_drawer_.get(buffer.event()).add(buffer);
        }
    }

    private EnumEvent randomEvent(int kmer) {
        return EnumEvent.MATCH;
    }

    final EnumMap<EnumEvent,BaseCallsPool> event_drawer_ = new EnumMap<EnumEvent,BaseCallsPool> (EnumEvent.class);
}
