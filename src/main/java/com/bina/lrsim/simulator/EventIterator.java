package com.bina.lrsim.simulator;

import java.util.Iterator;

import org.apache.log4j.Logger;

import com.bina.lrsim.bioinfo.Context;
import com.bina.lrsim.bioinfo.EnumBP;
import com.bina.lrsim.bioinfo.Heuristics;
import com.bina.lrsim.bioinfo.Kmerizer;
import com.bina.lrsim.h5.pb.BaseCalls;
import com.bina.lrsim.h5.pb.EnumDat;
import com.bina.lrsim.interfaces.EventGroup;

/**
 * Created by bayo on 6/16/15.
 */
public class EventIterator implements Iterator<Event> {
  private final static Logger log = Logger.getLogger(EventIterator.class.getName());
  private final EventGroup alignment_;
  private final int lf_; // left flank
  private final int rf_; // right flank
  private final int hp_anchor_; // homopolymer anchor length
  // sometimes two events (kmer and homopolymer) would be generated at a location next_, this
  // pointer stores
  // the event not returned by the first call of next();
  private Event extra_ = null;
  private int next_;
  private int end_;
  private byte[] key_;

  public EventIterator() {
    alignment_ = null;
    lf_ = -1;
    rf_ = -1;
    hp_anchor_ = -1;
    next_ = -1;
    end_ = next_;
  }

