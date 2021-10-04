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

import java.util.Stack;


public final class RuntimeSubContextDebug extends RuntimeSubContext
{
  Debugger debugger;
  Stack stmtStack;

  public RuntimeSubContextDebug(Debugger debugger, RuntimeGlobalContext rgc) {
    super(rgc);
    this.debugger = debugger;
    stmtStack = new Stack();
  }

  public final void execStmt(Stmt stmt) throws Throwable {
    try {
      stmtStack.push(stmt);
      stmtLineStack.push(stmt.getLineNumber());
      debugger.execStmt(stmt);
    } finally {
      stmtStack.pop();
      stmtLineStack.pop();
    }
  }

} // end of class RuntimeSubContextDebug.
