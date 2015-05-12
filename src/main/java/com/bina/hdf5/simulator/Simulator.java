package com.bina.hdf5.simulator;

import com.bina.hdf5.bioinfo.Context;
import com.bina.hdf5.h5.bax.BaxH5Writer;
import com.bina.hdf5.h5.pb.PBReadBuffer;
import com.bina.hdf5.interfaces.RandomSequenceGenerator;
import com.bina.hdf5.simulator.samples.SamplesDrawer;
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
    public void simulate(String path, String movie_name, int firsthole, SamplesDrawer drawer, int total_bases, Random gen) throws Exception{
        BaxH5Writer writer = new BaxH5Writer();
        PBReadBuffer read = new PBReadBuffer();
        log.info("generating reads");

        int num_bases = 0;
        for(;num_bases <= total_bases;) {
            read.clear();
            for(Iterator<Context> itr = seqGen_.getSequence(drawer.drawLength(gen),gen) ; itr.hasNext() ; ) {
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
        }
        log.info(toString());
        log.info("generated "+writer.size() + " reads.");
        writer.write(path + "/" + movie_name + ".bax.h5", movie_name, firsthole);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("cummulated statistics\n");
        {
            long sum = 0;
            for (long entry : base_counter_) {
                sum += entry;
            }
            for (EnumEvent ev : EnumSet.allOf(EnumEvent.class)) {
                long count = base_counter_[ev.value()];
                sb.append(" " + ev.toString() + " " + count + " " + (double) (count) / sum);
            }
        }
        sb.append("\n");
        {
            long sum = 0;
            for (long entry : event_counter_) {
                sum += entry;
            }
            for (EnumEvent ev : EnumSet.allOf(EnumEvent.class)) {
                long count = event_counter_[ev.value()];
                sb.append(" " + ev.toString() + " " + count + " " + (double) (count) / sum);
            }
        }
        return sb.toString();

    }
}
