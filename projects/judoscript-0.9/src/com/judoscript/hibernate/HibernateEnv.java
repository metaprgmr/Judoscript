/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 09-24-2004  JH   Inception.
 * 12-08-2004  JH   Move to be compatible with both Hibernate 2 and 3+.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript.hibernate;

import java.util.Properties;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import org.apache.commons.logging.*;

import com.judoscript.*;


public class HibernateEnv implements Consts
{
  public static final String RESOURCE_PREFIX = ">";

  public static final Log logger = LogFactory.getLog("judo.hibernate");

  static final String JUDOSCRIPT_ECHO = "judoscript.echo";

  static final int HIB_TYPE_LONG          = 1;
  static final int HIB_TYPE_SHORT         = 2;
  static final int HIB_TYPE_INTEGER       = 3;
  static final int HIB_TYPE_BYTE          = 4;
  static final int HIB_TYPE_FLOAT         = 5;
  static final int HIB_TYPE_DOUBLE        = 6;
  static final int HIB_TYPE_CHARACTER     = 7;
  static final int HIB_TYPE_STRING        = 8;
  static final int HIB_TYPE_TEXT          = 9;
  static final int HIB_TYPE_TIME          = 10;
  static final int HIB_TYPE_DATE          = 11;
  static final int HIB_TYPE_TIMESTAMP     = 12;
  static final int HIB_TYPE_CALENDAR      = 13;
  static final int HIB_TYPE_CALENDAR_DATE = 14;
  static final int HIB_TYPE_BOOLEAN       = 15;
  static final int HIB_TYPE_TRUE_FALSE    = 16;
  static final int HIB_TYPE_YES_NO        = 17;
  static final int HIB_TYPE_BIG_DECIMAL   = 18;
  static final int HIB_TYPE_BINARY        = 19;
  static final int HIB_TYPE_BLOB          = 20;
  static final int HIB_TYPE_CLOB          = 21;
  static final int HIB_TYPE_LOCALE        = 22;
  static final int HIB_TYPE_CURRENCY      = 23;
  static final int HIB_TYPE_TIMEZONE      = 24;
  static final int HIB_TYPE_CLASS         = 25;
  static final int HIB_TYPE_SERIALIZABLE  = 26;
  static final HashMap hibTypeOrdinals = new HashMap();

  private static boolean isHib2 = false;
  private static String pkgPrefix = "org.hibernate.";

