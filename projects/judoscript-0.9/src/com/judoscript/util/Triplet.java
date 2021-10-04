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

public class Triplet
{
  public Object o1;
  public Object o2;
  public Object o3;

  public Triplet(Object o1, Object o2, Object o3) {
    this.o1 = o1;
    this.o2 = o2;
    this.o3 = o3;
  }

  public Triplet(Object o1, Object o2) { this(o1,o2,null); }
  public Triplet(Object o1) { this(o1,null,null); }
  public Triplet() { this(null,null,null); }

  public int hashCode() {
    return (o1.hashCode()&0x092489249) | (o2.hashCode()&0x048C22932) | (o3.hashCode()&0x025154484);
  }

  public boolean equals(Object o) {
    try {
      Triplet t = (Triplet)o;
      if (o1==null) { if (t.o1!=null) return false; } else if (!o1.equals(t.o1)) return false;
      if (o2==null) { if (t.o2!=null) return false; } else if (!o2.equals(t.o2)) return false;
      if (o3==null) { if (t.o3!=null) return false; } else if (!o3.equals(t.o3)) return false;
      return true;
    } catch(ClassCastException cce) { return false; }
  }
  public String toString() {
    StringBuffer sb = new StringBuffer();
    if (o1 != null) sb.append(o1);
    sb.append(':');
    if (o2 != null) sb.append(o2);
    sb.append(':');
    if (o3 != null) sb.append(o3);
    return sb.toString();
  }

}
