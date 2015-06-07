package com.bina.lrsim.h5.cmp;

/**
 * Created by bayo on 5/2/15.
 */

import java.util.Arrays;
import java.util.Iterator;

import org.apache.log4j.Logger;

import com.bina.lrsim.LRSim;
import com.bina.lrsim.bioinfo.Context;
import com.bina.lrsim.bioinfo.EnumBP;
import com.bina.lrsim.bioinfo.Heuristics;
import com.bina.lrsim.bioinfo.Kmerizer;
import com.bina.lrsim.h5.pb.BaseCalls;
import com.bina.lrsim.h5.pb.EnumDat;
import com.bina.lrsim.h5.pb.PBReadBuffer;
import com.bina.lrsim.h5.pb.PBSpec;
import com.bina.lrsim.interfaces.EventGroup;
import com.bina.lrsim.simulator.EnumEvent;
import com.bina.lrsim.simulator.Event;

public class CmpH5Alignment implements EventGroup {

  /**
   * @param left_flank number of bp before the position of interest
   * @param right_flank number of bp after the position of interest
   * @param left_mask omit this number of bases in the begining
   * @param right_mask omit this number of bases in the end
   * @paran hp_anchor use this many bases on both ends of homopolymer
   * @return an iterator of events associated with this alignment instance
   */
  @Override
  public Iterator<Event> getEventIterator(int left_flank, int right_flank, int left_mask, int right_mask, int hp_anchor) {
    if (!spanned_) {
      this.spanAlignment(1);
      spanned_ = true;
    }
    return new EventIterator(left_flank, right_flank, left_mask, right_mask, hp_anchor);
  }

  private class EventIterator implements Iterator<Event> {
    // sometimes two events (kmer and homopolymer) would be generated at a location next_, this
    // pointer stores
    // the event not returned by the first call of next();
    private Event extra_ = null;
    private int lf_; // left flank
    private int rf_; // right flank
    private int hp_anchor_; // homopolymer anchor length
    private int next_;
    private int end_;
    private byte[] key_;

    public EventIterator(int left_flank, int right_flank, int left_mask, int right_mask, int hp_anchor) {
      lf_ = left_flank;
      rf_ = right_flank;
      hp_anchor_ = hp_anchor;

      // mask out right flank
      end_ = ref_.length - 1;
      for (int count = 0; end_ >= 0; --end_) {
        if (EnumBP.Gap.ascii != seq_[end_]) {
          if (++count == right_mask) break;
        }
      }
      --end_;
      // end_ is now one before the position at which right_mask bp appeared on seq_
      for (int count = 0; end_ >= 0; --end_) {
        if (EnumBP.Gap.ascii != ref_[end_]) {
          if (++count == right_flank + 1) break;
        }
      }
      ++end_;

      // mask out left flank
      next_ = 0;
      for (int count = 0; next_ < ref_.length; ++next_) {
        if (EnumBP.Gap.ascii != seq_[next_]) {
          if (++count == left_mask) break;
        }
      }
      ++next_;
      for (int count = 0; next_ < ref_.length; ++next_) {
        if (EnumBP.Gap.ascii != ref_[next_]) {
          if (++count == left_flank + 1) break;
        }
      }

      // build key around the next_ position
      key_ = new byte[left_flank + rf_ + 1];
      key_[left_flank] = ref_[next_];
      for (int pos = next_ + 1, k = left_flank + 1; k < key_.length; ++pos) {
        if (EnumBP.Gap.ascii != ref_[pos]) {
          key_[k++] = ref_[pos];
        }
      }

      for (int pos = next_ - 1, k = left_flank - 1; k >= 0; --pos) {
        if (EnumBP.Gap.ascii != ref_[pos]) {
          key_[k--] = ref_[pos];
        }
      }
    }

    @Override
    public boolean hasNext() {
      return (null != extra_) || (next_ < end_);
    }

