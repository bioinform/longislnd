package com.bina.lrsim.util;

import htsjdk.samtools.BamFileIoUtils;
import htsjdk.samtools.util.IOUtil;

import java.io.File;
import java.util.EnumSet;
import java.util.Set;

/**
 * Created by guoy28 on 11/11/16.
 * try to unify file types and suffixes
 */
public enum SuffixFixedFileType {
  clrbam(FileType.clrbam, EnumSet.of(Suffix.bam), EnumSet.of(Suffix.bam)),
  ccs(FileType.ccs, EnumSet.of(Suffix.ccsRaw), EnumSet.of(Suffix.h5align)),
  bax(FileType.bax, EnumSet.of(Suffix.baxRaw), EnumSet.of(Suffix.h5align)),
  fastq(FileType.fastq, EnumSet.of(Suffix.fq, Suffix.fastq), EnumSet.of(Suffix.sam, Suffix.bam));

  public final FileType type;
  public final Set<Suffix> inputSuffix;
  public final Set<Suffix> outputSuffix;

  SuffixFixedFileType(FileType type, Set<Suffix> inputSuffix, Set<Suffix> outputSuffix) {
    this.type = type;
    this.inputSuffix = inputSuffix;
    this.outputSuffix = outputSuffix;
  }

  public boolean hasLegalOutputSuffix(String file) {
    return hasLegalSuffix(this.outputSuffix, file);
  }
  public boolean hasLegalInputSuffix(String file) {
    return hasLegalSuffix(this.inputSuffix, file);
  }
  /**
   * take a file name, set of legal suffixes,
   * and check if its suffix
   * is one of the legal suffixes
   * @param filename
   * @return
   */
  private boolean hasLegalSuffix(Set<Suffix> legalSuffixes, String filename) {
    for (Suffix suffix : legalSuffixes) {
      if (suffix.hasLegalSuffix(filename)) {
        return true;
      }
    }
    return false;
  }
  public enum FileType {
    clrbam, ccs, bax, fastq;
  }
  public enum Suffix {
    sam(IOUtil.SAM_FILE_EXTENSION),
    bam(BamFileIoUtils.BAM_FILE_EXTENSION),
    h5align(".cmp.h5"),
    fq(".fq"),
    fastq(".fastq"),
    fa(".fa"),
    fasta(".fasta"),
    //raw data in bax.h5 format
    baxRaw(".bax.h5"),
    //raw data in bas.h5 format
    basRaw(".bas.h5"),
    ccsRaw(".ccs.h5");

    public final String legalSuffix;
    Suffix(String suffix) {
      this.legalSuffix = suffix;
    }
    /**
     * take a file name and check if its suffix
     * is one of the legal suffixes
     * @param filename
     * @return
     */
    public boolean hasLegalSuffix(String filename) {
      if (filename == null || filename.length() == 0)
        return false;
      if (filename.endsWith(this.legalSuffix))
        return true;
      return false;
    }
  }
}
