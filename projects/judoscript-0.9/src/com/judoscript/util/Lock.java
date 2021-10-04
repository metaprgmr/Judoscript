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

public final class Lock
{
  String cur_owner;

  public Lock() { cur_owner = null; }
  public Lock(String owner) { cur_owner = owner; }

  public synchronized boolean isLocked() { return (cur_owner != null); }
  public synchronized boolean owns(String owner) { return owner.equals(cur_owner); }

  public synchronized void acquire(String owner) {
    if (owns(owner)) return;
    while (isLocked()) {
      try { wait(); } catch(Exception e) {}
    }
    cur_owner = owner;
  }

  public synchronized void release(String owner) {
    if (owns(owner)) {
      cur_owner = null;
      this.notify();
    }
  }
}

