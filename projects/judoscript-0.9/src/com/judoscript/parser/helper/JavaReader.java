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


package com.judoscript.parser.helper;

import java.io.*;
import com.judoscript.util.NamedReader;

public class JavaReader extends NamedReader
{
  private BufferedReader br;
  char[] buf = new char[1024];
  int start = 0;
  int avail = 0;
  boolean inTest = false;

  public static String beginMark = "/*[judo]";
  public static String endMark = "[judo]*/";

  public JavaReader(String fname) throws IOException {
    super(true, fname);
    br = (BufferedReader)getReader();
  }

  public JavaReader(File file) throws IOException {
    super(true, file);
    br = (BufferedReader)getReader();
  }

  public JavaReader(InputStream s, String name) throws IOException {
    super(true, s, name);
    br = (BufferedReader)getReader();
  }

  public JavaReader(Reader r, String name)  throws IOException {
    super(true, r instanceof NamedReader ? ((NamedReader)r).getReader() : r,
                r instanceof NamedReader ? ((NamedReader)r).getName() : name);
    br = (BufferedReader)getReader();
  }

  /*[judo]
    . 'Hohoho!';
  [judo]*/
  public int read(char[] ca, int offset, int len) throws IOException {
    if (len <= 0) return 0;
    if (avail <= 0) {
      String line = br.readLine();
      if (line == null) return -1;
      int idx;
      if (inTest) {
        idx = line.indexOf(endMark);
        if (idx >= 0) {
          line = line.substring(0,idx);
          inTest = false;
        }
      } else {
        idx = line.indexOf(beginMark);
        if (idx >= 0) {
          idx += beginMark.length();
          line = line.substring(idx);
          StringBuffer sb = new StringBuffer();
          for (; idx >= 0; --idx)
            sb.append(' ');
          sb.append(line);
          inTest = true;
        } else {
          line = "";
        }
      }
      line += '\n';
      start = 0;
      avail = line.length();
      if (avail > buf.length)
        buf = new char[avail];
      line.getChars(0, avail, buf, 0);
    }
    if (len <= avail) {
      System.arraycopy(buf, start, ca, offset, len);
      avail -= len;
      start += len;
      return len;
    } else {
      System.arraycopy(buf, start, ca, offset, avail);
      int ret = avail;
      avail = 0;
      return ret;
    }
  }

} // end of class JavaReader.
