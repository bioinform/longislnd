package com.bina.lrsim.pb.bam;

import com.bina.lrsim.bioinfo.Locus;
import com.bina.lrsim.pb.*;
import htsjdk.samtools.*;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;

/**
 * Created by bayolau on 1/8/16.
 */
public class BAMWriter extends ReadsWriter {
  private final static Logger log = Logger.getLogger(BAMWriter.class.getName());
  private final SAMFileWriter writer;
  private final SAMFileHeader header;
  private final SAMReadGroupRecord readGroupRecord;
  private final SAMProgramRecord programRecord;
  private final SAMRecord alignment;
  private final EnumMap<EnumDat, byte[]> enumData = new EnumMap<>(EnumDat.class);
  private int numReads;
  private final FileWriter clrBed;
  private final RunInfo runInfo;

  public BAMWriter(Spec spec, String filename, String moviename, int firsthole, RunInfo runInfo) {
    super(spec, filename, moviename, firsthole);
    FileWriter tmp = null;
    try {
      tmp = new FileWriter(new File(filename + ".clr.bed"));
    } catch (IOException e) {
      tmp = null;
    }
    clrBed = tmp;
    this.runInfo = runInfo;
    readGroupRecord = new SAMReadGroupRecord(this.moviename);
    readGroupRecord.setPlatform("PACBIO");
    readGroupRecord.setPlatformUnit(this.moviename);
    {
      StringBuilder sb = new StringBuilder();
      sb.append("READTYPE=SUBREAD;DeletionQV=dq;DeletionTag=dt;InsertionQV=iq;MergeQV=mq;SubstitutionQV=sq;Ipd:CodecV1=ip;");
      sb.append("BINDINGKIT=");
      sb.append(runInfo.bindingKit);
      sb.append(";SEQUENCINGKIT=");
      sb.append(runInfo.sequencingKit);
      sb.append(";BASECALLERVERSION=2.3.0.4.162638;FRAMERATEHZ=75.000000");
      readGroupRecord.setDescription(sb.toString());
    }

    programRecord = new SAMProgramRecord("LongISLND");
    programRecord.setProgramName("LongISLND");
    programRecord.setAttribute("DS", "LongISLND is an empirical read simulator developed by Bina Technologies");

    header = new SAMFileHeader();
    header.setAttribute("pb", "3.0.1");
    header.addReadGroup(readGroupRecord);
    header.addProgramRecord(programRecord);

    alignment = new SAMRecord(header);
    writer = new SAMFileWriterFactory().makeBAMWriter(header, false, new File(filename));
    numReads = 0;
  }

  @Override
  public void close() throws IOException {
    writeLociBed(this.filename, this.moviename, this.firsthole);
    writer.close();
    clrBed.close();
  }

  private boolean AddScore(String tag, EnumDat e, int begin, int end) {
    if (spec.getDataSet().contains(e)) {
      byte[] org = enumData.get(e);
      byte[] tmp = new byte[end - begin];
      int shift = e.isScore ? 33 : 0;
      for (int ii = begin, jj = 0; ii < end; ++ii, ++jj) {
        tmp[jj] = (byte) Math.min(Math.max(org[ii] + shift, 32), 126); // readable ascii code
      }
      alignment.setAttribute(tag, new String(tmp, StandardCharsets.UTF_8));
      return true;
    }
    return false;
  }

  @Override
  public void addLast(PBReadBuffer read, List<Integer> readLengths, int score, Locus locus, List<Locus> clrLoci) {
    for (EnumDat entry : spec.getDataSet()) {
      enumData.put(entry, read.get(entry).toByteArray());
    }
    int begin = 0;
    int end = 0;
    for (int index = 0; index < readLengths.size(); ++index) {
      end = readLengths.get(index);
      if (index % 2 == 0 && end != begin) {
        final String record_name = moviename + "/" + (firsthole + numReads) + "/" + begin + "_" + end;
        alignment.setReadName(record_name);
        alignment.setReadUnmappedFlag(true);
        alignment.setMappingQuality(255);
        alignment.setReadBases(Arrays.copyOfRange(enumData.get(EnumDat.BaseCall), begin, end));
        alignment.setBaseQualities(Arrays.copyOfRange(enumData.get(EnumDat.QualityValue), begin, end));
        alignment.setAttribute("RG", moviename);
        AddScore(EnumDat.DeletionQV.pbBamTag, EnumDat.DeletionQV, begin, end);
        AddScore(EnumDat.DeletionTag.pbBamTag, EnumDat.DeletionTag, begin, end);
        if (spec.getDataSet().contains(EnumDat.IPDV1)) alignment.setUnsignedArrayAttribute(EnumDat.IPDV1.pbBamTag, Arrays.copyOfRange(enumData.get(EnumDat.IPDV1), begin, end));
        AddScore(EnumDat.InsertionQV.pbBamTag, EnumDat.InsertionQV, begin, end);
        AddScore(EnumDat.MergeQV.pbBamTag, EnumDat.MergeQV, begin, end);
        alignment.setAttribute("np", Integer.valueOf(1));
        alignment.setAttribute("qs", Integer.valueOf(begin));
        alignment.setAttribute("qe", Integer.valueOf(end));
        alignment.setAttribute("rq", Float.valueOf(0.001f * score));
        alignment.setAttribute("sn", new float[] {7.94597f, 4.39955f, 10.1239f, 10.9799f});
        AddScore("sq", EnumDat.SubstitutionQV, begin, end);
        AddScore("st", EnumDat.SubstitutionTag, begin, end);
        alignment.setAttribute("zm", Integer.valueOf(firsthole + numReads));
        final boolean forward = (index / 2) % 2 == 0;
        final Integer flag = (index == 0 ? 0 : 1) | (index + 1 < readLengths.size() ? 2 : 0) | (forward ? 16 : 32);
        alignment.setAttribute("cx", flag);
        writer.addAlignment(alignment);
        if(clrBed != null && clrLoci != null) {
          try {
            super.writeBedLine(clrBed, record_name, clrLoci.get(index / 2));
          } catch (IOException e) { }
        }
      }
      begin = end;
    }
    addLocus(locus);
    ++numReads;
  }

  @Override
  public int size() {
    return numReads;
  }
}
