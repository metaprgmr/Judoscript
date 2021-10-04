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

public class Quadruplet
{
  public Object o1;
  public Object o2;
  public Object o3;
  public Object o4;

  public Quadruplet(Object o1, Object o2, Object o3, Object o4) {
    this.o1 = o1;
    this.o2 = o2;
    this.o3 = o3;
    this.o4 = o4;
  }

  public Quadruplet(Object o1, Object o2, Object o3) { this(o1,o2,o3,null); }
  public Quadruplet(Object o1, Object o2) { this(o1,o2,null,null); }
  public Quadruplet(Object o1) { this(o1,null,null,null); }
  public Quadruplet() { this(null,null,null,null); }
}
