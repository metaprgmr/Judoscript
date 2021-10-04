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

public class Doublet
{
  public Object o1;
  public Object o2;

  public Doublet(Object o1, Object o2) { this.o1 = o1; this.o2 = o2; }
  public Doublet(Object o1) { this(o1,null); }
  public Doublet() { this(null,null); }

  public int hashCode() { return (o1.hashCode() & 0x0AAAAAAAA) | (o2.hashCode() & 0x055555555); }
  public boolean equals(Object o) {
    try {
      Doublet t = (Doublet)o;
      if (o1==null) { if (t.o1!=null) return false; } else if (!o1.equals(t.o1)) return false;
      if (o2==null) { if (t.o2!=null) return false; } else if (!o2.equals(t.o2)) return false;
      return true;
    } catch(ClassCastException cce) { return false; }
  }
  public String toString() {
    StringBuffer sb = new StringBuffer();
    if (o1 != null) sb.append(o1);
    sb.append(':');
    if (o2 != null) sb.append(o2);
    return sb.toString();
  }
}
