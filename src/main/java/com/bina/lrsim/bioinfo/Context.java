package com.bina.lrsim.bioinfo;

import java.util.Iterator;

/**
 * Created by bayo on 5/13/15.
 *
 * a sequencing Context, eg a base call at position p plus flanking sequences
 *
 * kmer, hpLen are just common names of integers with unique mapping to a sequencing context
 */
public class Context {
    private int kmer_;
    private short hp_len_;

    public Context(int kmer, short hp_len) {
        kmer_ = kmer;
        hp_len_ = hp_len;
    }

    protected void set_kmer(int k) {
        kmer_ = k;
    }

    public final int kmer() {
        return kmer_;
    }

    public final short hp_len() {
        return hp_len_;
    }

    /**
     * decompose a possibly complicated context into a series of simpler contexts
     * @return an iterator of simpler contexts
     */

    Iterator<Context> decompose(int left_flank, int right_flank) {
        byte[] ascii = new byte[left_flank + hp_len()+ right_flank];
        byte[] ba = Kmerizer.toByteArray(kmer(), left_flank +1+ right_flank);

        int kk = 0;
        for(;kk< left_flank;++kk) {
            ascii[kk] = ba[kk];
        }

        for(;kk< left_flank + hp_len();++kk) {
            ascii[kk] = ba[left_flank];
        }

        for(int rr = 0 ; rr < right_flank; ++kk, ++rr) {
            ascii[kk] = ba[left_flank +1+rr];

        }
        if (kk != ascii.length) throw new RuntimeException("incorrect decompsitions");

        return new KmerIterator(ascii,0,ascii.length, left_flank, right_flank,false);
    }
}
