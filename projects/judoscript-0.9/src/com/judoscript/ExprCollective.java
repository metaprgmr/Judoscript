/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 03-17-2002  JH   Initial open source release.
 * 12-13-2004  JH   Added resolveRange() method.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript;

import java.util.Iterator;

public interface ExprCollective extends Variable
{
  public Variable resolve(Variable idx) throws Throwable;
  public Variable resolve(Variable[] dims) throws Throwable;
  public Variable resolveRange(Variable low, Variable hi) throws Throwable;
  public Variable addVariable(Variable val, int type) throws Throwable;
  public Variable setVariable(Variable idx, Variable val, int type) throws Throwable;
  public Variable setVariable(Variable[] dims, Variable val, int type) throws Throwable;
  public Iterator getIterator(int start, int to, int step, boolean upto, boolean backward) throws Throwable;
  public int size();
}
