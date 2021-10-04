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


public class StmtExpr extends ExprSingleBase implements Stmt
{
  int lineNum;
  int fileIndex;

  public StmtExpr(int line, Expr prim) { super(prim); lineNum = line; }

  public void setLineNumber(int line) { lineNum = line; }
  public int getLineNumber() { return lineNum; }

  public void setFileIndex(int findex) { fileIndex = findex; }
  public int getFileIndex() { return fileIndex; }

  public Stmt optimizeStmt() { return this; }

  public void pushNewFrame() throws Exception {
    ExceptionRuntime.rte(RTERR_INTERNAL_ERROR,"This statement should not create new frame!");
  }
  public void popFrame() {}

  public Variable eval() throws Throwable { return expr.eval(); }
  public void exec() throws Throwable { eval(); }

  public void dump(XMLWriter out) { expr.dump(out); }
}
