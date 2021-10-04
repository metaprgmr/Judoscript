/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 03-17-2002  JH   Initial open source release.
 * 06-21-2002  JH   Removed final on getValues() for OrderedMap to override.
 * 06-23-2002  JH   Added "copy" built-in method.
 * 06-24-2002  JH   Added isA(name) method and its support.
 * 07-11-2002  JH   In "copy" method, handled the case of getStore() returning null.
 * 08-13-2002  JH   Fixed getKeys methods: if size is 0, return an array not null.
 * 06-05-2003  JH   Use Variable instead of String for keys.
 *                  Internally, use a HashMap for name-values.
 * 06-05-2003  JH   Removed INTERNAL_PREFIX. Removed isPublic() method.
 *                  -- Classes like TreeNode should use their own internal nodes.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript.bio;

import java.util.*;
import java.io.Serializable;
import com.judoscript.*;
import com.judoscript.util.*;


/**
 * User-Defined object instance
 *
 */
public class UserDefined extends ObjectInstance implements ExprCollective
{
  protected Type ot;
  protected Map  storage;
  HashSet locals = null;

  protected UserDefined(Type ot, boolean create) {
    super();
    this.ot = ot;
    storage = create ? new HashMap() : null;
  }

  public UserDefined() { this(null, true); }
  public UserDefined(Type ot) { this(ot, true); }

  public UserDefined(Map map) throws Throwable {
    super();
    this.ot = null;
    storage = new HashMap();
    for (Iterator iter = map.keySet().iterator(); iter.hasNext(); ) {
      Object k = iter.next();
      storage.put(JudoUtil.toVariable(k), JudoUtil.toVariable(map.get(k)));
    }
  }

  public int getType() { return TYPE_STRUCT; }

  public void init(Object inits) throws Throwable {
    if ((inits != null) && (inits instanceof AssociateList)) {
      try {
        AssociateList ht = (AssociateList)inits;
        for (int i=0; i<ht.size(); ++i) {
          String name = JudoUtil.toParameterNameString(ht.getKeyAt(i));
          Variable var = ((Expr)ht.getValueAt(i)).eval();
          setVariable(name, var, 0);
        }
      } catch(Exception e) {
        ExceptionRuntime.rte(RTERR_OBJECT_INIT, "Invalid object initialization", e);
      }
    }

    // Run constructor(s)
    if (ot == null)
    	return;
    Function f = null;
    Expr[] args = null;
    try {
      RT.pushThis(this);
      if (!ot.hasParent()) {
        f = ot.getConstructor();
        if (f != null) {
        	if (inits instanceof Expr[])
        		args = (Expr[])inits;
          f.invoke(args, null);
        }
      } else {
        Stack stack = new Stack();
        Type type = ot;
        stack.push(type);
        while (type.hasParent()) {
          type = type.getParent();
          stack.push(type);
        }
        while (!stack.isEmpty()) {
          type = (Type)stack.pop();
          f = type.getConstructor();
          if (f != null) {
          	if (inits instanceof Expr[])
          		args = (Expr[])inits;
            f.invoke(args, null);
          }
        }
      }
    } catch(ExceptionControl ce) {
      /* won't happen */
    } finally {
      RT.popThis();
    }
  }

  public Map toMap() throws Throwable {
    HashMap ret = new HashMap();
    Iterator iter = storage.keySet().iterator();
    while (iter.hasNext()) {
      Variable k = (Variable)iter.next();
      Variable v = (Variable)storage.get(k);
      Object o;
      if (v instanceof UserDefined)
        o = ((UserDefined)v).toMap();
      else if (v instanceof _Array)
        o = ((_Array)v).toObjectArray();
      else
        o = v.getObjectValue();
      ret.put(k.getObjectValue(), o);
    }
    return ret;
  }

