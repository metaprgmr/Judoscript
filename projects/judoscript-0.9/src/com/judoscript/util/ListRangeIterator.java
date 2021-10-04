/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 04-16-2004  JH   Inception.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript.util;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Iterator;

/**
 * Handles Lists and arrays
 */
public class ListRangeIterator implements Iterator
{
  Object store;
  int start;
  int end;
  int step;
  boolean upto;
  boolean backward;
  private int len;
  private int ptr;

  public ListRangeIterator(Object store, int start, int end, int step, boolean upto, boolean backward)
         throws IllegalArgumentException
  {
    if (store instanceof List) {
      len = ((List)store).size();
    } else {
      Class cls = store.getClass();
      if (!cls.isArray())
        throw new IllegalArgumentException("ListIterator only handles Lists and arrays.");
      len = Array.getLength(store);
    }

    this.store = store;
    this.upto = upto;
    this.backward = backward;
    this.step = (step > 0) ? step : 1;

    if (backward) {
      this.start = (start <= 0) ? (len-1) : start;
      this.end   = (end < 0) ? 0 : end;
      this.step  = step <= 0 ? 1 : step;
      this.step = -this.step;
    } else {
      this.start = start<0 ? 0 : start;
      this.end   = end<0 ? len-1 : end;
      this.step  = step <= 0 ? 1 : step;
    }
    ptr = this.start;
  }

  public boolean hasNext() {
    if (upto)
      return backward ? (ptr >= 0 && ptr > end) : (ptr < end && ptr < len);
    else
      return backward ? (ptr >= 0 && ptr >= end) : (ptr <= end && ptr < len);
  }

  public Object next() {
    try {
      if (store instanceof List) {
        return ((List)store).get(ptr);
      } else {
        try {
          return Array.get(store, ptr);
        } catch(Exception e) {
          return null;
        }
      }
    } finally {
      ptr += step;
    }
  }

  public void remove() {
    if (store instanceof List)
      ((List)store).remove(ptr);
    // for array, can't do nothing.
  }

} // end of class ListRangeIterator.
