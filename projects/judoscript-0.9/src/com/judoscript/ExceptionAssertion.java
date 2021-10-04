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

public class ExceptionAssertion extends ExceptionRuntime
{
  int lineNo;

  public ExceptionAssertion(String msg, int line) {
    super(RTERR_ASSERTION_FAILURE,msg,null);
    lineNo = line;
  }

  public int getLineNumber() { return lineNo; }

} // end of class ExceptionAssertion.
