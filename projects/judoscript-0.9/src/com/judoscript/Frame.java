/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 03-17-2002  JH   Initial open source release.
 * 06-16-2003  JH   Changed from Context.java.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript;


public interface Frame
{
  public Variable resolveVariable(String name) throws Throwable;
  public Variable setVariable(String name, Variable var, int type) throws Throwable;
  public boolean  hasVariable(String name);
  public void     setLocal(String name);
  public boolean  isLocal(String name);
  public void     removeVariable(String name) throws Throwable;

  public void     clearVariables();
  public String   getLabel();

  public void     close();
  public boolean  isTerminal();
  public boolean  isFunction();
}
