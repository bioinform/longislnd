package com.bina.lrsim.pb;

import com.bina.lrsim.fastq.FastqWriter;
import com.bina.lrsim.pb.h5.bax.BaxH5Writer;
import com.bina.lrsim.pb.bam.BAMWriter;
import com.bina.lrsim.util.SuffixFixedFileType;
import htsjdk.samtools.BamFileIoUtils;

import java.io.File;

/**
 * Created by bayolau on 1/8/16.
 */
public class ReadsWriterFactory {
  public static ReadsWriter makeWriter(Spec spec, String fileName, String movieName, int firstHole, RunInfo runInfo) {
    if (SuffixFixedFileType.Suffix.fq.hasLegalSuffix(fileName) || SuffixFixedFileType.Suffix.fastq.hasLegalSuffix(fileName)) {
      return new FastqWriter(spec, fileName, movieName, firstHole, runInfo);
    } else if (SuffixFixedFileType.Suffix.bam.hasLegalSuffix(fileName)) {
      return new BAMWriter(spec, fileName, movieName, firstHole, runInfo);
    } else {
      return new BaxH5Writer(spec, fileName, movieName, firstHole, runInfo);
    }
  }
}
