/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 03-17-2002  JH   Initial open source release.
 * 05-30-2002  JH   Added empty constructor; plan to add save() and load().
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript.util;

import java.sql.*;
import java.util.*;


public class TableData
{
  boolean   titleCaseSensitive = true;
  String[]  titles;
  ArrayList data;

  /**
   *@limit if <= 0, is defaulted to 10M.
   */
  public TableData(ResultSet rs, boolean caseSens, int limit) throws SQLException {
    titleCaseSensitive = caseSens;
    data = new ArrayList();
    ResultSetMetaData rsmd = rs.getMetaData();
    titles = new String[rsmd.getColumnCount()];
    int i;
    for (i=titles.length-1; i>=0; --i)
      titles[i] = rsmd.getColumnName(i+1);
    if (limit <= 0) limit = 10 * 1024 * 1024;
    while (rs.next()) {
      Object[] oa = new Object[titles.length];
      for (i=titles.length-1; i>=0; --i)
        oa[i] = rs.getObject(i+1);
      data.add(oa);
    }
  }

  /**
   *@param titles each name must be unique.
   */
  public TableData(String[] titles, boolean caseSens) {
    titleCaseSensitive = caseSens;
    data = new ArrayList();
    this.titles = new String[titles.length];
    System.arraycopy(titles,0,this.titles,0,titles.length);
  }

  /**
   *@see load()
   */
  public TableData() { titles = new String[0]; data = null; }

  public String[] getTitles() { return titles; }

  public String getTitle(int col) {
    try { return titles[col]; } catch(Exception e) { return null; }
  }

  public void setTitleCaseSensitive(boolean set) { titleCaseSensitive = set; }

  public TableData cloneEmpty() { return new TableData(titles,titleCaseSensitive); }

  /**
   * If newtitles has fewer columns than the current titles,
   * they are copied starting from the first column;
   * if more, the extra ones are discarded.
   */
  public void setTitles(String[] newtitles) {
    if (titles == null) {
      titles = new String[newtitles.length];
      System.arraycopy(newtitles,0,titles,0,titles.length);
      return;
    }
    for (int i=0; i<newtitles.length; ++i) {
      if (i>=titles.length) break;
      titles[i] = newtitles[i];
    }
  }

  /**
   *@param col is 0-based.
   */
  public void setTitleAt(int col, String name) {
    if (col < titles.length) titles[col] = name;
  }

  public int size() { return data.size(); }
  public int lastIndex() { return data.size()-1; }
  public void clear() { data.clear(); }

  public int[] getColumnMaxWidths() {
    int[] ret = new int[titles.length];
    for (int i=data.size()-1; i>=0; --i) {
      for (int j=0; j<ret.length; ++j) {
        try {
          int len = data.get(i).toString().length();
          if (len > ret[j]) ret[j] = len;
        } catch(Exception e) {}
      }
    }
    return ret;
  }

  /**
   *@return an array of rows (arrays). Never null.
   */
  public Object[][] getData() {
    Object[][] oaa = new Object[data.size()][];
    for (int i=data.size()-1; i>=0; --i)
      oaa[i] = (Object[])data.get(i);
    return oaa;
  }

  public Object[] getRow(int row) {
    if (row < 0)
      return null;
    if (row >= data.size())
      try { setAt(row,0,null); } catch(Exception e) {}
    Object[] oa = (Object[])data.get(row);
    if (oa == null) {
      oa = new Object[titles.length];
      setRowNoCopy(row,oa);
    }
    return oa;
  }

  /**
   * The elements are copied.
   *
   *@param if row.length > titles.length, take only titles.length elements;<br>
   *       if row.length < titles.length, the missing ones are null.
   */
  public final void addRow(Object[] row) {
    Object[] oa = new Object[titles.length];
    for (int i=0; i<oa.length; i++)
      oa[i] = (i<row.length) ? row[i] : null;
    data.add(oa);
  }

  /**
   * Does not create new array if not necessary.
   */
  public final void addRowNoCopy(Object[] row) {
    if (row.length >= titles.length) {
      data.add(row);
    } else {
      Object[] oa = new Object[titles.length];
      System.arraycopy(row,0,oa,0,row.length);
      for (int i=row.length; i<titles.length; oa[i++]=null);
      data.add(oa);
    }
  }

  /** convenience */
  public final void addRow(Object o1, Object o2) {
    addRowNoCopy(new Object[]{o1,o2});
  }

  /** convenience */
  public final void addRow(Object o1, Object o2, Object o3) {
    addRowNoCopy(new Object[]{o1,o2,o3});
  }

  /** convenience */
  public final void addRow(Object o1, Object o2, Object o3, Object o4) {
    addRowNoCopy(new Object[]{o1,o2,o3,o4});
  }

  /** convenience */
  public final void addRow(Object o1, Object o2, Object o3, Object o4, Object o5) {
    addRowNoCopy(new Object[]{o1,o2,o3,o4,o5});
  }

  public int getColumnCount() { return titles.length; }

