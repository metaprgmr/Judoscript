/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 03-17-2002  JH   Initial open source release.
 * 06-23-2002  JH   Added isA() support.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript.bio; 

import java.sql.ResultSet;
import java.util.*;
import com.judoscript.*;
import com.judoscript.util.*;


public class _TableData extends JavaObject implements ExprTableData
{
  /** External */
  public _TableData() { super(new TableData()); }

  /** Internal */
  public _TableData(TableData td) { super(td); }

  /** Internal */
  public _TableData(ResultSet rs, boolean caseSens, int limit) throws Exception {
    super(new TableData(rs,caseSens,limit));
  }

  /** Internal */
  public _TableData(String[] titles, boolean caseSens) throws Exception {
    this(new TableData(titles,caseSens));
  }

  /** inits are titles */
  public void init(Object inits) throws Throwable {
    if ((inits != null) && (inits instanceof Expr[])) {
      try {
        Expr[] ea = (Expr[])inits;
        String[] sa = new String[ea.length];
        for (int i=sa.length-1; i>=0; --i)
          sa[i] = ea[i].getStringValue();
        setObject(new TableData(sa,true));
      } catch(Exception e) {
        ExceptionRuntime.rte(RTERR_OBJECT_INIT, "Invalid object initialization", e);
      }
    } else {
      super.init(inits); // in ObjectInstance.
    }
  }

  public String getTypeName() { return "TableData"; }

  /////////////////////////////////////////////////////////////
  // ExprTableData method
  //
  private int rowIndex = -1;

  public boolean nextRow() throws Exception {
    int sz = ((TableData)object).size();
    return rowIndex >= sz ? false : ++rowIndex < sz;
  }

  public ExprCollective currentRow() throws Exception {
    return new DataRow((TableData)object,rowIndex);
  }

  /////////////////////////////////////////////////////////////
  // ExprCollective methods
  //
  public final Variable resolve(String key) throws Exception { return ValueSpecial.UNDEFINED; }
  public final void removeVariable(Variable key) throws Exception {}

  public final Variable resolve(Variable key) throws Throwable {
    return new DataRow((TableData)object, (int)key.getLongValue());
  }

  public final Variable resolve(Variable[] keys) throws Throwable {
    switch (keys.length) {
    default: ExceptionRuntime.rte(RTERR_ILLEGAL_ARGUMENTS,"tableData is two-dimensional.");
    case 0:  return ValueSpecial.UNDEFINED;
    case 1:  return resolve(keys[0]);
    case 2:  break; // see next
    }
    TableData td = (TableData)object;
    int row = (int)keys[0].getLongValue();
    int col = keys[1].isNumber() ? (int)keys[1].getLongValue()
                                 : td.getColumnIndex(keys[1].getStringValue());
    return JudoUtil.toVariable(td.getAt(row,col));
  }

  public final Variable setVariable(Variable[] keys, Variable var, int type) throws Throwable {
    switch (keys.length) {
    default: ExceptionRuntime.rte(RTERR_ILLEGAL_ARGUMENTS,"tableData is two-dimensional.");
    case 0:  return ValueSpecial.UNDEFINED;
    case 1:  return setVariable(keys[0],var,type);
    case 2:  break; // see next
    }
    TableData td = (TableData)object;
    int row = (int)keys[0].getLongValue();
    int col = keys[1].isNumber() ? (int)keys[1].getLongValue()
                                    : td.getColumnIndex(keys[1].getStringValue());
    td.setAt(row,col,var.getObjectValue());
    return var;
  }

  public final Variable setVariable(Variable name, Variable val, int type) throws Throwable {
    Object[] oa = null;
    if (val instanceof _Array) {
      List l = ((_Array)val).getStorage();
      oa = new Object[l.size()];
      for (int i=oa.length-1; i>=0; --i)
        oa[i] = l.get(i);
    } else {
      Object o = val.getObjectValue();
      if (!(o instanceof Object[]))
        ExceptionRuntime.rte(RTERR_ILLEGAL_ARGUMENTS,"tableData rows must be Object[].");
      oa = (Object[])o;
    }
    ((TableData)object).setRow((int)name.getLongValue(), oa);
    return val;
  }

