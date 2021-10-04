/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 03-17-2002  JH   Initial open source release.
 * 06-23-2002  JH   Added "isA" user method.
 * 08-11-2002  JH   Fixed getStringValue() methods of all basic type
 *                  multi-dimensional arrays.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript.bio;

import java.lang.reflect.*;
import java.util.*;
import com.judoscript.*;
import com.judoscript.util.Lib;


//
// This class implements everything to create a Java array,
// as well as multi-dimensional array operations.
// One-dimensional arrays, however, are implemented in
// specific inner sub-classes for each Java type.
//
public abstract class JavaArray extends JavaObject
{
  int dims;

  public static Variable wrapArray(Object val) throws ExceptionRuntime {
    String arrClsName = val.getClass().getName();
    int dims = 0;
    for (; dims<arrClsName.length(); dims++) {
      if (arrClsName.charAt(dims) != '[')
        break;
    }
    arrClsName = arrClsName.substring(dims);
    if (arrClsName.endsWith(";")) {
      arrClsName = arrClsName.substring(1, arrClsName.length()-1);
      Class cls = null;
      try {
        cls = RT.getClass(arrClsName);
      } catch(Exception cnfe) {
        try {
          cls = RT.getSysClass(arrClsName);
        } catch(Exception cnfe1) {
          ExceptionRuntime.rte(RTERR_JAVA_EXCEPTION, "Class " + arrClsName + " not found");
        }
      }
      if (cls == String.class)             return new StringArray(val, dims);
      if (cls == java.util.Date.class)     return new DateArray(val, dims);
      if (cls == java.sql.Date.class)      return new SqlDateArray(val, dims);
      if (cls == java.sql.Time.class)      return new SqlTimeArray(val, dims);
      if (cls == java.sql.Timestamp.class) return new SqlTimestampArray(val, dims);
      if (cls == Boolean.class)            return new ObjectBooleanArray(val, dims);
      if (cls == Byte.class)               return new ObjectByteArray(val, dims);
      if (cls == Character.class)          return new ObjectCharacterArray(val, dims);
      if (cls == Short.class)              return new ObjectShortArray(val, dims);
      if (cls == Integer.class)            return new ObjectIntegerArray(val, dims);
      if (cls == Long.class)               return new ObjectLongArray(val, dims);
      if (cls == Float.class)              return new ObjectFloatArray(val, dims);
      if (cls == Double.class)             return new ObjectDoubleArray(val, dims);

      // Check if the object is an array of Variable's ...
      Class[] clzs = cls.getClasses();
      for (int i=0; i<clzs.length; ++i) {
        // If the object is an array of Variable's, create an _Array:
        if (clzs[i] == Variable.class) {
          _Array arr = new _Array();
          int len = Array.getLength(val);
          for (i=0; i<len; ++i)
            arr.append( (Variable)Array.get(val, i) );
          return arr;
        }
      }

      return new ObjectArray(val, dims);

    } else {
      switch(arrClsName.charAt(0)) {
      case 'Z': return new BooleanArray(val, dims);
      case 'B': return new ByteArray(val, dims);
      case 'C': return new CharArray(val, dims);
      case 'D': return new DoubleArray(val, dims);
      case 'F': return new FloatArray(val, dims);
      case 'I': return new IntArray(val, dims);
      case 'J': return new LongArray(val, dims);
      case 'S': return new ShortArray(val, dims);
      }
    }
    return ValueSpecial.UNDEFINED;
  }

