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


package com.judoscript.db;

import java.sql.*;
import com.judoscript.*;
import com.judoscript.bio.JavaObject;
import com.judoscript.bio._TableData;
import com.judoscript.bio._Array;
import com.judoscript.util.Lib;
import com.judoscript.util.TableDump;
import com.judoscript.util.XMLWriter;


public final class QueryResult extends JavaObject implements DBResult, ExprTableData 
{
  DBHandle handle;
  public PreparedStatement pstmt;
  public ResultSetMetaData rsmd;

  public QueryResult(ResultSet rs) throws Exception {
    super(ResultSet.class, rs);
    handle = null;
    pstmt = null;
    rsmd = rs.getMetaData();
  }

  public QueryResult(DBHandle hndl, PreparedStatement ps, ResultSet rs, ResultSetMetaData md) {
    super(ResultSet.class, rs);
    handle = hndl;
    pstmt = ps;
    rsmd = md;
  }

  public PreparedStatement ps() { return pstmt; }

  public boolean nextRow() {
    try { return ((ResultSet)object).next(); }
    catch(Exception e) { return false; }
  }

  public ExprCollective currentRow() { return this; }

  public static QueryResult prepare(DBHandle hndl, Expr[] params) throws Throwable
  {
    RT.echo(hndl.sql);
    String sql = hndl.sql.replace('\r',' ');
    PreparedStatement pstmt = hndl.isCall ? hndl.conn.getConnection().prepareCall(sql)
                                          : hndl.conn.getConnection().prepareStatement(sql);
    hndl.setBindVariables(pstmt,params);
    ResultSet rset = pstmt.executeQuery();
    hndl.getConnection().checkSQLWarning(pstmt);
    return new QueryResult(hndl, pstmt, rset, rset.getMetaData());
  }

  public DBConnect getConnection() { return handle.getConnection(); }

  public final ResultSet getResultSet() { return (ResultSet)object; }

  public Variable resolveVariable(String name) throws Throwable {
    try { return getCurrentColumn(name); } catch(Exception e) {}
    return ValueSpecial.UNDEFINED;
  }

  public final int getColumnCount() {
    try { return rsmd.getColumnCount(); } catch(Exception e) { return 0; }
  }

  public void refresh(Expr[] params) throws Throwable {
    try {
      if (getResultSet() != null)
        getResultSet().close();
    } catch(Exception e) {}
    if (handle == null)
      ExceptionRuntime.rte(RTERR_ILLEGAL_ARGUMENTS, "This result set can not be refreshed.");
    handle.setBindVariables(pstmt,params);
    object = pstmt.executeQuery();
    rsmd = getResultSet().getMetaData();
  }

  public void close() {
    try { getResultSet().close(); } catch(Exception e) {}
    try { pstmt.close(); } catch(Exception e) {}
    pstmt = null;
    rsmd = null;
  }

  public Variable setVariable(String name, Variable val, int type) throws Throwable {
    return handle.setVariable(name,val,type);
  }

  public final Variable invoke(int ord, String fxn, Expr[] params, int[] types) throws Throwable
  {
    int len = (params==null) ? 0 : params.length;
    switch(ord) {
    case BIM_DUMPRESULT: // return number of row.
      String tableName = rsmd.getTableName(1); // table name for the first column
      String fileName = (len<0) ? null : params[0].getStringValue();
      int limit = (len<1) ? 0 : (int)params[1].getLongValue();
      int promptSeg = (len<2) ? 0 : (int)params[2].getLongValue();
      return ConstInt.getInt( TableDump.dumpTable(tableName,fileName,getResultSet(),limit,promptSeg) );

    case BIM_GETONERESULT:
    case BIM_GETRESULT:    // return array if one column; TableData otherwise.
      limit = (len>0) ? (int)params[0].getLongValue() : 0;
      if (rsmd.getColumnCount() == 1) { // array
        _Array arr = new _Array();
        ResultSet rs = getResultSet();
        while (rs.next())
          arr.append(getCurrentColumn(1));
        if (ord == BIM_GETONERESULT) {
          if (arr.size() <= 0)
            return ValueSpecial.NIL;
          else
            return arr.resolve(0);
        }
        return arr;
      } else {
        return new _TableData(getResultSet(), rsmd.isCaseSensitive(1), limit);
      }
    case BIM_GETRESULTSETTYPE:
      try {
        switch(getResultSet().getType()) {
        case ResultSet.TYPE_FORWARD_ONLY:       return DBHandle.RSTYPE_FORWARD_ONLY;
        case ResultSet.TYPE_SCROLL_INSENSITIVE: return DBHandle.RSTYPE_SCROLL_INSENSITIVE;
        case ResultSet.TYPE_SCROLL_SENSITIVE:   return DBHandle.RSTYPE_SCROLL_SENSITIVE;
        }
      } catch(SQLException sqle) {
        ExceptionRuntime.rte(ExceptionRuntime.RTERR_JDBC_FAILURE,sqle.getMessage()); // TODO
      }
      break;
    case BIM_GETRESULTSET:
      return this;
    case BIM_GETRESULTSETMETADATA:
      return JudoUtil.toVariable(rsmd);
    case BIM_GETCOLUMNATTRS:
      return new _TableData(Lib.describeResultSet(rsmd));

    default: // otherwise, call the Java method of the result.
      return super.invoke(fxn,params,types);
    }
    return ValueSpecial.UNDEFINED;
  }