  public Variable invoke(String fxn, Expr[] params, int[] javaTypes) throws Throwable
  {
    if (fxn.startsWith("sort")) return sort(fxn,params);
    if (fxn.startsWith("filter")) return filter(fxn,params);

    Variable v;
    int i;
    int len = (params==null) ? 0 : params.length;
    switch(getMethodOrdinal(fxn)) {
    case BIM_APPEND:
    case BIM_ADDROW:
      TableData td = (TableData)object;
      if (len == 1) {
        v = params[0].eval();
        if (v instanceof _Array) {
          List l = ((_Array)v).getStorage();
          Object[] oa = new Object[l.size()];
          for (i=oa.length-1; i>=0; --i)
            oa[i] = l.get(i);
          td.addRowNoCopy(oa);
          return this;
        } else if (v instanceof JavaArray) {
          td.addRow( ((JavaArray)v).getObjectArrayValue() );
          return this;
        }
      }
      if (len > 0) {
        Object[] oa = new Object[ td.getColumnCount() ];
        for (i=0; i<oa.length; ++i)
          oa[i] = (i>=len) ? null : params[i].eval().getObjectValue();
        td.addRowNoCopy(oa);
      }
      return this;

    case BIM_SETROW:
      if (len <= 1) return ValueSpecial.UNDEFINED;
      int row = (int)params[0].getLongValue();
      Object[] oa;
      v = params[1].eval();
      if (v instanceof _Array) {
        oa = ((_Array)v).toObjectArray();
      } else if (v instanceof JavaArray) {
        oa = (Object[])v.getObjectValue();
      } else {
        oa = new Object[len-1];
        for (i=1; i<len; ++i)
          oa[i-1] = params[i].getObjectValue();
      }
      ((TableData)object).setRow(row,oa);
      return ValueSpecial.UNDEFINED;

    case BIM_SETTITLES:
      if (len <= 0) return ValueSpecial.UNDEFINED;
      v = params[0].eval();
      String[] sa;
      if (v instanceof _Array) {
        sa = ((_Array)v).toStringArray();
      } else if (v instanceof JavaArray) {
        if (v.getObjectValue() instanceof String[])
          sa = (String[])v.getObjectValue();
        else {
          oa = (Object[])v.getObjectValue();
          sa = new String[oa.length];
          for (i=0; i<sa.length; ++i)
            sa[i] = oa[i]==null ? null : oa[i].toString();
        }
      } else {
        sa = new String[len];
        for (i=0; i<len; ++i)
          sa[i] = params[i].getStringValue();
      }
      ((TableData)object).setTitles(sa);
      return ValueSpecial.UNDEFINED;

    default: return super.invoke(fxn,params,javaTypes);
    }
  }

