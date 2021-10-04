/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 03-17-2002  JH   Initial open source release.
 * 06-23-2002  JH   Added hasHandler(name).
 * 08-29-2002  JH   Changed event() to return boolean indicating processed
 *                  or not, to facilitate cascaded processing.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript;

import java.util.*;
import com.judoscript.util.XMLWriter;


public abstract class EventDriven implements Stmt
{
  /////////////////////////////////////
  //
  public static class EventBlock extends BlockSimple
  {
    Variable thisVal;

    public EventBlock(Variable thisVal, Stmt[] stmts) throws Throwable {
      super(stmts,null);
      this.thisVal = thisVal;
      this.exec();
    }

    protected int beginBlock() throws Throwable {
      RT.setLocalVariable(THIS_NAME,thisVal,thisVal.getJavaPrimitiveType());
      return 0;
    }
  }

  /////////////////////////////////////
  // EventDriven starts here
  //

  int lineNum;
  int fileIndex;
  protected Stmt[] init;
  protected Stmt[] finish;
  HashMap eventHandlers = new HashMap();   // name => StmtListStmt

  protected EventDriven(int lineNo) {
    lineNum = lineNo;
    init = null;
  }

  public abstract void start() throws Throwable;

  public void pushNewFrame() throws Throwable {
    try { RT.pushFrame(new FrameBlock((String)null), init); } catch(ExceptionControl ce) {}
  }
  public final void popFrame() { RT.popFrame(); }

  public void exec() throws Throwable {
    pushNewFrame();
    try {
      start();
    } finally {
      try {
        if (finish != null)
          RT.execStmts(finish);
      } finally {
        RT.popFrame(); // this; also clears variables therein.
      }
    }
  }

  public final void setInit(Stmt[] sa) { init = sa; }
  public final void setFinish(Stmt[] sa) { finish = sa; }

  public final void addHandler(int lineNo, String name, Stmt[] stmts) {
    eventHandlers.put(name,new StmtListStmt(stmts));
  }
  
  public final boolean hasHandler(String name) {
    return eventHandlers.get(name) != null;
  }

  /**
   * Called by the event engine.
   */
  public boolean event(String name, String name2, Variable cur) throws Throwable {
    StmtListStmt eh = (StmtListStmt)eventHandlers.get(name);
    if (eh != null) {
      //new EventBlock(cur, eh.stmts);
      if (cur != null)
        RT.setLocalVariable(THIS_NAME,cur,cur.getJavaPrimitiveType());
      RT.execStmts(eh.stmts);
      return true;
    } else if (name2 != null) {
      eh = (StmtListStmt)eventHandlers.get(name2);
      if (eh != null) {
        //new EventBlock(cur, eh.stmts);
        if (cur != null)
          RT.setLocalVariable(THIS_NAME,cur,cur.getJavaPrimitiveType());
        RT.execStmts(eh.stmts);
        return true;
      }
    }
    return false;
  }

  public Stmt optimizeStmt() { return this; }

  public void setLineNumber(int lineNum) { this.lineNum = lineNum; }
  public int  getLineNumber() { return lineNum; }

  public void setFileIndex(int findex) { fileIndex = findex; }
  public int getFileIndex() { return fileIndex; }

  public void dump(XMLWriter out) {
    out.simpleTagLn("EventDriven");
    // TODO: dump().
    out.endTagLn();
  }

  void dumpStmt(XMLWriter out, Stmt stmt) {
    stmt.dump(out);
    if (stmt instanceof Expr)
      out.println();
  }

} // end of class EventDriven.
