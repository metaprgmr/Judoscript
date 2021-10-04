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
import com.judoscript.bio.*;
import com.judoscript.*;
import com.judoscript.util.*;


public final class DBMetaData extends JavaObject
{
  boolean caseSens;

  public DBMetaData(DatabaseMetaData md) {
    super(md);
    try { caseSens = md.storesMixedCaseIdentifiers(); }
    catch(Exception e) { caseSens = false; }
  }

  public final DatabaseMetaData getDBMD() { return (DatabaseMetaData)object; }

  public Variable invoke(String fxn, Expr[] args, int[] javaTypes) throws Throwable
  {
    int len = (args==null) ? 0 : args.length;
    String s1;
    String sa[];
    String sa1[];
    switch(getMethodOrdinal(fxn)) {

    case BIM_GETSCHEMAS:
      return rs2td(getDBMD().getSchemas());

    case BIM_GETCATALOGS:
      return rs2td(getDBMD().getCatalogs());

    case BIM_GETTABLETYPES:
      return rs2td(getDBMD().getTableTypes());

    case BIM_GETTYPEINFO:
      return rs2td(getDBMD().getTypeInfo());

    // getProcedures([catalog.][schema_pattern.]proc_pattern)
    case BIM_GETPROCEDURES:
      if (len < 1)
        ExceptionRuntime.badParams(fxn,"procedure name pattern");
      sa = DBConnect.divideDBObjectName(args[0].getStringValue());
      return rs2td(getDBMD().getProcedures(sa[0],sa[1],sa[2]));

    // getProcedures([catalog.][schema_pattern.]proc_pattern, column_pattern)
    case BIM_GETPROCEDURECOLUMNS:
      if (len < 1)
        ExceptionRuntime.badParams(fxn,"procedure name pattern","column name pattern");
      sa = DBConnect.divideDBObjectName(args[0].getStringValue());
      return rs2td(getDBMD().getProcedureColumns(sa[0],sa[1],sa[2],args[1].getStringValue()));

    // getTablePrivileges([catalog.][schema_pattern.]table_pattern)
    case BIM_GETTABLEPRIVILEGES:
      if (len < 1)
        ExceptionRuntime.badParams(fxn,"table name pattern");
      sa = DBConnect.divideDBObjectName(args[0].getStringValue());
      return rs2td(getDBMD().getTablePrivileges(sa[0],sa[1],sa[2]));

    // getTables([catalog.][schema_pattern.]table_pattern [, types!])
    //   types: one or more of "TABLE", "VIEW", "SYSTEM TABLE",
    //          "GLOBAL TEMPORARY", "LOCAL TEMPORARY", "ALIAS", "SYNONYM".
    case BIM_GETTABLES:
      if (len < 1)
        ExceptionRuntime.badParams(fxn,"table name pattern","types");
      sa = DBConnect.divideDBObjectName(args[0].getStringValue());
      sa1 = (len == 1) ? null : new String[len-1];
      for (--len; len>=1; --len)
        sa1[len-1] = args[len].getStringValue();
      return rs2td(getDBMD().getTables(sa[0],sa[1],sa[2],sa1));

    // getColumns([catalog.][schema_pattern.]table_pattern, column_pattern)
    case BIM_GETCOLUMNS:
      if (len < 2)
        ExceptionRuntime.badParams(fxn,"table name pattern","column name pattern");
      sa = DBConnect.divideDBObjectName(args[0].getStringValue());
      return rs2td(getDBMD().getColumns(sa[0],sa[1],sa[2],args[1].getStringValue()));

    // getColumnPrivileges([catalog.][schema.]table, column_pattern)
    case BIM_GETCOLUMNPRIVILEGES:
      if (len < 2)
        ExceptionRuntime.badParams(fxn,"table name","column name pattern");
      sa = DBConnect.divideDBObjectName(args[0].getStringValue());
      return rs2td(getDBMD().getColumnPrivileges(sa[0],sa[1],sa[2],args[1].getStringValue()));

    // getBestRowIdentifier([catalog.][schema.]table, scope!, nullable),
    //   scope: 'bestRowNotPseudo', 'bestRowPseudo', 'bestRowSession',
    //          'bestRowTemporary', 'bestRowTransaction' or 'bestRowUnknown'.
    case BIM_GETBESTROWIDENTIFIER:
      if (len < 1)
        ExceptionRuntime.badParams(fxn,"table name","scope","nullable");
      sa = DBConnect.divideDBObjectName(args[0].getStringValue());
      s1 = (len>=2) ? args[1].getStringValue() : "bestRowNotPseudo";
      if (s1.equalsIgnoreCase("bestRowNotPseudo"))        len = DatabaseMetaData.bestRowNotPseudo;
      else if (s1.equalsIgnoreCase("bestRowPseudo"))      len = DatabaseMetaData.bestRowPseudo;
      else if (s1.equalsIgnoreCase("bestRowSession"))     len = DatabaseMetaData.bestRowSession;
      else if (s1.equalsIgnoreCase("bestRowTemporary"))   len = DatabaseMetaData.bestRowTemporary;
      else if (s1.equalsIgnoreCase("bestRowTransaction")) len = DatabaseMetaData.bestRowTransaction;
      else                                                len = DatabaseMetaData.bestRowUnknown;
      boolean b = (len>=3) ? args[2].getBoolValue() : true;
      return rs2td(getDBMD().getBestRowIdentifier(sa[0],sa[1],sa[2],len,b));

    // getVersionColumns([catalog.][schema.]table)
    case BIM_GETVERSIONCOLUMNS:
      if (len < 1)
        ExceptionRuntime.badParams(fxn,"table name");
      sa = DBConnect.divideDBObjectName(args[0].getStringValue());
      return rs2td(getDBMD().getVersionColumns(sa[0],sa[1],sa[2]));

    // getPrimaryKeys([catalog.][schema.]table)
    case BIM_GETPRIMARYKEYS:
      if (len < 1)
        ExceptionRuntime.badParams(fxn,"table name");
      sa = DBConnect.divideDBObjectName(args[0].getStringValue());
      return rs2td(getDBMD().getPrimaryKeys(sa[0],sa[1],sa[2]));

    // getImportedKeys([catalog.][schema.]table)
    case BIM_GETIMPORTEDKEYS:
      if (len < 1)
        ExceptionRuntime.badParams(fxn,"table name");
      sa = DBConnect.divideDBObjectName(args[0].getStringValue());
      return rs2td(getDBMD().getImportedKeys(sa[0],sa[1],sa[2]));

    // getExportedKeys([catalog.][schema.]table)
    case BIM_GETEXPORTEDKEYS:
      if (len < 1)
        ExceptionRuntime.badParams(fxn,"table name");
      sa = DBConnect.divideDBObjectName(args[0].getStringValue());
      return rs2td(getDBMD().getExportedKeys(sa[0],sa[1],sa[2]));

    // getCrossReference([pri_catalog.][pri_schema.]pri_table,[for_catalog.][for_schema.]for_table)
    case BIM_GETCROSSREFERENCE:
      if (len < 2)
        ExceptionRuntime.badParams(fxn,"primary table name","foreign table name");
      sa  = DBConnect.divideDBObjectName(args[0].getStringValue());
      sa1 = DBConnect.divideDBObjectName(args[1].getStringValue());
      return rs2td(getDBMD().getCrossReference(sa[0],sa[1],sa[2],sa1[0],sa1[1],sa1[2]));

    // getIndexInfo([catalog.][schema.]table[,unique=false[,approxiate=false]])
    case BIM_GETINDEXINFO:
      if (len < 1)
        ExceptionRuntime.badParams(fxn,"table name","unique","approximate");
      sa = DBConnect.divideDBObjectName(args[0].getStringValue());
      boolean uniq = (len>1) ? args[1].getBoolValue() : false;
      boolean appx = (len>2) ? args[2].getBoolValue() : false;
      return rs2td(getDBMD().getIndexInfo(sa[0],sa[1],sa[2],uniq,appx));

    // getUDTs([catalog.][schema_pattern.]type_pattern[,types!])
    //   types: one or more of 'JAVA_OBJECT', 'STRUCT' or 'DISTINCT'.
    case BIM_GETUDTS:
      if (len < 1)
        ExceptionRuntime.badParams(fxn,"type name pattern","types");
      sa = DBConnect.divideDBObjectName(args[0].getStringValue());
      int[] ia = null;
      if (len >= 2) {
        ia = new int[len-1];
        for (--len; len>=1; --len) {
          s1 = args[len].getStringValue();
          if (s1.equalsIgnoreCase("java_object"))
            ia[len] = java.sql.Types.JAVA_OBJECT;
          else if (s1.equalsIgnoreCase("struct"))
            ia[len] = java.sql.Types.STRUCT;
          else if (s1.equalsIgnoreCase("distinct"))
            ia[len] = java.sql.Types.DISTINCT;
        }
      }
      return rs2td(getDBMD().getUDTs(sa[0],sa[1],sa[2],ia));

    // tableExists([catalog.][schema_pattern.]table_pattern)
    case BIM_TABLEEXISTS:
      if (len < 1) return ConstInt.FALSE;
      sa = DBConnect.divideDBObjectName(args[0].getStringValue());
      return ConstInt.getBool( Lib.hasResults( getDBMD().getTables(sa[0], sa[1], sa[2], null) ) );

    // procedureExists([catalog.][schema_pattern.]proc_pattern)
    case BIM_PROCEXISTS:
      if (len < 1) return ConstInt.FALSE;
      sa = DBConnect.divideDBObjectName(args[0].getStringValue());
      return ConstInt.getBool( Lib.hasResults( getDBMD().getProcedures(sa[0], sa[1], sa[2]) ) );

    // udtExists([catalog.][schema_pattern.]type_pattern)
    case BIM_UDTEXISTS:
      if (len < 1) return ConstInt.FALSE;
      sa = DBConnect.divideDBObjectName(args[0].getStringValue());
      return ConstInt.getBool( Lib.hasResults( getDBMD().getUDTs(sa[0], sa[1], sa[2], null) ) );

    default:
      return super.invoke(fxn,args,javaTypes);
    }
  }

  _TableData rs2td(ResultSet rs) throws Exception { return new _TableData(rs,caseSens,0); }

} // end of class DBMetaData.
