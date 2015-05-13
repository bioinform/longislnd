package com.bina.lrsim.simulator;

import com.bina.lrsim.bioinfo.Context;
import com.bina.lrsim.h5.bax.BaxH5Writer;
import com.bina.lrsim.h5.pb.PBReadBuffer;
import com.bina.lrsim.interfaces.RandomSequenceGenerator;
import com.bina.lrsim.simulator.samples.SamplesDrawer;
import org.apache.log4j.Logger;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.Random;

/**
 * Created by bayo on 5/11/15.
 */
public class Simulator {
    private final static Logger log = Logger.getLogger(Simulator.class.getName());
    private RandomSequenceGenerator seqGen_;
    private final long[] base_counter_ = new long[EnumEvent.values().length];
    private final long[] event_counter_ = new long[EnumEvent.values().length];

    /**
     * Constructor
     * @param seqGen a random sequence generator
     */
    public Simulator(RandomSequenceGenerator seqGen) {
        seqGen_ = seqGen;
    }

    /**
     * Generate a pacbio h5 file containing reads simulated according to the sampler and reference
     * @param path         output path
     * @param movie_name   movie name
     * @param firsthole    first hole producing sequence
     * @param drawer       an instance from which samples can be drawn
     * @param total_bases  minimum number of bp to generate
     * @param gen          random number generator
     * @throws Exception
     */
    public int simulate(String path, String movie_name, int firsthole, SamplesDrawer drawer, int total_bases, Random gen) throws Exception{
        BaxH5Writer writer = new BaxH5Writer();
        PBReadBuffer read = new PBReadBuffer();
        log.info("generating reads");

        for(int num_bases = 0; num_bases <= total_bases;) {
            read.clear();
            for(Iterator<Context> itr = seqGen_.getSequence(drawer.drawLength(gen),drawer.leftFlank(),drawer.rightFlank(),gen) ; itr.hasNext() ; ) {
                Context c = itr.next();
                final int old_length = read.size();
                EnumEvent ev = drawer.appendTo(read,c.kmer(),gen);
                ++event_counter_[ev.value()];
                ++base_counter_[ev.value()];
                if(ev.equals(EnumEvent.INSERTION)) {
                    base_counter_[ev.value()] += (read.size()-old_length) - 2;
                }
            }

            writer.addLast(read, 1000);
            num_bases += read.size();
            if(writer.size() % 10000 == 1) {
                log.info(toString());
            }
        }
        log.info(toString());
        log.info("generated "+writer.size() + " reads.");
        writer.write(path + "/" + movie_name + ".bax.h5", movie_name, firsthole);
        return writer.size();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("simulated statistics\n");
        sb.append(EnumEvent.getPrettyStats(base_counter_));
        sb.append("\n");
        sb.append(EnumEvent.getPrettyStats(event_counter_));
        return sb.toString();
    }
}
