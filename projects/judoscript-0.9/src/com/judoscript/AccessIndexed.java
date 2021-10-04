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


//
// For arrays and structs
//

public class AccessIndexed extends ExprLValueBase
{
  Expr host;
  Expr idx;

  public AccessIndexed(Expr host, Expr idx) { this.host = host; this.idx = idx; }

  public String getName() { return "indexed access"; }
  public boolean isValue() { return false; }

  public final Variable eval() throws Throwable {
    try {
    	 Variable v = host.eval();
      return ((ExprCollective)v).resolve(idx.eval());
    } catch(ClassCastException cce) {
      ExceptionRuntime.rte(RTERR_ILLEGAL_INDEXED_ACCESS, null, cce);
    }
    return null;
  }

  public Variable setVariable(Variable val, int type) throws Throwable {
    try {
      ExprCollective var = (ExprCollective)host.eval();
      Variable key = idx.eval();
      var.setVariable(key,val,type);
      return var.resolve(key);
    } catch(ClassCastException cce) {
      ExceptionRuntime.rte(RTERR_ILLEGAL_INDEXED_ACCESS,null,cce);
    }
    return null;
  }

  public void dump(XMLWriter out) {
    out.simpleTag("AccessIndexed");
    host.dump(out);
    out.print("[");
    idx.dump(out);
    out.print("]");
    out.endTag();
  }

} // end of class AccessIndexed.
