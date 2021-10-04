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


package com.judoscript.parser.helper;

import java.util.Stack;
import com.judoscript.*;
import com.judoscript.util.XMLWriter;


public final class ExprPostInc extends StmtExpr
{
  int op;

  public ExprPostInc(int line, Expr prim, int op) {
    super(line, prim);
    this.op = op;
  }

  public Variable eval() throws Throwable { return null; } // no use

  public Expr reduce(Stack stack) {
    ExprAssign ea = new ExprAssign(-1,(ExprPrimary)expr,op,ConstInt.ONE);
    stack.push(expr.reduce(stack));
    stack.push(ea.reduce(stack));
    return new ExprReduced.TempVar(stack.size()-2);
  }

  public void dump(XMLWriter out) { // always reduced; never dumped?
    out.simpleTag("ExprPostInc");
    expr.dump(out);
    out.print((op==OP_PLUS_ASSIGN) ? " ++ " : " -- ");
    out.endTag();
  }

} // end of inner class ExprPostInc.

