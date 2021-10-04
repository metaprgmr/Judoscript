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

import java.util.Date;


public abstract class ExprNewBase implements Expr, Stmt
{
  public boolean getBoolValue() throws Throwable   { return eval().getBoolValue(); }
  public long    getLongValue() throws Throwable   { return eval().getLongValue(); }
  public double  getDoubleValue() throws Throwable { return eval().getDoubleValue(); }
  public String  getStringValue() throws Throwable { return eval().getStringValue(); }
  public Object  getObjectValue() throws Throwable { return eval().getObjectValue(); }
  public Date    getDateValue() throws Throwable   { return new Date(); }

  public boolean isNil()          { return false; }
  public boolean isUnknownType()  { return false; }
  public boolean isInt()          { return false; }
  public boolean isDouble()       { return false; }
  public boolean isNumber()       { return false; }
  public boolean isString()       { return false; }
  public boolean isValue()        { return false; }
  public boolean isDate()         { return false; }
  public boolean isObject()       { return false; }
  public boolean isJava()         { return false; }
  public boolean isCOM()          { return false; }
  public boolean isFunction()     { return false; }
  public boolean isArray()        { return false; }
  public boolean isSet()          { return false; }
  public boolean isStack()        { return false; }
  public boolean isQueue()        { return false; }
  public boolean isStruct()       { return false; }
  public boolean isComplex()      { return false; }
  public boolean isWebService()   { return false; }
  public boolean isA(String name) { return false; }

  public boolean isReadOnly() { return false; }
  public Expr optimize() { return this; }
  public int getJavaPrimitiveType() { return 0; }

  // Stmt piece

  int lineNum;
  int fileIndex = 0;

  protected ExprNewBase(int line) { lineNum = line; }

  public void setLineNumber(int line) { lineNum = line; }
  public int getLineNumber() { return lineNum; }

  public void setFileIndex(int findex) { fileIndex = findex; }
  public int getFileIndex() { return fileIndex; }

  public Stmt optimizeStmt() { return this; }

  public void pushNewFrame() throws Exception {
    ExceptionRuntime.rte(RTERR_INTERNAL_ERROR,"This statement should not create new frame!");
  }
  public void popFrame() {}

  public void exec() throws Throwable { eval(); }
}
