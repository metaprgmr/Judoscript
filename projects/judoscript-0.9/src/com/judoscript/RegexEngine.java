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

public interface RegexEngine
{
  public Variable compile(Expr[] args) throws Throwable;
  public Variable matcher(String s, Expr[] params) throws Throwable;
  public Variable matches(String s, Expr[] params) throws Throwable;
  public Variable matchesStart(String s, Expr[] params) throws Throwable;
  public Variable replaceAll(String s, Expr[] params) throws Throwable;
  public Variable replaceFirst(String s, Expr[] params) throws Throwable;
  public Variable split(String s, Expr[] params) throws Throwable;
  public Variable splitWithMatches(String s, Expr[] params, boolean needNonMatches) throws Throwable;
}
