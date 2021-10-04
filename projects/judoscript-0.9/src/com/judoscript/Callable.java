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


public interface Callable extends java.io.Serializable
{
  public static final String illegalAccess = "Callable objects don't support property access.";

  public Variable invoke(String fxn, Expr[] params, int[] javaTypes) throws Throwable;
}
