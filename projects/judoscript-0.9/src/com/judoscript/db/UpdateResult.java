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
import com.judoscript.util.XMLWriter;


public final class UpdateResult implements DBResult, MethodOrdinals
{
  DBHandle handle;
  PreparedStatement pstmt = null;
  int updateCount = 0;

  public UpdateResult(DBHandle hndl, Expr[] params) throws Throwable {
    handle = hndl;
    RT.echo(handle.sql);
    String sql = handle.sql.replace('\r',' ');
    pstmt = handle.isCall ? handle.conn.getConnection().prepareCall(sql)
                          : handle.conn.getConnection().prepareStatement(sql);
    handle.setBindVariables(pstmt,params);
    updateCount = pstmt.executeUpdate();
    getConnection().checkSQLWarning(pstmt);
  }

  public UpdateResult(DBHandle hndl, PreparedStatement ps, int updateCnt) {
    handle = hndl;
    pstmt = ps;
    updateCount = updateCnt;
  }

  public PreparedStatement ps() { return pstmt; }

  public DBConnect getConnection() { return handle.getConnection(); }

  public Variable resolveVariable(String name) throws Throwable { return ValueSpecial.UNDEFINED; }

  public Variable invoke(int ord, String fname, Expr[] params, int[] types) throws Exception
  {
    if (ord == BIM_GETRESULT || ord == BIM_GETONERESULT)
      return ConstInt.getInt(updateCount);
    return ValueSpecial.UNDEFINED;
  }

  public void refresh(Expr[] params) throws Throwable {
    handle.setBindVariables(pstmt,params);
    updateCount = pstmt.executeUpdate();
  }

  public void close() {
    try { pstmt.close(); } catch(Exception e) {}
    pstmt = null;
    updateCount = 0;
  }

  public Variable setVariable(String name, Variable val, int type) throws Throwable {
    return handle.setVariable(name,val,type);
  }

  public int getUpdateCount() { return updateCount; }

  public void dump(XMLWriter out) {
    out.simpleSingleTagLn("UpdateResult");
  }

} // end of class UpdateResult.

