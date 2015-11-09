package com.bina.lrsim.bioinfo;

import java.io.IOException;
import java.util.Iterator;

import org.apache.log4j.Logger;

import com.bina.lrsim.interfaces.EventGroup;
import com.bina.lrsim.interfaces.EventGroupFactory;
import com.bina.lrsim.interfaces.EventGroupsProcessor;

/**
 * Created by bayo on 7/15/15.
 *
 * this is a hacked up class to do ad-hoc analysis of alignments
 */
public class AdHocProcessor implements EventGroupsProcessor {
  private final static Logger log = Logger.getLogger(AdHocProcessor.class.getName());
  final int max_seq_len;
  final int target_ref_len;
  final int anchor;
  final long[] seq_count;

  public AdHocProcessor(final int target_ref_len, final int max_seq_len, final int anchor) {
    this.max_seq_len = max_seq_len;
    this.target_ref_len = target_ref_len;
    this.anchor = anchor;
    seq_count = new long[max_seq_len + 1];
  }

  private void Put(int seq_len) {
    if (seq_len <= max_seq_len) ++seq_count[seq_len];
  }

  private long Get(int seq_len) {
    return seq_count[seq_len];
  }

  @Override
  public void process(EventGroupFactory groups, int min_length, int flank_mask) throws IOException {
    long groupcount = 0;
    for (EventGroup group : groups) {
      if (group.seq_length() < min_length) continue;

      int begin = 0;
      for (int count = 0; count < flank_mask && begin < group.size(); ++begin) {
        final byte base = group.getSeq(begin);
        if (base != EnumBP.Gap.ascii) {
          ++count;
        }
      }

      int end = group.size();
      for (int count = 0; count < flank_mask && end > 0; --end) {
        final byte base = group.getSeq(end - 1);
        if (base != EnumBP.Gap.ascii) {
          ++count;
        }
      }
      process(group, begin, end);
      ++groupcount;
      if (groupcount % 1000 == 0) log.info(toString());
    }
    log.info(toString());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(target_ref_len);
    sb.append(": ");
    for (int ii = 0; ii < seq_count.length; ++ii) {
      if (seq_count[ii] > 0) {
        sb.append(" ");
        sb.append(ii);
        sb.append("-");
        sb.append(seq_count[ii]);
      }
    }
    return sb.toString();
  }

  private void process(EventGroup group, int begin, final int end) {
    PosItr itr = new PosItr(group, begin, end);
/*
    for (int aa = 0; aa < anchor; ++aa) {
      if (itr.hasNext()) itr.next();
      else return;
    }
    if (!itr.hasNext()) return;
    */

    while (itr.hasNext()) {
      final int loc_start = itr.next();
      int pos = loc_start;

      final byte org_base = group.getRef(loc_start);

      byte last_base = EnumBP.Gap.ascii;

      for(int pp = loc_start - 1; last_base == EnumBP.Gap.ascii && pp >=0; --pp) {
        last_base = group.getRef(pp);
      }

      boolean hp_of_target = (last_base != org_base) && (org_base == EnumBP.T.ascii);

      boolean valid = true;

/*
      for (int aa = 1; aa <= anchor; ++aa) {
        if (group.getSeq(pos - aa) != group.getRef(pos - aa)) {
          valid = false;
        }
      }
*/

      if (!valid) continue;

      int ref_len = 0;
      int seq_len = 0;

      for (; pos < end; ++pos) {
        final byte ref_base = group.getRef(pos);
        if (ref_base != EnumBP.Gap.ascii) {
          byte neighbor_base = EnumBP.Gap.ascii;
          for (int pp = pos - 1; EnumBP.Gap.ascii == neighbor_base && pp >= 0; --pp) {
            neighbor_base = group.getSeq(pp);
          }
          if (neighbor_base == ref_base) {
//            valid = false;
          }

          neighbor_base = EnumBP.Gap.ascii;
          for (int pp = pos + 1; EnumBP.Gap.ascii == neighbor_base && pp < end; ++pp) {
            neighbor_base = group.getSeq(pp);
          }
          if (neighbor_base == ref_base) {
//            valid = false;
          }
          ++ref_len;
          if(ref_len <= target_ref_len) {
            if(ref_base != org_base) {
              hp_of_target = false;
            }
          }
          else {
            if(ref_base == org_base) {
              hp_of_target = false;

            }

          }
        }

        if (ref_len <= target_ref_len) {
          if (group.getSeq(pos) != EnumBP.Gap.ascii) {
            ++seq_len;
          }
        } else {
          break;
        }
      }

      if (ref_len != target_ref_len + 1) continue;
/*
      for (int aa = 0; aa < anchor; ++aa) {
        if (group.getSeq(pos + aa) != group.getRef(pos + aa)) {
          valid = false;
        }
      }
      */

      if (valid && hp_of_target)
      {
        /*
        if(seq_len < target_ref_len) {
          StringBuilder s = new StringBuilder();
          StringBuilder r = new StringBuilder();
          for (int ii = loc_start-1; ii < pos+1; ++ii) {
            s.append((char) group.getSeq(ii));
            r.append((char) group.getRef(ii));
          }
          log.info(s.toString());
          log.info(r.toString());
          log.info(target_ref_len + "->" + seq_len);
        }
        */
        Put(seq_len);
      }
    }
  }

  private static class PosItr implements Iterator<Integer> {
    private final EventGroup group;
    private final int end;
    private int curr;

    PosItr(EventGroup group, final int begin, final int end) {
      this.group = group;
      this.end = end;
      curr = getNext(begin - 1);
    }

    private int getNext(int curr) {
      for (++curr; curr < end && (group.getRef(curr) == EnumBP.Gap.ascii || EnumBP.ascii2value(group.getRef(curr)) == EnumBP.N.value ); ++curr) {}
      return curr;
    }


    @Override
    public boolean hasNext() {
      return curr < end;
    }

    @Override
    public Integer next() {
      final Integer tmp = curr;
      curr = getNext(curr);
      return tmp;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException("cannot remove elements");
    }
  }


}
