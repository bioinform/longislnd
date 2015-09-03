package com.bina.lrsim.bioinfo;

import com.bina.lrsim.interfaces.RandomSequenceGenerator;
import htsjdk.samtools.reference.FastaSequenceFile;
import htsjdk.samtools.reference.ReferenceSequence;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by laub2 on 9/2/15.
 */
public abstract class ReferenceSequenceDrawer implements RandomSequenceGenerator {
  private final static Logger log = Logger.getLogger(ReferenceSequenceDrawer.class.getName());
  final Map<String, Chromosome> name_chromosome = new HashMap<String, Chromosome>();
  final ArrayList<String> name_ = new ArrayList<String>();FastaSequenceFile reference_;

  public static ReferenceSequenceDrawer Factory(String mode, String fasta) {
    switch (mode) {
      case "shotgun":
        return new ShotgunSequenceDrawer(fasta);
      case "fragment":
        return new FragmentSequenceDrawer(fasta);
    }
    log.error("sequencing type must be shotgun or fragment, given" + mode);
    return null;
  }

  public ReferenceSequenceDrawer(String filename) {
    reference_ = new FastaSequenceFile(new java.io.File(filename), true);
    for (ReferenceSequence rr = reference_.nextSequence(); null != rr; rr = reference_.nextSequence()) {
      name_.add(rr.getName());
      name_chromosome.put(rr.getName(), new Chromosome(rr));
      log.info("read " + rr.getName());
    }
  }

  public long num_non_n() {
    long ret = 0;
    for (Map.Entry<String, Chromosome> entry : name_chromosome.entrySet()) {
      ret += entry.getValue().num_non_n();
    }
    return ret;
  }

  @Override
  public byte[] getSequence(int length, RandomGenerator gen) {
    byte[] out = null;
    while (null == out) {
      out = getSequenceImpl(length, gen);
    }
    return out;
  }

  protected abstract byte[] getSequenceImpl(int length, RandomGenerator gen);

  protected byte[] get(String name) {
    return name_chromosome.get(name).seq().getBases();
  }

  protected byte[] get(int id) {
    return get(name_.get(id));
  }

  protected static class Chromosome {
    ReferenceSequence seq_;
    int num_non_n_;

    Chromosome(ReferenceSequence seq) {
      seq_ = seq;
      num_non_n_ = 0;

      for (byte bb : seq_.getBases()) {
        final byte value = EnumBP.ascii2value(bb);
        if (value >= 0 && value < 4) {
          ++num_non_n_;
        }
        if (value == EnumBP.Invalid.value) { throw new RuntimeException("reference contains " + bb); }
      }
    }

    public int size() {
      return seq_.length();
    }

    public int num_non_n() {
      return num_non_n_;
    }

    public ReferenceSequence seq() {
      return seq_;
    }

  }
}
