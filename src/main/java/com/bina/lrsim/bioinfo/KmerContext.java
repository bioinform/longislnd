package com.bina.lrsim.bioinfo;


import com.bina.lrsim.interfaces.Context;

/**
 * Created by bayo on 5/11/15.
 */
public class KmerContext implements Context {
    private int kmer_;

    @Override
    public int kmer() {
        return kmer_;
    }

    @Override
    public int hpLen() {
        return 1;
    }

    KmerContext(byte[] ascii, int middle, int left_flank, int right_flank, boolean rc) throws RuntimeException{
        if(rc){
            final byte[] tmp = new byte[left_flank+right_flank+1];

            int curr = ascii.length - 1 - (middle - left_flank);
            for(int idx = 0 ; idx < tmp.length ; ++idx, --curr) {
                tmp[idx] = EnumBP.ascii_rc(ascii[curr]);
            }
            kmer_ = Kmerizer.fromASCII(tmp);
        }
        else {
            kmer_ = Kmerizer.fromASCII(ascii, middle-left_flank, middle+right_flank+1);
        }
    }

}