  static {
    // which Hibernate we have?
    Class hibClz = null;
    try {
      hibClz = RT.getClass("org.hibernate.Hibernate");
    } catch(/*ClassNotFound*/Exception cnfe) {
      try {
        hibClz = RT.getClass("net.sf.hibernate.Hibernate");
        pkgPrefix = "net.sf.hibernate.";
        isHib2 = true;
      } catch(/*ClassNotFound*/Exception cnfe1) {
        pkgPrefix = null;
      }
    }

    if (hibClz != null) {
      try {
        hibTypeOrdinals.put(hibClz.getField("LONG").get(null),          new Integer(HIB_TYPE_LONG));
        hibTypeOrdinals.put(hibClz.getField("SHORT").get(null),         new Integer(HIB_TYPE_SHORT));
        hibTypeOrdinals.put(hibClz.getField("INTEGER").get(null),       new Integer(HIB_TYPE_INTEGER));
        hibTypeOrdinals.put(hibClz.getField("BYTE").get(null),          new Integer(HIB_TYPE_BYTE));
        hibTypeOrdinals.put(hibClz.getField("FLOAT").get(null),         new Integer(HIB_TYPE_FLOAT));
        hibTypeOrdinals.put(hibClz.getField("DOUBLE").get(null),        new Integer(HIB_TYPE_DOUBLE));
        hibTypeOrdinals.put(hibClz.getField("CHARACTER").get(null),     new Integer(HIB_TYPE_CHARACTER));
        hibTypeOrdinals.put(hibClz.getField("STRING").get(null),        new Integer(HIB_TYPE_STRING));
        hibTypeOrdinals.put(hibClz.getField("TIME").get(null),          new Integer(HIB_TYPE_TIME));
        hibTypeOrdinals.put(hibClz.getField("DATE").get(null),          new Integer(HIB_TYPE_DATE));
        hibTypeOrdinals.put(hibClz.getField("TIMESTAMP").get(null),     new Integer(HIB_TYPE_TIMESTAMP));
        hibTypeOrdinals.put(hibClz.getField("CALENDAR").get(null),      new Integer(HIB_TYPE_CALENDAR));
        hibTypeOrdinals.put(hibClz.getField("CALENDAR_DATE").get(null), new Integer(HIB_TYPE_CALENDAR_DATE));
        hibTypeOrdinals.put(hibClz.getField("BOOLEAN").get(null),       new Integer(HIB_TYPE_BOOLEAN));
        hibTypeOrdinals.put(hibClz.getField("TRUE_FALSE").get(null),    new Integer(HIB_TYPE_TRUE_FALSE));
        hibTypeOrdinals.put(hibClz.getField("YES_NO").get(null),        new Integer(HIB_TYPE_YES_NO));
        hibTypeOrdinals.put(hibClz.getField("BIG_DECIMAL").get(null),   new Integer(HIB_TYPE_BIG_DECIMAL));
        hibTypeOrdinals.put(hibClz.getField("BINARY").get(null),        new Integer(HIB_TYPE_BINARY));
        hibTypeOrdinals.put(hibClz.getField("TEXT").get(null),          new Integer(HIB_TYPE_TEXT));
        hibTypeOrdinals.put(hibClz.getField("BLOB").get(null),          new Integer(HIB_TYPE_BLOB));
        hibTypeOrdinals.put(hibClz.getField("CLOB").get(null),          new Integer(HIB_TYPE_CLOB));
        hibTypeOrdinals.put(hibClz.getField("LOCALE").get(null),        new Integer(HIB_TYPE_LOCALE));
        hibTypeOrdinals.put(hibClz.getField("CURRENCY").get(null),      new Integer(HIB_TYPE_CURRENCY));
        hibTypeOrdinals.put(hibClz.getField("TIMEZONE").get(null),      new Integer(HIB_TYPE_TIMEZONE));
        hibTypeOrdinals.put(hibClz.getField("CLASS").get(null),         new Integer(HIB_TYPE_CLASS));
        hibTypeOrdinals.put(hibClz.getField("SERIALIZABLE").get(null),  new Integer(HIB_TYPE_SERIALIZABLE));
      } catch(Exception e) {
      }
    }
  }

  Object cfg;     // ..hibernate.cfg.Configuration
  Object factory; // ..hibernate.SessionFactory
  private static Method cfg_buildSessionFactory = null;

  public static String getPkgPrefix() throws ExceptionRuntime {
    if (pkgPrefix == null)
      ExceptionRuntime.rte(RTERR_OBJECT_INIT,
        "Hibernate package is needed to script Hibernate ORM and HQL.");
    return pkgPrefix;
  }