  // See ExprAssign.
  public void copy(Variable src) throws Throwable {
    if (src instanceof UserDefined) {
      Map st = ((UserDefined)src).storage;
      Iterator keys = st.keySet().iterator();
      while (keys.hasNext()) {
        Object k = keys.next();
        storage.put(k, st.get(k));
      }
    } else {
      try {
        Map st = (Map)((JavaObject)src).getObjectValue(); // java.util.Map
        Iterator keys = st.keySet().iterator();
        while (keys.hasNext()) {
          Object k = keys.next();
          setVariable(JudoUtil.toVariable(k), JudoUtil.toVariable(st.get(k)), 0);
        }
      } catch(ClassCastException cce) {
        ExceptionRuntime.rte(RTERR_ILLEGAL_ARGUMENTS, "Can't copy an Object (or map) into an array or set.");
      }
    }
  }

  // See ExprAssign.
  public static void copyToMap(Map map, Variable src) throws Throwable {
    if (src instanceof UserDefined) {
      Map storage = ((UserDefined)src).storage;
      Iterator iter = storage.keySet().iterator();
      while (iter.hasNext()) {
        Variable k = (Variable)iter.next();
        map.put(k.getObjectValue(), ((Variable)storage.get(k)).getObjectValue());
      }
    } else {
      try {
        Map st = (Map)((JavaObject)src).getObjectValue(); // java.util.Map
        Iterator keys = st.keySet().iterator();
        while (keys.hasNext()) {
          Object k = keys.next();
          map.put(k, st.get(k));
        }
      } catch(ClassCastException cce) {
        ExceptionRuntime.rte(RTERR_ILLEGAL_ARGUMENTS, "Can't copy this into a Map.");
      }
    }
  }

  public boolean isA(String name) {
    if (ot != null) return ot.isA(name);
    return (name.equalsIgnoreCase("Struct") ||
            name.equalsIgnoreCase("Object") ||
            name.equalsIgnoreCase("OrderedMap") && (this instanceof OrderedMap) ||
            name.equalsIgnoreCase("SortedMap") && (this instanceof SortedMap)
           );
  }

  public void setLocal(String name) {
    if (locals == null) locals = new HashSet();
    locals.add(name);
  }

  public boolean isLocal(String name) {
    if (locals == null) return false;
    return locals.contains(name);
  }

  public final Function getFunction(String name) {
    if (ot == null) return null;
    Type myOT = ot;
    Function fun = myOT.getFunction(name); // super.foo() becomes '..foo()'; never found here.
    if (fun==null) {
      if (name.startsWith(".."))
        name = name.substring(2);
      do { // not defined here; search the ancestors.
        try { myOT = myOT.getParent(); } catch(ExceptionRuntime rte) { break; }
        if (myOT == Type.STRUCT) break;
        fun = myOT.getFunction(name);
      } while (fun == null);
    }
    return fun;
  }

  public final Variable invoke(Function fun, Expr[] params, int[] javaTypes) throws Throwable
  {
  	Variable ret = ValueSpecial.UNDEFINED;
    if (fun == null)
    	return ret;
    try {
      RT.setFunctionArguments(params);
      RT.pushThis(this);
      ret = ((Function)fun).invoke(params,javaTypes);
    } catch(ExceptionControl ce) {
      /* return is handled already; others ignored. */
    } finally {
      RT.popThis();
    }
    return ret;
  }

  public String getTypeName() { return (ot == null) ? "Object" : ot.getName(); }

  public boolean isOrderedMap() {
    if (this instanceof OrderedMap) return true;
    if (ot == null) return false;
    return ot.isOrderedMap();
  }

  public Iterator getIterator(int start, int end, int step, boolean upto, boolean backward) {
    return RangeIterator.getIterator(getKeys(), start, end, step, upto);
  }

  public String getStringValue() throws Throwable {
    Function f = getFunction("toString");
    try {
      if (f != null) return invoke(f,null,null).getStringValue();
      StringBuffer sb = new StringBuffer("{");
      Iterator iter = getKeys();
      boolean first = true;
      while (iter.hasNext()) {
        Variable k = (Variable)iter.next();
        if (k.isInternal()) continue;
        String key = k.toString();
        if (first) first = false;
        else sb.append(',');
        sb.append(key);
        sb.append('=');
        sb.append(storage.get(k));
      }
      sb.append('}');
      return sb.toString();
    } catch(Throwable e) { return ""; }
  }

