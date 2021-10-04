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

import java.util.Stack;


public abstract class ExprMultiBase extends ExprSingleBase
{
  protected Expr[] parts;
  protected int[]  ops;

  public ExprMultiBase(Expr first, int[] ops, Expr[] rest) {
    super(first);
    this.ops = ops;
    parts = rest;
  }

  public Expr reduce(Stack stack) {
    expr = expr.reduce(stack);
    for (int i=0; i<parts.length; i++)
      parts[i] = parts[i].reduce(stack);
    return this;
  }
}
