/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 07-07-2003  JH   Inception.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript;

import com.judoscript.util.XMLWriter;


public class StmtWith extends Block
{
  Expr host;


  public StmtWith(int lineNo, BlockSimple block, Expr host) {
    super(lineNo,block);
    this.host = host;
  }

  public void pushNewFrame() throws Throwable {
    Frame frame = null;
    try {
      frame = (Frame)host.eval();
    } catch(ClassCastException cce) {
      ExceptionRuntime.rte(RTERR_ILLEGAL_ARGUMENTS, "With() statement takes an Object instance");
    }
    RT.pushFrame(frame,(Stmt[])null);
  }

  public void dump(XMLWriter out) { // TODO
  }

} // end of class StmtWith.
