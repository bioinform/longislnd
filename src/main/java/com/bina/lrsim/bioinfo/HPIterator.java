package com.bina.lrsim.bioinfo;

import org.apache.log4j.Logger;

import java.util.Iterator;

/**
 * Created by bayo on 5/13/15.
 *
 * An iterator which iterates through homopolymer context sequence.
 *
 * A valid homopolymer context of length (q-p+1) is centered at a position, p,
 * such that the bp at p-1 is different. The bp from p to q are the same, and
 * the bp of q+1 is different.
 *
 * CGAAAAAAAAAATCA
 *   p        q
 *
 * The iterator for the above situation would output context at
 *
 * ... (p-1), p, q+1, ......
 *
 */
public class HPIterator implements Iterator<Context> {
    private final static Logger log = Logger.getLogger(HPIterator.class.getName());
    private byte[] seq_;
    private int curr_;
    private int end_;
    private int leftFlank_;
    private int rightFlank_;
    private boolean rc_;
    private byte[] buffer_;

    /**
     * Constructor to iterate the kmer context of through [begin,end) of a ascii stream
     * @param ascii       ascii file
     * @param begin       0-base begin
     * @param end         0-base end, exclusive
     * @param left_flank  number of bp before the position of interest
     * @param right_flank number of bp after the position of interest
     * @param rc          if we are doing reverse complement of ascii
     */
    HPIterator(byte[] ascii, int begin, int end, int left_flank, int right_flank, boolean rc) {
        leftFlank_ = left_flank;
        rightFlank_ = right_flank;
        rc_ = rc;
        seq_ = ascii;
        buffer_ = new byte[left_flank+1+right_flank];

        curr_ = begin + left_flank;
        end_ = end - right_flank;
        if(rc) {
            curr_ = seq_.length - 1 - curr_;
            end_ = seq_.length - 1 - end_;

            for(;0 <= curr_ && seq_[curr_]==seq_[curr_+1];--curr_) {
            }

            for(;end_+2< seq_.length && seq_[end_+1] == seq_[end_+2];++end_) {
            }

        }
        else {
            for(;curr_ < seq_.length && seq_[curr_]==seq_[curr_-1]; ++curr_){
            }
            for(;0 <= end_-2 && seq_[end_-1]==seq_[end_-2]; --end_) {
            }
        }
    }

    @Override
    public boolean hasNext() {
        if(rc_) return curr_>end_;
        else    return curr_<end_;
    }

    @Override
    public Context next() {
        if(rc_) {
            return rc_next();
        }
        else {
            return fw_next();
        }
    }

    private HPContext fw_next() {
        int kk = 0;
        for(int ii = curr_-leftFlank_; ii <= curr_ ; ++kk, ++ii) {
            buffer_[kk] = seq_[ii];
        }
        int diff_pos = curr_ + 1;
        for(; diff_pos < seq_.length && seq_[diff_pos] == seq_[curr_]; ++diff_pos) {
        }

        for(int pos = diff_pos ;kk<buffer_.length && pos < seq_.length;++kk,++pos) {
            buffer_[kk] = seq_[pos];
        }

        final int length = diff_pos-curr_;

        curr_ = diff_pos;

        if(kk == buffer_.length) {
            return new HPContext(buffer_,length);
        }
        else {
            return null;
        }
    }

    private HPContext rc_next() {
        int kk = 0;
        for(int ii = curr_+leftFlank_; ii>=curr_; ++kk, --ii) {
            buffer_[kk] = seq_[ii];
        }
        int diff_pos = curr_ - 1;
        for(; diff_pos >=0 && seq_[diff_pos] == seq_[curr_]; --diff_pos) {
        }

        for(int pos = diff_pos; kk<buffer_.length && pos >=0; ++kk, --pos) {
            buffer_[kk] = seq_[pos];
        }
        final int length = curr_ - diff_pos;

        curr_ = diff_pos;

        if(kk == buffer_.length) {
            return new HPContext(buffer_, length);
        }
        else {
            return null;
        }
    }


    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