    @Override
    public Event next() {
      if (null != extra_) {
        Event ret = extra_;
        extra_ = null;
        return ret;
      }
      boolean valid = true; // various conditions must be met to return this as an event to be considered

      BaseCalls bc = new BaseCalls(spec); // this is probably a memory bound block killer
      EnumEvent event = null;

      if (ref_[next_] == EnumBP.Gap.ascii) {
        log.info("alignment data can't be parsed properly");
        throw new RuntimeException("alignment data can't be parsed properly");
      }
      if (ref_[next_ + 1] == EnumBP.Gap.ascii) {
        bc.reserve(10);
        if (seq_[next_] != EnumBP.Gap.ascii) {
          fillbase(bc, next_);
        }
        for (int ins = next_ + 1; ref_[ins] == EnumBP.Gap.ascii; ++ins) {
          // this is a hack to accommodate spurious gap-to-gap alignment in pbalign's output
          if (seq_[ins] != EnumBP.Gap.ascii) {
            fillbase(bc, ins);
          } else {
            // throw new RuntimeException("gap-vs-gap alignment");
          }
        }
        if (bc.size() == 0) {
          event = EnumEvent.DELETION;
        } else if (bc.size() == 1) {
          if(bc.get(0,EnumDat.BaseCall) == ref_[next_]){
            event = EnumEvent.MATCH;
          } else {
            event = EnumEvent.SUBSTITUTION;
          }
        } else {
          event = EnumEvent.INSERTION;
          if (bc.size() - 1 > Heuristics.MAX_INS_BP) {
            valid = false;
          }
          else if (bc.get(0,EnumDat.BaseCall) != ref_[next_] || bc.get(1,EnumDat.BaseCall) != ref_[next_]) {
//            log.info(new String(Arrays.copyOfRange(seq_,next_-10,next_+11)));
//            log.info(new String(Arrays.copyOfRange(ref_,next_-10,next_+11)));
//            log.info("      "+new String(key_));
          }
        }
      } else if (seq_[next_] == EnumBP.Gap.ascii) {
        event = EnumEvent.DELETION;
      } else if (seq_[next_] == ref_[next_]) {
        event = EnumEvent.MATCH;
        fillbase(bc, next_);
      } else if (seq_[next_] != ref_[next_]) {
        event = EnumEvent.SUBSTITUTION;
        fillbase(bc, next_);
      } else {
        throw new RuntimeException("parsing error");
      }

      // this is a hack to accommodate spurious gap-to-gap alignment in pbalign's output
      if (event.equals(EnumEvent.INSERTION) && bc.size() == 0) {
        valid = false;
      }

      for (byte entry : key_) {
        if (EnumBP.N.ascii == entry) {
          valid = false;
          break;
        }
      }

      // kmer to return
      final int kmer = (valid) ?  Kmerizer.fromASCII(key_) : -1;

      if (valid) {
        extra_ = constructHPEvent(next_, lf_, rf_, hp_anchor_);
      }

      step();

      if (valid) {
        return new Event(new Context(kmer, 1), event, bc);
      } else {
        return null;
      }

    }

    private Event constructHPEvent(int start, int left_flank, int right_flank, int anchor) {
      // we don't look at the middle of homopolymer
      if (ref_[start] == ref_[start - 1]) return null;
      byte[] tmp = new byte[anchor + 1 + anchor];
      int kk = 0;

      // make sure the left flank is "intact"
      for (int pos = start - anchor; pos < start; ++pos) {
        if (ref_[pos] != EnumBP.N.ascii && ref_[pos] != EnumBP.Gap.ascii && seq_[pos] == ref_[pos]) {
          tmp[kk++] = ref_[pos];
        } else {
          return null;
        }
      }

      tmp[kk++] = ref_[start];

      // look for the next different base
      int next_diff = start + 1;
      int hp_length = 1;
      for (; next_diff < ref_.length && (ref_[next_diff] == ref_[start] || ref_[next_diff] == EnumBP.Gap.ascii); ++next_diff) {
        if (ref_[next_diff] == ref_[start]) {
          ++hp_length;
        }
      }

      // homopolymer sampling is not needed if it's <= the flanking bases
      if (hp_length <= left_flank && hp_length <= right_flank) { return null; }

      // make sure the right flank is "intact"
      for (int pos = next_diff; kk < tmp.length && pos < ref_.length; ++pos) {
        if (ref_[pos] != EnumBP.N.ascii && ref_[pos] != EnumBP.Gap.ascii && seq_[pos] == ref_[pos]) {
          tmp[kk++] = ref_[pos];
        } else {
          return null;
        }
      }
      if (kk != tmp.length) { return null; }

      BaseCalls bc = new BaseCalls(spec);
      try {
        for (int pos = start; pos < next_diff; ++pos) {
          if (seq_[pos] != EnumBP.Gap.ascii) {
            fillbase(bc, pos);
          }
        }
      } catch (Exception e) {
        log.info(e, e);
        return null;
      }

      EnumEvent ev;
      if (bc.size() < hp_length) {
        ev = EnumEvent.DELETION;
      } else if (bc.size() > hp_length) {
        ev = EnumEvent.INSERTION;
      } else {
        boolean same = true;
        for (int ii = 0; ii < bc.size(); ++ii) {
          if (bc.get(ii, EnumDat.BaseCall) != ref_[start]) {
            same = false;
          }
        }
        if (same) {
          ev = EnumEvent.MATCH;
        } else {
          ev = EnumEvent.SUBSTITUTION;
        }

      }
      /*
       * { StringBuilder sb = new StringBuilder(); sb.append("homopolymer " +start + " " + next_diff+" " + anchor+"\n"); for(int pos = start - anchor ; pos <
       * next_diff+anchor; ++pos) { sb.append((char)seq_[pos]); } sb.append("\n"); for(int pos = start - anchor ; pos < next_diff+anchor; ++pos) {
       * sb.append((char)ref_[pos]); } sb.append("\n"); for(int pos = 0; pos<bc.size(); ++pos){ sb.append((char)bc.get(pos,EnumDat.BaseCall)); } sb.append(" ");
       * for(int pos = start - anchor ; pos < start; ++pos) { sb.append((char)ref_[pos]); } sb.append(" "); for(int pos = next_diff ; pos < next_diff+anchor;
       * ++pos) { sb.append((char)ref_[pos]); } sb.append("\n"); sb.append(Arrays.toString(tmp)+" "+ hp_length); log.info(sb.toString());
       * 
       * }
       */

      return new Event(new Context(Kmerizer.fromASCII(tmp), hp_length), ev, bc);
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException("cannot remove elements");
    }

