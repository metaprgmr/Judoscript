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

public final class DBBatch extends ObjectInstance
{
  DBConnect conn;
  Statement stmt;
  int batchCount;

  public DBBatch() { super(); }

  public DBBatch(DBConnect con) { conn = con; }

  public void init(Object inits) throws Throwable {
    if (inits == null) return;
    if (inits instanceof Expr[]) { // { Connection, SQL }
      Expr[] ea = (Expr[])inits;
      conn = (DBConnect)ea[0].eval();
      stmt = null;
      batchCount = 0;
    } else super.init(inits);
  }

  public String getTypeName() { return "db_batch"; }

  public Variable eval() throws ExceptionRuntime, Throwable { return this; }

  public final Variable resolveVariable(String name) throws Throwable {
    if (name.equals("size"))
      return ConstInt.getInt(batchCount);

    return super.resolveVariable(name);
  }

  public Variable invoke(String fxn, Expr[] params, int[] javaTypes) throws Throwable
  {
    int ord = getMethodOrdinal(fxn);
    switch(ord) {
    case BIM_APPEND:
      if (stmt == null)
        stmt = conn.getConnection().createStatement();
      stmt.addBatch(params[0].getStringValue());
      ++batchCount;
      return ConstInt.ONE;
    case BIM_SIZE:
      return ConstInt.getInt(batchCount);
    case BIM_EXECUTE:
      int[] results = stmt.executeBatch();
      conn.checkSQLWarning(stmt);
      stmt.close();
      stmt = null;
      batchCount = 0;
      return JudoUtil.toVariable(results);
    default: return super.invoke(ord,fxn,params);
    }
  }

  public void close() {
    try { stmt.close(); } catch(Exception e) {}
    stmt = null;
    batchCount = 0;
  }

} // end of class DBBatch.
