package com.bina.lrsim.simulator.samples;

import com.bina.lrsim.interfaces.EventGroup;
import com.bina.lrsim.interfaces.EventGroupFactory;
import com.bina.lrsim.simulator.EnumEvent;
import com.bina.lrsim.simulator.Event;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.Arrays;
import java.util.Iterator;

/**
 * Created by bayo on 5/8/15.
 * <p/>
 * Class for going through, eg, alignment data to collect samples of sequence context-based output
 */
public class SamplesCollector extends Samples implements Closeable {
    private final static Logger log = Logger.getLogger(SamplesCollector.class.getName());
    private String outPrefix_;
    private DataOutputStream eventOut_;

    /**
     * Constructor
     *
     * @param outPrefix  prefix of output files
     * @param leftFlank  number of bp preceding the base of interest in the construction of sequencing context
     * @param rightFlank number of bp after the base of interest in the construction of sequencing context
     * @throws IOException
     */
    public SamplesCollector(String outPrefix, int leftFlank, int rightFlank, int hp_anchor) throws IOException {
        super(leftFlank, rightFlank, hp_anchor);
        outPrefix_ = outPrefix;
        eventOut_ = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(Suffixes.EVENTS.filename(outPrefix_))));
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
                if (event.event().recordEvery > 0 &&
                        kmer_event_count_ref()[EnumEvent.values().length * event.kmer() + event.event().value] % event.event().recordEvery == 0) {
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
                    event.write(eventOut_);
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
    public void process(EventGroupFactory groups, int min_length, int flank_mask) throws IOException {
        int ii = 0;
        for (; ii < groups.size(); ++ii) {
            EventGroup group = groups.getEventGroup(ii);
            if (ii % 10000 == 0 && ii != 0) {
                log.info("processing group " + ii + "/" + groups.size());
                log.info(toString());
            }
            if (null == group) {
                log.info("failed to retrieve group " + ii);
                continue;
            }
            if (group.seq_length() < min_length) {
                continue;
            }
            lengths_ref().addLast(group.seq_length());
            process(group.getEventIterator(left_flank(), right_flank(), flank_mask, flank_mask, hp_anchor()));
        }
        log.info("processed " + ii + " groups");
    }

    /**
     * finish sampling and write to file
     */
    @Override
    public void close() throws IOException {
        eventOut_.flush();
        eventOut_.close();

        writeStats(outPrefix_);
        writeIdx(outPrefix_);
        writeLengths(outPrefix_);
    }

}
