/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 03-17-2002  JH   Initial open source release.
 * 08-28-2002  JH   Added getStackTraceAsString() method -- empty.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript.bio;

import com.judoscript.*;
import com.judoscript.util.Lib;

/**
 * If the exception is a regular Java exception, object is set to it
 * and an ExceptionRuntime is created herein.
 * <p>
 * If the exception is an ExceptionRuntime and is a RTERR_JAVA_EXCEPTION,
 * object is set to its enclosed exception.
 * <p>
 * If the exception is a non-RTERR_JAVA_EXCEPTION, object is set to it.
 */
public class ExcptError extends JavaObject
{
  ExceptionRuntime rte;
  String filename;

  public ExcptError(Exception e) {
    super(getInitExcptClass(e), getInitExcpt(e));
    rte = e instanceof ExceptionRuntime
          ? (ExceptionRuntime)e
          : new ExceptionRuntime(RTERR_JAVA_EXCEPTION, null, (Exception)e);
    filename = RT.getSrcFileName();
  }

  private static Class getInitExcptClass(Exception e) {
    if (e instanceof ExceptionRuntime) {
      ExceptionRuntime rte = (ExceptionRuntime)e;
      if (rte.getType() == RTERR_JAVA_EXCEPTION)
        return rte.getRealException().getClass();
      return ExceptionRuntime.class;
    }
    return e.getClass();
  }
  private static Throwable getInitExcpt(Exception e) {
    if (e instanceof ExceptionRuntime) {
      ExceptionRuntime rte = (ExceptionRuntime)e;
      if (rte.getType() == RTERR_JAVA_EXCEPTION)
        return rte.getRealException();
    }
    return e;
  }

  public void throwThis() throws Exception { throw (Exception)object; }

  public String getStringValue() throws Throwable { return object.toString(); }

  public boolean hasVariable(String name) {
    return "message".equals(name) || "msg".equals(name) ||
           "name".equals(name) ||
           "type".equals(name) ||
           "line".equals(name) ||
           "file".equals(name);
  }

  public Variable resolveVariable(String name) throws Throwable {
    if ("message".equals(name) || "msg".equals(name))
      return JudoUtil.toVariable(rte.getMessage());
    else if ("name".equals(name))
      return JudoUtil.toVariable(rte.getName());
    else if ("type".equals(name))
      return ConstInt.getInt(rte.getType());
    else if ("line".equals(name))
      return ConstInt.getInt(rte.getLineNumber());
    else if ("file".equals(name))
      return JudoUtil.toVariable(filename);
    return ValueSpecial.UNDEFINED;
  }

  public Variable invoke(String fxn, Expr[] params, int[] javaTypes) throws ExceptionRuntime, Throwable
  {
    if (fxn.equals("pist") || fxn.equals("printInternalStackTrace")) {
      ((Throwable)object).printStackTrace();
    } else if (fxn.equals("getStackTraceAsString")) {
      return JudoUtil.toVariable(Lib.getStackTrace((Throwable)object));
    } else if (fxn.equals("printStackTrace")) {
      RT.getErr().println(rte.toString());
    } else {
      return super.invoke(fxn,params,javaTypes);
    }
    return ValueSpecial.UNDEFINED;
  }

  public void close() {}

  public String getTypeName() { return "Exception"; }

  public ExceptionRuntime getException() { return rte; }

} // end of class ExcptError.
