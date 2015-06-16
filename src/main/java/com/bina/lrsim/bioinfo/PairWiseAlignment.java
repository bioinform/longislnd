package com.bina.lrsim.bioinfo;

import org.apache.log4j.Logger;

/**
 * Created by bayo on 6/15/15.
 */
public class PairwiseAlignment {

  private final static Logger log = Logger.getLogger(PairwiseAlignment.class.getName());

  private final byte[] ref_;
  private final byte[] seq_;
  private final int[] seq_data_idx_; // the index in raw data corresponding at this seq_ index
  private boolean spanned_ = false;

  public PairwiseAlignment(byte[] ref_, byte[] seq_, int[] seq_data_idx_) {
    this.ref_ = ref_;
    this.seq_ = seq_;
    this.seq_data_idx_ = seq_data_idx_;
  }

  private static void spanLeftOnMatching(final byte[] fixed, final byte[] movable, final int[] indices) {
    if (fixed == movable) return;
    int left_most_target = 0;
    for (int pos = 0; pos < movable.length; ++pos) {
      final byte base = movable[pos];
      if (base == EnumBP.Gap.ascii || base == EnumBP.N.ascii || base == 'n') continue;
      int left_most_match = pos;
      for (int left_candidate = left_most_match - 1; left_candidate >= left_most_target && movable[left_candidate] == EnumBP.Gap.ascii; --left_candidate) {
        if (base == fixed[left_candidate]) {
          left_most_match = left_candidate;
        }
      }
      if (left_most_match != pos) {
        movable[pos] = EnumBP.Gap.ascii;
        movable[left_most_match] = base;
        if (null != indices) {
          indices[left_most_match] = indices[pos];
          indices[pos] = -1;
        }
        // if a base has been shifted left, make sure the next base, if already matching, won't get shifted right next to it
        if (pos + 1 < movable.length && movable[pos + 1] != EnumBP.Gap.ascii && movable[pos + 1] == fixed[pos + 1]) {
          left_most_target = left_most_match + 2;
        } else {
          left_most_target = left_most_match + 1;
        }
      }
    }
  }

  public byte getRef(int index) {
    return ref_[index];
  }

  public byte getSeq(int index) {
    return seq_[index];
  }

  public int getSeqDataIndex(int index) {
    return seq_data_idx_[index];
  }

  public int size() {
    return ref_.length;
  }

  public void span() {
    if (!spanned_) {
      spanAlignment();
      spanned_ = true;
    }
  }

  private void spanAlignment(/* int min_length */) {
    // log.info(new String(seq_));
    // log.info(new String(ref_));
    if (Heuristics.SPAN_LEFT_ON_MATCHES) {
      // try to push deletions to the right, also spread out deletions
      spanLeftOnMatching(ref_, seq_, seq_data_idx_);
      // try to push insertions to the right, also spread out insertions
      spanLeftOnMatching(seq_, ref_, null);
    }
    if (Heuristics.SPAN_RIGHT_ON_MISMATCHES) spanRightOnMismatch();
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
    for (int pos = 2; pos < ref_.length - 2; ++pos) {
      final byte last2_base = ref_[pos - 2];
      final byte last_base = ref_[pos - 1];
      final byte base = ref_[pos];
      final byte next_base = ref_[pos + 1];
      final byte next2_base = ref_[pos + 2];
      if (base == EnumBP.Gap.ascii && seq_[pos] != EnumBP.Gap.ascii) {
        // AC G --> AC G
        // A TG --> AT G
        // p
        // CC T --> CC T
        // C TT --> C TT
        if (last_base != EnumBP.Gap.ascii && last_base == seq_[pos - 1] && seq_[pos] != last_base && next_base != EnumBP.Gap.ascii && next_base != next2_base && seq_[pos + 1] == EnumBP.Gap.ascii && next2_base != EnumBP.Gap.ascii && next2_base == seq_[pos + 2]) {
          ref_[pos] = next_base;
          ref_[pos + 1] = EnumBP.Gap.ascii;
        }
        // A CG --> A CG
        // AT G --> A TG
        // p
        // C TT --> C TT
        // CC T --> CC T
        else if (last2_base != EnumBP.Gap.ascii && last2_base == seq_[pos - 2] && last_base != EnumBP.Gap.ascii && last_base != last2_base && seq_[pos - 1] == EnumBP.Gap.ascii && seq_[pos] != next_base && next_base != EnumBP.Gap.ascii && next_base == seq_[pos + 1]) {
          ref_[pos - 1] = EnumBP.Gap.ascii;
          ref_[pos] = last_base;
        }
      }
    }
  }

  private void spanRightOnMismatch() {
    for (int pos = ref_.length - 1; pos >= 0; --pos) {
      final byte base = ref_[pos];
      if (base == seq_[pos] || base == EnumBP.Gap.ascii || base == EnumBP.N.ascii || base == 'n') continue;
      int right_most_match = pos;
      int right_most_gap = pos;
      for (int right_canadidate = right_most_match + 1; right_canadidate < ref_.length && ref_[right_canadidate] == EnumBP.Gap.ascii; ++right_canadidate) {
        if (base == seq_[right_canadidate] || EnumBP.Gap.ascii == seq_[right_canadidate]) {
          right_most_match = right_canadidate;
        }
        right_most_gap = right_canadidate;
      }

      if (right_most_match != pos) {
        ref_[pos] = EnumBP.Gap.ascii;
        ref_[right_most_match] = base;
      } else if (right_most_gap != pos) {
        ref_[pos] = EnumBP.Gap.ascii;
        ref_[right_most_gap] = base;
      }
    }
  }
}
