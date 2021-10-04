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

import com.judoscript.util.Lib;
import com.judoscript.*;


class XmlEndTag extends ObjectInstance
{
  String tagName;     // (composite of uri and local)
  String uri;
  String local;
  String raw;

  XmlEndTag(boolean doNamespace, String uri, String local, String raw) {
    super();
    this.uri = uri;
    this.local = local;
    this.raw = "/" + raw;
    tagName = "/" + (doNamespace ? Lib.formTag(uri,local) : raw);
  }

  public String getTypeName() { return "XMLEndTag"; }
  public String getStringValue() throws Throwable {
    StringBuffer sb = new StringBuffer("<");
    sb.append(tagName);
    sb.append(">");
    return sb.toString();
  }

  public final String toString() {
    try { return getStringValue(); }
    catch(Throwable e) {
      return "";   
    }
  }

  /////////////////////////////////////////////////////////
  // Variable Setting/Getting
  //

  public void removeVariable(String name) {}
  public void clearVariables() {}
  public void close() {}

  public Variable invoke(String fxn, Expr[] params, int[] javaTypes) throws Throwable {
    int ord = getMethodOrdinal(fxn);
    if (ord == 0)
      return super.invoke(fxn,params,javaTypes);
    return invoke(ord,params);
  }

  public Variable invoke(int id, Expr[] params) throws Exception {
    switch (id) {
    case BIM_GETNAME:      return JudoUtil.toVariable(tagName);
    case BIM_GETURI:       return JudoUtil.toVariable(uri);
    case BIM_GETLOCAL:     return JudoUtil.toVariable(local);
    case BIM_GETRAW:       return JudoUtil.toVariable(raw);
    case BIM_ISENDTAG:     return ConstInt.getBool(getTypeName().equals("XMLEndTag"));
    case BIM_HASATTRS:     return ConstInt.FALSE;
    case BIM_COUNTATTRS:   return ConstInt.ZERO;
    //case BIM_GETATTRNAME:  return ValueSpecial.UNDEFINED;
    //case BIM_GETATTRVALUE: return ValueSpecial.UNDEFINED;
    }
    return ValueSpecial.UNDEFINED;
  }

} // end of class XmlEndTag.

