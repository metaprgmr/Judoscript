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


package com.judoscript;

import java.io.*;
import java.util.StringTokenizer;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import com.judoscript.user.SysFunLib;
import com.judoscript.util.Lib;


public class ExprMixture extends ExprConcat
{
  int beginLine = 0;
  int endLine = 0;

  public ExprMixture(Expr first, Expr[] rest) { super(first, rest); }
  public ExprMixture(List ops) { super(ops); }

  public final int getBeginLine() { return beginLine; }
  public final int getEndLine() { return endLine; }
  public void setLineNumbers(int beginLine, int endLine) {
    this.beginLine = beginLine;
    this.endLine = endLine;
  }

  static String trimLastNewline(String s) {
    int idx = s.lastIndexOf('\n');
    int idx1 = s.lastIndexOf('\r');
    if (idx1 == idx - 1)                // '\r\n' found.
      idx = idx1;
    else if ((idx < 0) && (idx1 >= 0))  // '\r' found.
      idx = idx1;
    if (idx >= 0) {                     // '\r\n', '\r' or '\n' is found.
      boolean flag = true;   // make sure between NEWLINE and $ it is all whitespaces.
      for (int i = s.length()-1; i>idx; i--) {
        if (!Character.isWhitespace(s.charAt(i))) {
          flag = false;
          break;
        }
      }
      if (flag)              // only NEWLINE and whitespaces till $ -- trim it!
        return s.substring(0, idx);
    }
    return s;
  }

  static String trimFirstNewline(String s) {
    int idx = s.indexOf('\n');
    int idx1 = s.indexOf('\r');
    if ((idx < 0) && (idx1 >= 0))  // '\r' found.
      idx = idx1;

    if (idx >= 0) {                // '\r', '\n' or '\r\n' found.
      boolean flag = true;   // make sure between ^ and NEWLINE it is all whitespaces.
      for (int i = 0; i<idx; i++) {
        if (!Character.isWhitespace(s.charAt(i))) {
          flag = false;
          break;
        }
      }
      if (flag)              // only NEWLINE from ^ -- trim it!
        return s.substring(idx+1);
    }
    return s;
  }

  static boolean checkIndentation(String val, String indent) {
    StringTokenizer st = new StringTokenizer(val, "\r\n");
    while (st.hasMoreTokens()) {
      String x = st.nextToken();
      if (StringUtils.isNotBlank(x) && !x.startsWith(indent))
        return false;
    }
    return true;
  }

  static String handleIndentation(String val, int indent) {
    StringBuffer sb = new StringBuffer();
    BufferedReader br = new BufferedReader(new StringReader(val));
    boolean start = true;
    while (true) {
      try {
        String x = br.readLine();
        if (x == null) break;
        if (start) start = false;
        else sb.append(SysFunLib.lineSep);
        if (StringUtils.isNotBlank(x))
          sb.append(x.substring(indent));
      } catch(Exception e) {}
    }
    return sb.toString();
  }

} // end of class ExprMixture.
