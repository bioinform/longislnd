package com.bina.hdf5.bioinfo;


/**
 * Created by bayo on 5/11/15.
 */
public class Context {
    Context(byte[] ascii, int middle, int left_flank, int right_flank, boolean rc) throws Exception{
        if(rc){
            //obvious not the smartest, should use running sum, but let's refactor after homopolymer is in
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

    private int kmer_;

    public int kmer() {
        return kmer_;
    }
}
