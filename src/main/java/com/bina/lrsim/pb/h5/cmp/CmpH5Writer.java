package com.bina.lrsim.pb.h5.cmp;

import com.bina.lrsim.bioinfo.EnumBP;
import com.bina.lrsim.pb.h5.H5AppendableByteArray;
import com.bina.lrsim.pb.h5.H5ScalarDSIO;
import com.bina.lrsim.pb.h5.Attributes;
import htsjdk.samtools.*;
import htsjdk.samtools.reference.IndexedFastaSequenceFile;
import ncsa.hdf.hdf5lib.exceptions.HDF5Exception;
import ncsa.hdf.object.FileFormat;
import ncsa.hdf.object.h5.H5File;
import ncsa.hdf.object.h5.H5Group;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * Created by bayolau on 11/3/15.
 */
public class CmpH5Writer implements Closeable {
  private final static Logger log = Logger.getLogger(CmpH5Writer.class.getName());

  final H5File h5;
  private final IndexedFastaSequenceFile references;

  private final int[] seqValue = new int[256];
  private final int[] refValue = new int[256];

  private final PBIndexMap alnGroup = new PBIndexMap(); // /ref<refInfo_id>/<movie_name>
  private final PBIndexMap movieInfo = new PBIndexMap(); // all Movie Names
  private final PBIndexMap refGroup = new PBIndexMap(); // names of references with at least one mapped reads
  private final PBIndexMap refInfo = new PBIndexMap(); // names of all references
  private final AlnIndex alnIndex = new AlnIndex(); // aln index for writing at closing
  private final Map<String, H5AppendableByteArray> agAln = new HashMap<>();
  private final Map<String, H5AppendableByteArray> agQv = new HashMap<>();

  public CmpH5Writer(String filename, File fasta) {
    this.h5 = new H5File(filename, FileFormat.CREATE);
    try {
      H5Group grp = (H5Group) h5.get("/");
      Attributes att = new Attributes();
      att.add("Version", new String[] {"2.0.0"}, null, false);
      att.add("ReadType", new String[] {"standard"}, null, false);
      att.writeTo(grp);
    } catch (Exception e) {
      throw new RuntimeException("failed to create group /");
    }
    try {
      this.references = new IndexedFastaSequenceFile(fasta);
    } catch (FileNotFoundException e) {
      log.error("failed to load reference file");
      throw new RuntimeException(e.getMessage());
    }

    for (SAMSequenceRecord record : this.references.getSequenceDictionary().getSequences()) {
      refInfo.get(record.getSequenceName());
    }
    seqValue['T'] = 128;
    seqValue['G'] = 64;
    seqValue['C'] = 32;
    seqValue['A'] = 16;
    seqValue['t'] = 128;
    seqValue['g'] = 64;
    seqValue['c'] = 32;
    seqValue['a'] = 16;
    refValue['T'] = 8;
    refValue['G'] = 4;
    refValue['C'] = 2;
    refValue['A'] = 1;
    refValue['t'] = 8;
    refValue['g'] = 4;
    refValue['c'] = 2;
    refValue['a'] = 1;
  }

  Pair<Integer, Integer> add(final String ag, byte[] aln, byte[] qv) {
    if (!agAln.containsKey(ag)) {
      try {
        h5.createGroup(ag, null);
      } catch (Exception e) {
        log.warn("failed to create " + ag);
      }
      agAln.put(ag, new H5AppendableByteArray(h5, ag + "/AlnArray", 50000));
      agQv.put(ag, new H5AppendableByteArray(h5, ag + "/QualityValue", 50000));
    }
    H5AppendableByteArray buffer = agAln.get(ag);
    final int begin = buffer.size();
    buffer.add(aln);
    final int end = buffer.size();
    agQv.get(ag).add(qv);
    return new ImmutablePair<>(begin, end);
  }

