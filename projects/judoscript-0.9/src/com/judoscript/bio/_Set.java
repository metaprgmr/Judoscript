/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 10-7-2002  JH   Inception.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript.bio;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.*;
import com.judoscript.*;
import com.judoscript.util.Doublet;
import com.judoscript.util.RangeIterator;


public class _Set extends ObjectInstance implements ExprCollective
{
  Set data;

  public _Set() { super(); data = new HashSet(); }
  public _Set(Set s) { super(); data = s; }

  public Set getStorage() { return data; }

  public final void init(Object inits) throws Throwable {
    if (inits == null) return;
    Expr[] es = null;
    Comparator cptr = null;
    if (inits instanceof Expr[]) {
      es = (Expr[])inits;
    } else if (inits instanceof Doublet) {
      Doublet dblt = (Doublet)inits;
      es = (Expr[])dblt.o2;
      if (dblt.o1 instanceof Comparator)
        cptr = (Comparator)dblt.o1;
      else if (dblt.o1 instanceof Expr) {
        try {
          cptr = new UserDefinedComparator();
          ((UserDefinedComparator)cptr).setAccessFunction((AccessFunction)((Expr)dblt.o1).eval());
        } catch(ClassCastException cce) {
          ExceptionRuntime.rte(RTERR_ILLEGAL_ARGUMENTS, "A comparator function is required.");
        }
      }
    } else super.init(inits); // fail.
    if (data == null) {
      data = (cptr==null) ? (Set)new HashSet() : new TreeSet(cptr);
    } else if (cptr != null) {
      if (!(data instanceof SortedSet) || (((SortedSet)data).comparator() != cptr)) {
        SortedSet ss = new TreeSet(cptr);
        ss.addAll(data);
        data = ss;
      }
    }
    if (es != null) {
      for (int i=0; i<es.length; i++)
        append(es[i].eval().cloneValue());
    }
  }

  public int size() { return data.size(); }

  public final Iterator getIterator(int start, int end, int step, boolean upto, boolean backward) {
    return RangeIterator.getIterator(data.iterator(), start, end, step, upto);
  }

  public final void close() {
    Iterator iter = data.iterator();
    while (iter.hasNext()) {
      Variable var = (Variable)iter.next();
    }
    data.clear();
  }

  public final String getStringValue() throws Throwable {
    StringBuffer sb = new StringBuffer("[");
    boolean first = true;
    Iterator iter = data.iterator();
    while (iter.hasNext()) {
      if (first) first = false;
      else sb.append(",");
      sb.append(((Variable)iter.next()).getStringValue());
    }
    sb.append("]");
    return sb.toString();
  }

  public final int getType() { return TYPE_SET; }
  public final boolean isSet() { return true; }

  public final Object getObjectValue() throws Throwable { return data; }

  public final Variable resolve(int index) throws ExceptionRuntime {
    ExceptionRuntime.rte(RTERR_ILLEGAL_INDEXED_ACCESS,"Sets do not allow indexed accesses.");
    return null;
  }

  public final Variable resolve(Variable idx) throws ExceptionRuntime {
    return resolve(0); // just fail.
  }

  public final Variable resolve(Variable[] idcs) throws Exception {
    return resolve(0); // just fail.
  }

  public final Variable setVariable(int idx, Variable val, int type) throws Exception {
    ExceptionRuntime.rte(RTERR_ILLEGAL_VALUE_SETTING,"Sets do not take indexed value setting.");
    return null;
  }

  public final Variable setVariable(Variable idx, Variable val, int type) throws Exception {
    return setVariable(0,null,0); // just fail.
  }

  public final Variable setVariable(Variable[] idcs, Variable val, int type) throws Exception {
    return setVariable(0,null,0); // just fail.
  }

  public final void removeVariable(String idx) {
  }

  public final void removeVariable(Variable idx) throws ExceptionRuntime {
    ExceptionRuntime.rte(RTERR_ILLEGAL_VALUE_SETTING,"Sets do not allow indexed removal.");
  }

  public Variable resolveVariable(String name) throws Throwable {
    if ("size".equals(name) || "length".equals(name))
      return ConstInt.getInt(data.size());
    return ValueSpecial.UNDEFINED;
  }
  public final Variable setVariable(String name, Variable val, int type) throws Exception {
    return setVariable(0,null,0); // just fail.
  }
  public final void clearVariables() { close(); }

  public final boolean areAllInt() {
    Iterator iter = data.iterator();
    while (iter.hasNext()) {
      if (!((Variable)iter.next()).isInt()) return false;
    }
    return true;
  }

