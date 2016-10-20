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
    List<Integer> twoIntegers = new ArrayList<>();
    twoIntegers.add(0);
    twoIntegers.add(1);
    CircularArrayList<Integer> test = new CircularArrayList<Integer>(twoIntegers);
    int n = 1000;
    for (Integer i : test) {
      if (n == 0)
        break;
      assertTrue((n % 2) == (i % 2));
      n--;
    }
  }
}
