package com.bina.lrsim.bioinfo;


import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * Created by bayo on 5/15/15.
 */
public class EnumBPTest {
  @Test
  public void testRC() {
    assertEquals("RC of g", (byte) 'C', EnumBP.ascii_rc((byte) 'g'));
    assertEquals("RC of G", (byte) 'C', EnumBP.ascii_rc((byte) 'G'));

    assertEquals("RC of c", (byte) 'G', EnumBP.ascii_rc((byte) 'c'));
    assertEquals("RC of C", (byte) 'G', EnumBP.ascii_rc((byte) 'C'));

    assertEquals("RC of t", (byte) 'A', EnumBP.ascii_rc((byte) 't'));
    assertEquals("RC of T", (byte) 'A', EnumBP.ascii_rc((byte) 'T'));

    assertEquals("RC of a", (byte) 'T', EnumBP.ascii_rc((byte) 'a'));
    assertEquals("RC of A", (byte) 'T', EnumBP.ascii_rc((byte) 'A'));

    assertEquals("RC of n", (byte) 'N', EnumBP.ascii_rc((byte) 'n'));
    assertEquals("RC of N", (byte) 'N', EnumBP.ascii_rc((byte) 'N'));

    assertEquals("RC of gap", (byte) ' ', EnumBP.ascii_rc((byte) ' '));
  }
}
