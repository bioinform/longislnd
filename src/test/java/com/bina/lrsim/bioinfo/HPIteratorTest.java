package com.bina.lrsim.bioinfo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import org.junit.Test;

import java.util.Iterator;

public class HPIteratorTest {

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

  private static void test1Decomposition(Context c, int left_flank, int right_flank) {
    int count1 = 0;
    for (Iterator<Context> itr1 = c.decompose(left_flank, right_flank); itr1.hasNext();) {
      Context c1 = itr1.next();
      assertEquals(c1.kmer(), c.kmer());
      assertEquals(c1.hp_len(), c.hp_len());
      ++count1;
    }
    assertEquals(1, count1);
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
    final int anchor = 2;

    int count = 0;
    for (Iterator<Context> itr = new HPIterator(fw, 0, fw.length, flank, flank, anchor, false); itr.hasNext();) {
      Context c = itr.next();
      assertEquals(1, c.hp_len());


      byte[] sequence = Kmerizer.toByteArray(c.kmer(), flank + 1 + flank);
      for (int pos = 0; pos < sequence.length; ++pos) {
        assertEquals(fw[count + pos], sequence[pos]);
      }

      test1Decomposition(c, flank, flank);

      ++count;
    }
    assertEquals(count, fw.length - flank - flank);
  }

  @Test
  public void testRCItr() {
    byte[] fw = getATCG(8);
    byte[] rc = getRC(fw);
    final int flank = 4;
    final int anchor = 2;

    int count = 0;
    for (Iterator<Context> itr = new HPIterator(fw, 0, fw.length, flank, flank, anchor, true); itr.hasNext();) {
      Context c = itr.next();
      assertEquals(1, c.hp_len());


      byte[] sequence = Kmerizer.toByteArray(c.kmer(), flank + 1 + flank);
      for (int pos = 0; pos < sequence.length; ++pos) {
        assertEquals(rc[count + pos], sequence[pos]);
      }

      test1Decomposition(c, flank, flank);

      ++count;
    }
    assertEquals(count, fw.length - flank - flank);
  }

  @Test
  public void testHPFW() {
    byte[] flanking = getATCG(2);
    final byte homo_base = (byte) 'T';
    final int homo_length = 8;

    byte[] fw = new byte[flanking.length * 2 + homo_length];
    {
      int pos = 0;
      for (int ii = 0; ii < flanking.length; ++ii, ++pos) {
        fw[pos] = flanking[ii];
      }
      for (int ii = 0; ii < homo_length; ++ii, ++pos) {
        fw[pos] = homo_base;
      }
      for (int ii = 0; ii < flanking.length; ++ii, ++pos) {
        fw[pos] = flanking[ii];
      }
    }

    final int flank = 4;
    final int anchor = 2;

    int count = 0;
    int kmer_pos = 0;
    Iterator<Context> kitr = new KmerIterator(fw, 0, fw.length, flank, flank, false);
    for (Iterator<Context> itr = new HPIterator(fw, 0, fw.length, flank, flank, anchor, false); itr.hasNext();) {
      Context c = itr.next();
      if (c.hp_len() == 1) {
        test1Decomposition(c, flank, flank);
        ++kmer_pos;

        Context kc = kitr.next();
        assertEquals(kc.kmer(), c.kmer());
        assertEquals(kc.hp_len(), c.hp_len());
      } else {
        // check homopolymer shortening
        {
          assertEquals(c.hp_len(), homo_length);
          byte[] sequence = Kmerizer.toByteArray(c.kmer(), anchor + 1 + anchor);
          assertEquals(sequence[anchor], homo_base);
          for (int aa = 0; aa < anchor; ++aa) {
            assertEquals(sequence[aa], flanking[flanking.length - anchor + aa]);
          }
        }
        // check decomposition
        for (Iterator<Context> itr1 = c.decompose(flank, flank); itr1.hasNext();) {
          Context c1 = itr1.next();
          assertEquals(1, c1.hp_len());
          byte[] sequence = Kmerizer.toByteArray(c1.kmer(), flank + 1 + flank);
          for (int pos = 0; pos < sequence.length; ++pos) {
            assertEquals(fw[kmer_pos + pos], sequence[pos]);
          }
          ++kmer_pos;
          Context kc = kitr.next();
          assertEquals(kc.kmer(), c1.kmer());
          assertEquals(kc.hp_len(), c1.hp_len());
        }
      }
      ++count;
    }
    assertFalse(kitr.hasNext());
    assertEquals(count, 2 * (flanking.length - flank) + 1);
  }


  @Test
  public void testHPRC() {
    byte[] flanking = getATCG(2);
    final byte homo_base = (byte) 'T';
    final int homo_length = 8;

    byte[] fw = new byte[flanking.length * 2 + homo_length];
    {
      int pos = 0;
      for (int ii = 0; ii < flanking.length; ++ii, ++pos) {
        fw[pos] = flanking[ii];
      }
      for (int ii = 0; ii < homo_length; ++ii, ++pos) {
        fw[pos] = homo_base;
      }
      for (int ii = 0; ii < flanking.length; ++ii, ++pos) {
        fw[pos] = flanking[ii];
      }
    }

    byte[] rc = getRC(fw);
    byte[] flanking_rc = getRC(flanking);
    final byte homo_base_rc = EnumBP.ascii_rc(homo_base);

    final int flank = 4;
    final int anchor = 2;

    int count = 0;
    int kmer_pos = 0;

    Iterator<Context> kitr = new KmerIterator(fw, 0, fw.length, flank, flank, true);
    for (Iterator<Context> itr = new HPIterator(fw, 0, fw.length, flank, flank, anchor, true); itr.hasNext();) {
      Context c = itr.next();
      if (c.hp_len() == 1) {
        test1Decomposition(c, flank, flank);
        ++kmer_pos;
        assertTrue(kitr.hasNext());
        Context kc = kitr.next();
        assertEquals(kc.kmer(), c.kmer());
        assertEquals(kc.hp_len(), c.hp_len());
      } else {
        // check homopolymer shortening
        {
          assertEquals(c.hp_len(), homo_length);
          byte[] sequence = Kmerizer.toByteArray(c.kmer(), anchor + 1 + anchor);
          assertEquals(sequence[anchor], homo_base_rc);
          for (int aa = 0; aa < anchor; ++aa) {
            assertEquals(sequence[aa], flanking_rc[flanking_rc.length - anchor + aa]);
          }
        }
        // check decomposition
        for (Iterator<Context> itr1 = c.decompose(flank, flank); itr1.hasNext();) {
          Context c1 = itr1.next();
          assertEquals(1, c1.hp_len());
          byte[] sequence = Kmerizer.toByteArray(c1.kmer(), flank + 1 + flank);
          for (int pos = 0; pos < sequence.length; ++pos) {
            assertEquals(rc[kmer_pos + pos], sequence[pos]);
          }
          ++kmer_pos;
          assertTrue(kitr.hasNext());
          Context kc = kitr.next();

          assertEquals(kc.kmer(), c1.kmer());
          assertEquals(kc.hp_len(), c1.hp_len());
        }
      }
      ++count;
    }
    assertFalse(kitr.hasNext());
    assertEquals(count, 2 * (flanking_rc.length - flank) + 1);
  }

}