  /////////////////////////////////////////////////////////////
  // ExprCollective methods
  //

  public final Variable resolve(Variable key) throws Throwable { return resolveVariable(key); }
  public final Variable resolve(String key) throws Throwable   { return resolveVariable(key); }
  public final Variable resolve(Variable[] keys) throws Throwable {
    int len = keys.length;
    if (len == 0) return ValueSpecial.UNDEFINED;
    Variable v = resolveVariable(keys[0].eval());
    if (len == 1) return v;
    if (!(v instanceof ExprCollective)) return ValueSpecial.UNDEFINED;
    Variable[] newkeys = new Variable[len-1];
    System.arraycopy(keys, 1, newkeys, 0, len-1);
    return ((ExprCollective)v).resolve(newkeys);
  }
  public final Variable setVariable(Variable[] keys, Variable var, int type) throws Throwable {
    int len = keys.length;
    if (len == 0)
      return ValueSpecial.UNDEFINED;
    Variable key0 = keys[0].eval();
    if (len == 1)
      return setVariable(key0, var, type);
    Variable v = resolveVariable(key0);
    Variable[] newkeys = new Variable[len-1];
    System.arraycopy(keys, 1, newkeys, 0, len-1);
    if (v instanceof ExprCollective) {
      return ((ExprCollective)v).setVariable(newkeys, var, type);
    } else {
      _Array ar = new _Array();
      v = ar.setVariable(newkeys, var, type);
      setVariable(key0, ar, type);
      return v.cloneValue();
    }
  }

  /////////////////////////////////////////////////////////////
  // Context methods -- delegate.
  //
  public final boolean hasVariable(String name)   {
    return storage.containsKey(JudoUtil.toVariable(name));
  }
  public final boolean hasVariable(Variable name) {
    return storage.containsKey(name);
  }
  public final Variable resolveVariable(String name) throws Throwable {
    return resolveVariable(JudoUtil.toVariable(name));
  }
  public Variable resolveVariable(Variable name) throws Throwable {
    return JudoUtil.toVariable(storage.get(name));
  }
  public Variable setVariable(String n, Variable v,int t) throws Throwable {
    return setVariable(JudoUtil.toVariable(n), v, t);
  }
  public Variable setVariable(String n, String v, int t) throws Throwable {
    return setVariable(JudoUtil.toVariable(n), JudoUtil.toVariable(v), t);
  }
  public Variable setVariable(Variable name, Variable val, int type) throws Throwable {
    if (val == null) {
      removeVariable(name);
      return ValueSpecial.UNDEFINED;
    }
    storage.put(name.cloneValue(), val.cloneValue());
    return val;
  }
  public void removeVariable(String name)   { storage.remove(JudoUtil.toVariable(name)); }
  public void removeVariable(Variable name) { storage.remove(name); }
  public void clearVariables() { storage.clear(); }
  public void close() { clearVariables(); }

  public Iterator getKeys() { return storage.keySet().iterator(); }
  public Set getKeysAsSet() { return (Set)((HashSet)storage.keySet()).clone(); }

  public Variable get(String key) {
    Variable v = (Variable)storage.get(JudoUtil.toVariable(key));
    return (v != null) ? v : ValueSpecial.UNDEFINED;
  }

  public Variable getKeys(Expr[] params, boolean doSort) throws Throwable
  {
    _Array arr = new _Array();
    if (size() > 0) {
      Iterator iter = getKeys();
      while (iter.hasNext()) {
        Variable k = JudoUtil.toVariable(iter.next());
        if (!k.isInternal())
          arr.append(k);
      }
      if (doSort)
        arr.sort(params, null);
    }
    return arr;
  }

