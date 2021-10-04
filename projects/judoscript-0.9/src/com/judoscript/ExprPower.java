/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 05-01-2005  JH   Initial open source release.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript;

import com.judoscript.util.XMLWriter;


public class ExprPower extends ExprSingleBase
{
  Expr expon;

  public ExprPower(Expr base, Expr expon) { super(base); this.expon = expon; }

  public int getType() { return TYPE_DOUBLE; }
  public int getJavaPrimitiveType() { return JAVA_DOUBLE; }

  public Variable eval() throws Throwable {
    try {
      double exp = expon.getDoubleValue();
      if (exp == 0)
        return ConstInt.ONE;
      if (exp == 1)
        return expr.eval();
      return new ConstDouble(Math.pow(expr.getDoubleValue(), expon.getDoubleValue()));
    } catch(ExceptionSpecialValue esv) {
      return esv.getResult();
    }
  }

  public void dump(XMLWriter out) {
    out.simpleTag("ExprPower");
    expr.dump(out);
    out.print(" ** ");
    expon.dump(out);
    out.closeTag();
  }

} // end of class ExprPower.
