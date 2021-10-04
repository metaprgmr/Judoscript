/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 08-04-2002  JH   Inception.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript.util;

import java.io.*;


public final class TreePrintWriter extends LinePrintWriter
{
  PrintWriter[] writers;
  boolean[]     flags;

  /**
   *@param pws saved and owned.
   */
  public TreePrintWriter(PrintWriter[] pws) {
    super(System.out);
    writers = pws;
    flags = new boolean[pws.length];
    for (int i=0; i<flags.length; ++i)
      flags[i] = true;
  }

  public void disable(int idx) {
    try { flags[idx] = false; } catch(ArrayIndexOutOfBoundsException e) {}
  }

  public void enable(int idx) {
    try { flags[idx] = true; } catch(ArrayIndexOutOfBoundsException e) {}
  }

  public PrintWriter release(int idx) {
    PrintWriter pw = writers[idx];
    flags[idx] = false;
    writers[idx] = null;
    return pw;
  }

  public boolean isEnabled(int idx) {
    try { return flags[idx]; } catch(ArrayIndexOutOfBoundsException e) { return false; }
  }

  public void disableAll() {
    for (int i=0; i<flags.length; ++i) flags[i] = false;
  }

  public void enableAll() {
    for (int i=0; i<flags.length; ++i) flags[i] = true;
  }

  public void flush() {
    for (int i=0; i<writers.length; ++i) {
      try { writers[i].flush(); } catch(NullPointerException npe) {}
    }
  }

  public void close() {
    for (int i=0; i<writers.length; ++i) {
      try { writers[i].close(); } catch(NullPointerException npe) {}
      flags[i] = false;
    }
  }

  public boolean checkError() {
    for (int i=0; i<writers.length; ++i) {
      try { if (writers[i].checkError()) return true; } catch(NullPointerException npe) {}
    }
    return false;
  }

  public void write(int c) {
    for (int i=0; i<writers.length; ++i) {
      try { if (flags[i]) writers[i].write(c); } catch(NullPointerException npe) {}
    }
  }

  public void write(char[] a, int b, int c) {
    for (int i=0; i<writers.length; ++i) {
      try { if (flags[i]) writers[i].write(a,b,c); } catch(NullPointerException npe) {}
    }
  }

  public void write(char[] a) {
    for (int i=0; i<writers.length; ++i) {
      try { if (flags[i]) writers[i].write(a); } catch(NullPointerException npe) {}
    }
  }

  public void write(String a, int b, int c) {
    for (int i=0; i<writers.length; ++i) {
      try { if (flags[i]) writers[i].write(a,b,c); } catch(NullPointerException npe) {}
    }
  }

  public void write(String a) {
    for (int i=0; i<writers.length; ++i) {
      try { if (flags[i]) writers[i].write(a); } catch(NullPointerException npe) {}
    }
  }

  public void print(boolean b) {
    for (int i=0; i<writers.length; ++i) {
      try { if (flags[i]) writers[i].print(b); } catch(NullPointerException npe) {}
    }
  }

  public void print(char a) {
    for (int i=0; i<writers.length; ++i) {
      try { if (flags[i]) writers[i].print(a); } catch(NullPointerException npe) {}
    }
  }

  public void print(int a) {
    for (int i=0; i<writers.length; ++i) {
      try { if (flags[i]) writers[i].print(a); } catch(NullPointerException npe) {}
    }
  }

  public void print(long a) {
    for (int i=0; i<writers.length; ++i) {
      try { if (flags[i]) writers[i].print(a); } catch(NullPointerException npe) {}
    }
  }

  public void print(float a) {
    for (int i=0; i<writers.length; ++i) {
      try { if (flags[i]) writers[i].print(a); } catch(NullPointerException npe) {}
    }
  }

  public void print(double a) {
    for (int i=0; i<writers.length; ++i) {
      try { if (flags[i]) writers[i].print(a); } catch(NullPointerException npe) {}
    }
  }

  public void print(char[] a) {
    for (int i=0; i<writers.length; ++i) {
      try { if (flags[i]) writers[i].print(a); } catch(NullPointerException npe) {}
    }
  }

  public void print(String a) {
    for (int i=0; i<writers.length; ++i) {
      try { if (flags[i]) writers[i].print(a); } catch(NullPointerException npe) {}
    }
  }

  public void print(Object a) {
    for (int i=0; i<writers.length; ++i) {
      try { if (flags[i]) writers[i].print(a); } catch(NullPointerException npe) {}
    }
  }

  public void println() {
    for (int i=0; i<writers.length; ++i) {
      try {
        if (flags[i]) {
          writers[i].println();
          writers[i].flush();
        }
      } catch(NullPointerException npe) {}
    }
  }

  public void println(boolean a) {
    for (int i=0; i<writers.length; ++i) {
      try {
        if (flags[i]) {
          writers[i].println(a);
          writers[i].flush();
        }
      } catch(NullPointerException npe) {}
    }
  }

  public void println(char a) {
    for (int i=0; i<writers.length; ++i) {
      try {
        if (flags[i]) {
          writers[i].println(a);
          writers[i].flush();
        }
      } catch(NullPointerException npe) {}
    }
  }

  public void println(int a) {
    for (int i=0; i<writers.length; ++i) {
      try {
        if (flags[i]) {
          writers[i].println(a);
          writers[i].flush();
        }
      } catch(NullPointerException npe) {}
    }
  }

  public void println(long a) {
    for (int i=0; i<writers.length; ++i) {
      try {
        if (flags[i]) {
          writers[i].println(a);
          writers[i].flush();
        }
      } catch(NullPointerException npe) {}
    }
  }

  public void println(float a) {
    for (int i=0; i<writers.length; ++i) {
      try {
        if (flags[i]) {
          writers[i].println(a);
          writers[i].flush();
        }
      } catch(NullPointerException npe) {}
    }
  }

  public void println(double a) {
    for (int i=0; i<writers.length; ++i) {
      try {
        if (flags[i]) {
          writers[i].println(a);
          writers[i].flush();
        }
      } catch(NullPointerException npe) {}
    }
  }

  public void println(char[] a) {
    for (int i=0; i<writers.length; ++i) {
      try {
        if (flags[i]) {
          writers[i].println(a);
          writers[i].flush();
        }
      } catch(NullPointerException npe) {}
    }
  }

  public void println(String a) {
    for (int i=0; i<writers.length; ++i) {
      try {
        if (flags[i]) {
          writers[i].println(a);
          writers[i].flush();
        }
      } catch(NullPointerException npe) {}
    }
  }

  public void println(Object a) {
    for (int i=0; i<writers.length; ++i) {
      try {
        if (flags[i]) {
          writers[i].println(a);
          writers[i].flush();
        }
      } catch(NullPointerException npe) {}
    }
  }

} // end of class TreePrintWriter.

