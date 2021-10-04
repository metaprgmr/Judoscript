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

import java.util.ArrayList;
import java.util.StringTokenizer;
import org.apache.commons.lang.StringUtils;

/**
 * The pattern only handles ? and *, and multiple patterns separated by |.
 */
public final class WildcardMatcher
{
  String[][] components;
  boolean caseSensitive;

  public WildcardMatcher() {
    components = null;
    caseSensitive = true;
  }

  public WildcardMatcher(String pattern, boolean caseSens) {
    caseSensitive = caseSens;
    if (!caseSens) pattern = pattern.toLowerCase();
    if (StringUtils.isBlank(pattern)) { components = null; return; }
    StringTokenizer st = new StringTokenizer(pattern, "|");
    components = new String[st.countTokens()][];
    ArrayList v = new ArrayList(3);

    for (int cnt=0; st.hasMoreTokens(); cnt++) {
      int start = 0;
      String x = st.nextToken();
      int len = x.length();
      for (int end=0; end<len; ++end) {
        char ch = x.charAt(end);
        if ((ch=='?') || (ch=='*')) {
          if (end > start) v.add(x.substring(start,end));
          if (ch=='?') {
            if ((++end<len) && (x.charAt(end)=='*')) {
              while ((++end<len) && ((x.charAt(end)=='*') || (x.charAt(end)=='?')));
              v.add("*");
            } else {
              v.add("?");
            }
          } else { // '*'
            while ((++end<len) && ((x.charAt(end)=='*') || (x.charAt(end)=='?')));
            v.add("*");
          }
          start = end;
        }
      }
      if (start<len) v.add(x.substring(start));
      String[] sa = new String[v.size()];
      int y;
      for (y=v.size()-1; y>=0; --y)
        sa[y] = (String)v.get(y);
      // check for ".../" "*" "/..." -- remove the first ending "/"
      for (y=1; y<sa.length-1; ++y) {
        if (sa[y].equals("*")) {
          if (sa[y-1].endsWith("/") && sa[y+1].startsWith("/") && sa[y-1].length()>1)
            sa[y-1] = sa[y-1].substring(0,sa[y-1].length()-1);
        }
      }
      v.clear();
      components[cnt] = sa;
    }
  }

  public void append(WildcardMatcher wm) {
    if (wm.matchesAll())
      return;
    if (matchesAll()) {
      components = new String[wm.components.length][];
      System.arraycopy(wm.components, 0, components, 0, wm.components.length);
      return;
    }
    int len = components.length + wm.components.length;
    String[][] saa = new String[len][];
    System.arraycopy(components, 0, saa, 0, components.length);
    System.arraycopy(wm.components, 0, saa, components.length, wm.components.length);
    components = saa;
  }

  public void addPattern(String pattern) {
    append( new WildcardMatcher(pattern,caseSensitive) );
  }

  public boolean matchesAll() {
    return (components == null) || (components.length == 0);
  }

  public boolean match(String text) {
    if ((components == null) || (components.length == 0))
      return true;
    if (!caseSensitive) text = text.toLowerCase();
loop:
    for (int i=0; i<components.length; ++i) {
      String[] sa = components[i];
      int ptr = 0;
      int idx = 0;
      for (; (ptr<text.length()) && (idx<sa.length); ++idx) {
        switch (sa[idx].charAt(0)) {
        case '?':
          ++ptr;
          break;
        case '*':
          if (++idx == sa.length)
            return true;
          int x = text.indexOf(sa[idx],ptr);
          if (x < 0)
            continue loop;
          ptr = x + sa[idx].length();
          break;
        default:
          if (text.regionMatches(caseSensitive,ptr,sa[idx],0,sa[idx].length())) {
            ptr += sa[idx].length();
          } else {
            continue loop;
          }
        }
      }
      if (ptr == text.length()) {
        if (idx == sa.length)
          return true;
        if ((idx == sa.length-1) && sa[idx].equals("*"))
          return true;
      }
    }
    return false;
  }

  public String toString() {
    int len = (components==null) ? 0 : components.length;
    StringBuffer sb = new StringBuffer();
    for (int i=0; i<len; i++) {
      if (i>0) sb.append("| ");
      String[] comp = components[i];
      int sz = (comp==null) ? 0 : comp.length;
      for (int j=0; j<sz; ++j) {
        sb.append(comp[j]);
        sb.append(" ");
      }
    }
    return sb.toString();
  }

/**************************************************************
  public static void main (String[] args) {
    try {
      WildcardMatcher wm = new WildcardMatcher(args[0],true);
      System.out.println(wm.match(args[1]));
    } catch(Exception e) { e.printStackTrace(); }
  }
**************************************************************/

} // end of class WildcardMatcher.
