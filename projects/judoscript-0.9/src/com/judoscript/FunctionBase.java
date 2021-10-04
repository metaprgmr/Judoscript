/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 08-18-2002  JH   Inception.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript;

import java.util.Map;
import com.judoscript.util.XMLWriter;

public abstract class FunctionBase implements Function
{
  String name;
  int beginLine;

  public void   setName(String name) { this.name = name; }
  public String getName() { return name; }
  public int    getLineNumber() { return beginLine; }
  public void   setLineNumber(int num) { beginLine = num; }
  public int    getFileIndex() { return 0; } // TODO

  public Map getAnnotation() { return null; }

  public void   dump(XMLWriter out) {}
}
