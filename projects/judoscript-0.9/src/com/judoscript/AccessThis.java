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


public class AccessThis extends ExprLValueBase
{
  public AccessThis() {}

  public final String getName() { return "this"; }
  public final int getType() { return TYPE_OBJECT; }
  public final boolean isNil() { return false; }

  public final Variable eval() { return RT.getThisObject(); }

  public final Variable setVariable(Variable val, int type) throws ExceptionRuntime {
    ExceptionRuntime.rte(RTERR_ILLEGAL_VALUE_SETTING,
                    "Can't set value '"+val.toString()+"' to a user-defined object");
    return null;
  }

  public void dump(XMLWriter out) { out.simpleSingleTag("AccessThis"); }

} // end of class AccessThis.


