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
import java.util.*;
import com.judoscript.*;
import com.judoscript.bio.*;
import com.judoscript.util.RangeIterator;
import com.judoscript.util.ResultSetIterator;


public class DBHandle extends ObjectInstance implements ExprTableData, ShortcutInvokable
{
  /////////////////////////////////////////////////////////
  // JDBC Types
  //
  static final ConstString RSTYPE_FORWARD_ONLY       = new ConstString("forward_only");
  static final ConstString RSTYPE_SCROLL_INSENSITIVE = new ConstString("scroll_insensitive");
  static final ConstString RSTYPE_SCROLL_SENSITIVE   = new ConstString("scroll_sensitive");

  public static String getSqlTypeName(int type) {
    switch(type) {
    case Types.ARRAY:     return "array";
    case Types.BIGINT:    return "long";
    case Types.BLOB:      return "blob";
    case Types.CHAR:      return "char";
    case Types.CLOB:      return "clob";
    case Types.DATE:      return "date";
    case Types.DOUBLE:    return "double";
    case Types.FLOAT:     return "float";
    case Types.INTEGER:   return "int";
    case Types.NUMERIC:   return "numeric";
    case Types.OTHER:     return "other";
    case Types.REF:       return "ref";
    case Types.SMALLINT:  return "short";
    case Types.STRUCT:    return "struct";
    case Types.TIME:      return "time";
    case Types.TIMESTAMP: return "timestamp";
    case Types.TINYINT:   return "byte";
    case Types.VARBINARY: return "bytes";
    case Types.LONGVARCHAR:
    case Types.VARCHAR:   return "String";
    case ORACLE_CURSOR:   return "oracle_cursor";
    case ORACLE_ROWID:    return "oracle_rowid";
    case ORACLE_BFILE:    return "oracle_bfile";
    }
    return "unknown:" + type;
  }


  /////////////////////////////////////////////////////////
  // DBHandle stuff
  //

  DBConnect conn = null;
  String sql = null;
  DBResult executeResult = null;
  boolean isCall = false;
  int rs_type   = ResultSet.TYPE_FORWARD_ONLY;
  int rs_fetch  = ResultSet.FETCH_FORWARD;
  int rs_concur = ResultSet.CONCUR_READ_ONLY;
  private HashMap namedBindParameters = null;

  public DBHandle() { super(); }

  public final void init(Object inits) throws Throwable {
    if (inits == null)
      return;
    if (inits instanceof Expr[]) { // { Connection, SQL }
      Expr[] ea = (Expr[])inits;
      try {
        conn = (DBConnect)ea[0].eval();
      } catch(Exception e) {
        ExceptionRuntime.rte(RTERR_JDBC_FAILURE,"Database connection is not set up yet.");
      }
      sql = ea[1].getStringValue();
      sql = checkNamedBindParameters(sql);
      isCall = (ea.length > 2) ? ea[2].getBoolValue() : false;

      String s;
      if (ea.length > 3) {
        if (ea[3] != null) { // type
          s = ea[3].getStringValue();
          if (s.equalsIgnoreCase("scroll_insensitive"))
            rs_type = ResultSet.TYPE_SCROLL_INSENSITIVE;
          if (s.equalsIgnoreCase("scroll_sensitive"))
            rs_type = ResultSet.TYPE_SCROLL_SENSITIVE;
          if (s.equalsIgnoreCase("forward_only"))
            rs_type = ResultSet.TYPE_FORWARD_ONLY;
          // otherwise, ignore.
        } else if (ea.length > 4 && ea[4] != null) { // fetch
          s = ea[4].getStringValue();
          if (s.equalsIgnoreCase("forward"))
            rs_concur = ResultSet.FETCH_FORWARD;
          if (s.equalsIgnoreCase("reverse"))
            rs_concur = ResultSet.FETCH_REVERSE;
          // otherwise, ignore.
        } else if (ea.length > 5 && ea[5] != null) { // concur
          s = ea[5].getStringValue();
          if (s.equalsIgnoreCase("read_only"))
            rs_concur = ResultSet.CONCUR_READ_ONLY;
          if (s.equalsIgnoreCase("updatable"))
            rs_concur = ResultSet.CONCUR_UPDATABLE;
          // otherwise, ignore.
        }
      }
    } else super.init(inits);
  }

