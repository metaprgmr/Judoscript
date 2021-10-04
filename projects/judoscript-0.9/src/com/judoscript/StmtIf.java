/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 03-17-2002  JH   Initial open source release.
 * 08-22-2002  JH   Added setElsePart() method.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript;

import com.judoscript.util.XMLWriter;


public class StmtIf extends StmtBase
{
  Expr[] conds;
  Stmt[] ifParts;
  Stmt   elsePart;

  public StmtIf(int line, Expr[] conds, Stmt[] ifParts, Stmt elsePart) {
    super(line);
    this.conds = conds;
    this.ifParts = ifParts;
    this.elsePart = elsePart;
  }

  public void setElsePart(Stmt elsePart) {
    this.elsePart = elsePart;
  }

  public void exec() throws Throwable {
    int len = conds.length;
    for (int i=0; i<len; i++) {
      if (conds[i].getBoolValue()) {
        RT.execStmt(ifParts[i]);
        return;
      }
    }
    if (elsePart != null)
      RT.execStmt(elsePart);
  }

  public void dump(XMLWriter out) {
    out.simpleTagLn("StmtIf");
    for (int i=0; i<conds.length; i++) {
      conds[i].dump(out);
      out.println();
      ifParts[i].dump(out);
    }
    if (elsePart != null) {
      out.simpleSingleTagLn("Else");
      elsePart.dump(out);
    }
    out.endTagLn();
  }
}