  /** returns self */
  Variable sort(String fxn, Expr[] params) throws Throwable {
    TableData td = (TableData)object;
    if (td == null) return this;

    int len = (params==null) ? 0 : params.length;
    if (fxn.equals("sort") && len==0)
      ExceptionRuntime.badParams("sort", "comparator function");

    int col = -1;
    int idx = 0;
    Comparator cptr = UserDefinedComparator.theNaturalComparator;

    if (fxn.startsWith("sortByColumn")) {
      if (len<=0) col = 0;
      else col = (int)params[idx++].getLongValue();
      boolean asc = true;
      if (fxn.endsWith("AsNumber")) {
        if (len > 1) asc = params[1].getBoolValue();
        cptr = asc ? UserDefinedComparator.theNumberComparator
                   : UserDefinedComparator.theDescendingNumberComparator;
        ++idx;
      } else if (fxn.endsWith("AsDate")) {
        if (len > 1) asc = params[1].getBoolValue();
        cptr = asc ? UserDefinedComparator.theDateComparator
                   : UserDefinedComparator.theDescendingDateComparator;
        ++idx;
      } else if (fxn.endsWith("AsString")) {
        if (len > 1) asc = params[1].getBoolValue();
        cptr = asc ? UserDefinedComparator.theStringComparator
                   : UserDefinedComparator.theDescendingStringComparator;
        ++idx;
      }
    }
    if (idx <= len-1) { // has comparator
      try {
        cptr = new UserDefinedComparator();
        ((UserDefinedComparator)cptr).setAccessFunction((AccessFunction)params[idx].eval().cloneValue());
      } catch(ClassCastException cce) {
        ExceptionRuntime.rte(RTERR_ILLEGAL_ARGUMENTS, "A comparator function is required.");
        return null; // to please javac.
      }
    }

    int i;

    if (col < 0 || col >= td.getColumnCount()) {
      // sort on rows.
      DataRow[] dra = new DataRow[td.size()];
      for (i=dra.length-1; i>=0; --i)
        dra[i] = new DataRow(td,i);
      Arrays.sort(dra, cptr);
      for (i=dra.length-1; i>=0; --i)
        td.setRowNoCopy(i,dra[i].getRow());
    } else {
      // sort by a column
      ColumnInRow[] cra = new ColumnInRow[td.size()];
      for (i=cra.length-1; i>=0; --i) {
        Object[] oa = td.getRow(i);
        cra[i] = new ColumnInRow(oa[col], i, oa);
      }
      Arrays.sort(cra, cptr);
      for (i=cra.length-1; i>=0; --i)
        td.setRowNoCopy(i,cra[i].arr);
    }
    return this;
  }

  Variable filter(String fxn, Expr[] params) throws Throwable {
    TableData td = (TableData)object;
    int len = (params==null) ? 0 : params.length;
    int col = -1;
    int idx = 0;
    if (fxn.endsWith("NotNull")) {
      if (fxn.endsWith("ByColumnNotNull")) {
        col = (len==0) ? 0 : (int)params[0].getLongValue();
        td = td.filterByColumnNotNull(col);
      } else {
        td = td.filterNotNullRows();
      }
      return new _TableData(td);
    }

    if (fxn.endsWith("ByColumn")) {
      col = (len==0) ? 0 : (int)params[0].getLongValue();
      ++idx;
    }
    if (idx >= len) return ValueSpecial.UNDEFINED;

    int i;
    TableData ret = td.cloneEmpty();
    UserDefinedComparator fltr = null;
    if (fltr == null) {
      try {
        fltr = new UserDefinedComparator();
        fltr.setAccessFunction((AccessFunction)params[0].eval().cloneValue());
      } catch(ClassCastException cce) {
        ExceptionRuntime.rte(RTERR_ILLEGAL_ARGUMENTS, "A filter function is required.");
      }
    }

    if (col < 0 || col >= td.getColumnCount()) {
      // filter on row
      for (i=0; i<td.size(); i++) {
        if (fltr.filter(new DataRow(td,i)))
          ret.addRowNoCopy(td.getRow(i));
      }
    } else {
      // filter by a column
      for (i=0; i<td.size(); i++) {
        Object[] oa = td.getRow(i);
        if (fltr.filter(JudoUtil.toVariable(oa[col])))
          ret.addRowNoCopy(oa);
      }
    }
    return new _TableData(ret);
  }

  /////////////////////////////////////////////////////////////
  // Context methods.
  //
  public final boolean hasVariable(String name) { return false; }
  public final Variable resolveVariable(String name) throws Throwable {
    if (name.equals("length"))
      return ConstInt.getInt(((TableData)object).size());
    return ValueSpecial.UNDEFINED;
  }
  public final Variable setVariable(String name, String val, int type) throws Exception {
    ExceptionRuntime.rte(RTERR_ILLEGAL_VALUE_SETTING,"tableData can't set named value.");
    return null; // never reached.
  }
  public Variable setVariable(String name, Variable val, int type) throws Exception {
    ExceptionRuntime.rte(RTERR_ILLEGAL_VALUE_SETTING,"tableData can't set named value.");
    return null; // never reached.
  }
  public void removeVariable(String name) {}
  public void clearVariables() { ((TableData)object).clear(); }
  public void close() { clearVariables(); }

