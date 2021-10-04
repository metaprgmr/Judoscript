/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 03-17-2002  JH   Initial open source release.
 * 06-10-2002  JH   Added "hsqldb" to the knownJdbcDrivers.
 * 08-08-2002  JH   Added "pointbase" to the knownJdbcDrivers.
 * 12-08-2004  JH   Added support for custom JDBC connection attributes.
 * 12-30-2004  JH   For courtesy driver load, extend the search to cover
 *                  two parts following "jdbc:"; this is prompted by the
 *                  need of TimesTen JDBC driver, because its URLs can
 *                  be "timesten:direct" or "timesten:client", whose
 *                  drivers are different.
 * 01-06-2005  JH   For courtesy driver load, search for "a:b" first,
 *                  and redo the hibernate dialect fetching.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript.db;

import java.sql.*;
import java.util.*;
import java.io.*;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.*;
import com.judoscript.*;
import com.judoscript.bio.*;
import com.judoscript.util.AssociateList;
import com.judoscript.util.Lib;


public final class DBConnect extends JavaObject implements ShortcutInvokable
{
  // Set the JDBC logger
  public static final Log logger = LogFactory.getLog("judo.jdbc");

  static class LogWriter extends Writer {
    Log logger;

    LogWriter(Log logger) { this.logger = logger; }
    public void write(char[] buf, int off, int len) throws IOException {
      logger.info(new String(buf, off, len));
    }
    public void flush() {}
    public void close() {}
  }

  static {
    DriverManager.setLogWriter(new PrintWriter(new LogWriter(logger)));
  }

  public static final Expr DEFAULT_CONNECTION_ACCESS = new AccessVar(DEFAULT_CONNECTION_NAME);

  public static final Properties knownJdbcDrivers = new Properties();
  static {
    Properties p = JudoUtil.loadProperties("db/jdbcdrivers", true);
    if (p != null) {
      Enumeration en= p.keys();
      while (en.hasMoreElements()) {
        String k = (String)en.nextElement();
        String v = (String)p.get(k);
        knownJdbcDrivers.put(k,v);
      }
    }
  }

  String url = null;
  DatabaseMetaData dbmd = null;
  JavaObject joDbmd = null;
  String catSep = "";
  boolean showWarnings = true;
  String driverClass = null;

  public DBConnect() { super(); }
  public DBConnect(Connection con) { super(con); }

  public void init(Object inits) throws Throwable {
    if (inits == null || !(inits instanceof AssociateList))
      ExceptionRuntime.rte(RTERR_ILLEGAL_ARGUMENTS,
        "Database connection requires url and/or username/password.");

    AssociateList ht = (AssociateList)inits;

    Properties props = new Properties();
    String url = null;
    String user = null;
    String pass = null;
    int i;
    int len = ht.size();
    String k;
    for (i=0; i<len; ++i) { // look for driver, url, user and password
      k = JudoUtil.toParameterNameString(ht.getKeyAt(i));
      if (k.equals("driver")) {
        driverClass = ((Expr)ht.getValueAt(i)).getStringValue();
        RT.getClass(driverClass);
      } else if (k.equals(OPTION_JDBC_URL)) {
        url = ((Expr)ht.getValueAt(i)).getStringValue();
      } else if (k.equals(OPTION_JDBC_USERNAME)) {
        user = ((Expr)ht.getValueAt(i)).getStringValue();
      } else if (k.equals(OPTION_JDBC_PASSWORD)) {
        pass = ((Expr)ht.getValueAt(i)).getStringValue();
      } else if (!hasVariable(k)) {
        props.put( k, ((Expr)ht.getValueAt(i)).getStringValue() );
      }
    }

    if (driverClass == null) {
      String[] connInfo = courtesyDriverLoad(url);
      if (connInfo != null)
        driverClass = connInfo[0];
    }
    if (driverClass == null) {
      ExceptionRuntime.rte(RTERR_JDBC_DRIVER_NOT_FOUND, "JDBC driver not specified and resolved.");
    }

    Connection conn;
    if (props.size() <= 0) {
      conn = DriverManager.getConnection(url, user, pass);
    } else { // if there are custom JDBC connection attributes
      if (StringUtils.isNotBlank(user))
        props.put("user", user);
      if (StringUtils.isNotBlank(pass))
        props.put("password", pass);
      conn = DriverManager.getConnection(url, props);
    }
    setObject(conn);

    // Set the known JDBC connection attributes
    for (i=0; i<len; ++i) {
      k = (String)ht.getKeyAt(i);
      if (hasVariable(k))
        setVariable(k, ((Expr)ht.getValueAt(i)).eval());
    }
  }

