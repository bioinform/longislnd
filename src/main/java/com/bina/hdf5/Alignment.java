package com.bina.hdf5;

/**
 * Created by bayo on 5/2/15.
 */

import com.bina.hdf5.h5.cmp.AlnData;
import com.bina.hdf5.h5.cmp.EnumBP;
import com.bina.hdf5.h5.cmp.EnumIdx;
import org.apache.log4j.Logger;

public class Alignment {

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

    public int seq_length() {
        return index_[EnumIdx.rEnd.value()] - index_[EnumIdx.rStart.value()];
    }

    public int ref_length() {
        return index_[EnumIdx.tEnd.value()] - index_[EnumIdx.tStart.value()];
    }

    public int aln_begin() {
        return EnumIdx.offset_begin.value();
    }

    public int aln_end() {
        return EnumIdx.offset_end.value();
    }

    public Alignment(int[] index, AlnData data) throws Exception {
        load(index, data);
    }

    public PBReadBuffer toSeqRead() throws Exception {
        return toRead(seq_);
    }

    public PBReadBuffer toRefRead() throws Exception {
        return toRead(seq_);
    }

    private PBReadBuffer toRead(byte[] ba) throws Exception {
        final int begin = index_[EnumIdx.offset_begin.value()];
        PBReadBuffer buffer = new PBReadBuffer(aln_length());
        PBBaseCall bc = new PBBaseCall();
        for (int ii = 0; ii < aln_length(); ++ii) {
            if (seq_[ii] != EnumBP.Gap.ascii()) {
                bc.set(EnumDat.BaseCall, ba[ii]);
                for (EnumDat e : EnumDat.getNonBaseSet()) {
                    bc.set(e, data_.get(e)[begin + ii]);
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
