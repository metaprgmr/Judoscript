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


public class ExprAndOrXor extends ExprMultiBase1
{
  // op: OP_AND/OR/XOR

  public ExprAndOrXor(int op, Expr first, Expr[] rest) { super(op,first,rest); }

  public int getType() { return TYPE_INT; }
  public int getJavaPrimitiveType() { return JAVA_LONG; }

  public Variable eval() throws Throwable {
    try { return ConstInt.getInt(getLongValue()); }
    catch(ExceptionSpecialValue esv) { return esv.getResult(); }
  }

  public long getLongValue() throws Throwable {
    long ret;
    ret = expr.getLongValue();
    switch(op) {
    case OP_AND:
      for (int i=parts.length-1; i>=0; i--) ret &= parts[i].getLongValue();
      return ret;
    case OP_OR:
      for (int i=parts.length-1; i>=0; i--) ret |= parts[i].getLongValue();
      return ret;
    case OP_XOR:
      for (int i=parts.length-1; i>=0; i--) ret ^= parts[i].getLongValue();
      return ret;
    default: return 0; // shouldn't happen.
    }
  }

  public double getDoubleValue() throws Throwable { return (double)getLongValue(); }

  public void dump(XMLWriter out) {
    out.simpleTag("ExprAndOrXor");
    expr.dump(out);
    String oper;
    if (op == OP_AND) oper = " &amp; ";
    else if (op == OP_OR) oper = " | ";
    else oper = " ^ ";
    for (int i=0; i<parts.length; i++) {
      out.print(oper);
      parts[i].dump(out);
    }
    out.closeTag();
  }

} // end of class ExprAndOrXor.
