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


public final class StmtListStmt implements Stmt
{
  Stmt[] stmts;

  public StmtListStmt(Stmt[] stmts) { this.stmts = stmts; }
  public StmtListStmt(BlockSimple blk) { this.stmts = blk.stmts; }

  public int getLineNumber() { return 0; }
  public int getFileIndex()  { return 0; }
  public void setLineNumber(int lineNum) {}
  public void exec() throws Throwable { RT.execStmts(stmts); }
  public void pushNewFrame() {}
  public void popFrame() {}
  public Stmt optimizeStmt() { return this; }

  public void dump(XMLWriter out) {
    for (int i=0; i<stmts.length; ++i) {
      stmts[i].dump(out);
      out.println();
    }
  }
}
