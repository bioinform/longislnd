package com.bina.lrsim.bioinfo;


import java.util.Iterator;

/**
 * Created by bayo on 5/11/15.
 */
public class HPContext extends Context {

    HPContext(byte[] ascii, int len) {
        super(Kmerizer.fromASCII(ascii),len);
    }

}