  public static JavaArray create(String className, Variable[] _dims) throws Throwable {
    try {
      int len = (_dims==null) ? 0 : _dims.length;
      if (len == 0)
        ExceptionRuntime.rte(RTERR_JAVA_OBJECT_CREATION,"Invalid Java array dimensions: "+len);
      int[] ia = new int[len];
      for (int i=0; i<ia.length; i++)
        ia[i] = (int)_dims[i].getLongValue();

      switch(getJavaPrimitiveType(className)) {
      case JAVA_BOOLEAN: return new BooleanArray(ia);
      case JAVA_BYTE:    return new ByteArray(ia);
      case JAVA_CHAR:    return new CharArray(ia);
      case JAVA_DOUBLE:  return new DoubleArray(ia);
      case JAVA_FLOAT:   return new FloatArray(ia);
      case JAVA_INT:     return new IntArray(ia);
      case JAVA_LONG:    return new LongArray(ia);
      case JAVA_SHORT:   return new ShortArray(ia);
      }
      if ("java.lang.String".equals(className))    return new StringArray(ia);
      if ("java.util.Date".equals(className))      return new DateArray(ia);
      if ("java.sql.Date".equals(className))       return new SqlDateArray(ia);
      if ("java.sql.Time".equals(className))       return new SqlTimeArray(ia);
      if ("java.sql.Timestamp".equals(className))  return new SqlTimestampArray(ia);
      if ("java.lang.Boolean".equals(className))   return new ObjectBooleanArray(ia);
      if ("java.lang.Byte".equals(className))      return new ObjectByteArray(ia);
      if ("java.lang.Character".equals(className)) return new ObjectCharacterArray(ia);
      if ("java.lang.Short".equals(className))     return new ObjectShortArray(ia);
      if ("java.lang.Integer".equals(className))   return new ObjectIntegerArray(ia);
      if ("java.lang.Long".equals(className))      return new ObjectLongArray(ia);
      if ("java.lang.Float".equals(className))     return new ObjectFloatArray(ia);
      if ("java.lang.Double".equals(className))    return new ObjectDoubleArray(ia);
      return new ObjectArray(RT.getClass(className), ia);

    } catch(ClassNotFoundException cnfe) {
      ExceptionRuntime.rte(RTERR_JAVA_EXCEPTION, "Class " + className + " not found");
    }
    return null;
  }

  protected JavaArray(Object obj, Class cls, int dims) {
    super();
    object = obj;
    classes = new Class[]{ cls };
    this.dims = dims;
  }

  protected JavaArray(Class cls, int[] dims) {
    super();
    classes = new Class[]{ cls };
    object = Array.newInstance(cls,dims);
    this.dims = dims.length;
  }

  public boolean isArray() { return true; }

  public final String getClassName() {
    try {
      StringBuffer sb = new StringBuffer(classes[0].getName());
      for (int i=0; i<dims; i++)
        sb.append("[]");
      return sb.toString();
    } catch(Exception e) {
      return "Unknown Class";
    }
  }

  // same as that of _Array.
  public Variable resolve(Variable[] idcs) throws Throwable {
    return resolve(idcs,idcs.length);
  }

  private Variable resolve(Variable[] idcs, int len) throws Throwable {
    ExprCollective ar = this;
    for (int i=0; i<len-1; i++) {
      try { ar = (ExprCollective)ar.resolve(idcs[i]); }
      catch(ClassCastException cce) { return ValueSpecial.UNDEFINED; }
    }
    return ar.resolve(idcs[len-1]);
  }

  public Variable resolve(Variable idx) throws Throwable {
    return resolve((int)idx.getLongValue());
  }

  public Variable resolve(int index) throws Throwable {
    try {
      if (dims == 1)
        return resolveOneDimension(index);
      return JudoUtil.toVariable(Array.get(object,index));
    } catch(Exception e) {}
    return ValueSpecial.UNDEFINED;
  }

  public Variable resolveRange(Expr low, Expr hi) throws Throwable {
    int iLow = (int)low.getLongValue();
    int iHi  = (int)hi.getLongValue();
    if (iLow < 0)
      iLow = 0;
    if (iHi > getSizeOneDimension())
      iHi = getSizeOneDimension() + 1;
    if (iLow >= iHi)
      return ValueSpecial.UNDEFINED;

    return JudoUtil.toVariable(subarray(iLow, iHi));
  }

  // same as that of _Array.
  public Variable setVariable(Variable[] idcs, Variable val, int type) throws Throwable {
    ExprCollective ar = (idcs.length > 1) ? (ExprCollective)resolve(idcs, idcs.length-1)
                                          : (ExprCollective)this;
    return ar.setVariable(idcs[idcs.length-1], val, type);
  }

  public Variable setVariable(Variable idx, Variable var, int type) throws Throwable {
    return setVariable((int)idx.getLongValue(),var,0);
  }

  public Variable setVariable(int idx, Variable var, int type) throws Throwable {
    if (dims == 1)
      return setVariableOneDimension(idx, var);
    Object o = null;
    try {
      if (!var.isNil())
        o = var.getObjectValue();
    } catch(ClassCastException cce) {
      ExceptionRuntime.rte(RTERR_ILLEGAL_VALUE_SETTING, "Invalid Java array value setting.");
    }
    Array.set(object,idx,o);
    return var;
  }

  abstract Variable resolveOneDimension(int index) throws Throwable;
  abstract Variable setVariableOneDimension(int index, Variable var) throws Throwable;
  abstract int getSizeOneDimension();
  abstract void reverse();
  abstract Object subarray(int start, int end);

  public final int size() { return getSizeOneDimension(); }

