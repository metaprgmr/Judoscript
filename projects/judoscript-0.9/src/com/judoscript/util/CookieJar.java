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
import java.util.*;
import org.apache.commons.lang.StringUtils;

public class CookieJar
{
  Vector jar = new Vector();

  public CookieJar() {}
  public CookieJar(String filename) throws IOException { load(filename); }

  /**
   * if not exist, just add.
   * if max age <= 0, remove any.
   * otherwise, replace.
   */
  public void put(Cookie cookie) {
    int idx = jar.indexOf(cookie);
    if (idx >= 0) {
      if (cookie.getMaxAge() > 0)
        jar.removeElementAt(idx);
      else
        jar.setElementAt(cookie,idx);
    } else if (cookie.getMaxAge() > 0) {
      jar.addElement(cookie);
    }
  }

  public int size() { return jar.size(); }

  public Cookie getAt(int i) { return (Cookie)jar.elementAt(i); }

  public void load(String filename) throws IOException {
    BufferedReader br = new BufferedReader(new FileReader(filename));
    while (true) {
      String s = br.readLine();
      if (s == null) break;
      s = s.trim();
      if ((s.length()==0) || s.startsWith("#")) continue;
      Cookie cookie = fromLine(s);
      if (cookie != null)
        jar.addElement(cookie);
    }
    br.close();
  }

  public void save(String filename) throws IOException {
    PrintWriter pw = new PrintWriter(new FileWriter(filename));
    for (int i=jar.size()-1; i>=0; --i)
      pw.println(toLine((Cookie)jar.elementAt(i)));
    pw.close();
  }


  /**
   * Creates a Netscape cookies.txt-style line:
   *  domain FALSE path secure expiredate name value
   * separator is '\t'; first 'FALSE' not so sure.
   */
  public static String toLine(Cookie cookie) {
    StringBuffer sb = new StringBuffer(StringUtils.defaultString(cookie.getDomain()));
    sb.append('\t');
    sb.append("FALSE");
    sb.append('\t');
    sb.append(StringUtils.defaultString(cookie.getPath()));
    sb.append('\t');
    sb.append(cookie.getSecure()?"TRUE":"FALSE");
    sb.append('\t');
    sb.append(System.currentTimeMillis()/1000 + cookie.getMaxAge());
    sb.append('\t');
    sb.append(cookie.getName());
    sb.append('\t');
    sb.append(cookie.getValue());
    return sb.toString();
  }

  public static Cookie fromLine(String line) {
    String[] sa = Lib.string2Array(line,'\t',false);
    if ((sa==null) || (sa.length<7)) return null;
    Cookie cookie = new Cookie(sa[5],sa[6]);
    cookie.setDomain(sa[0]);
    cookie.setPath(sa[2]);
    cookie.setSecure("TRUE".equals(sa[3]));
    long t = -1;
    try { t = Long.parseLong(sa[4])-System.currentTimeMillis()/1000; } catch(Exception e) {}
    cookie.setMaxAge((int)t);
    return cookie;
  }

} // end of class CookieJar.

