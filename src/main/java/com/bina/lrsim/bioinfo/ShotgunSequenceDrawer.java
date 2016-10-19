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
  final List<Long> cumulativeReferenceChromosomeSizes = new ArrayList<>();

  public ShotgunSequenceDrawer(final String filename) {
    super(filename);
    long numBases = 0;
    for (String entry : getNames()) {
      numBases += get(entry).getSeq().length;
      cumulativeReferenceChromosomeSizes.add(numBases);
    }
    log.info("cumulative reference chromosome sizes: " + Joiner.on(" ").join(cumulativeReferenceChromosomeSizes));
  }

  @Override
  protected Fragment drawRandomSequence(int length, RandomGenerator randomNumberGenerator) {
    final boolean needReverseComplement = randomNumberGenerator.nextBoolean();
    final long totalReferenceSize = cumulativeReferenceChromosomeSizes.get(cumulativeReferenceChromosomeSizes.size() - 1);
    //(randomNumberGenerator.nextLong() % totalReferenceSize + totalReferenceSize) % totalReferenceSize == randomNumberGenerator.nextLong() % totalReferenceSize?
    //TODO: simplify pos calculation
    final long randomPosition = (totalReferenceSize <= Integer.MAX_VALUE) ? randomNumberGenerator.nextInt((int) totalReferenceSize) : (randomNumberGenerator.nextLong() % totalReferenceSize + totalReferenceSize) % totalReferenceSize;

    int referenceChromosomeIndex = 0;
    //locate the first chromosome where cumulative lengths with all previous chromosomes is larger than randomPosition
    while (referenceChromosomeIndex < cumulativeReferenceChromosomeSizes.size() && randomPosition >= cumulativeReferenceChromosomeSizes.get(referenceChromosomeIndex)) {
      ++referenceChromosomeIndex;
    }

    final Fragment referenceFragment = get(referenceChromosomeIndex);
    final byte[] referenceSequence = referenceFragment.getSeq();

    if (length <= referenceSequence.length) {
      int begin = randomNumberGenerator.nextInt(referenceSequence.length - length + 1);
      int nCount = 0;
      final byte[] sampledSequence = new byte[length];
      if (needReverseComplement) {
        for (int ss = 0, cc = referenceSequence.length - 1 - begin; ss < length; ++ss, --cc) {
          sampledSequence[ss] = EnumBP.ascii_rc(referenceSequence[cc]);
          if (EnumBP.ascii2value(sampledSequence[ss]) == EnumBP.N.value) {
            nCount++;
          }
        }
        // shift begin/end back to forward strain's coordinate to describe locus
        begin = referenceSequence.length - begin - length;
      } else {
        for (int ss = 0; ss < length; ++ss) {
          sampledSequence[ss] = referenceSequence[begin + ss];
          if (EnumBP.ascii2value(sampledSequence[ss]) == EnumBP.N.value) {
            nCount++;
          }
        }
      }
      if (length * Heuristics.MAX_N_FRACTION_ON_READ < nCount) { return null; }
      // return new KmerIterator(get(ref_idx),ref_pos,ref_pos+length,leftFlank,rightFlank, rc);
      // return new HPIterator(get(ref_idx), ref_pos, ref_pos + length, leftFlank, rightFlank, getHpAnchor, rc);
      return new Fragment(sampledSequence, new Locus(referenceFragment.getLocus().getChrom(), begin, begin + length, needReverseComplement));
    } else {
      log.warn("skipping fragment of length " + length + " from being shotguned from reference " + referenceFragment.getLocus().getChrom() + ". Consider fragment mode.");
    }
    return null;
  }

}
