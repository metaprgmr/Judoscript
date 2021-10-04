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

import java.io.Writer;
import java.io.PrintWriter;
import java.io.OutputStream;

public class LinePrintWriter extends PrintWriter
{
  /**
   *@param pws saved and owned.
   */
  public LinePrintWriter(Writer w) { super(w); }
  public LinePrintWriter(OutputStream os) { super(os); }

  public void println()          { super.println();  flush(); }
  public void println(boolean a) { super.println(a); flush(); }
  public void println(int     a) { super.println(a); flush(); }
  public void println(long    a) { super.println(a); flush(); }
  public void println(float   a) { super.println(a); flush(); }
  public void println(double  a) { super.println(a); flush(); }
  public void println(char[]  a) { super.println(a); flush(); }
  public void println(String  a) { super.println(a); flush(); }
  public void println(Object  a) { super.println(a); flush(); }

} // end of class LinePrintWriter.