  public Iterator getIterator(int start, int end, int step, boolean upto, boolean backward) throws Exception {
    if (backward)
      ExceptionRuntime.rte(RTERR_ILLEGAL_ACCESS, "Query result can not be iterated backwards.");
    if (executeResult instanceof QueryResult)
      return RangeIterator.getIterator(new ResultSetIterator(((QueryResult)executeResult).getResultSet()),
                                       start, end, step, upto);
    ExceptionRuntime.rte(RTERR_ILLEGAL_ACCESS, "Only query results have iterators.");
    return null;
  }

  public final String getTypeName() { return "db_handle"; }

  public final DBConnect getConnection() { return conn; }

  public final Variable eval() throws ExceptionRuntime, Throwable { return this; }

  public final Variable resolveVariable(String name) throws Throwable {
    if (executeResult != null)
      return executeResult.resolveVariable(name);
    return ValueSpecial.UNDEFINED;
  }

  /////////////////////////////////////////////////////////
  // ExprTableData method
  //

  public boolean nextRow() throws Exception {
    try {
      return ((QueryResult)executeResult).nextRow();
    } catch(Exception e) {
      ExceptionRuntime.rte(RTERR_ILLEGAL_ACCESS, "Database handle is not for a query.");
      return false;
    }
  }

  public ExprCollective currentRow() throws Exception {
    try {
      return ((QueryResult)executeResult).currentRow();
    } catch(Exception e) {
      ExceptionRuntime.rte(RTERR_ILLEGAL_ACCESS, "Database handle is not for a query.");
      return null;
    }
  }

  /////////////////////////////////////////////////////////
  // ExprCollective methods
  //

