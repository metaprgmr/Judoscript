/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 03-17-2002  JH   Initial open source release.
 * 06-22-2002  JH   Added endTag and its support.
 * 06-23-2002  JH   Added isClosed, removeAttr(name) and hasAttr(name).
 * 06-25-2002  JH   Fixed in setAttr() for if(attrs==null).
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript.util;

import java.util.*;
import org.apache.commons.lang.StringUtils;

/**
 * This class represents a SGML tag or text; text can be a CDATA content.
 *
 * The tag name is also used for text content if it's plain text.
 * Tags, including tags, may have attributes. Attribute names are
 * always strings, but values can be anything. E.g., in JudoScript,
 * attribute values and plain texts can have embedded ${..}'s,
 * which are converted to ExprConcat or AccessVar operators.
 */
public class Markup
{
  public static final int TYPE_TEXT    = 1;
  public static final int TYPE_TAG     = 2;
  public static final int TYPE_SPECIAL = 3;

  int type = TYPE_TAG;
  boolean attr_casesens;
  Object name = null;
  String specialTagName = null;
  String endTag = ">";
  ArrayList attrs = null;
  int row = 1;
  int column = 1;
  boolean isClosed = false;
  boolean isSelfClosed = false;
  ArrayList children = null;

  public Markup(boolean casesens) {
    this(casesens, -1, -1);
  }

  public Markup(boolean casesens, int row, int col) {
    attr_casesens = casesens;
    this.row = row;
    this.column = col;
  }

  public Markup getEndTag() {
    if (isText())
      return null;
    Markup ret = new Markup(attr_casesens);
    ret.type = this.type;
    ret.name = "/" + this.name;
    ret.specialTagName = this.specialTagName;
    ret.endTag = this.endTag;
    ret.row = this.row;
    ret.column = this.column;
    ret.isClosed = true;
    ret.isSelfClosed = false;
    return ret;
  }

  public String getTypeName() {
    switch(type) {
    case TYPE_TEXT:    return "TEXT";
    case TYPE_TAG:     return "TAG";
    case TYPE_SPECIAL: return "SPECIAL";
    }
    return "???";
  }

  public String toString() {
    if (type == TYPE_TEXT)
      return getText();
    if (type == TYPE_SPECIAL)
      return specialTagName + (name==null?"":name.toString()) + endTag;
    StringBuffer sb = new StringBuffer();
    if (!name.toString().startsWith("<"))
      sb.append('<');
    sb.append(name);
    int len = (attrs == null) ? 0 : attrs.size();
    for (int i=0; i<len; i+=2) {
      sb.append(' ');
      sb.append(attrs.get(i).toString());
      String value = attrs.get(i+1).toString();
      if ((value == null) || (value.length() == 0))
        continue;
      sb.append("=\"");
      sb.append(value);
      sb.append("\"");
    }
    sb.append(endTag);
    return sb.toString();
  }

  public String toStringInternal() {
    StringBuffer sb = new StringBuffer();
    sb.append("type = ");
    switch(type) {
    case TYPE_TEXT:    sb.append("TYPE_TEXT"); break;
    case TYPE_TAG:     sb.append("TYPE_TAG"); break;
    case TYPE_SPECIAL: sb.append("TYPE_SPECIAL"); break;
    default:           sb.append("?? " + type); break;
    }
    sb.append("\nname = "); sb.append(name);
    sb.append("\nspecialTagName = "); sb.append(specialTagName);
    sb.append("\nendTag= "); sb.append(endTag);
    sb.append("\nAttributes:");
    int len = numAttrs();
    for (int i=0; i<len; ++i)
      sb.append("\n  " + getAttrName(i) + " = " + getAttrValue(i));
    return sb.toString();
  }

