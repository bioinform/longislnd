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

  private final static String usage = "parameters: out_prefix fofn min_read_score";

  /**
   * collect context-specific samples of reference->read edits from an alignment file
   * 
   * @param args see log.info
   */
  public static void main(String[] args) throws IOException {
    if (args.length != 3) {
      log.info(usage);
      System.exit(1);
    }
    final String out_prefix = args[0];
    final String in_file = args[1];
    final float min_read_score = Float.parseFloat(args[2]);

    int count = 0;
    long base_count = 0;

    try (DataOutputStream len_out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(Samples.Suffixes.LENGTH.filename(out_prefix))));
         DataOutputStream score_out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(Samples.Suffixes.SCORE.filename(out_prefix))))) {
      len_out.writeInt(-1);
      score_out.writeInt(-1);

      try (BufferedReader br = new BufferedReader(new FileReader(in_file))) {
        String line;
        while ((line = br.readLine()) != null) {
          String filename = line.trim(); // for each listed file
          if (filename.length() > 0) {
            log.info("processing " + filename);
            RegionGroup rg = new BaxH5Reader(filename);
            for (Iterator<Region> itr = rg.getRegionIterator(); itr.hasNext();) {
              Region rr = itr.next();
              for (int insertLength : rr.getInsertLengths()) {
                if (insertLength > 0 && rr.getReadScore() >= min_read_score) {
                  len_out.writeInt(insertLength);
                  score_out.writeInt((int)(rr.getReadScore()*1000)); // quick and dirty hack
                  ++count;
                  base_count += insertLength;
                }
              }
            }
          }
        }
      }
    }
    try (RandomAccessFile len_out = new RandomAccessFile(Samples.Suffixes.LENGTH.filename(out_prefix), "rws");
         RandomAccessFile score_out = new RandomAccessFile(Samples.Suffixes.SCORE.filename(out_prefix), "rws")) {
      len_out.writeInt(count);
      score_out.writeInt(count);
    }
    log.info("number of reads: " + count + " number of bases " + base_count);
  }
}
