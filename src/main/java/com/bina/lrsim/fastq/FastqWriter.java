package com.bina.lrsim.fastq;

import com.bina.lrsim.bioinfo.Locus;
import com.bina.lrsim.pb.*;
import htsjdk.samtools.fastq.FastqRecord;
import htsjdk.samtools.fastq.FastqWriterFactory;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Created by bayolau on 1/8/16.
 */
public class FastqWriter extends ReadsWriter {
  private final static Logger log = Logger.getLogger(FastqWriter.class.getName());
  private final htsjdk.samtools.fastq.FastqWriter writer;
  private int numReads;
  private final FileWriter clrBed;

  public FastqWriter(Spec spec, String filename, String moviename, int firsthole, RunInfo runInfo) {
    super(spec, filename, moviename, firsthole);
    FileWriter tmp = null;
    try {
      tmp = new FileWriter(new File(filename + ".clr.bed"));
    } catch (IOException e) {
      tmp = null;
    }
    clrBed = tmp;

    writer = new FastqWriterFactory().newWriter(new File(filename));
    numReads = 0;
  }

  @Override
  public void close() throws IOException {
    writeLociBed(this.filename, this.moviename, this.firsthole);
    writer.close();
    clrBed.close();
  }

  @Override
  public void addLast(PBReadBuffer read, List<Integer> readLengths, int score, Locus locus, List<Locus> clrLoci) {
    // shift quality score by 33
    final byte[] org = read.get(EnumDat.QualityValue).toByteArray();
    byte[] qual = new byte[org.length];
    for (int index = 0; index < qual.length; ++index) {
      qual[index] = (byte) Math.min(Math.max(org[index] + 33, 32), 126); // readable ascii code
    }
    // basecalls
    final byte[] seq = read.get(EnumDat.BaseCall).toByteArray();
    int begin = 0;
    int end = 0;
    for (int index = 0; index < readLengths.size(); ++index) {
      end = readLengths.get(index);
      if (index % 2 == 0 && end != begin) {
        final String record_name = moviename + "/" + (firsthole + numReads) + "/" + begin + "_" + end;

        writer.write(new FastqRecord(record_name, new String(seq, begin, end - begin), null, new String(qual, begin, end - begin)));
        if (clrBed != null && clrLoci != null) {
          try {
            super.writeBedLine(clrBed, record_name, clrLoci.get(index / 2));
          } catch (IOException e) {}
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
