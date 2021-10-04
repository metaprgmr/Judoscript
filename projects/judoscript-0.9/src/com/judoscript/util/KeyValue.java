/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 03-17-2002  JH   Initial open source release.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript.util;

public class KeyValue extends Triplet
{
  public KeyValue(Object key, Object v1, Object v2) { super(key,v1,v2); }
  public KeyValue(Object k, Object v1) { super(k,v1,null); }
  public KeyValue() { super(null,null,null); }

  public Object getKey() { return o1; }
  public Object getValue() { return o2; }
  public Object getValue1() { return o3; }

  public int hashCode() { return o1.hashCode(); }
  public boolean equals(Object o) {
    try { return o1.equals(((KeyValue)o).o1); } catch(Exception e) { return false; }
  }

}
