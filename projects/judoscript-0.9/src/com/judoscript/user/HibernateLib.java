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


package com.judoscript.user;

import java.util.*;
import java.io.Serializable;

import com.judoscript.*;
import com.judoscript.hibernate.HibernateEnv;
import com.judoscript.hibernate.HibernateSession;


public final class HibernateLib implements Consts
{
  public static void close(HibernateEnv env) throws Exception {
    checkNoTransaction(env,
      "Current Hibernate transaction must be committed or rolled back before you can close.");
    try { getSession(env, false).close(); } catch(Exception e) {}
    hibEnv = null;
  }
  public static void close() throws Exception {
    close(getDefaultEnv());
  }

  public static void addClass(HibernateEnv env, Object clazz) throws Exception {
    env.addClass(clazz);
  }
  public static void addClass(Object clazz) throws Exception {
    addClass(getDefaultEnv(), clazz);
  }

  public static void addResource(HibernateEnv env, String rsc) throws Exception {
    env.addResource(rsc);
  }
  public static void addResource(String rsc) throws Exception {
    addResource(getDefaultEnv(), rsc);
  }

  /**
   *@return Transaction
   */
  public static Object txBegin(HibernateEnv env) throws Exception {
    checkNoTransaction(env, "Can't start a new transaction while another one is running.");
    HibernateSession hibSess = getSession(env, true);
    return hibSess.txBegin();
  }
  public static Object txBegin() throws Exception {
    return txBegin(getDefaultEnv());
  }

  public static void txEnd(HibernateEnv env) throws Exception {
    HibernateSession hibSess = getSession(env, false);
    if (hibSess != null)
      hibSess.txEnd();
  } 
  public static void txEnd() throws Exception {
    txEnd(getDefaultEnv());
  }

  public static void txAbort(HibernateEnv env) throws Exception {
    HibernateSession hibSess = getSession(env, false);
    if (hibSess != null)
      hibSess.txAbort();
  } 
  public static void txAbort() throws Exception {
    txAbort(getDefaultEnv());
  }

  public static void lock(HibernateEnv env, Object obj, Object lockMode) throws Exception {
    HibernateSession hibSess = getSession(env, false);
    if (hibSess != null)
      hibSess.lock(obj, lockMode);
  }
  public static void lock(Object obj, Object lockMode) throws Exception {
    lock(getDefaultEnv(), obj, lockMode);
  }
  public static void unlock(HibernateEnv env, Object obj) throws Exception {
    lock(env, obj, null);
  }
  public static void unlock(Object obj) throws Exception {
    lock(getDefaultEnv(), obj, null);
  }

  public static Object get(HibernateEnv env, String name) throws Exception {
    name = name.toLowerCase();

    if (name.startsWith("config"))
      return env.getConfig();

    if (name.endsWith("factory"))
      return env.getSessionFactory();

    if (name.equals("session"))
      return getSession(env, true);

    return null;
  }
  public static Object get(String name) throws Exception {
    return get(getDefaultEnv(), name);
  }

  public static Object get(HibernateEnv env, Class clazz, Object id) throws Exception {
    try {
      HibernateSession sess = getSession(env, true);

      Class idClz = sess.getIdentifierClass(clazz);
      if (idClz.equals(Byte.class)) {
        if (id instanceof Byte) {
          ;
        } else if (id instanceof Number) {
          id = new Byte((byte)((Number)id).intValue());
        } else {
          id = Byte.valueOf(id.toString());
        }
      } else if (idClz.equals(Character.class)) {
        if (id instanceof Character) {
          ;
        } else if (id instanceof Number) {
          id = new Character((char)((Number)id).intValue());
        } else {
          id = new Character(id.toString().charAt(0));
        }
      } else if (idClz.equals(Short.class)) {
        if (id instanceof Short) {
          ;
        } else if (id instanceof Number) {
          id = new Short(((Number)id).shortValue());
        } else {
          id = Short.valueOf(id.toString());
        }
      } else if (idClz.equals(Integer.class)) {
        if (id instanceof Integer) {
          ;
        } else if (id instanceof Number) {
          id = new Integer(((Number)id).intValue());
        } else {
          id = Integer.valueOf(id.toString());
        }
      } else if (idClz.equals(Long.class)) {
        if (id instanceof Long) {
          ;
        } else if (id instanceof Number) {
          id = new Long(((Number)id).longValue());
        } else {
          id = Long.valueOf(id.toString());
        }
      } else if (idClz.equals(Float.class)) {
        if (id instanceof Float) {
          ;
        } else if (id instanceof Number) {
          id = new Float(((Number)id).floatValue());
        } else {
          id = Float.valueOf(id.toString());
        }
      } else if (idClz.equals(Double.class)) {
        if (id instanceof Double) {
          ;
        } else if (id instanceof Number) {
          id = new Double(((Number)id).doubleValue());
        } else {
          id = Double.valueOf(id.toString());
        }
      } else if (idClz.equals(String.class)) {
        id = id.toString();
      }

      return sess.get(clazz, (Serializable)id);
    } catch(Exception e) {
      if (e.getClass().getName().endsWith("hibernate.ObjectNotFoundException"))
        return null;
      throw e;
    }
  }
  public static Object get(Class clazz, Object id) throws Exception {
    return get(getDefaultEnv(), clazz, id);
  }

