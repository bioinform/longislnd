package com.bina.lrsim.simulator.samples;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;

import com.bina.lrsim.bioinfo.*;
import com.bina.lrsim.simulator.EnumEvent;
import com.bina.lrsim.util.ArrayUtils;

/**
 * Created by bayo on 5/10/15.
 * 
 * Base class which unifies I/O of sampling mechanism, see SampleCollector (write) and SampleDrawer (read)
 */
public abstract class Samples {
  private final static Logger base_log = Logger.getLogger(Samples.class.getName());

  private final long[] event_base_count_ = new long[EnumEvent.values().length];
  private final long[] event_count_ = new long[EnumEvent.values().length];
  private long[] kmer_event_count_;
  private ArrayList<int[]> lengths_;
  private ArrayList<Integer> scores_;

  private KmerIntIntCounter kmer_rlen_slen_count_;

  private int left_flank_;
  private int right_flank_;
  private int k_;
  private int num_kmer_;
  private int hp_anchor_;

  public final long[] event_base_count_ref() {
    return event_base_count_;
  }

  public final long[] event_count_ref() {
    return event_count_;
  }

  public final long[] kmer_event_count_ref() {
    return kmer_event_count_;
  }

  public int max_rlen() {
    return kmer_rlen_slen_count_.max1;
  }

  public int max_slen() {
    return kmer_rlen_slen_count_.max2;
  }

  public final long kmer_rlen_slen_count(int kmer, int rlen, int slen) {
    return kmer_rlen_slen_count_.get(kmer, rlen, slen);
  }

  public void add_kmer_rlen_slen_count(int kmer, int rlen, int slen) {
    kmer_rlen_slen_count_.increment(kmer, rlen, slen);
  }

  public final int getLengthSize() {
    return lengths_.size();
  }

  public final int[] getLength(int index) {
    return Arrays.copyOf(lengths_.get(index), lengths_.get(index).length);
  }

  public final int getScore(int index) {
    return scores_.get(index);
  }

  public final int left_flank() {
    return left_flank_;
  }

  public final int right_flank() {
    return right_flank_;
  }

  public final int hp_anchor() {
    return hp_anchor_;
  }

  public final int num_kmer() {
    return num_kmer_;
  }

  public final int k() {
    return k_;
  }

  public final void accumulateStats(Samples other) {
    if (left_flank_ != other.left_flank_) throw new RuntimeException("inconsistent left flank");
    if (right_flank_ != other.right_flank_) throw new RuntimeException("inconsistent right flank");
    if (k_ != other.k_) throw new RuntimeException("inconsistent k");
    if (num_kmer_ != other.num_kmer_) throw new RuntimeException("inconsistent numKmer");
    if (hp_anchor_ != other.hp_anchor_) throw new RuntimeException("inconsistent left flank");

    ArrayUtils.axpy(1, other.event_base_count_, event_base_count_);
    ArrayUtils.axpy(1, other.event_count_, event_count_);
    ArrayUtils.axpy(1, other.kmer_event_count_, kmer_event_count_);
    lengths_.addAll(other.lengths_);
    scores_.addAll(other.scores_);
    base_log.info("after accumulation " + lengths_.size() + " " + scores_.size() + " " + this.toString());
  }

  /**
   * Constructor for reading from a set of files storing sampled data
   * 
   * @param prefix prefix of the set of files
   * @throws IOException
   */
  public Samples(String prefix) throws IOException {
    loadIdx(prefix);
    kmer_event_count_ = new long[num_kmer_ * EnumEvent.values().length];
    loadStats(prefix);
    loadLengths(prefix);
    loadScores(prefix);
    base_log.info(this.toString());
  }

  /**
   * Constructor for setting internal variables
   * 
   * @param leftFlank number of bp preceeding the base of interest
   * @param rightFlank number of bp trailing the base of interest
   * @param hp_anchor number of bp to anchor a homopoloymer
   */
  public Samples(int leftFlank, int rightFlank, int hp_anchor) {
    left_flank_ = leftFlank;
    right_flank_ = rightFlank;
    k_ = left_flank_ + 1 + right_flank_;
    num_kmer_ = 1 << (2 * k_);
    hp_anchor_ = hp_anchor;
    kmer_event_count_ = new long[num_kmer_ * EnumEvent.values().length];
    lengths_ = new ArrayList<int[]>(1000);
    scores_ = new ArrayList<Integer>(1000);
    kmer_rlen_slen_count_ = new KmerIntIntCounter(2 * hp_anchor + 1, 100, 200);
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("samples statistics\n");
    sb.append("BP_STATS:" + EnumEvent.getPrettyStats(event_base_count_));
    sb.append("\n");
    sb.append("EVENT_STATS: " + EnumEvent.getPrettyStats(event_count_));
    sb.append("\n");
    if (kmer_rlen_slen_count_ != null) {
      sb.append(kmer_rlen_slen_count_.reduce(hp_anchor(), hp_anchor() + 1).toString("HP_REDUCED_STATS: "));
    }
    sb.append(stringifyKmerStats("KMER_STATS: "));
    if (kmer_rlen_slen_count_ != null) {
      sb.append(kmer_rlen_slen_count_.toString("HP_STATS: "));
    }
    return sb.toString();
  }

  protected final void writeIdx(String prefix) throws IOException {
    DataOutputStream dos = new DataOutputStream(new FileOutputStream(Suffixes.IDX.filename(prefix)));
    dos.writeInt(left_flank_);
    dos.writeInt(right_flank_);
    dos.writeInt(k_);
    dos.writeInt(num_kmer_);
    dos.writeInt(hp_anchor_);
    for (long entry : event_base_count_) {
      dos.writeLong(entry);
    }
    for (long entry : event_count_) {
      dos.writeLong(entry);
    }
    dos.flush();
    dos.close();
  }

