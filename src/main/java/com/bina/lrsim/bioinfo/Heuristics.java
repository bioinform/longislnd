package com.bina.lrsim.bioinfo;

/**
 * Created by bayo on 6/4/15.
 */
public class Heuristics {
  // span alignment to the left if a base has matches, there're many subtle reasons for doing this
  public static final boolean SPAN_LEFT_ON_MATCHES = true;

  // span alignment to the right if a base is mismatched, there're many subtle reasons for doing this
  public static final boolean SPAN_RIGHT_ON_MISMATCHES = true;

  // at a fixed insertion rate, extracting this many bp of insertion is probably due to mis-assembly/alignment error that is structural rather than sequencing
  // context related, note that this is related to SPAN_LEFT_ON_MATCHES and SPAN_RIGHT_ON_MISMATCHES
  public static final int MAX_INS_BP = 10;

  // the minimum number of homopolymer samples required to treat it as full homopolymer context
  public static final int MIN_HP_SAMPLES = 20;
}
