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


public class ExprComplement extends ExprSingleBase
{
  public ExprComplement(Expr e) { super(e); }

  public Variable eval() throws Throwable {
    try { return ConstInt.getInt(~expr.getLongValue()); }
    catch(ExceptionSpecialValue esv) { return esv.getResult(); }
  }

  public int getType() { return TYPE_INT; }

  public long   getLongValue() throws Throwable   { return ~expr.getLongValue(); }
  public double getDoubleValue() throws Throwable { return (double)getLongValue(); }

  public void dump(XMLWriter out) {
    out.simpleTag("ExprComplement");
    out.print(" ~ ");
    expr.dump(out);
    out.endTag();
  }

} // end of class ExprComplement.
