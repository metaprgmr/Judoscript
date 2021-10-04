/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 03-17-2002  JH   Initial open source release.
 * 06-02-2002  JH   Added ensurePath().
 * 08-26-2002  JH   Added isFileNewer().
 * 12-04-2002  JH   Added ${:}, ${/}, ${~} and ${.} support in getEnvVar(name).
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript.util;

import java.lang.reflect.*;
import java.util.*;
import java.util.zip.*;
import java.text.*;
import java.io.*;
import java.net.InetAddress;
import java.sql.*;
import org.apache.commons.lang.StringUtils;

/********************************************************************

  This class is a giant collection of one-stop helper functions
  that can be used directly from applications. Using these
  functions eliminates the need of creating helper objects,
  setting options and getting the results.  These functions are
  all <code>static final</code>, and mostly implemented with
  <code>try-catch</code>, so performance overhead is minimal.

********************************************************************/

public class Lib extends SimpleLib
{

////////////////////////////////////////////////////////
//
// Public Data Members
//
////////////////////////////////////////////////////////

  public static Enumeration EMPTY_ENUM = new Enumeration()
    {
      public final boolean hasMoreElements() { return false; }
      public final Object nextElement() { return null; }
    };

  public static boolean debug = false;

  /**
   * Some functions, such as formatting and date manipulation, 
   * typically share some objects. If <em>thread_safe</em> is
   * <code>true</code>, these functions are synchronized.
   *<p>
   * In a single-threaded program, you can turn this off to gain
   * some performance boost.
   */
  public static boolean thread_safe = true;
  public static String indentText = " ";

  public static final String[] emptyStringArray = new String[0]; 

////////////////////////////////////////////////////////
//
// Debugging
//
////////////////////////////////////////////////////////

  /**
   * <u>Category</u>: Debugging
   */
  public static String getStackTrace(Throwable t)
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    t.printStackTrace(pw);
    pw.flush();
    return sw.toString();
  }

  public static void showCallStack() {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    new Exception().printStackTrace(pw);
    pw.flush();
    try {
      BufferedReader br = new BufferedReader(new StringReader(sw.toString()));
      for (int i=0; ; ++i) {
        String line = br.readLine();
        if (line == null) break;
        if (i>1) System.out.println(line);
      }
    } catch(Exception e) {}
  }

  /**
   * <u>Category</u>: Debugging
   *<p>
   * This method serves the same purposes as the C built-in macros
   * <code>__FILE__</code> and <code>__LINE__</code>.
   * It returns the current method location from which it is called.
   *<p>
   * <em>toExclude</em> is optionally a method name to be ignored.
   *<p>
   * A typical use is to create your own debug method like this:
   *<p>
   *<pre>public class MyClass
   *{
   *  static final boolean debugging = true;
   *
   *  static final void dbg(String msg) {
   *    if (debugging)
   *      System.out.println("### " + __METHOD__("dbg",true,null,null) + msg);
   *  }
   *
   *  public void foo(String bar) {
   *    dbg(bar);
   *    .....
   *  }
   *}
   *</pre>
   *
   * When <code>foo("abcde")</code> is called, it will print out a message on
   * <code>System.out</code>:<pre>
   *  ### (MyClass.java:34) foo(): abcde
   *</pre>
   *@param toExclude if not null, the latest lines containing this string is ignored.
   *@param reference if not null, the distance from the found line and a previous line that
   *                 contains this pattern is used as a call depth and the resultant text
   *                 is accordingly indented.
   *@return <code><em>current_method</em>(): <em>msg</em></code> if <em>includeFileLine</em>
   *        is false, or <code> (<em>file_name:line_num</em>) <em>current_method</em>():
   *        <em>msg</em></code><br> if <em>includeFileLine</em> is true.
   *        If <em>pattern</em> is not null, it is indented.<br>
   *        Note that if the class is compiled without debug information, or JIT is on
   *        at run-time, the line number may not be available.
   */
  public static String __METHOD__(String toExclude, boolean includeFileLine, String msg, String reference)
  {
    return __METHOD__(toExclude, includeFileLine, msg, reference, null);
  }

  public static String __METHOD__(String toExclude, boolean includeFileLine,
                                  String msg, String reference, Exception excpt)
  {
    try {
      if (excpt == null) excpt = new Exception();
      BufferedReader br = new BufferedReader(new StringReader(getStackTrace(excpt)));
      boolean countDepth = false;
      String indent = "";
      String methodName = "";
      while (true) {
        String line = br.readLine();
        if (line == null) break;
        if (!line.startsWith("\tat")) continue;
        if (line.indexOf("__METHOD__") >= 0) continue;
        if (!countDepth) {
          if (StringUtils.isNotBlank(toExclude) && (line.indexOf(toExclude)>=0))
            continue;
          int idx = line.indexOf('(');
          if (idx < 0) continue;
          int idx1 = line.lastIndexOf('.',idx);
          if (idx1 < 0) continue;
          methodName = line.substring(idx1+1,idx);
          if (includeFileLine) {
            StringTokenizer st = new StringTokenizer(line.substring(idx+1),":)");
            String fileName = st.nextToken();
            try {
              int lineNo = Integer.parseInt(st.nextToken());
              methodName = "<" + fileName + ":" + lineNo + "> " + methodName;
            } catch(Exception e) { // no line number.
              methodName = "<" + fileName + "> " + methodName;
            }
          }
          if (StringUtils.isBlank(msg))
            methodName += "()";
          else
            methodName += "(): " + msg;
          if (StringUtils.isBlank(reference))
            return methodName;
          countDepth = true;
          if (line.indexOf(reference) >= 0)
            break;
        } else { // counting call depth
          indent += indentText;
          if (line.indexOf(reference) >= 0)
            break;
        }
      }
      return indent + methodName;
    }
    catch(Exception ex) {}
    return null;
  }

  /**
   * <u>Category</u>: Debugging
   *<p>
   * A quick debugging method that prints out the current file and
   * method name on to <code>System.out</code>.
   *
   *@see __METHOD__(java.lang.String,boolean,java.lang.String,java.lang.String)
   */
  public static void here()
  {
    System.out.println(__METHOD__("here",true,null,null));
    System.out.flush();
  }

  public static void here(int i) { here(String.valueOf(i)); }
  public static void here(long i) { here(String.valueOf(i)); }
  public static void here(double d) { here(String.valueOf(d)); }

  public static void here(Object o) {
    String s = null;
    if (o != null) s = o.toString();
    here(s);  
  }

  /**
   * <u>Category</u>: Debugging
   *<p>
   * A quick debugging method that prints out the current file and
   * method name on to <code>System.out</code>.
   *
   *@see __METHOD__(java.lang.String,boolean,java.lang.String)
   */
  public static void here(String msg)
  {
    System.out.println(__METHOD__("here",true,msg,null));
    System.out.flush();
  }

  public static void hereWrt(String reference) {
    System.out.println(__METHOD__("hereWrt",true,null,reference));
    System.out.flush();
  }

  public static void hereWrt(String msg, String reference) {
    System.out.println(__METHOD__("hereWrt",true,msg,reference));
    System.out.flush();
  }

/***
  /*
   * <u>Category</u>: Debugging
   *<p>
   * If <em>expr</em> is <code>false</code>, throws {@link com.judoscript.util.AssertionException}
   * with the current method/file/line information.
   *
   *@see __METHOD__(java.lang.String,boolean,java.lang.String)
   *
  public static void assert(boolean expr) throws AssertionException {
    assert(expr,null);
  }

  /*
   * <u>Category</u>: Debugging
   *<p>
   * If <em>expr</em> is <code>false</code>, throws {@link com.judoscript.util.AssertionException}
   * with the current method/file/line information and <em>msg</em>.
   *
   *@see __METHOD__(java.lang.String,boolean,java.lang.String)
   *
  public static void assert(boolean expr, String msg) throws AssertionException
  {
    if (expr) return;
    if (msg == null) msg = "Assertion failure.";
    else msg = "Assertion failure: " + msg;
    throw new AssertionException(__METHOD__("assert",true,msg,null));
  }
***/

