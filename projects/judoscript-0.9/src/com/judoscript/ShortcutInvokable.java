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


public interface ShortcutInvokable
{
  public Variable invoke(int fxnOrd, String fxn, Expr[] params, int[] javaTypes) throws Throwable;
}
