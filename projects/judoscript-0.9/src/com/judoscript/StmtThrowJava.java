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

import com.judoscript.bio.*;
import com.judoscript.util.XMLWriter;


public class StmtThrowJava extends StmtBase
{
  Expr clsName;
  Expr[] params;
  int[] javaTypes;

  public StmtThrowJava(int line, Expr clsName, Expr[] params, int[] javatypes) {
    super(line);
    this.clsName = clsName;
    this.params = params;
    this.javaTypes = javatypes;
  }

  public void exec() throws Throwable {
    JavaObject jo;
    if (clsName == null) {
      try {
        jo = (JavaObject)RT.resolveVariable("$_");
        if (jo == null)
          return; // invalid throw -- just ignore
      } catch(Exception e) {
        return;   // invalid throw -- just ignore
      }
    } else {
      String x = clsName.getStringValue();
      jo = new JavaObject(RT.getClass(x), RT.calcValues(params), javaTypes);
      if (!(jo.getObjectValue() instanceof Exception))
        ExceptionRuntime.rte(RTERR_ILLEGAL_ARGUMENTS,"Class '"+x+"' is not an Exception");
    }
    throw (Exception)jo.getObjectValue();
  }

  public void dump(XMLWriter out) {
    out.simpleTag("StmtThrowJava");
    clsName.dump(out);
    out.endTagLn();
  }

} // end of class StmtThrowJava.