    private void fillbase(BaseCalls bc, int index) {
      final int begin = index_[EnumIdx.offset_begin.value];
      int loc_idx = bc.size();
      bc.push_back();
      bc.set(loc_idx, EnumDat.BaseCall, seq_[index]);
      for (EnumDat ed : spec.getNonBaseDataSet()) {
        bc.set(loc_idx, ed, data_.get(ed)[begin + index]);
      }
    }

    private void step() {
      // set next_ to next value position
      for (++next_; next_ < end_; ++next_) {
        if (EnumBP.Gap.ascii != ref_[next_]) {
          break;
        }
      }

      if (next_ < end_) {
        // update key
        for (int ii = 0; ii < key_.length - 1; ++ii) {
          key_[ii] = key_[ii + 1];
        }
        int flank = next_ + 1;
        for (int count = 0; flank < ref_.length; ++flank) {
          if (EnumBP.Gap.ascii != ref_[flank]) {
            if (++count == rf_) break;
          }
        }
        key_[key_.length - 1] = ref_[flank];
      }
    }
  }

  public int[] aln() {
    return aln_;
  } // this is for diagnostic

  public byte[] ref() {
    return ref_;
  }

  public byte[] read() {
    return seq_;
  }

  public int aln_length() {
    return index_[EnumIdx.offset_end.value] - index_[EnumIdx.offset_begin.value];
  }

  @Override
  public int seq_length() {
    return index_[EnumIdx.rEnd.value] - index_[EnumIdx.rStart.value];
  }

  @Override
  public int ref_length() {
    return index_[EnumIdx.tEnd.value] - index_[EnumIdx.tStart.value];
  }

  public int aln_begin() {
    return EnumIdx.offset_begin.value;
  }

  public int aln_end() {
    return EnumIdx.offset_end.value;
  }

  public CmpH5Alignment(int[] index, AlnData data, PBSpec spec) {
    this.spec = spec;
    load(index, data);
  }

  public PBReadBuffer toSeqRead() {
    return toRead(seq_);
  }

  public PBReadBuffer toRefRead() {
    return toRead(ref_);
  }

  private PBReadBuffer toRead(byte[] ba) {
    final int begin = index_[EnumIdx.offset_begin.value];
    PBReadBuffer buffer = new PBReadBuffer(spec, aln_length());
    BaseCalls bc = new BaseCalls(spec, 1);
    for (int ii = 0; ii < aln_length(); ++ii) {
      if (ba[ii] != EnumBP.Gap.ascii) {
        bc.set(0, EnumDat.BaseCall, ba[ii]);
        for (EnumDat e : spec.getNonBaseDataSet()) {
          bc.set(0, e, data_.get(e)[begin + ii]);
        }
        buffer.addLast(bc);
      }
    }
    return buffer;
  }

