package com.bina.lrsim.pb.bam;

import com.bina.lrsim.bioinfo.Locus;
import com.bina.lrsim.pb.EnumDat;
import com.bina.lrsim.pb.PBReadBuffer;
import com.bina.lrsim.pb.PBSpec;
import com.bina.lrsim.pb.ReadsWriter;
import htsjdk.samtools.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;

/**
 * Created by bayolau on 1/8/16.
 */
public class BAMWriter extends ReadsWriter {
  final SAMFileWriter writer;
  final SAMFileHeader header;
  final SAMReadGroupRecord readGroupRecord;
  final SAMProgramRecord programRecord;
  final SAMRecord alignment;
  final EnumMap<EnumDat, byte[]> enum_data = new EnumMap<>(EnumDat.class);
  int num_reads;

  public BAMWriter(PBSpec spec, String filename, String moviename, int firsthole) {
    super(spec, filename, moviename, firsthole);
    readGroupRecord = new SAMReadGroupRecord(moviename_);
    readGroupRecord.setPlatform("PACBIO");
    readGroupRecord.setPlatformUnit(moviename_);
    readGroupRecord.setDescription("READTYPE=SUBREAD;DeletionQV=dq;DeletionTag=dt;InsertionQV=iq;MergeQV=mq;SubstitutionQV=sq;Ipd:CodecV1=ip;BINDINGKIT=100372700;SEQUENCINGKIT=100356200;BASECALLERVERSION=2.3.0.4.162638;FRAMERATEHZ=75.000000");

    programRecord = new SAMProgramRecord("LongISLND");
    programRecord.setProgramName("LongISLND");
    programRecord.setAttribute("DS", "LongISLND is an empirical read simulator developed by Bina Technologies");

    header = new SAMFileHeader();
    header.setAttribute("pb", "3.0.1");
    header.addReadGroup(readGroupRecord);
    header.addProgramRecord(programRecord);

    alignment = new SAMRecord(header);
    writer = new SAMFileWriterFactory().makeBAMWriter(header, false, new File(filename));
    num_reads = 0;
  }

  @Override
  public void close() throws IOException {
    writer.close();
  }

  private boolean AddScore(String tag, EnumDat e, int begin, int end) {
    if (spec.getDataSet().contains(e)) {
      byte[] org = enum_data.get(e);
      byte[] tmp = new byte[end - begin];
      int shift = e.isScore ? 33 : 0;
      for (int ii = begin, jj = 0; ii < end; ++ii, ++jj) {
        tmp[jj] = (byte) (org[ii] + shift);
      }
      alignment.setAttribute(tag, new String(tmp, StandardCharsets.UTF_8));
      return true;
    }
    return false;
  }

  @Override
  public void addLast(PBReadBuffer read, ArrayList<Integer> readLengths, int score, Locus locus) {
    for (EnumDat entry : spec.getDataSet()) {
      enum_data.put(entry, read.get(entry).toByteArray());
    }
    int begin = 0;
    int end = 0;
    for (int index = 0; index < readLengths.size(); ++index) {
      end = readLengths.get(index);
      if (index % 2 == 0) {
        alignment.setReadName(moviename_ + "/" + size() + "/" + begin + "_" + end);
        alignment.setReadUnmappedFlag(true);
        alignment.setMappingQuality(255);
        alignment.setReadBases(Arrays.copyOfRange(enum_data.get(EnumDat.BaseCall), begin, end));
        alignment.setBaseQualities(Arrays.copyOfRange(enum_data.get(EnumDat.QualityValue), begin, end));
        alignment.setAttribute("RG", moviename_);
        AddScore("dq", EnumDat.DeletionQV, begin, end);
        AddScore("dt", EnumDat.DeletionTag, begin, end);
        if (spec.getDataSet().contains(EnumDat.IDPV1)) alignment.setUnsignedArrayAttribute("ip", enum_data.get(EnumDat.IDPV1));
        AddScore("iq", EnumDat.InsertionQV, begin, end);
        AddScore("mq", EnumDat.MergeQV, begin, end);
        alignment.setAttribute("np", Integer.valueOf(1));
        alignment.setAttribute("qs", Integer.valueOf(begin));
        alignment.setAttribute("qe", Integer.valueOf(end));
        alignment.setAttribute("rq", Float.valueOf(0.001f * score));
        alignment.setAttribute("sn", new float[] {7.94597f, 4.39955f, 10.1239f, 10.9799f});
        AddScore("sq", EnumDat.SubstitutionQV, begin, end);
        AddScore("st", EnumDat.SubstitutionTag, begin, end);
        alignment.setAttribute("zm", Integer.valueOf(num_reads));
        final boolean forward = (index / 2) % 2 == 0;
        alignment.setAttribute("cx", Integer.valueOf(1 | 2 | (forward ? 16 : 32)));
        writer.addAlignment(alignment);
      }
      begin = end;
    }
    ++num_reads;
  }

  @Override
  public int size() {
    return num_reads;
  }
}
