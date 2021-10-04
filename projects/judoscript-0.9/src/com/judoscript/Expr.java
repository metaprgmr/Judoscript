/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 03-17-2002  JH   Initial open source release.
 * 06-24-2002  JH   Added isA() method.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript;

import java.io.Serializable;
import java.util.Stack;
import java.util.Date;
import com.judoscript.util.XMLDumpable;


public interface Expr extends Consts, XMLDumpable, Serializable
{
  //////////////////////////////////////////////
  // Types
  //

  public int getType(); // one of the TYPE_xxxx's.
  public int getJavaPrimitiveType(); // one of JAVA_xxxx's, or 0 for non-primitive types.

  public boolean isNil();
  public boolean isUnknownType();
  public boolean isInt();
  public boolean isDouble();
  public boolean isNumber();
  public boolean isString();
  public boolean isValue();
  public boolean isDate();
  public boolean isObject();
  public boolean isJava();
  public boolean isCOM();
  public boolean isFunction();
  public boolean isArray();
  public boolean isSet();
  public boolean isStack();
  public boolean isQueue();
  public boolean isStruct();
  public boolean isComplex();
  public boolean isWebService();
  public boolean isA(String name);

  public boolean isReadOnly();

  //////////////////////////////////////////////
  // Operations
  //

  public Variable eval() throws Throwable;

  public boolean getBoolValue() throws Throwable;
  public long    getLongValue() throws Throwable;
  public double  getDoubleValue() throws Throwable;
  public String  getStringValue() throws Throwable;
  public Object  getObjectValue() throws Throwable;
  public Date    getDateValue() throws Throwable;

  public Expr reduce(Stack stack);
  public Expr optimize();
}
