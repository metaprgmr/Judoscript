/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 03-17-2002  JH   Initial open source release.
 * 08-12-2002  JH   Removed the statement-wide curStmt for multi-threadedness.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript;

import java.util.*;
import com.judoscript.util.XMLWriter;


public class BlockSimple implements Stmt
{
  public static final BlockSimple EMPTY = new BlockSimple();

  int lineNum;
  int fileIndex;

  HashMap labels = null;
  Stmt[] stmts;
  int codeLength;


  public BlockSimple() { super(); lineNum = 0; stmts = null; codeLength = 0; }

  public BlockSimple(BlockSimple blk) {
    this(blk.lineNum,blk.stmts,blk.labels);
  }

  public BlockSimple(Stmt stmt) { setStmts(stmt); }

  public BlockSimple(Stmt[] stmts, HashMap labels) { this(-1,stmts,labels); }

  public BlockSimple(int lineNo, Stmt stmt) {
    lineNum = lineNo;
    setStmts(stmt);
  }

  public BlockSimple(int lineNo, Stmt[] stmts, HashMap labels) {
    lineNum = lineNo;
    setStmts(stmts,labels);
  }

  public BlockSimple(int lineNo, BlockSimple block) {
    this(lineNo,null,null);
    this.stmts = (block==null) ? null : block.stmts;
    this.labels = (block==null) ? null : block.labels;
    this.codeLength = (block==null) ? 0 : block.codeLength;
  }

  public void setStmts(Stmt stmt) {
    if (stmt instanceof BlockSimple) {
      BlockSimple blk = (BlockSimple)stmt;
      setStmts(blk.stmts, blk.labels);
    } else {
      codeLength = 1;
      stmts = new Stmt[]{ stmt };
    }
  }

  public final void setStmts(Stmt[] _stmts, HashMap labels) {
    this.labels = labels;
    codeLength = (_stmts == null) ? 0 : _stmts.length;
    stmts = new Stmt[codeLength];
    for (int i=0; i<codeLength; i++) stmts[i] = _stmts[i];
  }

  // returns the number of statements added.
  public int prependStmts(Stmt[] _stmts) {
    int len = _stmts==null ? 0 : _stmts.length;
    if (len > 0) {
      Stmt[] xx = new Stmt[codeLength+len];
      System.arraycopy(_stmts,0,xx,0,len);
      System.arraycopy(stmts,0,xx,len,codeLength);
      codeLength += len;
      stmts = xx;
    }
    return len;
  }

  public Stmt[] getStmts() { return stmts; } 

  // if >=0, exec() keeps looping.
  protected int beginBlock() throws Throwable {
    return 0; // start from beginning.
              // StmtSwitch will set to a specific statement.
  }

  // if true, exec() keeps looping.
  protected boolean endBlock() throws Throwable {
    return false; // default only one iteration.
  }

  public void pushNewFrame() throws Throwable {
    String label = null;
    if (this instanceof StmtLoop)
      label = ((StmtLoop)this).getLabel();
    RT.pushFrame(new FrameBlock(label),(Stmt[])null);
  }
  public void popFrame() { RT.popFrame(); }

  public void exec() throws Throwable {
    pushNewFrame();
    try {
      execHere();
    } finally {
      try { handleFinally(); } finally { popFrame(); }
    }
  }

  // For eval() and evalFile() -- see EVAL... in Sys.java.
  public void exec(Expr[] args) throws Throwable {
    pushNewFrame();
    try {
      RT.setLocalVariable(CONTEXT_NAME, (FrameBlock)RT.peekFrame(), 0);
      RT.setLocalVariable(ARGS_NAME, JudoUtil.toArray(RT.calcValues(args)), 0);
      execHere();
    } finally {
      try { handleFinally(); } finally { popFrame(); }
    }
  }

  protected void handleException(Exception e) throws Throwable { throw e; }
  protected void handleFinally() throws Throwable {}

