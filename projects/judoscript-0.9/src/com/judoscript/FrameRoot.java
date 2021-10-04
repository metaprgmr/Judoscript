/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 03-17-2002  JH   Initial open source release.
 * 06-16-2003  JH   Changed from ContextRoot.java.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript;

import java.util.HashMap;
import com.judoscript.util.Lock;

/**
 * This is the run-time sandbox.
 */
public final class FrameRoot extends FrameBlock
{
  HashMap locks = null;
  HashMap signals = null;

  public FrameRoot() {
    super((String)null);
    vars = new HashMap();
  }

  public boolean isTerminal() { return true; }

  public void lock(String name, boolean doLock, String thrd) {
    Lock sync = null; // a place holder; can be any type.
    synchronized(this) {
      if (locks == null) {
        if (!doLock) return;
        locks = new HashMap();
      }
      sync = (Lock)locks.get(name);
      if (sync == null) {
        if (doLock) {
          sync = new Lock(thrd);
          locks.put(name,sync);
        }
        return;
      }
    }
    if (doLock) sync.acquire(thrd);
    else        sync.release(thrd);
  }

  public void waitFor(String name, String thrd) {
    Object o = null;
    synchronized(this) {
      if (signals == null)
        signals = new HashMap();
      o = signals.get(name);
      if (o == null) {
        o = name;
        signals.put(name,name);
      }
    }
    synchronized(o) {
      try { o.wait(); } catch(Exception e) {}
    }
  }

  public void notify(String name, boolean all, String thrd) {
    Object o = null;
    synchronized(this) {
      if (signals == null) return;
      o = signals.get(name);
      if (o == null) return;
    }
    synchronized(o) {
      if (all) o.notifyAll();
      else o.notify();
    }
  }

} // end of class FrameRoot.
