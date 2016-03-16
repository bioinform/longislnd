package com.bina.lrsim;

import com.bina.lrsim.pb.h5.bax.BaxH5Reader;
import com.bina.lrsim.pb.Spec;
import htsjdk.samtools.fastq.FastqRecord;
import htsjdk.samtools.fastq.FastqWriter;
import htsjdk.samtools.fastq.FastqWriterFactory;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by bayolau on 11/2/15.
 */
public class H5Fastq {
  private final static Logger log = Logger.getLogger(H5Fastq.class.getName());
  private final static Set<String> VALID_READ_TYPES = new HashSet<>(Arrays.asList("bax", "ccs"));
  private final static String usage = "parameters: spec list-of-files";

  public static void main(String[] args) throws IOException {
    if (args.length < 2) {
      log.info(usage);
      System.exit(1);
    }

    final String readType = args[0];
    if (!VALID_READ_TYPES.contains(readType)) {
      log.error("read_type must be bax or ccs");
      log.info(usage);
      System.exit(1);
    }

    final Spec spec = Spec.fromReadType(readType);

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
