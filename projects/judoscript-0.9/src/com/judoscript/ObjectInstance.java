/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 03-17-2002  JH   Initial open source release.
 * 06-23-2002  JH   Added isA() method.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript;

import java.util.*;
import com.judoscript.bio.*;
import com.judoscript.util.*;
import com.judoscript.util.XMLWriter;


/**
 * Base class for all non-value objects, including built-in and user-defined.
 *<p>
 * The protocol is, a concrete ObjectInstance instance should have an empty
 * constructor, and optionally implements init().
 * NewObject operator new an instance w/o parameters, then call the init().
 * init() expects parmeters as either Expr[] (for sequential initializers)
 * or HashMap (for named initializers).
 *<p>
 * Default ObjectInstance.init() throws a bunch of runtime exceptions.
 *
 *@see com.judoscript.NewObject
 */
public abstract class ObjectInstance extends VariableAdapter implements Frame
{
  private static int rtID = 1;

  boolean readOnly;
  protected int id;

  public ObjectInstance() {
    id = rtID++;
    readOnly = false;
  }

  // See JavaExtension.java.
  public void init(Object inits, int[] javaTypes) throws Throwable { init(inits); }

  /**
   *@param inits either Expr[] sequential initialization or AssociatedList for named initialization.
   */
  public void init(Object inits) throws Throwable {
    if (inits == null)
      return;
    if (inits instanceof Expr[])
      ExceptionRuntime.rte(RTERR_OBJECT_INIT, getTypeName()+" does not take sequential initializers.");
    if (inits instanceof AssociateList)
      ExceptionRuntime.rte(RTERR_OBJECT_INIT, getTypeName()+" does not take named initializers.");
    ExceptionRuntime.rte(RTERR_OBJECT_INIT,"Invalid initializers");
  }

  public Variable setVariable(String name, Variable var, int type) throws Throwable {
    ExceptionRuntime.rte(RTERR_ILLEGAL_VALUE_SETTING, "Attempting to set value for "+getTypeName());
    return null;
  }

  public String getLabel() { return null; }

  public void removeVariable(String name) throws Throwable {}
  public void clearVariables() {}
  public Variable resolveVariable(String name) throws Throwable { return ValueSpecial.UNDEFINED; }
  public boolean hasVariable(String name) {
    try { return !resolveVariable(name).isNil(); } catch(Throwable e) { return false; }
  }

  public Variable resolveVariable(Variable n) throws Throwable {
    return resolveVariable(n.getStringValue());
  }
  public Variable setVariable(Variable n, Variable var, int type) throws Throwable {
    return setVariable(n.getStringValue(), var, type);
  }
  public boolean  hasVariable(Variable n) {
    try { return hasVariable(n.getStringValue()); } catch(Throwable e) { return false; }
  }
  public void setLocal(Variable n) {
    try { setLocal(n.getStringValue()); } catch(Throwable e) {}
  }
  public boolean isLocal(Variable n) {
    try { return isLocal(n.getStringValue()); } catch(Throwable e) { return false; }
  }
  public void removeVariable(Variable n) throws Throwable { removeVariable(n.getStringValue()); }

  ///////////////////////////////////////////////////////
  // ExprCollective implementation
  //

  public Variable resolveRange(Variable low, Variable hi) throws Throwable {
    ExceptionRuntime.rte(RTERR_NO_SUCH_VALUE, getTypeName()+" has no ranged value.");
    return null;
  }
  public Variable addVariable(Variable var, int type) throws Throwable {
    ExceptionRuntime.rte(RTERR_ILLEGAL_VALUE_SETTING, getTypeName() + " doesn't support appending values.");
    return null;
  }

  ///////////////////////////////////////////////////////
  // Expr implementation
  //

  public Variable eval() throws Throwable { return this; }
  public Variable cloneValue() { return this; }
  public Expr reduce(Stack stack) { return this; }
  public Expr optimize() { return this; }

  public boolean getBoolValue() throws ExceptionRuntime, Throwable {
    return true;
  }
  public long getLongValue() throws ExceptionRuntime, Throwable {
    ExceptionRuntime.rte(RTERR_NO_SUCH_VALUE, getTypeName()+" has no integer value.");
    return 0;
  }
  public double getDoubleValue() throws ExceptionRuntime, Throwable {
    ExceptionRuntime.rte(RTERR_NO_SUCH_VALUE, getTypeName()+" has no double value.");
    return 0;
  }
  public String getStringValue() throws Throwable {
    return getTypeName() + "@" + Integer.toHexString(id);
  }
  public Object getObjectValue() throws Throwable { return this; }
  public Date getDateValue() throws ExceptionRuntime, Throwable {
    ExceptionRuntime.rte(RTERR_NO_SUCH_VALUE, getTypeName()+" has no date value.");
    return null;
  }

  ///////////////////////////////////////////////////////
  // Variable implementation
  //

  public int getType() { return (this instanceof JavaObject) ? TYPE_JAVA : TYPE_OBJECT; }
  public boolean isObject() { return true; }

  public long getEpoch() throws Throwable { return getDateValue().getTime(); }
  public java.sql.Date getSqlDate() throws ExceptionRuntime {
    ExceptionRuntime.rte(RTERR_NO_SUCH_VALUE, "Object has no SQL date value.");
    return null;
  }
  public java.sql.Time getSqlTime() throws ExceptionRuntime {
    ExceptionRuntime.rte(RTERR_NO_SUCH_VALUE, "Object has no SQL time value.");
    return null;
  }
  public java.sql.Timestamp getSqlTimestamp() throws ExceptionRuntime {
    ExceptionRuntime.rte(RTERR_NO_SUCH_VALUE, "Object has no SQL timestamp value.");
    return null;
  }

  public void dump(XMLWriter out) {
    out.openTag("Object");
    out.tagAttr("type", getTypeName());
    out.tagAttr("id", id);
    out.closeSingleTag();
  }

  public void    setLocal(String name) {}
  public boolean isLocal(String name) { return true; }
  public boolean isTerminal() { return false; }
  public boolean isFunction() { return false; }

  public final void    setReadOnly(boolean set) { readOnly = set; }
  public final boolean getReadOnly() { return readOnly; }
  public final void    checkWritable() throws ExceptionRuntime {
    if (readOnly)
      ExceptionRuntime.rte(RTERR_ILLEGAL_VALUE_SETTING,
                           "Trying to set values on a read-only " + getTypeName() + ".");
  }

  public String toString() {
    try { return getStringValue(); } catch(Throwable e) {}
    return "";
  }

} // end of class ObjectInstance.

