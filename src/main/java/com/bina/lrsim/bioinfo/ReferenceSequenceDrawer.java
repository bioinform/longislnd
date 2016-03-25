package com.bina.lrsim.bioinfo;

import com.bina.lrsim.interfaces.RandomFragmentGenerator;
import htsjdk.samtools.reference.FastaSequenceFile;
import htsjdk.samtools.reference.ReferenceSequence;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.*;

/**
 * Created by bayolau on 9/2/15.
 */
public abstract class ReferenceSequenceDrawer implements RandomFragmentGenerator {
  private final static Logger log = Logger.getLogger(ReferenceSequenceDrawer.class.getName());
  private final Map<String, Chromosome> nameChromosomeMap = new HashMap<>();
  private final List<String> name = new ArrayList<>();

  public static ReferenceSequenceDrawer Factory(final String mode, final String fasta) {
    switch (mode) {
      case "shotgun":
        return new ShotgunSequenceDrawer(fasta);
      case "fragment":
        return new FragmentSequenceDrawer(fasta);
      case "shotgunfragment":
        return new ShotgunFragmentSequenceDrawer(fasta);
    }
    log.error("sequencing type must be shotgun or fragment, given" + mode);
    return null;
  }

  public ReferenceSequenceDrawer(final String filename) {
    FastaSequenceFile reference = new FastaSequenceFile(new File(filename), true);
    for (ReferenceSequence rr = reference.nextSequence(); null != rr; rr = reference.nextSequence()) {
      name.add(rr.getName());
      nameChromosomeMap.put(rr.getName(), new Chromosome(rr));
      log.info("read " + rr.getName());
    }
  }

  public long getNonNCount() {
    long ret = 0;
    for (Map.Entry<String, Chromosome> entry : nameChromosomeMap.entrySet()) {
      ret += entry.getValue().getNonNCount();
    }
    return ret;
  }

  @Override
  public Fragment getFragment(int length, RandomGenerator gen) {
    Fragment out = null;
    while (null == out) {
      out = getSequenceImpl(length, gen);
    }
    return out;
  }

  protected abstract Fragment getSequenceImpl(int length, RandomGenerator gen);

  protected Fragment get(String name) {
    final byte[] seq = nameChromosomeMap.get(name).getSeq().getBases();
    return (seq != null) ? new Fragment(seq, new Locus(name, 0, seq.length, false)) : null;
  }

  protected Fragment get(int id) {
    return get(name.get(id));
  }

  public final List<String> getNames() {
    return Collections.unmodifiableList(name);
  }

  protected static class Chromosome {
    final ReferenceSequence seq;
    int nonNCount;

    Chromosome(ReferenceSequence seq) {
      this.seq = seq;
      nonNCount = 0;

      for (byte bb : seq.getBases()) {
        final byte value = EnumBP.ascii2value(bb);
        if (value >= 0 && value < 4) {
          nonNCount++;
        }
        if (value == EnumBP.Invalid.value) { throw new RuntimeException("reference contains " + bb); }
      }
    }

    public int size() {
      return seq.length();
    }

    public int getNonNCount() {
      return nonNCount;
    }

    public ReferenceSequence getSeq() {
      return seq;
    }

  }
}
