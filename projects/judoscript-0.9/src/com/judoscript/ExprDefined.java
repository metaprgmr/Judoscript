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


public class ExprDefined extends ExprAnyBase
{
  Expr operand;

  public ExprDefined(Expr operand) { this.operand = operand; }

  public boolean getBoolValue() throws Throwable {
    return operand.eval() != ValueSpecial.UNDEFINED;
  }

  public Variable eval() throws Throwable { return getBoolValue() ? ConstInt.TRUE : ConstInt.FALSE; }

  public int getType() { return TYPE_INT; }
  public int getJavaPrimitiveType() { return JAVA_BOOLEAN; }

  public void dump(XMLWriter out) {
    out.openTag("ExpreDefined");
    //TODO out.tagAttr("constant_name", constName);
    out.endTag();
  }

} // end of class ExprDefined.


// http://www.sjwar.org -- Reunion play.
