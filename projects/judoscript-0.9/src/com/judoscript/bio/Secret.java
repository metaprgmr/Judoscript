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


package com.judoscript.bio;

import com.judoscript.*;


public final class Secret extends ObjectInstance
{
  Expr value;
  Expr agent;

  public Secret(Expr val, Expr agent) {
    super();
    this.value = val;
    this.agent = agent;
  }

  public String getTypeName() { return "Secret"; }
  public void close() {}
  public Variable resolveVariable(String name) throws Throwable { return ValueSpecial.UNDEFINED; }
  public Variable setVariable(String name, Variable val) { return ValueSpecial.UNDEFINED; }

  public Variable invoke(RuntimeContext c, String f, Expr[] a, int[] jt) { return ValueSpecial.UNDEFINED; }

  public String getStringValue() throws Throwable {
    if (agent == null)
      return value.getStringValue();
    Variable a = agent.eval();
    if (a instanceof ValueSpecial)
      return value.getStringValue();
    return a.invoke("decrypt", new Expr[]{value}, null).getStringValue();
  }

} // end of class Secret.

