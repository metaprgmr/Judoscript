/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 11-13-2002  JH   Inception -- taken out of Lib.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript.util;

import org.apache.commons.lang.StringUtils;

public class SimpleLib
{
  public static int parseInt(String s, int defVal) {
    try { return Integer.parseInt(s); } catch(NumberFormatException e) {
      try { return (int)parseLongHex(s); } catch(NumberFormatException e1) { return defVal; }
    }
  }

  /**
   * Parses a string into an integer number, which may
   * be in decimal, octal or hexadecimal format.
   */
  public static int parseInt(String s) throws NumberFormatException {
    return (int)parseLong(s);
  }

  /**
   * Parses a string into a long number, which may
   * be in decimal, octal or hexadecimal format.
   */
  public static long parseLong(String x) throws NumberFormatException {
    if (x.length() == 0) return 0;
    if (x.charAt(0) == '0') {
      if (x.length() == 1)
        return 0;
      char ch = x.charAt(1); // if exceptions, it's 0 anyway.
      if ((ch=='x') || (ch=='X'))
        return Long.parseLong(x.substring(2),16);  // Hex
      return Long.parseLong(x,8);  // Octal
    }
    return Long.parseLong(x);  // Decimal
  }

  public static long parseLongHex(String s) throws NumberFormatException {
    try {
      if (s.endsWith("H") || s.endsWith("h")) s = s.substring(0,s.length()-1);
      long ret = 0;
      s = s.trim().toUpperCase();
      for (int i=s.length()-1; i>=0; i--) {
        int x;
        char c = s.charAt(i);
        if ((c>='0') && (c<='9')) x = (c-'0');
        else if ((c>='A') && (c<='F')) x = (c-'A');
        else throw new NumberFormatException(s + " is not a valid hex number.");
        ret |= x << (4*i);
      }
      return ret;
    } catch(NullPointerException npe) {
        throw new NumberFormatException("Can not parse null as a hex number.");
    }
  }

  public static boolean startsWith(byte[] buf, String pattern) {
    int l = pattern.length();
    if (buf.length < l)
      return false;
    for (int i=0; i<l; i++)
      if (buf[i] != (byte)pattern.charAt(i))
        return false;
    return true;
  }

  public static String toStringNeverNull(Object o) {
    try { return StringUtils.defaultString(o.toString()); } catch(Exception e) {}
    return "";
  }

  public static String toStringNeverNull(Object o, String def) {
    try { return StringUtils.defaultString(o.toString()); } catch(Exception e) {}
    return def;
  }

  /**
   * Handles the cases of null parameter(s)
   */
  public static boolean equals(java.util.Date d1, java.util.Date d2) {
    try { return (d1.getTime() == d2.getTime()); } catch(Exception e) {}
    return false;
  }

  public static java.sql.Date neverNull(java.sql.Date d) {
    return new java.sql.Date( (d==null) ? System.currentTimeMillis() : d.getTime() );
  }

  public static String leadToUpperCase(String s) {
    if (s==null) return null;
    int l = s.length();
    if (l==0) return "";
    char ch = Character.toUpperCase(s.charAt(0));
    if (l==1) s = String.valueOf(ch);
    else s = String.valueOf(ch) + s.substring(1);
    return s;
  }

  private static final char[] hexTable =
    {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};

  public static String hex(int val, int nibbles) {
    if ((nibbles<=0) || (nibbles>8)) nibbles = 8;
    StringBuffer sb = new StringBuffer(nibbles);
    int i;
    for (i=0; i<nibbles; i++) sb.append('0');
    for (i=0; i<nibbles; i++) {
      int x = val & 0x0F;
      sb.setCharAt(nibbles-i-1,hexTable[x]);
      val >>= 4;
    }
    return sb.toString();
  }

  public static String replace(boolean caseSens, String s, String _old, String _new) {
    int len = _old.length();
    if (len == 0 || (caseSens && s.indexOf(_old) < 0)) return s;

    StringBuffer sb = new StringBuffer(s.length());
    if (len == 1) {
      char ch = caseSens ? _old.charAt(0) : Character.toLowerCase(_old.charAt(0));
      for (int j=0; j<s.length(); ++j) {
        boolean unmatch = caseSens ? (s.charAt(j) != ch)
                                   : (Character.toLowerCase(s.charAt(j)) != ch);
        if (unmatch)
          sb.append(s.charAt(j));
        else if (_new != null)
          sb.append(_new);
      }
    } else {
      int startIdx = 0;
      String ss = s;
      if (!caseSens) {
        ss = ss.toLowerCase();
        _old = _old.toLowerCase();
      }
      while (startIdx < ss.length()) {
        int idx = ss.indexOf(_old,startIdx);
        if (idx < 0) {
          sb.append(s.substring(startIdx));
          break;
        } else {
          sb.append(s.substring(startIdx,idx));
          if (_new != null) sb.append(_new);
          startIdx = idx + len;
        }
      }
    }
    return sb.toString();
  }

  public static String unquote(String s) { return unquote(s, null, null); }

  public static String unquote(String s, String quot) { return unquote(s, quot, null); }

  public static String unquote(String s, String left, String right) {
    if (left == null) {
      if (s.startsWith("'")  && s.endsWith("'") || s.startsWith("\"") && s.endsWith("\""))
        return s.substring(1, s.length()-1);
    } else if (right != null) {
      if (s.startsWith(left) && s.endsWith(right))
        return s.substring(left.length(), s.length()-right.length());
    } else if (left.length() > 1) {
      if (s.startsWith(left) && s.endsWith(left))
        return s.substring(left.length(), s.length()-left.length());
    } else {
      String r = left;
      switch(left.charAt(0)) {
      case '[': r = "]"; break;
      case '{': r = "}"; break;
      case '(': r = ")"; break;
      case '<': r = ">"; break;
      }
      if (s.startsWith(left) && s.endsWith(r))
        return s.substring(1, s.length()-1);
    }
    return s;
  }

  public static String[] toStringArray(java.util.List list) {
    int len = (list==null) ? 0 : list.size();
    String[] sa = new String[len];
    for (int i=0; i<len; ++i) {
      try { sa[i] = list.get(i).toString(); } catch(Exception e) {}
    }
    return sa;
  }

  public static boolean existsClass(String name) {
    try {
      Class.forName(name);
      return true;
    } catch(Exception e) { return false; }
  }

} // end of class SimpleLib.
