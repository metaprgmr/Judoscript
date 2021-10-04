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

import com.judoscript.util.XMLWriter;


public class ExprNegate extends ExprSingleBase
{
  public ExprNegate(Expr e) { super(e); }

  public Variable eval() throws Throwable {
    Variable val = expr.eval();
    if (val instanceof ValueSpecial)
      return ValueSpecial.negate((ValueSpecial)val);
    if (isInt())
      return ConstInt.getInt(-val.getLongValue());
    return ConstDouble.getDouble(-val.getDoubleValue());
  }

  public long getLongValue() throws Throwable {
    try {
      return -expr.getLongValue();
    } catch(ExceptionSpecialValue esv) {
      if (esv == ExceptionSpecialValue.POSITIVE_INFINITY) throw ExceptionSpecialValue.NEGATIVE_INFINITY;
      if (esv == ExceptionSpecialValue.NEGATIVE_INFINITY) throw ExceptionSpecialValue.POSITIVE_INFINITY;
      if (esv == ExceptionSpecialValue.MAX_NUMBER) throw ExceptionSpecialValue.MIN_NUMBER;
      if (esv == ExceptionSpecialValue.MIN_NUMBER) throw ExceptionSpecialValue.MAX_NUMBER;
      throw esv;
    }
  }

  public double getDoubleValue() throws Throwable {
    try {
      return -expr.getDoubleValue();
    } catch(ExceptionSpecialValue esv) {
      if (esv == ExceptionSpecialValue.POSITIVE_INFINITY) throw ExceptionSpecialValue.NEGATIVE_INFINITY;
      if (esv == ExceptionSpecialValue.NEGATIVE_INFINITY) throw ExceptionSpecialValue.POSITIVE_INFINITY;
      if (esv == ExceptionSpecialValue.MAX_NUMBER) throw ExceptionSpecialValue.MIN_NUMBER;
      if (esv == ExceptionSpecialValue.MIN_NUMBER) throw ExceptionSpecialValue.MAX_NUMBER;
      throw esv;
    }
  }

  public void dump(XMLWriter out) {
    out.simpleTag("ExprNegate");
    out.print(" - ");
    expr.dump(out);
    out.endTag();
  }

} // end of class ExprNegate.
