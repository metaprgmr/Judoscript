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

import java.util.Vector;

public class AssociateList // implements List
{
  Vector vec = new Vector();

  public int size() { return vec.size() / 2; }

  public void add(Object key, Object val) { vec.addElement(key); vec.addElement(val); }

  public Object getKeyAt(int idx) throws ArrayIndexOutOfBoundsException {
    if (idx >= size())
      throw new ArrayIndexOutOfBoundsException("" + idx + ">=" + size());
    return vec.elementAt(idx*2);
  }

  public Object getValueAt(int idx) throws ArrayIndexOutOfBoundsException {
    if (idx >= size())
      throw new ArrayIndexOutOfBoundsException("" + idx + ">=" + size());
    return vec.elementAt(idx*2+1);
  }

  public void setAt(Object key, Object val, int idx) {
    if (idx*2+2 > vec.size())
      vec.setSize(idx*2+2);
    vec.setElementAt(key,idx*2);
    vec.setElementAt(val,idx*2+1);
  }

  public void setSize(int sz) { vec.setSize((sz<=0)?0:sz*2); }

  public void append(AssociateList al) {
    for (int i=0; i<al.size(); ++i)
      add(al.getKeyAt(i), al.getValueAt(i));
  }
}
