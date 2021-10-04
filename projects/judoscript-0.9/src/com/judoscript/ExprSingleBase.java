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

import java.util.Stack;


public abstract class ExprSingleBase extends ExprAnyBase
{
  protected Expr expr;

  public ExprSingleBase() { this.expr = null; }
  public ExprSingleBase(Expr expr) { this.expr = expr; }

  public Variable eval() throws Throwable { // for numeric
    try {
      if (isInt()) return ConstInt.getInt(getLongValue());
      return ConstDouble.getDouble(getDoubleValue());
    } catch(ExceptionSpecialValue esv) {
      return esv.getResult();
    }
  }

  public int getType() { return expr.getType(); }
  public int getJavaPrimitiveType() { return expr.getJavaPrimitiveType(); }
  public boolean isNil() { return false; }

  public Expr reduce(Stack stack) {
    if (expr != null)
      expr = expr.reduce(stack);
    return this;
  }

} // end of class ExprSingleBase.
