/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 10-17-2002  JH   Inception.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript.util;



public class _Stack extends java.util.Stack
{
  public Object pop() {
    try { return super.pop(); } catch(Exception e) { return null; }
  }

  public Object peek() {
    try { return super.peek(); } catch(Exception e) { return null; }
  }

  public Object get(int i) {
    try { return get(i); } catch(Exception e) { return null; }
  }

} // end of class _Stack.
