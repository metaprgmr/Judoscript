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


package com.judoscript.parser.helper;

import com.judoscript.*;
import com.judoscript.bio.JavaObject;
import com.judoscript.util.XMLWriter;
import com.judoscript.util.XMLDumpable;

public final class Arguments implements XMLDumpable
{
  int[] javaTypes;
  Expr[] params;
  boolean expand;

  public Arguments(ParserHelper helper, Object[] oa, boolean expand) {
    this.expand = expand;
    int len = (oa==null) ? 0 : oa.length;
    if (len==0) { javaTypes = null; params = null; }
    params = new Expr[len/2];
    javaTypes = new int[len/2];
    boolean hasJavaType = false;
    for (int i=0; i<params.length; i++) {
      params[i] = ParserHelper.reduce(oa[i*2]);
      javaTypes[i] = ((Integer)oa[i*2+1]).intValue();
      if (javaTypes[i] != JavaObject.JAVA_ANY) hasJavaType = true;
    }
    if (!hasJavaType) {
      javaTypes = null;
      expand = false;  // never expand for Java calls.
    }
  }

  public Expr[] getParams() { return params; }
  public int[] getTypes()   { return javaTypes; }
  public boolean doExpand() { return expand; }

  public void dump(XMLWriter out) { StmtBase.dumpArguments(out,params); }

} // end of class Arguments.

