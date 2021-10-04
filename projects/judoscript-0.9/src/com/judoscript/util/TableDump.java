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


package com.judoscript.util;


import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.io.*;
import java.sql.*;
import org.apache.commons.lang.StringUtils;

/**
 * A dump file stores a part or whole of a database table.
 * Dump files are gzipped. The contents are Java-serialized,
 * which are all of basic data types:
 *
 *<pre>
 * String      java.version
 * String      java.vm.version
 * String      java.class.version
 * String[]    column attribute names -- titles for a TableData
 * Object[][]  column attributes      -- data for a TableData
 * (Object[])+ column values
 * Boolean.FALSE
 *</pre>
 *
 * This class provides methods to read from a dump file.
 *<p>
 * Its static method <code>dumpTable()</code> dumps the content of
 * a relational database table with a WHERE clause into a dump file.
 *<p>
 * There is also a convenience method, <code>generateLoadScript()</code>, * that generates JudoScript code to upload the data into a(nother)
 * database table, based on the meta information of the columns.
 */
public class TableDump implements Serializable
{
  String javaVersion;
  String javaVmVersion;
  String javaClassVersion;
  TableData columnAttrs;
  boolean hasMore = false;
  Object[] currentRow = null;
  ObjectInputStream in;

  /**
   *  Takes a dump file name, opens it and reads in the meta information.
   */
  public TableDump(String fileName) throws IOException, ClassNotFoundException {
    reInit(fileName); }

  /**
   *  Closes the current one if open, opens a new file and reads in the meta information.
   */
  public void reInit(String fileName) throws IOException, ClassNotFoundException {
    close();
    in = new ObjectInputStream( new GZIPInputStream( new FileInputStream(fileName) ) );
    javaVersion = (String)in.readObject();
    javaVmVersion = (String)in.readObject();
    javaClassVersion = (String)in.readObject();
    columnAttrs = new TableData((String[])in.readObject(),true);
    Object[][] oaa = (Object[][])in.readObject();
    for (int i=0; i<oaa.length; i++)
      columnAttrs.addRowNoCopy(oaa[i]);
    hasMore = true;
  }

  public void close() {
    try { in.close(); } catch(Exception e) {}
    hasMore = false;
    currentRow = null;
    columnAttrs = null;
    javaVersion = javaVmVersion = javaClassVersion = null;
    in = null;
  }

  public String getJavaVersion() { return javaVersion; }
  public String getJavaVmVersion() { return javaVmVersion; }
  public String getJavaClassVersion() { return javaClassVersion; }
  public TableData getColumnAttrs() { return columnAttrs; }

  /**
   * @return the next row or null if no more rows.
   */
  public Object[] next() throws IOException {
    if (!hasMore) return null;
    try {
      Object o = in.readObject();
      if (o instanceof Boolean) {
        hasMore = false;
        return null;
      }
      return (Object[])o;
    } catch(ClassNotFoundException cnfe) {
      return null; // never happens.
    }
  }

