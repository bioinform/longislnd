package com.bina.lrsim.simulator.samples;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.bina.lrsim.pb.RunInfo;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.log4j.Logger;

import com.bina.lrsim.bioinfo.*;
import com.bina.lrsim.simulator.EnumEvent;
import com.bina.lrsim.util.ArrayUtils;

/**
 * Created by bayo on 5/10/15.
 * 
 * Base class which unifies I/O of sampling mechanism, see SampleCollector (write) and SampleDrawer (read)
 * Samples here mean the error moedel learned from data.
 */
public abstract class Samples {
  private final static Logger base_log = Logger.getLogger(Samples.class.getName());

  private final long[] eventBaseCount = new long[EnumEvent.values().length];
  private final long[] eventCount = new long[EnumEvent.values().length];
  //a 2-dimensional array is stored as a 1-dimensional array
  private long[] kmerEventCount;
  private List<int[]> lengths;
  private List<Integer> scores;
  private final List<RunInfo> runinfos = new ArrayList<RunInfo>();

  private KmerIntIntCounter kmerRlenSlenCount;

  private int leftFlank;
  private int rightFlank;
  private int k;
  private int numKmer;
  private int hpAnchor;

  public final long[] getEventBaseCountRef() {
    return eventBaseCount;
  }

  public final long[] getEventCountRef() {
    return eventCount;
  }

  public final long[] getKmerEventCountRef() {
    return kmerEventCount;
  }

  public int getMaxRlen() {
    return kmerRlenSlenCount.max1;
  }

  public int getMaxSlen() {
    return kmerRlenSlenCount.max2;
  }

  public final long getKmerRlenSlenCount(int kmer, int rlen, int slen) {
    return kmerRlenSlenCount.get(kmer, rlen, slen);
  }

  public void addKmerRlenSlenCount(int kmer, int rlen, int slen) {
    kmerRlenSlenCount.increment(kmer, rlen, slen);
  }

  public final int getLengthSize() {
    return lengths.size();
  }

  public final int getNumRunInfo() {
    return runinfos.size();
  }

  public final RunInfo getRunInfo(int index) {
    return runinfos.get(index);
  }

  public final int[] getLength(int index) {
    return Arrays.copyOf(lengths.get(index), lengths.get(index).length);
  }

  public final int getScore(int index) {
    return scores.get(index);
  }

  public final int getLeftFlank() {
    return leftFlank;
  }

  public final int getRightFlank() {
    return rightFlank;
  }

  public final int getHpAnchor() {
    return hpAnchor;
  }

  public final int getNumKmer() {
    return numKmer;
  }

  public final int getK() {
    return k;
  }

  public final void accumulateStats(Samples other) {
    if (leftFlank != other.leftFlank) throw new RuntimeException("inconsistent left flank");
    if (rightFlank != other.rightFlank) throw new RuntimeException("inconsistent right flank");
    if (k != other.k) throw new RuntimeException("inconsistent k");
    if (numKmer != other.numKmer) throw new RuntimeException("inconsistent numKmer");
    if (hpAnchor != other.hpAnchor) throw new RuntimeException("inconsistent left flank");

    ArrayUtils.axpy(1, other.eventBaseCount, eventBaseCount);
    ArrayUtils.axpy(1, other.eventCount, eventCount);
    ArrayUtils.axpy(1, other.kmerEventCount, kmerEventCount);
    kmerRlenSlenCount.accumulate(other.kmerRlenSlenCount);
    runinfos.addAll(other.runinfos);
    lengths.addAll(other.lengths);
    scores.addAll(other.scores);
    base_log.info("after accumulation " + lengths.size() + " " + scores.size() /*+ " " + this.toString()*/);
  }

  /**
   * Constructor for reading from a set of files storing sampled data
   * 
   * @param prefix prefix of the set of files
   * @throws IOException
   */
  public Samples(String prefix) throws IOException {
    loadIdx(prefix);
    kmerEventCount = new long[numKmer * EnumEvent.values().length];
    loadRunInfo(prefix);
    loadStats(prefix);
    loadLengths(prefix);
    loadScores(prefix);
//    base_log.info(this.toString());
  }

