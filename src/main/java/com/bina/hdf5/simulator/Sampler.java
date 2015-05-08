package com.bina.hdf5.simulator;

import com.bina.hdf5.interfaces.EventGroup;
import com.bina.hdf5.interfaces.EventGroupFactory;
import org.apache.log4j.Logger;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Iterator;

/**
 * Created by bayo on 5/8/15.
 */
public class Sampler implements Closeable{
    private final static Logger log = Logger.getLogger(Sampler.class.getName());
    private String outPrefix_;

    public Sampler(String outPrefix, int leftFlank, int rightFlank) throws IOException {
        outPrefix_ = outPrefix;
        eventOut_ = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(Suffixes.EVENTS.filename(outPrefix_)))) ;
        Arrays.fill(event_base_count_, 0);
        Arrays.fill(event_count_, 0);
        leftFlank_ = leftFlank;
        rightFlank_ = rightFlank;
        k_ = leftFlank_ + 1 + rightFlank_;
        numKmer_ = 1 << ( 2*k_ );
        log.info("flanks=("+leftFlank_+","+rightFlank_+") k="+k_+" num_kmers="+numKmer_);
        kmer_event_count_ = new long[numKmer_ * EnumEvent.values().length];
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
            EventGroup aln = groups.getEventGroup(ii);
            if(ii%5000 == 0){
                log.info("processing group " + ii + "/" + groups.size());
                log.info(toString());
            }
            if( null == aln ){
                log.info("failed to retrieve group " + ii);
                continue;
            }
            process(aln.getEventIterator(leftFlank_, rightFlank_));
            ++count;
        }
        log.info("processed " + count + " groups");
    }

    @Override
    public void close() {

        try {
            eventOut_.flush();
            eventOut_.close();

            RandomAccessFile fos = new RandomAccessFile(Suffixes.STATS.filename(outPrefix_),"rw");
            FileChannel file = fos.getChannel();
            MappedByteBuffer buf = file.map(FileChannel.MapMode.READ_WRITE, 0, Long.SIZE/8 * kmer_event_count_.length);
            for (long entry : kmer_event_count_) {
                buf.putLong(entry);
            }
            buf.force();
            file.close();
            fos.close();
        }
        catch (IOException e){
            log.info(e,e);
        }
    }

    private DataOutputStream eventOut_;

    private final long[] event_base_count_ = new long[EnumEvent.values().length];
    private final long[] event_count_ = new long[EnumEvent.values().length];
    private long[] kmer_event_count_;

    private int leftFlank_;
    private int rightFlank_;
    private int k_;
    private int numKmer_;

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Sampler\n");
        long sum = 0;
        for(long entry : event_base_count_) sum+=entry;
        sb.append("base  count:");
        for(long entry : event_base_count_) sb.append(" "+entry+"("+100*(double)entry/(double)sum+")");
        sb.append("\n");

        sum = 0;
        for(long entry : event_count_) sum+=entry;
        sb.append("event count:");
        for(long entry : event_count_) sb.append(" "+entry+"("+100*(double)entry/(double)sum+")");
        sb.append("\n");

        return sb.toString();
    }


    private enum Suffixes{
        EVENTS(".events"),
        STATS(".stats");
        private String suffix_;
        Suffixes(String s){
            suffix_ = s;
        }
        public String filename(String prefix){
            return prefix+suffix_;
        }
    }
}
