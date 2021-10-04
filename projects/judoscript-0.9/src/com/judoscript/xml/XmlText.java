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

import org.xml.sax.*;


class XmlText extends XmlTag
{
  StringBuffer buf;
  int depth;

  XmlText(int depth, boolean doNamespace, String uri, String local, String raw, Attributes attrs) {
    super(doNamespace,uri,local,raw,attrs,true);
    this.depth = depth;
    buf = new StringBuffer(20);
  }

  public final int getDepth() { return depth; }
  public final void append(String s) { buf.append(s); }
  public final void append(char[] ch, int start, int length) { buf.append(ch,start,length); }

  public final String getTypeName() { return "XMLText"; }
  public final String getStringValue() throws Throwable { return buf.toString(); }

} // end of class XmlText.