  public void add(SAMRecord record) {
    if (record.getReadUnmappedFlag()) return; // do nothing if unmmapped

    // these are pacbio conventions, the h5 format is basically unmaintainable without these
    final int firstSlash = record.getReadName().indexOf('/');
    final String movieName = record.getReadName().substring(0, firstSlash);

    final int secondSlash = record.getReadName().indexOf('/', firstSlash + 1);
    final int minus = record.getReadName().indexOf('-', firstSlash + 1);

//    final int hole = Integer.valueOf(record.getReadName().substring(first_slash + 1, (second_slash >= 0) ? second_slash : minus));
    final int hole = Integer.valueOf(record.getReadName().substring(firstSlash + 1, (secondSlash >= 0) ? secondSlash : (minus >= 0 ? minus : record.getReadName().length())));

    final int pass = (minus >= 0) ? Integer.valueOf(record.getReadName().substring(minus + 1)) : 0; // this is to pick up cobbsalad ccs notation

    final int firstUnderscore = record.getReadName().indexOf('_', firstSlash + 1); // will be found for subread, not for ccs
    int rStart = 0, rEnd = record.getReadLength();
    if (firstUnderscore >= 0) {
      rStart = Integer.valueOf(record.getReadName().substring(secondSlash + 1, firstUnderscore));
      rEnd = Integer.valueOf(record.getReadName().substring(firstUnderscore + 1));
    }

    // derive required data from CIGAR string
    int alignmentLength = 0;
    int match = 0;
    int mismatch = 0;
    int ins = 0;
    int del = 0;
    List<CigarElement> cigarList = record.getCigar().getCigarElements();
    for (CigarElement entry : cigarList) {
      if (entry.getOperator().consumesReferenceBases() || entry.getOperator().consumesReadBases()) {
        alignmentLength += entry.getLength();
        entry.getOperator();
      }
    }
    byte[] alnArray = new byte[alignmentLength];
    byte[] qvArray = new byte[alignmentLength];
    int seq_next = 0;
    int ref_next = 0;
    int aln_next = 0;
    byte[] ref = references.getSubsequenceAt(record.getReferenceName(), record.getAlignmentStart(), record.getAlignmentEnd()).getBases();
    byte[] seq = record.getReadBases();
    if (record.getReadNegativeStrandFlag()) { // RC the bases in pacbio notation, reverse later
      seq = Arrays.copyOf(seq, seq.length); // just to be safe
      for (int ii = 0; ii < seq.length; ++ii) {
        seq[ii] = EnumBP.ascii_rc(seq[ii]);
      }
      ref = Arrays.copyOf(ref, ref.length); // just to be safe
      for (int ii = 0; ii < ref.length; ++ii) {
        ref[ii] = EnumBP.ascii_rc(ref[ii]);
      }
    }
    final byte[] qual = record.getBaseQualities();
    for (CigarElement entry : cigarList) {
      final boolean hasSeq = entry.getOperator().consumesReadBases();
      final boolean hasRef = entry.getOperator().consumesReferenceBases();
      if (hasSeq && hasRef) { // if base-to-base
        for (int count = 0; count < entry.getLength(); ++count, ++aln_next, ++seq_next, ++ref_next) {
          final int s = this.seqValue[seq[seq_next]];
          final int r = this.refValue[ref[ref_next]];
          if (r == s / 16) {
            ++match;
          } else {
            ++mismatch;
          }
          alnArray[aln_next] = (byte) (r | s);
          qvArray[aln_next] = qual[seq_next];
        }
      } else if (hasSeq) { // if insertion
        for (int count = 0; count < entry.getLength(); ++count, ++aln_next, ++seq_next) {
          alnArray[aln_next] = (byte) (this.seqValue[seq[seq_next]]);
          qvArray[aln_next] = qual[seq_next];
        }
        ins += entry.getLength();
      } else if (hasRef) { // if deletion
        for (int count = 0; count < entry.getLength(); ++count, ++aln_next, ++ref_next) {
          alnArray[aln_next] = (byte) (this.refValue[ref[ref_next]]);
        }
        del += entry.getLength();
      }
    }

    final int lSoftClip = (cigarList.size() > 0 && cigarList.get(0).getOperator() == CigarOperator.S) ? cigarList.get(0).getLength() : 0;
    final int rSoftClip = (cigarList.size() > 1 && cigarList.get(cigarList.size() - 1).getOperator() == CigarOperator.S) ? cigarList.get(cigarList.size() - 1).getLength() : 0;
    alnArray = Arrays.copyOfRange(alnArray, lSoftClip, alnArray.length - rSoftClip);
    qvArray = Arrays.copyOfRange(qvArray, lSoftClip, qvArray.length - rSoftClip);

    if (record.getReadNegativeStrandFlag()) { // reverse for PacBio's notation
      ArrayUtils.reverse(alnArray);
      ArrayUtils.reverse(qvArray);
      rStart += rSoftClip;
      rEnd -= lSoftClip;
    } else {
      rStart += lSoftClip;
      rEnd -= rSoftClip;
    }

    ins -= lSoftClip + rSoftClip;

    final String chrom = record.getReferenceName();

    final String rgPath = "/ref" + String.format("%06d", refInfo.get(chrom));
    final int lastPath = refGroup.last_index();
    final int pathIndex = refGroup.get(rgPath);
    if (pathIndex > lastPath) {
      try {
        h5.createGroup(rgPath, null);
      } catch (Exception e) {
        throw new RuntimeException("failed to create group " + rgPath);
      }
    }

    final String ag = rgPath + "/" + movieName;
    final Pair<Integer, Integer> begin_end = this.add(ag, alnArray, qvArray);

    int[] index = new int[EnumIdx.values().length];
    index[EnumIdx.AlnID.value] = alnIndex.size() + 1;
    index[EnumIdx.AlnGroupID.value] = alnGroup.get(ag);
    index[EnumIdx.MovieId.value] = movieInfo.get(movieName);
    index[EnumIdx.RefGroupID.value] = pathIndex;
    index[EnumIdx.tStart.value] = record.getAlignmentStart() - 1; // 1-base to 0-base
    index[EnumIdx.tEnd.value] = record.getAlignmentEnd(); // 1-base inclusive to 0-base exclusive
    index[EnumIdx.RCRefStrand.value] = record.getReadNegativeStrandFlag() ? 1 : 0;
    index[EnumIdx.HoleNumber.value] = hole;
    index[EnumIdx.SetNumber.value] = 0;
    index[EnumIdx.StrobeNumber.value] = 0;
    index[EnumIdx.MoleculeID.value] = hole;
    index[EnumIdx.rStart.value] = rStart;
    index[EnumIdx.rEnd.value] = rEnd;
    index[EnumIdx.MapQV.value] = record.getMappingQuality();
    index[EnumIdx.nM.value] = match;
    index[EnumIdx.nMM.value] = mismatch;
    index[EnumIdx.nIns.value] = ins;
    index[EnumIdx.nDel.value] = del;
    index[EnumIdx.offset_begin.value] = begin_end.getLeft();
    index[EnumIdx.offset_end.value] = begin_end.getRight();
    index[EnumIdx.nBackRead.value] = 0;
    index[EnumIdx.nBackOverlap.value] = 0;
    alnIndex.add(index);
  }

