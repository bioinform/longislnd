package com.bina.lrsim;

import java.io.*;
import java.util.Iterator;

import org.apache.log4j.Logger;

import com.bina.lrsim.h5.bax.BaxH5Reader;
import com.bina.lrsim.h5.bax.Region;
import com.bina.lrsim.interfaces.RegionGroup;
import com.bina.lrsim.simulator.samples.Samples;

/**
 * Created by bayo on 5/11/15.
 */
public class H5RegionSampler {
  private final static Logger log = Logger.getLogger(H5RegionSampler.class.getName());

  private final static String usage = "parameters: out_prefix in_file";

  /**
   * collect context-specific samples of reference->read edits from an alignment file
   * 
   * @param args see log.info
   */
  public static void main(String[] args) throws IOException {
    if (args.length != 2) {
      log.info(usage);
      System.exit(1);
    }
    final String out_prefix = args[0];
    final String in_file = args[1];

    int count = 0;

    try (DataOutputStream len_out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(Samples.Suffixes.LENGTH.filename(out_prefix))));
         DataOutputStream score_out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(Samples.Suffixes.SCORE.filename(out_prefix))))) {
      len_out.writeInt(-1);
      score_out.writeInt(-1);

      RegionGroup rg = new BaxH5Reader(in_file);
      for (Iterator<Region> itr = rg.getRegionIterator(); itr.hasNext();) {
        Region rr = itr.next();
        if (rr.getMaxInsertLength() > 0 && rr.getRegionScore() > 0) {
          len_out.writeInt(rr.getMaxInsertLength());
          score_out.writeInt(rr.getRegionScore());
          ++count;
        }
      }
    }
    try (RandomAccessFile len_out = new RandomAccessFile(Samples.Suffixes.LENGTH.filename(out_prefix), "rws");
         RandomAccessFile score_out = new RandomAccessFile(Samples.Suffixes.SCORE.filename(out_prefix), "rws")) {
      len_out.writeInt(count);
      score_out.writeInt(count);
    }
    log.info(count);
  }
}