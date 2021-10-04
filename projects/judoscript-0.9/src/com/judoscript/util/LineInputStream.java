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

import java.io.*;


public class LineInputStream extends FilterInputStream
{
  public LineInputStream(InputStream is) {
    super(is.markSupported() ? is : new BufferedInputStream(is));
  }

  public String readLine() throws IOException {
    int ch = read();
    if (ch == -1) return null;
    StringBuffer sb = new StringBuffer();
    for (;;) {
      if ((ch == -1) || (ch == '\n'))
        break;
      if (ch == '\r') {
        mark(2);
        ch = read();
        if (ch != '\n')
          reset();
        break;
      }
      sb.append((char)ch);
      ch = read();
    }
    return sb.toString();
  }

} // end of class LineInputStream().
