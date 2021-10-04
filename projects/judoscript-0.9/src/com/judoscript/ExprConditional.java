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
import com.judoscript.util.XMLWriter;


public class ExprConditional extends ExprSingleBase
{
  Expr lhs;
  Expr rhs;

  public ExprConditional(Expr cond, Expr lhs, Expr rhs) {
    super(cond);
    this.lhs = lhs;
    this.rhs = rhs;
  }

  public int getType() { return lhs.getType() ; }

  public Variable eval() throws Throwable {
    return expr.getBoolValue() ? lhs.eval() : rhs.eval();
  }
  public long getLongValue() throws Throwable {
    return expr.getBoolValue() ? lhs.getLongValue() : rhs.getLongValue();
  }
  public double getDoubleValue() throws Throwable {
    return expr.getBoolValue() ? lhs.getDoubleValue() : rhs.getDoubleValue();
  }
  public String getStringValue() throws Throwable {
    return expr.getBoolValue() ? lhs.getStringValue() : rhs.getStringValue();
  }

  public Expr reduce(Stack stack) {
    expr = expr.reduce(stack);
    lhs = lhs.reduce(stack);
    rhs = rhs.reduce(stack);
    return this;
  }

  public void dump(XMLWriter out) {
    out.simpleTag("ExprConditional");
    expr.dump(out);
    out.print(" ? ");
    lhs.dump(out);
    out.print(" : ");
    rhs.dump(out);
    out.endTag();
  }

}