  protected Variable getKeysFiltered(Expr[] params) throws Throwable {
    _Array arr = new _Array();
    if (size() == 0) return arr;
    UserDefinedComparator udop = new UserDefinedComparator();
    try {
      udop.setAccessFunction((AccessFunction)params[0].eval());
    } catch(ClassCastException e) {
      ExceptionRuntime.rte(RTERR_ILLEGAL_ARGUMENTS, "Trying to use non-function variable as a filter.");
    } catch(ArrayIndexOutOfBoundsException aioobe) {
      ExceptionRuntime.rte(RTERR_ILLEGAL_ARGUMENTS, "Need a function variable for the filter.");
    }
    Iterator iter = getKeys();
    while (iter.hasNext()) {
      Variable key = (Variable)iter.next();
      Variable v = JudoUtil.toVariable(key);
      if (udop.filter(key))
        arr.append(v);
    }
    return arr;
  }

  Variable getKeysByValues(Expr[] params, boolean doSort, boolean doFilter)
                          throws Throwable
  {
    _Array ar = new _Array();
    if (size() == 0) return ar;
    int len = (params == null) ? 0 : params.length;

    UserDefinedComparator udop = new UserDefinedComparator();
    if (doFilter) {
      try {
        udop.setAccessFunction((AccessFunction)params[0].eval());
      } catch(Exception e) {
        ExceptionRuntime.rte(RTERR_ILLEGAL_ARGUMENTS, "Need a function variable for the filter.");
      }
    }

    ArrayList jarr = null;
    if (doSort) jarr = new ArrayList();
    Iterator iter = getKeys();
    while (iter.hasNext()) {
      Variable k = (Variable)iter.next();
      Variable v = (Variable)storage.get(k);
      if (doFilter) {
        if (udop.filter(v)) {
          if (doSort)
            jarr.add( new ValueKeyPair(v, k) );
          else
            ar.append(k);
        }
      } else if (doSort) {
        jarr.add( new ValueKeyPair(v, k) );
      } else {
        ar.append(k);
      }
    }

    if (!doSort)
      return ar;

    try {
      Object[] oa = jarr.toArray();
      switch (len) {
      default:
        udop.setAccessFunction((AccessFunction)params[1].eval());
        Arrays.sort(oa, udop);
        break;
      case 1:
        if (!doFilter) {
          udop.setAccessFunction((AccessFunction)params[0].eval());
          Arrays.sort(oa, udop);
          break;
        } // otherwise, params[0] is the filter; fall thru ...
      case 0:
        Arrays.sort(oa, UserDefinedComparator.theNaturalComparator);
        break;
      }
      for (int i=0; i<oa.length; i++)
        ar.append(((ValueKeyPair)oa[i]).key);
    } catch(ClassCastException e) {
      ExceptionRuntime.rte(RTERR_ILLEGAL_ARGUMENTS, "Trying to use non-function variable as a comparator.");
    }

    return ar;
  }

  public Variable getValues() throws Throwable {
    if (size() == 0) return ValueSpecial.UNDEFINED;
    _Array arr = new _Array();
    Iterator iter = getKeys();
    while (iter.hasNext()) {
      Variable k = (Variable)iter.next();
      arr.append((Variable)storage.get(k));
    }
    return arr;
  }

  public final boolean exists(String key) {
    return exists(JudoUtil.toVariable(key));
  }

  public final boolean exists(Variable key) {
    try { return storage.containsKey(key); } catch(Exception e) {}
    return false;
  }

  public final int size() { return (storage==null) ? 0 : storage.size(); }

