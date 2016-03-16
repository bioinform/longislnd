package com.bina.lrsim.simulator;

import java.util.Iterator;

import org.apache.log4j.Logger;

import com.bina.lrsim.bioinfo.Context;
import com.bina.lrsim.bioinfo.EnumBP;
import com.bina.lrsim.bioinfo.Heuristics;
import com.bina.lrsim.bioinfo.Kmerizer;
import com.bina.lrsim.pb.BaseCalls;
import com.bina.lrsim.pb.EnumDat;
import com.bina.lrsim.interfaces.EventGroup;

/**
 * Created by bayo on 6/16/15.
 */
public class EventIterator implements Iterator<Event> {
  private final static Logger log = Logger.getLogger(EventIterator.class.getName());
  private final EventGroup alignment;
  private final int lf; // left flank
  private final int rf; // right flank
  private final int hpAnchor; // homopolymer anchor length
  // sometimes two events (kmer and homopolymer) would be generated at a location next, this
  // pointer stores
  // the event not returned by the first call of next();
  private Event extra = null;
  private int next;
  private int end;
  private byte[] key;

  public EventIterator() {
    alignment = null;
    lf = -1;
    rf = -1;
    hpAnchor = -1;
    next = -1;
    end = next;
  }

  public EventIterator(EventGroup alignment, int leftFlank, int rightFlank, int leftMask, int rightMask, int hpAnchor) {
    this.alignment = alignment;
    this.lf = leftFlank;
    this.rf = rightFlank;
    this.hpAnchor = hpAnchor;

    // mask out right side of read
    end = this.alignment.size();
    for (int count = 0; count < rightMask && end > 0; --end) {
      if (EnumBP.Gap.ascii != this.alignment.getSeq(end - 1)) {
        ++count;
      }
    }
    // mask out right flank
    for (int count = 0; count < rightFlank && end > 0; --end) {
      if (EnumBP.Gap.ascii != this.alignment.getRef(end - 1)) {
        ++count;
      }
    }

    // mask out left flank
    next = 0;
    for (int count = 0; count < leftMask && next < this.alignment.size(); ++next) {
      if (EnumBP.Gap.ascii != this.alignment.getSeq(next)) {
        ++count;
      }
    }
    for (int count = 0; count < leftFlank && next < this.alignment.size(); ++next) {
      if (EnumBP.Gap.ascii != this.alignment.getRef(next)) {
        ++count;
      }
    }
    for (; next < alignment.size() && this.alignment.getRef(next) == EnumBP.Gap.ascii; ++next) {}

    if (hasNext()) {
      // build key around the next position
      key = new byte[leftFlank + rf + 1];
      key[leftFlank] = this.alignment.getRef(next);
      for (int pos = next + 1, k = leftFlank + 1; k < key.length; ++pos) {
        if (EnumBP.Gap.ascii != this.alignment.getRef(pos)) {
          key[k++] = this.alignment.getRef(pos);
        }
      }

      for (int pos = next - 1, k = leftFlank - 1; k >= 0; --pos) {
        if (EnumBP.Gap.ascii != this.alignment.getRef(pos)) {
          key[k--] = this.alignment.getRef(pos);
        }
      }
    }
  }

  @Override
  public boolean hasNext() {
    return (null != extra) || (next < end);
  }

