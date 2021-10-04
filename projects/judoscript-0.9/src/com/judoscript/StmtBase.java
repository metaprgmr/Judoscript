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


public abstract class StmtBase implements Stmt
{
  int lineNum;
  int fileIndex;

  public final void setLineNumber(int lineNum) { this.lineNum = lineNum; }
  public final int getLineNumber() { return lineNum; }

  public final void setFileIndex(int findex) { fileIndex = findex; }
  public final int getFileIndex() { return fileIndex; }

  public StmtBase(int lineno) { lineNum = lineno; }

  public Stmt optimizeStmt() { return this; }

  public void pushNewFrame() throws Exception {
    ExceptionRuntime.rte(RTERR_INTERNAL_ERROR,"This statement should not create new frame!");
  }
  public void popFrame() {}

  public static void dumpArguments(XMLWriter out, Expr[] args) {
    if (args == null) {
      out.simpleSingleTag("Arguments");
      return;
    }
    out.simpleTag("Arguments");
    for (int i=0; i<args.length; i++) {
      if (i>0) out.print(", ");
      args[i].dump(out);
    }
    out.endTag();
    out.flush();
  }
}
