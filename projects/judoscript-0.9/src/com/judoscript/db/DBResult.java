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


public interface DBResult
{
  public DBConnect getConnection();
  public Variable resolveVariable(String name) throws Throwable;
  public Variable invoke(int fxn, String fname, Expr[] params, int[] types) throws Throwable;
  public void refresh(Expr[] params) throws Throwable;
  public PreparedStatement ps();
  public void close();
}