  public final boolean hasMethod(String fxn) {
    if (null != getFunction(fxn)) return true; // the parenthood has been checked there.
    if (fxn.startsWith("..")) fxn = fxn.substring(2);
    switch(getMethodOrdinal(fxn)) {
    case BIM_ISINT:
    case BIM_ISDOUBLE:
    case BIM_ISNUMBER:
    case BIM_ISSTRING:
    case BIM_ISDATE:
    case BIM_ISJAVA:
    case BIM_ISARRAY:
    case BIM_ISSET:
    case BIM_ISSTRUCT:
    case BIM_ISSTACK:
    case BIM_ISQUEUE:
    case BIM_ISFUNCTION:
    case BIM_ISOBJECT:
    case BIM_ISCOMPLEX:
    case BIM_ISA:
    case BIM_ISNULL:
    case BIM_TYPENAME: // above: commone
    case BIM_SIZE:
    case BIM_CLEAR:
    case BIM_KEYS:
    case BIM_KEYSSORTED:
    case BIM_KEYSFILTERED:
    case BIM_VALUES:
    case BIM_KEYSSORTEDBYVALUE:
    case BIM_KEYSFILTEREDBYVALUE:
    case BIM_KEYSFILTEREDANDSORTEDBYVALUE:
    case BIM_HAS:
    case BIM_HASMETHOD:
    case BIM_ASSERTHAS:
    case BIM_REMOVE:
    case BIM_GET:
    case BIM_SET:
    case BIM_APPEND:
    case BIM_TRANSPOSE:
    case BIM_COPY:      return true;
    default:            return false;
    }
  }

  public Variable invoke(String fxn, Expr[] params, int[] javaTypes) throws Throwable {
    Function f = getFunction(fxn);
    if (f != null)
      return invoke(f, params, javaTypes);
    Variable v = resolve(fxn);
    if (v instanceof AccessFunction) {
      fxn = ((AccessFunction)v).getName();
      f = getFunction(fxn);
      if (f != null)
        return invoke(f, params, javaTypes);
      if (!hasMethod(fxn))
        return RT.getScript().invoke(fxn, params, javaTypes);
    }
    if (!hasMethod(fxn))
      return RT.getScript().invoke(fxn, params, javaTypes);

    if (fxn.startsWith("..")) fxn = fxn.substring(2);
    return invoke(getMethodOrdinal(fxn), fxn, params);
  }

  public Variable invoke(int ord, String fxn, Expr[] params) throws Throwable {
    int len = (params==null) ? 0 : params.length;
    switch(ord) {
    case BIM_SIZE:         return ConstInt.getInt(size());
    case BIM_CLEAR:        checkWritable(); clearVariables(); break;
    case BIM_KEYS:         return getKeys(null, false);
    case BIM_KEYSSORTED:   return getKeys(params, true);
    case BIM_KEYSFILTERED: return getKeysFiltered(params);
    case BIM_VALUES:       return getValues();
    case BIM_KEYSSORTEDBYVALUE:            return getKeysByValues(params, true, false);
    case BIM_KEYSFILTEREDBYVALUE:          return getKeysByValues(params, false, true);
    case BIM_KEYSFILTEREDANDSORTEDBYVALUE: return getKeysByValues(params, true, true);
    case BIM_HAS:
      try {
        for (int i=0; i<len; ++i) {
          if (!exists(params[i].eval()))
            return ConstInt.FALSE;
        }
        return ConstInt.TRUE;
      } catch(Exception e) {}
      break;
    case BIM_HASMETHOD:
      if (len <= 0) return ValueSpecial.UNDEFINED;
      return ConstInt.getBool(hasMethod(params[0].getStringValue()));
    case BIM_ASSERTHAS:
      for (int i=0; i<len; ++i) {
        if (!exists(params[i].eval())) {
          ExceptionRuntime.rte(RTERR_ASSERTION_FAILURE, "Member "+params[i].toString()+" does not exist.");
        }
      }
      return ConstInt.TRUE;
    case BIM_REMOVE:
      for (int i=0; i<len; i++)
        try { removeVariable(params[i].eval()); } catch(Exception e) {}
      break;

    case BIM_GET:
      if (len > 0)
        return resolveVariable(params[0].eval());
      break;

    case BIM_APPEND:
    case BIM_SET:
      if (len > 0)
        return setVariable(params[0].eval(), (len>1) ? params[1].eval() : null, 0);
      break;

    case BIM_TRANSPOSE:
      if (len > 0) {
        String name = params[0].getStringValue();
        if (name.equals("Struct")) // always true.
          return this;
        if (name.equals("OrderedMap")) {
          if (isOrderedMap()) // already true
            return this;
          ExceptionRuntime.rte(RTERR_ILLEGAL_ARGUMENTS, "Can not transpose class "+name+" to OrderedMap.");
        }
        try {
          Type objtype = (Type)RT.getScript().getObjectType(name);
          if (objtype == null)
            ExceptionRuntime.rte(RTERR_ILLEGAL_ARGUMENTS, "Class " + name + " not found.");
          if (objtype.isOrderedMap() && !isOrderedMap()) {
            ExceptionRuntime.rte(RTERR_ILLEGAL_ARGUMENTS,
              "Class " + getTypeName() + " can not be transposed to class " + name +
              ", an OrderedMap its descendent.");
          }
          objtype.preTranspose(this);
          ot = objtype;
        } catch(ClassCastException cce) {
          ExceptionRuntime.rte(RTERR_ILLEGAL_ARGUMENTS,
            "Class " + getTypeName() + " can not be transposed to class " + name);
        }
      }
      return this;

    case BIM_COPY:
      if (len > 0) {
        Variable v = params[0].eval();
        if (v instanceof UserDefined) {
          Map store = ((UserDefined)v).storage;
          if (store != null) { // when can this happen?
            Iterator iter = getKeys();
            while (iter.hasNext()) {
              String name = iter.next().toString();
              setVariable(name, (Variable)storage.get(name), 0);
            }
          }
        } else if (v instanceof JavaObject) {
          Object o = v.getObjectValue();
          if (o instanceof Map) {
            Iterator iter = getKeys();
            while (iter.hasNext()) {
              Object k = iter.next();
              setVariable( k.toString(), JudoUtil.toVariable(((Map)o).get(k)), 0 );
            }
          }
        }
      }
      return this;

    default: return super.invoke(ord,fxn,params);
    }

    return ValueSpecial.UNDEFINED;
  }

