package com.bina.lrsim.bioinfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.log4j.Logger;

import com.bina.lrsim.interfaces.RandomSequenceGenerator;

import htsjdk.samtools.reference.FastaSequenceFile;
import htsjdk.samtools.reference.ReferenceSequence;

/**
 * Created by bayo on 5/7/15.
 *
 * This class is obvious hacked up and need some clean up
 */
public class ReferenceSequenceDrawer implements RandomSequenceGenerator {
  private final static Logger log = Logger.getLogger(ReferenceSequenceDrawer.class.getName());
  final Map<String, Chromosome> name_chromosome = new HashMap<String, Chromosome>();
  final ArrayList<String> name_ = new ArrayList<String>();
  final ArrayList<Long> ref_cdf_ = new ArrayList<Long>();
  FastaSequenceFile reference_;

  public ReferenceSequenceDrawer(String filename) {
    reference_ = new FastaSequenceFile(new java.io.File(filename), true);
    long num_bases = 0;
    for (ReferenceSequence rr = reference_.nextSequence(); null != rr; rr = reference_.nextSequence()) {
      name_.add(rr.getName());
      name_chromosome.put(rr.getName(), new Chromosome(rr));
      log.info("read " + rr.getName());
      num_bases += get(name_.size() - 1).length;
      ref_cdf_.add(num_bases);
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
  public Iterator<Context> getSequence(int length, int leftFlank, int rightFlank, int hp_anchor, RandomGenerator gen) {
    Iterator<Context> itr = null;
    while (null == itr) {
      itr = getSequenceImpl(length, leftFlank, rightFlank, hp_anchor, gen);
    }
    return itr;
  }

  private Iterator<Context> getSequenceImpl(int length, int leftFlank, int rightFlank, int hp_anchor, RandomGenerator gen) {
    final boolean rc = gen.nextBoolean();
    final long num_bases = ref_cdf_.get(ref_cdf_.size() - 1);
    final long pos = (num_bases <= Integer.MAX_VALUE) ? gen.nextInt((int) num_bases) : gen.nextLong() % num_bases;

    int ref_idx = 0;
    for (; ref_idx < ref_cdf_.size() && pos >= ref_cdf_.get(ref_idx); ++ref_idx) {}
    final int ref_pos = (0 == ref_idx) ? (int) pos : (int) (pos - ref_cdf_.get(ref_idx - 1));

    if (ref_pos + length <= get(ref_idx).length) {
      final byte[] chromosome = get(ref_idx);
      final byte[] sequence = new byte[length];
      if (rc) {
        for (int ss = 0, cc = chromosome.length - 1 - ref_pos; ss < length; ++ss, --cc) {
          sequence[ss] = EnumBP.ascii_rc(chromosome[cc]);
          if (sequence[ss] == 'N' || sequence[ss] == 'n') { return null; }
        }

      } else {
        for (int ss = 0; ss < length; ++ss) {
          sequence[ss] = chromosome[ref_pos + ss];
          if (sequence[ss] == 'N' || sequence[ss] == 'n') { return null; }
        }
      }
      // return new KmerIterator(get(ref_idx),ref_pos,ref_pos+length,leftFlank,rightFlank, rc);
      // return new HPIterator(get(ref_idx), ref_pos, ref_pos + length, leftFlank, rightFlank, hp_anchor, rc);
      return new HPIterator(sequence, 0, length, leftFlank, rightFlank, hp_anchor);
    }
    return null;
  }

  public byte[] get(String name) {
    return name_chromosome.get(name).seq().getBases();
  }

  public byte[] get(int id) {
    return get(name_.get(id));
  }

  private static class Chromosome {
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
