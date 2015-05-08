package com.bina.hdf5.util;

/**
 * Created by bayo on 5/8/15.
 */
public class Kmerizer {
    public static int fromASCII(byte[] ascii){
        int out = 0;
        for(int ii = 0 ; ii < ascii.length ; ++ii){
            out *= 4;
            out += EnumBP.ascii2value(ascii[ii]);
        }
        return out;
    }
}