  @Override
  public void close() {
    // AlnInfo
    try {
      h5.createGroup("/AlnInfo", null);
      alnIndex.save(h5, "/AlnInfo/AlnIndex");
    } catch (Exception e) {
      log.error("failed to write AlnIndex");
    }
    // AlnGroup
    try {
      h5.createGroup("/AlnGroup", null);
      int lastIndex = alnGroup.last_index();
      int[] iBuffer = new int[lastIndex];
      String[] sBuffer = new String[lastIndex];
      for (int ii = 1; ii <= lastIndex; ++ii) {
        iBuffer[ii - 1] = ii;
        sBuffer[ii - 1] = alnGroup.get(ii);
      }
      final long[] dims = new long[] {(long) lastIndex};
      H5ScalarDSIO.Write(h5, "/AlnGroup/ID", iBuffer, dims, false);
      H5ScalarDSIO.Write(h5, "/AlnGroup/Path", sBuffer, dims, false);
    } catch (Exception e) {
      log.error("failed to write AlnGroup");
    }
    // RefGroup
    try {
      h5.createGroup("/RefGroup", null);
      int lastIndex = refGroup.last_index();
      int[] iBuffer = new int[lastIndex];
      String[] sBuffer = new String[lastIndex];
      for (int ii = 1; ii <= lastIndex; ++ii) {
        sBuffer[ii - 1] = refGroup.get(ii);
        iBuffer[ii - 1] = Integer.valueOf(sBuffer[ii - 1].substring(4));
      }
      final long[] dims = new long[] {(long) lastIndex};
      H5ScalarDSIO.Write(h5, "/RefGroup/RefInfoID", iBuffer, dims, false);
      H5ScalarDSIO.Write(h5, "/RefGroup/Path", sBuffer, dims, false);
      for (int ii = 1; ii <= lastIndex; ++ii) {
        iBuffer[ii - 1] = ii;
      }
      H5ScalarDSIO.Write(h5, "/RefGroup/ID", iBuffer, dims, false);
    } catch (Exception e) {
      e.printStackTrace();
      log.error("failed to write RefGroup");
    }
    // MovieInfo
    try {
      h5.createGroup("/MovieInfo", null);
      int lastIndex = movieInfo.last_index();
      int[] iBuffer = new int[lastIndex];
      String[] sBuffer = new String[lastIndex];
      for (int ii = 1; ii <= lastIndex; ++ii) {
        iBuffer[ii - 1] = ii;
        sBuffer[ii - 1] = movieInfo.get(ii);
      }
      final long[] dims = new long[] {(long) lastIndex};
      H5ScalarDSIO.Write(h5, "/MovieInfo/ID", iBuffer, dims, false);
      H5ScalarDSIO.Write(h5, "/MovieInfo/Name", sBuffer, dims, false);
    } catch (Exception e) {
      log.error("failed to write MovieInfo");
    }
    // RefInfo
    try {
      h5.createGroup("/RefInfo", null);
      int lastIndex = refInfo.last_index();
      int[] iBuffer = new int[lastIndex];
      String[] sBuffer = new String[lastIndex];
      for (int ii = 1; ii <= lastIndex; ++ii) {
        iBuffer[ii - 1] = ii;
        sBuffer[ii - 1] = refInfo.get(ii);
      }
      final long[] dims = new long[] {(long) lastIndex};
      H5ScalarDSIO.Write(h5, "/RefInfo/ID", iBuffer, dims, false);
      H5ScalarDSIO.Write(h5, "/RefInfo/FullName", sBuffer, dims, false);
      for (int ii = 1; ii <= lastIndex; ++ii) {
        iBuffer[ii - 1] = this.references.getSequenceDictionary().getSequence(sBuffer[ii-1]).getSequenceLength();
        sBuffer[ii - 1] = "Fake"+String.valueOf(ii);
      }
      H5ScalarDSIO.Write(h5, "/RefInfo/Length", iBuffer, dims, false);
      H5ScalarDSIO.Write(h5, "/RefInfo/MD5", sBuffer, dims, false);
    } catch (Exception e) {
      e.printStackTrace();
      log.error("failed to write RefInfo");
    }
    // FileLog
    try {
      h5.createGroup("/FileLog", null);
      final long[] dims = new long[] {(long) 1};
      String[] sBuffer = new String[1];
      int[] iBuffer = new int[1];
      sBuffer[0] = "fake";
      iBuffer[0] = 1;

      H5ScalarDSIO.Write(h5, "/FileLog/CommandLine", sBuffer, dims, false);
      H5ScalarDSIO.Write(h5, "/FileLog/ID", iBuffer, dims, false);
      H5ScalarDSIO.Write(h5, "/FileLog/Log", sBuffer, dims, false);
      H5ScalarDSIO.Write(h5, "/FileLog/Program", sBuffer, dims, false);
      H5ScalarDSIO.Write(h5, "/FileLog/Timestamp", sBuffer, dims, false);
      H5ScalarDSIO.Write(h5, "/FileLog/Version", sBuffer, dims, false);
    } catch (Exception e) {
      log.error("failed to write FileLog");
    }
    try {
      h5.close();
    } catch (HDF5Exception e) {
      log.error("failed to close h5");
    }
  }

  private static class PBIndexMap {
    private final List<String> intString = new ArrayList<>();
    private final Map<String, Integer> stringInt = new HashMap<>();

    PBIndexMap() {
      log("");
    }

    int log(String key) {
      stringInt.put(key, intString.size());
      intString.add(key);
      return last_index();
    }

    Integer get(String key) {
      Integer ret = stringInt.get(key);
      if (null == ret) {
        ret = log(key);
      }
      return ret;
    }

    int last_index() {
      return intString.size() - 1;
    }

    String get(int key) {
      return intString.get(key);
    }
  }
}
