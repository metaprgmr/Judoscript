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

import java.util.*;
import com.judoscript.util.XMLWriter;
import com.judoscript.util.XMLDumpable;


public class ObjectType implements XMLDumpable
{
  String name;
  ObjectType parent;
  HashMap fxns = null;

  public ObjectType(String name, ObjectType parent) {
    this.name = name;
    this.parent = parent;
  }

  public String getName() { return name; }
  public ObjectType getParent() { return parent; }

  public void addFunction(Function f) {
    if (fxns == null) fxns = new HashMap();
    fxns.put(f.getName(), f);
  }

  public Function getFunction(String name) { return (Function)fxns.get(name); }

  public void dump(XMLWriter out) {
    out.openTag("ObjectType");
    out.tagAttr("name", name);
    if (parent != null) out.tagAttr("parent", parent.getName());
    out.closeTagLn();
    Script.dumpFunctions(out,fxns);
    out.endTagLn();
  }
}