  public Variable invoke(String fxn, Expr[] params, int[] types) throws Throwable
  {
    return invoke(getMethodOrdinal(fxn),fxn,params,types);
  }

  ///////////////////////////////////
  // Getting result column value
  //

  Variable getCurrentColumn(String name) throws Exception {
    int idx = getResultSet().findColumn(name);
    return getCurrentColumn(idx);
  }

  Variable getCurrentColumn(int idx) throws Exception {
    int type = rsmd.getColumnType(idx);
    ResultSet rs = getResultSet();
    Object o;
    switch(type) {
    case Types.BIT:       return ConstInt.getBool(rs.getBoolean(idx));
    case Types.NUMERIC:   //if (rsmd.getScale(idx) > 0)
                          //  return new ConstDouble(rs.getDouble(idx));
                          //else
                          //  return ConstInt.getInt(rs.getLong(idx));
                          return new ConstDouble(rs.getDouble(idx));
    case Types.TINYINT:   return ConstInt.getInt(rs.getByte(idx));
    case Types.SMALLINT:  return ConstInt.getInt(rs.getShort(idx));
    case Types.INTEGER:   return ConstInt.getInt(rs.getInt(idx));
    case Types.BIGINT:    return ConstInt.getInt(rs.getLong(idx));
    case Types.FLOAT:     return new ConstDouble(rs.getFloat(idx));
    case Types.DECIMAL:
    case Types.REAL:
    case Types.DOUBLE:    return new ConstDouble(rs.getDouble(idx));
    case Types.VARBINARY: return JudoUtil.toVariable(rs.getBytes(idx));
                          // to get stream, use $dbhndl.getInputStream($idx)
                          // or $dbhndl.getCharacterStream($idx).
    case Types.DATE:      o = rs.getDate(idx);      break; // because of the MySQL driver.
    case Types.TIME:      o = rs.getTime(idx);      break; // because of the MySQL driver.
    case Types.TIMESTAMP: o = rs.getTimestamp(idx); break; // because of the MySQL driver.
    case Types.BLOB:      o = rs.getBlob(idx);      break;
    case Types.CLOB:      o = rs.getClob(idx);      break;
    case Types.ARRAY:     o = rs.getArray(idx);     break;
    case Types.REF:       o = rs.getRef(idx);       break;
/*
    case Types.CHAR:
    case Types.LONGVARCHAR:
    case Types.VARCHAR:
    case Types.STRUCT:
    case Types.OTHER:
    case JAVA_OBJECT:
    case ORACLE_ROWID:
    case ORACLE_BFILE:
    case ORACLE_CURSOR:
    // and more JDBC 2.0 types
*/
    default:              o = rs.getObject(idx); break;
    }
    return JudoUtil.toVariable(o);
  }

  /////////////////////////////////////////////////////////
  // ExprCollective methods
  //

  public Variable resolve(Variable idx) throws Throwable {
    if (idx.isString())
      return getCurrentColumn(idx.getStringValue());
    else
      return getCurrentColumn((int)idx.getLongValue());
  }

  public Variable resolve(Variable[] dims) throws ExceptionRuntime {
    ExceptionRuntime.rte(RTERR_INVALID_ARRAY_ACCESS,"Multi-dimensional access not supported.");
    return null;
  }
  public Variable setVariable(Variable idx, Variable val, int type) throws ExceptionRuntime {
    ExceptionRuntime.rte(RTERR_ILLEGAL_VALUE_SETTING, "No indexed attributes to set.");
    return null;
  }
  public Variable setVariable(Variable[] dims, Variable val, int type) throws ExceptionRuntime {
    ExceptionRuntime.rte(RTERR_ILLEGAL_VALUE_SETTING, "No indexed attributes to set.");
    return null;
  }

  public int getType() { return TYPE_OBJECT; }
  public String getObjectTypeName() { return "result set"; }
  public void dump(XMLWriter out) {}

} // end of class QueryResult.

