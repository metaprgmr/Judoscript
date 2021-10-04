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


public class StmtFor extends BlockSimple implements StmtLoop
{
  Expr[] forInit;
  Expr   forCond;
  Expr[] forNext;
  String label;

  public StmtFor(int line, String label, Expr[] forInit, Expr forCond, Expr[] forNext, BlockSimple b) {
    super(line,b);
    this.forInit = forInit;
    this.forCond = forCond;
    this.forNext = forNext;
    this.label = label;
  }

  public final String getLabel() { return label; }

  public void pushNewFrame() throws Throwable {
    try { RT.pushFrame(new FrameBlock(label),(Stmt[])null); } catch(ExceptionControl ce) {}
    int len = (forInit==null) ? 0 : forInit.length;
    for (int i=0; i<len; i++)
      try { forInit[i].eval(); } catch(ExceptionControl ce) {}
  }

  protected int beginBlock() throws Throwable {
    return (forCond==null) ? 0 : ( forCond.getBoolValue() ? 0 : -1 );
  }

  protected boolean endBlock() throws Throwable {
    int len = (forNext==null) ? 0 : forNext.length;
    for (int i=0; i<len; i++)
      try { forNext[i].eval(); } catch(ExceptionControl ce) {}
    return true;
  }

  public void dump(XMLWriter out) {
    int i, len;

    out.simpleTagLn("StmtFor");

    out.simpleTag("forInit");
    len = (forInit==null) ? 0 : forInit.length;
    for (i=0; i<len; i++)
      forInit[i].dump(out);
    out.endTagLn();

    out.simpleTag("forCond");
    if (forCond != null) forCond.dump(out);
    out.endTagLn();

    out.simpleTag("forNext");
    len = (forNext==null) ? 0 : forNext.length;
    for (i=0; i<len; i++)
      forNext[i].dump(out);
    out.endTagLn();

    super.dump(out);
    out.endTagLn();
  }

} // end of class StmtFor.