  void sort(Comparator cptr) throws Exception {
    if (cptr != null) {
      String name = null;
      if (object instanceof boolean[])     name = "boolean";
      else if (object instanceof byte[])   name = "byte";
      else if (object instanceof char[])   name = "char";
      else if (object instanceof short[])  name = "short";
      else if (object instanceof int[])    name = "int";
      else if (object instanceof long[])   name = "long";
      else if (object instanceof float[])  name = "float";
      else if (object instanceof double[]) name = "double";
      else { Arrays.sort((Object[])object,cptr); return; }
      ExceptionRuntime.rte(RTERR_ILLEGAL_VALUE_SETTING,
                           name + "arrays do not do custom sorting.");
    }
    if (object instanceof byte[])   Arrays.sort((byte[])object);
    else if (object instanceof char[])   Arrays.sort((char[])object);
    else if (object instanceof short[])  Arrays.sort((short[])object);
    else if (object instanceof int[])    Arrays.sort((int[])object);
    else if (object instanceof long[])   Arrays.sort((long[])object);
    else if (object instanceof float[])  Arrays.sort((float[])object);
    else if (object instanceof double[]) Arrays.sort((double[])object);
    else if (object instanceof boolean[])
      ExceptionRuntime.rte(RTERR_ILLEGAL_VALUE_SETTING,
                           "Do not know how to sort a boolean array.");
    else Arrays.sort((Object[])object);
  }

  public Variable resolveVariable(String name) throws ExceptionRuntime, Throwable {
    if ("length".equals(name) || "size".equals(name))
      return ConstInt.getInt(getSizeOneDimension());
    ExceptionRuntime.rte(RTERR_INVALID_MEMBER_ACCESS,
                         "Illegal Java array access with name '" + name + "'.");
    return null;
  }

  public Variable setVariable(String name, Variable val, int type) throws Throwable {
    ExceptionRuntime.rte(RTERR_ILLEGAL_VALUE_SETTING,
                         "Illegal Java array setting with name '" + name + "'.");
    return null;
  }

  public final String getTypeName() { return getClassName(); }

  public String getStringValue() throws Throwable {
    StringBuffer sb = new StringBuffer();
    getDimensionArrayString(sb, object, dims);
    return sb.toString();
  }

  public final Variable invoke(String fxn, Expr[] params, int[] javaTypes) throws Throwable
  {
    int len = (params!=null) ? params.length : 0;
    int ord = getMethodOrdinal(fxn);
    switch(ord) {

    case BIM_SIZE:      return ConstInt.getInt(getSizeOneDimension());
    case BIM_LASTINDEX: return ConstInt.getInt(getSizeOneDimension()-1);
    case BIM_REVERSE:   reverse(); break;
    case BIM_SUBARRAY:
      if (len <= 0) break;
      int start = (int)params[0].getLongValue();
      int end = (len > 1)
                ? Math.min((int)params[1].getLongValue(), getSizeOneDimension())
                : getSizeOneDimension();
      try { return JudoUtil.toVariable(subarray(start,end)); } catch(ArrayIndexOutOfBoundsException e) {}
      return ValueSpecial.UNDEFINED;

    case BIM_SORT:
      Comparator cptr = null;
      if (len > 0) {
        try {
          cptr = new UserDefinedComparator();
          ((UserDefinedComparator)cptr).setAccessFunction((AccessFunction)params[0].eval().cloneValue());
        } catch(ClassCastException cce) {
          ExceptionRuntime.rte(RTERR_ILLEGAL_ARGUMENTS, "A comparator function is required.");
        }
      }
      sort(cptr);
      break;

    case BIM_TOFIXEDPOSITIONSTRING:
      if (len<1) return this;
      int[] ia = new int[len];
      for (int i=0; i<len; i++) ia[i] = (int)params[i].getLongValue();
      return JudoUtil.toVariable(Lib.toFixedPosition((Object[])object, ia));

    case BIM_CONCAT:
      String sep = ",";
      if (len > 0) sep = params[0].getStringValue();
      StringBuffer sb = new StringBuffer();
      AccessFunction accfxn = len <= 1 ? null : (AccessFunction)params[1].eval();
      Expr[] ea = (accfxn != null) ? new Expr[1] : null;
      len = getSizeOneDimension();
      for (int i=0; i<len; ++i) {
        if (i>0) sb.append(sep);
        Variable v = resolve(i);
        if (accfxn != null) {
          ea[0] = v;
          v = RT.call(accfxn.getName(), ea, null);
        }
        sb.append(v.getStringValue());
      }
      return JudoUtil.toVariable(sb.toString());

    default: return super.invoke(fxn,params,javaTypes);
    }
    return this;
  }


