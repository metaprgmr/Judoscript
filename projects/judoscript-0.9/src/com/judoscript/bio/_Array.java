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


package com.judoscript.bio;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.*;
import com.judoscript.*;
import com.judoscript.util.ListRangeIterator;
import com.judoscript.util.Lib;


public class _Array extends ObjectInstance implements ExprCollective
{
  List data;

  public _Array() {
    super();
    data = new ArrayList();
  }

  public _Array(List al) {
    super();
    data = (al != null) ? al : new ArrayList();
  }

  public final Iterator getIterator(int start, int end, int step, boolean upto, boolean backward) {
    return new ListRangeIterator(data, start, end, step, upto, backward);
  }

  public List getStorage() { return data; }

  public final void init(Object inits) throws Throwable {
    if (inits == null) return;
    if (inits instanceof Expr[]) {
      Expr[] es = (Expr[])inits;
      for (int i=0; i<es.length; i++)
        append(es[i].eval().cloneValue());
    } else super.init(inits);
  }

  public int size() { return data.size(); }

  public final void close() {
//    for (int i=data.size()-1; i>=0; i--) {
//      Variable var = (Variable)data.get(i);
//    }
    data.clear();
  }

  public final String getStringValue() throws Throwable {
    StringBuffer sb = new StringBuffer("[");
    int len = data.size();
    for (int i=0; i<len; i++) {
      if (i>0) sb.append(",");
      sb.append(resolve(i).getStringValue());
    }
    sb.append("]");
    return sb.toString();
  }

  public final int getType() { return TYPE_ARRAY; }
  public final boolean isArray() { return true; }

  public final Object getObjectValue() throws Throwable { return data; }

  public final Object[] getObjectArrayValue() throws Throwable {
    Object[] ret = new Object[data.size()];
    for (int i=ret.length-1; i>=0; --i)
      ret[i] = ((Expr)data.get(i)).getObjectValue();
    return ret;
  }

  public final Variable resolve(int index) {
    if (index < data.size()) {
      try {
        Variable var = (Variable)data.get(index);
        if (var != null)
          return var;
      } catch(Exception e) {}
    }
    return ValueSpecial.UNDEFINED;
  }

  public Variable resolveRange(Variable low, Variable hi) throws Throwable {
    int iLow = (int)low.getLongValue();
    int iHi  = (int)hi.getLongValue();
    if (iLow < 0)
      iLow = 0;
    if (iHi >= data.size())
      iHi = data.size() - 1;
    if (iLow > iHi)
      return ValueSpecial.UNDEFINED;

    ArrayList al = new ArrayList();
    for (; iLow <= iHi; ++iLow)
      al.add(data.get(iLow));

    return new _Array(al);
  }

  public final Variable resolve(Variable idx) {
    try { return resolve((int)idx.getLongValue()); } catch(Throwable e) {}
    return ValueSpecial.UNDEFINED;
  }

  public final Variable resolve(Variable[] idcs) throws Throwable {
    ExprCollective ar = this;
    for (int i=0; i<idcs.length-1; i++) {
      try { ar = (ExprCollective)ar.resolve(idcs[i]); }
      catch(ClassCastException cce) { return ValueSpecial.UNDEFINED; }
    }
    return ar.resolve(idcs[idcs.length-1]);
  }

  public final Variable setVariable(int idx, Variable val, int type) throws Exception {
    checkWritable();
    return private_setVariable(idx, val, type);
  }

  public final Variable setVariable(Variable idx, Variable val, int type) throws Throwable {
    checkWritable();
    return private_setVariable((int)idx.getLongValue(), val, type);
  }

  public final Variable setVariable(Variable[] idcs, Variable val, int type) throws Throwable {
    checkWritable();
    return private_setVariable(idcs, val, type);
  }

  public final Variable addVariable(Variable val, int type) throws Throwable {
    checkWritable();
    return private_setVariable(data.size(), val, type);
  }

  private final void ensureSize(int newSize) { ensureSize(newSize, null); }

  private final void ensureSize(int newSize, Variable v) {
    if (newSize > data.size()) {
      newSize -= data.size();
      for (int i=newSize; i>0; --i)
        data.add(v);
    }
  }

  final Variable private_setVariable(int idx, Variable val, int type) throws Exception {
    val = val.cloneValue();
    if (idx >= data.size())
      ensureSize(idx+1);

    data.set(idx, val);
    return val;
  }

