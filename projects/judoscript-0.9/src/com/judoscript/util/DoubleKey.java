/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 11-25-2002  JH   Inception.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript.util;

public class DoubleKey extends Doublet
{
  public Object value;

  public DoubleKey(Object o1, Object o2, Object v) { super(o1,o2); value = v; }
  public DoubleKey(Object o1, Object v) { super(o1); value = v; }
  public DoubleKey() { value = null; }

  public String toString() { return super.toString() + "=>" + value; }
}