  /**
   * Constructor for setting internal variables
   * 
   * @param leftFlank number of bp preceeding the base of interest
   * @param rightFlank number of bp trailing the base of interest
   * @param hp_anchor number of bp to anchor a homopoloymer
   */
  public Samples(int leftFlank, int rightFlank, int hp_anchor) {
    this.leftFlank = leftFlank;
    this.rightFlank = rightFlank;
    k = this.leftFlank + 1 + this.rightFlank;
    numKmer = 1 << (2 * k); //number of possible Kmers == 4^K == 2^(2K)
    hpAnchor = hp_anchor;
    kmerEventCount = new long[numKmer * EnumEvent.values().length];
    lengths = new ArrayList<>(1000);
    scores = new ArrayList<>(1000);
    kmerRlenSlenCount = new KmerIntIntCounter(2 * hp_anchor + 1, 100, 200);
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("samples statistics\n");
    sb.append("BP_STATS:" + EnumEvent.getPrettyStats(eventBaseCount));
    sb.append("\n");
    sb.append("EVENT_STATS: " + EnumEvent.getPrettyStats(eventCount));
    sb.append("\n");
    if (kmerRlenSlenCount != null) {
      sb.append(kmerRlenSlenCount.reduce(getHpAnchor(), getHpAnchor() + 1).toString("HP_REDUCED_STATS: "));
    }
    sb.append(stringifyKmerStats("KMER_STATS: "));
    if (kmerRlenSlenCount != null) {
      sb.append(kmerRlenSlenCount.toString("HP_STATS: "));
    }
    return sb.toString();
  }

  protected final void writeIdx(String prefix) throws IOException {
    DataOutputStream dos = new DataOutputStream(new FileOutputStream(Suffixes.IDX.filename(prefix)));
    dos.writeInt(leftFlank);
    dos.writeInt(rightFlank);
    dos.writeInt(k);
    dos.writeInt(numKmer);
    dos.writeInt(hpAnchor);
    for (long entry : eventBaseCount) {
      dos.writeLong(entry);
    }
    for (long entry : eventCount) {
      dos.writeLong(entry);
    }
    dos.flush();
    dos.close();
  }

  private void loadIdx(String prefix) throws IOException {
    DataInputStream dis = new DataInputStream(new FileInputStream(Suffixes.IDX.filename(prefix)));
    leftFlank = dis.readInt();
    rightFlank = dis.readInt();
    k = dis.readInt();
    numKmer = dis.readInt();
    hpAnchor = dis.readInt();
    //eventBaseCount.length == 4, corresponding to insertion, deletion, substitution, match
    for (int ii = 0; ii < eventBaseCount.length; ++ii) {
      eventBaseCount[ii] = dis.readLong();
    }
    //eventCount.length == 4, corresponding to insertion, deletion, substitution, match
    for (int ii = 0; ii < eventCount.length; ++ii) {
      eventCount[ii] = dis.readLong();
    }
    dis.close();
  }

  /**
   * Write summary stats to a human readable file
   * @param prefix prefix of model files
   * @throws IOException
   */
  protected final void writeSummary(String prefix) throws IOException {
    FileWriter writer = new FileWriter(new File(Suffixes.SUMMARY.filename(prefix)));
    writer.write(this.toString());
    writer.flush();
    writer.close();
  }

  protected final void writeStats(String prefix) throws IOException {
    try(ObjectOutputStream oout = new ObjectOutputStream((new FileOutputStream(Suffixes.STATS.filename(prefix))))) {
      oout.writeObject(kmerEventCount);
      oout.writeObject(kmerRlenSlenCount);
    }
    /*
    RandomAccessFile fos = new RandomAccessFile(Suffixes.STATS.filename(prefix), "rw");
    FileChannel file = fos.getChannel();
    MappedByteBuffer buf = file.map(FileChannel.MapMode.READ_WRITE, 0, Long.SIZE / 8 * kmerEventCount.length);
    for (long entry : kmerEventCount) {
      buf.putLong(entry);
    }
    buf.force();
    file.close();
    fos.close();
    */
  }

  private final void loadRunInfo(String prefix) throws IOException {
    try (ObjectInputStream runInfoIn = new ObjectInputStream(new FileInputStream(Samples.Suffixes.RUNINFO.filename(prefix)))) {
      runinfos.add((RunInfo) runInfoIn.readObject());
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
      throw new IOException();
    }
  }

  private final void loadStats(String prefix) throws IOException {
    try(ObjectInputStream oin = new ObjectInputStream((new FileInputStream(Suffixes.STATS.filename(prefix))))) {
      kmerEventCount = (long[]) oin.readObject();
      kmerRlenSlenCount = (KmerIntIntCounter) oin.readObject();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
      throw new IOException();
    }
    /*
    RandomAccessFile fos = new RandomAccessFile(Suffixes.STATS.filename(prefix), "r");
    FileChannel file = fos.getChannel();
    MappedByteBuffer buf = file.map(FileChannel.MapMode.READ_ONLY, 0, Long.SIZE / 8 * kmerEventCount.length);
    StringBuilder sb = new StringBuilder();
    sb.append(" ");
    for (int ii = 0; ii < kmerEventCount.length; ++ii) {
      kmerEventCount[ii] = buf.getLong();
    }
    base_log.debug(stringifyKmerStats("KMER_STATS: "));
    file.close();
    fos.close();
    */
  }