  @Override
  public Event next() {
    if (null != extra) {
      Event ret = extra;
      extra = null;
      return ret;
    }
    boolean valid = true; // various conditions must be met to return this as an event to be considered

    BaseCalls bc = new BaseCalls(alignment.getSpec()); // this is probably a memory bound block killer
    EnumEvent event = null;

    if (alignment.getRef(next) == EnumBP.Gap.ascii) {
      throw new RuntimeException("alignment data can't be parsed properly");
    }
    if (alignment.getRef(next + 1) == EnumBP.Gap.ascii) {
      bc.reserve(10);
      if (alignment.getSeq(next) != EnumBP.Gap.ascii) {
        fillBase(bc, next);
      }
      for (int ins = next + 1; alignment.getRef(ins) == EnumBP.Gap.ascii; ++ins) {
        // this is a hack to accommodate spurious gap-to-gap alignment in pbalign's output
        if (alignment.getSeq(ins) != EnumBP.Gap.ascii) {
          fillBase(bc, ins);
        } else {
          // throw new RuntimeException("gap-vs-gap alignment");
        }
      }
      if (bc.size() == 0) {
        event = EnumEvent.DELETION;
      } else if (bc.size() == 1) {
        if (bc.get(0, EnumDat.BaseCall) == alignment.getRef(next)) {
          event = EnumEvent.MATCH;
        } else {
          event = EnumEvent.SUBSTITUTION;
        }
      } else {
        event = EnumEvent.INSERTION;
        if (bc.size() - 1 > Heuristics.MAX_INS_BP) {
          valid = false;
        } else if (bc.get(0, EnumDat.BaseCall) != alignment.getRef(next) || bc.get(1, EnumDat.BaseCall) != alignment.getRef(next)) {
          // log.info(new String(Arrays.copyOfRange(seq_,next-10,next+11)));
          // log.info(new String(Arrays.copyOfRange(ref_,next-10,next+11)));
          // log.info("      "+new String(key));
        }
      }
    } else if (alignment.getSeq(next) == EnumBP.Gap.ascii) {
      int nextCall = next;
      for (; nextCall < alignment.size() && alignment.getSeq(nextCall) == EnumBP.Gap.ascii; ++nextCall) {}
      if(nextCall < alignment.size()) {
        fillBase(bc, nextCall);
      }
      event = EnumEvent.DELETION;
    } else if (alignment.getSeq(next) == alignment.getRef(next)) {
      event = EnumEvent.MATCH;
      fillBase(bc, next);
    } else if (alignment.getSeq(next) != alignment.getRef(next)) {
      event = EnumEvent.SUBSTITUTION;
      fillBase(bc, next);
    } else {
      throw new RuntimeException("parsing error");
    }

    // this is a hack to accommodate spurious gap-to-gap alignment in pbalign's output
    if (event.equals(EnumEvent.INSERTION) && bc.size() == 0) {
      valid = false;
    }

    for (byte entry : key) {
      if (EnumBP.N.value == EnumBP.ascii2value(entry)) {
        valid = false;
        break;
      }
    }

    // kmer to return
    final int kmer = (valid) ? Kmerizer.fromASCII(key) : -1;

    if (valid) {
      extra = constructHPEvent(next, lf, rf, hpAnchor);
    }

    step();

    if (valid) {
      return new Event(new Context(kmer, 1), event, bc);
    } else {
      return null;
    }

  }

