package com.bina.lrsim;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import com.bina.lrsim.pb.Spec;
import com.bina.lrsim.pb.h5.bax.BaxH5Writer;
import com.bina.lrsim.pb.PBReadBuffer;
import htsjdk.samtools.fastq.FastqReader;
import htsjdk.samtools.fastq.FastqRecord;
import org.apache.log4j.Logger;

/**
 * Created by bayo on 5/11/15.
 */
public class FastqH5 {
  private final static Logger log = Logger.getLogger(FastqH5.class.getName());
  private final static String usage = "parameters: list-of-fastq";
  private final static Spec spec = Spec.FastqSpec;

  /**
   *
   * @param args see log.info
   */
  public static void main(String[] args) throws IOException {
    if (args.length < 2) {
      log.info(usage);
      System.exit(1);
    }

    final File path = new File(args[0]);

    final String moviePrefix = new SimpleDateFormat("'m'yyMMdd'_'HHmmss'_'").format(Calendar.getInstance().getTime());

    PBReadBuffer read = new PBReadBuffer(spec);

    int currentFileIndex = 0;
    String movieName = moviePrefix + String.format("%05d", currentFileIndex++) + "_cFromFastq_s1_p0";
    BaxH5Writer writer = new BaxH5Writer(spec, new File(path, movieName + spec.getSuffix()).getPath(), movieName, 0);
    final int targetChunk = 200000000;
    int size = 0;
    for (int ii = 1; ii < args.length; ++ii) {
      final String fastq = args[ii];
      for (FastqRecord record : new FastqReader(new File(fastq))) {
        if (size > targetChunk) {
          writer.close();
          movieName = moviePrefix + String.format("%05d", currentFileIndex++) + "_cFromFastq_s1_p0";
          writer = new BaxH5Writer(spec, new File(path, movieName + spec.getSuffix()).getPath(), movieName, 0);
          size = 0;
        }
        read.clear();
        byte[] qv = record.getBaseQualityString().getBytes();
        for (int jj = 0; jj < qv.length; ++jj) {
          qv[jj] -= 33;
        }
        read.addASCIIBases(record.getReadString().getBytes(), null /* not supposed to work with fastq */, qv);
        writer.addLast(read, Collections.singletonList(read.size()), 900, null, null);
        size += read.size();
      }
    }
    writer.close();
    log.info("finished.");
  }
}
