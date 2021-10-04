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


package com.judoscript.studio;

import java.io.*;
import com.judoscript.util.LinePrintWriter;

public interface StudioIODevice
{
  public static int OUT = 0;
  public static int ERR = 1;
  public static int LOG = 2;
  public static int INFO = 3;
  public static int TITLE = 4;

  public void close(int type);
  public void flush(int type);
  public void write(int type, int ch);
  public void write(int type, char[] cbuf, int off, int len);
  public void write(int type, String str, int off, int len);
  public void write(int type, String str);
  public void writeln(int type, String str);

  public BufferedReader getIn();
  public LinePrintWriter getOutWriter();
  public LinePrintWriter getErrWriter();
  public LinePrintWriter getLogWriter();

} // end of class StudioIODevice.