  private final Variable private_setVariable(Variable[] idcs, Variable val, int type) throws Throwable {
    ExprCollective ar = this;
    Variable var;
    for (int i=0; i<idcs.length-1; i++) {
      int idx = (int)idcs[i].getLongValue();
      var = resolve(idx);
      if (var.isNil() || !(var instanceof ExprCollective)) {
        var = new _Array();
        ar.setVariable(idcs[i], var, type);
      }
      ar = (ExprCollective)var;
    }
    return ar.setVariable(idcs[idcs.length-1], val, type);
  }

  public final void removeVariable(String idx) {
    try { setVariable(Integer.parseInt(idx), ValueSpecial.UNDEFINED, 0); } catch(Exception e) {}
  }

  public final void removeVariable(Variable idx) {
    try { setVariable(idx, ValueSpecial.UNDEFINED, 0); } catch(Throwable e) {}
  }

  public Variable resolveVariable(String name) throws Throwable {
    if ("size".equals(name) || "length".equals(name))
      return ConstInt.getInt(data.size());
    return ValueSpecial.UNDEFINED;
  }
  public final Variable setVariable(String name, Variable val, int type) { return ValueSpecial.UNDEFINED; }
  public final void clearVariables() { close(); }

  public final boolean areAllInt() {
    for (int i=data.size()-1; i>=0; i--) {
      if (!((Variable)data.get(i)).isInt()) return false;
    }
    return true;
  }

  public final String getTypeName() { return "Array"; }

  public final void append(Variable v) { data.add(v); }

  public final void sort(Expr[] params, Comparator cptr) throws Throwable {
    if ((params != null) && (params.length > 0)) {
      try {
        Variable v = params[0].eval();
        if (v instanceof JavaObject) {
          cptr = (Comparator)v.getObjectValue();
        } else {
          cptr = new UserDefinedComparator();
          ((UserDefinedComparator)cptr).setAccessFunction((AccessFunction)v);
        }
      } catch(ClassCastException cce) {
        ExceptionRuntime.rte(RTERR_ILLEGAL_ARGUMENTS, "A comparator function is required.");
      }
    } else if (cptr == null) {
      cptr = UserDefinedComparator.theNaturalComparator;
    }
    Object[] oa = data.toArray();
    Arrays.sort(oa, cptr);
    for (int i=oa.length-1; i>=0; i--)
      data.set(i,oa[i]);
  }