  void getDimensionArrayString(StringBuffer sb, Object obj, int dim) {
    if (dim == 1) {
      getSingleDimensionArrayString(sb, obj);
      return;
    }
    sb.append('[');
    Object[] oa = (Object[])obj;
    for (int i=0; i<oa.length; i++)
      getDimensionArrayString(sb, oa[i], dim-1);
    sb.append(']');
  }

  protected abstract void getSingleDimensionArrayString(StringBuffer sb, Object arr);


  /////////////////////////////////////////////////////////////////
  // class BooleanArray
  //
  static final class BooleanArray extends JavaArray
  {
    BooleanArray(Object obj, int dims) { super(obj, Boolean.TYPE, dims); }
    BooleanArray(int[] dims) { super(Boolean.TYPE, dims); }
    Variable resolveOneDimension(int index) {
      return ConstInt.getBool( Array.getBoolean(object, index) );
    }
    Variable setVariableOneDimension(int index, Variable val) throws Throwable {
      Array.setBoolean(object, index, val.getBoolValue());
      return val.cloneValue();
    }
    int getSizeOneDimension() {
      return (dims>1) ? ((Object[])object).length : ((boolean[])object).length;
    }
    public Object[] getObjectArrayValue() {
      if (dims>1) return (Object[])object;
      boolean[] ba = (boolean[])object;
      Object[] oa = new Object[ba.length];
      for (int i=oa.length-1; i>=0; --i)
        oa[i] = ba[i] ? Boolean.TRUE : Boolean.FALSE;
      return oa;
    }
    protected void getSingleDimensionArrayString(StringBuffer sb, Object arr) {
      sb.append('[');
      boolean[] ea = (boolean[])arr;
      for (int i=0; i<ea.length; i++) {
        if (i>0) sb.append(',');
        sb.append(ea[i]);
      }
      sb.append(']');
    }
    void reverse() {
      boolean[] arr = (boolean[])object;
      int len = arr.length;
      for (int i=len/2-1; i>=0; --i) {
        boolean x = (boolean)arr[i];
        arr[i] = arr[len-1-i];
        arr[len-1-i] = x;
      }
    }
    Object subarray(int start, int end) {
      boolean[] arr = new boolean[end-start];
      boolean[] oarr = (boolean[])object;
      for (int i=0; i<arr.length; ++i)
        arr[i] = oarr[i+start];
      return arr;
    }
  }

  /////////////////////////////////////////////////////////////////
  // class ByteArray
  //
  static final class ByteArray extends JavaArray
  {
    ByteArray(Object obj, int dims) { super(obj, Byte.TYPE, dims); }
    ByteArray(int[] dims) { super(Byte.TYPE, dims); }
    Variable resolveOneDimension(int index) {
      return ConstInt.getInt( Array.getByte(object, index) & 0x0FF );
    }
    Variable setVariableOneDimension(int index, Variable val) throws Throwable {
      Array.setByte(object,index,(byte)val.getLongValue());
      return val.cloneValue();
    }
    int getSizeOneDimension() {
      return (dims>1) ? ((Object[])object).length : ((byte[])object).length;
    }
    public Object[] getObjectArrayValue() {
      if (dims>1) return (Object[])object;
      byte[] ba = (byte[])object;
      Object[] oa = new Object[ba.length];
      for (int i=oa.length-1; i>=0; --i)
        oa[i] = new Byte(ba[i]);
      return oa;
    }
    protected void getSingleDimensionArrayString(StringBuffer sb, Object arr) {
      sb.append('[');
      byte[] ea = (byte[])arr;
      for (int i=0; i<ea.length; i++) {
        if (i>0) sb.append(',');
        sb.append(ea[i]);
      }
      sb.append(']');
    }
    void reverse() {
      byte[] arr = (byte[])object;
      int len = arr.length;
      for (int i=len/2-1; i>=0; --i) {
        byte x = (byte)arr[i];
        arr[i] = arr[len-1-i];
        arr[len-1-i] = x;
      }
    }
    Object subarray(int start, int end) {
      byte[] arr = new byte[end-start];
      byte[] oarr = (byte[])object;
      for (int i=0; i<arr.length; ++i)
        arr[i] = oarr[i+start];
      return arr;
    }
  }

