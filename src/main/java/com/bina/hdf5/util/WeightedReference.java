package com.bina.hdf5.util;

import htsjdk.samtools.reference.FastaSequenceFile;
import htsjdk.samtools.reference.ReferenceSequence;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by bayo on 5/7/15.
 */
public class WeightedReference {
    static private class ReferenceWeight{
        public ReferenceSequence seq() { return seq_; }
        public double weight() { return weight_; }
        ReferenceWeight(ReferenceSequence seq, double weight){
            seq_ = seq;
            weight_ = weight;
        }
        ReferenceSequence seq_;
        double weight_;
    }

    public byte[] get(String name){
        return name_ref_wei_.get(name).seq().getBases();
    }

    public byte[] get(int id){
        log.info(id+"->"+name_.get(id));
        return get(name_.get(id));
    }

    public WeightedReference(String filename){
        reference_ = new FastaSequenceFile(new java.io.File(filename),true);
        for(ReferenceSequence rr = reference_.nextSequence() ; null != rr ; rr=reference_.nextSequence()){
            name_.add(rr.getName());
            name_ref_wei_.put(rr.getName(), new ReferenceWeight(rr, 1.0));
            log.info("read "+rr.getName());
        }
    }

    final Map<String,ReferenceWeight> name_ref_wei_ = new HashMap<String,ReferenceWeight>();
    final ArrayList<String> name_ = new ArrayList<String>();
    FastaSequenceFile reference_;
    private final static Logger log = Logger.getLogger(WeightedReference.class.getName());
}
