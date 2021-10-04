/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 11-05-2002  JH   Inception.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript.bio;

import java.util.Iterator;
import com.judoscript.*;
import com.judoscript.util.*;


/**
 * User-Defined Java Extension
 *
 */
public class JavaExtension extends ObjectInstance implements ExprCollective
{
  Type jt;
  JavaObject jo;

  public JavaExtension(Type jt) {
    super();
    this.jt = jt;
    jo = null;
  }

  public int getType() { return TYPE_JAVA; }

  public void init(Object inits, int[] javaTypes) throws Throwable {

    // invoke the constructor;
    // if no constructors, try to create the Java object.

    Function f = jt.getConstructor();
    if (f == null) {
      jo = new JavaObject(jt.clazz, RT.calcValues((Expr[])inits), javaTypes);
      ((JavaExtensionUser)jo.object)._setJavax(this);
    } else {
      RT.pushThis(this);
      try { f.invoke((Expr[])inits,javaTypes); }
      finally { RT.popThis(); }
    }
    if (jo == null)
      ExceptionRuntime.rte(RTERR_OBJECT_INIT,
        "Java super class is not constructed; need to call super() in your class.");
  }

  public Variable call(String name, Variable[] params) throws Throwable {
    RT.pushThis(this);
    try {
      Function f = jt.getFunction(name);
      if (f == null)  // name is cryptic; should never be null.
        ExceptionRuntime.methodNotFound(jt.getName(), name);
      return f.invoke(params,null);
    } finally { RT.popThis(); }
  }

  public boolean callForBool(String n, Variable[] p) throws Throwable { return call(n,p).getBoolValue(); }
  public byte callForByte(String n, Variable[] p) throws Throwable { return (byte)call(n,p).getLongValue(); }
  public char callForChar(String n, Variable[] p) throws Throwable { return (char)call(n,p).getLongValue(); }
  public short callForShort(String n, Variable[] p) throws Throwable { return (short)call(n,p).getLongValue(); }
  public int callForInt(String n, Variable[] p) throws Throwable { return (int)call(n,p).getLongValue(); }
  public long callForLong(String n, Variable[] p) throws Throwable { return call(n,p).getLongValue(); }
  public float callForFloat(String n, Variable[] p) throws Throwable { return (float)call(n,p).getDoubleValue(); }
  public double callForDouble(String n, Variable[] p) throws Throwable { return call(n,p).getDoubleValue(); }
  public String callForString(String n, Variable[] p) throws Throwable { return call(n,p).getStringValue(); }
  public Object callForObject(String n, Variable[] p) throws Throwable { return call(n,p).getObjectValue(); }

  public final Variable invoke(String fxn, Expr[] params, int[] javaTypes) throws Throwable
  {
    try {
      if (fxn.equals("super")) {
        if (jo != null)
          ExceptionRuntime.rte(RTERR_OBJECT_INIT, "Java constructor is already invoked.");
        jo = new JavaObject(jt.clazz, RT.calcValues(params), javaTypes);
        ((JavaExtensionUser)jo.object)._setJavax(this);
        return this;
      }
      if (fxn.startsWith(".."))
        fxn = "super_" + fxn.substring(2);
      return jo.invoke(fxn, params, javaTypes);
 
    } catch(ExceptionRuntime rte) {
      if (rte.getRealException() instanceof NoSuchMethodException)
        return RT.getScript().invoke(fxn, params, javaTypes);
      else
        throw rte;
    }
  }

  /////////////////////////////////////////////////////////////
  // Context methods -- delegate.
  //
  public String getTypeName()    { return (jo==null) ? null : jo.getTypeName(); }
  public Object getObjectValue() throws Throwable { return (jo==null) ? null : jo.getObjectValue(); }
  public String getStringValue() throws Throwable { return (jo==null) ? null : jo.getStringValue(); }

  public final boolean  hasVariable(String name) {
    return (jo==null) ? false : jo.hasVariable(name);
  }
  public final Variable resolveVariable(String name) throws Throwable {
    return (jo==null) ? ValueSpecial.UNDEFINED : jo.resolveVariable(name);
  }
  public Variable setVariable(String name, Variable val, int type) throws Throwable {
    return (jo==null) ? ValueSpecial.UNDEFINED : jo.setVariable(name,val,type);
  }
  public void close() { if (jo!=null) jo.close(); }

  /////////////////////////////////////////////////////////////
  // ExprCollective methods
  //
  public int size() { return jo.size(); }
  public Variable resolve(Variable idx) throws Throwable { return jo.resolve(idx); }
  public Variable resolve(Variable[] dims) throws Throwable { return jo.resolve(dims); }
  public Variable setVariable(Variable idx, Variable val, int type) throws Throwable {
    return jo.setVariable(idx,val,type);
  }
  public Variable setVariable(Variable[] dims, Variable val, int type) throws Throwable {
    return jo.setVariable(dims,val,type);
  }
  public Iterator getIterator(int start, int to, int step, boolean upto, boolean backward) throws Throwable {
    return jo.getIterator(start, to, step, backward, upto);
  }

  /////////////////////////////////////////////////////////////
  // Type
  //

  public static class Type extends UserDefined.UserType
  {
    Class  clazz;
    Class[] parents; // only 1st can be a class; following are interfaces.

    public Type(String name, Class clazz, Class[] parents) {
      super(name);
      this.clazz = clazz;
      this.parents = parents;
    }

    public void dump(XMLWriter out) {
      out.openTag("JavaExtension.Type");
      out.tagAttr("name", name);
      out.tagAttr("class", clazz.getName());
      out.closeTagLn();
      Script.dumpFunctions(out,fxns);
      out.endTagLn();
    }

    public ObjectInstance create() {
      return new JavaExtension(this);
    }

  } // end of inner class Type.

} // end of class JavaExtension.
