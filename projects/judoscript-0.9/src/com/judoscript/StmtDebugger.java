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


public abstract class StmtDebugger implements Stmt
{
/***
  public int getLineNumber() { return 0; }
  public int getFileIndex() { return 0; }
  public void setLineNumber(int lineNum) {}
  public void pushNewFrame() {}
  public Stmt optimizeStmt() { return this; }
  public void dump(XMLWriter out) {}

  ///////////////////////////////////////////////////////
  // static instances and inner classes
  //

  public static final StmtDebugger BP = new StmtDebugger() {
    public void exec() { RT.getGlobalContext().debuggerSetBP(); }
  };

  public static final StmtDebugger CLEAR_DISPLAY = new StmtDebugger() {
    public void exec() { RT.getGlobalContext().debuggerClearDisplay(); }
  };

  public static final class Watch extends StmtDebugger
  {
    String name;
    boolean toAdd;
    public Watch(boolean toAdd, String varname) { name = varname; this.toAdd = toAdd; }
    public void exec() { RT.getGlobalContext().debuggerAddWatch(toAdd,name); }
  }

  public static final class Status extends StmtDebugger
  {
    Expr msg;
    public Status(Expr msg) { this.msg = msg; }
    public void exec() {
      try { RT.getGlobalContext().debuggerPrintStatus(msg.getStringValue()); }
      catch(Exception e) {}
    }
  }

  public static final class Show extends StmtDebugger
  {
    int type; // c.f. RuntimeContext.PRINT_OUT/ERR/LOG
    boolean enabled;
    public Show(int target, boolean set) {
      type = target;
      enabled = set;
    }
    public void exec() { RT.getGlobalContext().debuggerSetShow(type, enabled); }
  }

  public static final class IgnoreUserStreams extends StmtDebugger
  {
    boolean enabled;
    public IgnoreUserStreams(boolean set) { enabled = set; }
    public void exec() { RT.getGlobalContext().debuggerIgnoreUserStreams(enabled); }
  }

***/

} // end of class StmtDebugger.