  public final String stringifyKmerStats(String prefix) {
    StringBuilder sb = new StringBuilder();
    long[] localLog = new long[EnumEvent.values().length];
    for (int ii = 0; ii < kmerEventCount.length; ++ii) {
      localLog[ii % EnumEvent.values().length] = kmerEventCount[ii];
      if (ii % EnumEvent.values().length + 1 == EnumEvent.values().length) {
        sb.append(prefix + Kmerizer.toString(ii / EnumEvent.values().length, 1 + leftFlank + rightFlank));
        double total = 0;
        for (long l : localLog) {
          total += l;
        }
        for (long l : localLog) {
          sb.append(String.format("%6.2f", 100 * l / total));
        }
        sb.append("       ");
        for (long l : localLog) {
          sb.append(String.format(" %d", l));
        }
        sb.append("\n");
      }
    }
    return sb.toString();
  }

  /**
   * load a list of length arrays
   *
   * during simulation, each reference sequence drawn from sampler will be simulated based on
   * one of these length arrays.
   *
   * @param prefix
   * @throws IOException
   */
  protected final void loadLengths(String prefix) throws IOException {
    DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(Suffixes.LENGTH.filename(prefix))));
    int newSize = dis.readInt();
    lengths = new ArrayList<>(newSize);
    for (int ii = 0; ii < newSize; ++ii) {
      final int numInserts = dis.readInt();
      int[] tmp = new int[numInserts];
      for (int jj = 0; jj < numInserts; ++jj) {
        tmp[jj] = dis.readInt();
      }
      lengths.add(tmp);
    }
    dis.close();
    base_log.info("loaded " + lengths.size() + " length");
  }

  protected final void loadScores(String prefix) throws IOException {
    DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(Suffixes.SCORE.filename(prefix))));
    int newSize = dis.readInt();
    scores = new ArrayList<>(newSize);
    for (int ii = 0; ii < newSize; ++ii) {
      scores.add(dis.readInt());
    }
    dis.close();
    base_log.info("loaded " + scores.size() + " score");
  }

  protected final void filterScoreLength(LengthLimits limits) {
    List<int[]> newLengths = new ArrayList<>(lengths.size());
    List<Integer> newScores = new ArrayList<>(scores.size());

    DescriptiveStatistics stats = new DescriptiveStatistics();
    for (int[] entry: lengths) {
      stats.addValue(new MultiPassSpec(entry).fragmentLength);
    }
    final double sample_median = stats.getPercentile(50);
    base_log.info("sample median fragment length: " + sample_median);
    base_log.info("target median fragment length: " + limits.scaledMedianFragmentLength);
    final double scale;
    if (limits.scaledMedianFragmentLength > 0) {
      scale = limits.scaledMedianFragmentLength / sample_median;
    }
    else {
      scale = 1;
    }
    base_log.info("scaling lengths by: " + scale);

    stats.clear();
    for (int idx = 0; idx < lengths.size(); ++idx) {
      final int[] entry = lengths.get(idx);
      for (int jj = 0; jj < entry.length; ++jj) {
        entry[jj] = (int)(scale * entry[jj] + 0.5);
      }
      final MultiPassSpec spec = new MultiPassSpec(entry);
      if ( spec.numPasses < limits.minNumPasses || spec.numPasses > limits.maxNumPasses || spec.fragmentLength > limits.maxFragmentLength || spec.fragmentLength < limits.minFragmentLength) {
        continue;
      }
      stats.addValue(spec.fragmentLength);
      newLengths.add(entry);
      newScores.add(scores.get(idx));
    }
    base_log.info("model median fragment length: " + stats.getPercentile(50));
    base_log.info("length distribution filtering decreased samples from " + lengths.size() + " to " + newLengths.size());
    lengths = newLengths;
    scores = newScores;
  }

  public enum Suffixes {
    EVENTS(".events"), STATS(".stats"), IDX(".idx"), LENGTH(".len"), SCORE(".scr"), SUMMARY(".summary"), HP(".hp"), RUNINFO(".runinfo");
    private String suffix;

    Suffixes(String s) {
      suffix = s;
    }

    public String filename(String prefix) {
      return prefix + suffix;
    }
  }

  static public class LengthLimits {
    public final int minFragmentLength;
    public final int maxFragmentLength;
    public final int minNumPasses;
    public final int maxNumPasses;
    public final int scaledMedianFragmentLength;

    public LengthLimits(int minFragmentLength, int maxFragmentLength, int minNumPasses, int maxNumPasses, int scaledMedianFragmentLength) {
      this.minFragmentLength = minFragmentLength;
      this.maxFragmentLength = maxFragmentLength;
      this.minNumPasses = minNumPasses;
      this.maxNumPasses = maxNumPasses;
      this.scaledMedianFragmentLength = scaledMedianFragmentLength;
    }
  }
}