  private final void loadIdx(String prefix) throws IOException {
    DataInputStream dis = new DataInputStream(new FileInputStream(Suffixes.IDX.filename(prefix)));
    left_flank_ = dis.readInt();
    right_flank_ = dis.readInt();
    k_ = dis.readInt();
    num_kmer_ = dis.readInt();
    hp_anchor_ = dis.readInt();
    for (int ii = 0; ii < event_base_count_.length; ++ii) {
      event_base_count_[ii] = dis.readLong();
    }
    for (int ii = 0; ii < event_count_.length; ++ii) {
      event_count_[ii] = dis.readLong();
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
    RandomAccessFile fos = new RandomAccessFile(Suffixes.STATS.filename(prefix), "rw");
    FileChannel file = fos.getChannel();
    MappedByteBuffer buf = file.map(FileChannel.MapMode.READ_WRITE, 0, Long.SIZE / 8 * kmer_event_count_.length);
    for (long entry : kmer_event_count_) {
      buf.putLong(entry);
    }
    buf.force();
    file.close();
    fos.close();
  }

  private final void loadStats(String prefix) throws IOException {
    RandomAccessFile fos = new RandomAccessFile(Suffixes.STATS.filename(prefix), "r");
    FileChannel file = fos.getChannel();
    MappedByteBuffer buf = file.map(FileChannel.MapMode.READ_ONLY, 0, Long.SIZE / 8 * kmer_event_count_.length);
    StringBuilder sb = new StringBuilder();
    sb.append(" ");
    for (int ii = 0; ii < kmer_event_count_.length; ++ii) {
      kmer_event_count_[ii] = buf.getLong();
    }
    base_log.debug(stringifyKmerStats("KMER_STATS: "));
    file.close();
    fos.close();
  }

  public final String stringifyKmerStats(String prefix) {
    StringBuilder sb = new StringBuilder();
    long[] local_log = new long[EnumEvent.values().length];
    for (int ii = 0; ii < kmer_event_count_.length; ++ii) {
      local_log[ii % EnumEvent.values().length] = kmer_event_count_[ii];
      if (ii % EnumEvent.values().length + 1 == EnumEvent.values().length) {
        sb.append(prefix + Kmerizer.toString(ii / EnumEvent.values().length, 1 + left_flank_ + right_flank_));
        double total = 0;
        for (long l : local_log) {
          total += l;
        }
        for (long l : local_log) {
          sb.append(String.format("%6.2f", 100 * l / total));
        }
        sb.append("       ");
        for (long l : local_log) {
          sb.append(String.format(" %d", l));
        }
        sb.append("\n");
      }
    }
    return sb.toString();
  }

  protected final void loadLengths(String prefix) throws IOException {
    DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(Suffixes.LENGTH.filename(prefix))));
    int new_size = dis.readInt();
    lengths_ = new ArrayList<int[]>(new_size);
    for (int ii = 0; ii < new_size; ++ii) {
      final int num_inserts = dis.readInt();
      int[] tmp = new int[num_inserts];
      for (int jj = 0; jj < num_inserts; ++jj) {
        tmp[jj] = dis.readInt();
      }
      lengths_.add(tmp);
    }
    dis.close();
    base_log.info("loaded " + lengths_.size() + " length");
  }

  protected final void loadScores(String prefix) throws IOException {
    DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(Suffixes.SCORE.filename(prefix))));
    int new_size = dis.readInt();
    scores_ = new ArrayList<Integer>(new_size);
    for (int ii = 0; ii < new_size; ++ii) {
      scores_.add(dis.readInt());
    }
    dis.close();
    base_log.info("loaded " + scores_.size() + " score");
  }

  protected final void filterScoreLength(LengthLimits limits) {
    ArrayList<int[]> new_lengths_ = new ArrayList<int[]>(lengths_.size());
    ArrayList<Integer> new_scores_ = new ArrayList<Integer>(scores_.size());

    for (int idx = 0; idx < lengths_.size(); ++idx) {
      final int local_max = NumberUtils.max(lengths_.get(idx));
      int num_passes = 0;
      for(int length: lengths_.get(idx)) {
        if (length > Heuristics.SMRT_INSERT_FRACTION * local_max) { ++num_passes; } // ugly hack, should be done something else, no time to fix
      }
      if ( num_passes < limits.min_num_passes || num_passes > limits.max_num_passes || local_max > limits.max_fragment_length || local_max < limits.min_fragment_length) {
        continue;
      }
      new_lengths_.add(lengths_.get(idx));
      new_scores_.add(scores_.get(idx));
    }
    base_log.info("length distribution filtering decreased samples from " + lengths_.size() + " to " + new_lengths_.size());
    lengths_ = new_lengths_;
    scores_ = new_scores_;
  }

  public enum Suffixes {
    EVENTS(".events"), STATS(".stats"), IDX(".idx"), LENGTH(".len"), SCORE(".scr"), SUMMARY(".summary"), HP(".hp");
    private String suffix_;

    Suffixes(String s) {
      suffix_ = s;
    }

    public String filename(String prefix) {
      return prefix + suffix_;
    }
  }

  static public class LengthLimits {
    public final int min_fragment_length;
    public final int max_fragment_length;
    public final int min_num_passes;
    public final int max_num_passes;

    public LengthLimits(int min_fragment_length, int max_fragment_length, int min_num_passes, int max_num_passes) {
      this.min_fragment_length = min_fragment_length;
      this.max_fragment_length = max_fragment_length;
      this.min_num_passes = min_num_passes;
      this.max_num_passes = max_num_passes;
    }
  }
}
