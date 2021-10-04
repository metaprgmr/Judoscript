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


public class StmtReturn extends StmtBase
{
  Expr expr;

  public StmtReturn(int line, Expr expr) { super(line); this.expr = expr; }

  public void exec() throws Throwable {
  	Variable ret = (expr==null) ? ValueSpecial.UNDEFINED : expr.eval();
    ExceptionControl.throwReturnException(ret);
  }

  public void dump(XMLWriter out) {
    if (expr != null) {
      out.simpleTag("StmtReturn");
      expr.dump(out);
      out.endTagLn();
    } else {
      out.simpleSingleTagLn("StmtReturn");
    }
  }
}
