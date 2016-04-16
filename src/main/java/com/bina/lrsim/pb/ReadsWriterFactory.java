package com.bina.lrsim.pb;

import com.bina.lrsim.pb.h5.bax.BaxH5Writer;
import com.bina.lrsim.pb.bam.BAMWriter;
import htsjdk.samtools.BamFileIoUtils;

import java.io.File;

/**
 * Created by bayolau on 1/8/16.
 */
public class ReadsWriterFactory {
  public static ReadsWriter makeWriter(Spec spec, String fileName, String movieName, int firstHole, RunInfo runInfo) {
    return BamFileIoUtils.isBamFile(new File(fileName)) ? new BAMWriter(spec, fileName, movieName, firstHole, runInfo) : new BaxH5Writer(spec, fileName, movieName, firstHole, runInfo);
  }
}
