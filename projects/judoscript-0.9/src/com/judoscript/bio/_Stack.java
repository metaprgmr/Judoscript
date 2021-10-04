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


package com.judoscript.bio;

import java.util.Stack;
import java.util.HashMap;
import com.judoscript.*;


public final class _Stack extends ObjectInstance
{
  Stack data = new Stack();

  public _Stack() { super(); }

  public void close() { data.clear(); }

  public int getType() { return TYPE_STACK; }
  public boolean isStack() { return true; }

  public String getStringValue() throws Throwable {
    StringBuffer sb = new StringBuffer("{");
    for (int i=0; i<data.size(); i++) {
      if (i>0) sb.append(",");
      try { sb.append(((Variable)data.elementAt(i)).getStringValue()); } catch(Exception e) {}
    }
    sb.append("}");
    return sb.toString();
  }

  public Variable resolve(int idx) {
    try {
      Variable var = (Variable)data.elementAt(idx);
      if (var != null) return var;
    } catch(Exception e) {}
    return ValueSpecial.UNDEFINED;
  }

  public String getTypeName() { return "Stack"; }

  /////////////////////////////////////////////////////////
  // Ordinals
  //

  public static final int BIM_SIZE    = 1;
  public static final int BIM_CLEAR   = 2;
  public static final int BIM_PUSH    = 3;
  public static final int BIM_POP     = 4;
  public static final int BIM_PEEKAT  = 5;
  public static final int BIM_PEEK    = 6;
  public static final int BIM_ISEMPTY = 7;
  static final int BIM_count = 7;

  static HashMap bimStack = new HashMap();
  static {
    bimStack.put("size",   new Integer(BIM_SIZE));
    bimStack.put("clear",  new Integer(BIM_CLEAR));
    bimStack.put("push",   new Integer(BIM_PUSH));
    bimStack.put("pop",    new Integer(BIM_POP));
    bimStack.put("peek",   new Integer(BIM_PEEK));
    bimStack.put("peekAt", new Integer(BIM_PEEKAT));
    bimStack.put("isEmpty",new Integer(BIM_ISEMPTY));
  }

  public static String[] listMethods() {
    String[] ret = new String[BIM_count];
    ret[BIM_SIZE-1]   = "size()";
    ret[BIM_CLEAR-1]  = "clear()";
    ret[BIM_PUSH-1]   = "push(variable)";
    ret[BIM_POP-1]    = "pop()";
    ret[BIM_PEEK-1]   = "peek()";
    ret[BIM_PEEKAT-1] = "peekAt( [index] )";
    ret[BIM_ISEMPTY-1]= "isEmpty()";
    return ret;
  }

  public Variable invoke(String fxn, Expr[] params, int[] javaTypes) throws Throwable
  {
    int len;
    Variable v;
    Integer I = (Integer)bimStack.get(fxn);
    if (I!=null) id = I.intValue();
    switch(id) {
    case 0:
    default:
      return ValueSpecial.UNDEFINED;
    case BIM_SIZE:
      return ConstInt.getInt(data.size());
    case BIM_CLEAR:
      checkWritable();
      close();
      break;
    case BIM_PUSH:
      checkWritable();
      len = (params==null) ? 0 : params.length;
      for (int i=0; i<len; i++) {
        v = params[i].eval().cloneValue();
        internal_push(v);
      }
      break;
    case BIM_POP:
      checkWritable();
      return internal_pop();
    case BIM_PEEKAT:
      len = (params==null) ? 0 : params.length;
      if (len > 0) {
        int idx = (int)params[0].getLongValue();
        if ((idx < 0) || (idx >= len))
          return ValueSpecial.UNDEFINED;
        return (Variable)data.elementAt(idx);
      } // otherwise, fall thru ...
    case BIM_PEEK:
      if (data.isEmpty()) break;
      return (Variable)data.peek();
    case BIM_ISEMPTY:
      return ConstInt.getBool(data.isEmpty());
    }
    return ValueSpecial.UNDEFINED;
  }

  public void internal_push(Variable v) { data.push(v); }

  public Variable internal_pop() {
    if (data.isEmpty()) return ValueSpecial.UNDEFINED;
    Variable v = (Variable)data.pop();
    return v;
  }

  public Variable internal_peek() {
    if (data.isEmpty()) return ValueSpecial.UNDEFINED;
    return (Variable)data.peek();
  }

  public boolean internal_isEmpty() { return data.isEmpty(); }

  public int size() { return data.size(); }

} // end of class _Stack.
