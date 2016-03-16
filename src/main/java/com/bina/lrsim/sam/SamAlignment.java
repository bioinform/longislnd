package com.bina.lrsim.sam;

import java.util.Iterator;

import org.apache.log4j.Logger;

import com.bina.lrsim.bioinfo.PairwiseAlignment;
import com.bina.lrsim.pb.EnumDat;
import com.bina.lrsim.pb.Spec;
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
    int[] seqDataIdx = new int[length];

    int seqSamPos = 0;
    int refRefPos = 0;
    final byte[] refRef = references.getSubsequenceAt(samRecord.getReferenceName(), samRecord.getAlignmentStart(), samRecord.getAlignmentEnd()).getBases();

    int seqNext = 0;
    int refNext = 0;
    for (CigarElement entry : samRecord.getCigar().getCigarElements()) {
      final boolean hasSeq = entry.getOperator().consumesReadBases();
      final boolean hasRef = entry.getOperator().consumesReferenceBases();
      if (hasSeq) {
        for (int counter = 0; counter < entry.getLength(); ++counter, ++seqNext, ++seqSamPos) {
          seq[seqNext] = samRecord.getReadBases()[seqSamPos]; // assumes it'll get compiled out
          seqDataIdx[seqNext] = seqSamPos;
        }
      } else if (hasRef) {
        for (int counter = 0; counter < entry.getLength(); ++counter, ++seqNext) {
          seq[seqNext] = ' ';
          seqDataIdx[seqNext] = -1;
        }
      }
      if (hasRef) {
        for (int counter = 0; counter < entry.getLength(); ++counter, ++refNext, ++refRefPos) {
          ref[refNext] = refRef[refRefPos];
        }
      } else if (hasSeq) {
        for (int counter = 0; counter < entry.getLength(); ++counter, ++refNext) {
          ref[refNext] = ' ';
        }
      }
    }
    // log.info(new String(seq));
    // log.info(new String(ref));
    pairwiseAlignment = new PairwiseAlignment(ref, seq, seqDataIdx);
    pairwiseAlignment.span();
  }

  @Override
  public Iterator<Event> iterator(int leftFlank, int rightFlank, int leftMask, int rightMask, int hpAnchor) {
    if (null != pairwiseAlignment) {
      pairwiseAlignment.span();
      return new EventIterator(this, leftFlank, rightFlank, leftMask, rightMask, hpAnchor);
    } else {
      return new EventIterator();
    }
  }

  @Override
  public int getSeqLength() {
    return samRecord.getReadLength();
  }

  @Override
  public int getRefLength() {
    return samRecord.getCigar().getReferenceLength();
  }

  @Override
  public byte getData(EnumDat ed, int seqIdx) {
    return 0;
  }

  @Override
  public Spec getSpec() {
    return Spec.FastqSpec;
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