  public final String getTypeName() { return "Set"; }

  public final void append(Variable v) { data.add(v); }

  public final _Array sort(Expr[] params, Comparator cptr) throws Throwable {
    if ((params != null) && params.length > 0) {
      try {
        cptr = new UserDefinedComparator();
        ((UserDefinedComparator)cptr).setAccessFunction(
          (AccessFunction)params[0].eval().cloneValue());
      } catch(ClassCastException cce) {
        ExceptionRuntime.rte(RTERR_ILLEGAL_ARGUMENTS, "A comparator function is required.");
      }
    } else if (cptr == null) {
      cptr = UserDefinedComparator.theNaturalComparator;
    }
    Object[] oa = data.toArray();
    Arrays.sort(oa, cptr);
    _Array ar = new _Array();
    for (int i=oa.length-1; i>=0; i--)
      ar.private_setVariable(i,(Variable)oa[i],0);
    return ar;
  }

  public final Variable invoke(String fxn, Expr[] params, int[] javaTypes) throws Throwable
  {
    int len = (params==null) ? 0 : params.length;
    Iterator iter;
    int i;
    Variable v;
    _Array ar;
    boolean needClose;
    int ord = getMethodOrdinal(fxn);

    switch(ord) {
    case BIM_SIZE:
      return ConstInt.getInt(data.size());

    case BIM_CLEAR:
      checkWritable();
      close();
      break;

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
      iter = data.iterator();
      while (iter.hasNext())
        pw.println(((Variable)iter.next()).getStringValue());
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

    case BIM_GETONE:
      for (iter=data.iterator(); iter.hasNext(); )
        return (Variable)iter.next();
      break;

    case BIM_TOARRAY:
      ar = new _Array();
      iter = data.iterator();
      while (iter.hasNext())
        ar.data.add((Variable)iter.next());
      return ar;
      
    case BIM_TOBOOLEANARRAY:
      boolean[] boola = new boolean[data.size()];
      i=0;
      iter = data.iterator();
      while (iter.hasNext())
        boola[i++] = ((Variable)iter.next()).getBoolValue();
      return JudoUtil.toVariable(boola);

    case BIM_TOBYTEARRAY:
      byte[] bytea = new byte[data.size()];
      i=0;
      iter = data.iterator();
      while (iter.hasNext())
        bytea[i++] = (byte)((Variable)iter.next()).getLongValue();
      return JudoUtil.toVariable(bytea);

    case BIM_TOCHARARRAY:
      char[] chara = new char[data.size()];
      i=0;
      iter = data.iterator();
      while (iter.hasNext()) {
        String s = ((Variable)iter.next()).getStringValue();
        chara[i++] = (s.length() > 0) ? s.charAt(0) : '\0';
      }
      return JudoUtil.toVariable(chara);

    case BIM_TOSHORTARRAY:
      short[] shorta = new short[data.size()];
      i=0;
      iter = data.iterator();
      while (iter.hasNext())
        shorta[i++] = (short)((Variable)iter.next()).getLongValue();
      return JudoUtil.toVariable(shorta);

    case BIM_TOINTARRAY:
      int[] inta = new int[data.size()];
      i=0;
      iter = data.iterator();
      while (iter.hasNext())
        inta[i++] = (int)((Variable)iter.next()).getLongValue();
      return JudoUtil.toVariable(inta);

    case BIM_TOLONGARRAY:
      long[] longa = new long[data.size()];
      i=0;
      iter = data.iterator();
      while (iter.hasNext())
        longa[i++] = ((Variable)iter.next()).getLongValue();
      return JudoUtil.toVariable(longa);

    case BIM_TOFLOATARRAY:
      float[] floata = new float[data.size()];
      i=0;
      iter = data.iterator();
      while (iter.hasNext())
        floata[i++] = (float)((Variable)iter.next()).getDoubleValue();
      return JudoUtil.toVariable(floata);

    case BIM_TODOUBLEARRAY:
      double[] doublea = new double[data.size()];
      i=0;
      iter = data.iterator();
      while (iter.hasNext())
        doublea[i++] = ((Variable)iter.next()).getDoubleValue();
      return JudoUtil.toVariable(doublea);

    case BIM_TOBOOLEANOBJECTARRAY:
      Boolean[] Boola = new Boolean[data.size()];
      i=0;
      iter = data.iterator();
      while (iter.hasNext())
        Boola[i++] = ((Variable)iter.next()).getBoolValue() ? Boolean.TRUE : Boolean.FALSE;
      return JudoUtil.toVariable(Boola);

    case BIM_TOBYTEOBJECTARRAY:
      Byte[] Bytea = new Byte[data.size()];
      i=0;
      iter = data.iterator();
      while (iter.hasNext())
        Bytea[i++] = new Byte((byte)((Variable)iter.next()).getLongValue());
      return JudoUtil.toVariable(Bytea);

    case BIM_TOCHAROBJECTARRAY:
      Character[] Chara = new Character[data.size()];
      i=0;
      iter = data.iterator();
      while (iter.hasNext()) {
        String s = ((Variable)iter.next()).getStringValue();
        Chara[i++] = new Character((s.length() > 0) ? s.charAt(0) : '\0');
      }
      return JudoUtil.toVariable(Chara);

    case BIM_TOSHORTOBJECTARRAY:
      Short[] Shorta = new Short[data.size()];
      i=0;
      iter = data.iterator();
      while (iter.hasNext())
        Shorta[i++] = new Short((short)((Variable)iter.next()).getLongValue());
      return JudoUtil.toVariable(Shorta);

    case BIM_TOINTOBJECTARRAY:
      Integer[] Inta = new Integer[data.size()];
      i=0;
      iter = data.iterator();
      while (iter.hasNext())
        Inta[i++] = new Integer((int)((Variable)iter.next()).getLongValue());
      return JudoUtil.toVariable(Inta);

    case BIM_TOLONGOBJECTARRAY:
      Long[] Longa = new Long[data.size()];
      i=0;
      iter = data.iterator();
      while (iter.hasNext())
        Longa[i++] = new Long(((Variable)iter.next()).getLongValue());
      return JudoUtil.toVariable(Longa);

    case BIM_TOFLOATOBJECTARRAY:
      Float[] Floata = new Float[data.size()];
      i=0;
      iter = data.iterator();
      while (iter.hasNext())
        Floata[i++] = new Float((float)((Variable)iter.next()).getDoubleValue());
      return JudoUtil.toVariable(Floata);

    case BIM_TODOUBLEOBJECTARRAY:
      Double[] Doublea = new Double[data.size()];
      i=0;
      iter = data.iterator();
      while (iter.hasNext())
        Doublea[i++] = new Double(((Variable)iter.next()).getDoubleValue());
      return JudoUtil.toVariable(Doublea);

    case BIM_TOSTRINGARRAY:
      String[] sa = new String[data.size()];
      i=0;
      iter = data.iterator();
      while (iter.hasNext())
        sa[i++] = ((Variable)iter.next()).getStringValue();
      return JudoUtil.toVariable(sa);

    case BIM_TOOBJECTARRAY:
      Object[] oa = new Object[data.size()];
      i=0;
      iter = data.iterator();
      while (iter.hasNext())
        oa[i++] = ((Variable)iter.next()).getObjectValue();
      return JudoUtil.toVariable(oa);
      
    case BIM_TOJAVASET:
      Set s = new HashSet();
      iter = data.iterator();
      while (iter.hasNext())
        s.add(((Variable)iter.next()).getObjectValue());
      return JudoUtil.toVariable(s);
      
    case BIM_SORT:           return sort(params,null);
    case BIM_SORT_AS_STRING: return sort(null,getBuiltinComparator(params,len,'s'));
    case BIM_SORT_AS_NUMBER: return sort(null,getBuiltinComparator(params,len,'n'));
    case BIM_SORT_AS_DATE:   return sort(null,getBuiltinComparator(params,len,'d'));

    case BIM_SUM:
      if (data.size() == 0) return ValueSpecial.UNDEFINED;
      iter = data.iterator();
      if (!iter.hasNext()) return ValueSpecial.UNDEFINED;
      if (areAllInt()) {
        double sum = 0;
        while (iter.hasNext())
          sum += ((Variable)iter.next()).getDoubleValue();
        return new ConstDouble(sum);
      } else {
        long sum = 0;
        while (iter.hasNext())
          sum += ((Variable)iter.next()).getLongValue();
        return ConstInt.getInt(sum);
      }

    case BIM_MAX:
      if (data.size() == 0) return ValueSpecial.UNDEFINED;
      iter = data.iterator();
      if (!iter.hasNext()) return ValueSpecial.UNDEFINED;
      if (areAllInt()) {
        double max = ((Variable)iter.next()).getDoubleValue();
        while (iter.hasNext()) {
          double x = ((Variable)iter.next()).getDoubleValue();
          if (x > max) max = x;
        }
        return new ConstDouble(max);
      } else {
        long max = ((Variable)iter.next()).getLongValue();
        while (iter.hasNext()) {
          long x = ((Variable)iter.next()).getLongValue();
          if (x > max) max = x;
        }
        return ConstInt.getInt(max);
      }

    case BIM_MIN:
      if (data.size() == 0) return ValueSpecial.UNDEFINED;
      iter = data.iterator();
      if (!iter.hasNext()) return ValueSpecial.UNDEFINED;
      if (areAllInt()) {
        double min = ((Variable)iter.next()).getDoubleValue();
        while (iter.hasNext()) {
          double x = ((Variable)iter.next()).getDoubleValue();
          if (x < min) min = x;
        }
        return new ConstDouble(min);
      } else {
        long min = ((Variable)iter.next()).getLongValue();
        while (iter.hasNext()) {
          long x = ((Variable)iter.next()).getLongValue();
          if (x < min) min = x;
        }
        return ConstInt.getInt(min);
      }

    case BIM_RANGE:
      ar = new _Array();
      if (data.size() == 0) return ar;
      iter = data.iterator();
      if (!iter.hasNext()) return ar;
      if (areAllInt()) {
        double min = ((Variable)iter.next()).getDoubleValue();
        double max = min;
        while (iter.hasNext()) {
          double x = ((Variable)iter.next()).getDoubleValue();
          if (x < min) min = x;
          if (x > max) max = x;
        }
        ar.append(new ConstDouble(min));
        ar.append(new ConstDouble(max));
      } else {
        long min = ((Variable)iter.next()).getLongValue();
        long max = min;
        while (iter.hasNext()) {
          long x = ((Variable)iter.next()).getLongValue();
          if (x < min) min = x;
          if (x > max) max = x;
        }
        ar.append(ConstInt.getInt(min));
        ar.append(ConstInt.getInt(max));
      }
      return ar;

    case BIM_AVG:
      if (data.size() == 0) return ConstInt.ZERO;
      double sum = 0;
      iter = data.iterator();
      while (iter.hasNext())
        sum += ((Variable)iter.next()).getDoubleValue();
      return new ConstDouble(sum / data.size());

    case BIM_EXISTS:
      if (len == 0) return ConstInt.FALSE;
      Variable[] values = RT.calcValues(params);
      UserDefinedComparator udop = null;
      v = values[0];
      if (len > 1)
        udop = new UserDefinedComparator((AccessFunction)values[1]);

      if (udop == null) {
        return ConstInt.getBool(data.contains(v));
      } else {
        iter = data.iterator();
        while (iter.hasNext()) {
          try { if (udop.compare(v,(Variable)iter.next()) == 0) return ConstInt.TRUE; }
          catch(Exception e) {}
        }
        return ConstInt.FALSE;
      }

    case BIM_REMOVE:
      for (i=0; i<len; i++)
        data.remove(params[i].eval());
      return ValueSpecial.UNDEFINED;

    case BIM_APPEND:
      for (i=0; i<len; i++)
        append(params[i].eval().cloneValue());
      return ValueSpecial.UNDEFINED;

    case BIM_CONCAT:
      String sep = ",";
      if (len > 0) sep = params[0].getStringValue();
      StringBuffer sb = new StringBuffer();
      AccessFunction accfxn = len <= 1 ? null : (AccessFunction)params[1].eval();
      Expr[] ea = (accfxn != null) ? new Expr[1] : null;
      iter = data.iterator();
      boolean first = true;
      while (iter.hasNext()) {
        if (first) first = false;
        else sb.append(sep);
        v = (Variable)iter.next();
        if (accfxn != null) {
          ea[0] = v;
          v = RT.call(accfxn.getName(), ea, null);
        }
        sb.append(v.getStringValue());
      }
      return JudoUtil.toVariable(sb.toString());

    case BIM_SUBSET:
      if (len == 0) break;
      udop = new UserDefinedComparator();
      udop.setAccessFunction((AccessFunction)params[0].eval());
      Set set;
      if (data instanceof SortedSet)
        set = new TreeSet(((SortedSet)data).comparator());
      else
        set = new HashSet();
      iter = data.iterator();
      while (iter.hasNext()) {
        v = (Variable)iter.next();
        if ( udop.filter(v) )
          set.add(v);
      }
      return new _Set(set);
      
    default:
      return super.invoke(ord,fxn,params);
    }

    return this;
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

} // end of class _Set.
