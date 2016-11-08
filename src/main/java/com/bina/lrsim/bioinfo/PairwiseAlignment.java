package com.bina.lrsim.bioinfo;

import org.apache.log4j.Logger;

/**
 * Created by bayo on 6/15/15.
 */
public class PairwiseAlignment {

  private final static Logger log = Logger.getLogger(PairwiseAlignment.class.getName());

  private final byte[] ref;
  private final byte[] seq;
  private final int[] seqDataIdx; // the index in raw data corresponding at this seq_ index
  private boolean spanned = false;

  public PairwiseAlignment(byte[] ref, byte[] seq, int[] seqDataIdx) {
    this.ref = ref;
    this.seq = seq;
    this.seqDataIdx = seqDataIdx;
  }

  /**
   *
   * @param fixed
   * @param movable
   * @param indices
   * @param leftMostTarget inclusive left boundary
   * @param rightMostTarget exclusive right boundary
   */
  private static void spanLeftOnMatching(final byte[] fixed, final byte[] movable, final int[] indices, int leftMostTarget, int rightMostTarget) {
    if (fixed == movable) return;
    for (int pos = leftMostTarget; pos < rightMostTarget; ++pos) {
      final byte base = movable[pos];
      //skip gaps/spaces (could be insertions/deletions)
      if (base == EnumBP.Gap.ascii || EnumBP.ascii2value(base) == EnumBP.N.value) continue;
      int leftMostMatch = pos;
      for (int leftCandidate = leftMostMatch - 1; leftCandidate >= leftMostTarget && movable[leftCandidate] == EnumBP.Gap.ascii; --leftCandidate) {
        if (base == fixed[leftCandidate]) {
          leftMostMatch = leftCandidate;
        }
      }
      /* an example of what this method is doing
      fixed:    CCGAACGAA--
      movable:  CC----GAATT

      ||
      \/
      fixed:    CCGAACGAA--
      movable:  CCG----AATT
      the match G is moved to the leftmost position by skipping some gaps

      ||
      \/
      fixed:    CCGAACGAA--
      movable:  CCG-A---ATT
      the match A which was next to G will be moved to the left, but a
      spacing is kept between two adjacent moved matches. as a result,
      gaps will be dispersed rather than clustered. The purpose is to
      avoid alignment bias.

       */
      if (leftMostMatch != pos) {
        movable[pos] = EnumBP.Gap.ascii;
        movable[leftMostMatch] = base;
        if (null != indices) {
          indices[leftMostMatch] = indices[pos];
          indices[pos] = -1;
        }
        // if a base has been shifted left, make sure the next base, if already matching, won't get shifted right next to it
        if (pos + 1 < rightMostTarget && movable[pos + 1] != EnumBP.Gap.ascii && movable[pos + 1] == fixed[pos + 1]) {
          leftMostTarget = leftMostMatch + 2;
        } else {
          leftMostTarget = leftMostMatch + 1;
        }
      }
    }
  }

  public byte getRef(int index) {
    return ref[index];
  }

  public byte getSeq(int index) {
    return seq[index];
  }

  public int getSeqDataIndex(int index) {
    return seqDataIdx[index];
  }

  public int size() {
    return ref.length;
  }

  public void span() {
    if (!spanned) {
      spanAlignment();
      spanned = true;
    }
  }

  private void spanAlignment(/* int min_length */) {
    // if there are soft-clips, the left and right ends will have long stretches of non-gap seq
    // the boundaries prevent spanning outside of the first and last non-gap ref
    int leftBoundary = 0;
    // locate the last upstream softclipped position in reference
    for (; leftBoundary < size() && ref[leftBoundary] == EnumBP.Gap.ascii; ++leftBoundary) {}
    int rightBoundary = size();
    // locate the first downstream softclipped position in reference
    for (; rightBoundary > 0 && ref[rightBoundary - 1] == EnumBP.Gap.ascii; --rightBoundary) {}
    // log.info(new String(seq_));
    // log.info(new String(ref_));
    if (Heuristics.SPAN_LEFT_ON_MATCHES) {
      // try to push deletions to the right, also spread out deletions
      spanLeftOnMatching(ref, seq, seqDataIdx, leftBoundary, rightBoundary);
      // try to push insertions to the right, also spread out insertions
      spanLeftOnMatching(seq, ref, null, leftBoundary, rightBoundary);
    }
    if (Heuristics.SPAN_RIGHT_ON_MISMATCHES) spanRightOnMismatch(leftBoundary, rightBoundary);
    if (Heuristics.MERGE_INDELS) mergeIndels();
    // log.info(new String(seq_));
    // log.info(new String(ref_));
    /*
     * final int length = ref_.length;
     * 
     * for (int pos = 0; pos < length;) { final byte base = ref_[pos];
     * 
     * int next_diff = pos + 1;
     * 
     * if (base != EnumBP.Gap.ascii && base == seq_[pos] && base != EnumBP.N.ascii && base != 'n') { int hp_length = 1; for (; next_diff < length &&
     * (ref_[next_diff] == EnumBP.Gap.ascii || ref_[next_diff] == base); ++next_diff) { if (ref_[next_diff] == base) { ++hp_length; } }
     * 
     * if (hp_length >= min_length) { int left_most = pos; for (; left_most > 0 && ref_[left_most - 1] == EnumBP.Gap.ascii && seq_[left_most - 1] == base;
     * --left_most) {} if (left_most != pos) { ref_[pos] = EnumBP.Gap.ascii; ref_[left_most] = base; } } }
     * 
     * pos = next_diff; }
     */
  }

