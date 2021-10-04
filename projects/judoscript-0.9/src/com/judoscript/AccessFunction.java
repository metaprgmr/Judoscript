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

import com.judoscript.util.XMLWriter;


public class AccessFunction extends ExprAnyBase implements Variable, MethodOrdinals
{
  String name;

  public AccessFunction(String fxnname) { name = fxnname; }

  public int compareTo(Object o) { return 0; }

  public final Variable eval() throws Throwable { return this; }

  public String getTypeName() { return "function_ref"; }
  public final String getName() { return name; }
  public final int getType() { return TYPE_FUNCTION; }
  public final boolean isNil() { return false; }

  public boolean getBoolValue() throws Throwable { return true; }
  public long   getLongValue() throws Throwable { return 0; }
  public double getDoubleValue() throws Throwable { return 0.0; }
  public String getStringValue() throws Throwable { return name + "()"; }
  public Object getObjectValue() throws Throwable { return this; }
  public java.util.Date getDateValue() throws Throwable { return new java.util.Date(0); }
  public java.sql.Date getSqlDate() { return null; }
  public java.sql.Time getSqlTime() { return null; }
  public java.sql.Timestamp getSqlTimestamp() { return null; }
  public Variable cloneValue() { return this; }
  public Variable invoke(String fxn, Expr[] params, int[] javaTypes) {
    switch (VariableAdapter.getMethodOrdinal(fxn)) {
    case BIM_ISFUNCTION: return ConstInt.TRUE;
    case BIM_TYPENAME:   return new ConstString(getTypeName());
    }
    return ValueSpecial.UNDEFINED;
  }
  public Variable resolveVariable(String name) throws Throwable   { return ValueSpecial.UNDEFINED; }
  public Variable resolveVariable(Variable name) throws Throwable { return ValueSpecial.UNDEFINED; }
  public void close() {}

  public String toString() {
    try { return getStringValue(); }
    catch(Throwable e) {
      return "";   
    }
  }

  public void dump(XMLWriter out) {
    out.openTag("AccessFunction");
    out.tagAttr("name", name);
    out.closeSingleTag();
  }
}
