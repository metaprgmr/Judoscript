/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 03-17-2002  JH   Initial open source release.
 * 08-10-2002  JH   Support variable names without leading $.
 * 08-10-2002  JH   Support java static member access with ::.
 * 08-10-2002  JH   Fixed indirect function invocation.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript.parser.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import com.judoscript.util.AssociateList;
import com.judoscript.util.XMLWriter;
import com.judoscript.*;


public final class ExprPrimary extends ExprAnyBase implements Stmt
{
  Object[] qualifiers;  // String    -- member accesses
                        // Arguments -- function invocation
                        // Expr      -- array access

  public ExprPrimary(Object[] qualifiers) {
    this.qualifiers = qualifiers;
  }

  public Variable eval() throws Throwable {
    ExceptionRuntime.rte(RTERR_INTERNAL_ERROR, "ExprPrimary must be reduced. Should not happen.");
    return null;
  }

  //
  // To reduce a primary expression down to simple
  // variable access, object member access, array/map
  // element access and function/method call.
  //
  public Expr reduce(Stack stack) {
    String s;
    Expr curExpr = null;
    int len = qualifiers.length;
    Object o = qualifiers[0];
    int i = 1;
    if (o instanceof Expr) {
      curExpr = ((Expr)o).reduce(stack);
    } else {
      s = (String)qualifiers[0];
      // is the next an Arguments?
      if (len>1 && ((qualifiers[1] instanceof Arguments) || (qualifiers[1] instanceof AssociateList))) {
        curExpr = createExprCall(null, s, qualifiers[1]);
        if (len==2)
          return curExpr;
        i = 2;
      } else if (s.startsWith("#")) {
        curExpr = new AccessConst(s);
      } else {
        curExpr = new AccessVar(s);
      }
    }
    if (len==1)
      return curExpr;
    stack.push(curExpr);
    curExpr = new ExprReduced.TempVar(stack.size()-1);
    
    Expr mdHost = null;
    ArrayList dims = new ArrayList();
    for (; i<len; i++) {
      o = qualifiers[i];
      if (o instanceof Expr) {
        if (dims.size() == 0)
          mdHost = curExpr; 
        dims.add(((Expr)o).reduce(stack));
      } else {
        if (o instanceof AccessExpr) {
          Expr x = ((AccessExpr)o).expr;
          // is this a call?
          if ((len>i+1) &&
              ((qualifiers[i+1] instanceof Arguments) || (qualifiers[i+1] instanceof AssociateList)))
          {
            curExpr = createExprCall(curExpr, x, qualifiers[++i]);
            stack.push(curExpr);
          } else {
            stack.push(new AccessMember(curExpr, x));
          }
        } else if (o instanceof Expr[]) { // range operator
          Expr[] range = (Expr[])o;
          stack.push(new AccessRange(curExpr, range[0], range[1]));
        } else {
          if (getAccessOp(stack, mdHost, dims))
            curExpr = new ExprReduced.TempVar(stack.size()-1);
          if (o instanceof String) { // Arguments are handled for a direct method call.
            s = (String)o;
            if (s.equals("->")) // next must be an Arguments; just let go.
              continue;
            // is the next a call?
            if ((len>i+1) &&
                ((qualifiers[i+1] instanceof Arguments) || (qualifiers[i+1] instanceof AssociateList)))
            {
              pushExprCall(stack, curExpr, s, qualifiers[++i]);
            } else {
              if (s.equals("prototype")) // warn for possible backward JavaScript code.
                printBackwardJavaScriptWarning();
              stack.push(new AccessMember(curExpr,s));
            }
          } else {
            pushExprCall(stack, curExpr, null, o);
          }
        }
        curExpr = new ExprReduced.TempVar(stack.size()-1);
      }
    }
    if (getAccessOp(stack, mdHost, dims))
      curExpr = new ExprReduced.TempVar(stack.size()-1);
    return curExpr;
  }

  private boolean getAccessOp(Stack stack, Expr mdHost, List dims) {
    int _dims = dims.size();
    if (_dims == 0)
      return false;
    if (_dims == 1) {
      stack.push(new AccessIndexed(mdHost, ((Expr)dims.get(0)).reduce(stack)));
    } else {
      Expr[] ea = new Expr[_dims];
      for (int j=0; j<_dims; j++)
        ea[j] = ((Expr)dims.get(j)).reduce(stack);
      stack.push(new AccessMD(mdHost, ea));
    }
    dims.clear();
    return true;
  }

