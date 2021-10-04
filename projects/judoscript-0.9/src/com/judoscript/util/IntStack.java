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

import java.util.EmptyStackException;

public class IntStack
{
  int[] data;
  int delta;
  int ptr = -1;

  public IntStack() { this(16,8); }

  public IntStack(int initSize, int delta) {
    data = new int[initSize];
    this.delta = delta;
  }

  public final boolean empty() { return (ptr<0); }
  public final void clear() { ptr = -1; }
  public final int size() { return ptr+1; }

  public void push(int val) {
    if (++ptr >= data.length) {
      int[] ia = new int[data.length+delta];
      System.arraycopy(data, 0, ia, 0, data.length);
      data = ia;
    }
    data[ptr] = val;
  }

  public int pop() throws EmptyStackException {
    if (empty())
      throw new EmptyStackException();
    return data[ptr--];
  }

  public int peek() throws EmptyStackException {
    return get(ptr);
  }

  public int get(int i) throws EmptyStackException {
    if (empty())
      throw new EmptyStackException();
    return data[i];
  }

  public int[] toIntArray() {
    int[] ret = new int[size()];
    System.arraycopy(data, 0, ret, 0, ret.length);
    return ret;
  }

} // end of class IntStack.
