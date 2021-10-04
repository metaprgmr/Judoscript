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
// For Array and Map
//

public class AccessMD extends ExprLValueBase
{
  Expr host;
  Expr[] dims;

  public AccessMD(Expr host, Expr[] dims) { this.host = host; this.dims = dims; }

  public String getName() { return "multi-dimensional access"; }
  public boolean isValue() { return false; }

  public final Variable eval() throws Throwable {
    try {
      ExprCollective var = (ExprCollective)host.eval();
      return var.resolve(RT.calcValues(dims));
    } catch(ClassCastException cce) {
      ExceptionRuntime.rte(RTERR_ILLEGAL_INDEXED_ACCESS,null,cce);
    }
    return null;
  }

  public Variable setVariable(Variable val, int type) throws Throwable {
    try {
      ExprCollective var = (ExprCollective)host.eval();
      Variable[] keys = RT.calcValues(dims);
      var.setVariable(keys,val,type);
      return val.cloneValue();
    } catch(ClassCastException cce) {
      ExceptionRuntime.rte(RTERR_ILLEGAL_INDEXED_ACCESS,null,cce);
    }
    return null;
  }

  public void dump(XMLWriter out) {
    out.simpleTag("AccessMD");
    host.dump(out);
    for (int i=0; i<dims.length; i++) {
      out.print("[");
      dims[i].dump(out);
      out.print("]");
    }
    out.endTag();
  }
}
