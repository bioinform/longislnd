package com.bina.lrsim;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.bina.lrsim.pb.h5.bax.BaxH5Writer;
import com.bina.lrsim.pb.PBReadBuffer;
import com.bina.lrsim.sam.PBFastqSpec;
import htsjdk.samtools.fastq.FastqReader;
import htsjdk.samtools.fastq.FastqRecord;
import org.apache.log4j.Logger;

import com.bina.lrsim.pb.PBSpec;

/**
 * Created by bayo on 5/11/15.
 */
public class FastqH5 {
  private final static Logger log = Logger.getLogger(FastqH5.class.getName());
  private final static String usage = "parameters: list-of-fastq";

  /**
   *
   * @param args see log.info
   */
  public static void main(String[] args) throws IOException {
    if (args.length < 2) {
      log.info(usage);
      System.exit(1);
    }
    final String path = args[0];
    final PBSpec spec = new PBFastqSpec();

    final String movie_prefix = new SimpleDateFormat("'m'yyMMdd'_'HHmmss'_'").format(Calendar.getInstance().getTime());

    PBReadBuffer read = new PBReadBuffer(spec);

    int current_file_index = 0;
    String movie_name = movie_prefix + String.format("%05d", current_file_index++) + "_cFromFastq_s1_p0";
    BaxH5Writer writer = new BaxH5Writer(spec, path + "/" + movie_name + spec.getSuffix(), movie_name, 0);
    final int target_chunk = 200000000;
    int size = 0;
    for (int ii = 1; ii < args.length; ++ii) {
      final String fastq = args[ii];
      for (FastqRecord record : new FastqReader(new File(fastq))) {
        if (size > target_chunk) {
          writer.close();
          movie_name = movie_prefix + String.format("%05d", current_file_index++) + "_cFromFastq_s1_p0";
          writer = new BaxH5Writer(spec, path + "/" + movie_name + spec.getSuffix(), movie_name, 0);
          size = 0;
        }
        read.clear();
        byte[] qv = record.getBaseQualityString().getBytes();
        for (int jj = 0; jj < qv.length; ++jj) {
          qv[jj] -= 33;
        }
        read.addASCIIBases(record.getReadString().getBytes(), null /* not supposed to work with fastq */, qv);
        final List<Integer> section_ends = new ArrayList<>();
        section_ends.add(read.size());
        writer.addLast(read, section_ends, 900, null, null);
        size += read.size();
      }
    }
    writer.close();
    log.info("finished.");
  }
}