  /////////////////////////////////////////////////////////////
  // Type
  //

  public static abstract class UserType implements XMLDumpable, Serializable
  {
    protected String name;
    protected HashMap fxns = null;

    protected UserType(String name) { this.name = name; }

    public String getName() { return name; }

    public void setConstructor(Function ctor) {
      try { addFunction(ctor); } catch(Exception e) {}
    }
    public Function getConstructor() { return getFunction("ctor"); }
    public boolean hasConstructor() { return hasFunction("ctor"); }

    public void addFunction(Function f) throws ExceptionRuntime {
      if (fxns == null) fxns = new HashMap();
      String name = f.getName();
      if (fxns.containsKey(name))
        ExceptionRuntime.rte(RTERR_INTERNAL_ERROR,
          "Method "+name+"() in class "+this.name+" already defined.");
      fxns.put(name,f);
    }

    // see TreeNode.java.
    public void addFunction(String name, Function f) throws ExceptionRuntime {
      f.setName(name);
      addFunction(f);
    }

    public Function getFunction(String name) {
      return (fxns==null) ? null : (Function)fxns.get(name);
    }

    public boolean hasFunction(String name) {
      return (fxns==null) ? false : (null!=fxns.get(name));
    }

    public abstract ObjectInstance create() throws Throwable;

  } // end of inner class UserType.


  public static class Type extends UserType
  {
    String parentName;
    Object comparator;

    public Type(String name, String parentName) { this(name,parentName,null); }

    public Type(String name, String parentName, Object cptr) {
      super(name);
      this.parentName = parentName;
      comparator = cptr;
    }

    public boolean isBuiltin() { return false; }
    public boolean hasParent() {
      return (parentName != null) && !"OrderedMap".equals(parentName) && !"SortedMap".equals(parentName);
    }

    public boolean isA(String n) {
      if (n.equals(name)) return true;
      try {
        Type p = getParent();
        return (p==STRUCT) ? false : p.isA(n);
      } catch(Exception e) { return false; }
    }

    public Type getParent() throws ExceptionRuntime {
      if (parentName == null || parentName.equals("OrderedMap") || parentName.equals("SortedMap"))
         return STRUCT;
      Type t = (Type)RT.getScript().getObjectType(parentName);
      if (t == null)
        ExceptionRuntime.rte(RTERR_UNDEFINED_OBJECT_TYPE, "Object type '"+parentName+"' not defined.");
      return t;
    }

