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

import java.util.*;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.math.BigDecimal;

/*
import net.sf.hibernate.Hibernate;
import net.sf.hibernate.Session;
import net.sf.hibernate.SessionFactory;
import net.sf.hibernate.Transaction;
import net.sf.hibernate.Query;
import net.sf.hibernate.LockMode;
import net.sf.hibernate.cfg.Configuration;
import net.sf.hibernate.type.Type;
import net.sf.hibernate.type.TypeFactory;
*/

import com.judoscript.*;
import com.judoscript.util.XMLWriter;
import org.apache.commons.lang.StringUtils;

public abstract class HibernateSession implements Consts
{
  public abstract void init(Object sessionFactory) throws Exception;

  public abstract void close() throws Exception;

  /**
   *@return Transaction
   */
  public abstract Object txBegin() throws Exception;

  public abstract void txEnd() throws Exception;

  public abstract void txAbort() throws Exception;

  public abstract boolean inTx();

  public abstract void lock(Object obj, Object lockMode) throws Exception;

  public abstract Object get(Class clazz, Serializable id) throws Exception;

  /**
   *@param lock LockMode
   */
  public abstract Object get(Class clazz, Serializable id, Object lock) throws Exception;

  public abstract Class getIdentifierClass(Class clazz) throws Exception;

  /**
   *@param action is StmtHQL.ACTION_QUERY/_ITERATE/_DELETE.
   *@param bindVars is really ExprHibernateBindVar[].
   *@return Either a List or Object if limit==1, or null if action is DELETE.
   */
  public abstract Object exec(int action, Object col, String hql, int from, int limit, Expr[] bindVars)
    throws Throwable;

  /**
   * If this operation is in a transaction, don't commit it;
   * otherwise, auto-commit this transaction.
   */
  public abstract void objDo(int type, Object val, Serializable id) throws Exception;

  public static final int OP_SAVE   = 1;
  public static final int OP_SOUC   = 2;
  public static final int OP_UPDATE = 3;
  public static final int OP_DELETE = 4;
  public static final int OP_SAVEORUPDATECOPY = OP_SOUC;

  protected abstract Object getSession();

  //////////////////////////////////////////////////
  // Impl
  //
  // Each transaction starts with a per-thread session
  // and its associted transaction.
  //

  public static Expr getBindVar(String varName, String typeName, Expr typeObj, Expr val) throws Exception {
    return new ExprHibernateBindVar(varName, typeName, typeObj, val);
  }

  public static boolean bindVarsContainsEntity(Expr[] vars) {
    int len = vars == null ? 0 : vars.length;
    for (int i=0; i<len; i++) {
      if (((ExprHibernateBindVar)vars[i]).isEntity())
        return true;
    }
    return false;
  }

  /////////////////////////////////////////////////////////
  //
  //
  static final class ExprHibernateBindVar implements Expr
  {
    static Object hib_STRING = null; // Hibernate.STRING
    static Method TypeFactory_heuristicType = null;

    String bindName;
    Object type;     // type.Type
    Expr   typeObj;
    Expr   expr;

    public ExprHibernateBindVar(String name, String typeName, Expr typeObj, Expr val) throws Exception
    {
      bindName = name;
      this.typeObj = typeObj; this.expr = val;
      setType(typeName);
    }

    protected void setType(String typeName) throws Exception {
      if (StringUtils.isBlank(typeName)) {
        if (hib_STRING == null) {
          Class cls = RT.getClass(HibernateEnv.getPkgPrefix() + "Hibernate");
          hib_STRING = cls.getField("STRING").get(null);
        }
        type = hib_STRING;
      }
      else if (typeName.equals("entity") || typeName.equals("type"))
        type = null;
      else {
        if (TypeFactory_heuristicType == null) {
          Class cls = RT.getClass(HibernateEnv.getPkgPrefix() + "type.TypeFactory");
          TypeFactory_heuristicType = cls.getMethod("heuristicType", new Class[]{String.class});
        }
        try {
          typeName = RT.getClass(typeName).getName();
        } catch(Exception e) {}
        type = TypeFactory_heuristicType.invoke(null, new Object[]{typeName});
      }
    }

    /**
     *@return type.Type
     */
    public Object getHibernateType() throws Throwable {
      if (type != null)
        return type;

      if (typeObj != null) {
        // This only applies to hib::delete and hib::query for collection,
        // where 'entity' is prohibited so typeObj should be an expression
        // evaluated to a Type.
        return typeObj.getObjectValue();
      }

      return null; // indicating this is an entity
    }

