package com.bina.lrsim.sam;

import java.util.Iterator;

import org.apache.log4j.Logger;

import com.bina.lrsim.bioinfo.PairwiseAlignment;
import com.bina.lrsim.pb.EnumDat;
import com.bina.lrsim.pb.PBSpec;
import com.bina.lrsim.interfaces.EventGroup;
import com.bina.lrsim.simulator.Event;
import com.bina.lrsim.simulator.EventIterator;

import htsjdk.samtools.CigarElement;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.reference.ReferenceSequenceFile;

/**
 * Created by bayo on 6/15/15.
 */
public class SamAlignment implements EventGroup {

  private final static Logger log = Logger.getLogger(SamAlignment.class.getName());
  private final SAMRecord samRecord;
  private PairwiseAlignment pairwiseAlignment = null;

  public SamAlignment(SAMRecord samRecord, ReferenceSequenceFile references) {
    this.samRecord = samRecord;
    load(this.samRecord, references);
  }

  private static int getAlignmentLength(SAMRecord samRecord) {
    int length = 0;
    for (CigarElement entry : samRecord.getCigar().getCigarElements()) {
      if (entry.getOperator().consumesReferenceBases() || entry.getOperator().consumesReadBases()) {
        length += entry.getLength();
      }
    }
    return length;
  }

  public void load(SAMRecord samRecord, ReferenceSequenceFile references) {
    if (samRecord.getNotPrimaryAlignmentFlag() || samRecord.getSupplementaryAlignmentFlag()) {
      pairwiseAlignment = null;
      return;
    }
    final int length = getAlignmentLength(samRecord);
    if (length == 0) {
      pairwiseAlignment = null;
      return;
    }
    byte[] ref = new byte[length];
    byte[] seq = new byte[length];
    int[] seq_data_idx = new int[length];

    int seq_sam_pos = 0;
    int ref_ref_pos = 0;
    final byte[] ref_ref = references.getSubsequenceAt(samRecord.getReferenceName(), samRecord.getAlignmentStart(), samRecord.getAlignmentEnd()).getBases();

    int seq_next = 0;
    int ref_next = 0;
    for (CigarElement entry : samRecord.getCigar().getCigarElements()) {
      final boolean hasSeq = entry.getOperator().consumesReadBases();
      final boolean hasRef = entry.getOperator().consumesReferenceBases();
      if (hasSeq) {
        for (int counter = 0; counter < entry.getLength(); ++counter, ++seq_next, ++seq_sam_pos) {
          seq[seq_next] = samRecord.getReadBases()[seq_sam_pos]; // assumes it'll get compiled out
          seq_data_idx[seq_next] = seq_sam_pos;
        }
      } else if (hasRef) {
        for (int counter = 0; counter < entry.getLength(); ++counter, ++seq_next) {
          seq[seq_next] = ' ';
          seq_data_idx[seq_next] = -1;
        }
      }
      if (hasRef) {
        for (int counter = 0; counter < entry.getLength(); ++counter, ++ref_next, ++ref_ref_pos) {
          ref[ref_next] = ref_ref[ref_ref_pos];
        }
      } else if (hasSeq) {
        for (int counter = 0; counter < entry.getLength(); ++counter, ++ref_next) {
          ref[ref_next] = ' ';
        }
      }
    }
    // log.info(new String(seq));
    // log.info(new String(ref));
    pairwiseAlignment = new PairwiseAlignment(ref, seq, seq_data_idx);
    pairwiseAlignment.span();
  }

  @Override
  public Iterator<Event> iterator(int left_flank, int right_flank, int left_mask, int right_mask, int hp_anchor) {
    if (null != pairwiseAlignment) {
      pairwiseAlignment.span();
      return new EventIterator(this, left_flank, right_flank, left_mask, right_mask, hp_anchor);
    } else {
      return new EventIterator();
    }
  }

  @Override
  public int seq_length() {
    return samRecord.getReadLength();
  }

  @Override
  public int ref_length() {
    return samRecord.getCigar().getReferenceLength();
  }

  @Override
  public byte getData(EnumDat ed, int seq_idx) {
    return 0;
  }

  @Override
  public PBSpec getSpec() {
    return new PBFastqSpec();
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

}
