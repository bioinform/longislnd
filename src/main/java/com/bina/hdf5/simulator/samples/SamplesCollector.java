package com.bina.hdf5.simulator.samples;

import com.bina.hdf5.interfaces.EventGroup;
import com.bina.hdf5.interfaces.EventGroupFactory;
import com.bina.hdf5.simulator.EnumEvent;
import com.bina.hdf5.simulator.Event;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.Arrays;
import java.util.Iterator;

/**
 * Created by bayo on 5/8/15.
 */
public class SamplesCollector extends Samples implements Closeable{
    private final static Logger log = Logger.getLogger(SamplesCollector.class.getName());
    private String outPrefix_;
    private DataOutputStream eventOut_;

    public SamplesCollector(String outPrefix, int leftFlank, int rightFlank) throws IOException {
        super(leftFlank, rightFlank);
        outPrefix_ = outPrefix;
        eventOut_ = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(Suffixes.EVENTS.filename(outPrefix_)))) ;
        Arrays.fill(event_base_count_, 0);
        Arrays.fill(event_count_, 0);
        log.info("flanks=("+leftFlank_+","+rightFlank_+") k="+k_+" num_kmers="+numKmer_);
    }

    public void process(Iterator<Event> itr) throws Exception{
        while(itr.hasNext()){
            Event event = itr.next();
            if(null == event){
                continue;
            }
            event.write(eventOut_);
            final int idx = event.event().value();

            ++event_count_[idx];
            ++event_base_count_[idx];
            if( event.event().equals(EnumEvent.INSERTION)){
                event_base_count_[idx] += event.size() - 2;
            }
            ++kmer_event_count_[EnumEvent.values().length*event.kmer()+event.event().value()];
        }
    }

    public void process(EventGroupFactory groups) throws Exception{
        long count = 0;
        for(int ii = 0 ; ii < groups.size() ; ++ii){
            EventGroup group = groups.getEventGroup(ii);
            if(ii%5000 == 0){
                log.info("processing group " + ii + "/" + groups.size());
                log.info(toString());
            }
            if( null == group ){
                log.info("failed to retrieve group " + ii);
                continue;
            }
            if(group.seq_length() < 1000){
                continue;
            }
            lengths_.addLast(group.seq_length());
            process(group.getEventIterator(leftFlank_, rightFlank_));
            ++count;
        }
        log.info("processed " + count + " groups");
    }

    @Override
    public void close() {

        try {
            eventOut_.flush();
            eventOut_.close();

            writeStats(outPrefix_);
            writeIdx(outPrefix_);
            writeLengths(outPrefix_);

        }
        catch (IOException e){
            log.info(e,e);
        }
    }


}
