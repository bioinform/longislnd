package com.bina.lrsim.util;

import java.util.Iterator;
import java.util.List;

/**
 * Created by guoy28 on 10/19/16.
 *
 * a list of elements that can be iterated
 * infinitely in a circular fashion.
 *
 */
public class CircularArrayList<E> implements Iterable<E> {
  private List<E> inputList;
  private int size;

  public CircularArrayList (List<E> input) {
    this.inputList = input;
    this.size = input.size();
  }

  public Iterator<E> iterator() {
    return new CircularIterator();
  }

  private class CircularIterator<E> implements Iterator<E> {
    private int index = 0;

    @Override
    public boolean hasNext() {
      return !inputList.isEmpty();
    }

    @Override
    public E next() {
      if (!hasNext()) {
        return null;
      }
      return (E) inputList.get(index++ % size);
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }
}
