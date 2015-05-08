package com.bina.hdf5.h5.cmp;

/**
 * Created by bayo on 5/2/15.
 */

import com.bina.hdf5.h5.pb.EnumDat;
import com.bina.hdf5.H5Test;
import com.bina.hdf5.h5.pb.PBReadBuffer;
import com.bina.hdf5.interfaces.EventGroup;
import com.bina.hdf5.simulator.BaseCalls;
import com.bina.hdf5.simulator.EnumEvent;
import com.bina.hdf5.simulator.Event;
import com.bina.hdf5.util.EnumBP;
import com.bina.hdf5.util.Kmerizer;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.Iterator;

public class CmpH5Alignment implements EventGroup {

    public Iterator<Event> getEventIterator(int left_flank, int right_flank) {
        return new EventIterator(left_flank, right_flank);
    }

    private class EventIterator implements Iterator<Event> {
        public EventIterator(int left_flank, int right_flank) {
            rf_ = right_flank;
            key_ = new byte[left_flank + rf_ + 1];

            int key_idx = key_.length - 1;
            end_ = ref_.length - 1;
            for (int count = 0; end_ >= 0; --end_) {
                if (EnumBP.Gap.ascii() != ref_[end_]) {
                    key_[key_idx--] = ref_[end_];
                    if (++count == rf_ + 1) break;
                }
            }
            ++end_;

            key_idx = 0;
            next_ = 0;
            for (int count = 0; next_ < end_; ++next_) {
                if (EnumBP.Gap.ascii() != ref_[next_]) {
                    key_[key_idx++] = ref_[next_];
                    if (++count == left_flank + 1) break;
                }
            }
        }

        @Override
        public boolean hasNext() {
            return next_ < end_;
        }

        @Override
        public Event next() {
            BaseCalls bc = new BaseCalls();
            EnumEvent event = null;
            try {
                if (seq_[next_] == EnumBP.Gap.ascii()) {
                    event = EnumEvent.DELETION;
                } else if (ref_[next_ + 1] == EnumBP.Gap.ascii()) {
                    event = EnumEvent.INSERTION;
                    bc.reserve(10);
                    if (seq_[next_] != EnumBP.Gap.ascii()) {
                        fillbase(bc, next_);
                    }
                    for (int ins = next_ + 1; ref_[ins] == EnumBP.Gap.ascii(); ++ins) {
                        fillbase(bc, ins);
                    }
                } else if (seq_[next_] == ref_[next_]) {
                    event = EnumEvent.MATCH;
                    fillbase(bc, next_);
                } else if (seq_[next_] != ref_[next_]) {
                    event = EnumEvent.SUBSTITUTION;
                    fillbase(bc, next_);
                } else {
                    throw new Exception("parsing error");
                }
            } catch (Exception e) {
                log.info("failed");
                return null;
            }

            //kmer to return
            final int kmer = Kmerizer.fromASCII(key_);
            boolean hasN = false;
            for(byte entry: key_){
                if(EnumBP.N.ascii() == entry){
                    hasN = true;
                    break;
                }
            }

            step();

            if(hasN){
                return null;
            }
            else{
                return new Event(kmer, event, bc);
            }

        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("cannot remove elements");
        }

        private void fillbase(BaseCalls bc, int index) throws Exception {
            final int begin = index_[EnumIdx.offset_begin.value()];
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
                if (EnumBP.Gap.ascii() != ref_[next_]) {
                    break;
                }
            }

            if(next_ < end_){
                //update key
                for (int ii = 0; ii < key_.length - 1; ++ii) {
                    key_[ii] = key_[ii + 1];
                }
                int flank = next_ + 1;
                for (int count = 0; flank < ref_.length; ++flank) {
                    if (EnumBP.Gap.ascii() != ref_[flank]) {
                        if (++count == rf_) break;
                    }
                }
                key_[key_.length - 1] = ref_[flank];
            }
        }

        private int rf_;

        private int next_;
        private int end_;
        private byte[] key_;
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
        return index_[EnumIdx.offset_end.value()] - index_[EnumIdx.offset_begin.value()];
    }

    @Override
    public int seq_length() {
        return index_[EnumIdx.rEnd.value()] - index_[EnumIdx.rStart.value()];
    }

    @Override
    public int ref_length() {
        return index_[EnumIdx.tEnd.value()] - index_[EnumIdx.tStart.value()];
    }

    public int aln_begin() {
        return EnumIdx.offset_begin.value();
    }

    public int aln_end() {
        return EnumIdx.offset_end.value();
    }

    public CmpH5Alignment(int[] index, AlnData data) throws Exception {
        load(index, data);
    }

    public PBReadBuffer toSeqRead() throws Exception {
        return toRead(seq_);
    }

    public PBReadBuffer toRefRead() throws Exception {
        return toRead(ref_);
    }

    private PBReadBuffer toRead(byte[] ba) throws Exception {
        final int begin = index_[EnumIdx.offset_begin.value()];
        PBReadBuffer buffer = new PBReadBuffer(aln_length());
        BaseCalls bc = new BaseCalls(1);
        for (int ii = 0; ii < aln_length(); ++ii) {
            if (ba[ii] != EnumBP.Gap.ascii()) {
                bc.set(0,EnumDat.BaseCall, ba[ii]);
                for (EnumDat e : EnumDat.getNonBaseSet()) {
                    bc.set(0,e, data_.get(e)[begin + ii]);
                }
                buffer.addLast(bc);
            }
        }
        return buffer;
    }

    public void load(int[] index, AlnData data) throws Exception {

        try {
            final int begin = index[EnumIdx.offset_begin.value()];
            final int end = index[EnumIdx.offset_end.value()];
            final int length = end - begin;
            byte[] ref_loc = new byte[length];
            byte[] seq_loc = new byte[length];
            int[] aln_loc = new int[length];

            byte[] aln = data.get(EnumDat.AlnArray);

            for (int ii = 0; ii < length; ++ii) {
                byte entry = aln[begin + ii];
                aln_loc[ii] = entry & 0xff;
                ref_loc[ii] = EnumBP.cmp2ref(entry).ascii();
                if (EnumBP.Invalid.value() == ref_loc[ii]) throw new Exception("bad ref char");
                seq_loc[ii] = EnumBP.cmp2seq(entry).ascii();
                if (EnumBP.Invalid.value() == seq_loc[ii]) throw new Exception("bad seq char");
            }

            index_ = index;
            data_ = data;
            ref_ = ref_loc;
            seq_ = seq_loc;
            aln_ = aln_loc;
        } catch (Exception e) {
            throw e;
        }
    }

    private int[] index_ = null;
    private AlnData data_ = null;

    private int[] aln_ = null; // for diagnostic
    private byte[] ref_ = null;
    private byte[] seq_ = null;
    private final static Logger log = Logger.getLogger(H5Test.class.getName());
}
