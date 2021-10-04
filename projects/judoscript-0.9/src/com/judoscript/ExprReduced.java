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

import java.util.Stack;
import com.judoscript.util.XMLWriter;

// For return type and NIL-ness, we should really eval() first and then
// decide. This, however, contradicts to the purpose of the type-methods,
// because they are used to determine the types in some expressions.
// Same thing with member accesses.
//
// The solution is, dismantle expressions with function calls; use hidden
// variables to hold the function results, and eventually the expresion
// itself will be just value operations! This is done by the parser.
//
// E.g.     $a.foo().bar($a.init(2)) + 1
// becomes: 1. %0 = $a;
//          2. %1 = %0.foo();
//          3. %2 = $a;
//          4. %3 = %2.init(2);
//          5. %4 = %0.bar(%3);
//          F. %4 + 1
// E.g.     $a.$b.$c + 1
// becomes: 1. %0 = $a;
//          2. %1 = %0.$b
//          3. %2 = %1.$c;
//          F. %2 + 1
//
// This is only done for the consumer of an expression, e.g. statements.
//

public class ExprReduced extends ExprSingleBase
{
  public static class TempVar extends ExprAnyBase
  {
    int idx;

    public TempVar(int idx) { this.idx = idx; }

    public Variable eval() throws Throwable { return RT.tempVarAt(idx); }
    public int getIndex() { return idx; }

    public int getType() { try { return eval().getType(); } catch (Throwable e) { return TYPE_UNKNOWN; } }
    public boolean isUnknownType() { return false; }
    public boolean isNil()    { try { return eval().isNil(); } catch (Throwable e) { return false; } }
    public boolean isInt()    { try { return eval().isInt(); } catch (Throwable e) { return false; } }
    public boolean isDouble() { try { return eval().isDouble(); } catch (Throwable e) { return false; } }
    public boolean isString() { try { return eval().isString(); } catch (Throwable e) { return false; } }
    public boolean isDate()   { try { return eval().isDate(); } catch (Throwable e){return false; } }
    public boolean isObject() { try { return eval().isObject(); } catch (Throwable e) { return false; } }
    public boolean isArray()  { try { return eval().isArray(); } catch (Throwable e) { return false; } }
    public boolean isSet()    { try { return eval().isSet(); } catch (Throwable e) { return false; } }
    public boolean isStack()  { try { return eval().isStack(); } catch (Throwable e) { return false; } }
    public boolean isQueue()  { try { return eval().isQueue(); } catch (Throwable e) { return false; } }
    public boolean isReadOnly() { return false; }

    public Expr reduce(Stack stack) { return this; }
    public Expr optimize() { return this; }

    public void dump(XMLWriter out) {
      out.openTag("TempVar");
      out.tagAttr("id", idx);
      out.closeSingleTag();
    }

  } // end of inner class TempVar.

  ////////////////////////////////////////////////////////////////
  // ExprReduced
  //

  Expr[] subExprs = null;

  public ExprReduced(Expr e, Expr[] ea) { super(e); subExprs = ea; }

  public Variable eval() throws Throwable {
    RT.markExprStack();
    try {
      for (int i=0; i<subExprs.length; i++) {
        Variable var = subExprs[i].eval();
        if (var.isValue()) var = var.cloneValue();
        RT.pushExprStack(var);
      }
      Variable v = expr.eval();
      return v;
    } finally {
      RT.resetExprStack();
    }
  }

  public long getLongValue() throws Throwable { return eval().getLongValue(); }
  public double getDoubleValue() throws Throwable { return eval().getDoubleValue(); }
  public String getStringValue() throws Throwable { return eval().getStringValue(); }

  public void dump(XMLWriter out) { dump(out,"ExprReduced"); }

  protected void dump(XMLWriter out, String tagName) {
    out.simpleTagLn(tagName);
    int len = subExprs.length;
    for (int i=0; i<len; i++) {
      out.openTag("implicit");
      out.tagAttr("id", i);
      out.closeTag();
      subExprs[i].dump(out);
      out.endTagLn();
    }
    out.simpleTag("final");
    expr.dump(out);
    out.endTagLn();
    out.endTagLn();
  }

} // end of class ExprReduced.