  public Variable resolve(Variable idx) throws Throwable {
    if ((executeResult == null) || !(executeResult instanceof QueryResult))
      return ValueSpecial.UNDEFINED;

    if (idx.isString())
      return ((QueryResult)executeResult).getCurrentColumn(idx.getStringValue());
    else
      return ((QueryResult)executeResult).getCurrentColumn((int)idx.getLongValue());
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
  public int size() {
    try { return ((QueryResult)executeResult).getColumnCount(); } catch(Exception e) { return 0; }
  }

  public final void removeVariable(String name) {}
  public final void clearVariables() {}

  public final void close() {
    if (executeResult != null)
      executeResult.close();
    executeResult = null;
    sql = null;
  }

  public Variable invoke(String fxn, Expr[] params, int[] javaTypes) throws Throwable
  {
    return invoke(getMethodOrdinal(fxn),fxn,params,javaTypes);
  }

  public Variable invoke(int ord, String fxn, Expr[] params, int[] javaTypes) throws Throwable
  {
    int len = (params==null) ? 0 : params.length;
    int i;
    Variable v;
    switch(ord) {
    case BIM_GETSQL:        return JudoUtil.toVariable(sql);
    case BIM_EXECUTEQUERY:  return executeQuery(params, javaTypes);
    case BIM_EXECUTEUPDATE: return executeUpdate(params, javaTypes);
    case BIM_EXECUTESQL:    return executeSQL(params, javaTypes);
    case BIM_CLOSE:         close(); break;

    case BIM_GETPREPAREDSTATEMENT:
      return (executeResult==null) ? ValueSpecial.UNDEFINED : JudoUtil.toVariable(executeResult.ps());

    case BIM_HASRESULTSET:
      try { return ConstInt.getBool( executeResult instanceof QueryResult ); }
      catch(Exception e) { return ConstInt.FALSE; }
    case BIM_NEXT:
      if (executeResult != null)
        return executeResult.invoke(BIM_NEXT, "next", params, javaTypes);
      break;

    default:
      if (executeResult != null)
        return executeResult.invoke(ord,fxn,params,javaTypes);
      return super.invoke(ord,fxn,params);
    }

    return ValueSpecial.UNDEFINED;
  }

  public Variable executeQuery(Expr[] params, int[] javaTypes) throws Throwable
  {
    try {
      if (executeResult == null) {
        executeResult = QueryResult.prepare(this,params);
      } else if (!(executeResult instanceof QueryResult)) {
        executeResult.close();
        executeResult = QueryResult.prepare(this,params);
      } else {
        executeResult.refresh(params);
      }
    } finally {
      flushOutBoundVariables();
    }
    return this;
  }

  public Variable executeUpdate(Expr[] params, int[] javaTypes) throws Throwable
  {
    try {
      if (executeResult == null) {
        executeResult = new UpdateResult(this,params);
      } else if (!(executeResult instanceof UpdateResult)) {
        executeResult.close();
        executeResult = new UpdateResult(this,params);
      } else {
        executeResult.refresh(params);
      }
    } finally {
      flushOutBoundVariables();
    }
    return ConstInt.getInt(((UpdateResult)executeResult).getUpdateCount());
  }

  public Variable executeSQL(Expr[] params, int[] javaTypes) throws Throwable {
    if (executeResult != null) executeResult.close();

    RT.echo(sql);
    sql = sql.replace('\r',' ');
    PreparedStatement ps = isCall ? conn.getConnection().prepareCall(sql)
                                  : conn.getConnection().prepareStatement(sql);
    setBindVariables(ps, params);
    boolean res = ps.execute();
    conn.checkSQLWarning(ps);
    try {
      if (res) { // true: has result sets
        ResultSet rs = ps.getResultSet(); // we don't handle multi-result-sets yet.
        executeResult = new QueryResult(this, ps, rs, rs.getMetaData());
        return this;
      } else { // has no result sets; treat it as an update.
        ResultSet rs = ps.getResultSet(); // we don't handle multi-result-sets yet.
        int updateCnt = ps.getUpdateCount();
        executeResult = new UpdateResult(this, ps, updateCnt);
        return ConstInt.getInt(updateCnt);
      }
    } finally {
      flushOutBoundVariables();
    }
  }

  Stack outBoundStack = null;

  final void pushOutBoundVariable(ExprOutBoundVar obv) {
    if (outBoundStack == null) outBoundStack = new Stack();
    outBoundStack.push(obv);
  }

  final void flushOutBoundVariables() throws Throwable {
    if (outBoundStack == null) return;
    while (!outBoundStack.empty()) {
      ((ExprOutBoundVar)outBoundStack.pop()).bound();
    }
    outBoundStack.clear();
  }

  void setBindVariables(PreparedStatement ps, Expr[] params) throws Throwable {
    JavaObject jo = null;
    ExprCall exprCall;
    int bvIdx;
    int len = (params==null) ? 0 : params.length;
    for (int i=0; i<len; i++) {
      if (params[i] instanceof ExprOutBoundVar) {
        CallableStatement cs = (CallableStatement)ps;
        ExprOutBoundVar obv = (ExprOutBoundVar)params[i];
        obv.setCallableStatement(cs);
        bvIdx = obv.getBindIndex(namedBindParameters);
        // if this is in-out, set the value if it exists
        if (obv.inOut) {
          Variable inVal = obv.eval();
          if (!inVal.isNil()) {
            switch(obv.sqlType) {
            case Types.TINYINT:   cs.setByte(bvIdx, (byte)inVal.getLongValue());     break; 
            case Types.SMALLINT:  cs.setShort(bvIdx, (short)inVal.getLongValue());   break; 
            case Types.INTEGER:   cs.setInt(bvIdx, (int)inVal.getLongValue());       break; 
            case Types.BIGINT:    cs.setLong(bvIdx, inVal.getLongValue());           break; 
            case Types.FLOAT:     cs.setFloat(bvIdx, (float)inVal.getDoubleValue()); break; 
            case Types.DOUBLE:
            case Types.NUMERIC:   cs.setDouble(bvIdx, inVal.getDoubleValue());       break; 
            case Types.CHAR:
            case Types.LONGVARCHAR:
            case Types.VARCHAR:
            default:              cs.setString(bvIdx, inVal.getStringValue());       break;
            case Types.DATE:      cs.setDate(bvIdx, inVal.getSqlDate());             break;
            case Types.TIME:      cs.setTime(bvIdx, inVal.getSqlTime());             break;
            case Types.TIMESTAMP: cs.setTimestamp(bvIdx, inVal.getSqlTimestamp());   break;
            case Types.VARBINARY:
              if (inVal instanceof JavaArray) {
                byte[] bytes = (byte[]) ((JavaArray)inVal).getObjectValue();
                cs.setBytes(bvIdx, bytes);
              } else {
                cs.setBytes(bvIdx, inVal.getStringValue().getBytes());
              }
              break;
            case Types.REF:
            case Types.BLOB:
            case Types.CLOB:
            case Types.ARRAY:
            case Types.STRUCT:
            case Types.OTHER:
            case ORACLE_ROWID:
            case ORACLE_BFILE:
            case ORACLE_CURSOR:   cs.setObject(bvIdx, inVal.getObjectValue());       break;
            }
          }
        }
 
        cs.registerOutParameter(bvIdx, obv.sqlType);
        pushOutBoundVariable(obv);

      } else {
        ExprBindVar bv = (ExprBindVar)params[i];
        if (bv.sqlType == PREPARED_STMT_CALL) {
          if (jo==null)
            jo = new JavaObject(ps);
          ((ExprCall)bv.expr).javaInvoke(jo);
          continue;
        }

        Variable v = bv.eval();
        bvIdx = getBindIndex(bv.bindIdx, bv.bindName);
        if (v.isNil()) {
          ps.setNull(bvIdx, bv.sqlType);
          continue;
        }
        switch(bv.sqlType) {
        case Types.TINYINT:   ps.setByte(bvIdx, (byte)v.getLongValue());     break; 
        case Types.SMALLINT:  ps.setShort(bvIdx, (short)v.getLongValue());   break; 
        case Types.INTEGER:   ps.setInt(bvIdx, (int)v.getLongValue());       break; 
        case Types.BIGINT:    ps.setLong(bvIdx, v.getLongValue());           break; 
        case Types.FLOAT:     ps.setFloat(bvIdx, (float)v.getDoubleValue()); break; 
        case Types.DOUBLE:
        case Types.NUMERIC:   ps.setDouble(bvIdx, v.getDoubleValue());       break; 
        case Types.CHAR:
        case Types.LONGVARCHAR:
        case Types.VARCHAR:
        default:              ps.setString(bvIdx, v.getStringValue());       break; 
        case Types.DATE:      ps.setDate(bvIdx, v.getSqlDate());             break;
        case Types.TIME:      ps.setTime(bvIdx, v.getSqlTime());             break;
        case Types.TIMESTAMP: ps.setTimestamp(bvIdx, v.getSqlTimestamp());   break;
        case Types.VARBINARY:
          if (v instanceof JavaArray) {
            byte[] bytes = (byte[]) ((JavaArray)v).getObjectValue();
            ps.setBytes(bvIdx, bytes);
          } else {
            ps.setBytes(bvIdx, v.getStringValue().getBytes());
          }
          break;
        case Types.REF:
        case Types.BLOB:
        case Types.CLOB:
        case Types.ARRAY:
        case Types.STRUCT:
        case Types.OTHER:
        case ORACLE_ROWID:
        case ORACLE_CURSOR:
        case ORACLE_BFILE:    ps.setObject(bvIdx, v.getObjectValue());       break;
        }
      }
    }
  }

  String checkNamedBindParameters(String sql) {
    if (sql.indexOf(':') < 0)
      return sql;

    namedBindParameters = new HashMap();
    StringBuffer sb = new StringBuffer();
    StringBuffer sb1 = new StringBuffer();
    int state = 0;
    int idx = 1;
    for (int i=0; i<sql.length(); ++i) {
      char ch = sql.charAt(i);
      switch(state) {
      case 1:  // state of in-string
        switch(ch) {
        case ':':  state = 2; ch = '?'; break;
        case '\'': state = 1; break;
        default:   sb.append(ch); break;
        }
        break;
      case 2:  // state of the parameter name
        if (!Character.isJavaIdentifierPart(ch)) {
          state = 0;
          sb.append(ch);
          namedBindParameters.put(sb1.toString(), new Integer(idx++));
          sb1.setLength(0);
        } else {
          sb1.append(ch);
        }
        break;
      default: // original state
        switch(ch) {
        case ':':  state = 2; ch = '?'; break;
        case '\'': state = 1; break;
        }
        sb.append(ch);
        break;
      }
    }
    if (sb1.length() > 0)
      namedBindParameters.put(sb1.toString(), new Integer(idx));
    return sb.toString();
  }

  int getBindIndex(int bindIdx, String bindName) throws Exception {
    if (bindName != null) {
      try {
        return ((Integer)namedBindParameters.get(bindName)).intValue();
      } catch(Exception e) {
        ExceptionRuntime.rte(RTERR_ILLEGAL_ACCESS, "Invalid bind variable name: '" + bindName + "'.");
      }
    }
    return bindIdx;
  }

} // end of class DBHandle.

