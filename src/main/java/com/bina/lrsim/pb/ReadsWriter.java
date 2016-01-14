package com.bina.lrsim.pb;

import com.bina.lrsim.bioinfo.Locus;

import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by bayolau on 1/8/16.
 */
public abstract class ReadsWriter implements Closeable {
  protected final String filename_;
  protected final String moviename_;
  protected final int firsthole_;
  protected final PBSpec spec;
  private final ArrayList<Locus> loci_;

  public ReadsWriter(PBSpec spec, String filename, String moviename, int firsthole) {
    this.loci_ = new ArrayList<Locus>();
    this.filename_ = filename;
    this.moviename_ = moviename;
    this.firsthole_ = firsthole;
    this.spec = spec;
  }

  @Override
  public abstract void close() throws IOException;

  public final void addLocus(Locus locus) {
    this.loci_.add(locus);
  }

  public final void writeLociBed(String prefix, String moviename, int firsthole) {
    boolean writing = false;
    for (Locus entry : this.loci_) {
      if (null != entry) {
        writing = true;
        break;
      }
    }
    if (!writing) return;
    try (FileWriter fw = new FileWriter(new File(prefix + ".bed"))) {
      int shift = 0;
      for (Locus entry : this.loci_) {
        if (null != entry) {
          fw.write(entry.getChrom());
          fw.write('\t');
          fw.write(String.valueOf(entry.getBegin0()));
          fw.write('\t');
          fw.write(String.valueOf(entry.getEnd0()));
          fw.write('\t');
          fw.write(moviename);
          fw.write('/');
          fw.write(String.valueOf(firsthole + shift));
          fw.write("\t500\t");
          fw.write(entry.isRc() ? '-' : '+');
          fw.write(System.lineSeparator());
        }
        ++shift;
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public abstract void addLast(PBReadBuffer read, ArrayList<Integer> readLengths, int score, Locus locus);

  public abstract int size();
}