  /////////////////////////////////////////////////////////////////
  // class CharArray
  //
  static final class CharArray extends JavaArray
  {
    CharArray(Object obj, int dims) { super(obj,Character.TYPE,dims); }
    CharArray(int[] dims) { super(Character.TYPE,dims); }
    Variable resolveOneDimension(int index) {
      return JudoUtil.toVariable( "" + Array.getChar(object,index) );
    }
    Variable setVariableOneDimension(int index, Variable val) throws Exception {
      Array.setChar(object,index,ConstString.toChar(val));
      return val.cloneValue();
    }
    int getSizeOneDimension() {
      return (dims>1) ? ((Object[])object).length : ((char[])object).length;
    }
    public Object[] getObjectArrayValue() {
      if (dims>1) return (Object[])object;
      char[] ca = (char[])object;
      Object[] oa = new Object[ca.length];
      for (int i=oa.length-1; i>=0; --i)
        oa[i] = new Character(ca[i]);
      return oa;
    }
    protected void getSingleDimensionArrayString(StringBuffer sb, Object arr) {
      sb.append('[');
      char[] ea = (char[])arr;
      for (int i=0; i<ea.length; i++) {
        if (i>0) sb.append(',');
        sb.append('\'');
        sb.append(ea[i]);
        sb.append('\'');
      }
      sb.append(']');
    }
    void reverse() {
      char[] arr = (char[])object;
      int len = arr.length;
      for (int i=len/2-1; i>=0; --i) {
        char x = (char)arr[i];
        arr[i] = arr[len-1-i];
        arr[len-1-i] = x;
      }
    }
    Object subarray(int start, int end) {
      char[] arr = new char[end-start];
      char[] oarr = (char[])object;
      for (int i=0; i<arr.length; ++i)
        arr[i] = oarr[i+start];
      return arr;
    }
  }

  /////////////////////////////////////////////////////////////////
  // class ShortArray
  //
  static final class ShortArray extends JavaArray
  {
    ShortArray(Object obj, int dims) { super(obj,Short.TYPE,dims); }
    ShortArray(int[] dims) { super(Short.TYPE,dims); }
    Variable resolveOneDimension(int index) {
      return ConstInt.getInt( Array.getShort(object,index) );
    }
    Variable setVariableOneDimension(int index, Variable val) throws Throwable {
      Array.setShort(object,index,(short)val.getLongValue());
      return val.cloneValue();
    }
    int getSizeOneDimension() {
      return (dims>1) ? ((Object[])object).length : ((short[])object).length;
    }
    public Object[] getObjectArrayValue() {
      if (dims>1) return (Object[])object;
      short[] sa = (short[])object;
      Object[] oa = new Object[sa.length];
      for (int i=oa.length-1; i>=0; --i)
        oa[i] = new Short(sa[i]);
      return oa;
    }
    protected void getSingleDimensionArrayString(StringBuffer sb, Object arr) {
      sb.append('[');
      short[] ea = (short[])arr;
      for (int i=0; i<ea.length; i++) {
        if (i>0) sb.append(',');
        sb.append(ea[i]);
      }
      sb.append(']');
    }
    void reverse() {
      short[] arr = (short[])object;
      int len = arr.length;
      for (int i=len/2-1; i>=0; --i) {
        short x = (short)arr[i];
        arr[i] = arr[len-1-i];
        arr[len-1-i] = x;
      }
    }
    Object subarray(int start, int end) {
      short[] arr = new short[end-start];
      short[] oarr = (short[])object;
      for (int i=0; i<arr.length; ++i)
        arr[i] = oarr[i+start];
      return arr;
    }
  }

  /////////////////////////////////////////////////////////////////
  // class IntArray
  //
  static final class IntArray extends JavaArray
  {
    IntArray(Object obj, int dims) { super(obj,Integer.TYPE,dims); }
    IntArray(int[] dims) { super(Integer.TYPE,dims); }
    Variable resolveOneDimension(int index) {
      return ConstInt.getInt( Array.getInt(object,index) );
    }
    Variable setVariableOneDimension(int index, Variable val) throws Throwable {
      Array.setInt(object,index,(int)val.getLongValue());
      return val.cloneValue();
    }
    int getSizeOneDimension() {
      return (dims>1) ? ((Object[])object).length : ((int[])object).length;
    }
    public Object[] getObjectArrayValue() {
      if (dims>1) return (Object[])object;
      int[] ia = (int[])object;
      Object[] oa = new Object[ia.length];
      for (int i=oa.length-1; i>=0; --i)
        oa[i] = new Integer(ia[i]);
      return oa;
    }
    protected void getSingleDimensionArrayString(StringBuffer sb, Object arr) {
      sb.append('[');
      int[] ea = (int[])arr;
      for (int i=0; i<ea.length; i++) {
        if (i>0) sb.append(',');
        sb.append(ea[i]);
      }
      sb.append(']');
    }
    void reverse() {
      int[] arr = (int[])object;
      int len = arr.length;
      for (int i=len/2-1; i>=0; --i) {
        int x = (int)arr[i];
        arr[i] = arr[len-1-i];
        arr[len-1-i] = x;
      }
    }
    Object subarray(int start, int end) {
      int[] arr = new int[end-start];
      int[] oarr = (int[])object;
      for (int i=0; i<arr.length; ++i)
        arr[i] = oarr[i+start];
      return arr;
    }
  }

