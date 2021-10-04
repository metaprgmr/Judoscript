/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 07-20-2004  JH   Inception.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript.util;

/**
 * Used to represent stdout and stderr.
 * It never closes.
 */
public final class SystemOutput extends LinePrintWriter
{
  public SystemOutput(java.io.OutputStream os) { super(os); }

  // never close!
  public void close() { flush(); }
}

