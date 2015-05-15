package com.bina.lrsim.simulator.samples;

import com.bina.lrsim.bioinfo.Kmerizer;
import com.bina.lrsim.simulator.EnumEvent;
import com.bina.lrsim.util.ArrayUtils;
import com.bina.lrsim.util.IntBuffer;
import org.apache.log4j.Logger;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

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
    private IntBuffer lengths_;

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

    public final IntBuffer lengths_ref() {
        return lengths_;
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
        if(left_flank_ != other.left_flank_) throw new RuntimeException("inconsistent left flank");
        if(right_flank_ != other.right_flank_) throw new RuntimeException("inconsistent right flank");
        if(k_ != other.k_) throw new RuntimeException("inconsistent k");
        if(num_kmer_ != other.num_kmer_) throw new RuntimeException("inconsistent numKmer");
        if(hp_anchor_ != other.hp_anchor_) throw new RuntimeException("inconsistent left flank");

        ArrayUtils.axpy(1,other.event_base_count_,event_base_count_);
        ArrayUtils.axpy(1,other.event_count_,event_count_);
        ArrayUtils.axpy(1,other.kmer_event_count_,kmer_event_count_);
        lengths_.addLast(other.lengths_);
        base_log.info("after accumulation "+this.toString());
    }

    /**
     * Constructor for reading from a set of files storing sampled data
     * @param prefix prefix of the set of files
     * @throws IOException
     */
    public Samples(String prefix) throws IOException {
        loadIdx(prefix);
        kmer_event_count_ = new long[num_kmer_ * EnumEvent.values().length];
        loadStats(prefix);
        loadLengths(prefix);
    }

    /**
     * Constructor for setting internal variables
     * @param leftFlank number of bp preceeding the base of interest
     * @param rightFlank number of bp trailing the base of interest
     * @param hp_anchor number of bp to anchor a homopoloymer
     */
    public Samples(int leftFlank, int rightFlank, int hp_anchor) {
        left_flank_ = leftFlank;
        right_flank_ = rightFlank;
        k_ = left_flank_ + 1 + right_flank_;
        num_kmer_ = 1 << ( 2*k_ );
        hp_anchor_ = hp_anchor;
        kmer_event_count_ = new long[num_kmer_ * EnumEvent.values().length];
        lengths_ = new IntBuffer(1000);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("samples statistics\n");
        sb.append(EnumEvent.getPrettyStats(event_base_count_));
        sb.append("\n");
        sb.append(EnumEvent.getPrettyStats(event_count_));
        return sb.toString();
    }


    protected final void writeIdx(String prefix) throws IOException {
        DataOutputStream dos = new DataOutputStream(new FileOutputStream(Suffixes.IDX.filename(prefix)));
        dos.writeInt(left_flank_);
        dos.writeInt(right_flank_);
        dos.writeInt(k_);
        dos.writeInt(num_kmer_);
        dos.writeInt(hp_anchor_);
        for(long entry: event_base_count_){
            dos.writeLong(entry);
        }
        for(long entry: event_count_){
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
        for(int ii = 0 ; ii < event_base_count_.length ; ++ii){
            event_base_count_[ii] = dis.readLong();
        }
        for(int ii = 0 ; ii < event_count_.length ; ++ii){
            event_count_[ii] = dis.readLong();
        }
        dis.close();
        base_log.info(this.toString());
    }

    protected final void writeStats(String prefix) throws IOException {
        RandomAccessFile fos = new RandomAccessFile(Suffixes.STATS.filename(prefix),"rw");
        FileChannel file = fos.getChannel();
        MappedByteBuffer buf = file.map(FileChannel.MapMode.READ_WRITE, 0, Long.SIZE/8 * kmer_event_count_.length);
        for (long entry : kmer_event_count_) {
            buf.putLong(entry);
        }
        buf.force();
        file.close();
        fos.close();
    }


    private final void loadStats(String prefix) throws IOException {
        RandomAccessFile fos = new RandomAccessFile(Suffixes.STATS.filename(prefix),"r");
        FileChannel file = fos.getChannel();
        MappedByteBuffer buf = file.map(FileChannel.MapMode.READ_ONLY, 0, Long.SIZE / 8 * kmer_event_count_.length);
        StringBuilder sb = new StringBuilder();
        sb.append(" ");
        for(int ii = 0 ; ii < kmer_event_count_.length ; ++ii){
            if(ii%EnumEvent.values().length == 0){
                sb.append(Kmerizer.toString(ii/EnumEvent.values().length,1+ left_flank_ + right_flank_)
                         );
            }
            kmer_event_count_[ii] = buf.getLong();
            sb.append(" "+kmer_event_count_[ii]);
            if( ii % EnumEvent.values().length == 3){
                sb.append("\n");
            }
        }
        base_log.debug(sb.toString());
        file.close();
        fos.close();
    }

    protected final void writeLengths(String prefix) throws IOException {
        DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(Suffixes.LENGTH.filename(prefix))));
        dos.writeInt(lengths_.size());
        for(int ii = 0 ; ii < lengths_.size() ;++ii) {
            dos.writeInt(lengths_.get(ii));
        }
        dos.close();
    }

    protected final void loadLengths(String prefix) throws IOException {
        DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(Suffixes.LENGTH.filename(prefix))));
        int new_size = dis.readInt();
        lengths_ = new IntBuffer(new_size);
        for(int ii = 0 ; ii < new_size ; ++ii) {
            lengths_.addLast(dis.readInt());
        }
        dis.close();
        base_log.info("loaded " + lengths_.size() + " length");
    }

    protected enum Suffixes{
        EVENTS(".events"),
        STATS(".stats"),
        IDX(".idx"),
        LENGTH(".len");
        private String suffix_;
        Suffixes(String s){
            suffix_ = s;
        }
        public String filename(String prefix){
            return prefix+suffix_;
        }
    }
}