  public final Connection getConnection() { return (Connection)object; }

  public final String getObjectTypeName() { return "db_connect"; }

  public final boolean hasVariable(String name) {
    return "autoCommit".equals(name) ||
           "readOnly".equals(name) ||
           "catalog".equals(name) ||
           "typeMap".equals(name) ||
           "transactionIsolation".equals(name) ||
           "driver".equals(name);
  }

  public final Variable resolveVariable(String name) throws Throwable {
    checkConnection();
    try {
      if ("driver".equals(name))
        return JudoUtil.toVariable(driverClass);
      if ("autoCommit".equals(name))
        return ConstInt.getBool( getConnection().getAutoCommit() );
      if ("readOnly".equals(name))
        return ConstInt.getBool( getConnection().isReadOnly() );
      if ("catalog".equals(name))
        return JudoUtil.toVariable( getConnection().getCatalog() );
      if ("typeMap".equals(name))
        return JudoUtil.toVariable(getConnection().getTypeMap());
      if ("transactionIsolation".equals(name)) {
        switch(getConnection().getTransactionIsolation()) {
        case Connection.TRANSACTION_NONE:
          return JudoUtil.toVariable("transaction_none");
        case Connection.TRANSACTION_READ_UNCOMMITTED:
          return JudoUtil.toVariable("transaction_read_uncommitted");
        case Connection.TRANSACTION_READ_COMMITTED:
          return JudoUtil.toVariable("transaction_read_committed");
        case Connection.TRANSACTION_SERIALIZABLE:
          return JudoUtil.toVariable("transaction_serializable");
        }
      }
    } catch(SQLException sqle) {
      logger.error("Failed to get a Connection attribute.", sqle);
    }
    ExceptionRuntime.rte(RTERR_ILLEGAL_ARGUMENTS,
              "Illegal database connection member access for '" + name + "'.");
    return null;
  }

  public final Variable setVariable(String name, Variable val) throws Throwable {
    if (name.equals("driver"))
      return ValueSpecial.UNDEFINED;

    checkConnection();
    if ("autoCommit".equals(name))
      getConnection().setAutoCommit(val.getBoolValue());
    else if ("readOnly".equals(name))
      getConnection().setReadOnly(val.getBoolValue());
    else if ("catalog".equals(name))
      getConnection().setCatalog(val.getStringValue());
    else if ("typeMap".equals(name)) {
      try {
        Map map = (Map)val.getObjectValue();
        Iterator iter = map.entrySet().iterator();
        while (iter.hasNext()) {
          Object o = iter.next();
          Object c = map.get(o);
          if (!(c instanceof Class))
            ExceptionRuntime.rte(RTERR_ILLEGAL_VALUE_SETTING,
              "In the type map, " + o + " does not reference a java.lang.Class.");
          if (!(o instanceof String)) { // make sure the key is a String.
            iter.remove();
            map.put(o.toString(),c);
          }
        }
        getConnection().setTypeMap(map);
      } catch(ClassCastException cce) {
        ExceptionRuntime.rte(RTERR_ILLEGAL_VALUE_SETTING,
                             "Database type map must be a java.util.Map object.");
      }
    } else if ("transactionIsolation".equals(name)) {
      String s = val.getStringValue();
      if ( "transaction_none".equals(s) )
        getConnection().setTransactionIsolation( Connection.TRANSACTION_NONE );
      else if ( "transaction_read_committed".equals(s) )
        getConnection().setTransactionIsolation( Connection.TRANSACTION_READ_COMMITTED );
      else if ( "transaction_read_uncommitted".equals(s) )
        getConnection().setTransactionIsolation( Connection.TRANSACTION_READ_UNCOMMITTED );
      else if ( "transaction_serializable".equals(s) )
        getConnection().setTransactionIsolation( Connection.TRANSACTION_SERIALIZABLE );
      else
        ExceptionRuntime.rte(RTERR_ILLEGAL_VALUE_SETTING,
                             "Illegal translation isolation value: '"+s+"'.");
    }
    return ValueSpecial.UNDEFINED;
  }

