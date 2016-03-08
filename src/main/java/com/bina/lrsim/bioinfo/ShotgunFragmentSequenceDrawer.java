package com.bina.lrsim.bioinfo;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.log4j.Logger;

/**
 * Created by bayo on 5/7/15.
 *
 * This class is obvious hacked up and need some clean up
 */
public class ShotgunFragmentSequenceDrawer extends ReferenceSequenceDrawer {
  private final static Logger log = Logger.getLogger(ShotgunFragmentSequenceDrawer.class.getName());
  final List<Long> refBases = new ArrayList<Long>();

  public ShotgunFragmentSequenceDrawer(String filename) {
    super(filename);
    for (String entry : name_) {
      refBases.add((long) get(entry).getSeq().length);
    }
  }

  @Override
  protected Fragment getSequenceImpl(int length, RandomGenerator gen) {
    final boolean rc = gen.nextBoolean();
    List<Long> cdf = new ArrayList<Long>();
    long numFrag = 0;
    for (Long entry : refBases) {
      numFrag += (length > entry) ? 1 : (entry / length);
      cdf.add(numFrag);
    }
    final long pos = (numFrag <= Integer.MAX_VALUE) ? gen.nextInt((int) numFrag) : (gen.nextLong() % numFrag + numFrag) % numFrag;

    int ref_idx = 0;
    for (; ref_idx < cdf.size() && pos >= cdf.get(ref_idx); ++ref_idx) {}

    final Fragment ref_frag = get(ref_idx);
    final byte[] ref_seq = ref_frag.getSeq();

    int begin = 0;
    if (length > ref_seq.length) {
      length = ref_seq.length;
    } else {
      begin = gen.nextInt(ref_seq.length - length + 1);
    }

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
  }

}
