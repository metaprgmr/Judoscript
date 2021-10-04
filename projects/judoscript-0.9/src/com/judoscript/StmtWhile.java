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


public class StmtWhile extends BlockSimple implements StmtLoop
{
  Expr   expr;
  String label;
  boolean isDo;

  public StmtWhile(int line, String label, Stmt stmt, Expr e, boolean isDo) {
    super(line,stmt);
    expr = e;
    this.label = label;
    this.isDo = isDo;
  }

  public final String getLabel() { return label; }

  protected int beginBlock() throws Throwable {
    if (isDo) return 0;
    return expr.getBoolValue() ? 0 : -1;
  }

  protected boolean endBlock() throws Throwable {
    if (!isDo) return true;
    return expr.getBoolValue();
  }

  public void dump(XMLWriter out) {
    out.simpleTag(isDo ? "StmtDoWhile" : "StmtWhile");
    expr.dump(out);
    out.println();
    super.dump(out);
    out.endTagLn(); }

} // end of class StmtWhile.
