package com.bina.lrsim.bioinfo;

import org.apache.log4j.Logger;

import java.util.Iterator;

/**
 * Created by bayo on 5/11/15.
 */
public final class KmerIterator implements Iterator<Context> {
    private final static Logger log = Logger.getLogger(KmerIterator.class.getName());
    private final byte[] seq_;
    private int curr_;
    private final int end_;
    private final int leftFlank_;
    private final int rightFlank_;
    private final boolean rc_;

    /**
     * Constructor to iterate the kmer context of through [begin,end) of a ascii stream
     *
     * @param ascii       ascii file
     * @param begin       0-base begin
     * @param end         0-base end, exclusive
     * @param left_flank  number of bp before the position of interest
     * @param right_flank number of bp after the position of interest
     * @param rc          if we are doing reverse complement
     */
    KmerIterator(byte[] ascii, int begin, int end, int left_flank, int right_flank, boolean rc) {
        leftFlank_ = left_flank;
        rightFlank_ = right_flank;
        rc_ = rc;
        seq_ = ascii;
        curr_ = begin + left_flank;
        end_ = end - right_flank;
    }

    @Override
    public boolean hasNext() {
        return curr_ < end_;
    }

    @Override
    public Context next() {
        KmerContext c = null;
        try {
            //there can be a running sum optimization
            c = new KmerContext(seq_, curr_, leftFlank_, rightFlank_, rc_);
        } catch (RuntimeException e) {
            e.printStackTrace();
            c = null;
        }
        ++curr_;
        return c;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
