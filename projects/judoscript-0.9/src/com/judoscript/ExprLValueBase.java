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


public abstract class ExprLValueBase extends ExprAnyBase implements ExprLValue
{
  public int getType() {
    try { return eval().getType(); }
    catch(Throwable e) { return TYPE_UNKNOWN; }
  }

  public boolean isNil() {
    try { return eval().isNil(); }
    catch(Throwable e) { return false; }
  }

  public Variable selfNumericOp(int op, double val, int type) throws Throwable {
    boolean isInt = (val == (double)(long)val);
    Variable v = eval();
    isInt &= v.isInt();

    if ((v==null) || v.isNil()) {
      switch(op) {
      case OP_PLUS_ASSIGN:
      case OP_MINUS_ASSIGN:
        v = isInt ? (Variable)ConstInt.getInt((long)val) : ConstDouble.getDouble(val);
        setVariable(v,type);
        return v.cloneValue();
      }
      return ValueSpecial.UNDEFINED;
    }

    if (op == OP_MINUS_ASSIGN) val = -val;
    double res = v.getDoubleValue();
    long ires = (long)res;
    switch(op) {
    case OP_PLUS_ASSIGN:
    case OP_MINUS_ASSIGN:   res += val; ires = (long)res; break;
    case OP_MUL_ASSIGN:     res *= val; ires = (long)res; break;
    case OP_DIV_ASSIGN:     res /= val; ires = (long)res; break;
    case OP_MOD_ASSIGN:     ires = ((long)res) % (long)val; break;
    case OP_LSHIFT_ASSIGN:  ires = ((long)res) << (long)val; break;
    case OP_RSHIFT_ASSIGN:  ires = ((long)res) >> (long)val; break;
    case OP_RUSHIFT_ASSIGN: ires = ((long)res) >>> (long)val; break;
    case OP_AND_ASSIGN:     ires = ((long)res) & (long)val; break;
    case OP_OR_ASSIGN:      ires = ((long)res) | (long)val; break;
    case OP_XOR_ASSIGN:     ires = ((long)res) ^ (long)val; break;
    }
    return setVariable(isInt ? (Variable)ConstInt.getInt(ires) : ConstDouble.getDouble(res), 0);
  }

  public Variable selfStringOp(String val, int type) throws Throwable {
    Variable v = eval();
    if (val == null) return v.cloneValue();
    v = JudoUtil.toVariable(v.getStringValue() + val);
    return setVariable(v,type);
  }

} // end of class ExprLValueBase.
