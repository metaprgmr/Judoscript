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


public class ExprConditionalAndOr extends ExprMultiBase1
{
  // op: OP_AND/OR

  public ExprConditionalAndOr(int op, Expr first, Expr[] rest) { super(op,first,rest); }

  public Variable eval() throws Throwable { return ConstInt.getInt(getLongValue()); }

  public long getLongValue() throws Throwable {
    boolean ret;
    if (op == OP_OR) {
      if (expr.getBoolValue()) return 1;
      for (int i=parts.length-1; i>=0; i--) {
        if (parts[i].getBoolValue()) return 1;
      }
      return 0;
    } else { // OP_AND
      if (!expr.getBoolValue()) return 0;
      for (int i=parts.length-1; i>=0; i--) {
        if (!parts[i].getBoolValue()) return 0;
      }
      return 1;
    }
  }

  public int getType() { return TYPE_INT; }
  public int getJavaPrimitiveType() { return JAVA_BOOLEAN; }

  public double getDoubleValue() throws Throwable { return (double)getLongValue(); }
  public String getStringValue() throws Throwable { return String.valueOf(getLongValue()); }

  public void dump(XMLWriter out) {
    out.simpleTag("ExprConditionalAndOr");
    expr.dump(out);
    String oper = (op==OP_AND) ? " &amp;&amp; " : " || ";
    for (int i=0; i<parts.length; i++) {
      out.print(oper);
      parts[i].dump(out);
    }
    out.endTag();
  }

} // end of class ExprConditionalAndOr.
