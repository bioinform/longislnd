package com.bina.hdf5.bioinfo;

import java.util.Random;

/**
 * Created by bayo on 5/8/15.
 *
 * kmerize up to 16bp, randomly select a base for N
 */
public class Kmerizer {
    static private Random gen_ = new Random(1111);
    /**
     * kmerize a ascii stream of up to 16 bp
     * @param ascii ascii stream
     * @param begin begin position
     * @param end   ending position
     * @return
     * @throws Exception
     */
    public static int fromASCII(byte[] ascii, int begin, int end) throws Exception {
        if(end-begin > 16) throw new Exception("stream too big for 16-mer");
        int out = 0;
        for(int ii = begin ; ii < end; ++ii){
            out *= 4;
            int val = EnumBP.ascii2value(ascii[ii]);
            if(val == EnumBP.N.value()) {
                val = gen_.nextInt(4);
            }
            if(val >=4) throw new Exception("bad ascii stream");
            out += EnumBP.ascii2value(ascii[ii]);
        }
        return out;
    }


    public static int fromASCII(byte[] ascii) throws Exception {
        return fromASCII(ascii, 0, ascii.length);
    }

    public static String toString(int kmer, int length) {
        StringBuilder sb = new StringBuilder();
        for(byte entry: toByteArray(kmer,length))  {
            sb.append((char)entry);
        }
        return sb.toString();
    }
    public static byte[] toByteArray(int kmer, int length) {
        byte[] tmp = new byte[length];
        for(int ii = length-1 ; ii>=0 ; --ii) {
            tmp[ii] = EnumBP.value2ascii((byte)(kmer%4));
            kmer/=4;
        }
        return tmp;
    }
}
