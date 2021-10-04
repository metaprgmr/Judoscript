/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 08-10-2004  JH   Inception.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript;

public interface Namespace
{
  Variable resolveVariable(String name) throws Throwable;
  Variable invoke(String fxn, Expr[] args, int[] javaTypes) throws Throwable;

  public class Adapter implements Namespace
  {
    String ns;

    public Adapter(String ns) {
      this.ns = ns;
    }

    public Variable resolveVariable(String name) throws Throwable {
      return RT.resolveVariable(ns+"::"+name);
    }

    public Variable invoke(String fxn, Expr[] args, int[] javaTypes) throws Throwable {
      return RT.call(ns+"::"+fxn, args, javaTypes, false, false);
    }
  }

} // end of class Namespace.
