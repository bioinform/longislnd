package com.bina.lrsim.bioinfo;


import org.apache.log4j.Logger;

/**
 * Created by bayo on 5/11/15.
 */
public final class KmerContext extends Context {
  private final static Logger log = Logger.getLogger(KmerContext.class.getName());

  KmerContext(byte[] ascii, int middle, int leftFlank, int rightFlank) {
    super(Kmerizer.fromASCII(ascii, middle - leftFlank, middle + rightFlank + 1), 1);
  }
}
