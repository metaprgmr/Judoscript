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


package com.judoscript.xml;

import java.util.HashMap;

public class XmlProperty
{
  public boolean namespace = false;
  public boolean validate  = false;
  public boolean schema    = false;
  public boolean ignoreWhitespace = false;
  public boolean ignoreComment    = false;
  public HashMap xmlns = new HashMap();
  public void addXmlNS(String ns, String value) { xmlns.put(ns,value); }
}