  /**
   *@param lock LockMode or String
   */
  public static Object get(HibernateEnv env, Class clazz, Serializable id, Object lock) throws Exception {
    try {
      return getSession(env, true).get(clazz, id, lock);
    } catch(Exception e) {
      if (e.getClass().getName().endsWith("hibernate.ObjectNotFoundException"))
        return null;
      throw e;
    }
  }
  public static Object get(Class clazz, Serializable id, Object lock) throws Exception {
    return get(getDefaultEnv(), clazz, id, lock);
  }

  public static void saveObject(HibernateEnv env, Object obj) throws Exception {
    getSession(env, true).objDo(HibernateSession.OP_SAVE, obj, null);
  }
  public static void saveObject(Object obj) throws Exception {
    saveObject(getDefaultEnv(), obj);
  }

  public static void saveObject(HibernateEnv env, Object obj, Serializable id) throws Exception {
    getSession(env, true).objDo(HibernateSession.OP_SAVE, obj, id);
  }
  public static void saveObject(Object obj, Serializable id) throws Exception {
    saveObject(getDefaultEnv(), obj, id);
  }

  public static void updateObject(HibernateEnv env, Object obj) throws Exception {
    getSession(env, true).objDo(HibernateSession.OP_UPDATE, obj, null);
  }
  public static void updateObject(Object obj) throws Exception {
    updateObject(getDefaultEnv(), obj);
  }

  public static void updateObject(HibernateEnv env, Object obj, Serializable id) throws Exception {
    getSession(env, true).objDo(HibernateSession.OP_UPDATE, obj, id);
  }
  public static void updateObject(Object obj, Serializable id) throws Exception {
    updateObject(getDefaultEnv(), obj, id);
  }

  public static void saveOrUpdateCopy(HibernateEnv env, Object obj) throws Exception {
    getSession(env, true).objDo(HibernateSession.OP_SOUC, obj, null);
  }
  public static void saveOrUpdateCopy(Object obj) throws Exception {
    saveOrUpdateCopy(getDefaultEnv(), obj);
  }

  public static void saveOrUpdateCopy(HibernateEnv env, Object obj, Serializable id) throws Exception {
    getSession(env, true).objDo(HibernateSession.OP_SOUC, obj, id);
  }
  public static void saveOrUpdateCopy(Object obj, Serializable id) throws Exception {
    saveOrUpdateCopy(getDefaultEnv(), obj, id);
  }

  public static void deleteObject(HibernateEnv env, Object obj) throws Exception {
    getSession(env, true).objDo(HibernateSession.OP_DELETE, obj, null);
  }
  public static void deleteObject(Object obj) throws Exception {
    deleteObject(getDefaultEnv(), obj);
  }

  /**
   *@param action is StmtHQL.ACTION_QUERY/_ITERATE/_DELETE.
   *@param bindVars is really HibernateSession.ExprHibernateBindVar[].
   *@return Either a List or Object if limit==1, or null if action is DELETE.
   */
  public static Object exec(HibernateEnv env, int action, Object coll, String hql,
                            int from, int limit, Expr[] bindVars)
     throws Throwable
  {
    return getSession(env, true).exec(action, coll, hql, from, limit, bindVars);
  }
  public static Object exec(int action, Object coll, String hql, int from, int limit, Expr[] bindVars)
     throws Throwable
  {
    return exec(getDefaultEnv(), action, coll, hql, from, limit, bindVars);
  }

  //////////////////////////////////////////////////
  // Impl
  //
  // Each transaction starts with a per-thread session
  // and its associated transaction.
  //

  protected static HibernateEnv getDefaultEnv() throws ExceptionRuntime {
    try {
      return (HibernateEnv)RT.resolveGlobalVariable(Consts.DEFAULT_HIBERNATE_NAME).getObjectValue();
    } catch(Throwable t) {
      if (t instanceof ExceptionRuntime)
        throw (ExceptionRuntime)t;
      ExceptionRuntime.rte(RTERR_ENVIRONMENT_ERROR, "Hibernate not initialized: " + t.getMessage());
      return null;
    }
  }

  protected static HibernateSession getSession(HibernateEnv env, boolean doCreate) throws Exception {
    HashMap map;
    HibernateSession sess;

    if (hibEnv != null) {
      map = (HashMap)hibEnv.get();
      sess = (HibernateSession)map.get(env);
      if (sess != null)
        return sess;
    }

    if (!doCreate)
      return null;

    if (hibEnv != null) {
      map = (HashMap)hibEnv.get();
    } else {
      hibEnv = new ThreadLocal();
      map = new HashMap();
      hibEnv.set(map);
    }

    sess = env.newSession();
    map.put(env, sess);
    return sess;
  }

  protected static void checkNoTransaction(HibernateEnv env, String msg) throws Exception {
    HibernateSession hibSess = getSession(env, false);
    if (hibSess != null && hibSess.inTx())
      ExceptionRuntime.rte(RTERR_ILLEGAL_ACCESS, msg);
  }

  protected static ThreadLocal hibEnv = null; // HashMap of Configuration => HibernateSession

} // end of class HibernateLib.