  /**
   * To set up the Hibernate environment within JudoScript runtime.
   * It takes the list of persistent classes and optionally a set of
   * configuration properties, and initiates a Hibernate Configuration
   * and SessionFactory as singletons in RuntimeGlobalContext, and
   * also detects the Hibernate version 2 or not.
   *
   *@param ..hibernate.cfg.Configuration
   */
  public HibernateEnv(Properties props, ArrayList clsNames) throws Exception {
    getPkgPrefix(); // to check

    // Check and fix the dialect ...
    if (props != null) {
      String dialect = (String)props.get("hibernate.dialect");
      if (dialect != null) {
        int idx = dialect.lastIndexOf('.');
        if (idx < 0) {
          dialect = pkgPrefix + "dialect." + dialect;
          try {
            RT.getClass(dialect);
            props.put("hibernate.dialect", dialect);
          } catch(Exception e) {
            // never mind; just use the name verbatim.
          }
        }
      }

      if (props.containsKey(JUDOSCRIPT_ECHO) &&
          props.getProperty(JUDOSCRIPT_ECHO).equalsIgnoreCase("true"))
      {
        for (Iterator iter=props.keySet().iterator(); iter.hasNext(); ) {
          String k = (String)iter.next();
          logger.info("[hib::setup] " + k + " = " + props.get(k));
        }
        logger.info("[hib::setup] judoscript.hibernate.version = " + (isHib2 ? '2' : '3'));
      }
    }

    // Use reflection API to setup ...
    Class cfgCls = RT.getClass(pkgPrefix + "cfg.Configuration");
    Method cfg_setProperties = cfgCls.getMethod("setProperties", new Class[]{Properties.class});
    cfg_buildSessionFactory = cfgCls.getMethod("buildSessionFactory");
    Object[] oa1 = new Object[1];

    cfg = cfgCls.newInstance();

    if (props != null) {
      oa1[0] = props;
      try {
        cfg_setProperties.invoke(cfg, oa1);
      } catch(InvocationTargetException ite) {
        throw (Exception)ite.getTargetException();
      }
    }

    int len = clsNames == null ? 0 : clsNames.size();
    if (len > 0) {
      Method cfg_addClass = cfgCls.getMethod("addClass", new Class[]{Class.class});
      Method cfg_addResource = cfgCls.getMethod("addResource", new Class[]{String.class});
      for (int i=0; i<len; ++i) {
        Method mthd = cfg_addClass;
        Object clz = clsNames.get(i);
        if (clz instanceof Class) {
          oa1[0] = clz;
        } else {
          String s = clz.toString();
          if (s.startsWith(RESOURCE_PREFIX)) {
            mthd = cfg_addResource;
            oa1[0] = s.substring(RESOURCE_PREFIX.length());
          } else {
            oa1[0] = RT.getClass(s);
          }
        }
        try {
          mthd.invoke(cfg, oa1);
        } catch(InvocationTargetException ite) {
          throw (Exception)ite.getTargetException();
        }
      }
    }

    // Obtaining factory is delayed in case addClass() is intended.
  }

  public void addClass(Object clz) throws Exception {
    if (cfg == null)
      ExceptionRuntime.rte(RTERR_ILLEGAL_ACCESS,
        "Unable to add classes because Hibernate is not initialized yet.");
    
    Method cfg_addClass = cfg.getClass().getMethod("addClass", new Class[]{Class.class});
    Object[] params = new Object[]{ (clz instanceof Class) ? clz : RT.getClass(clz.toString()) };
    try {
      cfg_addClass.invoke(cfg, params);
    } catch(InvocationTargetException ite) {
      throw (Exception)ite.getTargetException();
    }
  }

  public void addResource(String resource) throws Exception {
    if (cfg == null)
      ExceptionRuntime.rte(RTERR_ILLEGAL_ACCESS,
        "Unable to add classes because Hibernate is not initialized yet.");
    
    Method cfg_addResource = cfg.getClass().getMethod("addResource", new Class[]{String.class});
    Object[] params = new Object[]{ resource };
    try {
      cfg_addResource.invoke(cfg, params);
    } catch(InvocationTargetException ite) {
      throw (Exception)ite.getTargetException();
    }
  }

  public HibernateSession newSession() throws Exception {
    if (factory == null) {
      try {
        factory = cfg_buildSessionFactory.invoke(cfg);
      } catch(InvocationTargetException ite) {
        throw (Exception)ite.getTargetException();
      }
    }

    String name = "com.judoscript.hibernate.Hibernate" + (isHib2 ? '2' : '3') + "Session";
    HibernateSession sess = (HibernateSession)RT.getClass(name).newInstance();
    sess.init(factory);
    return sess;
  }

  /**
   *@return net.sf.hibernate.cfg.Configuration or org.hibernate.cfg.Configuration
   */
  public Object getConfig() { return cfg; }

  /**
   *@return net.sf.hibernate.SessionFactory or org.hibernate.SessionFactory
   */
  public Object getSessionFactory() { return factory; }

} // end of class HibernateSessionEnv.