  public final Variable invoke(String fxn, Expr[] params, int[] javaTypes) throws Throwable
  {
    int len = (params==null) ? 0 : params.length;
    int i, start, end;
    Variable v;
    _Array ar;
    boolean needClose;
    int ord = getMethodOrdinal(fxn);

    switch(ord) {
    case BIM_SIZE:
      return ConstInt.getInt(data.size());

    case BIM_SAVEASLINES:
      if (len <= 0)
        break;
      v = params[0].eval();
      PrintWriter pw;
      if (v instanceof JavaObject && v.getObjectValue() instanceof PrintWriter) {
        pw = (PrintWriter)v.getObjectValue();
        needClose = false;
      } else {
        pw = new PrintWriter(new FileWriter(v.getStringValue()));
        needClose = true;
      }
      for (i=0; i<data.size(); ++i)
        pw.println(((Variable)data.get(i)).getStringValue());
      if (needClose)
        pw.close();
      break;

    case BIM_LOADASLINES:
      if (len <= 0)
        break;
      BufferedReader br;
      v = params[0].eval();
      if (v instanceof JavaObject && v.getObjectValue() instanceof BufferedReader) {
        br = (BufferedReader)v.getObjectValue();
        needClose = false;
      } else {
        br = new BufferedReader(new FileReader(v.getStringValue()));
        needClose = true;
      }
      while (true) {
        String s = br.readLine();
        if (s == null)
          break;
        append(JudoUtil.toVariable(s));
      }
      if (needClose)
        br.close();
      break;

    case BIM_LASTINDEX:
      return ConstInt.getInt(data.size()-1);

    case BIM_FIRST:
      if (data.size() <= 0) return ValueSpecial.UNDEFINED;
      return resolve(0);

    case BIM_LAST:
      if (data.size() <= 0) return ValueSpecial.UNDEFINED;
      return resolve(data.size()-1);

    case BIM_CLEAR:
      checkWritable();
      close();
      break;

    case BIM_INSERT:
      checkWritable();
      if (len <= 0) break;
      data.add( (len==1) ? 0 : (int)params[1].getLongValue(), params[0].eval().cloneValue() );
      break;

    case BIM_PREPEND:
      checkWritable();
      if (len <= 0) break;
      int len1 = data.size();
      ensureSize(len1 + len);
      for (i=len1-1; i>=0; i--)
        data.set(i+len, data.get(i));
      for (i=0; i<len; i++)
        data.set(i, params[i].eval().cloneValue());
      break;

    case BIM_PREPENDARRAY:
      checkWritable();
      if (len <= 0) break;
      ArrayList vec = new ArrayList();
      for (i=0; i<len; i++) {
        v = params[i].eval();
        if (v instanceof _Array)
          vec.addAll(((_Array)v).data);
        else
          vec.add(v);
      }
      vec.addAll(data);
      data =vec;
      break;

    case BIM_APPEND:
      checkWritable();
      for (i=0; i<len; i++)
        data.add(params[i].eval().cloneValue());
      break;

    case BIM_APPENDARRAY:
      checkWritable();
      if (len <= 0) break;
      for (i=0; i<len; i++) {
        v = params[i].eval();
        if (v instanceof _Array)
          data.addAll(((_Array)v).data);
        else
          data.add(v);
      }
      break;

    case BIM_SETSIZE:
      if (len < 0) break;
      checkWritable();
      i = (int)params[0].getLongValue();
      if (i > data.size()) {
        ensureSize(i, (len>1) ? params[1].eval() : null);
      } else if (i < data.size()) {
        for (int j=data.size()-1; j>=i; j--)
          data.remove(j);
      }
      break;

    case BIM_TOBOOLEANARRAY:
      boolean[] boola = new boolean[data.size()];
      for (i=boola.length-1; i>=0; --i)
        boola[i] = ((Variable)data.get(i)).getBoolValue();
      return JudoUtil.toVariable(boola);

    case BIM_TOBYTEARRAY:
      byte[] bytea = new byte[data.size()];
      for (i=bytea.length-1; i>=0; --i)
        bytea[i] = (byte)((Variable)data.get(i)).getLongValue();
      return JudoUtil.toVariable(bytea);

    case BIM_TOCHARARRAY:
      char[] chara = new char[data.size()];
      for (i=chara.length-1; i>=0; --i) {
        String s = ((Variable)data.get(i)).getStringValue();
        chara[i] = (s.length() > 0) ? s.charAt(0) : '\0';
      }
      return JudoUtil.toVariable(chara);

    case BIM_TOSHORTARRAY:
      short[] shorta = new short[data.size()];
      for (i=shorta.length-1; i>=0; --i)
        shorta[i] = (short)((Variable)data.get(i)).getLongValue();
      return JudoUtil.toVariable(shorta);

    case BIM_TOINTARRAY:
      int[] inta = new int[data.size()];
      for (i=inta.length-1; i>=0; --i)
        inta[i] = (int)((Variable)data.get(i)).getLongValue();
      return JudoUtil.toVariable(inta);

    case BIM_TOLONGARRAY:
      long[] longa = new long[data.size()];
      for (i=longa.length-1; i>=0; --i)
        longa[i] = ((Variable)data.get(i)).getLongValue();
      return JudoUtil.toVariable(longa);

    case BIM_TOFLOATARRAY:
      float[] floata = new float[data.size()];
      for (i=floata.length-1; i>=0; --i)
        floata[i] = (float)((Variable)data.get(i)).getDoubleValue();
      return JudoUtil.toVariable(floata);

    case BIM_TODOUBLEARRAY:
      double[] doublea = new double[data.size()];
      for (i=doublea.length-1; i>=0; --i)
        doublea[i] = ((Variable)data.get(i)).getDoubleValue();
      return JudoUtil.toVariable(doublea);

    case BIM_TOBOOLEANOBJECTARRAY:
      Boolean[] Boola = new Boolean[data.size()];
      for (i=Boola.length-1; i>=0; --i)
        Boola[i] = ((Variable)data.get(i)).getBoolValue() ? Boolean.TRUE : Boolean.FALSE;
      return JudoUtil.toVariable(Boola);

    case BIM_TOBYTEOBJECTARRAY:
      Byte[] Bytea = new Byte[data.size()];
      for (i=Bytea.length-1; i>=0; --i)
        Bytea[i] = new Byte((byte)((Variable)data.get(i)).getLongValue());
      return JudoUtil.toVariable(Bytea);

    case BIM_TOCHAROBJECTARRAY:
      Character[] Chara = new Character[data.size()];
      for (i=Chara.length-1; i>=0; --i) {
        String s = ((Variable)data.get(i)).getStringValue();
        Chara[i] = new Character((s.length() > 0) ? s.charAt(0) : '\0');
      }
      return JudoUtil.toVariable(Chara);

    case BIM_TOSHORTOBJECTARRAY:
      Short[] Shorta = new Short[data.size()];
      for (i=Shorta.length-1; i>=0; --i)
        Shorta[i] = new Short((short)((Variable)data.get(i)).getLongValue());
      return JudoUtil.toVariable(Shorta);

    case BIM_TOINTOBJECTARRAY:
      Integer[] Inta = new Integer[data.size()];
      for (i=Inta.length-1; i>=0; --i)
        Inta[i] = new Integer((int)((Variable)data.get(i)).getLongValue());
      return JudoUtil.toVariable(Inta);

    case BIM_TOLONGOBJECTARRAY:
      Long[] Longa = new Long[data.size()];
      for (i=Longa.length-1; i>=0; --i)
        Longa[i] = new Long(((Variable)data.get(i)).getLongValue());
      return JudoUtil.toVariable(Longa);

    case BIM_TOFLOATOBJECTARRAY:
      Float[] Floata = new Float[data.size()];
      for (i=Floata.length-1; i>=0; --i)
        Floata[i] = new Float((float)((Variable)data.get(i)).getDoubleValue());
      return JudoUtil.toVariable(Floata);

    case BIM_TODOUBLEOBJECTARRAY:
      Double[] Doublea = new Double[data.size()];
      for (i=Doublea.length-1; i>=0; --i)
        Doublea[i] = new Double(((Variable)data.get(i)).getDoubleValue());
      return JudoUtil.toVariable(Doublea);

    case BIM_TOSTRINGARRAY:
      return JudoUtil.toVariable(toStringArray());

    case BIM_TOOBJECTARRAY:
      return JudoUtil.toVariable(toObjectArray());
      
    case BIM_TOFIXEDPOSITIONSTRING:
      if (len<1) return this;
      int[] ia = new int[len];
      for (i=0; i<len; i++) ia[i] = (int)params[i].getLongValue();
      return JudoUtil.toVariable(Lib.toFixedPosition(toObjectArray(), ia));

    case BIM_SORT:           sort(params,null); break;
    case BIM_SORT_AS_STRING: sort(null,getBuiltinComparator(params,len,'s')); break;
    case BIM_SORT_AS_NUMBER: sort(null,getBuiltinComparator(params,len,'n')); break;
    case BIM_SORT_AS_DATE:   sort(null,getBuiltinComparator(params,len,'d')); break;

    case BIM_REVERSE:
      len = data.size();
      for (i=len/2-1; i>=0; --i) {
        v = (Variable)data.get(i);
        data.set(i,data.get(len-1-i));
        data.set(len-1-i,v);
      }
      break;

    case BIM_SUM:
      if (data.size() == 0) return ConstInt.ZERO;
      if (areAllInt()) {
        long sum = 0;
        for (i=data.size()-1; i>=0; --i)
          sum += ((Variable)data.get(i)).getLongValue();
        return ConstInt.getInt(sum);
      } else {
        double sum = 0;
        for (i=data.size()-1; i>=0; --i)
          sum += ((Variable)data.get(i)).getDoubleValue();
        return new ConstDouble(sum);
      }

    case BIM_MAX:
      if (data.size() == 0) return ValueSpecial.NEGATIVE_INFINITY;
      if (areAllInt()) {
        long max = ((Variable)data.get(data.size()-1)).getLongValue();
        for (i=data.size()-2; i>=0; --i) {
          long x = ((Variable)data.get(i)).getLongValue();
          if (x > max) max = x;
        }
        return ConstInt.getInt(max);
      } else {
        double max = ((Variable)data.get(data.size()-1)).getDoubleValue();
        for (i=data.size()-2; i>=0; i++) {
          double x = ((Variable)data.get(i)).getDoubleValue();
          if (x > max) max = x;
        }
        return new ConstDouble(max);
      }

    case BIM_MIN:
      if (data.size() == 0) return ValueSpecial.NEGATIVE_INFINITY;
      if (areAllInt()) {
        long min = ((Variable)data.get(data.size()-1)).getLongValue();
        for (i=data.size()-2; i>=0; --i) {
          long x = ((Variable)data.get(i)).getLongValue();
          if (x < min) min = x;
        }
        return ConstInt.getInt(min);
      } else {
        double min = ((Variable)data.get(data.size()-1)).getDoubleValue();
        for (i=data.size()-2; i>=0; --i) {
          double x = ((Variable)data.get(i)).getDoubleValue();
          if (x < min) min = x;
        }
        return new ConstDouble(min);
      }

    case BIM_RANGE:
      if (data.size() == 0) return ConstInt.ZERO;
      ar = new _Array();
      if (areAllInt()) {
        long min = ((Variable)data.get(data.size()-1)).getLongValue();
        long max = min;
        for (i=data.size()-2; i>=0; --i) {
          long x = ((Variable)data.get(i)).getLongValue();
          if (x < min) min = x;
          if (x > max) max = x;
        }
        ar.append(ConstInt.getInt(min));
        ar.append(ConstInt.getInt(max));
      } else {
        double min = ((Variable)data.get(data.size()-1)).getDoubleValue();
        double max = min;
        for (i=data.size()-2; i>=0; --i) {
          double x = ((Variable)data.get(i)).getDoubleValue();
          if (x < min) min = x;
          if (x > max) max = x;
        }
        ar.append(new ConstDouble(min));
        ar.append(new ConstDouble(max));
      }
      return ar;

    case BIM_AVG:
      if (data.size() == 0) return ConstInt.ZERO;
      double sum = 0;
      for (i=data.size()-1; i>=0; --i)
        sum += ((Variable)data.get(i)).getDoubleValue();
      return new ConstDouble(sum / data.size());

    case BIM_SUBARRAY:
      if (len <= 0) break;
      start = (int)params[0].getLongValue();
      end = (len <= 1) ? data.size()
                           : Math.min((int)params[1].getLongValue(), data.size());
      ar = new _Array();
      for (; start < end; start++)
        ar.data.add(data.get(start));
      return ar;

    case BIM_INDEXOF:
    case BIM_LASTINDEXOF:
    case BIM_EXISTS:
      if (len == 0) return (ord==BIM_EXISTS) ? ConstInt.FALSE : ConstInt.MINUSONE;
      Variable[] values = RT.calcValues(params);
      i = -1;
      UserDefinedComparator udop = null;
      v = values[0];
      if (len == 2) {
        if (values[1] instanceof AccessFunction)
          udop = new UserDefinedComparator((AccessFunction)values[1]);
        else
          i = (int)values[1].getLongValue();
      } else if (len > 2) {
        i = (int)values[1].getLongValue();
        udop = new UserDefinedComparator((AccessFunction)values[2]);
      }

      if (ord == BIM_LASTINDEXOF) {
        if (i<0 && i>=data.size())
          i = data.size()-1;
        if (udop != null) {
          for (; i>=0; i--) {
            try { if (udop.compare(v,(Variable)data.get(i)) == 0) break; }
            catch(Exception e) {}
          }
        } else if (v.isNumber()) {
          double val = v.getDoubleValue();
          for (; i>=0; i--) {
            try { if (val == ((Variable)data.get(i)).getDoubleValue()) break; }
            catch(Exception e) {}
          }
        } else if (v.isJava()) {
          Object val = v.getObjectValue();
          for (; i>=0; i--) {
            try { if (val.equals(((Variable)data.get(i)).getObjectValue())) break; }
            catch(Exception e) {}
          }
        } else {
          String val = v.getStringValue();
          for (; i>=0; i--) {
            try { if (val.equals(((Variable)data.get(i)).getStringValue())) break; }
            catch(Exception e) {}
          }
        }
      } else { // BIM_INDEXOF, BIM_EXISTS.
        if (i < 0) i = 0;
        if (udop != null) {
          for (; i<data.size(); i++) {
            try { if (udop.compare(v,(Variable)data.get(i)) == 0) break; }
            catch(Exception e) {}
          }
        } else if (v.isNumber()) {
          double val = v.getDoubleValue();
          for (; i<data.size(); i++) {
            try { if (val == ((Variable)data.get(i)).getDoubleValue()) break; }
            catch(Exception e) {}
          }
        } else if (v.isJava()) {
          Object val = v.getObjectValue();
          for (; i<data.size(); i++) {
            try { if (val.equals(((Variable)data.get(i)).getObjectValue())) break; }
            catch(Exception e) {}
          }
        } else {
          String val = v.getStringValue();
          for (; i<data.size(); i++) {
            try { if (val.equals(((Variable)data.get(i)).getStringValue())) break; }
            catch(Exception e) {}
          }
        }
        if (i >= data.size()) i = -1;
      }
      return (ord==BIM_EXISTS) ? ConstInt.getBool(i>=0) : ConstInt.getInt(i);

    case BIM_REMOVE:
      if (len > 0) {
        try {
          return (Variable)data.remove((int)params[0].getLongValue());
        } catch(ArrayIndexOutOfBoundsException aioob) {}
      }
      return ValueSpecial.UNDEFINED;

    case BIM_CONCAT:
      String sep = ",";
      if (len > 0) sep = params[0].getStringValue();
      StringBuffer sb = new StringBuffer();
      AccessFunction accfxn = len <= 1 ? null : (AccessFunction)params[1].eval();
      Expr[] ea = (accfxn != null) ? new Expr[1] : null;
      start = len >= 2 ? (int)params[1].getLongValue() : 0;
      if (start < 0)
        start = 0;
      end = len >= 3 ? (start + (int)params[3].getLongValue()) : data.size();
      if (end > data.size())
        end = data.size();
      for (i=start; i<end; ++i) {
        if (i > start)
          sb.append(sep);
        v = (Variable)data.get(i);
        if (accfxn != null) {
          ea[0] = v;
          v = RT.call(accfxn.getName(), ea, null);
        }
        if (v != null)
          sb.append(v.getStringValue());
      }
      return JudoUtil.toVariable(sb.toString());

    case BIM_FILTER:
      if (len == 0) break;
      boolean local = (len < 2) ? false : params[1].getBoolValue();
      udop = new UserDefinedComparator();
      udop.setAccessFunction((AccessFunction)params[0].eval());
      vec = new ArrayList();
      for (i=0; i<data.size(); i++) {
        v = (Variable)data.get(i);
        if ( udop.filter(v) )
          vec.add(v);
      }
      if (!local) return new _Array(vec);
      data = vec;
      break;
      
    case BIM_CONVERT:
      if (len == 0) break;
      local = (len < 2) ? false : params[1].getBoolValue();
      udop = new UserDefinedComparator();
      udop.setAccessFunction((AccessFunction)params[0].eval());
      if (!local) {
        vec = new ArrayList();
        for (i=0; i<data.size(); i++)
          vec.add( udop.convert((Variable)data.get(i)) );
        return new _Array(vec);
      }
      for (i=0; i<data.size(); i++)
        data.set( i, udop.convert((Variable)data.get(i)) );
      break;

    default:
      return super.invoke(fxn,params,javaTypes);
    }

    return this;
  }