  /**
   *@return the column index is 0-based.
   */
  public int getColumnIndex(String col) {
    int i;
    if (titleCaseSensitive) {
      for (i=0; i<titles.length; ++i)
       if (titles[i].equals(col))
         return i;
    } else {
      for (i=0; i<titles.length; ++i)
       if (titles[i].equalsIgnoreCase(col))
         return i;
    }
    return -1;
  }

  /**
   *@param the column index is 0-based.
   */
  public Object[] getColumn(int idx) {
    if (idx >= titles.length)
      return null;
    Object[] oa = new Object[data.size()];
    for (int i=data.size()-1; i>=0; --i) {
      Object[] x = ((Object[])data.get(i));
      oa[i] = (x==null) ? null : x[idx];
    }
    return oa;
  }

  public Object[] getColumn(String col) {
    return getColumn(getColumnIndex(col));
  }

  /**
   *@param idx 0-based column index
   *@param row 0-based row index
   */
  public Object getAt(int row, int col) {
    if (col >= titles.length) return null;
    if (row >= data.size()) return null;
    Object[] oa = (Object[])data.get(row);
    return (oa!=null) ? oa[col] : null;
  }

  public Object getAt(int row, String col) {
    return getAt(row,getColumnIndex(col));
  }

  /**
   * Sets the value at a specific cell. If row is beyond current row number,
   * a new row is inserted; the rows between the last one and this new one
   * are <em>not</em> initialized.
   *
   *@param idx 0-based column index
   *@param row 0-based row index
   */
  public void setAt(int row, int col, Object val) {
    if (col >= titles.length)
      return;
    if (row < data.size()) {
      Object[] oa = (Object[])data.get(row);
      if (oa == null) {
        oa = new Object[titles.length];
        data.set(row, oa);
      }
      oa[col] = val;
    } else {
      Lib.ensureSize(data, row+1);
      Object[] oa = new Object[titles.length];
      data.set(row, oa);
      oa[col] = val;
    }
  }

  public void setAt(int row, String col, Object val) { setAt(row,getColumnIndex(col),val); }
  public void setAt(int row, int col, boolean b)     { setAt(row,col,b?Boolean.TRUE:Boolean.FALSE); }
  public void setAt(int row, String col, boolean b)  { setAt(row,col,b?Boolean.TRUE:Boolean.FALSE); }
  public void setAt(int row, int col, byte b)        { setAt(row,col,new Byte(b)); }
  public void setAt(int row, String col, byte b)     { setAt(row,col,new Byte(b)); }
  public void setAt(int row, int col, char c)        { setAt(row,col,new Character(c)); }
  public void setAt(int row, String col, char c)     { setAt(row,col,new Character(c)); }
  public void setAt(int row, int col, short s)       { setAt(row,col,new Short(s)); }
  public void setAt(int row, String col, short s)    { setAt(row,col,new Short(s)); }
  public void setAt(int row, int col, int i)         { setAt(row,col,new Integer(i)); }
  public void setAt(int row, String col, int i)      { setAt(row,col,new Integer(i)); }
  public void setAt(int row, int col, long i)        { setAt(row,col,new Long(i)); }
  public void setAt(int row, String col, long i)     { setAt(row,col,new Long(i)); }
  public void setAt(int row, int col, float d)       { setAt(row,col,new Float(d)); }
  public void setAt(int row, String col, float d)    { setAt(row,col,new Float(d)); }
  public void setAt(int row, int col, double d)      { setAt(row,col,new Double(d)); }
  public void setAt(int row, String col, double d)   { setAt(row,col,new Double(d)); }

  public final void setRowNoCopy(int row, Object[] values) { data.set(row, values); }

  public void setRow(int row, Object[] values) {
    for (int i=0; i<titles.length; i++) {
      try { setAt(row, i, (i < values.length) ? values[i] : null); }
      catch(IllegalArgumentException ire) { /* doesn't happen */ }
    }
  }

  public TableData filterNotNullRows() {
    TableData td = cloneEmpty();
    for (int i=0; i<data.size(); ++i) {
      Object[] oa = (Object[])data.get(i);
      if (oa != null) {
        boolean good = false;
        for (int j=0; j<titles.length; j++) {
          if (oa[j] != null) {
            good = true;
            break;
          }
        }
        if (good) td.addRowNoCopy(oa);
      }
    }
    return td;
  }

  public TableData filterByColumnNotNull(int col) {
    if (col < 0 || col >= titles.length)
      return null;

    TableData td = cloneEmpty();
    for (int i=0; i<data.size(); ++i) {
      Object[] oa = (Object[])data.get(i);
      if (oa != null && oa[col] != null)
        td.addRowNoCopy(oa);
    }
    return td;
  }

  public String toString() {
    StringBuffer sb = new StringBuffer("{[");
    for (int i=0; i<titles.length; i++) {
      if (i>0) sb.append(',');
      sb.append(titles[i]);
    }
    sb.append("]");
    int len = data==null ? 0 : data.size();
    for (int i=0; i<len; ++i) {
      Object[] oa = (Object[])data.get(i);
      sb.append('(');
      if (oa != null) {
        for (int j=0; j<oa.length; ++j) {
          if (j>0) sb.append(',');
          sb.append(oa[j]);
        }
      }
      sb.append(')');
    }
    sb.append('}');
    return sb.toString();
  }

} // end of class TableData.
