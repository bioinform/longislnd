package com.bina.lrsim.pb;

import com.bina.lrsim.pb.h5.bax.BaxH5Writer;
import com.bina.lrsim.pb.bam.BAMWriter;
import htsjdk.samtools.BamFileIoUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;

/**
 * Created by bayolau on 1/8/16.
 */
public class ReadsWriterFactory {
  public static ReadsWriter makeWriter(PBSpec spec, String file_name, String movie_name, int first_hole) {
    return BamFileIoUtils.isBamFile(new File(file_name)) ? new BAMWriter(spec, file_name, movie_name, first_hole) : new BaxH5Writer(spec, file_name, movie_name, first_hole);
  }
}
