/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 03-17-2002  JH   Initial open source release.
 * 06-05-2003  JH   Added resolveVariable(Variable).
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript;


public interface Variable extends Expr, Comparable
{
  public boolean isInternal();
  public String  getTypeName();
  public Variable cloneValue();
  public void setJavaPrimitiveType(int type);

  public java.sql.Date getSqlDate() throws Throwable;
  public java.sql.Time getSqlTime() throws Throwable;
  public java.sql.Timestamp getSqlTimestamp() throws Throwable;

  public Variable invoke(String fxn, Expr[] params, int[] javaTypes) throws Throwable;

  public Variable resolveVariable(String name) throws Throwable;
  public Variable resolveVariable(Variable name) throws Throwable;

  /**
   *  When isArray(), returns an array of Object values.
   *  Otherwise, returns an array with one element, its Object value.
   */
  public Object[] getObjectArrayValue() throws Throwable;

  public void close();
}
