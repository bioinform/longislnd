package com.bina.lrsim.bioinfo;

import java.util.ArrayList;

import com.google.common.base.Joiner;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.log4j.Logger;


/**
 * Created by bayo on 5/7/15.
 *
 * This class is obvious hacked up and need some clean up
 */
public class ShotgunSequenceDrawer extends ReferenceSequenceDrawer {
  private final static Logger log = Logger.getLogger(ShotgunSequenceDrawer.class.getName());
  final ArrayList<Long> ref_cdf_ = new ArrayList<Long>();

  public ShotgunSequenceDrawer(String filename) {
    super(filename);
    long num_bases = 0;
    for (String entry : name) {
      num_bases += get(entry).getSeq().length;
      ref_cdf_.add(num_bases);
    }
    log.info("length cdf: " + Joiner.on(" ").join(ref_cdf_));
  }

  @Override
  protected Fragment getSequenceImpl(int length, RandomGenerator gen) {
    final boolean rc = gen.nextBoolean();
    final long num_bases = ref_cdf_.get(ref_cdf_.size() - 1);
    final long pos = (num_bases <= Integer.MAX_VALUE) ? gen.nextInt((int) num_bases) : (gen.nextLong() % num_bases + num_bases) % num_bases;

    int ref_idx = 0;
    for (; ref_idx < ref_cdf_.size() && pos >= ref_cdf_.get(ref_idx); ++ref_idx) {}

    final Fragment ref_frag = get(ref_idx);
    final byte[] ref_seq = ref_frag.getSeq();

    if (length <= ref_seq.length) {
      int begin = gen.nextInt(ref_seq.length - length + 1);
      int number_of_n = 0;
      final byte[] sequence = new byte[length];
      if (rc) {
        for (int ss = 0, cc = ref_seq.length - 1 - begin; ss < length; ++ss, --cc) {
          sequence[ss] = EnumBP.ascii_rc(ref_seq[cc]);
          if (EnumBP.ascii2value(sequence[ss]) == EnumBP.N.value) {
            ++number_of_n;
          }
        }
        // shift begin/end back to forward strain's coordinate to describe locus
        begin = ref_seq.length - begin - length;
      } else {
        for (int ss = 0; ss < length; ++ss) {
          sequence[ss] = ref_seq[begin + ss];
          if (EnumBP.ascii2value(sequence[ss]) == EnumBP.N.value) {
            ++number_of_n;
          }
        }
      }
      if (length * Heuristics.MAX_N_FRACTION_ON_READ < number_of_n) { return null; }
      // return new KmerIterator(get(ref_idx),ref_pos,ref_pos+length,leftFlank,rightFlank, rc);
      // return new HPIterator(get(ref_idx), ref_pos, ref_pos + length, leftFlank, rightFlank, hp_anchor, rc);
      return new Fragment(sequence, new Locus(ref_frag.getLocus().getChrom(), begin, begin + length, rc));
    } else {
      log.warn("skipping fragment of length " + length + " from being shotguned from reference " + ref_frag.getLocus().getChrom() + ". Consider fragment mode.");
    }
    return null;
  }

}
