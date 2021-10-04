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
import org.xml.sax.helpers.AttributesImpl;
import com.judoscript.*;


class XmlTag extends XmlEndTag
{
  Attributes attrs;

  XmlTag(boolean doNS, String uri, String local, String raw, Attributes attrs) {
    this(doNS,uri,local,raw,attrs,false);
  }

  XmlTag(boolean doNS, String uri, String local, String raw, Attributes attrs, boolean toClone)
  {
    super(doNS,uri,local,raw);
    this.attrs = !toClone ? attrs : new AttributesImpl(attrs);
    this.tagName = this.tagName.substring(1); // remove "/"
    this.raw = this.raw.substring(1);         // remove "/"
  }

  public String getTypeName() { return "XMLTag"; }
  public String getStringValue() throws Throwable {
    StringBuffer sb = new StringBuffer("<");
    sb.append(tagName);
    int len = attrs.getLength();
    for (int i=0; i<len; i++) {
      sb.append(' ');
      sb.append(attrs.getQName(i)); // getQName() or getLocal()??? TODO
      sb.append('=');
      String x = attrs.getValue(i);
      char quot = (x.indexOf('"') >= 0) ? '\'' : '"';
      sb.append(quot);
      sb.append(x);
      sb.append(quot);
    }
    sb.append('>');
    return sb.toString();
  }

  /////////////////////////////////////////////////////////
  // Variable Setting/Getting
  //

  public final Variable resolveVariable(String name) throws ExceptionRuntime, Throwable {
    return JudoUtil.toVariable( attrs.getValue(name) );
  }

  /////////////////////////////////////////////////////////
  // Method Ordinals
  //

  public Variable invoke(String fxn, Expr[] params, int[] javaTypes) throws Throwable
  {
    switch(getMethodOrdinal(fxn)) {
    case BIM_HASATTRS:   return ConstInt.getBool( (attrs != null) && (attrs.getLength() > 0) );
    case BIM_COUNTATTRS: return ConstInt.getInt( (attrs==null) ? 0 : attrs.getLength() );
    case BIM_GETATTRNAME:
      try { return JudoUtil.toVariable( attrs.getQName((int)params[0].getLongValue()) ); }
      catch(Exception e) {} // TODO: or getLocalName()???
    case BIM_GETATTRVALUE:
      try { return JudoUtil.toVariable( attrs.getValue((int)params[0].getLongValue()) ); }
      catch(Exception e) {}
    default:
      return super.invoke(fxn,params,javaTypes);
    }
  }

} // end of class XmlTag.