  /////////////////////////////////////////////////////////
  // Method Ordinals
  //

  public Variable invoke(String fxn, Expr[] params, int[] javaTypes) throws Throwable
  {
    return invoke(getMethodOrdinal(fxn),fxn,params,javaTypes);
  }

  public Variable invoke(int ord, String fxn, Expr[] params, int[] javaTypes) throws Throwable
  {
    int len = (params==null) ? 0 : params.length;
    String[] sa;
    ResultSet rs;
    boolean b;
    Variable v;
    String table;
    Statement stmt;
    int i;
    if (ord == BIM_CLOSE) { close(); return ValueSpecial.UNDEFINED; }
    checkConnection();
    switch(ord) {

    case BIM_CLOSE:         getConnection().close(); break;
    case BIM_COMMIT:        getConnection().commit(); break;
    case BIM_ROLLBACK:      getConnection().rollback(); break;
    case BIM_CLEARWARNINGS: getConnection().clearWarnings(); break;

    case BIM_GETMETADATA:
      checkMetaData();
      return joDbmd;

    case BIM_NATIVESQL:
      if (len < 1) break;
      return JudoUtil.toVariable(getConnection().nativeSQL(params[0].getStringValue()));

    case BIM_ADDTYPEMAP:
      if (len < 2) break;
      Class cls = null;
      try { cls = (Class)params[1].eval().getObjectValue(); }
      catch(Exception e) {
        ExceptionRuntime.rte(RTERR_ILLEGAL_VALUE_SETTING,
          "addTypeMap() takes a class for its second parameter.");
      }
      Map map = getConnection().getTypeMap();
      map.put(params[0].getStringValue(),cls);
      getConnection().setTypeMap(map);
      break;

    case BIM_REPORTWARNINGS:
      showWarnings = (len<1) ? true : params[0].getBoolValue();
      return ConstInt.getBool(showWarnings);

    case BIM_EXECUTEQUERY:
      if (len < 1) break;
      return executeQuery(params[0]);

    case BIM_EXECUTESQL:
      if (len < 1) break;
      return executeSQL(params);

    case BIM_EXECUTESQLFILE:
      if (len < 1) break;
      return executeSQLFile(params);

    case BIM_EXECUTEBATCH:
      if (len < 1) break;
      return executeBatch(params);

    case BIM_OBJECTEXISTS: // Oracle only
      if (len <= 0) break;
      if (url.indexOf("oracle") > 0) {
        try {
          stmt = getConnection().createStatement();
          rs = stmt.executeQuery("SELECT 1 FROM user_objects WHERE object_name='" +
                                 params[0].getStringValue() + "'");
          return ConstInt.getBool(Lib.hasResults(rs));
        } catch(Exception e) {}
      }
      break;

    case BIM_GETOBJECTTYPE: // Oracle only
      if (len <= 0) break;
      if (url.indexOf("oracle") > 0) {
        try {
          stmt = getConnection().createStatement();
          rs = stmt.executeQuery("SELECT OBJECT_TYPE FROM USER_OBJECTS WHERE OBJECT_NAME='" +
                                 params[0].getStringValue() + "'");
          if (rs.next())
            return JudoUtil.toVariable(rs.getString(1));
        } catch(Exception e) {}
      }
      break;

    case BIM_TABLEEXISTS:
      checkMetaData();
      return joDbmd.invoke("tableExists",params,javaTypes);

    case BIM_PROCEXISTS:
      checkMetaData();
      return joDbmd.invoke("procExists",params,javaTypes);

    case BIM_UDTEXISTS:
      checkMetaData();
      return joDbmd.invoke("udtExists",params,javaTypes);

    case BIM_DESCRIBE:
      if (len <= 0) break;
      return new _TableData(Lib.describeTable(getConnection(),params[0].getStringValue()));

    case BIM_COUNTROWS:
      if (len <= 0) break;
      table = params[0].getStringValue();
      try {
        stmt = getConnection().createStatement();
        rs = stmt.executeQuery("SELECT COUNT(*) FROM " + table);
        checkSQLWarning(stmt);
        if (rs.next())
          return ConstInt.getInt(rs.getInt(1));
        rs.close();
        stmt.close();
        rs = null;
        stmt = null;
      } catch(Exception e) {}
      break;

    case BIM_CREATEBATCH: return new DBBatch(this);

    default: return super.invoke(fxn,params,javaTypes);
    }

    return ValueSpecial.UNDEFINED;
  }

