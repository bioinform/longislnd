package com.bina.lrsim.h5.cmp;

/**
 * Created by bayo on 5/2/15.
 */

import com.bina.lrsim.LRSim;
import com.bina.lrsim.bioinfo.Context;
import com.bina.lrsim.bioinfo.EnumBP;
import com.bina.lrsim.bioinfo.Kmerizer;
import com.bina.lrsim.h5.pb.BaseCalls;
import com.bina.lrsim.h5.pb.EnumDat;
import com.bina.lrsim.h5.pb.PBReadBuffer;
import com.bina.lrsim.interfaces.EventGroup;
import com.bina.lrsim.simulator.EnumEvent;
import com.bina.lrsim.simulator.Event;
import org.apache.log4j.Logger;

import java.util.Iterator;

public class CmpH5Alignment implements EventGroup {

    /**
     * @param left_flank  number of bp before the position of interest
     * @param right_flank number of bp after the position of interest
     * @param left_mask   omit this number of bases in the begining
     * @param right_mask  omit this number of bases in the end
     * @paran hp_anchor   use this many bases on both ends of homopolymer
     * @return an iterator of events associated with this alignment instance
     */
    @Override
    public Iterator<Event> getEventIterator(int left_flank, int right_flank, int left_mask, int right_mask, int hp_anchor) {
        if(! spanned_) {
            this.spanHomopolymer(Math.max(left_flank, right_flank) + 1);
            spanned_ = true;
        }
        return new EventIterator(left_flank, right_flank, left_mask, right_mask, hp_anchor);
    }

    private class EventIterator implements Iterator<Event> {
        // sometimes two events (kmer and homopolymer) would be generated at a location next_, this pointer stores
        // the event not returned by the first call of next();
        private Event extra_ = null;
        private int lf_; //left flank
        private int rf_; //right flank
        private int hp_anchor_; // homopolymer anchor length
        private int next_;
        private int end_;
        private byte[] key_;

        public EventIterator(int left_flank, int right_flank, int left_mask, int right_mask, int hp_anchor) {
            lf_ = left_flank;
            rf_ = right_flank;
            hp_anchor_ = hp_anchor;

            //mask out right flank
            end_ = ref_.length - 1;
            for (int count = 0; end_ >= 0; --end_) {
                if (EnumBP.Gap.ascii != seq_[end_]) {
                    if (++count == right_mask) break;
                }
            }
            --end_;
            //end_ is now one before the position at which right_mask bp appeared on seq_
            for (int count = 0; end_ >= 0; --end_) {
                if (EnumBP.Gap.ascii != ref_[end_]) {
                    if (++count == right_flank + 1) break;
                }
            }
            ++end_;

            //mask out left flank
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

            //build key around the next_ position
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
            BaseCalls bc = new BaseCalls(); // this is probably a memory bound block killer
            EnumEvent event = null;

            if (ref_[next_] == EnumBP.Gap.ascii) {
                log.info("alignment data can't be parsed properly");
                throw new RuntimeException("alignment data can't be parsed properly");
            }
            if (seq_[next_] == EnumBP.Gap.ascii) {
                event = EnumEvent.DELETION;
            } else if (ref_[next_ + 1] == EnumBP.Gap.ascii) {
                event = EnumEvent.INSERTION;
                bc.reserve(10);
                if (seq_[next_] != EnumBP.Gap.ascii) {
                    fillbase(bc, next_);
                }
                for (int ins = next_ + 1; ref_[ins] == EnumBP.Gap.ascii; ++ins) {
                    // this is a hack to accommodate spurious gap-to-gap alignment in pbalign's output
                    if (seq_[ins] != EnumBP.Gap.ascii) {
                        fillbase(bc, ins);
                    } else {
//                        throw new RuntimeException("gap-vs-gap alignment");
                    }
                }
            } else if (seq_[next_] == ref_[next_]) {
                event = EnumEvent.MATCH;
                fillbase(bc, next_);
            } else if (seq_[next_] != ref_[next_]) {
                event = EnumEvent.SUBSTITUTION;
                fillbase(bc, next_);
            } else {
                throw new RuntimeException("parsing error");
            }

            //kmer to return
            int kmer = -1;
            boolean valid = true;

            // this is a hack to accommodate spurious gap-to-gap alignment in pbalign's output
            if (event.equals(EnumEvent.INSERTION) && bc.size() == 0) {
                valid = false;
            }

            try {
                kmer = Kmerizer.fromASCII(key_);
            } catch (RuntimeException e) {
                e.printStackTrace();
                valid = false;
            }

            for (byte entry : key_) {
                if (EnumBP.N.ascii == entry) {
                    valid = false;
                    break;
                }
            }

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
            //we don't look at the middle of homopolymer
            if (ref_[start] == ref_[start - 1]) return null;
            byte[] tmp = new byte[anchor + 1 + anchor];
            int kk = 0;

            //make sure the left flank is "intact"
            for (int pos = start - anchor; pos < start; ++pos) {
                if (ref_[pos] != EnumBP.N.ascii && ref_[pos] != EnumBP.Gap.ascii && seq_[pos] == ref_[pos]) {
                    tmp[kk++] = ref_[pos];
                } else {
                    return null;
                }
            }

            tmp[kk++] = ref_[start];

            //look for the next different base
            int next_diff = start + 1;
            int hp_length = 1;
            for (; next_diff < ref_.length && (ref_[next_diff] == ref_[start] || ref_[next_diff] == EnumBP.Gap.ascii); ++next_diff) {
                if (ref_[next_diff] == ref_[start]) {
                    ++hp_length;
                }
            }

            //homopolymer sampling is not needed if it's <= the flanking bases
            if (hp_length <= left_flank && hp_length <= right_flank) {
                return null;
            }

            //make sure the right flank is "intact"
            for (int pos = next_diff; kk < tmp.length && pos < ref_.length; ++pos) {
                if (ref_[pos] != EnumBP.N.ascii && ref_[pos] != EnumBP.Gap.ascii && seq_[pos] == ref_[pos]) {
                    tmp[kk++] = ref_[pos];
                } else {
                    return null;
                }
            }
            if (kk != tmp.length) {
                return null;
            }

            BaseCalls bc = new BaseCalls();
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
            {
                StringBuilder sb = new StringBuilder();
                sb.append("homopolymer " +start + " " + next_diff+" " + anchor+"\n");
                for(int pos = start - anchor ; pos < next_diff+anchor; ++pos) {
                    sb.append((char)seq_[pos]);
                }
                sb.append("\n");
                for(int pos = start - anchor ; pos < next_diff+anchor; ++pos) {
                    sb.append((char)ref_[pos]);
                }
                sb.append("\n");
                for(int pos = 0; pos<bc.size(); ++pos){
                    sb.append((char)bc.get(pos,EnumDat.BaseCall));
                }
                sb.append(" ");
                for(int pos = start - anchor ; pos < start; ++pos) {
                    sb.append((char)ref_[pos]);
                }
                sb.append(" ");
                for(int pos = next_diff ; pos < next_diff+anchor; ++pos) {
                    sb.append((char)ref_[pos]);
                }
                sb.append("\n");
                sb.append(Arrays.toString(tmp)+" "+ hp_length);
                log.info(sb.toString());

            }
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
            for (EnumDat ed : EnumDat.getNonBaseSet()) {
                bc.set(loc_idx, ed, data_.get(ed)[begin + index]);
            }
        }

