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


public class ExprAddMinus extends ExprMultiBase
{
  // ops[] are OP_PLUS/MINUS's

  public ExprAddMinus(Expr first, int[] ops, Expr[] rest) { super(first,ops,rest); }

  public int getType() {
    if (!expr.isInt()) return TYPE_DOUBLE;
    for (int i=0; i<parts.length; i++) {
      if (!parts[i].isInt()) return TYPE_DOUBLE;
    }
    return TYPE_INT;
  }

  // Implement '+' either as a number or string.
  public Variable eval() throws Throwable {
    Variable res = expr.eval();
    for (int i=0; i<parts.length; i++) {
      Variable tmp = parts[i].eval();
      if (ops[i] == OP_PLUS) {
        if (res.isNumber() && tmp.isNumber()) {
          if (res instanceof ValueSpecial || tmp instanceof ValueSpecial)
            res = ValueSpecial.handleAddMinus(res, OP_PLUS, tmp);
          else if (res.isInt() && tmp.isInt())
            res = ConstInt.getInt(res.getLongValue() + tmp.getLongValue());
          else
            res = ConstDouble.getDouble(res.getDoubleValue() + tmp.getDoubleValue());
        } else {
          res = JudoUtil.toVariable(res.getStringValue() + tmp.getStringValue());
        }
      } else {
        if (res.isNumber() && tmp.isNumber()) {
          if (res instanceof ValueSpecial || tmp instanceof ValueSpecial)
            res = ValueSpecial.handleAddMinus(res, OP_MINUS, tmp);
          else if (res.isInt() && tmp.isInt())
            res = ConstInt.getInt(res.getLongValue() - tmp.getLongValue());
          else
            res = ConstDouble.getDouble(res.getDoubleValue() - tmp.getDoubleValue());
        } else {
          return ValueSpecial.NaN;
        }
      }
    }
    return res;
  }

  public void dump(XMLWriter out) {
    out.simpleTag("ExprAddMinus");
    expr.dump(out);
    for (int i=0; i<parts.length; i++) {
      out.print((ops[i] == OP_PLUS) ? " + " : " - ");
      parts[i].dump(out);
    }
    out.endTag();
  }

} // end of class ExprAddMinus.