  public void setRow(int r) { row = r; }
  public void setColumn(int c) { column = c; }
  public int getRow() { return row; }
  public int getColumn() { return column; }
  public boolean isText() { return (type==TYPE_TEXT); }
  public boolean isTag() { return (type==TYPE_TAG); }
  public boolean isSpecial() { return (type==TYPE_SPECIAL); }
  public boolean isComment() {
    if (type != TYPE_SPECIAL)
      return false;
    if (!"<!".equals(specialTagName))
      return false;
    if (!name.toString().startsWith("--"))
      return false;
    if (!name.toString().endsWith("--"))
      return false;
    return true;
  }
  public boolean isA(String name) {
    if (!isTag())
      return false;
    try { return attr_casesens ? name.equals(this.name) : name.equalsIgnoreCase(this.name.toString()); }
    catch(Exception e) { return false; }
  }
  public boolean isClosed() { return isClosed; }
  public boolean isClosedTag() { return isClosed && isTag(); }
  public boolean isSelfClosed() { return isSelfClosed; }
  public void setClosed() { isClosed = true; }
  public void setSelfClosed() { isClosed = true; isSelfClosed = true; }
  public String getName() { return (type==TYPE_SPECIAL) ? specialTagName : name.toString(); }
  public String getText() { return name.toString(); }
  public boolean hasAttr(String name) { return getAttr(name) != null; }
  public String getAttrName(int idx) {
      try { return (String)attrs.get(idx * 2); }
      catch(Exception e) { return null; }
  }
  public String getAttrValue(int idx) {
    try { return attrs.get(idx * 2 + 1).toString(); }
    catch(Exception e) { return null; }
  }
  public void setAttrValue(int idx, Object val) {
    try { attrs.set(idx * 2 + 1, val); }
    catch(Exception e) {}
  }
  public String getAttr(String name) {
    try {
      int len = numAttrs();
      for (int i=len-1; i>=0; i--) {
        if (attr_casesens) {
          if (!name.equals(getAttrName(i)))
            continue;
        } else {
          if (!name.equalsIgnoreCase(getAttrName(i)))
            continue;
        }
        return getAttrValue(i);
      }
    } catch(Exception e) {}
    return null;
  }
  public int  numAttrs() { return (attrs==null) ? 0 : attrs.size()/2; }
  
  public Map getAllAttrs() {
  	HashMap ret = new HashMap();
  	for (int i=numAttrs()-1; i>=0; i--)
  		ret.put(getAttrName(i), getAttrValue(i));
  	return ret;
  }
  public void setIsTag() { type = TYPE_TAG; }
  public void setIsText() { type = TYPE_TEXT; setSelfClosed(); }
  public void setIsSpecial(String tagName, String endtag) {
    if (attrs!=null)
      attrs = null;
    type = TYPE_SPECIAL;
    specialTagName = tagName;
    endTag = StringUtils.defaultString(endtag,">");
  }
  public void setIsSpecial(String tagName) { setIsSpecial(tagName,null); }
  public void setName(String name) { setName(name, null); }
  public void setName(String name, String endtag) {
    this.name = name;
    if (name.startsWith("/"))
      setClosed();
    endTag = StringUtils.defaultString(endtag,">");
  }
  public void setText(Object text) { name = text; }
  public void addAttr(String name, Object value) {
    if (hasAttr(name))
      setAttr(name, value);
    if (attrs == null)
      attrs = new ArrayList();
    attrs.add(name);
    attrs.add(value);
  }
  public void setAttr(String name, Object value) {
    if (attrs == null)
      attrs = new ArrayList();
    for (int i=0; i<attrs.size(); i+=2) {
      if (name.equals(attrs.get(i))) {
        attrs.set(i+1, value);
        return;
      }
    }
    addAttr(name, value);
  }
  public void removeAttr(String name) {
    if (attrs == null)
      return;
    for (int i=0; i<attrs.size(); i+=2) {
      if (name.equals(attrs.get(i))) {
        attrs.remove(i);
        attrs.remove(i);
        return;
      }
    }
  }

  public void addChild(Markup child) {
    if (children == null)
      children = new ArrayList();
    children.add(child);
  }

  public int numChildren() { return children == null ? 0 : children.size(); }

  public Markup getChild(int i) {
    try {
      return (Markup)children.get(i);
    } catch(Exception e) {
      return null;
    }
  }

  public void printTree() { _printTree(""); }

  private void _printTree(String indent) {
    System.out.print(indent);
    if (isTag())
      System.out.println(toString());
    else
      System.out.println("TEXT: \"" + name + "\"");

    indent += "  ";
    for (int i=0; i<numChildren(); ++i) {
      getChild(i)._printTree(indent);
    }
  }

  /**
   *@return a List of name, value, name, value ... pairs.
   */
  public ArrayList getNameValues() { return attrs; }

} // end of class Markup.
