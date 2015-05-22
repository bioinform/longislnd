package com.bina.lrsim.bioinfo;

import java.util.Iterator;

/**
 * Created by bayo on 5/13/15.
 * <p/>
 * a sequencing Context, eg a base call at position p plus flanking sequences
 * <p/>
 * kmer, hpLen are just common names of integers with unique mapping to a sequencing context
 */
public class Context {
    private int kmer_;
    private final int hp_len_;

    public Context(int kmer, int hp_len) {
        kmer_ = kmer;
        hp_len_ = hp_len;
    }

    protected final void set_kmer(int k) {
        kmer_ = k;
    }

    public final int kmer() {
        return kmer_;
    }

    public final int hp_len() {
        return hp_len_;
    }

    public String toString() {
        return String.valueOf(kmer()) + " " + String.valueOf(hp_len());
    }


    /**
     * decompose a possibly complicated context into a series of simpler contexts
     *
     * @param leftFlank  left flank of the resulting iterator
     * @param rightFlank right flank of the resulting iterator
     * @return
     */
    public Iterator<Context> decompose(int leftFlank, int rightFlank) {
        throw new UnsupportedOperationException("cannot find a simpler decomposition");
    }
}
