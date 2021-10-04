/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 1-29-2005   JH   Adapted from "Hibernate2Session.java" by only
 *                  changing the import list.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript.hibernate;

import java.util.*;
import java.io.Serializable;
import java.lang.reflect.Field;

import org.hibernate.classic.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.Query;
import org.hibernate.LockMode;
import org.hibernate.type.Type;

import com.judoscript.*;

//
// Note!
//
// Any changes made here must also occur in Hibernate2Session!
//
public class Hibernate3Session extends HibernateSession
{
  private Session session = null;
  private Transaction tx = null;

  public Hibernate3Session() throws Exception {
  }

  public void init(Object factory) throws Exception {
    session = ((SessionFactory)factory).openSession();
  }

  public void close() throws Exception {
    session.close();
  }

  /**
   *@return Transaction
   */
  public Object txBegin() throws Exception {
    tx = session.beginTransaction();
    return tx;
  }

  public void txEnd() throws Exception {
    if (tx != null) {
      tx.commit();
      tx = null;
    }
  }

  public void txAbort() throws Exception {
    if (tx != null) {
      tx.commit();
      tx = null;
    }
  }

  public boolean inTx() {
    return tx != null;
  }

  public void lock(Object obj, Object lockMode) throws Exception {
    session.lock(obj, getLockMode(lockMode));
  }

  LockMode getLockMode(Object lock) throws Exception {
    if (lock == null)
      return LockMode.NONE;

    if (lock instanceof LockMode)
      return (LockMode)lock;

    try {
      Field fld = LockMode.class.getField( lock.toString().toUpperCase() );
      return (LockMode)fld.get(null);
    } catch(Exception e) {
      ExceptionRuntime.rte(RTERR_ILLEGAL_ARGUMENTS, "Lock mode '" + lock + "' not found.");
      return null;
    }
  }

  public Object get(Class clazz, Serializable id) throws Exception {
    return session.load(clazz, id);
  }

  /**
   *@param lock LockMode
   */
  public Object get(Class clazz, Serializable id, Object lock) throws Exception {
    if (lock instanceof String)
      lock = getLockMode((String)lock);
    return session.load(clazz, id, (LockMode)lock);
  }

  /**
   *@param action is StmtHQL.ACTION_QUERY/_ITERATE/_DELETE.
   *@param bindVars is really ExprHibernateBindVar[].
   *@return Either a List or Object if limit==1, or null if action is DELETE.
   */
  public Object exec(int action, Object col, String hql, int from, int limit, Expr[] bindVars)
    throws Throwable
  {
    boolean failed = false;
    boolean localTx = tx==null;
    if (localTx)
      tx = session.beginTransaction();

    // --- Transaction Start ---
    try {
      if (action == StmtHQL.ACTION_DELETE)
        return new Integer(deleteObjects(hql, bindVars));  // hib::delete

      if (col != null)
        return filter(col, hql, bindVars);                 // hib::query in col

      // Query/Iterate
      Query qry = session.createQuery(hql).setFirstResult(from);
      if (limit > 0)
        qry.setMaxResults(limit);

      // Set the bind variables:
      int len = bindVars == null ? 0 : bindVars.length;
      for (int i=0; i<len; ++i) {
        ExprHibernateBindVar hbv = (ExprHibernateBindVar)bindVars[i];
        Object val = hbv.getValue();
        Type type = (Type)hbv.getHibernateType();

        if (type == null)
          qry.setEntity(hbv.bindName, val);
        else
          qry.setParameter(hbv.bindName, val, type);
      }

      if (limit == 1)
        return qry.uniqueResult();

      if (action == StmtHQL.ACTION_ITERATE)
        return qry.iterate();                              // hib::iterate

      return qry.list();                                   // hib::query

    } catch(Exception e) {
      try { tx.rollback(); } catch(Exception e1) {}
      failed = true;
      throw e;
    } finally {
      if (localTx) {
        try {
          if (!failed)
            tx.commit();
        } finally {
          tx = null;
        }
      }
    }
  }

  Collection filter(Object coll, String hql, Expr[] bindVars) throws Throwable {
    int len = bindVars == null ? 0 : bindVars.length;
    if (len == 0) {
      return session.filter(coll, hql);
    } else {
      Object[] oa = getObjectTypes(len, bindVars);
      return session.filter(coll, hql, (Object[])oa[0], (Type[])oa[1]);
    }
  }

  int deleteObjects(String hql, Expr[] bindVars) throws Throwable {
    int len = bindVars == null ? 0 : bindVars.length;
    if (len == 0) {
      len = session.delete(hql);
    } else {
      Object[] oa = getObjectTypes(len, bindVars);
      len = session.delete(hql, (Object[])oa[0], (Type[])oa[1]);
    }
    return len;
  }

  static Object[] getObjectTypes(int len, Expr[] bindVars) throws Throwable {
    Object[] oa = new Object[len];
    Type[] ta = new Type[len];
    for (int i=0; i<len; ++i) {
      ExprHibernateBindVar hbv = (ExprHibernateBindVar)bindVars[i];
      oa[i] = hbv.getValue();
      ta[i] = (Type)hbv.getHibernateType();
    }
    return new Object[]{ oa, ta };
  }

  /**
   * If this operation is in a transaction, don't commit it;
   * otherwise, auto-commit this transaction.
   */
  public void objDo(int type, Object val, Serializable id) throws Exception {
    boolean localTx = tx==null;
    if (localTx)
      tx = session.beginTransaction();

    // --- Transaction Start ---
    try {
      switch(type) {
      case OP_SAVE:
        if (id==null)
          session.saveOrUpdate(val);
        else
          session.save(val, id);
        break;
      case OP_UPDATE:
        if (id==null)
          session.update(val);
        else
          session.update(val, id);
        break;
      case OP_SAVEORUPDATECOPY:
        if (id==null)
          session.saveOrUpdateCopy(val);
        else
          session.saveOrUpdateCopy(val, id);
        break;
      case OP_DELETE:
        session.delete(val);
        break;
      default:
        return;
      }
      // --- Transaction Finish ---

      if (localTx)
        tx.commit();
    } catch(Exception e) {
      try { tx.rollback(); } catch(Exception e1) {}
      throw e;
    } finally {
      if (localTx)
        tx = null;
    }
  }

  protected Object getSession() {
    return session;
  }

  public Class getIdentifierClass(Class clazz) throws Exception {
    return session.getSessionFactory().getClassMetadata(clazz).getIdentifierType().getReturnedClass();
  }

} // end of class Hibernate3Session.
