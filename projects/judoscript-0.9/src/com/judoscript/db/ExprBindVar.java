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


package com.judoscript.db;

import java.util.Date;
import java.util.Stack;
import com.judoscript.*;
import com.judoscript.util.XMLWriter;


public final class ExprBindVar implements Expr
{
  String bindName;
  int bindIdx;
  public int sqlType;
  public String typeName;
  public Expr expr;

  public ExprBindVar(int idx, String name, int type, String typeName, Expr expr) {
    this.expr = expr;
    bindIdx = idx;
    bindName = name;
    sqlType = type;
    this.typeName = typeName;
  }

  public int getType() { return expr.getType(); }
  public int getJavaPrimitiveType() { return expr.getJavaPrimitiveType(); }
  public boolean isNil()          { return expr.isNil(); }
  public boolean isUnknownType()  { return expr.isUnknownType(); }
  public boolean isInt()          { return expr.isInt(); }
  public boolean isDouble()       { return expr.isDouble(); }
  public boolean isNumber()       { return expr.isNumber(); }
  public boolean isString()       { return expr.isString(); }
  public boolean isValue()        { return expr.isValue(); }
  public boolean isDate()         { return expr.isDate(); }
  public boolean isObject()       { return expr.isObject(); }
  public boolean isJava()         { return expr.isJava(); }
  public boolean isCOM()          { return expr.isCOM(); }
  public boolean isFunction()     { return expr.isFunction(); }
  public boolean isArray()        { return expr.isArray(); }
  public boolean isSet()          { return expr.isSet(); }
  public boolean isStack()        { return expr.isStack(); }
  public boolean isQueue()        { return expr.isQueue(); }
  public boolean isStruct()       { return expr.isStruct(); }
  public boolean isComplex()      { return expr.isComplex(); }
  public boolean isWebService()   { return expr.isWebService(); }
  public boolean isA(String name) { return expr.isA(name); }
  public boolean isReadOnly() { return expr.isReadOnly(); }
  public Expr reduce(Stack stack) { expr = expr.reduce(stack); return this; }
  public Expr optimize() { return this; }

  public Variable eval() throws Throwable {
    return (sqlType == PREPARED_STMT_CALL) ? ValueSpecial.NIL : expr.eval();
  }
  public boolean getBoolValue()   throws Throwable { return expr.getBoolValue(); }
  public long    getLongValue()   throws Throwable { return expr.getLongValue(); }
  public double  getDoubleValue() throws Throwable { return expr.getDoubleValue(); }
  public String  getStringValue() throws Throwable { return expr.getStringValue(); }
  public Object  getObjectValue() throws Throwable { return expr.getObjectValue(); }
  public Date    getDateValue()   throws Throwable { return expr.getDateValue(); }

  public void dump(XMLWriter out) {
    out.openTag("ExprBindVar");
    out.tagAttr("index",String.valueOf(bindIdx));
    out.tagAttr("sqlType",DBHandle.getSqlTypeName(sqlType));
    out.closeTag();
    expr.dump(out);
    out.endTag();    
  }

} // end of class ExprBindVar.
