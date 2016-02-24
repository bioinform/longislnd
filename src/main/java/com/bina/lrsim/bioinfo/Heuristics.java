package com.bina.lrsim.bioinfo;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Created by bayo on 6/4/15.
 */
public class Heuristics {

  public static final int SAMPLE_PERIOD_INS = 1; // ~10% insertion, but can be multi-bp, so let's suck it up for now
  public static final int SAMPLE_PERIOD_DEL = 1; // ~6% deletion rate take all delection samples
  public static final int SAMPLE_PERIOD_SUB = 1; // < %1 substitution rate
  public static final int SAMPLE_PERIOD_MAT = 10;// > 85% matches, down-sample 10-fold, must use compression

  // span alignment to the left if a base has matches, there're many subtle reasons for doing this
  public static final boolean SPAN_LEFT_ON_MATCHES = true;

  // span alignment to the right if a base is mismatched, there're many subtle reasons for doing this
  public static final boolean SPAN_RIGHT_ON_MISMATCHES = true;

  // replace some indels with substitutions
  public static final boolean MERGE_INDELS = true;

  // at a fixed insertion rate, extracting this many bp of insertion is probably due to mis-assembly/alignment error that is structural rather than sequencing
  // context related, note that this is related to SPAN_LEFT_ON_MATCHES and SPAN_RIGHT_ON_MISMATCHES
  public static final int MAX_INS_BP = 10;

  // the minimum number of homopolymer samples required to treat it as full homopolymer context
  public static final int MIN_HP_SAMPLES = 20;

  // the minimum number of kmer samples of an event, to treat that as one with non zero custom frequency
  // useful if very little data is sampled
  public static final int MIN_KMER_SAMPLES_FOR_NON_ZERO_CUSTOM_FREQUENCY = 0;

  // the maximum number of sample extracted per kmer per event
  // this is to optimize I/O
  public static final int MAX_KMER_EVENT_SAMPLES = 100;

  // maximum fraction of N's to allow, relative to the length of the simulated reads
  public static final double MAX_N_FRACTION_ON_READ = 0.005;

  // force inserted bases to be the same as adjucent bases
  public static final boolean ARTIFICIAL_CLEAN_INS = false;

  // limit insertion size to this value
  public static final int MAX_INS_LENGTH = Integer.MAX_VALUE / 2;

  // discard dirty homopolymer samples
  public static final boolean DISCARD_DIRTY_HOMOPOLYMER_SAMPLES = false;

  // fraction of maximum insert, for an insert to be consider not a full insert in SMRT belt context
  public static final double SMRT_INSERT_FRACTION = 0.5;

  // approximation of SMRT belt adapter read out
  public static final byte[] SMRT_ADAPTOR_STRING;
  public static final byte[] SMRT_ADAPTOR_SCORE;
  public static final byte[] SMRT_ADAPTOR_TAG;
  static {
    SMRT_ADAPTOR_STRING = "ATCTCTCTCTTTTCCTCCTCCTCCGTTGTTGTTGTTGAGAGAGAT".getBytes(StandardCharsets.US_ASCII);
    SMRT_ADAPTOR_SCORE = new byte[SMRT_ADAPTOR_STRING.length];
    Arrays.fill(SMRT_ADAPTOR_SCORE, (byte) 15); // all Q15, probably ok
    SMRT_ADAPTOR_TAG = new byte[SMRT_ADAPTOR_STRING.length];
    Arrays.fill(SMRT_ADAPTOR_TAG, EnumBP.N.ascii);
  }
}
