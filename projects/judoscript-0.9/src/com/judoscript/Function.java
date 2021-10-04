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

import java.util.Map;
import com.judoscript.util.XMLWriter;

public interface Function extends Consts
{
  public int getLineNumber();
  public int getFileIndex();
  public void setLineNumber(int lineNum);
  public String getName();
  public void   setName(String name);
  public Variable invoke(Expr[] args, int[] javaTypes) throws Throwable;
  public void dump(XMLWriter out);

  // Returns (String => Variable)*
  public Map getAnnotation();
}
