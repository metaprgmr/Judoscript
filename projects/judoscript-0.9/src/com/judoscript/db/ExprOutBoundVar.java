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
import java.util.HashMap;
import java.util.Stack;
import java.sql.CallableStatement;
import java.sql.*;
import com.judoscript.*;
import com.judoscript.bio._Date;
import com.judoscript.util.XMLWriter;


public final class ExprOutBoundVar extends VariableAdapter
{
  String bindName;
  int bindIdx;
  public int sqlType;
  public ExprLValue hostVar;
  public boolean inOut = false;
  CallableStatement cs = null;

  public ExprOutBoundVar(int idx, String name, int type, ExprLValue var, boolean inOut) {
    bindIdx = idx;
    bindName = name;
    sqlType = type;
    hostVar = var;
    this.inOut = inOut;
  }

  public void setCallableStatement(CallableStatement cs) { this.cs = cs; }

  //public String getName() { return varName; }

  public String getTypeName() { return "outbound_variable"; }
  public int getType() { return TYPE_OBJECT; }
  public boolean isNil() { return false; }
  public boolean isUnknownType() { return false; }
  public boolean isInt() {
    switch(sqlType) {
    case Types.INTEGER:
    case Types.BIGINT:
    case Types.SMALLINT:
    case Types.TINYINT:  return true;
    }
    return false;
  }
  public boolean isDouble() {
    switch (sqlType) {
    case Types.NUMERIC:
    case Types.DOUBLE:
    case Types.FLOAT:    return true;
    }
    return false;
  }
  public boolean isNumber() {
    switch(sqlType) {
    case Types.NUMERIC:
    case Types.DOUBLE:
    case Types.FLOAT:
    case Types.INTEGER:
    case Types.BIGINT:
    case Types.SMALLINT:
    case Types.TINYINT:  return true;
    }
    return false;
  }
  public boolean isString() {
    return (sqlType == Types.VARCHAR) ||
           (sqlType == Types.LONGVARCHAR) ||
           (sqlType == Types.CHAR);
  }
  public boolean isDate() {
    switch (sqlType) {
    case Types.DATE:
    case Types.TIME:
    case Types.TIMESTAMP: return true;
    }
    return false;
  }
  public boolean isObject() { return (sqlType == Types.STRUCT); }
  public boolean isJava()   { return false; }
  public boolean isFunction() { return false; }
  public boolean isArray()  { return (sqlType == Types.ARRAY); }
  public boolean isSet()    { return false; }
  public boolean isStack()  { return false; }
  public boolean isQueue()  { return false; }
  public boolean isStruct() { return false; }
  public boolean isA(String name) { return ExprAnyBase.isA(this,name); }
  public boolean isReadOnly() { return false; }

  public Expr reduce(Stack stack) { return this; }
  public Expr optimize() { return this; }

  public Variable eval() throws Throwable { return hostVar.eval(); }

  public Variable bound() throws Throwable {
    Variable ret = ValueSpecial.UNDEFINED;
    switch(sqlType) {
    case Types.TINYINT:   ret = ConstInt.getInt(cs.getByte(bindIdx));  break;
    case Types.SMALLINT:  ret = ConstInt.getInt(cs.getShort(bindIdx)); break;
    case Types.INTEGER:   ret = ConstInt.getInt(cs.getInt(bindIdx));   break;
    case Types.BIGINT:    ret = ConstInt.getInt(cs.getLong(bindIdx));  break;
    case Types.FLOAT:     ret = new ConstDouble(cs.getFloat(bindIdx)); break;
    case Types.DOUBLE:
    case Types.NUMERIC:   ret = new ConstDouble(cs.getDouble(bindIdx));       break;
    case Types.CHAR:
    case Types.LONGVARCHAR:
    case Types.VARCHAR:   ret = JudoUtil.toVariable(cs.getString(bindIdx)); break;
    case Types.DATE:      ret = new _Date(cs.getDate(bindIdx).getTime()); break;
    case Types.TIME:      ret = new _Date(cs.getTime(bindIdx).getTime()); break;
    case Types.TIMESTAMP: ret = new _Date(cs.getTimestamp(bindIdx).getTime()); break;
    case ORACLE_CURSOR:   ret = JudoUtil.toVariable(cs.getObject(bindIdx));   break;
    case Types.OTHER:

    case ORACLE_ROWID:
    case ORACLE_BFILE:

    case Types.REF:
    case Types.VARBINARY:
    case Types.BLOB:
    case Types.CLOB:
    case Types.ARRAY:
    case Types.STRUCT:
    default:              break; // TODO
    }
    hostVar.setVariable(ret,sqlType);
    return ret;
  }
  public boolean getBoolValue() throws Throwable   { return eval().getBoolValue(); }
  public long    getLongValue() throws Throwable   { return eval().getLongValue(); }
  public double  getDoubleValue() throws Throwable { return eval().getDoubleValue(); }
  public String  getStringValue() throws Throwable { return eval().getStringValue(); }
  public Date    getDateValue() throws Throwable   { return eval().getDateValue(); }

  ////////////////////////////////////////////////////////////////////
  // Variable interface implementation
  //
  // big time trick (continued): all method should call their evalTo-counterpart,
  // which replaces the ExprOutBoundVar with respective Variable to complete the trick!
  //

  public Object  getObjectValue() throws Throwable {
    return null; // TODO
  }
  public boolean isValue()     { return true; }
  public Variable cloneValue() { return this; } // scarcely happen.

  long getEpoch() throws Throwable { return getDateValue().getTime(); }
  public java.sql.Date getSqlDate() throws Throwable { return new java.sql.Date(getEpoch()); }
  public java.sql.Time getSqlTime() throws Throwable { return new java.sql.Time(getEpoch()); }
  public java.sql.Timestamp getSqlTimestamp() throws Throwable { return new Timestamp(getEpoch()); }

  public Variable resolveVariable(String name) throws Throwable { return ValueSpecial.UNDEFINED; }

  public void close() { cs = null; }

  public int getBindIndex(HashMap namedBindParameters) throws Exception {
    if (bindName != null) {
      try {
        bindIdx = ((Integer)namedBindParameters.get(bindName)).intValue();
      } catch(Exception e) {
        ExceptionRuntime.rte(RTERR_ILLEGAL_ACCESS, "Invalid bind variable name: '" + bindName + "'.");
      }
    }
    return bindIdx;
  }

  public void dump(XMLWriter out) {
    out.openTag("ExprOutBoundVar");
    out.tagAttr("index",String.valueOf(bindIdx));
    out.tagAttr("sqlType",DBHandle.getSqlTypeName(sqlType));
    out.endTag();    
    hostVar.dump(out);
    out.closeTag();    
  }

} // end of class ExprOutBoundVar.
