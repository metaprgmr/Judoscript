/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 04-14-2004  JH   Inception.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript.util;

import java.util.Iterator;
import java.util.Enumeration;

public class RangeIterator implements Iterator
{
  public static Iterator getIterator(Object o, int start, int end, int step, boolean upto)
                         throws IllegalArgumentException
  {
    if (o == null) {
      return new Iterator() {
        public boolean hasNext() { return false; }
        public Object next() { return null; }
        public void remove() {}
      };
    }

    Iterator iter = null;
    if (o instanceof Iterator)
      iter = (Iterator)o;
    else if (o instanceof Enumeration)
      iter = new EnumerationIterator((Enumeration)o);

    if (iter != null)
      return (start <= 0 && end < 0 && step <= 1)
             ? iter : new RangeIterator(iter, start, end, step, upto);

    throw new IllegalArgumentException("Expect an iterator or enumeration: " + o);
  }

  Iterator iter;
  int count, step;

  public RangeIterator(Enumeration en, int start, int end, int step, boolean upto) {
    this(new EnumerationIterator(en), start, end, step, upto);
  }

  public RangeIterator(Iterator iter, int start, int end, int step, boolean upto) {
    this.iter = iter;

    start = start < 0 ? 0 : start;
    for (; start>0; --start) {
      if (iter.hasNext())
        iter.next();
    }

    count = (end <= 0) ? 0x7FFFFFFF : (end-start);
    if (upto)
      --count;
    this.step = step <= 0 ? 1 : step;
  }

  public boolean hasNext() {
    for (int i=0; i<step-1; ++i) {
      if (iter.hasNext())
        iter.next();
    }
    count -= step;
    if (count <= 0)
      return false;
    return iter.hasNext();
  }

  public Object next() { return iter.next(); }

  public void remove() { iter.remove(); }

} // end of class RangeIterator.
