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

public interface ExprLValue extends Expr
{
  public String getName();
  public Variable setVariable(Variable val, int type) throws Throwable;
  public Variable selfNumericOp(int op, double val, int type) throws Throwable;
  public Variable selfStringOp(String val, int type) throws Throwable;
}
