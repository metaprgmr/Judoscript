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


public class ExprMulDivMod extends ExprAddMinus
{
  // ops: OP_MUL/DIV/MOD
  boolean isInteger = false;
  boolean isDouble = false;

  public ExprMulDivMod(Expr first, int[] ops, Expr[] rest) {
    super(first,ops,rest);

    // for %, it's int always.
    for (int i=0; i<ops.length; i++) {
      if (ops[i] == OP_MOD) {
        isInteger = true;
        isDouble = false;
        return;
      } else if (ops[i] == OP_DIV) {
        isInteger = false;
        isDouble = true;
        return;
      }
    }
  }

  public int getType() {
    if (isInteger) return TYPE_INT;
    if (isDouble) return TYPE_DOUBLE;
    return super.getType();
  }

  public Variable eval() throws Throwable {
    try {
      if ( isInt() ) return ConstInt.getInt(getLongValue());
      return ConstDouble.getDouble(getDoubleValue());
    } catch(ExceptionSpecialValue esv) {
      Variable r = esv.getResult();
      return !r.isNil() ? r : ConstInt.ZERO;
    }
  }

  public long getLongValue() throws Throwable {
    if ( !isInt() ) return (long)getDoubleValue();

    try {
      Variable[] va = getOperands();
      long ret = va[0].getLongValue();
      for (int i=1; i<va.length; i++) {
        long x = va[i].getLongValue();
        switch(ops[i-1]) {
        case OP_MUL: ret *= x; break;
        case OP_DIV: if (x==0)
                       throw ret > 0 ? ExceptionSpecialValue.POSITIVE_INFINITY
                                     : ExceptionSpecialValue.NEGATIVE_INFINITY;
                     ret /= x;
                     break;
        case OP_MOD: if (x==0)
                       throw ret > 0 ? ExceptionSpecialValue.POSITIVE_INFINITY
                                     : ExceptionSpecialValue.NEGATIVE_INFINITY;
                     ret %= x;
                     break;
        default: ret = 0;
        }
      }
      return ret;
    } catch(ExceptionSpecialValue esv) {
      if (esv.getResult().isNil()) return 0;
      throw esv;
    }
  }

  public double getDoubleValue() throws Throwable {
    if ( isInt() ) return (double)getLongValue();

    try {
      Variable[] va = getOperands();
      double ret = va[0].getDoubleValue();
      for (int i=1; i<va.length; i++) {
        double x = va[i].getDoubleValue();
        switch(ops[i-1]) {
        case OP_MUL: ret *= x; break;
        case OP_DIV: if (x==0)
                       throw ret > 0 ? ExceptionSpecialValue.POSITIVE_INFINITY
                                     : ExceptionSpecialValue.NEGATIVE_INFINITY;
                     ret /= x;
                     break;
        default: ret = 0;
        }
      }
      return ret;
    } catch(ExceptionSpecialValue esv) {
      if (esv.getResult().isNil()) return 0.0;
      throw esv;
    }
  }

  /**
   * If there are special values, <em>appropriate</em> ExceptionSpecialValue is thrown.
   * For result of 0, ExceptionSpecialValue.UNDEFINED is thrown.
   */
  Variable[] getOperands() throws Throwable {
    boolean hasSpecial = false;
    Variable[] va = new Variable[1+parts.length];
    Variable x = expr.eval();
    if (x instanceof ValueSpecial) {
      switch(((ValueSpecial)x).getID()) {
      case ValueSpecial.ID_NIL:               x = ConstInt.ZERO; break;
      case ValueSpecial.ID_UNDEFINED:         x = ConstInt.ZERO; break;
      case ValueSpecial.ID_NaN:               throw ExceptionSpecialValue.NaN;
      case ValueSpecial.ID_POSITIVE_INFINITY:
      case ValueSpecial.ID_NEGATIVE_INFINITY:
      case ValueSpecial.ID_MAX_NUMBER:
      case ValueSpecial.ID_MIN_NUMBER:        hasSpecial = true; break;
      }
    }
    va[0] = x;
    for (int i=0; i<parts.length; i++) {
      x = parts[i].eval();
      if (x instanceof ValueSpecial) {
        switch(((ValueSpecial)x).getID()) {
        case ValueSpecial.ID_NIL:               x = ConstInt.ZERO; break;
        case ValueSpecial.ID_UNDEFINED:         x = ConstInt.ZERO; break;
        case ValueSpecial.ID_NaN:               throw ExceptionSpecialValue.NaN;
        case ValueSpecial.ID_POSITIVE_INFINITY:
        case ValueSpecial.ID_NEGATIVE_INFINITY:
        case ValueSpecial.ID_MAX_NUMBER:
        case ValueSpecial.ID_MIN_NUMBER:        hasSpecial = true; break;
        }
      }
      va[1+i] = x;
    }

    if (hasSpecial)
      ValueSpecial.handleMulDivMod(va, ops);

    return va;
  }

  public void dump(XMLWriter out) {
    out.simpleTag("ExprMulDivMod");
    expr.dump(out);
    for (int i=0; i<parts.length; i++) {
      switch(ops[i]) {
      case OP_MUL: out.print(" * "); break;
      case OP_DIV: out.print(" / "); break;
      case OP_MOD: out.print(" % "); break;
      }
      parts[i].dump(out);
    }
    out.endTag();
  }

} // end of class ExprMulDivMod.
