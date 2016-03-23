package com.bina.lrsim;

import java.io.File;
import java.io.IOException;

import com.bina.lrsim.pb.h5.cmp.CmpH5Writer;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SamReaderFactory;
import org.apache.log4j.Logger;

/**
 * Created by bayo on 5/11/15.
 */
public class SamH5 {
  private final static Logger log = Logger.getLogger(SamH5.class.getName());
  private final static String usage = "parameters: output reference-fasta sam";

  /**
   * @param args see log.info
   */
  public static void main(String[] args) throws IOException {
    if (args.length < 3) {
      log.info(usage);
      System.exit(1);
    }

    final CmpH5Writer writer = new CmpH5Writer(args[0], new File(args[1]));

    for (int ii = 2; ii < args.length; ++ii) {
      for (SAMRecord record : SamReaderFactory.makeDefault().open(new File(args[ii]))) {
        writer.add(record);
      }
    }

    writer.close();
  }
}