  /////////////////////////////////////////////////////////////
  // Class of DataRow
  //
  static final class DataRow extends JavaArray.ObjectArray implements ExprCollective
  {
    TableData tdata;
 
    DataRow(TableData td, int row) throws Exception {
      super(td.getRow(row),1);
      tdata = td;
    }

    Object[] getRow() { return (Object[])object; }

    public Variable resolveVariable(String fld) throws Throwable {
      if (fld.equals("length"))
        return ConstInt.getInt(tdata.getColumnCount());
      try { return JudoUtil.toVariable(getRow()[tdata.getColumnIndex(fld)]); }
      catch(Exception e) { return ValueSpecial.UNDEFINED; }
    }

    public Variable resolve(Variable idx) throws Throwable {
      if (idx.isNumber())
        return JudoUtil.toVariable(getRow()[(int)idx.getLongValue()]);
      return resolveVariable(idx.getStringValue());
    }

    public Variable resolve(Variable[] dims) throws Throwable {
      if (dims.length != 1)
        ExceptionRuntime.rte(RTERR_ILLEGAL_VALUE_SETTING, "Row data is one-dimensional.");
      return resolve(dims[0]);
    }

    public Variable setVariable(Variable idx, Variable val, int type) throws Throwable {
      int col = (idx.isNumber()) ? (int)idx.getLongValue()
                                    : tdata.getColumnIndex(idx.getStringValue());
      getRow()[col] = val.getObjectValue();
      return val;
    }

    public final Variable setVariable(String name, Variable val, int type) throws Throwable {
      int col = tdata.getColumnIndex(name);
      getRow()[col] = val.getObjectValue();
      return val;
    }

    public Variable setVariable(Variable[] dims, Variable val, int type) throws Throwable {
      if (dims.length != 1)
        ExceptionRuntime.rte(RTERR_ILLEGAL_VALUE_SETTING, "Row data is one-dimensional.");
      return setVariable(dims[0],val,type);
    }

  } // end of inner class DataRow.


  /////////////////////////////////////////////////////////////
  // Class of ColumnInRow
  //

  static final class ColumnInRow extends VariableAdapter
  {
    Variable v;
    int      index;
    Object[] arr;

    ColumnInRow(Object val, int idx, Object[] a) throws Exception {
      v = JudoUtil.toVariable(val);
      index = idx;
      arr = a;
    }

    // Variable
    public String  getStringValue() throws Throwable { return v.getStringValue(); }
    public boolean getBoolValue() throws Throwable { return v.getBoolValue(); }
    public long getLongValue() throws Throwable { return v.getLongValue(); }
    public double  getDoubleValue() throws Throwable { return v.getDoubleValue(); }
    public Object getObjectValue() throws Throwable { return v.getObjectValue(); }
    public boolean isValue() { return v.isValue(); }
    public Variable cloneValue() { return this; }
    public java.util.Date getDateValue() throws Throwable { return v.getDateValue(); }
    public java.sql.Date getSqlDate() throws Throwable { return v.getSqlDate(); }
    public java.sql.Time getSqlTime() throws Throwable { return v.getSqlTime(); }
    public java.sql.Timestamp getSqlTimestamp() throws Throwable { return v.getSqlTimestamp(); }
    public Variable invoke(String fxn, Expr[] params, int[] javaTypes) throws Throwable {
      return v.invoke(fxn,params,javaTypes);
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
    public boolean isA(String name) { return v.isA(name); }
    public boolean isReadOnly() { return v.isReadOnly(); }
    public Variable eval() throws Throwable { return v.eval(); }
    public Expr reduce(Stack stack) { return v.reduce(stack); }
    public Expr optimize() { return this; }
    public void dump(XMLWriter out) { v.dump(out); }

  } // end of inner class ColumnInRow.

} // end of class _TableData.