  // Different from reduce(stack) only in that the last
  // tempVar is omitted.
  // (In reduce(), the last tempVar may be used for getType(),
  // where in here, it is not necessary and mustn't be there
  // so the value is set to the destination.)
  public Expr leftReduce(Stack stack) {
    String s;
    Expr curExpr = null;
    int len = qualifiers.length;
    Object o = qualifiers[0];
    int i = 1;
    if (o instanceof Expr) {
      curExpr = ((Expr)o).reduce(stack);
    } else {
      s = (String)qualifiers[0];
      // is the next an Arguments?
      if ((len>1) && ((qualifiers[1] instanceof Arguments) || (qualifiers[1] instanceof AssociateList))) {
        curExpr = createExprCall(curExpr, s, qualifiers[1]);
        if (len==2)
          return curExpr;
        i = 2;
      } else {
        curExpr = new AccessVar(s);
      }
    }
    if (len==1)
      return curExpr;
    stack.push(curExpr);
    curExpr = new ExprReduced.TempVar(stack.size()-1);

    Expr mdHost = null;
    List dims = new ArrayList();
    for (; i<len; i++) {
      o = qualifiers[i];
      if (o instanceof Expr) {
        if (dims.size() == 0)
          mdHost = curExpr; 
        dims.add(o);
      } else {
        if (o instanceof AccessExpr) {
          Expr x = ((AccessExpr)o).expr;
          // is this a call?
          if ((len>i+1) &&
              ((qualifiers[i+1] instanceof Arguments) || (qualifiers[i+1] instanceof AssociateList)))
          {
            curExpr = createExprCall(curExpr, x, qualifiers[++i]);
            stack.push(curExpr);
          } else {
            stack.push(new AccessMember(curExpr, x));
          }
        } else if (o instanceof Expr[]) { // range operator
          ; // not happening.
        } else {
          getAccessOp(stack, mdHost, dims);
          curExpr = new ExprReduced.TempVar(stack.size()-1);
          if (o instanceof String) { // Arguments are handled here as well.
            s = (String)o;
            if (s.equals("->")) // next must be an Arguments; just let go.
              continue;
            // is this a call?
            if ((len>i+1) &&
                ((qualifiers[i+1] instanceof Arguments) || (qualifiers[i+1] instanceof AssociateList)))
            {
              curExpr = createExprCall(curExpr, s, qualifiers[++i]);
            } else {
              if (s.equals("prototype")) // warn for possible backward JavaScript code.
                printBackwardJavaScriptWarning();
              curExpr = new AccessMember(curExpr, s);
            }
          } else {
            curExpr = createExprCall(curExpr, null, o);
          }
          if (i==len-1)
            return curExpr;
          stack.push(curExpr);
        }
        curExpr = new ExprReduced.TempVar(stack.size()-1);
      }
    }
    getAccessOp(stack, mdHost, dims);
    return (Expr)stack.pop();
  }

  void printBackwardJavaScriptWarning() {
    System.err.println("Warning: Is this an old JavaScript program?\n" +
                       "         If so, update to use the new, class-based programming\n" +
                       "         instead of fiddling with 'prototype'.");
  }

  public void dump(XMLWriter out) { // always reduced; never dumped?
    out.simpleTag("ExprPrimary");
    Object o = qualifiers[0];
    if (o instanceof String) out.print((String)o);
    else ((Expr)o).dump(out);
    for (int i=1; i<qualifiers.length; i++) {
      o = qualifiers[i];
      if (o instanceof Arguments) {
        ((Arguments)o).dump(out);
      } else {
        out.print(" . ");
        if (o instanceof Expr) ((Expr)o).dump(out);
        else out.printQuoted(o.toString());
      }
    }
    out.endTag();
  }

  private void pushExprCall(Stack stack, Expr cur, String name, Object params) {
    Object o = createExprCall(cur, name, params);
    if (o != null)
      stack.push(o);
  }

  private ExprCall createExprCall(Expr cur, Object name, Object params) {
    if (params instanceof Arguments) {
      Arguments args = (Arguments)params;
      return name != null ? new ExprCall(cur, name, args.getParams(), args.getTypes(), args.doExpand())
                          : new ExprCall(null, cur, args.getParams(), args.getTypes(), args.doExpand());
    } else if (params instanceof AssociateList) {
      return name != null ? new ExprCall(cur, name, (AssociateList)params)
                          : new ExprCall(null, cur, (AssociateList)params);
    }
    return null;
  }

  ///////////////////////////////////////
  // Stmt implementation
  //

  int lineNum;
  int fileIndex;

  public void setLineNumber(int line) { lineNum = line; }
  public int getLineNumber() { return lineNum; }

  public void setFileIndex(int findex) { fileIndex = findex; }
  public int getFileIndex() { return fileIndex; }

  public Stmt optimizeStmt() { return this; }
  public void pushNewFrame() {}
  public void popFrame() {}
  public void exec() throws ExceptionRuntime {
    ExceptionRuntime.rte(RTERR_INTERNAL_ERROR, "ExprPrimary should never appear at run-time.");
  }

  public static class AccessExpr
  {
    public Expr expr;
    public AccessExpr(Expr expr) { this.expr = expr; }
  }

} // end of class ExprPrimary.

