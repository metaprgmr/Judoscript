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
 * Used to represent stdin.
 * It never closes.
 */
public final class SystemInput extends java.io.BufferedReader
{
  public SystemInput(java.io.InputStream is) { super(new java.io.InputStreamReader(is)); }

  // never close!
  public void close() {}
}

