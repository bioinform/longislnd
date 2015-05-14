package com.bina.lrsim.bioinfo;


import com.bina.lrsim.interfaces.Context;

import java.util.Iterator;

/**
 * Created by bayo on 5/11/15.
 */
public class HPContext implements Context {
    private int kmer_;
    private int len_;
    private int leftFlank_;
    private int rightFlank_;

    /**
     * decompose this complicated context into a series of simpler contexts
     * @return an iterator of simpler contexts
     */

    Iterator<Context> decompose() {
        byte[] ascii = new byte[leftFlank_+len_+rightFlank_];
        byte[] ba = Kmerizer.toByteArray(kmer_,leftFlank_+1+rightFlank_);

        int kk = 0;
        for(;kk<leftFlank_;++kk) {
            ascii[kk] = ba[kk];
        }

        for(;kk<leftFlank_+len_;++kk) {
            ascii[kk] = ba[leftFlank_];
        }

        for(int rr = 0 ; rr < rightFlank_; ++kk, ++rr) {
            ascii[kk] = ba[leftFlank_+1+rr];

        }
        if (kk != ascii.length) throw new RuntimeException("incorrect decompsitions");

        return new KmerIterator(ascii,0,ascii.length,leftFlank_,rightFlank_,false);
    }

    @Override
    public int kmer() {
        return kmer_;
    }

    @Override
    public int hpLen() {
        return len_;
    }

    HPContext(byte[] ascii, int len, int left_flank, int right_flank) {
        kmer_ = Kmerizer.fromASCII(ascii);
        len_ = len;
        leftFlank_ = left_flank;
        rightFlank_ = right_flank;
    }

}
