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


public class ExprShift extends ExprSingleBase
{
  Expr rhs;
  int op;

  public ExprShift(Expr lhs, int op, Expr rhs) {
    super(lhs);
    this.op = op;
    this.rhs = rhs;
  }

  public int getType() { return TYPE_INT; }
  public int getJavaPrimitiveType() { return JAVA_LONG; }

  public Variable eval() throws Throwable {
    try { return ConstInt.getInt(getLongValue()); }
    catch(ExceptionSpecialValue esv) { return esv.getResult(); }
  }

  public double getDoubleValue() throws Throwable { return (double)getLongValue(); }

  public long getLongValue() throws Throwable {
    long left, right = 0;
    try {
      left = expr.getLongValue();
    } catch(ExceptionSpecialValue esv) {
      throw esv;
    }

    try {
      right = rhs.getLongValue();
    } catch(ExceptionSpecialValue esv) {
      switch(op) {
      case OP_LSHIFT:  throw esv;
      case OP_RUSHIFT: if (left<0)
                         throw ExceptionSpecialValue.MIN_NUMBER;
      case OP_RSHIFT:  left = 0;  break;
      default: left = 0; // shouldn't happen.
      }
    }

    switch(op) {
    case OP_LSHIFT:  left <<= right;  break;
    case OP_RSHIFT:  left >>= right;  break;
    case OP_RUSHIFT: left >>>= right; break;
    default: left = 0; // shouldn't happen.
    }
    return left;
  }

  public void dump(XMLWriter out) {
    out.simpleTag("ExprShift");
    expr.dump(out);
    switch(op) {
    case OP_LSHIFT:  out.print(" &lt;&lt; ");     break;
    case OP_RSHIFT:  out.print(" &gt;&gt; ");     break;
    case OP_RUSHIFT: out.print(" &gt;&gt;&gt; "); break;
    }
    rhs.dump(out);
    out.endTag();
  }

} // end of class ExprShift.
