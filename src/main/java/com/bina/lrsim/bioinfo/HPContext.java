package com.bina.lrsim.bioinfo;


import java.util.Arrays;
import java.util.Iterator;

/**
 * Created by bayo on 5/11/15.
 */
public class HPContext extends Context {
    private byte[] ascii_;

    HPContext(byte[] ascii, int left_flank, int right_flank, int hp_anchor) {
        super((left_flank+right_flank+1==ascii.length)
                ? Kmerizer.fromASCII(ascii)
                : Kmerizer.fromASCII(Arrays.copyOfRange(ascii,left_flank-hp_anchor,ascii.length-right_flank+hp_anchor))
                ,ascii.length-left_flank-right_flank);
        ascii_ = ascii;
    }

    /**
     * decompose a possibly complicated context into a series of simpler contexts
     * @return an iterator of simpler contexts
     */
    @Override
    public Iterator<Context> decompose(int left_flank, int right_flank) {
        return new KmerIterator(ascii_,0,ascii_.length, left_flank, right_flank,false);
    }

}