    public boolean isOrderedMap() {
      if (parentName == null) return false;
      if (parentName.equals("OrderedMap")) return true;
      try { return getParent().isOrderedMap(); }
      catch (Exception e) { return false; }
    }

    public boolean isSortedMap() {
      if (parentName == null) return false;
      if (parentName.equals("SortedMap")) return true;
      try { return getParent().isSortedMap(); }
      catch (Exception e) { return false; }
    }

    public void preTranspose(UserDefined o) throws Exception {} // to be overridden.

    public void dump(XMLWriter out) {
      out.openTag("UserDefined.Type");
      out.tagAttr("name", name);
      if (hasParent()) out.tagAttr("parent", parentName);
      out.closeTagLn();
      Script.dumpFunctions(out, fxns);
      out.endTagLn();
    }

    static final Type STRUCT = new Type("struct", null);

    public ObjectInstance create() throws Throwable {
      if ("OrderedMap".equals(parentName)) return new OrderedMap(this);
      if ("SortedMap".equals(parentName)) return new SortedMap(this);
      return new UserDefined(this);
    }

  } // end of inner class Type.

  private static class ValueKeyPair extends VariableAdapter
  {
    Variable v;
    Variable key;

    ValueKeyPair(Variable v, Variable key) { this.v = v; this.key = key; }

    // Variable
    public String  getStringValue() throws Throwable { return v.getStringValue(); }
    public boolean getBoolValue() throws Throwable { return v.getBoolValue(); }
    public long    getLongValue() throws Throwable { return v.getLongValue(); }
    public double  getDoubleValue() throws Throwable { return v.getDoubleValue(); }
    public Object  getObjectValue() throws Throwable { return v.getObjectValue(); }
    public boolean isValue() { return v.isValue(); }
    public Variable cloneValue() { return v.cloneValue(); }
    public java.util.Date getDateValue() throws Throwable { return v.getDateValue(); }
    public java.sql.Date getSqlDate() throws Throwable { return v.getSqlDate(); }
    public java.sql.Time getSqlTime() throws Throwable { return v.getSqlTime(); }
    public java.sql.Timestamp getSqlTimestamp() throws Throwable { return v.getSqlTimestamp(); }
    public Variable invoke(String fxn, Expr[] params, int[] javaTypes) throws Throwable {
      return v.invoke(fxn, params, javaTypes);
    }
    public Variable resolveVariable(String name) throws Throwable {
      return v.resolveVariable(name);
    }
    public String getTypeName() { return v.getTypeName(); }
    public void close() { v.close(); }

    // Expr
    public int getType() { return v.getType(); }
    public boolean isNil()    { return v.isNil(); }
    public boolean isUnknownType() { return v.isUnknownType(); }
    public boolean isInt()    { return v.isInt(); }
    public boolean isDouble() { return v.isDouble(); }
    public boolean isNumber() { return v.isNumber(); }
    public boolean isString() { return v.isString(); }
    public boolean isDate()   { return v.isDate(); }
    public boolean isObject() { return v.isObject(); }
    public boolean isJava()   { return v.isJava(); }
    public boolean isFunction() { return v.isFunction(); }
    public boolean isArray()  { return v.isArray(); }
    public boolean isSet()    { return v.isSet(); }
    public boolean isStack()  { return v.isStack(); }
    public boolean isQueue()  { return v.isQueue(); }
    public boolean isStruct() { return v.isStruct(); }
    public boolean isComplex() { return v.isComplex(); }
    public boolean isA(String name) { return v.isA(name); }
    public boolean isReadOnly() { return v.isReadOnly(); }
    public Variable eval() throws Throwable { return v.eval(); }
    public Expr reduce(Stack stack) { return v.reduce(stack); }
    public Expr optimize() { return v.optimize(); }
    public void dump(XMLWriter out) { v.dump(out); }

  } // end of inner class ValueKeyPair.

} // end of class UserDefined.
