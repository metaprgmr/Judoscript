/*
 * see copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 03-17-2002  JH   Initial open source release.
 * 08-22-2002  JH   Added .() operator support for java.util.Map containers.
 * 04-21-2004  JH   Added caching for class.getMethods(), per
 *                  suggestion from David Medinets (dmedinets@wwre.org)
 * 08-10-2004  JH   Added Namespace for static Java member accesses.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript.bio;

import java.lang.reflect.*;
import java.util.*;
import java.sql.Timestamp;
import javax.rmi.PortableRemoteObject;
import com.judoscript.*;
import com.judoscript.util.EnumerationIterator;
import com.judoscript.util.RangeIterator;
import com.judoscript.util.ListRangeIterator;
import com.judoscript.util.Lib;
import com.judoscript.util.AssociateList;


//
// There is a special case: java.lang.Class as an object. ...
//

public class JavaObject extends ObjectInstance implements ExprCollective, Namespace
{
  static final HashMap _javaTypes = new HashMap();
  static {
    _javaTypes.put("boolean", JAVA_BOOLEAN_I);
    _javaTypes.put("byte",    JAVA_BYTE_I);
    _javaTypes.put("char",    JAVA_CHAR_I);
    _javaTypes.put("short",   JAVA_SHORT_I);
    _javaTypes.put("int",     JAVA_INT_I);
    _javaTypes.put("long",    JAVA_LONG_I);
    _javaTypes.put("float",   JAVA_FLOAT_I);
    _javaTypes.put("double",  JAVA_DOUBLE_I);
    _javaTypes.put("string",  JAVA_STRING_I);
    _javaTypes.put("String",  JAVA_STRING_I);
  }

  public static int getJavaPrimitiveType(String name) {
    Integer I = (Integer)_javaTypes.get(name);
    return (I==null) ? JAVA_ANY : I.intValue();
  }

  //////////////////////////////////////////////////////////////////////////////
  // JavaObject stuff
  //

  protected Object object;
  protected Class[] classes;

  private static final Map methodsInClass = new HashMap();

  public Class getValueClass() {
    try { return classes[0]; }
    catch(Exception e) { return object.getClass(); }
  }

  public String getClassName() {
    try {
      return classes[0].getName();
    } catch(Exception e) {
      try { return getValueClass().getName(); } catch(Exception ee) {}
    }
    return "Unknown Class";
  }

  protected JavaObject() { super(); object = null; }

  public JavaObject(Class clazz) { super(); reInit(null, clazz); }

  public JavaObject(Object val) { super(); reInit(val); }

  public JavaObject(Class clazz, Object val) { super(); reInit(val, clazz); }

  /**
   *@param inits Expr[] or AssociateList
   */
  public JavaObject(Class clazz, Object inits, int[] javaTypes) throws Throwable {
    super();
    if (!Modifier.isPublic(clazz.getModifiers()))
      ExceptionRuntime.rte(RTERR_JAVA_OBJECT_CREATION, "Class "+getClassName()+" is not public.");
    classes = Lib.getPublicClasses(clazz);
    try {
      int len = 0;
      Variable[] initVals = null;
      if (inits != null) {
        if (inits instanceof AssociateList) {
          object = getValueClass().newInstance();
          if (!(object instanceof Map))
            ExceptionRuntime.rte(RTERR_JAVA_OBJECT_CREATION,
              "Class "+getClassName()+" does not take named initialization; only java.util.Map classes do.");
          Map map = (Map)object;
          AssociateList ht = (AssociateList)inits;
          for (int i=0; i<ht.size(); ++i)
            map.put(JudoUtil.toParameterNameString(ht.getKeyAt(i)),
                    ((Expr)ht.getValueAt(i)).getObjectValue());
          return;
        } else {
          len = ((Expr[])inits).length;
          initVals = RT.calcValues((Expr[])inits); // and move on.
        }
      }

      if (len == 0) {
        object = getValueClass().newInstance();
      } else {
        MatchFinder mf = pickConstructor(initVals, javaTypes);
        object = ((Constructor)mf.method).newInstance( getParamValues(initVals, mf.foundTypes) );
      }
    } catch(InstantiationException ie) {
      ExceptionRuntime.rte(RTERR_JAVA_OBJECT_CREATION, "Failed to create a "+getClassName()+" instance.",ie);
    } catch(IllegalAccessException iae) {
      ExceptionRuntime.rte(RTERR_JAVA_OBJECT_CREATION, "Unable to instantiate "+getClassName()+"().", iae);
    } catch(IllegalArgumentException iare) {
      ExceptionRuntime.rte(RTERR_JAVA_OBJECT_CREATION, "Illegal argument(s) to "+getClassName()+"().", iare);
    } catch(InvocationTargetException ite) {
      Throwable t = ite.getTargetException();
      if (t instanceof Exception)
        throw (Exception)t;
      throw new Exception(t.toString());
    } catch(Exception e) {
      ExceptionRuntime.rte(RTERR_JAVA_OBJECT_CREATION, "Failed to create a "+getClassName()+" instance", e);
    }
  }

  public void reInit(Object o) { reInit(o, null); }

  public void reInit(Object o, Class c) {
    object = o;
    classes = Lib.getPublicClasses(o, c);
  }

  public Iterator getIterator(int start, int end, int step, boolean upto, boolean backward) throws Throwable {
    Iterator iter = null;
    Object o = getObjectValue();
    if (o instanceof Iterator) {
      iter = (Iterator)o;
    } else if (o instanceof Enumeration) {
      iter = new EnumerationIterator((Enumeration)o);
    } else if (o instanceof Collection) {
      if (backward)
        return new ListRangeIterator(o, start, end, step, upto, true);
      iter = ((Collection)o).iterator();
    } else if (o instanceof Collection) {
      iter = ((Collection)o).iterator();
    } else if (o instanceof Map) {
      iter = ((Map)o).keySet().iterator();
    }
    if (iter != null)
      return RangeIterator.getIterator(iter, start, end, step, upto);

    Class cls = o.getClass();
    if (cls.isArray())
      return new ListRangeIterator(o, start, end, step, upto, backward);

    // look for iterator(), or the lone Iterator xxx() the lone Enumeration xxx()
    Method[] mthds =  cls.getMethods();
    Method withIter = null;
    Method withEnum = null;
    int iterCnt = 0;
    int enumCnt = 0;
    for (int i=0; i<mthds.length; ++i) {
      Class retType = mthds[i].getReturnType();
      if (retType.equals(Iterator.class) && mthds[i].getParameterTypes().length==0) {
        withIter = mthds[i];
        if (mthds[i].getName().equals("iterator")) {
          iterCnt = 1;
          break;
        } else {
          ++iterCnt;
        }
      } else if (retType.equals(Enumeration.class) && mthds[i].getParameterTypes().length==0) {
        withEnum = mthds[i];
        ++enumCnt;
      }
    }

    if (iterCnt == 1)
      ;
    else if (enumCnt == 1)
      withIter = withEnum;
    else
      withIter = null;

    if (withIter != null)
      o = withIter.invoke(o, new Object[]{});

    return RangeIterator.getIterator(o, start, end, step, upto);
  }

  public boolean isJava() { return true; }
  public final boolean isStatic() { return (object==null); }
  public final boolean isPrimitive() { return getValueClass().isPrimitive(); }

  public final Variable eval() throws Throwable { return this; }

  public final boolean getBoolValue() throws ExceptionRuntime, Throwable {
    if (Boolean.class.isInstance(object)) return ((Boolean)object).booleanValue();
    if (Number.class.isInstance(object))  return ((Number)object).intValue() != 0;
    return true;
  }
  public final long getLongValue() throws ExceptionRuntime, Throwable {
    if (Boolean.class.isInstance(object)) return ((Boolean)object).booleanValue() ? 1 : 0;
    if (Number.class.isInstance(object))  return ((Number)object).longValue();
    if (java.util.Date.class.isInstance(object)) return ((java.util.Date)object).getTime();
    ExceptionRuntime.rte(RTERR_NO_SUCH_VALUE, getClassName()+" has no integer value.");
    return 0;
  }
  public final double getDoubleValue() throws ExceptionRuntime, Throwable {
    if (Boolean.class.isInstance(object)) return ((Boolean)object).booleanValue() ? 1 : 0;
    if (Number.class.isInstance(object))  return ((Number)object).doubleValue();
    if (java.util.Date.class.isInstance(object)) return (double)((java.util.Date)object).getTime();
    ExceptionRuntime.rte(RTERR_NO_SUCH_VALUE, getClassName()+" has no double value.");
    return 0;
  }
  public String getStringValue() throws Throwable {
    return (object==null)
           ? getClassName()
           : object instanceof Class ? ((Class)object).getName() : object.toString();
  }
  public java.util.Date getDateValue() throws ExceptionRuntime, Throwable {
    if (java.util.Date.class.isInstance(object))
      return (java.util.Date)object;
    ExceptionRuntime.rte(RTERR_NO_SUCH_VALUE, getClassName() + " has not Date value.");
    return null;
  }
  public final Object getObjectValue() throws Throwable { return (object!=null) ? object : classes[0]; }

  public boolean isArray() {
    try {
      return (this instanceof JavaArray) ? true : getObjectValue() instanceof Collection;
    } catch(Throwable e) {
      return false;
    }
  }

  public Object[] getObjectArrayValue() throws Throwable {
    Object o = getObjectValue();
    if (o instanceof Collection)
      return ((Collection)o).toArray();
    return super.getObjectArrayValue();
  }

  public java.sql.Date getSqlDate() throws ExceptionRuntime {
    if (java.sql.Date.class.isInstance(object))
      return (java.sql.Date)object;
    if (java.util.Date.class.isInstance(object))
      return new java.sql.Date(((java.util.Date)object).getTime());
    ExceptionRuntime.rte(RTERR_NO_SUCH_VALUE, getClassName() + " has not SQL Date value.");
    return null;
  }
  public java.sql.Time getSqlTime() throws ExceptionRuntime {
    if (java.sql.Time.class.isInstance(object))
      return (java.sql.Time)object;
    if (java.sql.Time.class.isInstance(object))
      return new java.sql.Time(((java.util.Date)object).getTime());
    ExceptionRuntime.rte(RTERR_NO_SUCH_VALUE,getClassName()+" has not SQL Time value.");
    return null;
  }
  public java.sql.Timestamp getSqlTimestamp() throws ExceptionRuntime {
    if (Timestamp.class.isInstance(object))
      return (java.sql.Timestamp)object;
    if (java.util.Date.class.isInstance(object))
      return new Timestamp(((java.util.Date)object).getTime());
    ExceptionRuntime.rte(RTERR_NO_SUCH_VALUE,getClassName()+" has not SQL Timestamp value.");
    return null;
  }

  public int size() {
    if (object instanceof Collection)
      return ((Collection)object).size();
    if (object instanceof Map)
      return ((Map)object).size();
    return 0; // 0-dimensional.
  }

  public boolean hasVariable(String name) {
    try { getValueClass().getField(name); return true; }
    catch(NoSuchFieldException nsfe) { return false; }
  }

  public Variable resolveVariable(String name) throws Throwable {
    if ("class".equals(name) && isStatic())
      return JudoUtil.toVariable(classes[0]);

    try {
      if (object instanceof Collection && name.equals("length"))
        return ConstInt.getInt(((Collection)object).size());
    } catch(Exception e) {}

    try {
      Field fld = getValueClass().getField(name);
      Class cls = fld.getType();
      if (cls.isPrimitive()) {
        switch(getJavaPrimitiveType(cls.getName())) {
        case JAVA_BOOLEAN: return fld.getBoolean(object) ? ConstInt.TRUE : ConstInt.FALSE;
        case JAVA_BYTE:    return ConstInt.getInt(fld.getByte(object));
        case JAVA_CHAR:    return JudoUtil.toVariable("" + fld.getChar(object));
        case JAVA_SHORT:   return ConstInt.getInt(fld.getShort(object));
        case JAVA_INT:     return ConstInt.getInt(fld.getInt(object));
        case JAVA_LONG:    return ConstInt.getInt(fld.getLong(object));
        case JAVA_FLOAT:   return new ConstDouble(fld.getFloat(object));
        case JAVA_DOUBLE:  return new ConstDouble(fld.getDouble(object));
        }
      } else if (cls==String.class) {
        return JudoUtil.toVariable((String)fld.get(object));
      } else if (cls.isArray()) {
        return JavaArray.wrapArray(fld.get(object));
      } else if ((cls==java.util.Date.class) || (cls==java.sql.Date.class) ||
                 (cls==java.sql.Time.class)  || (cls==Timestamp.class))
      {
        return new _Date(((java.util.Date)fld.get(object)).getTime());
      } else if (cls == Boolean.class) {
        return ConstInt.getBool( ((Boolean)fld.get(object)).booleanValue() );
      } else if ((cls == Byte.class)    || (cls == Short.class) ||
                 (cls == Integer.class) || (cls == Long.class))
      {
        return ConstInt.getInt( ((Number)fld.get(object)).longValue() );
      } else if ((cls == Float.class) || (cls == Double.class)) {
        return new ConstDouble( ((Number)fld.get(object)).doubleValue() );
      } else if (cls == Character.class) {
        return JudoUtil.toVariable( fld.get(object).toString() );
      } else {
        return JudoUtil.toVariable(fld.get(object), fld.getType());
      }
    } catch(NoSuchFieldException nsfe) {
      try {
        if (object instanceof Map)
          return JudoUtil.toVariable(((Map)object).get(name));
        else
          return invoke("get"+Lib.capitalizeFirstLetter(name), null, null);
      } catch(InvocationTargetException ite) {
        Throwable ex = (Throwable)ite.getTargetException();
        if (ex instanceof NoSuchMethodError)
          ;
        else if (ex instanceof Exception)
          throw (Exception)ex;
        else
          throw new Exception(ex.toString());
      } catch(NoSuchMethodException nsme) {}
      ExceptionRuntime.rte(RTERR_ILLEGAL_ARGUMENTS, "Field " + name + " not found.");
    } catch(SecurityException se) {
    } catch(IllegalAccessException iae) {
      ExceptionRuntime.rte(RTERR_INVALID_MEMBER_ACCESS,
                "Unable to access " + getClassName() + "." + name + ".");
    } catch(NullPointerException npe) {
    }
    return ValueSpecial.UNDEFINED;
  }

  public Variable setVariable(String name, Variable val, int type) throws Throwable {
    try {
      Field fld = getValueClass().getField(name);
      Class cls = fld.getType();
      if (cls.isPrimitive()) {
        switch(getJavaPrimitiveType(cls.getName())) {
        case JAVA_BOOLEAN: fld.setBoolean(object, val.getBoolValue()); break;
        case JAVA_BYTE:    fld.setByte(object, (byte)val.getLongValue()); break;
        case JAVA_CHAR:    fld.setChar(object, JudoUtil.toChar(val)); break;
        case JAVA_SHORT:   fld.setShort(object,(short)val.getLongValue()); break;
        case JAVA_INT:     fld.setInt(object,(int)val.getLongValue()); break;
        case JAVA_LONG:    fld.setLong(object,val.getLongValue()); break;
        case JAVA_FLOAT:   fld.setFloat(object,(float)val.getDoubleValue()); break;
        case JAVA_DOUBLE:  fld.setDouble(object,val.getDoubleValue()); break;
        }
      } else if (cls == String.class) {
        fld.set(object, val.getStringValue());
      } else if (cls == java.util.Date.class) {
        fld.set(object, val.getDateValue());
      } else if (cls == java.sql.Date.class) {
        fld.set(object, val.getSqlDate());
      } else if (cls == java.sql.Time.class) {
        fld.set(object, val.getSqlTime());
      } else if (cls == Timestamp.class) {
        fld.set(object, val.getSqlTimestamp());
      } else {
        fld.set(object, val.getObjectValue());
      }
      return val.cloneValue();
    } catch(NoSuchFieldException nsfe) {
      try {
        if (object instanceof Map)
          ((Map)object).put(name, val.getObjectValue());
        else
          invoke("set"+Lib.capitalizeFirstLetter(name), new Expr[]{val}, null);
        return val.cloneValue();
      } catch(Exception e) {
      }
    } catch(SecurityException se) {
    } catch(IllegalAccessException ia) {
      ExceptionRuntime.rte(RTERR_ILLEGAL_VALUE_SETTING,
                "Unable to set " + getClassName() + "." + name + ".");
    } catch(NullPointerException npe) {}
    ExceptionRuntime.rte(RTERR_ILLEGAL_VALUE_SETTING, "No such field: " + getValueClass().getName()+".");
    return null;
  }

  public String getTypeName() { return getClassName(); }
  public void setObject(Object o) { object = o; classes = Lib.getPublicClasses(o.getClass()); }

  public void close() {}

  public Variable invoke(String fxn, Expr[] params, int[] javaTypes) throws Throwable {
    int i, len = (params==null) ? 0 : params.length;
    Variable[] va = RT.calcValues(params);
    MatchFinder mf = pickMethod(fxn, va, javaTypes);
    if (mf.isNotMatch()) {
      if (mf.vargsMethod != null) {
        Object[] oa = new Object[len];
        for (i=0; i<len; i++)
          oa[i] = params[i].getObjectValue();
        Object o = ((Method)mf.vargsMethod).invoke( object, new Object[]{ oa } );
        return JudoUtil.toVariable(o);
      }

      if ("instanceof".equals(fxn)) {
        for (i=len-1; i>=0; --i) {
          try {
            if ( RT.getClass(params[i].getStringValue()).isInstance(object) )
              return ConstInt.TRUE;
          } catch(Exception e) {}
        }
        return ConstInt.FALSE;
      }

      if ("cast".equals(fxn)) {
        if (len <= 0)
          return this;
        if (object == null)
          ExceptionRuntime.rte(RTERR_ILLEGAL_ARGUMENTS, "Static java class can not be cast.");
        String clsName = params[0].getStringValue();
        Class cls = RT.getClass(clsName);
        if (!cls.isInstance(object))
          ExceptionRuntime.rte(RTERR_ILLEGAL_ARGUMENTS, getClassName()+" can not be cast to " + clsName);
        return new JavaObject(cls, object);
      }

      if ("narrow".equals(fxn)) { // for RMI/EJB
        if (len <= 0)
          return this;
        return narrow(object, RT.getClass(params[0].getStringValue()));
      }

      if (fxn.equals("toFixedPositionString") && (object instanceof Collection)) {
        if (len<1)
          return this;
        int[] ia = new int[len];
        for (i=0; i<len; i++)
          ia[i] = (int)params[i].getLongValue();
        return JudoUtil.toVariable(Lib.toFixedPosition(((Collection)object).toArray(), ia));
      }

      if (fxn.equals("getOne") && (object instanceof Set)) {
        for (Iterator iter=((Set)object).iterator(); iter.hasNext(); )
          return JudoUtil.toVariable(iter.next());
        return ValueSpecial.NIL;
      }

      if (fxn.equals("getGroups") && object.getClass().getName().endsWith("Matcher")) {
        Class clz = Class.forName("com.judoscript.jdk14.JDK14Thingy");
        Method mthd = clz.getMethod("regexGetGroups", new Class[]{ Object.class });
        return JudoUtil.toVariable( mthd.invoke(null, new Object[]{ object }) );
      }

      try {
        return super.invoke(fxn, params, javaTypes);
      } catch(NoSuchMethodException nsme) {
//        if ((mf.method==null) || !fxn.equals(((Method)mf.method).getName()))
//          throw nsme;
        String x = mf.hasName() ? "accept the specified parameter(s)" : "exist";
        ExceptionRuntime.rte(RTERR_JAVA_EXCEPTION, null,
          new NoSuchMethodException("Method " + fxn + "() in class " + getClassName() + " doesn't " + x + '.'));
      }
    }

    try {
      Class retCls = ((Method)mf.method).getReturnType();
      Object o = ((Method)mf.method).invoke( object, getParamValues(va, mf.foundTypes) );
      if (o==null)
        return fxn.equals("readLine") ? (Variable)ConstString.EOF : ValueSpecial.NIL;

      // In Java extension classes, if the code invokes an own method, the mf seems to
      // point to that method's. That's why retCls is moved up which shouldn't matter.
      // Still need investigation. -- TODO
      if (retCls.isPrimitive()) {
        switch(getJavaPrimitiveType(retCls.getName())) {
        case JAVA_BOOLEAN: return ConstInt.getBool(((Boolean)o).booleanValue());
        case JAVA_BYTE:
        case JAVA_CHAR:    return JudoUtil.toVariable(o.toString());
        case JAVA_SHORT:
        case JAVA_INT:
        case JAVA_LONG:    return ConstInt.getInt(((Number)o).longValue());
        case JAVA_FLOAT:
        case JAVA_DOUBLE:  return new ConstDouble(((Number)o).doubleValue());
        default:           return ValueSpecial.UNDEFINED;
        }
      } else if (retCls == String.class) {
        return JudoUtil.toVariable((String)o);
      } else if (o instanceof Variable) {
        return (Variable)o;
      } else if (retCls.isArray()) {
        return JavaArray.wrapArray(o);
      } else {
        return JudoUtil.toVariable(o, retCls);
      }
    } catch(IllegalAccessException iae) {
      ExceptionRuntime.rte(RTERR_JAVA_METHOD_CALL,
        "Unable to access " + getClassName() + "." + fxn + "().");
    } catch(IllegalArgumentException iare) {
      StringBuffer sb = new StringBuffer();
      len = (va==null) ? 0 : va.length;
      for (i=0; i<len; ++i) {
        if (i>0)
          sb.append(", ");
        sb.append(va[i].getStringValue());
      }
      ExceptionRuntime.rte(RTERR_JAVA_METHOD_CALL,
        "Illegal argument(s) for " + getClassName() + "." + fxn + "(" + sb + ").");
    } catch(InvocationTargetException ite) {
      Throwable th = ite.getTargetException();
      if (th instanceof ExceptionRuntime)
        throw th;
      ExceptionRuntime.rte(RTERR_JAVA_EXCEPTION, null, th);
    } catch(NullPointerException npe) {
      ExceptionRuntime.rte(RTERR_JAVA_METHOD_CALL,
        "Null pointer encountered when invoking " + getClassName() + "." + fxn + "().");
    }
    return ValueSpecial.UNDEFINED; // not reachable.
  }

  // To avoid dependency on javax.rmi.POrtalbRemoteObject.
  private static Variable narrow(Object object, Class cls) throws Exception {
    return JudoUtil.toVariable(PortableRemoteObject.narrow(object, cls));
  }

  final Object[] getParamValues(Variable[] va, int[] javaTypes) throws Throwable {
    int len = (va==null) ? 0 : va.length;
    Object[] oa = new Object[len];
    for (int i=0; i<len; i++) {
      try {
        oa[i] = getParamValue(va[i], javaTypes[i]);
      } catch(Throwable th) {
        ExceptionRuntime.rte(RTERR_ILLEGAL_ARGUMENTS, "Parameter "+(i+1)+" must be java class/array.");
      }
    }
    return oa;
  }

  final Object getParamValue(Variable var, int javaType) throws Throwable {
    switch(javaType) {
    case JAVA_BOOLEAN_O:
    case JAVA_BOOLEAN:    return var.getBoolValue() ? Boolean.TRUE : Boolean.FALSE;
    case JAVA_BYTE_O:
    case JAVA_BYTE:       return new Byte((byte)var.getLongValue());
    case JAVA_SHORT_O:
    case JAVA_SHORT:      return new Short((short)var.getLongValue());
    case JAVA_INT_O:
    case JAVA_INT:        return new Integer((int)var.getLongValue());
    case JAVA_LONG_O:
    case JAVA_LONG:       return new Long(var.getLongValue());
    case JAVA_FLOAT_O:
    case JAVA_FLOAT:      return new Float((float)var.getDoubleValue());
    case JAVA_DOUBLE_O:
    case JAVA_DOUBLE:     return new Double(var.getDoubleValue());
    case JAVA_STRING:     return var.getStringValue();
    case JAVA_CHAR_O:
    case JAVA_CHAR:       return new Character(JudoUtil.toChar(var));
    case JAVA_DATE_O:     return new java.util.Date(var.getLongValue());
    case JAVA_SQL_DATE_O: return new java.sql.Date(var.getLongValue());
    case JAVA_SQL_TIME_O: return new java.sql.Time(var.getLongValue());
    case JAVA_SQL_TIMESTAMP_O: return new Timestamp(var.getLongValue());
    default:              return var.getObjectValue();
    }
  }

  final MatchFinder pickConstructor(Variable[] paramVals, int[] javaTypes) throws Throwable {
    Constructor[] ctors = getValueClass().getConstructors();
    int len = (paramVals==null) ? 0 : paramVals.length;
    MatchFinder ret = new MatchFinder(0.0, paramVals, javaTypes);
    for (int i=0; i<ctors.length; i++) {
      MatchFinder mf = matchParams(ctors[i].getParameterTypes(), paramVals, javaTypes);
      if (mf.score > ret.score) {
        ret = mf;
        ret.method = ctors[i];
      }
    }
    if (ret.score <= 0.0)
      ExceptionRuntime.rte(RTERR_JAVA_OBJECT_CREATION,
                "Constructor not found for class " + getClassName());
    return ret;
  }

  final MatchFinder pickMethod(String methodName, Variable[] paramVals, int[] javaTypes) throws Throwable {
    boolean hasName = false;
    Class cls = null;
    for (int x=0; x<classes.length; x++) {
      cls = classes[x];

      // make sure the map doesn't grow unbounded.
      if (methodsInClass.size() > 200)
        methodsInClass.clear();
      Method[] mthds = (Method[]) methodsInClass.get(cls);
      if (mthds == null) {
        mthds = cls.getMethods();
        methodsInClass.put(cls, mthds);
      }

      int len = (paramVals==null) ? 0 : paramVals.length;
      MatchFinder ret = new MatchFinder(0.0, paramVals, javaTypes);
      for (int i=0; i<mthds.length; i++) {
        String s = mthds[i].getName();
        if (s.equals(methodName)) {
          hasName = true;
          MatchFinder mf = matchParams(mthds[i].getParameterTypes(), paramVals, javaTypes);
          if (mf.score > ret.score) {
            ret = mf;
            ret.method = mthds[i];
          }
        } else if (s.equals("__vargs__" + methodName)) {
          ret.vargsMethod = mthds[i];
        }
      }
      if (ret.score > 0.0 || ret.vargsMethod != null)
        return ret;
    }
    return hasName ? NOT_MATCH_WITH_NAME : NOT_MATCH_NO_NAME;
  }

  private static final double java_prim_map[][] = { // val is java; param is primitive
  // param \ val   Bool  Byte  Char  Sht   Int   Long  Flt   Dbl  ACls
  /* boolean */  { 0.9 , 0.3 , 0.3 , 0.3 , 0.3 , 0.3 , 0.3 , 0.3  },
  /* byte    */  { 0.3 , 0.9 , 0.5 , 0.5 , 0.5 , 0.5 , 0.4 , 0.4  },
  /* char    */  { 0.3 , 0.5 , 0.9 , 0.5 , 0.5 , 0.5 , 0.4 , 0.4  },
  /* short   */  { 0.3 , 0.5 , 0.5 , 0.9 , 0.85, 0.85, 0.8 , 0.8  },
  /* int     */  { 0.3 , 0.5 , 0.5 , 0.85, 0.9 , 0.85, 0.8 , 0.8  },
  /* long    */  { 0.3 , 0.5 , 0.5 , 0.85, 0.85, 0.9 , 0.8 , 0.8  },
  /* float   */  { 0.3 , 0.5 , 0.5 , 0.8 , 0.8 , 0.8 , 0.9 , 0.85 },
  /* double  */  { 0.3 , 0.5 , 0.5 , 0.8 , 0.8 , 0.8 , 0.85, 0.9  }
  }; // any class  t/f   t/f   t/f   t/f   t/f   t/f   t/f   t/f  t/f

  private static final double prim_map[][] = { // val is primitive; may be cast
  //                               explicit cast                is   is   is   is   is  is
  // param\val        bool byte char  sht  int long  flt  dbl   Int  Dbl  Str  Dat  Any null
  //                   <0>  <1>  <2>  <3>  <4>  <5>  <6>  <7>   <8>  <9> <10> <11> <12> <13>

  /*  0 boolean   */ {1.0 , .2 , .2 , .2 , .2 , .2 , .2 , .2 ,  .61, .51, .31, .0 , .0 , .1 },
  /*  1 byte      */ { .2 ,1.0 , .5 , .36, .38, .38, .36, .36,  .81, .61, .31, .0 , .0 , .1 },
  /*  2 char      */ { .2 , .35,1.0 , .35, .37, .37, .35, .35,  .8 , .6 , .91, .0 , .0 , .1 },
  /*  3 short     */ { .2 , .5 , .39,1.0 , .39, .39, .37, .37,  .91, .7 , .41, .0 , .0 , .1 },
  /*  4 int       */ { .2 , .39, .38, .5 ,1.0 , .5 , .38, .38,  .99, .81, .41, .0 , .0 , .1 },
  /*  5 long      */ { .2 , .38, .37, .39, .5 ,1.0 , .39, .39, 1.  , .82, .41, .0 , .0 , .1 },
  /*  6 float     */ { .2 , .37, .36, .38, .36, .36,1.0 , .5 ,  .82, .99, .41, .0 , .0 , .1 },
  /*  7 double    */ { .2 , .36, .35, .37, .35, .35, .5 ,1.0 ,  .83,1.0 , .41, .0 , .0 , .1 },

  /*  8 Boolean   */ { .9 , .19, .19, .19, .19, .19, .19, .19,  .51, .41, .31, .0 , .0 , .99},
  /*  9 Byte      */ { .2 , .9 , .4 , .26, .28, .28, .25, .25,  .71, .51, .3 , .0 , .0 , .99},
  /* 10 Character */ { .2 , .25, .9 , .25, .27, .27, .26, .26,  .7 , .5 , .9 , .0 , .0 , .99},
  /* 11 Short     */ { .2 , .4 , .29, .9 , .29, .29, .27, .27,  .81, .6 , .4 , .0 , .0 , .99},
  /* 12 Integer   */ { .2 , .29, .28, .4 , .9 , .4 , .28, .28,  .97, .71, .4 , .0 , .0 , .99},
  /* 13 Long      */ { .2 , .28, .27, .29, .4 , .9 , .29, .29,  .98, .72, .4 , .0 , .0 , .99},
  /* 14 Float     */ { .2 , .27, .26, .28, .26, .26, .9 , .4 ,  .72, .98, .4 , .0 , .0 , .99},
  /* 15 Double    */ { .2 , .26, .25, .27, .25, .25, .4 , .9 ,  .73, .99, .4 , .0 , .0 , .99},

  /* 16 String    */ { .11, .11, .11, .11, .11, .11, .11, .11,  .4 , .4 ,1.0 , .1 , .1 , .99},
  /* 17 Date      */ { .0 , .0 , .0 , .0 , .1 , .11, .11, .11,  .5 , .5 , .4 ,1.0 , .0 , .99},
  /* 18 SQL Date  */ { .0 , .0 , .0 , .0 , .1 , .11, .11, .11,  .5 , .5 , .4 , .99, .0 , .99},
  /* 19 SQL Time  */ { .0 , .0 , .0 , .0 , .1 , .11, .11, .11,  .5 , .5 , .4 , .98, .0 , .99},
  /* 20 Timestamp */ { .0 , .0 , .0 , .0 , .1 , .11, .11, .11,  .5 , .5 , .4 , .97, .0 , .99},
  /* 21 Object    */ { .1 , .1 , .1 , .1 , .1 , .1 , .1 , .1 ,  .39, .39, .9 , .9 ,1.0 , .99}
  }; // AClass

  final MatchFinder matchParams(Class[] classes, Variable[] va, int[] javaTypes) throws Throwable {
    int len = (classes==null) ? 0 : classes.length;
    int valen = (va==null) ? 0 : va.length;
    if (len != valen)
      return NOT_MATCH_WITH_NAME;
    if (len == 0)
      return SURE_MATCH;
    MatchFinder ret = new MatchFinder(0.0, va, javaTypes);
    for (int i=0; i<len; i++) {
      if (va[i].isNil()) {
        if (!classes[i].isPrimitive())
          ret.score += 0.99;
        continue;
      }
      double delta = 0.0;
      int typ = JAVA_ANY;
      int paramIdx = -1;
      int valIdx = -1;
      if (classes[i].isPrimitive()) {
        if      (classes[i] == Boolean.TYPE)         { paramIdx=0;  typ=JAVA_BOOLEAN; }
        else if (classes[i] == Byte.TYPE)            { paramIdx=1;  typ=JAVA_BYTE; }
        else if (classes[i] == Character.TYPE)       { paramIdx=2;  typ=JAVA_CHAR; }
        else if (classes[i] == Short.TYPE)           { paramIdx=3;  typ=JAVA_SHORT; }
        else if (classes[i] == Integer.TYPE)         { paramIdx=4;  typ=JAVA_INT; }
        else if (classes[i] == Long.TYPE)            { paramIdx=5;  typ=JAVA_LONG; }
        else if (classes[i] == Float.TYPE)           { paramIdx=6;  typ=JAVA_FLOAT; }
        else if (classes[i] == Double.TYPE)          { paramIdx=7;  typ=JAVA_DOUBLE; }
      } else if (!(va[i] instanceof JavaObject)) {
        if      (classes[i] == Boolean.class)        { paramIdx=8;  typ=JAVA_BOOLEAN_O; }
        else if (classes[i] == Byte.class)           { paramIdx=9;  typ=JAVA_BYTE_O; }
        else if (classes[i] == Character.class)      { paramIdx=10; typ=JAVA_CHAR_O; }
        else if (classes[i] == Short.class)          { paramIdx=11; typ=JAVA_SHORT_O; }
        else if (classes[i] == Integer.class)        { paramIdx=12; typ=JAVA_INT_O; }
        else if (classes[i] == Long.class)           { paramIdx=13; typ=JAVA_LONG_O; }
        else if (classes[i] == Float.class)          { paramIdx=14; typ=JAVA_FLOAT_O; }
        else if (classes[i] == Double.class)         { paramIdx=15; typ=JAVA_DOUBLE_O; }
        else if (classes[i] == String.class)         { paramIdx=16; typ=JAVA_STRING; }
        else if (classes[i] == java.util.Date.class) { paramIdx=17; typ=JAVA_DATE_O; }
        else if (classes[i] == java.sql.Date.class)  { paramIdx=18; typ=JAVA_SQL_DATE_O; }
        else if (classes[i] == java.sql.Time.class)  { paramIdx=19; typ=JAVA_SQL_TIME_O; }
        else if (classes[i] == Timestamp.class)      { paramIdx=20; typ=JAVA_SQL_TIMESTAMP_O; }
        else if (!classes[i].isArray())              { paramIdx=21; }
      }

      if (va[i] instanceof JavaObject) {
        Object o = va[i].getObjectValue();
        Class cls = ((JavaObject)va[i]).getValueClass();
        if      (cls == Boolean.class)   valIdx = 0;
        else if (cls == Byte.class)      valIdx = 1;
        else if (cls == Character.class) valIdx = 2;
        else if (cls == Short.class)     valIdx = 3;
        else if (cls == Integer.class)   valIdx = 4;
        else if (cls == Long.class)      valIdx = 5;
        else if (cls == Float.class)     valIdx = 6;
        else if (cls == Double.class)    valIdx = 7;

        if (paramIdx < 0) {
          delta = classes[i].getName().equals(cls.getName())
                  ? 1.0
                  : classes[i].isInstance(o) ? 0.99 : 0.0;
        } else if (valIdx >= 0 && valIdx < 8) {
          delta = java_prim_map[paramIdx][valIdx];
        }

      } else { // value not Java

        if (paramIdx < 0) continue;
        switch(ret.foundTypes[i]) {
        case JAVA_BOOLEAN: valIdx = 0; break;
        case JAVA_BYTE:    valIdx = 1; break;
        case JAVA_CHAR:    valIdx = 2; break;
        case JAVA_SHORT:   valIdx = 3; break;
        case JAVA_INT:     valIdx = 4; break;
        case JAVA_LONG:    valIdx = 5; break;
        case JAVA_FLOAT:   valIdx = 6; break;
        case JAVA_DOUBLE:  valIdx = 7; break;
        default:
          if (va[i] instanceof ConstString) valIdx = 10;
          else if (va[i].isNil())       valIdx = 13;
          else if (va[i].isDate())      valIdx = 11;
          else if (va[i].isInt())       valIdx = 8;
          else if (va[i].isDouble())    valIdx = 9;
          else if (va[i].isString())    valIdx = 10;
          else                          valIdx = 12;
        }
        if (valIdx >= 0) {
          delta = prim_map[paramIdx][valIdx];
        }
      }

      if (delta > 0) {
        ret.score += delta;
        ret.foundTypes[i] = typ;
      }
    }
    ret.score /= len;
    return ret;
  }

  ///////////////////////////
  //
  static class MatchFinder
  {
    private boolean _hasName = false; // only for NOT_MATCH_WITH/NO_NAME.
    Method vargsMethod = null;

    int[] foundTypes = null;
    double score;
    Object method = null; // Constructor or Method.

    private MatchFinder(boolean hasName) {
      _hasName = hasName;
      score = -1.0;
    }

    MatchFinder() { score = -1.0; }
    MatchFinder(double score) { this.score = score; }
    MatchFinder(double score, Variable[] paramVals, int[] types) {
      int len = paramVals==null ? 0 : paramVals.length;
      this.score = score;
      foundTypes = new int[len];
      for (int i=0; i<len; i++) {
        foundTypes[i] = (types!=null && i<types.length) ? types[i] : JAVA_ANY;
        if (foundTypes[i] == JAVA_ANY)
          foundTypes[i] = paramVals[i].getJavaPrimitiveType();
      }
    }

    MatchFinder returnBad() { score = -1.0; return this; }

    public boolean isNotMatch() { return score <= 0.0; }
    public boolean hasName() { return _hasName; }

    public String toString() {
      StringBuffer sb = new StringBuffer();
      if (method != null) sb.append(method.toString());
      sb.append(" : "); 
      sb.append(score);
      int len = (foundTypes == null) ? 0 : foundTypes.length;
      for (int i=0; i<len; ++i) {
        sb.append("-");
        switch(foundTypes[i]) {
        case JAVA_ANY:     sb.append("ANY"); break;
        case JAVA_STRING:  sb.append("STRING"); break;
        case JAVA_BOOLEAN: sb.append("BOOLEAN"); break;
        case JAVA_BYTE:    sb.append("BYTE"); break;
        case JAVA_CHAR:    sb.append("CHAR"); break;
        case JAVA_SHORT:   sb.append("SHORT"); break;
        case JAVA_INT:     sb.append("INT"); break;
        case JAVA_LONG:    sb.append("LONG"); break;
        case JAVA_FLOAT:   sb.append("FLOAT"); break;
        case JAVA_DOUBLE:  sb.append("DOUBLE"); break;
        default:           sb.append(foundTypes[i]); break;
        }
      }
      return sb.toString();
    }
  }

  //
  // Public helpers
  //

  public static JavaObject wrapClass(Class cls) { return new JavaObject(cls,null); }

  static final MatchFinder NOT_MATCH_WITH_NAME = new MatchFinder(true);
  static final MatchFinder NOT_MATCH_NO_NAME = new MatchFinder(false);
  static final MatchFinder SURE_MATCH = new MatchFinder(1.0);

  // java.util.List implementation

  public Variable resolve(Variable[] idcs) throws Throwable {
    ExceptionRuntime.rte(RTERR_ILLEGAL_VALUE_SETTING,
                         getClassName() + " does not support multi-dimensional getting.");
    return null;
  }

  public Variable setVariable(Variable[] idcs, Variable val, int type) throws Throwable {
    ExceptionRuntime.rte(RTERR_ILLEGAL_VALUE_SETTING,
                         getClassName() + " does not support multi-dimensional setting.");
    return null;
  }

  public Variable setVariable(Variable idx, Variable var, int type) throws Throwable {
    if (object instanceof Collection) {
      return setVariable((int)idx.getLongValue(), var, 0);
    } else if (object instanceof Map) {
      ((Map)object).put(idx.getObjectValue(), var.getObjectValue());
      return var;
    } else {
      return setVariable(idx.getStringValue(), var, type);
    }
  }

  public final Variable addVariable(Variable var, int type) throws Throwable {
    if (object instanceof Collection) {
      ((Collection)object).add(var.getObjectValue());
      return var;
    }
    return super.addVariable(var, type);
  }

  public Variable resolve(Variable idx) throws Throwable {
    if (object instanceof Collection) {
      List list = (List)object;
      int i = (int)idx.getLongValue();
      return (i>=0 && i<list.size()) ? JudoUtil.toVariable(list.get(i)) : ValueSpecial.UNDEFINED;
    } else if (object instanceof Map) {
      return JudoUtil.toVariable(((Map)object).get(idx.getObjectValue()));
    } else {
      return resolveVariable(idx.getStringValue());
    }
  }

  public Variable resolve(int index) throws Throwable {
    try { return JudoUtil.toVariable(((List)object).get(index)); } catch(Exception e) {}
    return ValueSpecial.UNDEFINED;
  }

  public Variable setVariable(int idx, Variable var, int type) throws Throwable {
    try {
      List list = (List)object;
      if (list.size() < idx+1)
        Lib.ensureSize(list, idx+1);
      list.set(idx,var.getObjectValue());
    } catch(Exception e) {}
    return var;
  }

} // end of class JavaObject.

