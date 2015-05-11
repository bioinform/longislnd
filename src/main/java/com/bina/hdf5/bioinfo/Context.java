package com.bina.hdf5.bioinfo;

import java.util.logging.Logger;

/**
 * Created by bayo on 5/11/15.
 */
public class Context {
    private final static Logger log = Logger.getLogger(Context.class.getName());
    Context(byte[] ascii, int middle, int left_flank, int right_flank, boolean rc) {
        if(rc){
            //obvious not the smartest, should use running sum, but let's refactor after homopolymer is in
            final byte[] tmp = new byte[left_flank+right_flank+1];

            int curr = ascii.length - 1 - (middle - left_flank);
            for(int idx = 0 ; idx < tmp.length ; ++idx, --curr) {
                if(curr < 0) {
                    log.info(ascii.length + " " + left_flank + " " + middle + " " + right_flank + " " +rc);
                    log.info(" "+curr);
                }
                tmp[idx] = ascii[curr];
            }
            kmer_ = Kmerizer.fromASCII(tmp);
        }
        else {
            if(middle+right_flank+1 >= ascii.length){
                log.info(ascii.length + " " + left_flank + " " + middle + " " + right_flank + " " +rc);
            }
            kmer_ = Kmerizer.fromASCII(ascii, middle-left_flank, middle+right_flank+1);
        }
    }

    private int kmer_;

    public int kmer() {
        return kmer_;
    }
}
