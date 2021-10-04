/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 09-24-2004  JH   Inception.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript;

import java.util.Set;
import java.util.Properties;
import java.util.ArrayList;
import com.judoscript.user.HibernateLib;
import com.judoscript.hibernate.HibernateEnv;
import com.judoscript.hibernate.HibernateSession;
import com.judoscript.db.DBConnect;
import com.judoscript.util.AssociateList;
import com.judoscript.util.XMLWriter;


public class StmtHQL extends StmtBase
{
  public static final int CMD_TX_BEGIN = 1;
  public static final int CMD_TX_END   = 2;
  public static final int CMD_TX_ABORT = 3;
  public static final int CMD_GET      = 4;
  public static final int CMD_CLOSE    = 5;

  public static final int ACTION_QUERY   = 1;
  public static final int ACTION_ITERATE = 2;
  public static final int ACTION_DELETE  = 3;

  public String  envName    = DEFAULT_HIBERNATE_NAME;
  public int     action     = ACTION_QUERY;
  public String  returnName = Consts.THIS_NAME;
  public Expr    from       = null;
  public Expr    limit      = null;
  public Expr    collection = null;
  public Expr    hql        = null;
  public Expr[]  bindVars   = JudoUtil.emptyExprs;

  public StmtHQL() { super(0); }

  public void exec() throws Throwable {
    Object colVal = collection == null ? null : collection.getObjectValue();
    String hqlVal = hql.getStringValue();
    int fromVal = from == null ? 0 : (int)from.getLongValue();
    int limitVal = limit == null ? 0 : (int)limit.getLongValue();

    HibernateEnv env = (HibernateEnv)RT.resolveGlobalVariable(envName).getObjectValue();
    Object ret = HibernateLib.exec(env, action, colVal, hqlVal, fromVal, limitVal, bindVars);
    RT.setVariable(returnName, JudoUtil.toVariable(ret), 0);
  }

  public void checkBindVars() throws Exception {
    String name;
    if (action == ACTION_DELETE)
      name = "delete";
    else if (collection != null)
      name = "query on collection";
    else
      return;
    if (HibernateSession.bindVarsContainsEntity(bindVars))
      throw new Exception("Sorry, hib::" + name + " does not take entity bind variables.");
  }

  public void dump(XMLWriter out) { // TODO
    out.simpleTagLn("StmtHQL");
    out.endTagLn();
  }

  public static Expr getBindVar(String varName, String typeName, Expr typeObj, Expr val) throws Exception {
    return HibernateSession.getBindVar(varName, typeName, typeObj, val);
  }

  /////////////////////////////////////////////////////////
  //
  //
  public static final class Setup extends StmtBase
  {
    public AssociateList init = null;
    ArrayList classNames = new ArrayList();

    public Setup(int lineNo) { super(lineNo); }

    // Not used.
    public void setName(String name) {
      if (init == null)
        init = new AssociateList();
      init.add("judoscript.name", name);
    }

    public void addResource(String rscName) { classNames.add(HibernateEnv.RESOURCE_PREFIX + rscName); }
    public void addClass(String clsName) { classNames.add(clsName); }

    public void exec() throws Throwable {
      String name = DEFAULT_HIBERNATE_NAME;

      String suggestedJdbcDriver = null;
      String suggestedSqlDialect = null;

      // Process the properties:
      String[] guessedConnInfo = null;
      Properties props = null;
      if (init != null) {
        props = new Properties();
        String k;
        int len = init.size();
        for (int i=0; i<len; ++i) {
          k = JudoUtil.toParameterNameString(init.getKeyAt(i));
          if (k.equals("judoscript.name")) {
            name = (String)init.getKeyAt(i);
          } else {
            String v = JudoUtil.toParameterNameString(init.getValueAt(i));
            props.put(k, v);
            if (k.equals("hibernate.connection.url"))
              guessedConnInfo = DBConnect.courtesyDriverLoad(v);
          }
        }

        Set keys = props.keySet();
        if (guessedConnInfo != null) {
          k = "hibernate.connection.driver_class";
          if (!keys.contains(k) && (guessedConnInfo[0] != null))
            props.put(k, guessedConnInfo[0]);
  
          k = "hibernate.dialect";
          if (!keys.contains(k) && (guessedConnInfo[1] != null))
            props.put(k, guessedConnInfo[1]); // the dialect name may not have package prefix,
                                              // which will be handled by HibernateEnv().
        }

        if (keys.contains("hibernate.connection.url") && !keys.contains("hibernate.connection.driver_class"))
          ExceptionRuntime.rte(Consts.RTERR_JDBC_DRIVER_NOT_FOUND,
            "No JDBC driver class specified for Hibernate (hibernate.connection.driver_class).");
      }

      RT.setGlobalVariable(name, JudoUtil.toVariable(new HibernateEnv(props, classNames)), 0);
    }

    public void dump(XMLWriter out) { // TODO
      out.simpleTagLn("StmtHQL.Setup");
      out.endTagLn();
    }
  }

} // end of class StmtHQL.