  private Event constructHPEvent(int start, int leftFlank, int rightFlank, int anchor) {
    // we don't look at the middle of homopolymer
    if (alignment.getRef(start) == alignment.getRef(start - 1)) return null;
    byte[] tmp = new byte[anchor + 1 + anchor];
/*
    int kk = anchor - 1;
    for(int pos = start - 1; pos >=0 && kk >= 0; --pos) {
      byte base = alignment.getRef(pos);
      if(base == EnumBP.N.ascii || base == 'n') {
        return null;
      }
      else if (base != EnumBP.Gap.ascii) {
        tmp[kk--] = base;
      }
    }
    tmp[anchor] = alignment.getRef(start);
    if(tmp[anchor] == tmp[anchor-1]) return null;
    kk = anchor + 1;
    */
    int kk = 0;

    // make sure the left flank is "intact"
    for (int pos = start - anchor; pos < start; ++pos) {
      if (EnumBP.ascii2value(alignment.getRef(pos)) != EnumBP.N.value && alignment.getRef(pos) != EnumBP.Gap.ascii && alignment.getSeq(pos) == alignment.getRef(pos)) {
        tmp[kk++] = alignment.getRef(pos);
      } else {
        return null;
      }
    }

    tmp[kk++] = alignment.getRef(start);

    // look for the next different base
    int nextDiff = start + 1;
    int hpLength = 1;
    for (; nextDiff < alignment.size() && (alignment.getRef(nextDiff) == alignment.getRef(start) || alignment.getRef(nextDiff) == EnumBP.Gap.ascii); ++nextDiff) {
      if (alignment.getRef(nextDiff) == alignment.getRef(start)) {
        ++hpLength;
      }
    }

    // homopolymer sampling is not needed if it's <= the flanking bases
    if (hpLength <= leftFlank && hpLength <= rightFlank) { return null; }
/*
    for (int pos = next_diff; kk < tmp.length && pos < alignment.size(); ++pos) {
      byte base = alignment.getRef(pos);
      if(base == EnumBP.N.ascii || base == 'n') {
        return null;
      }
      else if (base != EnumBP.Gap.ascii) {
        tmp[kk++] = base;
      }
    }
    */
    // make sure the right flank is "intact"
    for (int pos = nextDiff; kk < tmp.length && pos < alignment.size(); ++pos) {
      if (EnumBP.ascii2value(alignment.getRef(pos)) != EnumBP.N.value && alignment.getRef(pos) != EnumBP.Gap.ascii && alignment.getSeq(pos) == alignment.getRef(pos)) {
        tmp[kk++] = alignment.getRef(pos);
      } else {
        return null;
      }
    }
    if (kk != tmp.length) { return null; }

    BaseCalls bc = new BaseCalls(alignment.getSpec());
    try {
      for (int pos = start; pos < nextDiff; ++pos) {
        if (alignment.getSeq(pos) != EnumBP.Gap.ascii) {
          fillBase(bc, pos);
        }
      }
    } catch (Exception e) {
      log.info(e, e);
      return null;
    }

    EnumEvent ev;
    if (bc.size() < hpLength) {
      ev = EnumEvent.DELETION;
    } else if (bc.size() > hpLength) {
      ev = EnumEvent.INSERTION;
    } else {
      boolean same = true;
      for (int ii = 0; ii < bc.size(); ++ii) {
        if (bc.get(ii, EnumDat.BaseCall) != alignment.getRef(start)) {
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
        sb.append((char) alignment.getSeq(pos));
      }
      sb.append("\n");
      for (int pos = start - anchor; pos < next_diff + anchor; ++pos) {
        sb.append((char) alignment.getRef(pos));
      }
      sb.append("\n");
      for (int pos = 0; pos < bc.size(); ++pos) {
        sb.append((char) bc.get(pos, EnumDat.BaseCall));
      }
      sb.append(" ");
      for (int pos = start - anchor; pos < start; ++pos) {
        sb.append((char) alignment.getRef(pos));
      }
      sb.append(" ");
      for (int pos = next_diff; pos < next_diff + anchor; ++pos) {
        sb.append((char) alignment.getRef(pos));
      }
      sb.append("\n");
      sb.append(new String(tmp) + " " + hp_length);
      log.info(sb.toString());
    }
    */

    return new Event(new Context(Kmerizer.fromASCII(tmp), hpLength), ev, bc);
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException("cannot remove elements");
  }

  private void fillBase(BaseCalls bc, int index) {
    final int locIdx = bc.size();
    final int seqIdx = alignment.getSeqDataIndex(index);
    bc.push_back();
    bc.set(locIdx, EnumDat.BaseCall, alignment.getSeq(index));
    for (EnumDat ed : alignment.getSpec().getNonBaseDataSet()) {
      bc.set(locIdx, ed, alignment.getData(ed, seqIdx));
    }
  }

  private void step() {
    // set next to next value position
    for (++next; next < end; ++next) {
      if (EnumBP.Gap.ascii != alignment.getRef(next)) {
        break;
      }
    }

    if (next < end) {
      // update key
      for (int ii = 0; ii < key.length - 1; ++ii) {
        key[ii] = key[ii + 1];
      }
      int flank = next + 1;
      for (int count = 0; flank < alignment.size(); ++flank) {
        if (EnumBP.Gap.ascii != alignment.getRef(flank)) {
          if (++count == rf) break;
        }
      }
      key[key.length - 1] = alignment.getRef(flank);
    }
  }
}
