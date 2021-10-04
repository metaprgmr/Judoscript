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

import com.judoscript.bio._Timer;
import com.judoscript.util.XMLWriter;


public class StmtSchedule extends StmtBase
{
  // For _Timer to access
  public boolean isAbsolute = false;
  public boolean isStart = false;
  public Expr    start_delay = null;
  public Expr    period = null;
  public Expr    port = null;
  public Expr    title = null;
  public BlockSimple block = null;
  public BlockSimple listener = null;

  public StmtSchedule(int line) { super(line); }

  public void exec() throws Throwable { new _Timer(this).start(); }

  public void dump(XMLWriter out) {
    int i, len;

    out.simpleTag("StmtSchedule");
    out.tagAttr("absolute", isAbsolute);
    out.tagAttr("start", isStart ? "at" : "after");

    out.simpleTag("start_delay");
    start_delay.dump(out);
    out.endTag();

    out.simpleTag("period");
    start_delay.dump(out);
    out.endTag();

    out.endTag();
  }

} // end of class StmtSchedule.
