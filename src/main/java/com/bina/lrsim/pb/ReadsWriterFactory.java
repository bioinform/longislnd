package com.bina.lrsim.pb;

import com.bina.lrsim.pb.h5.bax.BaxH5Writer;
import com.bina.lrsim.pb.bam.BAMWriter;

/**
 * Created by bayolau on 1/8/16.
 */
public class ReadsWriterFactory {
  public static ReadsWriter makeWriter(PBSpec spec, String file_name, String movie_name, int first_hole) {
    if (file_name.length() > 4 && file_name.substring(file_name.length() - 4).equals(".bam")) {
      return new BAMWriter(spec, file_name, movie_name, first_hole);
    } else {
      return new BaxH5Writer(spec, file_name, movie_name, first_hole);
    }
  }
}
