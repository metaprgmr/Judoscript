/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 09-21-2002  JH   Inception.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript.util;

import java.util.ArrayList;

public class Queue extends ArrayList
{

  public Object enq(Object item) {
    add(item);
    return item;
  }

  public Object deq() {
    try { return get(0); }
    catch(Exception e) { return null; }
    finally { try { remove(0); } catch(Exception e) {} }
  }

  public Object head() {
    try { return get(0); } catch(Exception e) { return null; }
  }

  public Object tail() {
    try { return get(size()-1); } catch(Exception e) { return null; }
  }

  public boolean empty() { return isEmpty(); }

} // end of class Queue.
