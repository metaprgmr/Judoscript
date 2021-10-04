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

import com.judoscript.bio.JavaObject;
import com.judoscript.util.AssociateList;
import com.judoscript.util.XMLWriter;


public class ExprCall extends StmtExpr
{
  Object name; // thread names are prepended with THREAD_PREFIX;
               // otherwise threads are exactly the same as functions.
  Expr[] args;
  int[]  javaTypes;
  int threadMode; // 0: not a thread
                  // 1: regular thread
                  // 2: daemon thread
  boolean paramsExpand;

  public ExprCall(Expr obj, Object varname, Expr[] args, int[] javaTypes) {
    this(obj,varname,args,javaTypes,false);
  }

  public ExprCall(Expr obj, Object varname, Expr[] args, int[] javaTypes, boolean expand) {
    super(0,obj);
    name = varname;
    this.args = args;
    this.javaTypes = javaTypes;
    threadMode = 0;
    paramsExpand = expand;
  }

  public ExprCall(Expr obj, Object varname, AssociateList namedParmas) {
    super(0,obj);
    throw new IllegalAccessError("NOT IMPLEMENTED YET!!!"); // TODO
  }

  // For starting threads
  public ExprCall(boolean daemon, Object varname, Expr[] args) {
    super(0,null);
    name = varname;
    this.args = args;
    this.javaTypes = null;
    threadMode = daemon ? 2 : 1;
  }

  public int getType() { return TYPE_UNKNOWN; }

  public Variable eval() throws Throwable {
    if (threadMode != 0) {
      RT.getScript().startThread(name.toString(), (threadMode==2), args);
      return ValueSpecial.UNDEFINED;
    }
    String fxnName = null;
    boolean fxnNameOnly = false;
    if (name instanceof Expr) {
      Variable v = ((Expr)name).eval();
      if (v instanceof AccessFunction) {
        fxnName = ((AccessFunction)v).getName();
        fxnNameOnly = true;
      } else {
        fxnName = v.getStringValue();
      }
    } else {
      fxnName = name.toString();
    }

    Expr[] params = args;
    if (paramsExpand)
      params = RT.calcValues(args,true);

    if (expr != null) // Object method invocation
      return expr.eval().invoke(fxnName, params, javaTypes);
    else
      return RT.call(fxnName, params, javaTypes, fxnNameOnly);
  }

  public String getStringValue() throws Throwable { return eval().getStringValue(); }

  // Type, NIL-ness, ... are meaningless because this expression is used only
  // by temp vars -- temp vars are the ones to be checked!

  public void dump(XMLWriter out) {
    out.simpleTag("ExprCall");
    if (expr != null) expr.dump(out);
    out.openTag("method");
    out.tagAttr("name", name.toString());
    out.closeSingleTag();
    if (args != null) {
      for (int i=0; i<args.length; i++) {
        if (i>0) out.print(", ");
        args[i].dump(out);
      }
    }
    out.endTag();
  }

  public Variable javaInvoke(JavaObject jo) throws Throwable {
    return jo.invoke(name.toString(),args,javaTypes);
  }
}
