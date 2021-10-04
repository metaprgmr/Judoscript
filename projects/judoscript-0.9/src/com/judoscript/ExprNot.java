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


public final class ExprNot extends ExprSingleBase
{
  public ExprNot(Expr e) { super(e); }

  public int getType() { return TYPE_INT; }
  public int getJavaPrimitiveType() { return JAVA_BOOLEAN; }

  public Variable eval() throws Throwable { return ConstInt.getBool( !expr.getBoolValue() ); }
  public boolean getBoolValue() throws Throwable  { return !expr.getBoolValue(); }
  public long getLongValue() throws Throwable     { return getBoolValue() ? 1 : 0; }
  public double getDoubleValue() throws Throwable { return getBoolValue() ? 1 : 0; }
  public String getStringValue() throws Throwable { return getBoolValue() ? "1" : "0"; }

  public void dump(XMLWriter out) {
    out.simpleTag("ExprNot");
    out.print(" ! ");
    expr.dump(out);
    out.endTag();
  }

} // end of class ExprNot.