  /**
   *  Generates a JudoScript program that optionally creates the database table and
   *  loads the data into it.
   *<p>
   *  The generated code includes "connect.judi", which should have these connection
   *  parameters specified as <code>url</code>, <code>username</code> and <code>password</code>.
   *  You may modify the generated script to put in correct connection parameters,
   *  and also the CREATE TABLE statement for the column types.
   *
   * @param pw  The print writer used for the generated script code.
   * @param dumpFileName the dump file name.
   *                     Can be null, where <code><em>tableName</em>.tdmp</code> is used.
   * @param tableName    the database table name; must not be null.
   * @param createTable  flag indicating a CREATE TABLE statement need be generated.
   */
  public void generateLoadScript(PrintWriter pw, String dumpFileName, String tableName, boolean createTable)
              throws IOException
  {
    pw.println("// Make sure 'connection.judi' file is available");
    pw.println("// and url, username and password are correctly defined.");
    pw.println();

    // Part 1. Connect.
    pw.println("!include 'connection.judi'");
    pw.println("connect to url, username, password;");
    pw.println();

    // Part 2. Create table.
    int len = columnAttrs.size();
    int i;
    if (createTable) {
      pw.println("// COLUMN TYPES MAY BE MODIFIED.");
      pw.println("executeSQL {");
      pw.println();
      pw.print  ("  CREATE TABLE " + tableName + " (");
      for (i=0; i<len; ++i) {
        if (i>0) pw.println(",");
        pw.print  ("    ");
        pw.print(Lib.leftAlign((String)columnAttrs.getAt(i,"name"),14,false));
        pw.print(" ");
        String type = (String)columnAttrs.getAt(i,"type");
        pw.print(type);
        type = type.toUpperCase();
        if (type.indexOf("CHAR") >= 0) {
          int prec = ((Number)columnAttrs.getAt(i,"precision")).intValue();
          if (prec > 0) {
            pw.print("(");
            pw.print(prec);
            pw.print(")");
          }
        }
      }
      pw.println();
      pw.println("  );");
      pw.println();
      pw.println("}");
      pw.println();
    }

    // Part 3. Load data.
    if (StringUtils.isBlank(dumpFileName)) dumpFileName = tableName + ".tdmp";
    pw.println("dmp = openTableDump('" + dumpFileName + "');");
    pw.println("prepare:");
    pw.print  ("  INSERT INTO " + tableName + "(");
    for (i=0; i<len; ++i) {
      if (i>0) pw.print(',');
      pw.print(columnAttrs.getAt(i,"name"));
    }
    pw.println(")");
    pw.print  ("  VALUES(");
    for (i=0; i<columnAttrs.size(); ++i) {
      if (i>0) pw.print(',');
      pw.print('?');
    }
    pw.println(");");
    pw.println();
    pw.println("cnt=0;");
    pw.println("for ; (x = dmp.next()) != null; ++cnt {");
    pw.println("  // MODIFY THIS LIST!");
    pw.println("  executeUpdate with");
    for (i=0; i<len; ++i) {
      pw.print("    @" + (i+1));
      int type = ((Integer)columnAttrs.getAt(i,"typeValue")).intValue();
      switch(type) {
      case Types.ARRAY:     pw.print(":array");     break;
      case Types.BLOB:      pw.print(":blob");      break;
      case Types.CLOB:      pw.print(":clob");      break;
      case Types.REF:       pw.print(":ref");       break;
      case Types.STRUCT:    pw.print(":struct");    break;
      case Types.TINYINT:   pw.print(":byte");      break;
      case Types.SMALLINT:  pw.print(":short");     break;
      case Types.BIGINT:    pw.print(":long");      break;
      case Types.INTEGER:   pw.print(":int");       break;
      case Types.DOUBLE:    pw.print(":double");    break;
      case Types.FLOAT:     pw.print(":float");     break;
      case Types.NUMERIC:   pw.print(":numeric");   break;
      case Types.DATE:      pw.print(":date");      break;
      case Types.TIME:      pw.print(":time");      break;
      case Types.TIMESTAMP: pw.print(":timestamp"); break;
      case Types.VARBINARY: pw.print(":bytes");     break;
      case Types.VARCHAR:   pw.print(":varchar");   break;
      case -8:  pw.print(":oracle_rowid");  break;            // ORACLE_ROWID = -8;
      case -10: pw.print(":oracle_cursor"); break;            // ORACLE_CURSOR = -10;
      case -13: pw.print(":oracle_bfile");  break;            // ORACLE_BFILE = -13;
      case -14: pw.print(":oracle_plsql_index_table"); break; // ORACLE_PLSQL_INDEX_TABLE = -14;
      case 999: pw.print(":oracle_fixed_char"); break;        // ORACLE_FIXED_CHAR = 999;
      default:              pw.print(":other");     break;
      }
      pw.print(" = x[" + i + "]");
      pw.println( (i<len-1) ? ',' : ';' );
    }
    pw.println("  if cnt % 100 == 0 { flush '.'; }");
    pw.println("}");
    pw.println(". nl, unit(cnt, 'row');");
    pw.println();
    pw.println("disconnect();");
    pw.flush();
  }

  /**
   *  Dumps whole or part of a database table to a dump file.
   *
   * @param con           the database connection.
   * @param tableName     the database table name. Must not be null.
   * @param fileName      the dump file name. If null, default use <code><em>tableName</em>.tdmp</code>.
   * @param whereClause   the where clause for the internal SELECT statement. Can be null.
   * @param limit         if > 0, the maximum number of rows to dump.
   * @param promptSegment if > 0, every so many rows written, a count is displayed
   *                      to <code>System.out</code> as a prompt.
   */
  public static long dumpTable(Connection con, String tableName, String fileName,
                               String whereClause, int limit, int promptSegment)
                     throws SQLException, IOException
  {
    String sql = "SELECT * FROM " + tableName;
    if (StringUtils.isNotBlank(whereClause))
      sql += " WHERE " + whereClause;
    Statement stmt = con.createStatement();
    try {
      ResultSet rs = stmt.executeQuery(sql);
      return dumpTable(tableName, fileName, rs, limit, promptSegment);
    } finally {
      try { stmt.close(); } catch(Exception e) {}
    }
  }

  public static long dumpTable(String tableName, String fileName,
                               ResultSet rs, int limit, int promptSegment)
                     throws SQLException, IOException
  {
    if (fileName==null) fileName = tableName + ".tdmp";
    ObjectOutputStream oos = new ObjectOutputStream(
                              new GZIPOutputStream(new FileOutputStream(fileName)));
    TableData td = Lib.describeResultSet(rs.getMetaData());

    // write out headers
    oos.writeObject(System.getProperty("java.version"));
    oos.writeObject(System.getProperty("java.vm.version"));
    oos.writeObject(System.getProperty("java.class.version"));
    oos.writeObject(td.getTitles());
    oos.writeObject(td.getData());

    int cols = td.size();
    long count = 0;
    if (limit <= 0) limit = Integer.MAX_VALUE;
    try {
      while (rs.next()) {
        if (count>=limit) break;

        // hopefully rs.getObject() handles everything right,
        // including BLOB/CLOBs. Fingers crossed.
        Object[] oa = new Object[cols];
        for (int i=0; i<cols; i++)
          oa[i] = rs.getObject(i+1);
        oos.writeObject(oa);
        ++count;
        if (promptSegment > 0) {
          if (count % promptSegment == 0)
            System.out.println(count);
        }
      }
    } finally {
      try { rs.close(); } catch(Exception e) {}
      oos.writeObject(Boolean.FALSE);
      oos.close();
    }
    return count;
  }

} // end of class TableDump.