  public final byte[] toByteArray() throws Throwable {
    byte[] ba = new byte[data.size()];
    for (int i=0; i<ba.length; ++i)
      ba[i] = (byte)((Variable)data.get(i)).getLongValue();
    return ba;
  }

  public final String[] toStringArray() throws Throwable {
    String[] sa = new String[data.size()];
    for (int i=sa.length-1; i>=0; --i)
      sa[i] = ((Variable)data.get(i)).getStringValue();
    return sa;
  }

  public final Object[] toObjectArray() throws Throwable {
    Object[] oa = new Object[data.size()];
    for (int i=oa.length-1; i>=0; --i) {
      Variable elm = (Variable)data.get(i);
      Object o;
      if (elm instanceof _Array)
        o = ((_Array)elm).toObjectArray();
      else if (elm instanceof UserDefined)
        o = ((UserDefined)elm).toMap();
      else
        o = elm.getObjectValue();
      oa[i] = o;
    }
    return oa;
  }

  public final List toObjectList() throws Throwable {
    ArrayList ret = new ArrayList();
    for (int i=data.size()-1; i>=0; --i)
      ret.add( ((Variable)data.get(i)).getObjectValue() );
    return ret;
  }

  Comparator getBuiltinComparator(Expr[] params, int len, char type) throws Throwable {
    boolean asc = true;
    if (len > 0) asc = params[0].getBoolValue();
    switch(type) {
    case 's': return asc ? UserDefinedComparator.theStringComparator
                         : UserDefinedComparator.theDescendingStringComparator;
    case 'd': return asc ? UserDefinedComparator.theDateComparator
                         : UserDefinedComparator.theDescendingDateComparator;
    case 'n': return asc ? UserDefinedComparator.theNumberComparator
                         : UserDefinedComparator.theDescendingNumberComparator;
    default:  return UserDefinedComparator.theNaturalComparator;
    }
  }

  public static final class _LinkedList extends _Array
  {
    public _LinkedList() { super(); data = new LinkedList(); }
    public _LinkedList(LinkedList ll) { super(); data = ll; }
  }

} // end of class _Array.
