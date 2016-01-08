package com.bina.lrsim.h5.cmp;

/**
 * Created by bayo on 5/2/15.
 */

import java.util.Iterator;

import org.apache.log4j.Logger;

import com.bina.lrsim.bioinfo.EnumBP;
import com.bina.lrsim.simulator.EventIterator;
import com.bina.lrsim.bioinfo.PairwiseAlignment;
import com.bina.lrsim.pb.EnumDat;
import com.bina.lrsim.pb.PBSpec;
import com.bina.lrsim.interfaces.EventGroup;
import com.bina.lrsim.simulator.Event;

public class CmpH5Alignment implements EventGroup {

  private final static Logger log = Logger.getLogger(CmpH5Alignment.class.getName());
  private final PBSpec spec;
  private int[] index_ = null;
  private AlnData data_ = null;
  private PairwiseAlignment pairwiseAlignment = null;

  public CmpH5Alignment(int[] index, AlnData data, PBSpec spec) {
    this.spec = spec;
    load(index, data);
  }

  /**
   * @param left_flank number of bp before the position of interest
   * @param right_flank number of bp after the position of interest
   * @param left_mask omit this number of bases in the begining
   * @param right_mask omit this number of bases in the end
   * @paran hp_anchor use this many bases on both ends of homopolymer
   * @return an iterator of events associated with this alignment instance
   */
  @Override
  public Iterator<Event> iterator(int left_flank, int right_flank, int left_mask, int right_mask, int hp_anchor) {
    pairwiseAlignment.span();
    return new EventIterator(this, left_flank, right_flank, left_mask, right_mask, hp_anchor);
  }

  @Override
  public int seq_length() {
    return index_[EnumIdx.rEnd.value] - index_[EnumIdx.rStart.value];
  }

  @Override
  public int ref_length() {
    return index_[EnumIdx.tEnd.value] - index_[EnumIdx.tStart.value];
  }

  public void load(int[] index, AlnData data) {
    final int begin = index[EnumIdx.offset_begin.value];
    final int end = index[EnumIdx.offset_end.value];
    final int length = end - begin;
    byte[] ref_loc = new byte[length];
    byte[] seq_loc = new byte[length];
    int[] seq_data_idx_loc = new int[length];

    byte[] aln = data.get(EnumDat.AlnArray);

    for (int ii = 0; ii < length; ++ii) {
      byte entry = aln[begin + ii];
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

      seq_data_idx_loc[ii] = (seq_loc[ii] != EnumBP.Gap.ascii) ? (begin + ii) : -1;
    }

    index_ = index;
    data_ = data;
    pairwiseAlignment = new PairwiseAlignment(ref_loc, seq_loc, seq_data_idx_loc);
  }

  @Override
  public byte getData(EnumDat ed, int seq_idx) {
    return data_.get(ed)[seq_idx];
  }

  @Override
  public PBSpec getSpec() {
    return spec;
  }

  @Override
  public byte getRef(int index) {
    return pairwiseAlignment.getRef(index);
  }

  @Override
  public byte getSeq(int index) {
    return pairwiseAlignment.getSeq(index);
  }

  @Override
  public int getSeqDataIndex(int index) {
    return pairwiseAlignment.getSeqDataIndex(index);
  }

  @Override
  public int size() {
    return pairwiseAlignment.size();
  }
  @Override public String toString() {
    return pairwiseAlignment.toString();
  }
}
