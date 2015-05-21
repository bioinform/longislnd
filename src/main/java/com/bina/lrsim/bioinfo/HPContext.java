package com.bina.lrsim.bioinfo;


import java.util.Iterator;

/**
 * Created by bayo on 5/11/15.
 */
public class HPContext extends Context {
    private byte[] ascii_;

    static private int constructor_kmerizer(byte[] ascii, int left_flank, int right_flank, int hp_anchor) {
        if(ascii.length == 1 + left_flank + right_flank) {
            return Kmerizer.fromASCII(ascii);
        }
        else {
            byte[] tmp = new byte[2*hp_anchor+1];
            int k = 0;
            for(int pos = left_flank-hp_anchor; pos <= left_flank; ++pos,++k) {
                tmp[k] = ascii[pos];
            }
            for(int pos = ascii.length - right_flank; pos < ascii.length - right_flank + hp_anchor; ++pos, ++k) {
                tmp[k] = ascii[pos];
            }
            return Kmerizer.fromASCII(tmp);
        }
    }

    HPContext(byte[] ascii, int left_flank, int right_flank, int hp_anchor) {
        super( constructor_kmerizer(ascii,left_flank,right_flank,hp_anchor)
             , ascii.length-left_flank-right_flank);
        ascii_ = ascii;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for(byte entry: ascii_) {
            sb.append((char)entry);
        }
        sb.append(" ");
        sb.append(String.valueOf(hp_len()));
        sb.append(" ");
        sb.append(String.valueOf(kmer()));
        return sb.toString();
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
