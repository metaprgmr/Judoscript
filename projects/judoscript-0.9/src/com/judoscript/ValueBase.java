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

public abstract class ValueBase extends VariableAdapter
{
  public String concat(String v) { return ""; }
  public abstract void setStringValue(String v);
  public abstract void setBoolValue(boolean b);
  public abstract void setLongValue(long v);
  public abstract void setDoubleValue(double v);

} // end of class ValueBase.
