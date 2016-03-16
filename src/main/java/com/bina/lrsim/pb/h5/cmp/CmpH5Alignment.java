package com.bina.lrsim.pb.h5.cmp;

/**
 * Created by bayo on 5/2/15.
 */

import java.util.Iterator;

import com.bina.lrsim.pb.Spec;
import org.apache.log4j.Logger;

import com.bina.lrsim.bioinfo.EnumBP;
import com.bina.lrsim.simulator.EventIterator;
import com.bina.lrsim.bioinfo.PairwiseAlignment;
import com.bina.lrsim.pb.EnumDat;
import com.bina.lrsim.interfaces.EventGroup;
import com.bina.lrsim.simulator.Event;

public class CmpH5Alignment implements EventGroup {

  private final static Logger log = Logger.getLogger(CmpH5Alignment.class.getName());
  private final Spec spec;
  private int[] index = null;
  private AlnData data = null;
  private PairwiseAlignment pairwiseAlignment = null;

  public CmpH5Alignment(int[] index, AlnData data, Spec spec) {
    this.spec = spec;
    load(index, data);
  }

  /**
   * @param leftFlank number of bp before the position of interest
   * @param rightFlank number of bp after the position of interest
   * @param leftMask omit this number of bases in the begining
   * @param rightMask omit this number of bases in the end
   * @paran getHpAnchor use this many bases on both ends of homopolymer
   * @return an iterator of events associated with this alignment instance
   */
  @Override
  public Iterator<Event> iterator(int leftFlank, int rightFlank, int leftMask, int rightMask, int hpAnchor) {
    pairwiseAlignment.span();
    return new EventIterator(this, leftFlank, rightFlank, leftMask, rightMask, hpAnchor);
  }

  @Override
  public int getSeqLength() {
    return index[EnumIdx.rEnd.ordinal()] - index[EnumIdx.rStart.ordinal()];
  }

  @Override
  public int getRefLength() {
    return index[EnumIdx.tEnd.ordinal()] - index[EnumIdx.tStart.ordinal()];
  }

  public void load(int[] index, AlnData data) {
    final int begin = index[EnumIdx.offset_begin.ordinal()];
    final int end = index[EnumIdx.offset_end.ordinal()];
    final int length = end - begin;
    byte[] refLoc = new byte[length];
    byte[] seqLoc = new byte[length];
    int[] seqDataIdxLoc = new int[length];

    byte[] aln = data.get(EnumDat.AlnArray);

    for (int ii = 0; ii < length; ++ii) {
      byte entry = aln[begin + ii];
      refLoc[ii] = EnumBP.cmp2Ref(entry).ascii;
      if (EnumBP.Invalid.value == refLoc[ii]) throw new RuntimeException("bad ref char");
      seqLoc[ii] = EnumBP.cmp2Seq(entry).ascii;
      if (EnumBP.Invalid.value == seqLoc[ii]) throw new RuntimeException("bad seq char");

      /*
       * this assert is to throw if there's a gap-to-gap alignment, which breaks some down stream pacbio tools, instead, the insertion code path is modified to
       * avoid the logging of gap-to-gap alignment
       */
      if (refLoc[ii] == seqLoc[ii] && refLoc[ii] == EnumBP.Gap.ascii) {
        // throw new RuntimeException("gap-to-gap alignment " + aln[ii]);
        log.warn("' '-to-' ' alignment found in cmp.h5 alignment:" + aln[ii]);
      }

      seqDataIdxLoc[ii] = (seqLoc[ii] != EnumBP.Gap.ascii) ? (begin + ii) : -1;
    }

    this.index = index;
    this.data = data;
    pairwiseAlignment = new PairwiseAlignment(refLoc, seqLoc, seqDataIdxLoc);
  }

  @Override
  public byte getData(EnumDat ed, int seqIdx) {
    return data.get(ed)[seqIdx];
  }

  @Override
  public Spec getSpec() {
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
