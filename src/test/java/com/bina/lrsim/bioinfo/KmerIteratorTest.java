package com.bina.lrsim.bioinfo;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

import java.util.Iterator;

public class KmerIteratorTest {

  private static byte[] getATCG(int repeat) {
    byte[] out = new byte[repeat * 4];
    byte[] unit = new byte[] {(byte) 'A', (byte) 'T', (byte) 'C', (byte) 'G'};
    for (int ii = 0; ii < out.length; ++ii) {
      out[ii] = unit[ii % unit.length];
    }
    return out;
  }

  private static byte[] getRC(byte[] fw) {
    byte[] rc = new byte[fw.length];

    for (int ii = 0; ii < rc.length; ++ii) {
      rc[ii] = EnumBP.ascii_rc(fw[rc.length - 1 - ii]);
    }
    return rc;
  }

  @Test
  public void testRC() {
    byte[] unit = new byte[] {(byte) 'C', (byte) 'G', (byte) 'A', (byte) 'T'};

    byte[] rc = getRC(getATCG(8));

    for (int ii = 0; ii < rc.length; ++ii) {
      assertEquals(rc[ii], unit[ii % unit.length]);
    }
  }

  @Test
  public void testFWItr() {
    byte[] fw = getATCG(8);
    final int flank = 4;

    int count = 0;
    for (Iterator<Context> itr = new KmerIterator(fw, 0, fw.length, flank, flank); itr.hasNext();) {
      Context c = itr.next();
      assertEquals(1, c.hp_len());


      byte[] sequence = Kmerizer.toByteArray(c.kmer(), flank + 1 + flank);
      for (int pos = 0; pos < sequence.length; ++pos) {
        assertEquals(fw[count + pos], sequence[pos]);
      }

      ++count;
    }
    assertEquals(count, fw.length - flank - flank);
  }
/*
  @Test
  public void testRCItr() {
    byte[] fw = getATCG(8);
    byte[] rc = getRC(fw);
    final int flank = 4;

    int count = 0;
    for (Iterator<Context> itr = new KmerIterator(fw, 0, fw.length, flank, flank, true); itr.hasNext();) {
      Context c = itr.next();
      assertEquals(1, c.hp_len());


      byte[] sequence = Kmerizer.toByteArray(c.kmer(), flank + 1 + flank);
      for (int pos = 0; pos < sequence.length; ++pos) {
        assertEquals(rc[count + pos], sequence[pos]);
      }

      ++count;
    }
    assertEquals(count, fw.length - flank - flank);
  }
*/
}
