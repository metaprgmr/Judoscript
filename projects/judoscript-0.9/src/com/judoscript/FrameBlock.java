/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 03-17-2002  JH   Initial open source release.
 * 06-16-2003  JH   Changed from ContextBlock.java.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript;

import java.util.*;
import com.judoscript.util.XMLWriter;


public class FrameBlock extends ObjectInstance implements Frame, Variable
{
  Map     vars;
  HashSet locals;
  String  label;

  public FrameBlock(String label) {
    locals = null;
    this.label = label;
    vars = new HashMap();
  }

  public FrameBlock(String label, Map init) throws Exception {
    locals = null;
    this.label = label;
    vars = null;
    if (init != null) {
      vars = (Map)init.getClass().newInstance();
      Iterator names = init.keySet().iterator();
      while (names.hasNext()) {
        String n = names.next().toString();
        Object v = init.get(n);
        try { vars.put(n, JudoUtil.toVariable(v)); } catch(Exception e) {}
      }
    }
  }

  public FrameBlock(Map map) throws Exception { this(null, map); }

  public final String getLabel() { return label; }
  public final Map getStorage() { return vars; }

  public boolean hasVariable(String name) {
    if (vars == null) return false;
    return null != vars.get(name);
  }

  public Variable resolveVariable(String name) throws Throwable {
    if (vars == null) return ValueSpecial.UNDEFINED;
    Variable v = (Variable)vars.get(name);
    if (v == null) return ValueSpecial.UNDEFINED;
    return v;
  }

  public Variable setVariable(String name, Variable val, int type) {
    if (vars == null)
      vars = new HashMap();
    if ((name!=null) && (val!=null)) {
      val = val.cloneValue();
      val.setJavaPrimitiveType(type);
      vars.put(name,val);
    }
    return val;
  }

  public void removeVariable(String name) {
    try {
      Variable var = (Variable)vars.get(name);
      vars.remove(name);
    } catch(NullPointerException npe) {}
  }

  public void setLocal(String name) {
    if (locals == null) locals = new HashSet();
    locals.add(name);
  }

  public boolean isLocal(String name) {
    if (locals == null) return false;
    return locals.contains(name);
  }

  public void clearVariables() {
    if (vars == null) return;
    Iterator values = vars.values().iterator();
    while (values.hasNext()) {
      Variable var = (Variable)values.next();
    }
    vars.clear();
  }

  public void close() { clearVariables(); }

  public boolean isTerminal() { return false; }
  public boolean isFunction() { return false; }

  // Variable interface impl

  public boolean isInternal() { return true; }
  public String  getTypeName() { return "Context"; } // really for FunctionUser.FunctionFrame
  public int getType() { return TYPE_CONTEXT; } // really for FunctionUser.FunctionFrame

  public void dump(XMLWriter out) {}

} // end of class FrameBlock.
