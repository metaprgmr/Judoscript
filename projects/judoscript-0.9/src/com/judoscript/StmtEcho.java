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


public class StmtEcho extends StmtBase
{
  Expr expr;
  boolean action; // true to start; false to stop.

  public StmtEcho(int line, boolean action) {
    super(line);
    expr = null;
    this.action = action;
  }

  public StmtEcho(int line, Expr expr) {
    super(line);
    this.expr = expr;
    action = true;
  }

  public void exec() throws Throwable {
    if (action == false)
      RT.echoOff();
    else {
      String name = (expr==null) ? null : expr.getStringValue();
      RT.echoOn(name);
    }
  }

  public void dump(XMLWriter out) {
    out.openTag("StmtEcho");
    out.tagAttr("echo","" + action);
    out.closeTag();
    if (expr != null)
      expr.dump(out);
    out.endTagLn();
  }
}
