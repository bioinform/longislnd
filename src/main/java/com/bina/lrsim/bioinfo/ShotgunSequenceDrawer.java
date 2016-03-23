package com.bina.lrsim.bioinfo;

import java.util.ArrayList;
import java.util.List;

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
  final List<Long> refCdf = new ArrayList<>();

  public ShotgunSequenceDrawer(final String filename) {
    super(filename);
    long numBases = 0;
    for (String entry : name) {
      numBases += get(entry).getSeq().length;
      refCdf.add(numBases);
    }
    log.info("length cdf: " + Joiner.on(" ").join(refCdf));
  }

  @Override
  protected Fragment getSequenceImpl(int length, RandomGenerator gen) {
    final boolean rc = gen.nextBoolean();
    final long numBases = refCdf.get(refCdf.size() - 1);
    final long pos = (numBases <= Integer.MAX_VALUE) ? gen.nextInt((int) numBases) : (gen.nextLong() % numBases + numBases) % numBases;

    int refIdx = 0;
    for (; refIdx < refCdf.size() && pos >= refCdf.get(refIdx); ++refIdx) {}

    final Fragment refFrag = get(refIdx);
    final byte[] refSeq = refFrag.getSeq();

    if (length <= refSeq.length) {
      int begin = gen.nextInt(refSeq.length - length + 1);
      int nCount = 0;
      final byte[] sequence = new byte[length];
      if (rc) {
        for (int ss = 0, cc = refSeq.length - 1 - begin; ss < length; ++ss, --cc) {
          sequence[ss] = EnumBP.ascii_rc(refSeq[cc]);
          if (EnumBP.ascii2value(sequence[ss]) == EnumBP.N.value) {
            nCount++;
          }
        }
        // shift begin/end back to forward strain's coordinate to describe locus
        begin = refSeq.length - begin - length;
      } else {
        for (int ss = 0; ss < length; ++ss) {
          sequence[ss] = refSeq[begin + ss];
          if (EnumBP.ascii2value(sequence[ss]) == EnumBP.N.value) {
            nCount++;
          }
        }
      }
      if (length * Heuristics.MAX_N_FRACTION_ON_READ < nCount) { return null; }
      // return new KmerIterator(get(ref_idx),ref_pos,ref_pos+length,leftFlank,rightFlank, rc);
      // return new HPIterator(get(ref_idx), ref_pos, ref_pos + length, leftFlank, rightFlank, getHpAnchor, rc);
      return new Fragment(sequence, new Locus(refFrag.getLocus().getChrom(), begin, begin + length, rc));
    } else {
      log.warn("skipping fragment of length " + length + " from being shotguned from reference " + refFrag.getLocus().getChrom() + ". Consider fragment mode.");
    }
    return null;
  }

}