  // returns String[3]: [0] - catalog, [1] - schema, [2] - object
  public static String[] divideDBObjectName(String s) {
    String[] sa = Lib.string2Array(s, '.', false, 3, null);
    if (sa[2] == null) {
      if (sa[1] != null) { sa[2]=sa[1]; sa[1]=sa[0]; sa[0]=null; }
      else { sa[2]=sa[0]; sa[1]=sa[0]=null; }
    }
    return sa;
  }

  public DatabaseMetaData checkMetaData() throws SQLException {
    if (dbmd == null) {
      dbmd = getConnection().getMetaData();
      joDbmd = new DBMetaData(dbmd);
      catSep = dbmd.getCatalogSeparator();
    }
    return dbmd;
  }

  public Variable executeQuery(Expr sql) throws Throwable {
    DBHandle handle = new DBHandle();
    handle.init(new Expr[] { this, sql });
    return handle.executeQuery(null,null);
  }

  // Only updates can be run here. returns the cumulative update count.
  // if queries do present, they are executed but no way to get result sets.
  public Variable executeSQL(Expr[] sqls) throws Throwable {
    int results[] = new int[sqls.length];
    Statement stmt = getConnection().createStatement();
    for (int i=0; i<sqls.length; ++i) {
      String sql = sqls[i].getStringValue();
      RT.echo(sql);
      if (!stmt.execute(sql.replace('\r',' ')))
        results[i] = stmt.getUpdateCount();
      checkSQLWarning(stmt);
    }
    stmt.close();
    stmt = null;
    return JudoUtil.toVariable(results);
  }

  public Variable executeSQLFile(Expr[] sqlfiles) throws Throwable {
    List results = new ArrayList();
    Statement stmt = getConnection().createStatement();
    for (int i=0; i<sqlfiles.length; ++i) {
      BufferedReader br = new BufferedReader(new FileReader(sqlfiles[i].getStringValue()));
      String sql = "";
      while (true) {
        String line = br.readLine();
        if (line == null) break;
        line = line.trim();
        if (line.startsWith("#")) continue; // comment.
        int idx = line.indexOf(";");
        if (idx < 0) {
          sql += " " + line;
          continue;
        } else {
          sql += line.substring(0,idx);
          line = line.substring(idx+1);
          RT.echo(sql);
          if (!stmt.execute(sql))
            results.add(new Integer(stmt.getUpdateCount()));
          checkSQLWarning(stmt);
          sql = "";
        }
      }
    }
    stmt.close();
    stmt = null;
    return JudoUtil.toVariable(list2intarray(results));
  }

