package com.bina.lrsim.util;

import java.util.ArrayList;
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

  public CircularArrayList () {
    this.inputList = new ArrayList<E>();
    this.size = 0;
  }

  public void add(E e) {
    this.inputList.add(e);
    this.size++;
  }

  /**
   * @warning better NOT add() during iteration
   * @return
   */
  public Iterator<E> iterator() {
    return new CircularIterator();
  }

  private class CircularIterator<E> implements Iterator<E> {
    private int index = 0;
    //in case the circular array is modified during iteration
    //we keep circulating in the old array
    private int originalSize = size;

    @Override
    public boolean hasNext() {
      return !inputList.isEmpty();
    }

    @Override
    public E next() {
      if (!hasNext()) {
        return null;
      }
      return (E) inputList.get(index++ % originalSize);
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }
}
