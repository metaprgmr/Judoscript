/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 12-13-2004  JH   Inception.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript;

import com.judoscript.util.XMLWriter;


public class AccessRange extends ExprLValueBase
{
  Expr host;
  Expr low;
  Expr hi;

  public AccessRange(Expr host, Expr low, Expr hi) {
    this.host = host;
    this.low = low;
    this.hi = hi;
  }

  public String getName() { return "range access"; }
  public boolean isValue() { return false; }

  public final Variable eval() throws Throwable {
    try {
      return ((ExprCollective)(host.eval())).resolveRange(low.eval(), hi.eval());
    } catch(ClassCastException cce) {
      ExceptionRuntime.rte(RTERR_ILLEGAL_INDEXED_ACCESS, null, cce);
    }
    return null;
  }

  public Variable setVariable(Variable val, int type) throws Throwable {
    ExceptionRuntime.rte(RTERR_ILLEGAL_INDEXED_ACCESS, "Can't not assign values to a range");
    return null;
  }

  public void dump(XMLWriter out) {
    out.simpleTag("AccessRange");
    host.dump(out);
    out.print("[");
    low.dump(out);
    out.print(':');
    hi.dump(out);
    out.print("]");
    out.endTag();
  }

} // end of class AccessIndexed.
