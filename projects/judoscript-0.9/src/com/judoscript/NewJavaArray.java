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
import com.judoscript.bio.JavaArray;
import com.judoscript.util.XMLWriter;


public final class NewJavaArray extends ExprNewBase
{
  Expr className;
  Expr[] dims;

  public NewJavaArray(Expr className, Expr[] dims) {
    super(-1);
    this.className = className;
    this.dims = dims;
  }

  public Variable eval() throws Throwable {
    return JavaArray.create(className.getStringValue(), RT.calcValues(dims));
  }

  public String getStringValue() throws Throwable {
    StringBuffer sb = new StringBuffer("<");
    sb.append(className.getStringValue());
    sb.append(">");
    for (int i=0; i<dims.length; i++)
      sb.append("[]");
    return sb.toString();
  }

  public int getType() { return TYPE_JAVA; }
  public boolean isJava() { return true; }
  public boolean isArray() { return true; }

  public Expr reduce(Stack stack) {
    int len = (dims==null) ? 0 : dims.length;
    for (int i=0; i<len; i++)
      dims[i] = dims[i].reduce(stack);
    return this;
  }

  public void dump(XMLWriter out) {
    out.openTag("NewJavaArray");
    out.tagAttr("class", className.toString());
    out.closeTag();
    for (int i=0; i<dims.length; i++) {
      out.print("[");
      dims[i].dump(out);
      out.print("]");
    }
    out.endTag();
  }
}
