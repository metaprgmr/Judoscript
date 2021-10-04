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

import com.judoscript.util.IntHashtable;


public abstract class Debugger
{
  RuntimeGlobalContextDebug rgcd;

  protected IntHashtable BPs = new IntHashtable();
  boolean doSS = false;
  boolean doStepInto = false;
  int callDepth = 0;

  public void init(String[] args, Script script) {
    callDepth = -1;
    rgcd = new RuntimeGlobalContextDebug(this, args, script);
  }
  public void start() { rgcd.getScript().start(rgcd); }
  public void setBP(int lineNum) { BPs.put(lineNum, Boolean.TRUE); }
  public void removeBP(int lineNum) { BPs.remove(lineNum); }
  public void clearAllBPs() { BPs.clear(); }
  public void setSingleStep(boolean set) { doSS = set; callDepth = -1; }
  public void setStepInto(boolean set) { doStepInto = set; }
  public Variable watch(String name) throws Throwable { return rgcd.resolveVariable(name); }

  public void execStmt(Stmt stmt) throws Throwable {
    if (isAtBP(stmt)) {
      breakPoint(stmt);
    } else if (doSS) {
      if (doStepInto) {
        breakPoint(stmt);
      } else {
        int depth = ((RuntimeGlobalContextDebug)RT.curCtxt()).stmtStack.size();
        if (callDepth < 0 || (depth==callDepth)) {
          breakPoint(stmt);
          callDepth = depth;
        }
      }
    }
    stmt.exec();
  }

  protected abstract void breakPoint(Stmt stmt);
  public abstract void finish();

  final boolean isAtBP(Stmt stmt) { return BPs.containsKey(stmt.getLineNumber()); }

} // end of class Debugger.

