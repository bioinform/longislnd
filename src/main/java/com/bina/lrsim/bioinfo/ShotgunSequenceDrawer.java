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
    for (String entry : name_) {
      num_bases += get(entry).length;
      ref_cdf_.add(num_bases);
    }
    log.info("length cdf: " + Joiner.on(" ").join(ref_cdf_));
  }

  @Override
  protected byte[] getSequenceImpl(int length, RandomGenerator gen) {
    final boolean rc = gen.nextBoolean();
    final long num_bases = ref_cdf_.get(ref_cdf_.size() - 1);
    final long pos = (num_bases <= Integer.MAX_VALUE) ? gen.nextInt((int) num_bases) : gen.nextLong() % num_bases;

    int ref_idx = 0;
    for (; ref_idx < ref_cdf_.size() && pos >= ref_cdf_.get(ref_idx); ++ref_idx) {}
    final int ref_pos = (0 == ref_idx) ? (int) pos : (int) (pos - ref_cdf_.get(ref_idx - 1));

    if (ref_pos + length <= get(ref_idx).length) {
      int number_of_n = 0;
      final byte[] chromosome = get(ref_idx);
      final byte[] sequence = new byte[length];
      if (rc) {
        for (int ss = 0, cc = chromosome.length - 1 - ref_pos; ss < length; ++ss, --cc) {
          sequence[ss] = EnumBP.ascii_rc(chromosome[cc]);
          if (sequence[ss] == 'N' || sequence[ss] == 'n') {
            ++number_of_n;
          }
        }
      } else {
        for (int ss = 0; ss < length; ++ss) {
          sequence[ss] = chromosome[ref_pos + ss];
          if (sequence[ss] == 'N' || sequence[ss] == 'n') {
            ++number_of_n;
          }
        }
      }
      if (length * Heuristics.MAX_N_FRACTION_ON_READ < number_of_n) { return null; }
      // return new KmerIterator(get(ref_idx),ref_pos,ref_pos+length,leftFlank,rightFlank, rc);
      // return new HPIterator(get(ref_idx), ref_pos, ref_pos + length, leftFlank, rightFlank, hp_anchor, rc);
      return sequence;
    }
    return null;
  }

}