        private void step() {
            //set next_ to next value position
            for (++next_; next_ < end_; ++next_) {
                if (EnumBP.Gap.ascii != ref_[next_]) {
                    break;
                }
            }

            if (next_ < end_) {
                //update key
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

    public CmpH5Alignment(int[] index, AlnData data) {
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
        PBReadBuffer buffer = new PBReadBuffer(aln_length());
        BaseCalls bc = new BaseCalls(1);
        for (int ii = 0; ii < aln_length(); ++ii) {
            if (ba[ii] != EnumBP.Gap.ascii) {
                bc.set(0, EnumDat.BaseCall, ba[ii]);
                for (EnumDat e : EnumDat.getNonBaseSet()) {
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

            /* this assert is to throw if there's a gap-to-gap alignment, which breaks
               some down stream pacbio tools, instead, the insertion code path is modified to avoid
               the logging of gap-to-gap alignment*/
            if (ref_loc[ii] == seq_loc[ii] && ref_loc[ii] == EnumBP.Gap.ascii) {
//                throw new RuntimeException("gap-to-gap alignment " + aln[ii]);
                log.warn("' '-to-' ' alignment found in cmp.h5 alignment:" + aln[ii]);
            }
        }

        index_ = index;
        data_ = data;
        ref_ = ref_loc;
        seq_ = seq_loc;
        aln_ = aln_loc;
    }

    private void spanHomopolymer(int min_length) {
        final int length = ref_.length;

        for (int pos = 0; pos < length; ) {
            final byte base = ref_[pos];

            int next_diff = pos + 1;

            if (base != EnumBP.Gap.ascii) {
                int hp_length = 1;
                for (; next_diff < length && (ref_[next_diff] == EnumBP.Gap.ascii || ref_[next_diff] == base); ++next_diff) {
                    if (ref_[next_diff] == base) {
                        ++hp_length;
                    }
                }

                if (hp_length >= min_length) {
                    int left_most = pos;
                    for (; left_most > 0 && ref_[left_most - 1] == EnumBP.Gap.ascii && seq_[left_most - 1] == base; --left_most) {
                    }
                    if (left_most != pos) {
                        ref_[pos] = EnumBP.Gap.ascii;
                        ref_[left_most] = base;
                    }
                }
            }

            pos = next_diff;
        }
    }

    private int[] index_ = null;
    private AlnData data_ = null;
    private boolean spanned_ = false;

    private int[] aln_ = null; // for diagnostic
    private byte[] ref_ = null;
    private byte[] seq_ = null;
    private final static Logger log = Logger.getLogger(LRSim.class.getName());
}
