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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Enumeration;
import com.judoscript.*;


public class _Queue extends ObjectInstance
{
  LinkedList data = new LinkedList ();

  public _Queue() { super(); }

  public void close() { data.clear(); }

  public final int getType() { return TYPE_QUEUE; }
  public final boolean isQueue() { return true; }

  public String getStringValue() throws Throwable {
    StringBuffer sb = new StringBuffer("{");
    ListIterator iter = data.listIterator(0);
    boolean start = true;
    while (iter.hasNext()) {
      if (start) start = false;
      else sb.append(",");
      try { sb.append(((Variable)iter.next()).getStringValue()); } catch(Exception e) {}
    }
    sb.append("}");
    return sb.toString();
  }

  public final Variable resolve(int idx) {
    try {
      Variable var = (Variable)data.get(idx);
      if (var != null) return var;
    } catch(Exception e) {}
    return ValueSpecial.UNDEFINED;
  }

  public final String getTypeName() { return "Queue"; }

  /////////////////////////////////////////////////////////
  // Ordinals
  //

  public static final int BIM_SIZE  = 1;
  public static final int BIM_CLEAR = 2;
  public static final int BIM_ENQ   = 3;
  public static final int BIM_DEQ   = 4;
  public static final int BIM_HEAD  = 5;
  public static final int BIM_TAIL  = 6;
  public static final int BIM_ISEMPTY = 7;
  static final int BIM_count = 7;

  static final HashMap bimQueue = new HashMap();
  static {
    bimQueue.put("size",  new Integer(BIM_SIZE));
    bimQueue.put("clear", new Integer(BIM_CLEAR));
    Integer I = new Integer(BIM_ENQ);
    bimQueue.put("enq",   I);
    bimQueue.put("enque", I);
    I = new Integer(BIM_DEQ);
    bimQueue.put("deq",   I);
    bimQueue.put("deque", I);
    bimQueue.put("head",  new Integer(BIM_HEAD));
    bimQueue.put("tail",  new Integer(BIM_TAIL));
    bimQueue.put("isEmpty",new Integer(BIM_ISEMPTY));
  }

  public static String[] listMethods() {
    String[] ret = new String[BIM_count];
    ret[BIM_SIZE-1]  = "size()";
    ret[BIM_CLEAR-1] = "clear()";
    ret[BIM_ENQ-1]   = "enq(variable)";
    ret[BIM_DEQ-1]   = "deq()";
    ret[BIM_HEAD-1]  = "head()";
    ret[BIM_TAIL-1]  = "tail()";
    ret[BIM_ISEMPTY-1]= "isEmpty()";
    return ret;
  }

  public Variable invoke(String fxn, Expr[] params, int[] javaTypes) throws Throwable
  {
    Variable v;
    Integer I = (Integer)bimQueue.get(fxn);
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
    case BIM_ENQ:
      checkWritable();
      int len = (params==null) ? 0 : params.length;
      if (len == 0) break;
      v = params[0].eval().cloneValue();
      data.addLast(v);
      break;
    case BIM_DEQ:
      checkWritable();
      if (data.size() == 0) break;
      v = (Variable)data.getFirst();
      data.removeFirst();
      return v;
    case BIM_HEAD:
      if (data.size() == 0) break;
      return (Variable)data.getFirst();
    case BIM_TAIL:
      if (data.size() == 0) break;
      return (Variable)data.getLast();
    case BIM_ISEMPTY:
      return ConstInt.getBool(data.size() == 0);
    }
    return ValueSpecial.UNDEFINED;
  }

} // end of class _Queue.
