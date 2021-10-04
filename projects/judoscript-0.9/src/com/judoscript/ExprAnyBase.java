/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 03-17-2002  JH   Initial open source release.
 * 06-23-2002  JH   Added isA().
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript;

import java.sql.Timestamp;
import java.util.Stack;
import com.judoscript.bio.*;


public abstract class ExprAnyBase implements Expr
{
  public boolean getBoolValue() throws Throwable { return eval().getBoolValue(); }
  public long getLongValue() throws Throwable { return eval().getLongValue(); }
  public double getDoubleValue() throws Throwable { return eval().getDoubleValue(); }
  public String getStringValue() throws Throwable { return eval().getStringValue(); }
  public Object getObjectValue() throws Throwable { return eval().getObjectValue(); }
  public java.util.Date getDateValue() throws Throwable { return eval().getDateValue(); }
  public java.sql.Date getSqlDate() throws Throwable { return new java.sql.Date(getDateValue().getTime()); }
  public java.sql.Time getSqlTime() throws Throwable { return new java.sql.Time(getDateValue().getTime()); }
  public Timestamp getSqlTimestamp() throws Throwable { return new Timestamp(getDateValue().getTime()); }

  /**
   * This is a Variable method.
   */
  public Object[] getObjectArrayValue() throws Throwable {
    return new Object[]{ getObjectValue() };
  }

  public int getType() { return TYPE_UNKNOWN; }
  public int getJavaPrimitiveType() { return 0; }
  public void setJavaPrimitiveType(int type) {}
  public boolean isNil() { return false; }

  public boolean isUnknownType()  { return TYPE_UNKNOWN==getType(); }
  public boolean isInternal()     { return TYPE_INTERNAL==getType(); }
  public boolean isInt()          { return TYPE_INT==getType(); }
  public boolean isDouble()       { return TYPE_DOUBLE==getType(); }
  public boolean isNumber()       { return isNumber(getType()); }
  public boolean isString()       { return TYPE_STRING==getType(); }
  public boolean isValue()        { return isValue(getType()); }
  public boolean isDate()         { return TYPE_DATE==getType(); }
  public boolean isObject()       { return TYPE_OBJECT==getType(); }
  public boolean isJava()         { return TYPE_JAVA==getType(); }
  public boolean isCOM()          { return TYPE_COM==getType(); }
  public boolean isFunction()     { return TYPE_FUNCTION==getType(); }
  public boolean isArray()        { return TYPE_ARRAY==getType(); }
  public boolean isSet()          { return TYPE_SET==getType(); }
  public boolean isStack()        { return TYPE_STACK==getType(); }
  public boolean isQueue()        { return TYPE_QUEUE==getType(); }
  public boolean isStruct()       { return TYPE_STRUCT==getType(); }
  public boolean isComplex()      { return TYPE_COMPLEX==getType(); }
  public boolean isWebService()   { return TYPE_WS==getType(); }
  public boolean isA(String name) { return isA(this,name); }
  public boolean isReadOnly() { return false; }

  public Expr optimize() { return this; }
  public Expr reduce(Stack stack) { return this; }
  //public void dump(XMLWriter out) {}

  public String toString() {
    try { return getStringValue(); }
    catch(Throwable e) { return ""; }
  }

  public static boolean isInt(int typ) { return (typ==TYPE_INT); }
  public static boolean isDouble(int typ) { return (typ==TYPE_DOUBLE); }
  public static boolean isNumber(int typ) { return (typ==TYPE_INT) || (typ==TYPE_DOUBLE) || (typ==TYPE_DATE); }
  public static boolean isDate(int typ) { return (typ==TYPE_DATE); }
  public static boolean isValue(int typ) { return isNumber(typ) || (typ==TYPE_STRING); }

  public static boolean isA(Expr inst, String name) {
    // check on Java class instances first:
    if (inst instanceof JavaObject) {
      try {
        return RT.getClass(name).isInstance(((JavaObject)inst).getObjectValue());
      } catch(Throwable e) {}
    }

    // check on user-defined struct/orderedMap/treeNode instances:
    if (inst instanceof UserDefined)
      return ((UserDefined)inst).isA(name);

    // check on known names:
    if (name.equalsIgnoreCase("null"))     return inst.isNil();
    if (name.equalsIgnoreCase("number"))   return inst.isNumber();
    if (name.equalsIgnoreCase("int"))      return inst.isInt();
    if (name.equalsIgnoreCase("integer"))  return inst.isInt();
    if (name.equalsIgnoreCase("double"))   return inst.isDouble();
    if (name.equalsIgnoreCase("float"))    return inst.isDouble();
    if (name.equalsIgnoreCase("string"))   return inst.isString();
    if (name.equalsIgnoreCase("date"))     return inst.isDate();
    if (name.equalsIgnoreCase("java"))     return inst.isJava();
    if (name.equalsIgnoreCase("function")) return inst.isFunction();
    if (name.equalsIgnoreCase("array"))    return inst.isArray();
    if (name.equalsIgnoreCase("stack"))    return inst.isStack();
    if (name.equalsIgnoreCase("queue"))    return inst.isQueue();
    return false;
  }

} // end of class ExprAnyBase.
