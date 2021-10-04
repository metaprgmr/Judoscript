/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 03-17-2002  JH   Initial open source release.
 * 08-10-2002  JH   Change to accept variable names without leading $.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript;

import com.judoscript.util.XMLWriter;


public class AccessVar extends ExprLValueBase
{
  public static final int ACCESS_DEFAULT = 0;
  public static final int ACCESS_GLOBAL  = 1;
  public static final int ACCESS_VAR_ENV = 2;

  String name;
  int    access;

  public AccessVar(String varname) {
    access = varname.startsWith("::") ? ACCESS_GLOBAL : ACCESS_DEFAULT;
    name = (access==ACCESS_GLOBAL) ? varname.substring(2).trim() : varname;
  }

  public AccessVar(String varname, int acc) {
    access = acc;
    name = varname;
  }

  public final String getName() { return name; }
  public final int getType() {
    try { return eval().getType(); }
    catch(Throwable e) { return TYPE_UNKNOWN; }
  }
  public final boolean isNil() {
    try { return eval().isNil(); }
    catch(Throwable e) { return false; }
  }

  public Variable eval() throws Throwable {
    Variable ret;

    if (access==ACCESS_GLOBAL) {
      ret = RT.resolveGlobalVariable(name);
      if (!ret.isNil())
        return ret;
      try {
      	ret = JudoUtil.toVariable(RT.getSystemProperties().get(name));
      } catch(Exception e) {
      	e.printStackTrace();
      	ret = ValueSpecial.UNDEFINED;
      }
    } else {
      int idx = name.indexOf("::");
      if (idx > 0) {
        ret = RT.getNamespace(name.substring(0, idx)).resolveVariable(name.substring(idx+2));
      } else {
        ret = RT.resolveVariable(name); // in the current scope
        if (ret != ValueSpecial.UNDEFINED)
          return ret;

        try {
          Variable udo = RT.getThisObject();
          if (udo != null)
            ret = udo.resolveVariable(name);
        } catch(Exception e) {}

        if (ret == ValueSpecial.UNDEFINED && access == ACCESS_VAR_ENV) {
          ret = RT.resolveGlobalVariable(name);
          if (ret.isNil())
            ret = JudoUtil.toVariable(RT.getSystemProperties().get(name));
//          String s = Lib.getEnvVar(name);
//          if (s!=null) return JudoUtil.toVariable(s);
        }
      }
    }

    if (ret == ValueSpecial.UNDEFINED) {
      switch(RT.getUndefinedAccessPolicy()) {
      case ISSUE_LEVEL_WARN:  RT.logger.warn("Variable " + name + " is not initialized."); break;
      case ISSUE_LEVEL_ERROR: ExceptionRuntime.rte(RTERR_ILLEGAL_ACCESS, "Variable " + name + " is not initialized.");
      }
    }
    return ret;
  }

  public Variable setVariable(Variable val, int type) throws Throwable {
    if (access==ACCESS_GLOBAL)
      return RT.getRootFrame().setVariable(name,val,type);
    else
      return RT.setVariable(name,val,type);
  }

  public void dump(XMLWriter out) {
    out.openTag("AccessVar");
    switch(access) {
    case ACCESS_GLOBAL:     out.tagAttr("global","true");  break;
    case ACCESS_VAR_ENV:    out.tagAttr("env/var","true"); break;
    }
    out.tagAttr("name", name);
    out.closeSingleTag();
  }

} // end of class AccessVar.