  public void load(int[] index, AlnData data) {
    final int begin = index[EnumIdx.offset_begin.value];
    final int end = index[EnumIdx.offset_end.value];
    final int length = end - begin;
    byte[] ref_loc = new byte[length];
    byte[] seq_loc = new byte[length];
    int[] aln_loc = new int[length];

    byte[] aln = data.get(EnumDat.AlnArray);

    for (int ii = 0; ii < length; ++ii) {
      byte entry = aln[begin + ii];
      aln_loc[ii] = entry & 0xff;
      ref_loc[ii] = EnumBP.cmp2ref(entry).ascii;
      if (EnumBP.Invalid.value == ref_loc[ii]) throw new RuntimeException("bad ref char");
      seq_loc[ii] = EnumBP.cmp2seq(entry).ascii;
      if (EnumBP.Invalid.value == seq_loc[ii]) throw new RuntimeException("bad seq char");

      /*
       * this assert is to throw if there's a gap-to-gap alignment, which breaks some down stream pacbio tools, instead, the insertion code path is modified to
       * avoid the logging of gap-to-gap alignment
       */
      if (ref_loc[ii] == seq_loc[ii] && ref_loc[ii] == EnumBP.Gap.ascii) {
        // throw new RuntimeException("gap-to-gap alignment " + aln[ii]);
        log.warn("' '-to-' ' alignment found in cmp.h5 alignment:" + aln[ii]);
      }
    }

    index_ = index;
    data_ = data;
    ref_ = ref_loc;
    seq_ = seq_loc;
    aln_ = aln_loc;
  }

  private void spanAlignment(int min_length) {
//    log.info(new String(seq_));
//    log.info(new String(ref_));
    if (Heuristics.SPAN_LEFT_ON_MATCHES) spanLeftOnMatching();
    if (Heuristics.SPAN_RIGHT_ON_MISMATCHES) spanRightOnMismatch();
    if (Heuristics.MERGE_INDELS) mergeIndels();
//    log.info(new String(seq_));
//    log.info(new String(ref_));
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

  private void spanLeftOnMatching() {
    int left_most_target = 0;
    for (int pos = 0; pos < ref_.length; ++pos) {
      final byte base = ref_[pos];
      if (base == EnumBP.Gap.ascii || base == EnumBP.N.ascii || base == 'n') continue;
      int left_most_match = pos;
      for (int left_candidate = left_most_match - 1; left_candidate >= left_most_target && ref_[left_candidate] == EnumBP.Gap.ascii; --left_candidate) {
        if (base == seq_[left_candidate]) {
          left_most_match = left_candidate;
        }
      }
      if (left_most_match != pos) {
        ref_[pos] = EnumBP.Gap.ascii;
        ref_[left_most_match] = base;
        // if a reference base has been shifted left, make sure the next base, if already matching, won't get shifted right next to it
        if (pos + 1 < ref_.length && ref_[pos + 1] != EnumBP.Gap.ascii && ref_[pos + 1] == seq_[pos + 1]) {
          left_most_target = left_most_match + 2;
        } else {
          left_most_target = left_most_match + 1;
        }
      }
    }
  }

  private void mergeIndels() {
    for (int pos = 2; pos < ref_.length - 2; ++pos) {
      final byte last2_base = ref_[pos - 2];
      final byte last_base = ref_[pos - 1];
      final byte base = ref_[pos];
      final byte next_base = ref_[pos + 1];
      final byte next2_base = ref_[pos + 2];
      if (base == EnumBP.Gap.ascii) {
        // AC G  --> AC G
        // A TG  --> AT G
        //  p
        // CC T  --> CC T
        // C TT  --> C TT
        if (       last_base != EnumBP.Gap.ascii
                && last_base == seq_[pos - 1]
                && seq_[pos] != last_base
                && next_base != EnumBP.Gap.ascii
                && next_base != next2_base
                && seq_[pos+1] == EnumBP.Gap.ascii
                && next2_base != EnumBP.Gap.ascii
                && next2_base == seq_[pos+2]
                ) {
          ref_[pos] = next_base;
          ref_[pos + 1] = EnumBP.Gap.ascii;
        }
        // A CG  --> A CG
        // AT G  --> A TG
        //   p
        // C TT  --> C TT
        // CC T  --> CC T
        else if (  last2_base != EnumBP.Gap.ascii
                && last2_base == seq_[pos - 2]
                && last_base != EnumBP.Gap.ascii
                && last_base != last2_base
                && seq_[pos - 1] == EnumBP.Gap.ascii
                && seq_[pos] != next_base
                && next_base != EnumBP.Gap.ascii
                && next_base == seq_[pos + 1]) {
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

  private int[] index_ = null;
  private AlnData data_ = null;
  private boolean spanned_ = false;
  private final PBSpec spec;

  private int[] aln_ = null; // for diagnostic
  private byte[] ref_ = null;
  private byte[] seq_ = null;
  private final static Logger log = Logger.getLogger(LRSim.class.getName());
}
