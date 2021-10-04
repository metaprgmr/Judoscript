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
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import com.judoscript.util.XMLWriter;


public class ExprConcat extends ExprAnyBase // ExprMultiBase1
{
  List operands;

  public ExprConcat(List ops) { operands = ops; }

  public ExprConcat (Expr first, Expr[] rest) {
    operands = new ArrayList();
    if (first instanceof ConstString || first instanceof ConstInt || first instanceof ValueSpecial)
      operands.add(first.toString());
    else
      operands.add(first);
    int len = rest == null ? 0 : rest.length;
    for (int i=0; i<len; ++i) {
      Expr n = rest[i];
      if (n instanceof ConstString || n instanceof ConstInt || n instanceof ValueSpecial)
        operands.add(n.toString());
      else
        operands.add(n);
    }
  }

  public final List getOperands() { return operands; }

  public Variable eval() throws Throwable {
    return JudoUtil.toVariable(getStringValue());
  }

  public long getLongValue() throws Throwable {
    try { return Integer.parseInt(getStringValue()); }
    catch(Exception e) { return 0; }
  }

  public double getDoubleValue() throws Throwable {
    try { return Double.parseDouble(getStringValue()); }
    catch(Exception e) { return 0.0; }
  }

  public String getStringValue() throws Throwable {
    StringBuffer sb = new StringBuffer();
    Iterator iter = operands.iterator();
    while (iter.hasNext()) {
      Object o = iter.next();
      if (o instanceof Expr)
        o = ((Expr)o).getStringValue();
      sb.append(o.toString());
    }
/*
    for (int i=0; i<parts.length; i++) {
      sb.append(parts[i].toString());
    }
*/
    return sb.toString();
  }

  public Expr reduce(Stack stack) {
    Object o;
    int len = operands==null ? 0 : operands.size();
    if (len == 1) {
      o = operands.get(0);
      if (o instanceof Expr)
        return ((Expr)o).reduce(stack);
      return JudoUtil.toVariable(o);
    }

    for (int i=0; i<len; ++i) {
      o = operands.get(i);
      if (o instanceof Expr) {
        Expr expr = (Expr)o;
        expr = expr.reduce(stack);
        operands.set(i, expr);
      }
    }
    return this;
  }

  public int getType() { return TYPE_STRING; }
  public int getJavaPrimitiveType() { return JAVA_STRING; }

  public void dump(XMLWriter out) {
    out.simpleTag("ExprConcat");
    for (int i=0; i<operands.size(); i++) {
      Object o = operands.get(i);
      if (i>0)
        out.print(" @ ");
      if (o instanceof Expr)
        ((Expr)o).dump(out);
      else
        out.println(o.toString());
    }
    out.endTag();
  }

} // end of class ExprConcat.
