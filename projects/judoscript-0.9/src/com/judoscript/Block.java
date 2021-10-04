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

import java.util.*;
import org.apache.commons.lang.StringUtils;
import com.judoscript.bio.*;
import com.judoscript.util.XMLWriter;


/**
 * Should be named "BlockTryCatchFinally".
 */
public class Block extends BlockSimple
{
  int catchStart;
  int catchLength;
  int finallyStart;
  int finallyLength;
  String exceptionName;

  public Block() {}

  public Block(int line, Stmt[] stmts, HashMap labels, int codeLen,
               int catchStart,   int errorLen,
               int finallyStart, int finallyLen, String exceptionName)
  {
    setStmts(stmts, labels);
    lineNum = line;
    this.codeLength = codeLen;
    this.catchLength = errorLen;
    this.finallyLength = finallyLen;
    this.catchStart = catchStart;
    this.finallyStart = finallyStart;
    this.exceptionName = exceptionName;
  }

  public Block(int lineNo, Stmt block) {
    super(lineNo, null, null);
    if (block instanceof BlockSimple)
      setStmts((BlockSimple)block);
    else
      setStmts(block);
  }

  public void setStmts(BlockSimple block) {
    setStmts(block.stmts, block.labels);
    if (block instanceof Block) {
      this.codeLength    = block.codeLength;
      this.catchStart    = ((Block)block).catchStart;
      this.catchLength   = ((Block)block).catchLength;
      this.finallyStart  = ((Block)block).finallyStart;
      this.finallyLength = ((Block)block).finallyLength;
    } else {
      this.catchStart    = -1;
      this.catchLength   = 0;
      this.finallyStart  = -1;
      this.finallyLength = 0;
    }
  }

  public int prependStmts(Stmt[] _stmts) {
    int len = super.prependStmts(_stmts);
    if (len > 0) {
      catchStart += len;
      finallyStart += len;
    }
    return len;
  }

  protected void handleException(Exception e) throws Throwable {
    if (catchLength <= 0) throw e; // unhandled error
    int i = catchStart;
    int j = catchStart + catchLength;
    RT.setLocalVariable(StringUtils.defaultString(exceptionName,THIS_NAME), new ExcptError(e), 0);
    while (i<j)
      RT.execStmt(stmts[i++]);
  }

  protected void handleFinally() throws Throwable {
    int i = finallyStart;
    int j = finallyStart + finallyLength;
    while (i<j)
      RT.execStmt(stmts[i++]);
  }

  public void dump(XMLWriter out) {
    int len = (stmts==null) ? 0 : stmts.length;
    int i;

    out.simpleTagLn("Block");
    if (len > 0) {
      if (labels == null) {
        for (i=0; i<len; i++)
          dumpStmt(out,stmts[i]);
      } else {
        Iterator keys = labels.keySet().iterator();
        String label;
        if (labels.size() > 1)
          dumpAll(out);
        else if (keys.hasNext()) {
          label = (String)keys.next();
          int idx = ((Integer)labels.get(label)).intValue();
          for (i=0; i<len; i++) {
            if (i==idx)
              dumpLabel(out,label);
            dumpStmt(out,stmts[i]);
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
      if (oa[i] != null)
        dumpLabel(out,oa[i]);
      dumpStmt(out,stmts[i]);
    }
  }

} // end of class Block.
