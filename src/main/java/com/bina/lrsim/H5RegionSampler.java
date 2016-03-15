package com.bina.lrsim;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import com.bina.lrsim.pb.Spec;
import org.apache.log4j.Logger;

import com.bina.lrsim.pb.h5.bax.BaxH5Reader;
import com.bina.lrsim.pb.h5.bax.Region;
import com.bina.lrsim.pb.PBBaxSpec;
import com.bina.lrsim.pb.PBCcsSpec;
import com.bina.lrsim.simulator.samples.Samples;

/**
 * Created by bayo on 5/11/15.
 */
public class H5RegionSampler {
  private final static Logger log = Logger.getLogger(H5RegionSampler.class.getName());

  private final static String usage = "parameters: out_prefix fofn read_type min_read_score";

  /**
   * collect context-specific samples of reference->read edits from an alignment file
   * 
   * @param args see log.info
   */
  public static void main(String[] args) throws IOException {
    if (args.length < 4) {
      log.info(usage);
      System.exit(1);
    }
    final String out_prefix = args[0];
    final String in_file = args[1];
    final String read_type = args[2];
    final float min_read_score = Float.parseFloat(args[3]);
    final int min_passes = (args.length > 4) ? Integer.parseInt(args[4]) : 0;

    final Spec spec;
    switch (read_type) {
      case "ccs":
        spec = Spec.CcsSpec;
        break;
      case "bax":
        spec = Spec.BaxSpec;
        break;
      default:
        spec = null;
        log.info(usage);
        log.info("spec must be ccs or bax");
        System.exit(1);
    }

    int numReads = 0;
    int numSubReads = 0;
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
            for (Region rr : new BaxH5Reader(filename, spec)) {
              if (rr.isSequencing() && rr.getReadScore() >= min_read_score) {
                if (spec == Spec.CcsSpec) {
                  for (int insertLength : rr.getInsertLengths()) {
                    if (insertLength > 0) {
                      len_out.writeInt(1);
                      len_out.writeInt(insertLength);
                      score_out.writeInt((int) (rr.getReadScore() * 1000)); // quick and dirty hack
                      base_count += insertLength;
                      ++numReads;
                      ++numSubReads;
                    }
                  }
                } else {
                  final List<Integer> len_list = rr.getInsertLengths();
                  int numNonZero = 0;
                  int max_ins = 0;
                  for(Integer ins: len_list) {
                    if(ins > 0) {
                      ++numNonZero;
                      max_ins = Math.max(max_ins,ins);
                    }
                  }
                  if (numNonZero >= min_passes && max_ins > 0) {
                    score_out.writeInt((int) (rr.getReadScore() * 1000)); // quick and dirty hack
                    len_out.writeInt(numNonZero);
                    for (Integer insertLength : len_list) {
                      if (insertLength > 0) {
                        len_out.writeInt(insertLength);
                        base_count += insertLength;
                        ++numSubReads;
                      }
                    }
                    ++numReads;
                  }
                }
              }
            }
          }
        }
      }
    }
    try (RandomAccessFile len_out = new RandomAccessFile(Samples.Suffixes.LENGTH.filename(out_prefix), "rws");
         RandomAccessFile score_out = new RandomAccessFile(Samples.Suffixes.SCORE.filename(out_prefix), "rws")) {
      len_out.writeInt(numReads);
      score_out.writeInt(numReads);
    }
    log.info("number of reads/subreads/bases: " + numReads + "/" + numSubReads + "/" + base_count);
  }
}
