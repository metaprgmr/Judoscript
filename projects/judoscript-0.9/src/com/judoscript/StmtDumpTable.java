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


package com.judoscript;

import java.sql.*;
import com.judoscript.db.DBConnect;
import com.judoscript.util.*;


public class StmtDumpTable extends StmtBase
{
  Expr connection;
  Expr table;
  Expr into;
  Expr limit;
  Expr prompt;
  Expr where;

  public StmtDumpTable(int line, Expr table) {
    super(line);
    this.table = table;
    connection = new AccessVar(DBConnect.DEFAULT_CONNECTION_NAME);
  }

  public void setConnection(Expr con) { connection = con; }
  public void setInto(Expr into) { this.into = into; }
  public void setLimit(Expr limit) { this.limit = limit; }
  public void setPrompt(Expr prompt) { this.prompt = prompt; }
  public void setWhere(Expr where) { this.where = where; }


  public void exec() throws Throwable {
    Connection conn = null;
    try {
      conn = ((DBConnect)connection.eval()).getConnection();
    } catch(Exception e) {
      ExceptionRuntime.rte(RTERR_JDBC_FAILURE,"Database connection is not set up yet.");
    }
    long cnt = TableDump.dumpTable(conn,
                                   table.getStringValue(),
                                   (into==null) ? null : into.getStringValue(),
                                   (where==null) ? null : where.getStringValue(),
                                   (limit==null) ? -1: (int)limit.getLongValue(),
                                   (prompt==null) ? 0 : (int)prompt.getLongValue());
    RT.setVariable("$_", ConstInt.getInt(cnt), 0);
  }

  public void dump(XMLWriter out) {
    out.openTag("StmtDumpTable");
    // TODO: dump().
    out.endTagLn();
  }
}
