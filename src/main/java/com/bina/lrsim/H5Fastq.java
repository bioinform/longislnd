package com.bina.lrsim;

import com.bina.lrsim.pb.h5.bax.BaxH5Reader;
import com.bina.lrsim.pb.PBBaxSpec;
import com.bina.lrsim.pb.PBCcsSpec;
import com.bina.lrsim.pb.PBSpec;
import htsjdk.samtools.fastq.FastqRecord;
import htsjdk.samtools.fastq.FastqWriter;
import htsjdk.samtools.fastq.FastqWriterFactory;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

/**
 * Created by bayolau on 11/2/15.
 */
public class H5Fastq {
  private final static Logger log = Logger.getLogger(H5Fastq.class.getName());
  private final static String usage = "parameters: spec list-of-files";

  public static void main(String[] args) throws IOException {
    if (args.length < 2) {
      log.info(usage);
      System.exit(1);
    }

    final PBSpec spec;
    switch (args[0]) {
      case "ccs":
        spec = new PBCcsSpec();
        break;
      case "bax":
        spec = new PBBaxSpec();
        break;
      default:
        spec = null;
        log.info(usage);
        log.info("spec must be ccs or bax");
        System.exit(1);
    }

    FastqWriterFactory fwf = new FastqWriterFactory();
    for (int ii = 1; ii < args.length; ++ii) {
      log.info("converting:" + args[ii]);
      try (FastqWriter writer = fwf.newWriter(new File(args[ii]+".fastq"))) {
        for (Iterator<FastqRecord> itr = new BaxH5Reader(args[ii], spec).reads(); itr.hasNext();) {
          writer.write(itr.next());
        }
      }
    }

  }
}
