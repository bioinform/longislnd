package com.bina.lrsim.bioinfo;

import com.bina.lrsim.interfaces.Context;
import com.bina.lrsim.interfaces.RandomSequenceGenerator;
import htsjdk.samtools.reference.FastaSequenceFile;
import htsjdk.samtools.reference.ReferenceSequence;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * Created by bayo on 5/7/15.
 */
public class WeightedReference implements RandomSequenceGenerator {
    @Override
    public Iterator<Context> getSequence( int length, int left_flank, int right_flank, Random gen ) {
        Iterator<Context> itr = null;
        while(null == itr){
            itr = getSequenceImpl(length,left_flank,right_flank,gen);
        }
        return itr;
    }

    private Iterator<Context> getSequenceImpl(int length, int left_flank, int right_flank, Random gen) {
        final boolean rc = gen.nextBoolean();
        final long num_bases = ref_cdf_.get(ref_cdf_.size()-1);
        final long pos = (num_bases <= Integer.MAX_VALUE) ? gen.nextInt((int)num_bases) : gen.nextLong() % num_bases;

        int ref_idx = 0;
        for( ; ref_idx < ref_cdf_.size() && pos >= ref_cdf_.get(ref_idx) ; ++ref_idx ) {
        }
        final int ref_pos = (0 == ref_idx) ? (int)pos : (int)(pos - ref_cdf_.get(ref_idx-1));

        if(ref_pos+length <= get(ref_idx).length){
            return new KmerIterator(get(ref_idx),ref_pos,ref_pos+length,left_flank,right_flank, rc);
        }
        return null;
    }

    public byte[] get(String name){
        return name_ref_wei_.get(name).seq().getBases();
    }

    public byte[] get(int id){
        return get(name_.get(id));
    }

    public WeightedReference(String filename){
        reference_ = new FastaSequenceFile(new java.io.File(filename),true);
        long num_bases = 0;
        for(ReferenceSequence rr = reference_.nextSequence() ; null != rr ; rr=reference_.nextSequence()){
            name_.add(rr.getName());
            name_ref_wei_.put(rr.getName(), new ReferenceWeight(rr, 1.0));
            log.info("read "+rr.getName());
            num_bases += get(name_.size()-1).length;
            ref_cdf_.add(num_bases);
        }
    }

    final Map<String,ReferenceWeight> name_ref_wei_ = new HashMap<String,ReferenceWeight>();
    final ArrayList<String> name_ = new ArrayList<String>();
    final ArrayList<Long> ref_cdf_ = new ArrayList<Long>();
    FastaSequenceFile reference_;
    private final static Logger log = Logger.getLogger(WeightedReference.class.getName());


    static private class ReferenceWeight{
        public int size() {return seq_.length();}
        public ReferenceSequence seq() { return seq_; }
        public double weight() { return weight_; }
        ReferenceWeight(ReferenceSequence seq, double weight){
            seq_ = seq;
            weight_ = weight;
        }
        ReferenceSequence seq_;
        double weight_;
    }
}