  public EventIterator(EventGroup alignment, int left_flank, int right_flank, int left_mask, int right_mask, int hp_anchor) {
    this.alignment_ = alignment;
    this.lf_ = left_flank;
    this.rf_ = right_flank;
    this.hp_anchor_ = hp_anchor;

    // mask out right side of read
    end_ = alignment_.size();
    for (int count = 0; count < right_mask && end_ > 0; --end_) {
      if (EnumBP.Gap.ascii != alignment_.getSeq(end_ - 1)) {
        ++count;
      }
    }
    // mask out right flank
    for (int count = 0; count < right_flank && end_ > 0; --end_) {
      if (EnumBP.Gap.ascii != alignment_.getRef(end_ - 1)) {
        ++count;
      }
    }

    // mask out left flank
    next_ = 0;
    for (int count = 0; count < left_mask && next_ < alignment_.size(); ++next_) {
      if (EnumBP.Gap.ascii != alignment_.getSeq(next_)) {
        ++count;
      }
    }
    for (int count = 0; count < left_flank && next_ < alignment_.size(); ++next_) {
      if (EnumBP.Gap.ascii != alignment_.getRef(next_)) {
        ++count;
      }
    }
    for (; next_ < alignment.size() && alignment_.getRef(next_) == EnumBP.Gap.ascii; ++next_) {}

    if (hasNext()) {
      // build key around the next_ position
      key_ = new byte[left_flank + rf_ + 1];
      key_[left_flank] = alignment_.getRef(next_);
      for (int pos = next_ + 1, k = left_flank + 1; k < key_.length; ++pos) {
        if (EnumBP.Gap.ascii != alignment_.getRef(pos)) {
          key_[k++] = alignment_.getRef(pos);
        }
      }

      for (int pos = next_ - 1, k = left_flank - 1; k >= 0; --pos) {
        if (EnumBP.Gap.ascii != alignment_.getRef(pos)) {
          key_[k--] = alignment_.getRef(pos);
        }
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

    BaseCalls bc = new BaseCalls(alignment_.getSpec()); // this is probably a memory bound block killer
    EnumEvent event = null;

    if (alignment_.getRef(next_) == EnumBP.Gap.ascii) {
      throw new RuntimeException("alignment data can't be parsed properly");
    }
    if (alignment_.getRef(next_ + 1) == EnumBP.Gap.ascii) {
      bc.reserve(10);
      if (alignment_.getSeq(next_) != EnumBP.Gap.ascii) {
        fillBase(bc, next_);
      }
      for (int ins = next_ + 1; alignment_.getRef(ins) == EnumBP.Gap.ascii; ++ins) {
        // this is a hack to accommodate spurious gap-to-gap alignment in pbalign's output
        if (alignment_.getSeq(ins) != EnumBP.Gap.ascii) {
          fillBase(bc, ins);
        } else {
          // throw new RuntimeException("gap-vs-gap alignment");
        }
      }
      if (bc.size() == 0) {
        event = EnumEvent.DELETION;
      } else if (bc.size() == 1) {
        if (bc.get(0, EnumDat.BaseCall) == alignment_.getRef(next_)) {
          event = EnumEvent.MATCH;
        } else {
          event = EnumEvent.SUBSTITUTION;
        }
      } else {
        event = EnumEvent.INSERTION;
        if (bc.size() - 1 > Heuristics.MAX_INS_BP) {
          valid = false;
        } else if (bc.get(0, EnumDat.BaseCall) != alignment_.getRef(next_) || bc.get(1, EnumDat.BaseCall) != alignment_.getRef(next_)) {
          // log.info(new String(Arrays.copyOfRange(seq_,next_-10,next_+11)));
          // log.info(new String(Arrays.copyOfRange(ref_,next_-10,next_+11)));
          // log.info("      "+new String(key_));
        }
      }
    } else if (alignment_.getSeq(next_) == EnumBP.Gap.ascii) {
      int next_call = next_;
      for (; next_call < alignment_.size() && alignment_.getSeq(next_call) == EnumBP.Gap.ascii; ++next_call) {}
      if(next_call < alignment_.size()) {
        fillBase(bc, next_call);
      }
      event = EnumEvent.DELETION;
    } else if (alignment_.getSeq(next_) == alignment_.getRef(next_)) {
      event = EnumEvent.MATCH;
      fillBase(bc, next_);
    } else if (alignment_.getSeq(next_) != alignment_.getRef(next_)) {
      event = EnumEvent.SUBSTITUTION;
      fillBase(bc, next_);
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
    final int kmer = (valid) ? Kmerizer.fromASCII(key_) : -1;

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
    if (alignment_.getRef(start) == alignment_.getRef(start - 1)) return null;
    byte[] tmp = new byte[anchor + 1 + anchor];
/*
    int kk = anchor - 1;
    for(int pos = start - 1; pos >=0 && kk >= 0; --pos) {
      byte base = alignment_.getRef(pos);
      if(base == EnumBP.N.ascii || base == 'n') {
        return null;
      }
      else if (base != EnumBP.Gap.ascii) {
        tmp[kk--] = base;
      }
    }
    tmp[anchor] = alignment_.getRef(start);
    if(tmp[anchor] == tmp[anchor-1]) return null;
    kk = anchor + 1;
    */
    int kk = 0;

    // make sure the left flank is "intact"
    for (int pos = start - anchor; pos < start; ++pos) {
      if (alignment_.getRef(pos) != EnumBP.N.ascii && alignment_.getRef(pos) != EnumBP.Gap.ascii && alignment_.getSeq(pos) == alignment_.getRef(pos)) {
        tmp[kk++] = alignment_.getRef(pos);
      } else {
        return null;
      }
    }

    tmp[kk++] = alignment_.getRef(start);

    // look for the next different base
    int next_diff = start + 1;
    int hp_length = 1;
    for (; next_diff < alignment_.size() && (alignment_.getRef(next_diff) == alignment_.getRef(start) || alignment_.getRef(next_diff) == EnumBP.Gap.ascii); ++next_diff) {
      if (alignment_.getRef(next_diff) == alignment_.getRef(start)) {
        ++hp_length;
      }
    }

    // homopolymer sampling is not needed if it's <= the flanking bases
    if (hp_length <= left_flank && hp_length <= right_flank) { return null; }
/*
    for (int pos = next_diff; kk < tmp.length && pos < alignment_.size(); ++pos) {
      byte base = alignment_.getRef(pos);
      if(base == EnumBP.N.ascii || base == 'n') {
        return null;
      }
      else if (base != EnumBP.Gap.ascii) {
        tmp[kk++] = base;
      }
    }
    */
    // make sure the right flank is "intact"
    for (int pos = next_diff; kk < tmp.length && pos < alignment_.size(); ++pos) {
      if (alignment_.getRef(pos) != EnumBP.N.ascii && alignment_.getRef(pos) != EnumBP.Gap.ascii && alignment_.getSeq(pos) == alignment_.getRef(pos)) {
        tmp[kk++] = alignment_.getRef(pos);
      } else {
        return null;
      }
    }
    if (kk != tmp.length) { return null; }

    BaseCalls bc = new BaseCalls(alignment_.getSpec());
    try {
      for (int pos = start; pos < next_diff; ++pos) {
        if (alignment_.getSeq(pos) != EnumBP.Gap.ascii) {
          fillBase(bc, pos);
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
        if (bc.get(ii, EnumDat.BaseCall) != alignment_.getRef(start)) {
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
    if (new String(tmp).equals("ATGTG") && hp_length == 6) {
      StringBuilder sb = new StringBuilder();
      sb.append("homopolymer " + start + " " + next_diff + " " + anchor + "\n");
      for (int pos = start - anchor; pos < next_diff + anchor; ++pos) {
        sb.append((char) alignment_.getSeq(pos));
      }
      sb.append("\n");
      for (int pos = start - anchor; pos < next_diff + anchor; ++pos) {
        sb.append((char) alignment_.getRef(pos));
      }
      sb.append("\n");
      for (int pos = 0; pos < bc.size(); ++pos) {
        sb.append((char) bc.get(pos, EnumDat.BaseCall));
      }
      sb.append(" ");
      for (int pos = start - anchor; pos < start; ++pos) {
        sb.append((char) alignment_.getRef(pos));
      }
      sb.append(" ");
      for (int pos = next_diff; pos < next_diff + anchor; ++pos) {
        sb.append((char) alignment_.getRef(pos));
      }
      sb.append("\n");
      sb.append(new String(tmp) + " " + hp_length);
      log.info(sb.toString());
    }
    */

    return new Event(new Context(Kmerizer.fromASCII(tmp), hp_length), ev, bc);
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException("cannot remove elements");
  }

  private void fillBase(BaseCalls bc, int index) {
    final int loc_idx = bc.size();
    final int seq_idx = alignment_.getSeqDataIndex(index);
    bc.push_back();
    bc.set(loc_idx, EnumDat.BaseCall, alignment_.getSeq(index));
    for (EnumDat ed : alignment_.getSpec().getNonBaseDataSet()) {
      bc.set(loc_idx, ed, alignment_.getData(ed, seq_idx));
    }
  }

  private void step() {
    // set next_ to next value position
    for (++next_; next_ < end_; ++next_) {
      if (EnumBP.Gap.ascii != alignment_.getRef(next_)) {
        break;
      }
    }

    if (next_ < end_) {
      // update key
      for (int ii = 0; ii < key_.length - 1; ++ii) {
        key_[ii] = key_[ii + 1];
      }
      int flank = next_ + 1;
      for (int count = 0; flank < alignment_.size(); ++flank) {
        if (EnumBP.Gap.ascii != alignment_.getRef(flank)) {
          if (++count == rf_) break;
        }
      }
      key_[key_.length - 1] = alignment_.getRef(flank);
    }
  }
}
