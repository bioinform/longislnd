package com.bina.hdf5.simulator;

import com.bina.hdf5.bioinfo.Context;
import com.bina.hdf5.bioinfo.WeightedReference;
import com.bina.hdf5.h5.bax.BaxH5Writer;
import com.bina.hdf5.h5.pb.PBReadBuffer;
import com.bina.hdf5.simulator.samples.SamplesDrawer;
import org.apache.log4j.Logger;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.Random;

/**
 * Created by bayo on 5/11/15.
 */
public class Simulator {
    static public void run(String[] args) {
        if(args.length != 6){
            log.info("parameters: out_dir fasta model_prefix total_bases sample_per seed");
            return;
        }
        String out_dir = args[0];
        String fasta = args[1];
        String model_prefix = args[2];
        int total_bases = Integer.getInteger(args[3]);
        int sample_per = Integer.getInteger(args[4]);
        int seed = Integer.getInteger(args[5]);
        try {
            SamplesDrawer samples = new SamplesDrawer(model_prefix,sample_per);
            Simulator sim = new Simulator(fasta);
            Random gen = new Random(seed);
            String movie_name = "m000000_000000_11111_cSIMULATED_s0_p0";
            sim.simulate(out_dir,movie_name,0,samples,total_bases,gen);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private final static Logger log = Logger.getLogger(Simulator.class.getName());
    private WeightedReference references_;
    private final long[] base_counter_ = new long[EnumEvent.values().length];

    public Simulator(String fasta) {
        references_ = new WeightedReference(fasta);
    }

    public void simulate(String path, String movie_name, int firsthole, SamplesDrawer drawer, int total_bases, Random gen) throws Exception{
        BaxH5Writer writer = new BaxH5Writer();
        PBReadBuffer read = new PBReadBuffer();

        int num_bases = 0;
        for(;num_bases <= total_bases;) {
            log.info("simulating an extra read at " + writer.size() + "/" + total_bases);
            read.clear();
            for(Iterator<Context> itr = references_.getSequence(drawer.drawLength(gen),gen) ; itr.hasNext() ; ) {
                Context c = itr.next();
                final int old_length = read.size();
                EnumEvent ev = drawer.appendTo(read,c.kmer(),gen);
                ++base_counter_[ev.value()];
                if(ev.equals(EnumEvent.INSERTION)) {
                    base_counter_[ev.value()] += (read.size()-old_length) - 2;
                }
            }
            writer.addLast(read, 1000);
            num_bases += read.size();
        }
        log.info(toString());
        writer.write(path+"/"+movie_name+".bax.h5", movie_name, firsthole);
    }

    public String toString() {
        long sum = 0;
        for(long entry: base_counter_) {
            sum+=entry;
        }
        StringBuilder sb = new StringBuilder();
        for(EnumEvent ev : EnumSet.allOf(EnumEvent.class)) {
            long count = base_counter_[ev.value()];
            sb.append(" " + ev.toString() + " " + count + " " + (double)(count)/sum);
        }
        return sb.toString();

    }
}