  public Variable executeBatch(Expr[] sqls) throws Throwable {
    Statement stmt = getConnection().createStatement();
    for (int i=0; i<sqls.length; ++i) {
      String sql = sqls[i].getStringValue();
      RT.echo(sql);
      stmt.addBatch(sql.replace('\r',' '));
    }
    int[] results = stmt.executeBatch();
    checkSQLWarning(stmt);
    stmt.close();
    stmt = null;
    return JudoUtil.toVariable(results);
  }

  public final void removeVariable(String name) {}
  public final void clearVariables() {}

  public final void close() {
    try { getConnection().close(); } catch(Exception e) {}
    dbmd = null;
    joDbmd = null;
  }

  void checkConnection() throws ExceptionRuntime {
    try { if (!getConnection().isClosed()) return; } catch(SQLException sqle) {}
    ExceptionRuntime.rte(RTERR_JDBC_FAILURE, "Database connection is not open.");
  }

  public void checkSQLWarning(Statement stmt) throws Exception {
    SQLWarning warn = stmt.getWarnings();
    if (warn == null) return;
    StringBuffer sb = new StringBuffer("VENDOR-CODE: ");
    sb.append(warn.getErrorCode());
    sb.append("; SQLSTATE: ");
    sb.append(warn.getSQLState());
    sb.append(ConstString.newline);
    String msg = warn.getMessage();
    if (msg != null) sb.append(msg);
    while (null != (warn = warn.getNextWarning())) {
      msg = warn.getMessage();
      if (msg != null) {
        sb.append(ConstString.newline);
        sb.append(msg);
      }
    }
    ExceptionRuntime.rte(RTERR_JDBC_WARNING, sb.toString());
  }

  /**
   * Tries to load the JDBC driver for the url.
   *
   *@return String[0] is driver class name; String[1] is Hibernate dialect name.
   */
  public static String[] courtesyDriverLoad(String url) {
    if (!url.startsWith("jdbc:")) // what?! Not a jdbc url?
      return null;

    if (url.startsWith("jdbc:spy:")) {
      try {
        RT.getClass("com.judoscript.jdbcspy.SpyDriver");
      } catch(Exception e) {
        logger.warn("Failed to load JDBC spy driver.", e);
      }
      url = "jdbc" + url.substring(8);
    }

    int idx = url.indexOf(':', 5);
    if (idx < 0) // better not mess up
      return null;
    String singleName = url.substring(5, idx).toLowerCase(); // jdbc:a:
    idx = url.indexOf(':', idx+1);
    String doubleName = null;
    if (idx > 5)
      doubleName = url.substring(5, idx).toLowerCase(); // jdbc:a:b:

    String val = null;
    if (doubleName != null)
      val = (String)knownJdbcDrivers.get(doubleName); // try "a:b" first
    if (val == null)
      val = (String)knownJdbcDrivers.get(singleName); // then try "a"

    String[] ret = { null, null }; // [0] - class name; [1] - Hib. dialect
    if (val != null) {
      idx = val.lastIndexOf(":");
      if (idx > 0) {
        try {
          ret[1] = val.substring(idx+1).trim();
        } catch(Exception e) {}
        val = val.substring(0, idx).trim();
      }
      ret[0] = getClassNameFromStrings(val);
    }

    if (ret[0] == null)
      RT.logger.warn("No JDBC drivers found for '" + url + "'.");

    return ret;
  }

  static String getClassNameFromStrings(String strs) {
    StringTokenizer st = new StringTokenizer(strs, ",");
    while (st.hasMoreTokens()) {
      try {
        String x = st.nextToken();
        Class.forName(x);
        return x;
      } catch(Exception e) {}
    }
    return null;
  }

  static int[] list2intarray(List arr) {
    int[] res = new int[arr.size()];
    for (int i=arr.size()-1; i>=0; --i)
      res[i] = ((Integer)arr.get(i)).intValue();
    return res;
  }

} // end of class DBConnect.
