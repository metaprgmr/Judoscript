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

import com.judoscript.util.Lib;


public class ExceptionExit extends Exception
{
  public ExceptionExit(String msg) { super(msg); }

  public void doExit() { System.exit(Lib.parseInt(getMessage(), 0)); }
}
