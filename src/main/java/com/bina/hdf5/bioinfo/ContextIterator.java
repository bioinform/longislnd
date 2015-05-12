package com.bina.hdf5.bioinfo;

import org.apache.log4j.Logger;

import java.util.Iterator;

/**
 * Created by bayo on 5/11/15.
 */
public class ContextIterator implements Iterator<Context> {
    private final static Logger log = Logger.getLogger(ContextIterator.class.getName());
    private byte[] seq_;
    private int curr_;
    private int end_;
    private int leftFlank_;
    private int rightFlank_;
    private boolean rc_;


    ContextIterator(byte[] ascii, int begin, int end, int left_flank, int right_flank, boolean rc) {
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
        Context c = null;
        try {
            c = new Context(seq_,curr_,leftFlank_,rightFlank_,rc_);
        } catch (Exception e) {
            e.printStackTrace();
            c = null;
        }
        ++curr_;
        // there can be a running sum optimization but let's wait for homopolymer
        return c;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