  /////////////////////////////////////////////////////////////////
  // class LongArray
  //
  static final class LongArray extends JavaArray
  {
    LongArray(Object obj, int dims) { super(obj,Long.TYPE,dims); }
    LongArray(int[] dims) { super(Long.TYPE,dims); }
    Variable resolveOneDimension(int index) {
      return ConstInt.getInt( Array.getLong(object,index) );
    }
    Variable setVariableOneDimension(int index, Variable val) throws Throwable {
      Array.setLong(object,index,val.getLongValue());
      return val.cloneValue();
    }
    int getSizeOneDimension() {
      return (dims>1) ? ((Object[])object).length : ((long[])object).length;
    }
    public Object[] getObjectArrayValue() {
      if (dims>1) return (Object[])object;
      long[] la = (long[])object;
      Object[] oa = new Object[la.length];
      for (int i=oa.length-1; i>=0; --i)
        oa[i] = new Long(la[i]);
      return oa;
    }
    protected void getSingleDimensionArrayString(StringBuffer sb, Object arr) {
      sb.append('[');
      long[] ea = (long[])arr;
      for (int i=0; i<ea.length; i++) {
        if (i>0) sb.append(',');
        sb.append(ea[i]);
      }
      sb.append(']');
    }
    void reverse() {
      long[] arr = (long[])object;
      int len = arr.length;
      for (int i=len/2-1; i>=0; --i) {
        long x = (long)arr[i];
        arr[i] = arr[len-1-i];
        arr[len-1-i] = x;
      }
    }
    Object subarray(int start, int end) {
      long[] arr = new long[end-start];
      long[] oarr = (long[])object;
      for (int i=0; i<arr.length; ++i)
        arr[i] = oarr[i+start];
      return arr;
    }
  }

  /////////////////////////////////////////////////////////////////
  // class FloatArray
  //
  static final class FloatArray extends JavaArray
  {
    FloatArray(Object obj, int dims) { super(obj,Float.TYPE,dims); }
    FloatArray(int[] dims) { super(Float.TYPE,dims); }
    Variable resolveOneDimension(int index) {
      return new ConstDouble( Array.getFloat(object,index) );
    }
    Variable setVariableOneDimension(int index, Variable val) throws Throwable {
      Array.setFloat(object,index,(float)val.getDoubleValue());
      return val.cloneValue();
    }
    int getSizeOneDimension() {
      return (dims>1) ? ((Object[])object).length : ((float[])object).length;
    }
    public Object[] getObjectArrayValue() {
      if (dims>1) return (Object[])object;
      float[] fa = (float[])object;
      Object[] oa = new Object[fa.length];
      for (int i=oa.length-1; i>=0; --i)
        oa[i] = new Float(fa[i]);
      return oa;
    }
    protected void getSingleDimensionArrayString(StringBuffer sb, Object arr) {
      sb.append('[');
      float[] ea = (float[])arr;
      for (int i=0; i<ea.length; i++) {
        if (i>0) sb.append(',');
        sb.append(ea[i]);
      }
      sb.append(']');
    }
    void reverse() {
      float[] arr = (float[])object;
      int len = arr.length;
      for (int i=len/2-1; i>=0; --i) {
        float x = (float)arr[i];
        arr[i] = arr[len-1-i];
        arr[len-1-i] = x;
      }
    }
    Object subarray(int start, int end) {
      float[] arr = new float[end-start];
      float[] oarr = (float[])object;
      for (int i=0; i<arr.length; ++i)
        arr[i] = oarr[i+start];
      return arr;
    }
  }

