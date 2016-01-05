package com.bina.lrsim.sam;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Iterator;

import com.bina.lrsim.interfaces.EventGroup;
import com.bina.lrsim.interfaces.EventGroupFactory;

import htsjdk.samtools.SAMRecordIterator;
import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.reference.IndexedFastaSequenceFile;
import htsjdk.samtools.reference.ReferenceSequenceFile;

/**
 * Created by bayo on 6/15/15.
 */
public class SamReader implements EventGroupFactory {
  private final htsjdk.samtools.SamReader samReader;
  private final ReferenceSequenceFile references;

  public SamReader(String filename, String fasta) {
    this.samReader = SamReaderFactory.makeDefault().open(new File(filename));
    try {
      this.references = new IndexedFastaSequenceFile(new File(fasta));
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e.getMessage());
    }
  }

  @Override
  public Iterator<EventGroup> iterator() {
    return new SamIterator(samReader.iterator(), references);
  }

  private static class SamIterator implements Iterator<EventGroup> {

    private final SAMRecordIterator samItr;
    private final ReferenceSequenceFile references;

    SamIterator(SAMRecordIterator samItr, ReferenceSequenceFile references) {
      this.samItr = samItr;
      this.references = references;
    }

    @Override
    public boolean hasNext() {
      return samItr.hasNext();
    }

    @Override
    public EventGroup next() {
      return new SamAlignment(samItr.next(), references);
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException("cannot remove elements");
    }
  }
}