  private void mergeIndels() {
    for (int pos = 2; pos < ref.length - 2; ++pos) {
      final byte last2Base = ref[pos - 2];
      final byte lastBase = ref[pos - 1];
      final byte base = ref[pos];
      final byte nextBase = ref[pos + 1];
      final byte next2Base = ref[pos + 2];
      if (base == EnumBP.Gap.ascii && seq[pos] != EnumBP.Gap.ascii) {
        /*
        an example for the change
                   *
        // seq:   AC G --> AC G
        // ref:   A TG --> AT G
        net effect is changing one insertion + one deletion to one mismatch
        // p
        // seq:   CC T --> CC T
        // ref:   C TT --> C TT
        note here seq[pos] != lastBase && nextBase != next2Base
        the second case is a situation where indels will not be merged, the reason is not merging indels in homopolymers.
        */
        if (lastBase != EnumBP.Gap.ascii && lastBase == seq[pos - 1] && seq[pos] != lastBase && nextBase != EnumBP.Gap.ascii && nextBase != next2Base && seq[pos + 1] == EnumBP.Gap.ascii && next2Base != EnumBP.Gap.ascii && next2Base == seq[pos + 2]) {
          ref[pos] = nextBase;
          ref[pos + 1] = EnumBP.Gap.ascii;
        }
        /*
        // an example for the change
                    *
        // seq:   A CG --> A CG
        // ref:   AT G --> A TG
        net effect is changing one insertion + one deletion to one mismatch
        // p
        // seq:   C TT --> C TT
        // ref:   CC T --> CC T
        // note here last2Base == seq[pos - 2] && lastBase != last2Base
        */
        else if (last2Base != EnumBP.Gap.ascii && last2Base == seq[pos - 2] && lastBase != EnumBP.Gap.ascii && lastBase != last2Base && seq[pos - 1] == EnumBP.Gap.ascii && seq[pos] != nextBase && nextBase != EnumBP.Gap.ascii && nextBase == seq[pos + 1]) {
          ref[pos - 1] = EnumBP.Gap.ascii;
          ref[pos] = lastBase;
        }
      }
    }
  }

  private void spanRightOnMismatch(int leftMostTarget, int rightMostTarget) {
    for (int pos = rightMostTarget - 1; pos >= leftMostTarget; --pos) {
      final byte base = ref[pos];
      if (base == seq[pos] || base == EnumBP.Gap.ascii || EnumBP.ascii2value(base) == EnumBP.N.value) continue;
      int rightMostMatch = pos;
      int rightMostGap = pos;
      for (int rightCanadidate = rightMostMatch + 1; rightCanadidate < rightMostTarget && ref[rightCanadidate] == EnumBP.Gap.ascii; ++rightCanadidate) {
        if (base == seq[rightCanadidate] || EnumBP.Gap.ascii == seq[rightCanadidate]) {
          rightMostMatch = rightCanadidate;
        }
        rightMostGap = rightCanadidate;
      }

      /*
      if we can find a match, then we right shift the matched base
      if we can't we right shift to the last gap
       */
      if (rightMostMatch != pos) {
        ref[pos] = EnumBP.Gap.ascii;
        ref[rightMostMatch] = base;
      } else if (rightMostGap != pos) {
        ref[pos] = EnumBP.Gap.ascii;
        ref[rightMostGap] = base;
      }
    }
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(new String(this.seq));
    sb.append('\n');
    sb.append(new String(this.ref));
    sb.append('\n');
    return sb.toString();
  }
}