  /////////////////////////////////////////////////////////////////
  // class DoubleArray
  //
  static final class DoubleArray extends JavaArray
  {
    DoubleArray(Object obj, int dims) { super(obj,Double.TYPE,dims); }
    DoubleArray(int[] dims) { super(Double.TYPE,dims); }
    Variable resolveOneDimension(int index) {
      return new ConstDouble( Array.getDouble(object,index) );
    }
    Variable setVariableOneDimension(int index, Variable val) throws Throwable {
      Array.setDouble(object,index,val.getDoubleValue());
      return val.cloneValue();
    }
    int getSizeOneDimension() {
      return (dims>1) ? ((Object[])object).length : ((double[])object).length;
    }
    public Object[] getObjectArrayValue() {
      if (dims>1) return (Object[])object;
      double[] da = (double[])object;
      Object[] oa = new Object[da.length];
      for (int i=oa.length-1; i>=0; --i)
        oa[i] = new Double(da[i]);
      return oa;
    }
    protected void getSingleDimensionArrayString(StringBuffer sb, Object arr) {
      sb.append('[');
      double[] ea = (double[])arr;
      for (int i=0; i<ea.length; i++) {
        if (i>0) sb.append(',');
        sb.append(ea[i]);
      }
      sb.append(']');
    }
    void reverse() {
      double[] arr = (double[])object;
      int len = arr.length;
      for (int i=len/2-1; i>=0; --i) {
        double x = (double)arr[i];
        arr[i] = arr[len-1-i];
        arr[len-1-i] = x;
      }
    }
    Object subarray(int start, int end) {
      double[] arr = new double[end-start];
      double[] oarr = (double[])object;
      for (int i=0; i<arr.length; ++i)
        arr[i] = oarr[i+start];
      return arr;
    }
  }

  /////////////////////////////////////////////////////////////////
  // class ObjectArray
  //
  public static class ObjectArray extends JavaArray
  {
    ObjectArray(Object obj, int dims) { super(obj, obj.getClass(), dims); }
    ObjectArray(Object obj, Class cls, int dims) { super(obj, cls, dims); }
    ObjectArray(Class cls, int[] dims) { super(cls, dims); }
    Variable resolveOneDimension(int index) throws Exception {
      Object o = Array.get(object, index);
      if (o == null) return ValueSpecial.UNDEFINED;
      return JudoUtil.toVariable(Array.get(object, index) );
    }
    Variable setVariableOneDimension(int index, Variable val) throws Throwable {
      Array.set(object, index, val.getObjectValue());
      return val.cloneValue();
    }
    int getSizeOneDimension() { return ((Object[])object).length; }
    public Object[] getObjectArrayValue() { return (Object[])object; }
    protected void getSingleDimensionArrayString(StringBuffer sb, Object arr) {
      sb.append('[');
      Object[] ea = (Object[])arr;
      for (int i=0; i<ea.length; i++) {
        if (i>0) sb.append(',');
        sb.append(ea[i]);
      }
      sb.append(']');
    }
    void reverse() {
      Object[] arr = (Object[])object;
      int len = arr.length;
      for (int i=len/2-1; i>=0; --i) {
        Object x = arr[i];
        arr[i] = arr[len-1-i];
        arr[len-1-i] = x;
      }
    }
    Object subarray(int start, int end) {
      Object[] arr = new Object[end-start];
      Object[] oarr = (Object[])object;
      for (int i=0; i<arr.length; ++i)
        arr[i] = oarr[i+start];
      return arr;
    }
  }

  /////////////////////////////////////////////////////////////////
  // class StringArray
  //
  static final class StringArray extends ObjectArray
  {
    StringArray(Object obj, int dims) { super(obj,String.class,dims); }
    StringArray(int[] dims) { super(String.class,dims); }
    Variable setVariableOneDimension(int index, Variable val) throws Throwable {
      Array.set(object,index,val.getStringValue());
      return val.cloneValue();
    }
  }

  /////////////////////////////////////////////////////////////////
  // class DateArray
  //
  static final class DateArray extends ObjectArray
  {
    DateArray(Object obj, int dims) { super(obj,java.util.Date.class,dims); }
    DateArray(int[] dims) { super(java.util.Date.class,dims); }
    Variable setVariableOneDimension(int index, Variable val) throws Throwable {
      Array.set(object,index,val.getDateValue());
      return val.cloneValue();
    }
  }

  /////////////////////////////////////////////////////////////////
  // class SqlDateArray
  //
  static final class SqlDateArray extends ObjectArray
  {
    SqlDateArray(Object obj, int dims) { super(obj,java.sql.Date.class,dims); }
    SqlDateArray(int[] dims) { super(java.sql.Date.class,dims); }
    Variable setVariableOneDimension(int index, Variable val) throws Throwable {
      Array.set(object,index,val.getSqlDate());
      return val.cloneValue();
    }
  }

  /////////////////////////////////////////////////////////////////
  // class SqlTimeArray
  //
  static final class SqlTimeArray extends ObjectArray
  {
    SqlTimeArray(Object obj, int dims) { super(obj,java.sql.Time.class,dims); }
    SqlTimeArray(int[] dims) { super(java.sql.Time.class,dims); }
    Variable setVariableOneDimension(int index, Variable val) throws Throwable {
      Array.set(object,index,val.getSqlTime());
      return val.cloneValue();
    }
  }

