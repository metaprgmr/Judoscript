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


package com.judoscript.util;

/**
 *  Represents an assertion failure.
 *
 *@see com.judoscript.util.Lib.assert(boolean, java.lang.String)
 */
public class AssertionException extends Exception
{
  public AssertionException(String msg) { super(msg); }
}
