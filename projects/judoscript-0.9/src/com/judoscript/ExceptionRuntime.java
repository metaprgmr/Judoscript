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

import org.apache.commons.lang.StringUtils;
import com.judoscript.util.Lib;
import com.judoscript.util.IntStack;


public class ExceptionRuntime extends Exception implements Consts
{
  public static String getName(int typ) {
    try { return rterr_names[typ]; } catch(Exception e) { return rterr_names[0]; }
  }

  int type;
  Throwable th;
  int stmtLines[];
  

  public ExceptionRuntime(int type, String msg, Throwable t) {
    super(msg);
    this.type = type;
    this.th = t;

    IntStack stmtLineStk = RT.curCtxt().stmtLineStack;
    stmtLines = stmtLineStk.toIntArray();
  }

  public final int getType() { return type; }
  public final String getName() { return getName(type); }
  public String getMessage() { return Lib.getExceptionMsg(super.getMessage(), th); }
  public Throwable getRealException() { return th; }
  public void printStackTrace() {
    if (th != null)
      th.printStackTrace();
    else
      super.printStackTrace();
  } 
  public int getLineNumber() {
    try { return stmtLines[stmtLines.length-1]; }
    catch(Exception e) { return -1; }
  }

  public String toString() {
    StringBuffer sb = new StringBuffer("Runtime error #");
    sb.append(type);
    sb.append(" (");
    sb.append(getName());
    sb.append(")");
    String msg = super.getMessage();
    if (StringUtils.isNotBlank(msg)) {
      sb.append(": ");
      sb.append(msg);
    } else if (th == null) {
      sb.append('.');
    }
    if (th != null) {
      msg = th.getMessage();
      if (StringUtils.isNotBlank(msg)) {
        sb.append("\n -- ");
        sb.append(msg);
      }
    }

    sb.append("\nCall stack: ");
    for (int i=stmtLines.length-1; i>=0; --i) {
      sb.append("\n   at line ");
      if (stmtLines[i] <= 0)
        sb.append("UNKNOWN");
      else
        sb.append(stmtLines[i]);
    }
    return sb.toString();
  }

  public void report() { RT.getErr().println(toString()); }

  public static void methodNotFound(String objType, String fxn) throws ExceptionRuntime {
    if (RT.ignoreUnfoundMethods()) return;
    String msg = null;
    if (objType == null) msg = "Function " + fxn + "()";
    else msg = "Method " + fxn + "() in " + objType;
    throw new ExceptionRuntime(RTERR_METHOD_NOT_FOUND, msg+" not found.",null);
  }

  public static void rte(int type) throws ExceptionRuntime {
    rte(type, null, null);
  }

  public static void rte(int type, String msg) throws ExceptionRuntime {
    rte(type, msg, null);
  }

  public static void assertFail(String msg) throws ExceptionRuntime {
    if (!RT.ignoreAssertions())
      throw new ExceptionAssertion(msg, RT.getLineNumber());
  }

  public static void rte(int type, String msg, Throwable t) throws ExceptionRuntime {
    throw new ExceptionRuntime(type, msg, t);
  }

  public static void badParams(String mthd, String p1) throws ExceptionRuntime {
    badParams(mthd, new String[]{p1});
  }

  public static void badParams(String mthd, String p1, String p2) throws ExceptionRuntime {
    badParams(mthd, new String[]{p1,p2});
  }

  public static void badParams(String mthd, String p1, String p2, String p3) throws ExceptionRuntime {
    badParams(mthd, new String[]{p1,p2,p3});
  }

  public static void badParams(String mthd, String p1, String p2, String p3, String p4) throws ExceptionRuntime {
    badParams(mthd, new String[]{p1,p2,p3,p4});
  }

  public static void badParams(String mthd, String[] sa) throws ExceptionRuntime {
    StringBuffer sb = new StringBuffer("Function ");
    sb.append(mthd);
    sb.append("() takes ");
    sb.append(sa.length);
    sb.append(" parameters: ");
    for (int i=0; i<sa.length; ++i) {
      if (i>0) {
        if (i==sa.length-1) sb.append(" and ");
        else sb.append(", ");
      }
      sb.append(sa[i]);
    }
    ExceptionRuntime.rte(RTERR_ILLEGAL_ARGUMENTS, sb.toString());
  }

} // end of class ExceptionRuntime.
