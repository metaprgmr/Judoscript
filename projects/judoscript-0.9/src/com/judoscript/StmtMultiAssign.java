/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 03-26-2004  JH   Inception.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript;

import java.util.*;
import com.judoscript.bio.*;
import com.judoscript.parser.helper.*;
import com.judoscript.util.XMLWriter;


public class StmtMultiAssign extends StmtBase
{
  boolean isLocal;
  ArrayList names = new ArrayList();
  Expr rhs;

  public StmtMultiAssign(int line, boolean isLocal) {
    super(line);
    this.isLocal = isLocal;
  }

  public void addName(String name) { names.add(name); }
  public void setValue(Expr val) { rhs = val; }

  public final void exec() throws Throwable {
    int idx;

    if (isLocal) {
      for (idx=0; idx<names.size(); ++idx)
        RT.setLocalVariable((String)names.get(idx));
    }

    Variable var = rhs.eval();
    if (var instanceof ExprCollective) {
      ExprCollective coll = (ExprCollective)var;
      for (idx = Math.min(coll.size(), names.size())-1; idx>=0; idx--)
        RT.setVariable((String)names.get(idx), coll.resolve(ConstInt.getInt(idx)), 0);
    } else if (names.size() > 0) {
      RT.setVariable((String)names.get(0), var, 0);
    }
  }

  public void dump(XMLWriter out) {
    out.openTag("StmtMultiAssign");
    out.endTag();
  }

} // end of class StmtMultiAssign.
