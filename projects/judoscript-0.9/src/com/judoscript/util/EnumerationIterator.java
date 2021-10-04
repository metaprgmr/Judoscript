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

public class EnumerationIterator implements Iterator
{
  Enumeration en;

  public EnumerationIterator(Enumeration en) { this.en = en; }

  public boolean hasNext() { return en.hasMoreElements(); }

  public Object next() { return en.nextElement(); }

  public void remove() {}

} // end of class EnumerationIterator.
