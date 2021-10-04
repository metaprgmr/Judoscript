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


public class AccessMember extends ExprLValueBase
{
  Expr object;
  Object name;

  public AccessMember(Expr obj, Object varname) { object = obj; name = varname; }

  public final String getName() { return toString(); }

  public final Variable eval() throws Throwable {
    Variable host = (object!=null) ? object.eval() : RT.getThisObject();
    if (name instanceof Expr)
      return host.resolveVariable(((Expr)name).eval());
    return host.resolveVariable(name.toString());
  }

  public final Variable setVariable(Variable val, int type) throws Throwable {
    ObjectInstance host = null;
    Variable v = null;
    try {
      v = object != null ? object.eval() : RT.getThisObject();
      host = (ObjectInstance)v;
    } catch(ClassCastException cce) {
      ExceptionRuntime.rte(ExceptionRuntime.RTERR_ILLEGAL_VALUE_SETTING,
        "Unable to set values to " + v.getTypeName());
    }

    if (name instanceof Expr)
      return host.setVariable(((Expr)name).eval(), val, type);
    return host.setVariable(name.toString(), val, type);
  }

  public String toString() { return name.toString(); }

  public void dump(XMLWriter out) {
    out.openTag("AccessMember");
    out.tagAttr("name", name.toString());
    if (object == null)
      out.closeSingleTag();
    else {
      out.closeTag();
      object.dump(out);
      out.endTag();
    }
  }
}
