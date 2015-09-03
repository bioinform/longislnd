package com.bina.lrsim.bioinfo;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.log4j.Logger;

import java.util.Arrays;

/**
 * Created by laub2 on 9/2/15.
 */
public class FragmentSequenceDrawer extends ReferenceSequenceDrawer {
  private final static Logger log = Logger.getLogger(FragmentSequenceDrawer.class.getName());

  public FragmentSequenceDrawer(String filename) {
    super(filename);
  }

  @Override
  protected byte[] getSequenceImpl(int length, RandomGenerator gen) {
    final boolean rc = gen.nextBoolean();

    // select a fragment with equal probability
    final byte[] fragment = get(gen.nextInt(name_.size()));

    // if fragment is shorter than indicated length, return the whole fragment
    final int begin, end;
    if (fragment.length <= length) {
      begin = 0;
      end = fragment.length;
    } else {
      // otherwise, assume that long fragment is sheared into such length
      begin = gen.nextInt(fragment.length - length + 1);
      end = begin + length;
    }
    if (rc) {
      final byte[] sequence = new byte[end - begin];
      for (int ss = 0, cc = fragment.length - 1 - begin; ss < sequence.length; ++ss, --cc) {
        sequence[ss] = EnumBP.ascii_rc(fragment[cc]);
      }
      return sequence;
    } else {
      return Arrays.copyOfRange(fragment, begin, end);
    }
  }
}
