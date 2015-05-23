package com.bina.lrsim.bioinfo;


import org.apache.log4j.Logger;

/**
 * Created by bayo on 5/11/15.
 */
public final class KmerContext extends Context {
  private final static Logger log = Logger.getLogger(KmerContext.class.getName());

  KmerContext(byte[] ascii, int middle, int leftFlank, int rightFlank, boolean rc) {
    super(-1, 1);
    if (rc) {
      final byte[] tmp = new byte[leftFlank + rightFlank + 1];

      int curr = ascii.length - 1 - (middle - leftFlank);
      for (int idx = 0; idx < tmp.length; ++idx, --curr) {
        tmp[idx] = EnumBP.ascii_rc(ascii[curr]);
      }
      super.set_kmer(Kmerizer.fromASCII(tmp));
    } else {
      super.set_kmer(Kmerizer.fromASCII(ascii, middle - leftFlank, middle + rightFlank + 1));
    }
  }
}
