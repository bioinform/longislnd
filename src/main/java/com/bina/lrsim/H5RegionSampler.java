package com.bina.lrsim;

import java.io.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.bina.lrsim.pb.RunInfo;
import com.bina.lrsim.pb.Spec;
import com.bina.lrsim.util.ProgramOptions;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SamReaderFactory;
import ncsa.hdf.object.FileFormat;
import ncsa.hdf.object.h5.H5File;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.log4j.Logger;

import com.bina.lrsim.pb.h5.bax.BaxH5Reader;
import com.bina.lrsim.pb.h5.bax.Region;
import com.bina.lrsim.simulator.samples.Samples;
import org.kohsuke.args4j.Option;

/**
 * Created by bayo on 5/11/15.
 */
public class H5RegionSampler {
  private final static Logger log = Logger.getLogger(H5RegionSampler.class.getName());
  private final static Set<String> VALID_READ_TYPES = new HashSet<>(Arrays.asList("clrbam", "bax", "ccs", "fastq"));

  public static class ModuleOptions extends ProgramOptions {
    @Option(name = "--outPrefix", required = true, usage = "prefix of output model files")
    private String outPrefix;

    @Option(name = "--inFile", required = true, usage = "input file name")
    private String inFile;

    @Option(name = "--readType", required = true, usage = "type of input data")
    private String readType;

    @Option(name = "--minReadScore", required = true, usage = "minimum read score")
    private float minReadScore;

    @Option(name = "--minPasses", required = false, usage = "minimum number of passes")
    private int minPasses = 0;
  }

  /**
   * collect context-specific samples of reference->read edits from an alignment file
   * 
   * @param args see log.info
   */
  public static void main(String[] args) throws IOException {
    final ModuleOptions po = ProgramOptions.parse(args, ModuleOptions.class);
    if (po == null) {
      System.exit(1);
    }

    if (!VALID_READ_TYPES.contains(po.readType)) {
      log.error("valid read types: " + StringUtils.join(VALID_READ_TYPES, ", "));
      System.exit(1);
    }

    final Triple<Integer, Integer, Long> triple;

    try (DataOutputStream lenOut = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(Samples.Suffixes.LENGTH.filename(po.outPrefix))));
         ObjectOutputStream runInfoOut = new ObjectOutputStream(new FileOutputStream(Samples.Suffixes.RUNINFO.filename(po.outPrefix)));
         DataOutputStream scoreOut = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(Samples.Suffixes.SCORE.filename(po.outPrefix))))) {
      lenOut.writeInt(-1);
      scoreOut.writeInt(-1);
      if (po.readType.equals("fastq")) {
        triple = SampleFASTQ(po, lenOut, runInfoOut, scoreOut);
      } else if (po.readType.equals("clrbam")) {
        triple = SampleClrBam(po, lenOut, runInfoOut, scoreOut);
      } else {
        triple = SampleFOFN(po, lenOut, runInfoOut, scoreOut);
      }
    }
    try (RandomAccessFile len_out = new RandomAccessFile(Samples.Suffixes.LENGTH.filename(po.outPrefix), "rws");
         RandomAccessFile score_out = new RandomAccessFile(Samples.Suffixes.SCORE.filename(po.outPrefix), "rws")) {
      len_out.writeInt(triple.getLeft());
      score_out.writeInt(triple.getLeft());
    }

    log.info("number of reads/subreads/bases: " + triple.getLeft() + "/" + triple.getMiddle() + "/" + triple.getRight());
  }

  static private Triple<Integer, Integer, Long> SampleFOFN(ModuleOptions po, DataOutputStream lenOut, ObjectOutputStream runInfoOut, DataOutputStream scoreOut) throws IOException {
    int numReads = 0;
    int numSubReads = 0;
    long baseCount = 0;
    final Spec spec = Spec.fromReadType(po.readType);
    { // to preserve git diff
      try (BufferedReader br = new BufferedReader(new FileReader(po.inFile))) {
        String line;
        while ((line = br.readLine()) != null) {
          String filename = line.trim(); // for each listed file
          if (filename.length() > 0) {
            log.info("processing " + filename);
            runInfoOut.writeObject(new RunInfo(new H5File(filename, FileFormat.READ)));
            for (Region rr : new BaxH5Reader(filename, spec)) {
              if (rr.isSequencing() && rr.getReadScore() >= po.minReadScore) {
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
                  for (Integer ins : lenList) {
                    if (ins > 0) {
                      ++numNonZero;
                      maxIns = Math.max(maxIns, ins);
                    }
                  }
                  if (numNonZero >= po.minPasses && maxIns > 0) {
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
    return new ImmutableTriple<>(numReads, numSubReads, baseCount);
  }

  static private Triple<Integer, Integer, Long> SampleFASTQ(ModuleOptions po, DataOutputStream lenOut, ObjectOutputStream runInfoOut, DataOutputStream scoreOut) throws IOException {
    int numReads = 0;
    int numSubReads = 0;
    long baseCount = 0;
    runInfoOut.writeObject(new RunInfo());
    try (BufferedReader br = new BufferedReader(new FileReader(po.inFile))) {
      String line;
      int count = 0;
      while ((line = br.readLine()) != null) {
        if (count == 3) {
          lenOut.writeInt(1);
          lenOut.writeInt(line.length());
          scoreOut.writeInt(999);
          baseCount += line.length();
          ++numReads;
          ++numSubReads;
        }
        count = (count + 1) % 4;
      }
    }
    return new ImmutableTriple<>(numReads, numSubReads, baseCount);
  }

  static private Triple<Integer, Integer, Long> SampleClrBam(ModuleOptions po, DataOutputStream lenOut, ObjectOutputStream runInfoOut, DataOutputStream scoreOut) throws IOException {
    int numSubReads = 0;
    long baseCount = 0;
    Set<Integer> allZmws = new HashSet<>();
    runInfoOut.writeObject(new RunInfo());
    try (htsjdk.samtools.SamReader br = SamReaderFactory.makeDefault().open(new File(po.inFile))) {
      for (SAMRecord record : br) {
        ++numSubReads;
        baseCount += record.getReadLength();
        allZmws.add((Integer) record.getAttribute("zm"));
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return new ImmutableTriple<>(allZmws.size(), numSubReads, baseCount);
  }
}
