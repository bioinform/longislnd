package com.bina.hdf5.bioinfo;

import java.util.logging.Logger;

/**
 * Created by bayo on 5/8/15.
 */
public class Kmerizer {
    private final static Logger log = Logger.getLogger(Kmerizer.class.getName());
    public static int fromASCII(byte[] ascii, int begin, int end){
        int out = 0;
        for(int ii = begin ; ii < end; ++ii){
            out *= 4;
            if(ii >= ascii.length){
                log.info(ascii.length + " " + begin + " " + ii + " " + end);
            }
            out += EnumBP.ascii2value(ascii[ii]);
        }
        return out;
    }

    public static int fromASCII(byte[] ascii){
        return fromASCII(ascii, 0, ascii.length);
    }
}
