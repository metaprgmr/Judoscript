/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 01-27-2005  JH   Inception.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript;

import java.util.*;
import com.judoscript.util.XMLWriter;

/**
 * This class is wraps a runtime-instance along with its script,
 * and make it available to other scripts running in multiple threads,
 * such JUSP pages. For each invocation, its global context is pushed
 * onto the client's context stack.
 *<p>
 * Let's call the caller as a client, running in its context called
 * RTC; the script object is called a server, running in its context
 * of RGC. Parameters and return values are conceptually passed
 * "by value".
 *<p>
 * Only server functions can be accessed. Try to refrain from using
 * global variable as much possible, especially as server programs.
 * When need to, treat the code as running in separate threads and
 * enforce concurrency constraints wherever appropriate.
 */
public class CallableLocal implements Variable, Callable
{
  Script script;
  RuntimeGlobalContext rgc;

  CallableLocal(Script script, RuntimeGlobalContext rgc) {
    this.script = script;
    this.rgc = rgc;
  }

  public Variable invoke(String fxn, Expr[] params, int[] javaTypes) throws Throwable
  {
    try {
      RT.pushContext(rgc);
      return script.invoke(fxn, params, javaTypes);
    } finally {
      RT.popContext();
    }
  }

  void illegalAccess() throws IllegalAccessException {
    throw new IllegalAccessException(illegalAccess);
  }

  ////////////////////////////////////////////////////
  // Variable methods (except for invoke())
  //

  public boolean isInternal() { return false; }
  public String  getTypeName() { return "LocalCallable"; }
  public Variable cloneValue() { return this; }
  public void setJavaPrimitiveType(int type) {}

  public java.sql.Date getSqlDate() throws Throwable { illegalAccess(); return null; }
  public java.sql.Time getSqlTime() throws Throwable { illegalAccess(); return null; }
  public java.sql.Timestamp getSqlTimestamp() throws Throwable { illegalAccess(); return null; }
  public Variable resolveVariable(String name) throws Throwable { illegalAccess(); return null; }
  public Variable resolveVariable(Variable name) throws Throwable { illegalAccess(); return null; }

  /**
   *  When isArray(), returns an array of Object values.
   *  Otherwise, returns an array with one element, its Object value.
   */
  public Object[] getObjectArrayValue() throws Throwable { illegalAccess(); return null; }

  public void close() { script = null; rgc = null; }

  ////////////////////////////////////////////////////
  // Expr methods (except for invoke())
  //

  public int getType() { return TYPE_CALLABLE; }
  public int getJavaPrimitiveType() { return 0; }

  public boolean isNil() { return false; }
  public boolean isUnknownType() { return false; }
  public boolean isInt() { return false; }
  public boolean isDouble() { return false; }
  public boolean isNumber() { return false; }
  public boolean isString() { return false; }
  public boolean isValue() { return false; }
  public boolean isDate() { return false; }
  public boolean isObject() { return false; }
  public boolean isJava() { return false; }
  public boolean isCOM() { return false; }
  public boolean isFunction() { return false; }
  public boolean isArray() { return false; }
  public boolean isSet() { return false; }
  public boolean isStack() { return false; }
  public boolean isQueue() { return false; }
  public boolean isStruct() { return false; }
  public boolean isComplex() { return false; }
  public boolean isWebService() { return false; }
  public boolean isA(String name) { return name.indexOf("Callable") >= 0; }

  public boolean isReadOnly() { return true; }

  public Variable eval() throws Throwable { return this; }

  public boolean getBoolValue() throws Throwable { illegalAccess(); return false; }
  public long    getLongValue() throws Throwable { illegalAccess(); return 0; }
  public double  getDoubleValue() throws Throwable { illegalAccess(); return 0; }
  public String  getStringValue() throws Throwable { illegalAccess(); return null; }
  public Object  getObjectValue() throws Throwable { illegalAccess(); return null; }
  public Date    getDateValue() throws Throwable { illegalAccess(); return null; }

  public Expr reduce(Stack stack) { return this; }
  public Expr optimize() { return this; }

  public void dump(XMLWriter out) {
    out.println("CallableLocal");
  }

  public int compareTo(Object o) { return 0; }

  ////////////////////////////////////////////////////
  // The JVM-wide CallableLocal registry
  //

  private static HashMap registry = new HashMap();

  public static void registerCallable(String name, Script script, RuntimeGlobalContext rgc)
    throws IllegalAccessException
  {
    if (registry.containsKey(name))
      throw new IllegalAccessException("Attempting to register service '" + name + "' more than once.");

    registry.put(name, new CallableLocal(script, rgc));
  }

  public static Callable getCallable(String name) throws IllegalAccessException {
    Callable c = (Callable)registry.get(name);
    if (c == null)
      throw new IllegalAccessException("Service '" + name + "' not found.");
    return c;
  }
  
} // end of class CallableLocal.
