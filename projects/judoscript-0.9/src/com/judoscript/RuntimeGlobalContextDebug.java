/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 03-17-2002  JH   Initial open source release.
 * 07-20-2004  JH   Use text I/O only.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript;

import java.io.*;
import java.util.Stack;
import com.judoscript.util.LinePrintWriter;

public final class RuntimeGlobalContextDebug extends RuntimeGlobalContext
{
  Debugger debugger;
  Stack stmtStack;

  public RuntimeGlobalContextDebug(Debugger debugger, String[] progArgs, Script script) {
    this(debugger, progArgs, script, null, null, null, null);
  }

//  public RuntimeGlobalContextDebug(Debugger debugger,
//             String[] progArgs, Script script, OutputStream os,
//             OutputStream es, PrintWriter logw, InputStream is)
  public RuntimeGlobalContextDebug(Debugger debugger,
             String[] progArgs, Script script, LinePrintWriter os,
             LinePrintWriter es, LinePrintWriter logw, BufferedReader is)
  {
    super(progArgs, script, os, es, logw, is);
    this.debugger = debugger;
    stmtStack = new Stack();
  }

  public RuntimeSubContext newSubContext() { return new RuntimeSubContextDebug(debugger,this); }

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

  public final void finish() { debugger.finish(); }

} // end of class RuntimeGlobalContextDebug.
