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
import com.judoscript.bio.JavaObject;
import com.judoscript.util.XMLWriter;


public final class NewJava extends ExprNewBase
{
  Expr className;
  Object inits; // Expr[] or AssociateList
  int[]  javaTypes;
  boolean isClazz;

  public NewJava(Expr className) {
    super(-1);
    isClazz = true;
    this.className = className;
  }

  public NewJava(Expr className, Object inits, int[] javaTypes) {
    super(-1);
    isClazz = false;
    this.className = className;
    this.inits = inits;
    this.javaTypes = javaTypes;
  }

  public Variable eval() throws Throwable {
    if (isClazz)  // TODO: ACCESS CONTROL for com.judoscript.* -- re. script.getStaticJavaClass().
      return RT.getScript().getStaticJavaClass(className.getStringValue());

    Variable var = className.eval();
    Class cls;
    if (var instanceof JavaObject) {
      cls = ((JavaObject)var).getValueClass();
    } else {
      cls = RT.getClass(var.getStringValue()); // TODO: ACCESS CONTROL for com.judoscript.*.
    }

    //return new JavaObject(cls, RT.calcValues(inits), javaTypes);
    return new JavaObject(cls, inits, javaTypes);
  }

  public int getType() { return TYPE_JAVA; }
  public boolean isJava() { return true; }

  public Expr reduce(Stack stack) {
    if (inits != null && inits instanceof Expr[]) {
      Expr[] a = (Expr[])inits;
      int len = (a==null) ? 0 : a.length;
      for (int i=0; i<len; i++)
        a[i] = a[i].reduce(stack);
    }
    return this;
  }

  public void dump(XMLWriter out) {
    out.openTag("NewJava");
    out.tagAttr("isClazz", String.valueOf(isClazz));
    out.closeTag();
    className.dump(out);
    if (inits != null && inits instanceof Expr[]) {
      Expr[] a = (Expr[])inits;
      int len = (a==null) ? 0 : a.length;
      for (int i=0; i<len; i++)
        a[i].dump(out);
      out.endTag();
    } else {
      ; // TODO
    }
  }
}