  public void execHere() throws Throwable {
    RuntimeContext rtc = RT.curCtxt();
    boolean isLoop = this instanceof StmtLoop;
    if (isLoop)
      rtc.pushLoopIndex();
    try {
loop: while (true) {
        int curStmt = beginBlock();
        if (curStmt < 0)
          break;
        if (isLoop)
          rtc.incLoopIndex();
inloop: for (int i=curStmt; i<codeLength; i++) {
          try {
            try {
              rtc.execStmt(stmts[i]);
            } catch(Exception e) {
              if (e instanceof ExceptionControl)
                throw e;
              handleException(e);
              break loop;
            }
          } catch(ExceptionControl ce) {
            if (ce.isReturn() && (this instanceof Function || this instanceof _Thread || this instanceof Script)) {
           		throw ce;
            } else if (this instanceof StmtLoop) {
              String label = ce.getLabel();
              boolean match = (label == null);
              if (!match)
                match = label.equals(rtc.currentFrame().getLabel());
              if (match) {
                if (ce.isBreak())
                  break loop;
                if (ce.isContinue())
                  break inloop;
              }
            } else if ((this instanceof StmtBreakable) && ce.isBreak()) { // for switch(), XML, etc.
              break loop;
            } else if (ce.isResume()) {
              continue;
            }

            throw ce;
          } // catch(ExceptionControl).
        } // for(;;)

        if (!endBlock())
        	break;
      } // while()
    } finally {
      if (isLoop)
      	rtc.popLoopIndex();
    }
  }

  public int labelToIndex(String name) {
    try { return ((Integer)labels.get(name)).intValue(); }
    catch(Exception e) { return -1; }
  }

  public Stmt optimizeStmt() {
    //int len = (stmts==null) ? 0 : stmts.length;
    //return (len == 1) ? stmts[0] : this;
    return this; // TODO: enhancement, say, for(;;), if/elif/else, ...
  }

  public void setLineNumber(int lineNum) { this.lineNum = lineNum; }
  public int getLineNumber() { return lineNum; }

  public void setFileIndex(int findex) { fileIndex = findex; }
  public int getFileIndex() { return fileIndex; }

  public void dump(XMLWriter out) {
    int len = (stmts==null) ? 0 : stmts.length;
    int i;

    out.simpleTagLn("BlockSimple");
    if (this instanceof StmtSwitch) { // special treatment for
      dumpAll(out);                   // switch() statement.
    } else {
      if (len > 0) {
        if (labels == null) {
          for (i=0; i<len; i++) dumpStmt(out,stmts[i]);
        } else {
          Iterator keys = labels.keySet().iterator();
          String label;
          if (labels.size() > 1)
            dumpAll(out);
          else {
            label = (String)keys.next();
            int idx = ((Integer)labels.get(label)).intValue();
            for (i=0; i<len; i++) {
              if (i==idx) dumpLabel(out,label);
              dumpStmt(out,stmts[i]);
            }
          }
        }
      }
    }
    out.endTagLn();
    out.flush();
  }

  void dumpAll(XMLWriter out) {
    int i;

    int len = (stmts==null) ? 0 : stmts.length;
    Object[] oa = new Object[len];
    Iterator keys = labels.keySet().iterator();
    while (keys.hasNext()) {
      Object label = keys.next();
      addToArray(oa, ((Integer)labels.get(label)).intValue(), label);
    }

    for (i=0; i<len; i++) {
      if (oa[i] != null) dumpLabel(out,oa[i]);
      dumpStmt(out,stmts[i]);
    }
  }

  void dumpLabel(XMLWriter out, Object label) {
    if (label instanceof Object[]) {
      Object[] oa = (Object[])label;
      for (int i=0; i<oa.length; i++) dumpLabel(out,oa[i]);
    } else if (label instanceof StmtSwitch.Case) {
      ((StmtSwitch.Case)label).dump(out);
    } else {
      String s = label.toString();
      out.openTag("Label");
      out.tagAttr("name", label.toString());
      out.closeSingleTagLn();
    }
  }

  void dumpStmt(XMLWriter out, Stmt stmt) {
    stmt.dump(out);
    if (stmt instanceof Expr) out.println();
  }

  void addToArray(Object[] oa, int idx, Object o) {
    if (oa[idx] == null) oa[idx] = o;
    else if (oa[idx] instanceof Object[]) {
      Object[] oa1 = (Object[])oa[idx];
      int len = oa1.length;
      Object[] oa2 = new Object[len];
      System.arraycopy(oa1,0,oa2,0,len);
      oa2[len] = o;
      oa[idx] = oa2;
    }
  }

} // end of class BlockSimple.
