package com.bina.lrsim;

import java.io.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.bina.lrsim.pb.RunInfo;
import com.bina.lrsim.pb.Spec;
import ncsa.hdf.object.FileFormat;
import ncsa.hdf.object.h5.H5File;
import org.apache.log4j.Logger;

import com.bina.lrsim.pb.h5.bax.BaxH5Reader;
import com.bina.lrsim.pb.h5.bax.Region;
import com.bina.lrsim.simulator.samples.Samples;

/**
 * Created by bayo on 5/11/15.
 */
public class H5RegionSampler {
  private final static Logger log = Logger.getLogger(H5RegionSampler.class.getName());
  private final static Set<String> VALID_READ_TYPES = new HashSet<>(Arrays.asList("bax", "ccs"));
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
    final String outPrefix = args[0];
    final String inFile = args[1];
    final String readType = args[2];
    final float minReadScore = Float.parseFloat(args[3]);
    final int minPasses = (args.length > 4) ? Integer.parseInt(args[4]) : 0;

    if (!VALID_READ_TYPES.contains(readType)) {
      log.error("read_type must be bax or ccs");
      log.info(usage);
      System.exit(1);
    }

    final Spec spec = Spec.fromReadType(readType);

    int numReads = 0;
    int numSubReads = 0;
    long baseCount = 0;

    try (DataOutputStream lenOut = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(Samples.Suffixes.LENGTH.filename(outPrefix))));
         ObjectOutputStream runInfoOut = new ObjectOutputStream(new FileOutputStream(Samples.Suffixes.RUNINFO.filename(outPrefix)));
         DataOutputStream scoreOut = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(Samples.Suffixes.SCORE.filename(outPrefix))))) {
      lenOut.writeInt(-1);
      scoreOut.writeInt(-1);

      try (BufferedReader br = new BufferedReader(new FileReader(inFile))) {
        String line;
        while ((line = br.readLine()) != null) {
          String filename = line.trim(); // for each listed file
          if (filename.length() > 0) {
            log.info("processing " + filename);
            runInfoOut.writeObject(new RunInfo(new H5File(filename, FileFormat.READ)));
            for (Region rr : new BaxH5Reader(filename, spec)) {
              if (rr.isSequencing() && rr.getReadScore() >= minReadScore) {
                if (spec == Spec.CcsSpec) {
                  for (int insertLength : rr.getInsertLengths()) {
                    if (insertLength > 0) {
                      lenOut.writeInt(1);
                      lenOut.writeInt(insertLength);
                      scoreOut.writeInt((int) (rr.getReadScore() * 1000)); // quick and dirty hack
                      baseCount += insertLength;
                      ++numReads;
                      ++numSubReads;
                    }
                  }
                } else {
                  final List<Integer> lenList = rr.getInsertLengths();
                  int numNonZero = 0;
                  int maxIns = 0;
                  for(Integer ins: lenList) {
                    if(ins > 0) {
                      ++numNonZero;
                      maxIns = Math.max(maxIns,ins);
                    }
                  }
                  if (numNonZero >= minPasses && maxIns > 0) {
                    scoreOut.writeInt((int) (rr.getReadScore() * 1000)); // quick and dirty hack
                    lenOut.writeInt(numNonZero);
                    for (Integer insertLength : lenList) {
                      if (insertLength > 0) {
                        lenOut.writeInt(insertLength);
                        baseCount += insertLength;
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
    try (RandomAccessFile len_out = new RandomAccessFile(Samples.Suffixes.LENGTH.filename(outPrefix), "rws");
         RandomAccessFile score_out = new RandomAccessFile(Samples.Suffixes.SCORE.filename(outPrefix), "rws")) {
      len_out.writeInt(numReads);
      score_out.writeInt(numReads);
    }
    log.info("number of reads/subreads/bases: " + numReads + "/" + numSubReads + "/" + baseCount);
    /*
    try(ObjectInputStream runInfoIn = new ObjectInputStream(new FileInputStream(Samples.Suffixes.RUNINFO.filename(outPrefix))))
    {
      RunInfo obj = (RunInfo) runInfoIn.readObject();
      System.out.println(obj.bindingKit);
      System.out.println(obj.instrumentName);
      System.out.println(obj.movieName);
      System.out.println(obj.platformName);
      System.out.println(obj.sequencingChemistry);
      System.out.println(obj.sequencingKit);
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
    */
  }
}
