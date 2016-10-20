package com.bina.lrsim.util;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import com.bina.lrsim.util.CircularArrayList;

import static junit.framework.TestCase.assertTrue;

/**
 * Created by guoy28 on 10/19/16.
 */
public class CircularArrayListTest {
  @Test
  public void basicTest() {
    CircularArrayList<Integer> test = new CircularArrayList<Integer>();
    test.add(0);
    test.add(1);
    int n = 10;
    for (Integer i : test) {
      if (n == 0)
        break;
      assertTrue((n % 2) == (i % 2));
      n--;
    }
  }
}