////////////////////////////////////////////////////////
//
// General
//
////////////////////////////////////////////////////////

  /**
   * <u>Category</u>: General
   *<p>
   *@return true if these two objects are both not null and
   *        <code>equals()</code> returns <code>true</code>
   */
  public static boolean Equals(Object obj1, Object obj2)
  {
    return (obj1 != null) && (obj2 != null) && obj1.equals(obj2);
  }

  /**
   * <u>Category</u>: General
   *<p>
   *@return true if these two objects are either both null or
   *        <code>equals()</code> returns <code>true</code>
   */
  public static boolean equalsNullable(Object obj1, Object obj2)
  {
    if ((obj1 == null) && (obj2 == null)) return true;
    try { return obj1.equals(obj2); } catch(Exception e) { return false; }
  }

  public static int compareToNullable(Comparable obj1, Comparable obj2)
  {
    if (obj1 == null) return (obj2 == null) ? 0 : -1;
    if (obj2 == null) return 1;
      
    try { return obj1.compareTo(obj2); } catch(Exception e) { return -1; }
  }

  /**
   * <u>Category</u>: General
   *<p>
   * Have the current thread to sleep for <em>millis</em>econds.
   *@return <code>true</code> if the period is completed;
   *        <code>false</code> if interrupted.
   */
  public static boolean sleep(long millis)
  {
    try { Thread.sleep(millis); } catch (InterruptedException e) { return false; }
    return true;
  }

  /**
   * <u>Category</u>: General
   *<p>
   * Calls <em>o</em>'s wait() and rest until the object is awaken or interrupted.
   */
  public static void rest(Object o)
  {
    synchronized(o) { try { o.wait(); } catch(InterruptedException ie) {} }
  }

  /**
   * <u>Category</u>: General
   *<p>
   * Calls o's wait() and rest until the object is awaken or interrupted.
   *@return <code>true</code> if the period is completed;
   *        <code>false</code> if interrupted.
   */
  public static boolean sleep(Object o, long millis)
  {
    synchronized(o) {
      try { o.wait(millis); } catch(InterruptedException ie) { return false; }
    }
    return true;
  }

  /**
   * <u>Category</u>: General
   *<p>
   * Waits <em>dur</em> milliseconds on object <em>o</em> since the <em>starttime</em>.
   *<p>
   * If <em>interruptable</em> is false, when interrupted, it goes back
   * to sleep until the duration of rest has elapsed. Otherwise, it
   * returns immediately.
   */
  public static void wait(long starttime, long dur, boolean interruptable)
  {
    while (true) {
      long lapsed = System.currentTimeMillis() - starttime;
      if (lapsed >= dur) return;
      try { Thread.sleep(dur - lapsed); }
      catch(InterruptedException ie) { if (interruptable) break; }
    }
  }

  /**
   * <u>Category</u>: General
   *<p>
   *@return the size of the List, or 0 if null
   */
  public static int getSize(List list)
  {
    try { return list.size(); } catch(Exception e) { return 0; }
  }

  public static void ensureSize(List list, int size) {
    if (list.size() < size) {
      if (list instanceof Vector) {
        ((Vector)list).setSize(size);
      } else {
        int cnt = size - list.size();
        while (cnt-- > 0)
          list.add(null);
      }
    }
  }

  /**
   * <u>Category</u>: General
   *<p>
   *@return the size of the Map, or 0 if null
   */
  public static int getSize(Map ht)
  {
    try { return ht.size(); } catch(Exception e) {}
    return 0;
  }

  /**
   * <u>Category</u>: General
   */
  public static boolean existsProperty(String name)
  {
    return !StringUtils.isBlank(System.getProperty(name));
  }

  /**
   * <u>Category</u>: General
   */
  public static String getProperty(String name)
  {
    return System.getProperty(name);
  }

  /**
   * <u>Category</u>: General
   */
  public static String getProperty(String name, String defVal)
  {
    return System.getProperty(name,defVal);
  }

  /**
   * <u>Category</u>: General
   */
  public static int getIntProperty(String name) { return getIntProperty(name,0); }

  /**
   * <u>Category</u>: General
   */
  public static int getIntProperty(String name, int defVal)
  {
    try { return Integer.parseInt(System.getProperty(name)); }
    catch(Exception e) { return defVal; }
  }

  /**
   * <u>Category</u>: General
   */
  public static boolean getBoolProperty(String name)
  {
    return getBoolProperty(name,false);
  }

  /**
   * <u>Category</u>: General
   */
  public static boolean getBoolProperty(String name, boolean defVal)
  {
    try { return System.getProperty(name).equalsIgnoreCase("true"); }
    catch(Exception e) { return defVal; }
  }

  /**
   * <u>Category</u>: General
   *<p>
   * Useful for EJB clients to check if an exception is a server-side one or not.
   */
  public static boolean isServerSideException(String stackTrace)
  {
    return (stackTrace.indexOf("server side") > 0);
  }

  /**
   * <u>Category</u>: General
   *<p>
   * Useful for EJB clients to check if an exception is a server-side one or not.
   */
  public static boolean isServerSideException(Throwable t)
  {
    String s = getStackTrace(t);
    return (s.indexOf("server side") > 0);
  }

  /**
   * A convenience function that, when the exception has no message, just
   * append a "." to the user message; otherwise, append ":" and the exception.
   */
  public static String getExceptionMsg(String msg, Throwable e) {
    if (e == null)
      return StringUtils.defaultString(msg);
    String emsg = e.getMessage();
    if (StringUtils.isBlank(emsg)) {
      emsg = e.getClass().getName();
      int idx = emsg.lastIndexOf(".");
      if (idx > 0) emsg = emsg.substring(idx+1);
      emsg = "<" + emsg + ">";
    }
    if (StringUtils.isBlank(msg)) return emsg;
    return msg + ": " + emsg;
  }

  public static void throwFileNotFoundException(String filename) throws FileNotFoundException {
    throw new FileNotFoundException("File '" + filename + "' not found.");
  }

  /**
   * <u>Category</u>: General
   */
  public static boolean isMac() { return StringUtils.contains(System.getProperty("os.name"), "Mac "); }

  public static final boolean is_windows = isWindows();

  /**
   * <u>Category</u>: General
   */
  public static boolean isWindows() { return StringUtils.contains(System.getProperty("os.name"), "Windows "); }

  /**
   * <u>Category</u>: General
   */
  public static boolean isUnix() {
    if (isOS400()) return false;
    return File.pathSeparatorChar==':' && File.separatorChar=='/';
  }

  /**
   * <u>Category</u>: General
   */
  public static boolean isOS400() { return StringUtils.contains(System.getProperty("os.name"), "/400"); }

  /**
   * <u>Category</u>: General
   */
  public static boolean isHPUX() { return StringUtils.contains(System.getProperty("os.name"), "HP-UX"); }

  /**
   * <u>Category</u>: General
   */
  public static boolean isLinux() { return StringUtils.contains(System.getProperty("os.name"), "Linux"); }

  /**
   * <u>Category</u>: General
   */
  public static boolean isSunOS() { return StringUtils.contains(System.getProperty("os.name"), "SunOS"); }

  /**
   * <u>Category</u>: General
   */
  public static String osName() { return System.getProperty("os.name"); }

  /**
   * <u>Category</u>: General
   */
  public static String javaVendor() { return System.getProperty("java.vendor"); }

  /**
   * <u>Category</u>: General
   */
  public static boolean isNetscape() { return StringUtils.contains(System.getProperty("java.vendor"), "Netscape"); }

  /**
   * <u>Category</u>: General
   */
  public static boolean isSunVendor() { return StringUtils.contains(System.getProperty("java.vendor"), "Sun "); }


  /**
   * Behaves in the same way as <code>java.util.StringTokenizer</code>
   * except it returns a <code>String[]</code>.
   */
  public static String[] tokenize(String s, String delims) {
    try {
      StringTokenizer st = new StringTokenizer(s,delims);
      String[] ret = new String[st.countTokens()];
      for (int i=0; i<ret.length; i++)
        ret[i] = st.nextToken();
      return ret;
    } catch(Exception e) { return null; }
  }

  /**
   *@return the number of apperances of c in s.
   */
  public static int count(String s, char c) {
    int cnt = 0;
    for (int i=s.length()-1; i>=0; i--)
      if (c == s.charAt(i)) ++cnt;
    return cnt;
  }

  public static int count(String s, String pat) {
    if (StringUtils.isBlank(pat))
      return 0;

    if (pat.length() == 1)
      return count(s, pat.charAt(0));

    int cnt = 0;
    int idx = 0;
    while (true) {
      idx = s.indexOf(pat,idx);
      if (idx < 0)
        break;
      ++cnt;
      idx += pat.length();
    }
    return cnt;
  }

  /**
   * The input string <em>s</em> is parsed into a string array; it behaves
   * differently from <code>java.util.StringTokenizer</code> in that, consecutive
   * delimiters in the input string are not considered as a single delimiter but
   * rather return empty strings ("") for them.
   *
   *@param delim  the delimiter character
   *@param trim   if true, trim the result strings
   *@param minLen the returned String[] has at least this many cells
   *@param defaultString in case the number of available strings are less then <em>minLen</em>,
   *              the empty cells will be filled with this value
   */
  public static String[] string2Array(String s, int delim, boolean trim, int minLen, String defaultString)
  {
    String[] sa = string2Array(s,delim,trim);
    if (minLen <= 0) return sa;
    int len = 0;
    if (sa != null) len = sa.length;
    if (len >= minLen) return sa;
    String[] sb = new String[minLen];
    int i;
    for (i=0; i<minLen; i++)
    {
      if (i < len)
        sb[i] = sa[i];
      else
        sb[i] = defaultString;
    }
    return sb;
  }

  public static String array2String(Object[] oa) {
    StringBuffer sb = new StringBuffer("[");
    int len = (oa==null) ? 0 : oa.length;
    for (int i=0; i<len; i++) {
      if (i>0) sb.append(", ");
      sb.append(oa[i]);
    }
    sb.append(']');
    return sb.toString();
  }

  /**
   * The input string <em>s</em> is parsed into a string array; it behaves
   * differently from <code>java.util.StringTokenizer</code> in that, consecutive
   * delimiters in the input string are not considered as a single delimiter but
   * rather return empty strings ("") for them.
   *
   *@param delim  the delimiter character
   *@param trim   if true, trim the result strings
   */
  public static String[] string2Array(String s, int delim, boolean trim)
  {
    if (s == null) return null;

    ArrayList v = new ArrayList();
    int idx1 = 0;
    int idx2 = 0;
    while (true) {
      idx2 = s.indexOf(delim, idx1);
      if (idx2 < 0) {
        try { v.add(s.substring(idx1)); }
        catch(Exception e) { /* if out o'bounds, just ignore. */ }
        break;
      }
      v.add(s.substring(idx1,idx2));
      idx1 = idx2 + 1;
    }

    String sa[] = new String[v.size()];
    for (int i=0; i<sa.length; i++) {
      if (trim) sa[i] = v.get(i).toString().trim();
      else sa[i] = v.get(i).toString();
    }

    return sa;
  }

  public static boolean isAbsolutePath(String path) {
    if (path.startsWith("/")) return true;
    if (path.startsWith("\\")) return true;
    if (is_windows && (path.length()>2) && (path.charAt(1)==':'))
      return true;
    return false;
  }

  public static String cleanupPath(String path) {
    path = path.replace('\\', '/');
    if (path.indexOf("//") >= 0)
      return replace(true, path, "//", "/");
    return path;
  }

  public static boolean pathStartsWith(String path, String base) {
    try {
      if (is_windows && (path.length()>2) && (path.charAt(1)==':'))
        return path.toUpperCase().startsWith(base.toUpperCase());
      return path.startsWith(base);
    } catch(Exception e) {
      return false;
    }
  }

  /**
   * formats the <em>seconds</em> into days, hours, minutes and seconds.
   */
  public static String formatDuration(int seconds) {
    int hrs  = (seconds % (3600*24)) / 3600;
    int days = seconds / (3600*24);

    String s_days;
    String s_hrs;
    String s_mins = String.valueOf( (seconds % 3600) / 60 );
    String s_secs = String.valueOf( seconds % 60 );
    if (s_secs.length()==1) s_secs = "0" + s_secs;
    if (hrs == 0) {
      s_hrs = s_mins + ":" + s_secs;
    } else {
      if (s_mins.length()==1) s_mins = "0" + s_mins;
      s_hrs = String.valueOf(hrs) + ":" + s_mins + ":" + s_secs;
    }
    if (days == 0) return s_hrs;
    return String.valueOf(days) + " days " + s_hrs;
  }

  //////////////////////////////////////////////////////////
  //
  // Text Formatting and Encoding
  //
  //////////////////////////////////////////////////////////

  public static String getFiller(int len, char filler) {
    if (len <= 0) return "";
    StringBuffer sb = new StringBuffer(len);
    for (int i=len; i>0; i--) sb.append(filler);
    return sb.toString();
  }

  public static void appendChars(StringBuffer sb, char filler, int len) {
    for (int i=0; i<len; ++i)
      sb.append(filler);
  }

  /**
   *@return a string that is center aligned to <em>width</em>, if the
   *        string length does not exceed <em>width</em>. When this
   *        happens, it will be led and trailed by <em>filler</em>.
   *        If <em>strict</em> is <code>true</code> and <em>val</em>
   *        is longer than <em>width</em>, the string will be chopped
   *        from both ends to fit.
   */
  public static String centerAlign(String val, int width, boolean strict, char filler, String mark)
  {
    int L = val.length();
    if (L >= width) {
      if (!strict || (L==width)) return val;
      int markLen = (mark==null) ? 0 : mark.length();
      if (markLen == 0) {
        L = (L-width) / 2;
        return val.substring(L,L+width);
      }
      if (width <= markLen * 2) return (mark+mark).substring(0,width);
      width -= markLen * 2;
      L = (L-width) / 2;
      return mark + val.substring(L,L+width) + mark;
    }
    int len = (width - val.length()) / 2;
    StringBuffer sb = new StringBuffer(width);
    appendChars(sb, filler, len);
    sb.append(val);
    appendChars(sb, filler, width-len-L);
    return sb.toString();
  }
  public static String centerAlign(String val, int width, boolean strict) {
    return centerAlign(val, width, strict, ' ', null);
  }
  public static String centerAlign(String val, int width) {
    return centerAlign(val, width, true, ' ', null);
  }
  public static String centerAlign(String val, int width, boolean strict, boolean isNum) {
    return centerAlign(val, width, strict, ' ', isNum ? "*" : null);
  }

  public static String leftAlign(String val, int width, boolean strict, char filler, String mark)
  {
    int L = val.length();
    if (L > width) {
      if (!strict) return val;
      int markLen = (mark==null) ? 0 : mark.length();
      if (markLen == 0) return val.substring(0,width);
      return val.substring(0,width-markLen) + mark;
    }
    return val + getFiller(width-L,filler);
  }
  public static String leftAlign(String val, int width, boolean strict) {
    return leftAlign(val, width, strict, ' ', null);
  }
  public static String leftAlign(String val, int width) {
    return leftAlign(val, width, true, ' ', null);
  }
  public static String leftAlign(String val, int width, boolean strict, boolean isNum) {
    return leftAlign(val, width, strict, ' ', isNum ? "*" : null);
  }

  public static String rightAlign(String val, int width, boolean strict, char filler, String mark)
  {
    int L = val.length();
    if (L > width) {
      if (!strict) return val;
      int markLen = (mark==null) ? 0 : mark.length();
      if (markLen == 0) return val.substring(L-width);
      return mark + val.substring(L-width+markLen);
    }
    return getFiller(width-L,filler) + val;
  }
  public static String rightAlign(String val, int width, boolean strict) {
    return rightAlign(val, width, strict, ' ', null);
  }
  public static String rightAlign(String val, int width) {
    return rightAlign(val, width, true, ' ', null);
  }
  public static String rightAlign(String val, int width, boolean strict, boolean isNum) {
    return rightAlign(val, width, strict, ' ', isNum ? "*" : null);
  }

  /**
   *@return a string ensured to be of at least length <em>len</em>. If
   *        the string is short than expected, spaces are filled to
   *        the tail (if <em>len</em> is positive) or head (if
   *        <em>len</em> is negative).
   */
  public static String ensureLength(String val, int len) { return ensureLength(val,len,' '); }

  /**
   *@return a string ensured to be of at least length <em>len</em>. If
   *        the string is short than expected, the <em>filler</em> are
   *        filled to the tail (if <em>len</em> is positive) or head
   *        (if <em>len</em> is negative).
   */
  public static String ensureLength(String val, int len, char filler) {
    boolean keepHead = (len>0);
    if (!keepHead) len = -len;
    int L = val.length();
    if (L >= len) return val;

    StringBuffer sb = new StringBuffer(len);
    if (!keepHead) appendChars(sb, filler, len-L);
    sb.append(val);
    if (keepHead) appendChars(sb, filler, len-L);
    return sb.toString();
  }

  /**
   *@return a string guaranteed to be of length <em>len</em>. If the
   *        string is longer, it will be chopped off from the tail (if
   *        <em>len</em> is positive) of from the head (if <em>len</em>
   *        is nagive). If the string is short than expected, spaces
   *        are filled to ensure the length.
   */
  public static String exactLength(String val, int len) { return exactLength(val,len,' '); }

  /**
   *@return a string guaranteed to be of length <em>len</em>. If the
   *        string is longer, it will be chopped off from the tail (if
   *        <em>len</em> is positive) of from the head (if <em>len</em>
   *        is nagive). If the string is short than expected, the
   *        <em>filler</em> are filled to ensure the length.
   */
  public static String exactLength(String val, int len, char filler) {
    boolean keepHead = (len>0);
    if (!keepHead) len = -len;
    int L = val.length();
    if (L == len) return val;

    if (L < len) {
      StringBuffer sb = new StringBuffer(len);
      if (!keepHead)
        appendChars(sb, filler, len-L);
      sb.append(val);
      if (keepHead)
        appendChars(sb, filler, len-L);
      return sb.toString();
    }
    return keepHead ? val.substring(0,len) : val.substring(L-len);
  }

  /**
   *@return     a string that is no longer than the specified length;
   *            if the string is longer than the specified length, the string
   *            will be chopped off from the tail (if <em>len</em> is positive)
   *            or from the head (if <em>len</em> is negative).
   *@param val  the value string
   *@param len  the intended length. If positive, limits the length from the head;
   *            if negative, from tail.
   */
  public static String limitLength(String val, int len) { return limitLength(val,len,null); }

  /**
   *@return     a string that is no longer than the specified length;
   *            if the string is longer than the specified length, the string
   *            will be chopped off from the tail (if <em>len</em> is positive)
   *            or from the head (if <em>len</em> is negative).
   *            if <em>mark</em> is not null, it will be appended or prepended
   *            to the result. The resultant length never exceeds that specified.
   *@param val  the value string
   *@param len  the intended length. If positive, limits the length from the head;
   *            if negative, from tail.
   *@param mark if not null, is appended (if <em>len</em> is positive) or prepended
   *            (if <em>len</em> is negative) to the tail or head of the cut-off
   *            string.
   */
  public static String limitLength(String val, int len, String mark)
  {
    if (val==null) return null;
    boolean keepHead = (len>0);
    if (!keepHead) len = -len;
    int L = val.length();
    if (L <= len) return val;

    if (mark != null) len -= mark.length();
    if (keepHead) {
      val = val.substring(0,len);
      if (mark != null) return val + mark;
    } else {
      val = val.substring(L-len);
      if (mark != null) return mark + val;
    }
    return val;
  }
  /**
   * The supported command line format:
   *<pre>  java java_class_name ( /name[=value] | -name[=value] | param )* </pre>
   * @return a hashtable for all the options;
   *         if value part is missing, defaulted to 'true'.
   *         special entry "-args" is an array of ( param )*, if any.
   */
  public static Map parseCmdline(String[] args) {
    int i;
    Map ht = new HashMap();
    ArrayList argv = new ArrayList();
    for (i=0; i<args.length; i++) {
      char ch = args[i].charAt(i);
      if ((ch != '-') && (ch != '/')) {
        argv.add(args[i]);
      } else {
        int idx = args[i].indexOf('=');
        if (idx > 0)
          ht.put(args[i].substring(0,idx), args[i].substring(idx+1));
        else
          ht.put(args[i], "true");
      }
    }
    int len = argv.size();
    if (len > 0) {
      String[] sa = new String[len];
      for (i=0; i<len; i++)
        sa[i] = (String)argv.get(i);
      ht.put("-args", sa);
    }
    return ht;
  }

  /**
   * For XML tag names with or without namespace.
   */
  public static String formTag(String uri, String local) {
    return StringUtils.isBlank(uri) ? local : (uri + ":" + local);
  }

  /**
   * Get all keys as String's sorted ascending
   */
  public static String[] getKeys(Map ht) { return getKeys(ht,null); }

  public static String[] getKeys(Map ht, Comparator cptr) {
    int sz = (ht==null) ? 0 : ht.size();
    String[] ret = new String[sz];
    if (sz == 0) return ret;

    Iterator itor = ht.keySet().iterator();
    for (int i=0; i<sz; i++)
      ret[i] = itor.next().toString();
    if (cptr == null) Arrays.sort(ret);
    else Arrays.sort(ret,cptr);
    return ret;
  }

  public static String sectionNumberToName(String sect)
  {
    String[] sa = string2Array(sect, (int)'.', true);
    StringBuffer sb = new StringBuffer(sa.length * 2);
    for (int i=0; i<sa.length; i++) {
      sb.append('_');
      sb.append(sa[i]);
    }
    return sb.toString();
  }

  public static Comparator getSectionNumberComparator()
  {
    return new Comparator() {
      public int compare(Object o1, Object o2) {
        try {
          StringTokenizer st1 = new StringTokenizer(o1.toString(),".");
          StringTokenizer st2 = new StringTokenizer(o2.toString(),".");
          while (st1.hasMoreTokens()) {
            if (!st2.hasMoreTokens()) return 1;
            int i1 = 0, i2 = 0;
            String s1 = st1.nextToken();
            String s2 = st2.nextToken();
            boolean numbers = true;
            try {
              i1 = Integer.parseInt(s1);
              i2 = Integer.parseInt(s2);
            } catch(NumberFormatException nfe1) { numbers = false; }
            if (numbers) {
              if (i1 > i2) return 1;
              if (i1 < i2) return -1;
            } else {
              i1 = s1.compareTo(s2);
              if (i1 != 0) return i1;
            }
          }
          return 0;
        } catch(Exception e) { return 1; }
      }
    };
  }

  public static long copyStream(InputStream is, OutputStream os) throws IOException {
    return copyStream(is, os, true);
  }

  public static long copyStream(InputStream is, OutputStream os, boolean close) throws IOException {
    long cnt = 0;
    int len = 0;
    byte[] buf = new byte[2048];
    try {
      while (true) {
        len = is.read(buf);
        if (len < 0)
          break;
        if (os!=null)
          os.write(buf, 0, len);
        cnt += len;
      }
      if (os!=null)
        os.flush();
    } finally {
      if (close) {
        try { is.close(); } catch(Exception e2) {}
        if (os!=null)
          try { os.close(); } catch(Exception e1) {}
      }
    }
    return cnt;
  }

  public static boolean copyFile(File src, File dest, boolean doCheck, StreamCopier sc)
                        throws IOException
  {
    ensureDirectory(dest);
    if (doCheck && dest.exists()) {
      if ((dest.length()==src.length()) && (dest.lastModified()==src.lastModified()))
        return false;
    }
    InputStream is;
    if (src instanceof ZipFS.ZippedFile)
      is = ((ZipFS.ZippedFile)src).getInputStream();
    else if (src instanceof UrlFS.UrlFile)
      is = ((UrlFS.UrlFile)src).getInputStream();
    else
      is = new FileInputStream(src);
    if (sc == null)
      copyStream(is, new FileOutputStream(dest));
    else
      sc.copyStream(new StreamCopier.Src(is, src), new StreamCopier.FileDest(dest), true);
    long t = src.lastModified();
    if (t > 0)
      dest.setLastModified(t);
    return true;
  }

  public static void moveFile(File src, File dest) throws IOException {
    ensureDirectory(dest);
    if (src.equals(dest)) return;
    if (!src.renameTo(dest))
      throw new IOException("Failed to move '" + src.getAbsolutePath() +
                            "' to '" + dest.getAbsolutePath() + "'.");
  } 
  public static String getFileName(String path) {
    int idx = path.lastIndexOf("/");
    if (idx < 0)
      idx = path.lastIndexOf("\\");
    if (idx > 0) return path.substring(idx+1);
    return path;
  }

  public static String getParentPath(String path) {
    try {
      if (path.length() < 2) return "";
      int idx = path.lastIndexOf("/", path.length()-2);
      if (idx < 0)
        idx = path.lastIndexOf("\\");
      if (idx > 0) return path.substring(0, idx+1).replace('\\', '/');
    } catch(Exception e) {}
    return "";
  }

  public static String getFileExt(String path) {
    int idx = path.lastIndexOf(".");
    if (idx < 0) return "";
    int idx1 = path.lastIndexOf("/");
    if (idx1 < 0)
      idx1 = path.lastIndexOf("\\");
    if ((idx1 < 0) || (idx > idx1))
      return path.substring(idx+1);
    return "";
  }

  public static String getHomeDir() { return System.getProperty("user.home"); }
  public static String getCurrentDir() { return System.getProperty("user.dir"); }
  public static String getUserName() { return System.getProperty("user.name"); }

  public static String groupNumber(long val) {
    NumberFormat nf = NumberFormat.getInstance();
    nf.setGroupingUsed(true);
    return nf.format(val);
  }

  public static String groupNumber(long val, int size, char separator) {
    DecimalFormat df = new DecimalFormat();
    DecimalFormatSymbols dfs = new DecimalFormatSymbols();
    df.setGroupingUsed(true);
    df.setGroupingSize(size);
    dfs.setGroupingSeparator(separator);
    df.setDecimalFormatSymbols(dfs);
    return df.format(val);
  }

  public static String decimalDigitsAsString(double val, int digits) {
    NumberFormat nf = NumberFormat.getInstance();
    nf.setMaximumFractionDigits(digits);
    nf.setGroupingUsed(false);
    return nf.format(val); 
  }

  public static double decimalDigits(double val, int digits) {
    try {
      return Double.parseDouble(decimalDigitsAsString(val, digits)); 
    } catch(NumberFormatException e) {} // won't happen.
    return 0;
  }

  public static long sum(int[] ia) {
    if (ia == null) return 0;
    long cnt = 0;
    for (int i=ia.length-1; i>=0; --i)
      cnt += ia[i];
    return cnt;
  }

  public static String getClassName(Class e) {
    String s = e.getName();
    int idx = 0;
    int arrays = 0;
    for (; idx<s.length(); ++idx) {
      if (s.charAt(idx) != '[')
        break;
      ++arrays;
    }

    if (arrays > 0) {
      s = s.substring(idx+1, s.length()-1);
      if (s.length() == 1) {
        switch(s.charAt(0)) {
        case 'B': s = "byte";    break;
        case 'C': s = "char";    break;
        case 'D': s = "double";  break;
        case 'F': s = "float";   break;
        case 'I': s = "int";     break;
        case 'J': s = "long";    break;
        case 'S': s = "short";   break;
        case 'Z': s = "boolean"; break;
        }
      }
      for (; idx>=0; --idx)
        s += "[]";
    }
    return s;
  }

  public static int indexOfWhitespace(String s) { return indexOfWhitespace(s, 0); }

  public static int indexOfWhitespace(String s, int start) {
    if (start < 0) start = 0;
    for (int i=start; i<s.length(); ++i) {
      if (Character.isWhitespace(s.charAt(i)))
        return i;
    }
    return -1;
  }

  public static int lastIndexOfWhitespace(String s) { return lastIndexOfWhitespace(s, -1); }

  public static int lastIndexOfWhitespace(String s, int start) {
    if (start < 0) start = s.length() - 1;
    for (int i=start; i>=0; --i) {
      if (Character.isWhitespace(s.charAt(i)))
        return i;
    }
    return -1;
  }

  public static String removeDotsInPath(String path) {
    path = path.replace('\\', '/');
    if ((path.indexOf("/../")<0) && (path.indexOf("/./")<0))
      return path;

    StringBuffer sb = new StringBuffer();
    int idx = path.indexOf("://");
    if (idx > 0) {
      sb.append(path.substring(0, idx+2));
      path = path.substring(idx+2);
    }
    StringTokenizer st = new StringTokenizer(path, "/");
    if (path.startsWith("/"))
      sb.append("/");
    else if (st.hasMoreTokens()) {
      sb.append(st.nextToken());
      sb.append("/");
    }
    String last=null;
    while (st.hasMoreTokens()) {
      path = st.nextToken();
      if (path.equals("."))
        ;
      else if (path.equals(".."))
        last = null;
      else {
        if (last != null) {
          sb.append(last);
          sb.append("/");
        }
        last = path;
      }
    }
    if (last!=null)
      sb.append(last);

    return sb.toString();
  }

  public static void ensureDirectory(File path) throws IOException {
    File p = path.getParentFile();
    if (!p.exists() && !p.mkdirs())
      throw new IOException("Failed to create directory '"+path+"'.");
  }

  private static long lastID = -1;

  public static synchronized long createID() {
    long id = System.currentTimeMillis() * 1000; // this makes it down to 1 mu-sec.
    if (id <= lastID) id = ++lastID;
    else lastID = id;
    return id;
  }

  private static String _codes = "qponmlkjicbaABCDEFGKL8MNPQRSTUVzyxwvutsrWXYZ1234hgfed56O790HIJ";
  private static int _radix = _codes.length();

  // base _codes.length() encoding
  public static String encodeNumber(long i) {
    if (i==0) return _codes.substring(0, 1);
    StringBuffer sb = new StringBuffer(50);
    sb.setLength(50);
    int idx = 49;
    while (i > 0) {
      int y = (int)(i % _radix);
      i /= _radix;
      sb.setCharAt(idx--, _codes.charAt(y));
    }
    return sb.substring(idx+1);
  }

  public static long decodeNumber(String s) throws NumberFormatException {
    long x = 0;
    for (int i=0; i<s.length(); ++i) {
      int idx = _codes.indexOf(s.charAt(i));
      if (idx < 0)
        throw new NumberFormatException("Illegal letter '" + s.charAt(i) + "' encountered.");
      x = x * _radix + idx;
    }
    return x;
  }

  public static String toAbsoluteUrl(String url, String base) {
    if (StringUtils.isNotBlank(base)) {
      int idx = base.indexOf('?');
      if (idx >= 0)
        base = base.substring(0, idx);

      url = removeDotsInPath(url);
      if (url.startsWith("/")) {
        HashMap ht1 = parseUrl(base, null);
        String x = (String)ht1.get("root");
        if (x != null)
          url = x + url;
      } else {
        if (url.startsWith("."))
          url = "/" + url;
        if (base.endsWith("/")) {
          base = base.substring(0, base.length()-1);
        } else {
          idx = base.lastIndexOf('/');
          if (base.indexOf("://") < idx-2)
            base = base.substring(0, idx-1);
        }
        url = base + url;
      }
    }

    return removeDotsInPath(url);
  }

  /**
   * @return a Map of these keys:
   * <ul>
   * <li><code>url</code>: the URL; if <code>base</code> is present, it's the resolved URL.
   * <li><code>protocol</code>
   * <li><code>domain</code>
   * <li><code>root</code>
   * <li><code>host</code>
   * <li><code>path</code>: the firtual path of the URL.
   * <li><code>ref</code>
   * <li><code>query_string</code>
   * <li><code>file_name</code>
   * </ul>
   */
  public static HashMap parseUrl(String url, String base) {
    if (StringUtils.isNotBlank(base))
      toAbsoluteUrl(url, base);

    String x;
    int idx;
    HashMap ht = new HashMap();
    ht.put("url", url);
    String protocol = null;
    idx = url.indexOf("://");
    boolean hasHost = (idx > 0);
    if (hasHost) {
      protocol = url.substring(0, idx).toLowerCase();
      ht.put("protocol", protocol);
      url = url.substring(idx+3);
      idx = url.indexOf("/");
      x = url;
      if (idx > 0)
        x = url.substring(0, idx).toLowerCase();
      ht.put("domain", x);
      ht.put("root", protocol + "://" + x);
      int idx1 = x.indexOf(":");
      if (idx1 < 0) {
        ht.put("host", x);
      } else {
        ht.put("host", x.substring(0,idx1).toLowerCase());
        try { ht.put("port", x.substring(idx1+1)); } catch(Exception e) {}
      }
      if ((idx < 0) || (idx >= url.length())) {
        ht.put("path", "/");
        return ht;
      }
      url = url.substring(idx);
    }
    idx = url.lastIndexOf("#");
    if (idx >= 0) {
      try { ht.put("ref", url.substring(idx+1)); } catch(Exception e) {}
      url = url.substring(0, idx);
    }
    idx = url.lastIndexOf("?");
    if (idx >= 0) {
      try { ht.put("query_string", url.substring(idx+1)); } catch(Exception e) {}
      url = url.substring(0, idx);
    }
    ht.put("path", url);
    idx = url.lastIndexOf("/");
    if (idx >= 0) {
      try { x = url.substring(idx+1); }
      catch(Exception e) { x = ""; }
    } else {
      x = url;
    }
    ht.put("file_name", x);
    return ht;
  }

  public static String getColumnTypeDisplayName(ResultSetMetaData rsmd, int col) throws Exception
  {
    StringBuffer sb;
    String colType = rsmd.getColumnTypeName(col).toUpperCase();

    if (colType.equals("NUMBER"))
    {
      int prec = rsmd.getPrecision(col);
      if (prec == 0) return "NUMBER";
      sb = new StringBuffer(15);
      sb.append("NUMBER");
      sb.append("(");
      sb.append(prec);
      int scale = rsmd.getScale(col);
      if (scale > 0) { sb.append(","); sb.append(scale); }
      sb.append(")");
      return sb.toString();
    }

    if (colType.indexOf("CHAR") >= 0)
    {
       int dispSz = rsmd.getColumnDisplaySize(col);
       if (dispSz <= 1) return colType;
       return colType + "(" + dispSz + ")";
    }

    return colType;
  }

  public static Object newInstance(Class cls, Object param) throws Exception {
    Constructor[] ca = cls.getConstructors();
    for (int i=ca.length-1; i>=0; --i) {
      Constructor c = ca[i];
      Class[] ps = c.getParameterTypes();
      if (ps.length!=1) continue;
      if (param == null) {
        if (ps[0].isPrimitive()) continue;
        if (ps[0].isArray()) continue;
        return c.newInstance(new Object[]{null});
      }
      Class cl = param.getClass();
      if (ps[0].getName().equals(cl.getName()))
        return c.newInstance(new Object[]{param});
      if (ps[0].isInstance(param))
        return c.newInstance(new Object[]{param});
    }
    throw new IllegalArgumentException("Unable to instantiate a " + cls.getName());
  }

  public static char[] base64Encode(byte[] data) {

    char[] out = new char[((data.length + 2) / 3) * 4];
  
    //
    // 3 bytes encode to 4 chars.  Output is always an even
    // multiple of 4 characters.
    //
    for (int i=0, index=0; i<data.length; i+=3, index+=4) {
        boolean quad = false;
        boolean trip = false;

        int val = (0xFF & (int) data[i]);
        val <<= 8;
        if ((i+1) < data.length) {
            val |= (0xFF & (int) data[i+1]);
            trip = true;
        }
        val <<= 8;
        if ((i+2) < data.length) {
            val |= (0xFF & (int) data[i+2]);
            quad = true;
        }
        out[index+3] = alphabet[(quad? (val & 0x3F): 64)];
        val >>= 6;
        out[index+2] = alphabet[(trip? (val & 0x3F): 64)];
        val >>= 6;
        out[index+1] = alphabet[val & 0x3F];
        val >>= 6;
        out[index+0] = alphabet[val & 0x3F];
    }
    return out;
  }

  public static byte[] base64Decode(String data) { return base64Decode(data.toCharArray()); }

  public static byte[] base64Decode(char[] data) {
    int tempLen = data.length;
    for( int ix=0; ix<data.length; ix++ ) {
      if( (data[ix] > 255) || codes[ data[ix] ] < 0 )
        --tempLen;  // ignore non-valid chars and padding
    }

    int len = (tempLen / 4) * 3;
    if ((tempLen % 4) == 3) len += 2;
    if ((tempLen % 4) == 2) len += 1;

    byte[] out = new byte[len];

    int shift = 0;   // # of excess bits stored in accum
    int accum = 0;   // excess bits
    int index = 0;

    for (int ix=0; ix<data.length; ix++) {
      int value = (data[ix]>255)? -1: codes[ data[ix] ];
  
      if (value >= 0) {
        accum <<= 6;
        shift += 6;
        accum |= value;
        if (shift >= 8) {
          shift -= 8;
          out[index++] = (byte) ((accum >> shift) & 0xff);
        }
      }
    }
  
    if( index != out.length)
      throw new Error("Miscalculated data length (" + index + " instead of " + out.length + ")");
  
    return out;
  }
  
  //
  // code characters for values 0..63
  //
  // TODO: check on the base64 standard
  //
  private static char[] alphabet =
    "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/="
    .toCharArray();
  
  //
  // lookup table for converting base64 characters to value in range 0..63
  //
  static private byte[] codes = new byte[256];
  static {
    for (int i=0; i<256; i++) codes[i] = -1;
    for (int i = 'A'; i <= 'Z'; i++) codes[i] = (byte)(     i - 'A');
    for (int i = 'a'; i <= 'z'; i++) codes[i] = (byte)(26 + i - 'a');
    for (int i = '0'; i <= '9'; i++) codes[i] = (byte)(52 + i - '0');
    codes['+'] = 62;
    codes['/'] = 63;
  }

  // this one closes the result set.
  public static boolean hasResults(ResultSet rs) {
    try {
      boolean ret = rs.next();
      rs.close();
      return ret;
    } catch(Exception e) {}
    return false;
  }

  public static TableData describeResultSet(ResultSetMetaData rsmd) throws SQLException
  {
    String[] columnAttrs = {
          "name",           // String  getColumnName
          "label",          // String  getColumnLabel
          "type",           // String  getColumnTypeName
          "typeValue",      // int     getColumnType
          "displaySize",    // int     getColumnDisplaySize
          "precision",      // int     getPrecision
          "scale",          // int     getScale
          "nullable",       // int     isNullable           "NULL", "NOT NULL", "NULL UNKNOWN"
          "autoIncrement",  // boolean isAutoIncrement
          "className",      // String  getColumnClassName
          "writability"     // boolean isReadOnly           "READONLY"
                            // boolean isWritable           "WRITABLE"
                            // boolean isDefinitelyWritable "DEFINITELY WRITABLE"

                            // boolean isCaseSensitive
                            // String  getCatalogName
                            // String  getSchemaName
                            // String  getTableName
                            // boolean isSearchable
                            // boolean isCurrency
                            // boolean isSigned
        };

    TableData td = new TableData(columnAttrs, rsmd.isCaseSensitive(1));
    int len = rsmd.getColumnCount();
    for (int i=1; i<=len; i++) {
      try {
        int idx = td.size();
        td.setAt(idx, "name",          rsmd.getColumnName(i));
        td.setAt(idx, "label",         rsmd.getColumnLabel(i));
        td.setAt(idx, "type",          rsmd.getColumnTypeName(i));
        td.setAt(idx, "typeValue",     rsmd.getColumnType(i));
        td.setAt(idx, "displaySize",   rsmd.getColumnDisplaySize(i));
        td.setAt(idx, "precision",     rsmd.getPrecision(i));
        td.setAt(idx, "scale",         rsmd.getScale(i));
        td.setAt(idx, "autoIncrement", rsmd.isAutoIncrement(i));
        String x;
        switch(rsmd.isNullable(i)) {
        case ResultSetMetaData.columnNoNulls:  x = "NOT NULL"; break;
        case ResultSetMetaData.columnNullable: x = "NULL";     break;
        default:                               x = "NULL UNKNOWN";
        }
        td.setAt(idx, "nullable", x);
        if (rsmd.isReadOnly(i))
          td.setAt(idx, "writability", "READ ONLY");
        else if (rsmd.isDefinitelyWritable(i))
          td.setAt(idx, "writability", "DEFINITELY WRITABLE");
        else
          td.setAt(idx, "writability", "WRITABLE");
      } catch(IllegalArgumentException iae) {} // not happening.
    }
    return td;
  }

  public static TableData describeTable(Connection con, String table) throws SQLException {
    Statement stmt = con.createStatement();
    ResultSet rs = stmt.executeQuery("SELECT * FROM " + table + " WHERE 1 > 2");
    TableData td = describeResultSet(rs.getMetaData());
    rs.close();
    stmt.close();
    return td;
  }

  public static String unit(long cnt, String singular, String plural) {
    if (cnt == 1) return "" + cnt + " " + singular;
    return "" + cnt + " " + (StringUtils.isBlank(plural) ? (singular+"s") : plural);
  }

  public static ThreadGroup rootThreadGroup() {
    ThreadGroup parent = Thread.currentThread().getThreadGroup();
    ThreadGroup rootGroup;
    do {
      rootGroup = parent;
      parent = parent.getParent();
    } while (parent != null);
    return rootGroup;
  }

  public static Thread[] getThreads(ThreadGroup tg) {
    Thread[] ta = new Thread[tg.activeCount()];
    tg.enumerate(ta);
    return ta;
  }

  public static void killAllThreads(ThreadGroup tg) {
    if (tg == null) return;
    Thread[] threads = Lib.getThreads(tg);
    for (int i=0; i<threads.length; ++i)
      try { threads[i].stop(); } catch(Exception e) {}
  }

  public static Object getFirst(Object[] oa) {
    if (oa == null) return null;
    try { return oa[0]; }
    catch(ArrayIndexOutOfBoundsException aioobe) {}
    return null;
  }

  public static Class[] getPublicClasses(Object o, Class c) {
    Class[] ca = (c != null) ? getPublicClasses(c) : null;
    Class[] ca1 = (o != null) ? getPublicClasses(o.getClass()) : null;
    if (ca == null) return ca1;

    HashSet set = new HashSet();
    int i = (ca == null) ? 0 : ca.length;
    for (--i; i>=0;  --i) set.add(ca[i]);
    i = (ca1 == null) ? 0 : ca1.length;
    for (--i; i>=0;  --i) set.add(ca1[i]);
    ca = new Class[set.size()];
    Iterator iter = set.iterator();
    i = 0;
    while (iter.hasNext()) ca[i++] = (Class)iter.next();
    return ca;
  }

  public static Class[] getPublicClasses(Class c) {
    int mod = c.getModifiers();
    if (Modifier.isPublic(mod))
      return new Class[]{ c };

    // this class or interface is not public

    if (Modifier.isInterface(mod))
      return c.getInterfaces();

    Class supr = c.getSuperclass();
    if (supr != Object.class) {
      if (Modifier.isPublic(supr.getModifiers()))
        return new Class[]{ supr };
    }

    Class[] ca = c.getClasses();
    if (ca.length==0)
      ca = c.getInterfaces();

    return ca;
  }

  public static void ensurePath(File f) {
    f.getParentFile().mkdirs();
  }

  public static boolean isFileNewer(Object one, Object two) {
    File f1 = null;
    File f2 = null;
    try {
      f1 = (one instanceof File) ? (File)one : new File(getPath(one.toString()));
      f2 = (two instanceof File) ? (File)two : new File(getPath(two.toString()));
      if (!f1.exists()) return false;
      if (!f2.exists()) return true;
      return f1.lastModified() > f2.lastModified();
    } catch(Exception e) { return false; }
  }

  /**
   * Handles the leading '~' situation.
   */
  public static String getPath(String path) {
    try {
      if (path.startsWith("~"))
        return getHomeDir() + path.substring(1);
    } catch(Exception e) {}
    return path;
  }

  /**
   *  This only works for Unix and Windows now.
   *  For unsupported platforms, simply returns null.
   */
  private static Properties envVars = null;

  public static Properties getEnvVars() {
    if (envVars != null) return envVars;
    envVars = new Properties();
    try {
      if (isUnix()) {
        envVars.load(Runtime.getRuntime().exec("/usr/bin/env").getInputStream());
      } else if (isWindows()) {
        // we handle this ourself because Java preparses '\\'!
        File batFile = File.createTempFile("getevs",".bat");
        PrintWriter pw = new PrintWriter(new FileWriter(batFile));
        pw.println("set");
        pw.close();
        InputStream is = Runtime.getRuntime().exec(batFile.getPath()).getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        while (true) {
          String line = br.readLine();
          if (line == null) break;
          int idx = line.indexOf('=');
          if (idx <= 0) continue;
          String n = line.substring(0,idx);
          if (is_windows) n = n.toUpperCase();
          envVars.put(n, line.substring(idx+1));
        }
        br.close();
        try { batFile.delete(); } catch(Exception e) { /* ok if not cleaned. */ }
      } else {
        // other platforms: sorry!
        envVars = null;
      }
    } catch(Exception e) {
      //e.printStackTrace();
    }
    return envVars;
  }

  public static String getEnvVar(String name) {
    if (name.length() == 1) {
      switch (name.charAt(0)) {
      case ':': return File.pathSeparator;
      case '/': return File.separator;
      case '~': return is_windows ? getHomeDir().replace('\\','/') : getHomeDir();
      case '.': return is_windows ? getCurrentDir().replace('\\','/') : getCurrentDir();
      }
    }
    return (String)getEnvVars().get(is_windows ? name.toUpperCase() : name);
  }

  public static void gzip(String fname, String outFile, boolean compress) throws IOException {
    if (StringUtils.isBlank(outFile)) {
      if (compress)
        outFile = fname + ".gz";
      else {
        String ext = getFileExt(fname).toLowerCase();
        if (ext.equals("gz") || ext.equals("gzip"))
          outFile = fname.substring(0,fname.length() - ext.length() - 1);
        else
          outFile = fname + ".gunzipped";
      }
    }

    InputStream is = new FileInputStream(fname);
    OutputStream os = new FileOutputStream(outFile);
    if (compress) os = new GZIPOutputStream(os);
    else          is = new GZIPInputStream(is);
    copyStream(is,os);
    try { os.close(); } catch(Exception e) {}
    try { is.close(); } catch(Exception e) {}
  }

  public static boolean endsWith(StringBuffer sb, String s) {
    try {
      int j = sb.length() - 1;
      for (int i=s.length() - 1; i>=0; j--,i--)
        if (s.charAt(i) != sb.charAt(j)) return false;
      return true;
    } catch(Exception e) {
      return false;
    }
  }

  public static boolean startsWith(StringBuffer sb, String s) {
    try {
      int j = 0;
      for (int i=0; i<s.length(); j++,i++)
        if (s.charAt(i) != sb.charAt(j)) return false;
      return true;
    } catch(Exception e) {
      return false;
    }
  }

  public static int indexOf(StringBuffer sb, String s) {
    try {
loop: for (int j=0; j<=sb.length()-s.length(); j++) {
        for (int i=0; i<s.length(); i++)
          if (s.charAt(i) != sb.charAt(j+i)) continue loop;
        return j;
      }
    } catch(Exception e) {}
    return -1;
  }

  public static int lastIndexOf(StringBuffer sb, String s) {
    try {
loop: for (int j=sb.length()-s.length(); j>=0; j--) {
        for (int i=0; i<s.length(); i++)
          if (s.charAt(i) != sb.charAt(j+i)) continue loop;
        return j;
      }
    } catch(Exception e) {}
    return -1;
  }

  public static String capitalizeFirstLetter(String s) {
    if (s == null) return null;
    if (StringUtils.isBlank(s)) return "";
    return Character.toUpperCase(s.charAt(0)) + s.substring(1);
  }

  public static String capitalizeAllFirstLetters(String s) {
    if (s == null) return null;
    if (StringUtils.isBlank(s)) return "";
    StringBuffer sb = new StringBuffer();
    boolean doCap = true;
    for (int i=0; i<s.length(); ++i) {
      char ch = s.charAt(i);
      if (Character.isWhitespace(ch)) {
        sb.append(ch);
        doCap = true;
        continue;
      }
      if (doCap) {
        sb.append(Character.toUpperCase(ch));
        doCap = false;
      } else {
        sb.append(ch);
      }
    }
    return sb.toString();
  }

  public static int countFileLines(File f) throws IOException {
    BufferedReader br = null;
    int total = 0;
    try {
      if (f instanceof ZipFS.ZippedFile)
        br = ((ZipFS.ZippedFile)f).getBufferedReader();
      else
        br = new BufferedReader(new FileReader(f));
      while (null != br.readLine())
        ++total;
      return total;
    } finally {
      try { br.close(); } catch(Exception e) {}
    }
  }

  public static int countFileWords(File f) throws IOException {
    BufferedReader br = null;
    int total = 0;
    try {
      if (f instanceof ZipFS.ZippedFile)
        br = ((ZipFS.ZippedFile)f).getBufferedReader();
      else
        br = new BufferedReader(new FileReader(f));
      String line;
      while (null != (line=br.readLine()))
        total += countWords(line);
      return total;
    } finally {
      try { br.close(); } catch(Exception e) {} }
  }

  public static int countWords(String line) {
    if (StringUtils.isBlank(line))
      return 0;

    int cnt = 0;
    try {
      int ptr = 0;
      while (true) {
        while (!Character.isLetterOrDigit(line.charAt(ptr)))
          ++ptr;
        ++cnt;
        while (Character.isLetterOrDigit(line.charAt(ptr)))
          ++ptr;
      }
    } catch(Exception e) {}
    return cnt;
  }

  public static String escapeNL(String s) {
    if ((s.indexOf('\n') < 0) && (s.indexOf('\r') < 0)) return s;
    try {
      BufferedReader br = new BufferedReader(new StringReader(s));
      StringBuffer sb = new StringBuffer();
      boolean first = true;
      while (true) {
        String line = br.readLine();
         if (line == null) break;
         if (first) first = false;
        else sb.append("\\n");
         sb.append(line);
      }
      if (s.endsWith("\n") || s.endsWith("\r"))
        sb.append("\\n");
      return sb.toString();
    } catch(Exception e) {
      return s;
    }
  }

  public static String[] parseFixedPosition(String s, int[] lens) {
    if (s == null) return null;
    ArrayList v = new ArrayList();
    int idx = 0;
    int i;
    for (i=0; i<lens.length; ++i) {
      if (s.length() <= idx + lens[i]) {
        v.add(s.substring(idx));
        break;
      }
      v.add(s.substring(idx,idx+lens[i]));
      idx += lens[i];
    }
    idx = v.size();
    String[] ret = new String[idx];
    for (i=idx-1; i>=0; i--) ret[i] = (String)v.get(i);
    return ret;
  }

  public static String toFixedPosition(Object[] oa, int[] ia) {
    StringBuffer sb = new StringBuffer();
    for (int i=0; i<oa.length; i++) {
      if (i >= ia.length) sb.append(oa[i].toString());
      else sb.append(exactLength(oa[i].toString(),ia[i]));
    }
    return sb.toString();
  }

  public static String toOSPath(String path) {
    return isWindows() ? replace(true, path, "/", "\\") : replace(true, path, "\\", "/");
  }

  public static String getHostName() {
    try {
      return InetAddress.getLocalHost().getHostName();
    } catch(Exception e) {
      return "";
    }
  }

  public static String trim(String s, char decorator) {
    int len = s==null ? 0 : s.length();
    if (len <= 0)
      return s;

    int start = 0;
    int end = len-1;
    for (; (start<len) && (s.charAt(start)==decorator); ++start);
    for (; (end>start) && (s.charAt(end)==decorator); --end);
    return s.substring(start, end+1);
  }

  public static String trim(String s, String decorator) {
    if (StringUtils.isBlank(decorator))
      return s.trim();

    int len = decorator.length();
    if (len==1)
      return trim(s, decorator.charAt(0));

    for (; s.startsWith(decorator); s=s.substring(len));
    for (; s.endsWith(decorator); s=s.substring(0, s.length()-len));
    return s;
  }


  public static StringPrintWriter createStringPrintWriter() {
    return new StringPrintWriter();
  }

  /**
   * Parses the string for embedded tags. Tags starts with <i>leftDelim</i>
   * and ends with <i>rightDelim</i>.
   *
   *@return If no tags present, null is returned. Otherwise, a List of text/tags
   *        is returned, with elements 0, 2, 4, ... being text and 1, 3, 5, ...
   *        being tags; text elements can be "".
   */
  public static List stringTemplate(String s, String leftDelim, String rightDelim) {
    if (s == null || s.indexOf(leftDelim) < 0)
      return null;

    // We implement it without using regex to support more versions of JDK.
    int leftDelimLen = leftDelim.length();
    int rightDelimLen = rightDelim.length();
    int endIdx = 0;
    ArrayList ret = new ArrayList();
    int idx = s.indexOf(leftDelim);
    do {
      ret.add(s.substring(endIdx, idx)); // the text.

      idx += leftDelimLen;
      endIdx = s.indexOf(rightDelim, idx);
      String x;
      if (endIdx < 0) { // treat the rest of the string as a tag
        ret.add(s.substring(idx));
        break;
      } else {
        ret.add(s.substring(idx, endIdx));
      }

      endIdx += rightDelimLen;
      idx = s.indexOf(leftDelim, endIdx);
    } while (idx > 0);
    if (endIdx > 0)
      ret.add(s.substring(endIdx));
    return ret;
  }

  public static String[] linesToArray(String s) {
    BufferedReader br = new BufferedReader(new StringReader(s));
    ArrayList al = new ArrayList();
    while (true) {
      try {
        String line = br.readLine();
        if (line == null)
          break;
        al.add(line);
      } catch(Exception e) {
        e.printStackTrace(); // shouldn't happen, right?
      }
    }
    return (String[])al.toArray((Object[])new String[al.size()]);
  }

  //
  //
  private Lib() {}

/*
  public static void main(String[] args) {
    try {
      Class c = Class.forName("javax.swing.JMenu$AccessibleJMenu");
      Class ca[] = getPublicClasses(c);
      for (int i=0; i<ca.length; ++i)
        System.out.println(ca[i].getName());
    } catch(Exception e) { e.printStackTrace(); }
  }
*/

} // end of class Lib.