    public boolean isEntity() { return (type==null) && (typeObj==null); }
    public Object getValue() throws Throwable {
      Integer I = (Integer)HibernateEnv.hibTypeOrdinals.get(getHibernateType());
      Variable var = expr.eval();
      if (I != null) {
        switch(I.intValue()) {
        case HibernateEnv.HIB_TYPE_LONG:          return new Long(var.getLongValue());
        case HibernateEnv.HIB_TYPE_SHORT:         return new Short((short)var.getLongValue());
        case HibernateEnv.HIB_TYPE_INTEGER:       return new Integer((int)var.getLongValue());
        case HibernateEnv.HIB_TYPE_BYTE:          return new Byte((byte)var.getLongValue());
        case HibernateEnv.HIB_TYPE_FLOAT:         return new Float((float)var.getDoubleValue());
        case HibernateEnv.HIB_TYPE_DOUBLE:        return new Double((double)var.getDoubleValue());
        case HibernateEnv.HIB_TYPE_CHARACTER:     return new Character(JudoUtil.toChar(var));
        case HibernateEnv.HIB_TYPE_STRING:
        case HibernateEnv.HIB_TYPE_TEXT:          return var.getStringValue();
        case HibernateEnv.HIB_TYPE_TIME:          return var.getSqlTime();
        case HibernateEnv.HIB_TYPE_DATE:          return var.getSqlDate();
        case HibernateEnv.HIB_TYPE_TIMESTAMP:     return var.getSqlTimestamp();
        case HibernateEnv.HIB_TYPE_CALENDAR:
        case HibernateEnv.HIB_TYPE_CALENDAR_DATE: Calendar cal = Calendar.getInstance();
                                                  cal.setTime(var.getDateValue());
                                                  return cal;
        case HibernateEnv.HIB_TYPE_BOOLEAN:
        case HibernateEnv.HIB_TYPE_TRUE_FALSE:
        case HibernateEnv.HIB_TYPE_YES_NO:        return var.getBoolValue() ? Boolean.TRUE: Boolean.FALSE;
        case HibernateEnv.HIB_TYPE_BIG_DECIMAL:   return new BigDecimal(var.getDoubleValue());
/*
        case HibernateEnv.HIB_TYPE_BINARY:
        case HibernateEnv.HIB_TYPE_BLOB:
        case HibernateEnv.HIB_TYPE_CLOB:
        case HibernateEnv.HIB_TYPE_LOCALE:
        case HibernateEnv.HIB_TYPE_CURRENCY:
        case HibernateEnv.HIB_TYPE_TIMEZONE:
        case HibernateEnv.HIB_TYPE_CLASS:
        case HibernateEnv.HIB_TYPE_SERIALIZABLE:
*/
        }
      }
      Object o = var.getObjectValue();
      try {
        if (I.intValue() == HibernateEnv.HIB_TYPE_BINARY && !(o instanceof byte[]))
          o = o.toString().getBytes();
      } catch(Exception e) {}
      return o;
    }

    public int getType() { return expr.getType(); }
    public int getJavaPrimitiveType() { return expr.getJavaPrimitiveType(); }
    public boolean isNil()          { return expr.isNil(); }
    public boolean isUnknownType()  { return expr.isUnknownType(); }
    public boolean isInt()          { return expr.isInt(); }
    public boolean isDouble()       { return expr.isDouble(); }
    public boolean isNumber()       { return expr.isNumber(); }
    public boolean isString()       { return expr.isString(); }
    public boolean isValue()        { return expr.isValue(); }
    public boolean isDate()         { return expr.isDate(); }
    public boolean isObject()       { return expr.isObject(); }
    public boolean isJava()         { return expr.isJava(); }
    public boolean isCOM()          { return expr.isCOM(); }
    public boolean isFunction()     { return expr.isFunction(); }
    public boolean isArray()        { return expr.isArray(); }
    public boolean isSet()          { return expr.isSet(); }
    public boolean isStack()        { return expr.isStack(); }
    public boolean isQueue()        { return expr.isQueue(); }
    public boolean isStruct()       { return expr.isStruct(); }
    public boolean isComplex()      { return expr.isComplex(); }
    public boolean isWebService()   { return expr.isWebService(); }
    public boolean isA(String name) { return expr.isA(name); }
    public boolean isReadOnly() { return expr.isReadOnly(); }
    public Expr reduce(Stack stack) { expr = expr.reduce(stack); return this; }
    public Expr optimize() { return this; }

    public Variable eval() throws Throwable { return expr.eval(); }
    public boolean getBoolValue()   throws Throwable { return expr.getBoolValue(); }
    public long    getLongValue()   throws Throwable { return expr.getLongValue(); }
    public double  getDoubleValue() throws Throwable { return expr.getDoubleValue(); }
    public String  getStringValue() throws Throwable { return expr.getStringValue(); }
    public Object  getObjectValue() throws Throwable { return expr.getObjectValue(); }
    public Date    getDateValue()   throws Throwable { return expr.getDateValue(); }

    public void dump(XMLWriter out) {
      out.openTag("StmtHQL.ExprHibernateBindVar");
      out.closeTag();
      expr.dump(out);
      out.endTag();
    }

  } // end of inner class ExprHibernateBindVar.

} // end of class HibernateSession.
