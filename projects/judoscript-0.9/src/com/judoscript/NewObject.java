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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;
import com.judoscript.bio.UserDefined;
import com.judoscript.bio.JavaObject;
import com.judoscript.util.AssociateList;
import com.judoscript.util.XMLWriter;


/**
 *@see com.judoscript.ObjectInstance
 */
public class NewObject extends ExprNewBase
{
  String objTypeName;
  Object inits;
  int[]  javaTypes;

  public NewObject(String objTypeName, Object initializers) {
    this(objTypeName,initializers,null);
  }

  public NewObject(String objTypeName, Object initializers, int[] types) {
    super(-1);
    this.objTypeName = objTypeName;
    inits = initializers;
    javaTypes = types;
  }

  public Variable eval() throws Throwable {
    if (objTypeName == null) { // struct
      try {
        UserDefined udo = new UserDefined();
        udo.init(inits);
        return udo;
      } catch(ClassCastException cce) {
        ExceptionRuntime.rte(RTERR_ILLEGAL_ARGUMENTS,
                             "Illegal initializers for user object.",cce);
      }
    }

    String className = RT.getScript().getRegisteredType(objTypeName);
    if (className == null) { // a named UserDefined object
      UserDefined.UserType ut = RT.getScript().getObjectType(objTypeName);
      ObjectInstance udo = ut.create();
      udo.init(inits,javaTypes);
      return udo;
    }

    try {
      Class cls = RT.getSysClass(className);
      Class pcls = cls.getSuperclass();
      while (pcls != null) {
        if (pcls == ObjectInstance.class)
          break;
        pcls = pcls.getSuperclass();
      }
      if (pcls != null) { // is an ObjectInstance
        ObjectInstance oi = (ObjectInstance)cls.newInstance();
        if (inits != null)
          oi.init(inits);
        return oi;
      } else { // a usual Java class
        if (inits!=null && !(inits instanceof Expr[]))
          ExceptionRuntime.rte(RTERR_OBJECT_INIT, objTypeName + " does not take named initializers.");
        return new JavaObject(RT.getClass(className), RT.calcValues((Expr[])inits), javaTypes);
      }
    } catch(ClassNotFoundException cnfe) {
      ExceptionRuntime.rte(RTERR_OBJECT_INIT,"Class "+className+" not found.",cnfe);
    } catch(InstantiationException ie) {
      ExceptionRuntime.rte(RTERR_OBJECT_INIT, "Failed to initiate class "+className,ie);
    } catch(IllegalAccessException iae) {
      ExceptionRuntime.rte(RTERR_OBJECT_INIT, "Failed to initiate class "+className,iae);
    }
    return null;
  }

  public int getType() { return TYPE_OBJECT; }
  public boolean isObject() { return true; }

  public Expr reduce(Stack stack) {
    try {
      if (inits != null) {
        HashMap ht = (HashMap)inits;
        Iterator keys = ht.keySet().iterator();
        while (keys.hasNext()) {
          String name = (String)keys.next();
          Expr e = (Expr)ht.get(name);
          ht.put(name,e.reduce(stack));
        }
      }
    } catch(ClassCastException cce) {
      // it's gonna fail at runtime; we don't have to do anything here.
    }
    return this;
  }

  public void dump(XMLWriter out) {
    out.openTag("NewObject");
    out.tagAttr("type", objTypeName);
    if (inits == null) {
      out.closeSingleTag();
    } else {
      out.closeTagLn();
      out.simpleTagLn("initializers");
      if (inits instanceof AssociateList) {
        AssociateList ht = (AssociateList)inits;
        for (int i=0; i<ht.size(); ++i) {
          try {
            String key = JudoUtil.toParameterNameString(ht.getKeyAt(i));
            Expr e = (Expr)ht.getValueAt(i);
            out.openTag("init");
            out.tagAttr("name", key);
            out.closeTagLn();
            e.dump(out);
            out.println();
            out.endTagLn();
          } catch(Throwable t) {}
        }
      } else if (inits instanceof Expr[]) {
        Expr[] ea = (Expr[])inits;
        for (int i=0; i<ea.length; i++) {
          ea[i].dump(out);
          out.println(", ");
        }
      }
      out.endTagLn();
      out.endTagLn();
    }
  }

} // end of class NewObject.
