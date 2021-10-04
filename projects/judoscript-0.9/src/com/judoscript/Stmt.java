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

import java.io.Serializable;
import com.judoscript.util.XMLDumpable;
import com.judoscript.util.XMLWriter;


public interface Stmt extends Consts, XMLDumpable, Serializable
{
  public int getLineNumber();
  public int getFileIndex();
  public void setLineNumber(int lineNum);
  public void exec() throws Throwable;
  public void pushNewFrame() throws Throwable;
  public void popFrame();
  public Stmt optimizeStmt();

  public static final Stmt NoOp = new Stmt() {
    public int getLineNumber() { return 0; }
    public int getFileIndex() { return 0; }
    public void setLineNumber(int lineNum) {}
    public void exec() {}
    public void pushNewFrame() {}
    public void popFrame() {}
    public Stmt optimizeStmt() { return this; }
    public void dump(XMLWriter out) {}
  };
}