  /////////////////////////////////////////////////////////////////
  // class SqlTimestampArray
  //
  static final class SqlTimestampArray extends ObjectArray
  {
    SqlTimestampArray(Object obj, int dims) { super(obj,java.sql.Timestamp.class,dims); }
    SqlTimestampArray(int[] dims) { super(java.sql.Timestamp.class,dims); }
    Variable setVariableOneDimension(int index, Variable val) throws Throwable {
      Array.set(object,index,val.getSqlTimestamp());
      return val.cloneValue();
    }
  }

  /////////////////////////////////////////////////////////////////
  // class ObjectBooleanArray
  //
  static final class ObjectBooleanArray extends ObjectArray
  {
    ObjectBooleanArray(Object obj, int dims) { super(obj,Boolean.class,dims); }
    ObjectBooleanArray(int[] dims) { super(Boolean.class,dims); }
    Variable setVariableOneDimension(int index, Variable val) throws Throwable {
      Array.set(object,index,val.getBoolValue()?Boolean.TRUE:Boolean.FALSE);
      return val.cloneValue();
    }
  }

  /////////////////////////////////////////////////////////////////
  // class ObjectByteArray
  //
  static final class ObjectByteArray extends ObjectArray
  {
    ObjectByteArray(Object obj, int dims) { super(obj,Byte.class,dims); }
    ObjectByteArray(int[] dims) { super(Byte.class,dims); }
    Variable setVariableOneDimension(int index, Variable val) throws Throwable {
      Array.set(object,index,new Byte((byte)val.getLongValue()));
      return val.cloneValue();
    }
  }

  /////////////////////////////////////////////////////////////////
  // class ObjectCharacterArray
  //
  static final class ObjectCharacterArray extends ObjectArray
  {
    ObjectCharacterArray(Object obj, int dims) { super(obj,Character.class,dims); }
    ObjectCharacterArray(int[] dims) { super(Character.class,dims); }
    Variable setVariableOneDimension(int index, Variable val) throws Exception {
      Array.set(object,index,new Character(ConstString.toChar(val)));
      return val.cloneValue();
    }
  }

  /////////////////////////////////////////////////////////////////
  // class ObjectShortArray
  //
  static final class ObjectShortArray extends ObjectArray
  {
    ObjectShortArray(Object obj, int dims) { super(obj,Short.class,dims); }
    ObjectShortArray(int[] dims) { super(Short.class,dims); }
    Variable setVariableOneDimension(int index, Variable val) throws Throwable {
      Array.set(object,index,new Short((short)val.getLongValue()));
      return val.cloneValue();
    }
  }

  /////////////////////////////////////////////////////////////////
  // class ObjectIntegerArray
  //
  static final class ObjectIntegerArray extends ObjectArray
  {
    ObjectIntegerArray(Object obj, int dims) { super(obj,Integer.class,dims); }
    ObjectIntegerArray(int[] dims) { super(Integer.class,dims); }
    Variable setVariableOneDimension(int index, Variable val) throws Throwable {
      Array.set(object,index,new Integer((int)val.getLongValue()));
      return val.cloneValue();
    }
  }

  /////////////////////////////////////////////////////////////////
  // class ObjectLongArray
  //
  static final class ObjectLongArray extends ObjectArray
  {
    ObjectLongArray(Object obj, int dims) { super(obj,Long.class,dims); }
    ObjectLongArray(int[] dims) { super(Long.class,dims); }
    Variable setVariableOneDimension(int index, Variable val) throws Throwable {
      Array.set(object,index,new Long(val.getLongValue()));
      return val.cloneValue();
    }
  }

  /////////////////////////////////////////////////////////////////
  // class ObjectFloatArray
  //
  static final class ObjectFloatArray extends ObjectArray
  {
    ObjectFloatArray(Object obj, int dims) { super(obj,Float.class,dims); }
    ObjectFloatArray(int[] dims) { super(Float.class,dims); }
    Variable setVariableOneDimension(int index, Variable val) throws Throwable {
      Array.set(object,index,new Float((float)val.getDoubleValue()));
      return val.cloneValue();
    }
  }

  /////////////////////////////////////////////////////////////////
  // class ObjectDoubleArray
  //
  static final class ObjectDoubleArray extends ObjectArray
  {
    ObjectDoubleArray(Object obj, int dims) { super(obj,Double.class,dims); }
    ObjectDoubleArray(int[] dims) { super(Double.class,dims); }
    Variable setVariableOneDimension(int index, Variable val) throws Throwable {
      Array.set(object,index,new Double(val.getLongValue()));
      return val.cloneValue();
    }
  }

} // end of class JavaArray.
