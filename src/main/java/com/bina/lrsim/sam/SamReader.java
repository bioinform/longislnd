package com.bina.lrsim.sam;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Iterator;

import com.bina.lrsim.interfaces.EventGroup;
import com.bina.lrsim.interfaces.EventGroupFactory;

import com.bina.lrsim.pb.Spec;
import htsjdk.samtools.*;
import htsjdk.samtools.reference.IndexedFastaSequenceFile;
import htsjdk.samtools.reference.ReferenceSequenceFile;
import org.apache.log4j.Logger;

/**
 * Created by bayo on 6/15/15.
 */
public class SamReader implements EventGroupFactory {
  private final static Logger log = Logger.getLogger(SamReader.class.getName());
  private final htsjdk.samtools.SamReader samReader;
  private final ReferenceSequenceFile references;
  private final Spec spec;

  public SamReader(File sambam, File fasta) {
    this.samReader = SamReaderFactory.makeDefault().open(sambam);
    try {
      this.references = new IndexedFastaSequenceFile(fasta);
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e.getMessage());
    }
    this.spec = Spec.FastqSpec;
  }

  public SamReader(File sambam, File fasta, Spec spec) {
    this.samReader = SamReaderFactory.makeDefault().open(sambam);
    try {
      this.references = new IndexedFastaSequenceFile(fasta);
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e.getMessage());
    }
    this.spec = spec;
  }

  @Override
  public Iterator<EventGroup> iterator() {
    return new SamIterator(samReader.iterator(), references, spec);
  }

  private static class SamIterator implements Iterator<EventGroup> {

    private final SAMRecordIterator samItr;
    private final ReferenceSequenceFile references;
    private SAMRecord buffer;
    private Spec spec;

    SamIterator(SAMRecordIterator samItr, ReferenceSequenceFile references, Spec spec) {
      this.samItr = samItr;
      this.references = references;
      this.buffer = this.seek();
      this.spec = spec;
    }

    @Override
    public boolean hasNext() {
      return this.buffer != null;
    }

    @Override
    public EventGroup next() {
      SAMRecord ret = buffer;
      buffer = seek();
      return new SamAlignment(ret, references, spec);
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException("cannot remove elements");
    }

    private SAMRecord seek() {
      SAMRecord ret = null;
      while (samItr.hasNext() && ret == null) {
        try {
          ret = samItr.next();
        } catch (SAMException e) {
          log.error("ignoring alignment. " + e.getMessage());
          ret = null;
        }
      }
      return ret;
    }
  }
}
