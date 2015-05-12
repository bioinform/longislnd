package com.bina.hdf5.bioinfo;

import java.util.Random;
import java.util.logging.Logger;

/**
 * Created by bayo on 5/8/15.
 */
public class Kmerizer {
    public static int fromASCII(byte[] ascii, int begin, int end) throws Exception {
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

    static private Random gen_ = new Random(1111);

    public static int fromASCII(byte[] ascii) throws Exception {
        return fromASCII(ascii, 0, ascii.length);
    }
}
