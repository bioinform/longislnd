package com.bina.lrsim.bioinfo;

import junit.framework.TestCase;
import org.testng.annotations.Test;

/**
 * Created by bayo on 5/15/15.
 */
public class EnumBPTest extends TestCase{
    @Test
    public void testRC() throws Exception{
        assertEquals((byte)'C',EnumBP.ascii_rc((byte)'g'));
        assertEquals((byte)'C',EnumBP.ascii_rc((byte)'G'));

        assertEquals((byte)'G',EnumBP.ascii_rc((byte)'c'));
        assertEquals((byte)'G',EnumBP.ascii_rc((byte)'C'));

        assertEquals((byte)'A',EnumBP.ascii_rc((byte)'t'));
        assertEquals((byte)'A',EnumBP.ascii_rc((byte)'T'));

        assertEquals((byte)'T',EnumBP.ascii_rc((byte)'a'));
        assertEquals((byte)'T',EnumBP.ascii_rc((byte)'A'));
    }
}
