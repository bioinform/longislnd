package com.bina.lrsim.simulator.samples;

import com.bina.lrsim.bioinfo.Kmerizer;
import com.bina.lrsim.simulator.EnumEvent;
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
    protected final long[] event_base_count_ = new long[EnumEvent.values().length];
    protected final long[] event_count_ = new long[EnumEvent.values().length];
    protected long[] kmer_event_count_;
    protected IntBuffer lengths_;
    protected int leftFlank_;
    protected int rightFlank_;
    protected int k_;
    protected int numKmer_;

    public int leftFlank() {
        return leftFlank_;
    }

    public int rightFlank() {
        return rightFlank_;
    }

    /**
     * Constructor for reading from a set of files storing sampled data
     * @param prefix prefix of the set of files
     * @throws IOException
     */
    public Samples(String prefix) throws IOException {
        loadIdx(prefix);
        kmer_event_count_ = new long[numKmer_ * EnumEvent.values().length];
        loadStats(prefix);
        loadLengths(prefix);
    }

    /**
     * Constructor for setting internal variables
     * @param leftFlank number of bp preceeding the base of interest
     * @param rightFlank number of bp trailing the base of interest
     */
    public Samples(int leftFlank, int rightFlank) {
        leftFlank_ = leftFlank;
        rightFlank_ = rightFlank;
        k_ = leftFlank_ + 1 + rightFlank_;
        numKmer_ = 1 << ( 2*k_ );
        kmer_event_count_ = new long[numKmer_ * EnumEvent.values().length];
        lengths_ = new IntBuffer(1000);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Samples\n");
        long sum = 0;
        for(long entry : event_base_count_) sum+=entry;
        sb.append("base  count:");
        for(long entry : event_base_count_) sb.append(" "+entry+"("+100*(double)entry/(double)sum+")");
        sb.append("\n");

        sum = 0;
        for(long entry : event_count_) sum+=entry;
        sb.append("event count:");
        for(long entry : event_count_) sb.append(" "+entry+"("+100*(double)entry/(double)sum+")");
        sb.append("\n");

        return sb.toString();
    }

    protected final void writeIdx(String prefix) throws IOException {
        DataOutputStream dos = new DataOutputStream(new FileOutputStream(Suffixes.IDX.filename(prefix)));
        dos.writeInt(leftFlank_);
        dos.writeInt(rightFlank_);
        dos.writeInt(k_);
        dos.writeInt(numKmer_);
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
        leftFlank_ = dis.readInt();
        rightFlank_ = dis.readInt();
        k_ = dis.readInt();
        numKmer_ = dis.readInt();
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
                sb.append(Kmerizer.toString(ii/EnumEvent.values().length,1+leftFlank_+rightFlank_)
                         );
            }
            kmer_event_count_[ii] = buf.getLong();
            sb.append(" "+kmer_event_count_[ii]);
            if( ii % EnumEvent.values().length == 3){
                sb.append("\n");
            }
        }
        base_log.info(sb.toString());
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
