package com.bina.lrsim.bioinfo;


/**
 * Created by bayo on 5/11/15.
 */
public class KmerContext extends Context {

    KmerContext(byte[] ascii, int middle, int left_flank, int right_flank, boolean rc) throws RuntimeException{
        super(-1,(short)1);
        if(rc){
            final byte[] tmp = new byte[left_flank+right_flank+1];

            int curr = ascii.length - 1 - (middle - left_flank);
            for(int idx = 0 ; idx < tmp.length ; ++idx, --curr) {
                tmp[idx] = EnumBP.ascii_rc(ascii[curr]);
            }
            super.set_kmer(Kmerizer.fromASCII(tmp));
        }
        else {
            super.set_kmer(Kmerizer.fromASCII(ascii, middle-left_flank, middle+right_flank+1));
        }
    }
}
