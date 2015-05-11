package com.bina.hdf5.simulator;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Created by bayo on 5/10/15.
 */
public abstract class Sampler {
    protected final long[] event_base_count_ = new long[EnumEvent.values().length];
    protected final long[] event_count_ = new long[EnumEvent.values().length];
    protected long[] kmer_event_count_;
    protected int leftFlank_;
    protected int rightFlank_;
    protected int k_;
    protected int numKmer_;

    public Sampler(String prefix) throws IOException {
        loadIdx(prefix);
        kmer_event_count_ = new long[numKmer_ * EnumEvent.values().length];
        loadStats(prefix);
    }

    public Sampler(int leftFlank, int rightFlank) {
        leftFlank_ = leftFlank;
        rightFlank_ = rightFlank;
        k_ = leftFlank_ + 1 + rightFlank_;
        numKmer_ = 1 << ( 2*k_ );
        kmer_event_count_ = new long[numKmer_ * EnumEvent.values().length];
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Sampler\n");
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
        MappedByteBuffer buf = file.map(FileChannel.MapMode.READ_ONLY, 0, Long.SIZE/8 * kmer_event_count_.length);
        for(int ii = 0 ; ii < kmer_event_count_.length ; ++ii){
            kmer_event_count_[ii] = buf.getLong();
        }
        file.close();
        fos.close();
    }

    protected enum Suffixes{
        EVENTS(".events"),
        STATS(".stats"),
        IDX(".idx");
        private String suffix_;
        Suffixes(String s){
            suffix_ = s;
        }
        public String filename(String prefix){
            return prefix+suffix_;
        }
    }
}
