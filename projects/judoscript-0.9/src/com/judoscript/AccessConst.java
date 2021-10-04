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


public class AccessConst extends AccessVar
{
  public AccessConst(String varname) { super(varname); }

  public final Variable eval() throws ExceptionRuntime {
    return RT.getScript().resolveConst(name);
  }

  // ???
  public final Variable setVariable(Variable val, int type) throws Exception {
    RT.setConst(name,val);
    return val;
  }

  public void dump(XMLWriter out) {
    out.openTag("AccessConst");
    out.tagAttr("name", name);
    out.closeSingleTag();
  }
}
