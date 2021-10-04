/* JudoScript, The Scripting Solution for the Java Platform
 * Copyright (C) 2001-2002 James Huang, http://www.judoscript.com
 *
 * This is free software; you can embed, modify and redistribute
 * it under the terms of the GNU Lesser General Public License
 * version 2.1 or up as published by the Free Software Foundation,
 * which you should have received a copy along with this software.
 * In case you did not, please download it from the internet at
 * http://www.gnu.org/copyleft/lesser.html
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 12-14-2002  JH   Inception.
 * 04-18-2004  JH   Added getIterator() for IEnumVARIANT.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript.ext.win32;

import java.util.List;
import java.util.Iterator;
import jp.ne.so_net.ga2.no_ji.jcom.*;
import com.judoscript.*;
import com.judoscript.util.RangeIterator;
import com.judoscript.util.ListRangeIterator;

public class ComIDispatch extends ObjectInstance implements ExprCollective
{
  IDispatch dispatch;

  public ComIDispatch(IDispatch disp) { dispatch = disp; }

  public int getType() { return TYPE_COM; }
  public String getTypeName() { return "ComIDispatch"; }
  public int size() { return 0; } // unknown.

  public Variable invoke(String fxn, Expr[] params, int[] javaTypes) throws Throwable
  {
    // IUnknown methods
    if (fxn.equals("queryInterface")) {
      if (params.length < 2) {
        ExceptionRuntime.rte(RTERR_ILLEGAL_ARGUMENTS,
                             "queryInterface() takes a Java class name and a guid");
      }
      Object id = params[1].getObjectValue();
      GUID guid = (id instanceof GUID) ? (GUID)id : GUID.parse(id.toString());
      return JComUtil.wrap(dispatch.queryInterface(params[0].getStringValue(), guid));
    } else if (fxn.equals("getIDispatch")) {
      return this;
    } else if (fxn.equals("getIEnumVARIANT")) {
      return JComUtil.getIEnumVARIANTWrapped(dispatch);
    } else if (fxn.equals("addRef")) {
      return ConstInt.getInt(dispatch.addRef());
    } else if (fxn.equals("release")) {
      return ConstInt.getBool(dispatch.release());
    }

    int len = (params==null) ? 0 : params.length;
    int typelen = (javaTypes==null) ? 0 : javaTypes.length;
    Object[] oa = new Object[len];
    for (int i=0; i<len; ++i)
      oa[i] = toObject(params[i].eval(), (i<typelen) ? javaTypes[i] : 0);
    return JudoUtil.toVariable(dispatch.method(fxn,oa));
  }

  public Variable setVariable(String name, Variable var, int type) throws Throwable {
    if (type == 0) type = var.getJavaPrimitiveType();
    dispatch.put(name, toObject(var,type));
    return var;
  }

  public Variable resolveVariable(String name) throws Throwable {
    try { return JudoUtil.toVariable(dispatch.get(name)); }
    catch(Exception e) { return ValueSpecial.NIL; }
  }

  public Variable resolve(Variable name) {
    try { return JudoUtil.toVariable(dispatch.get(name.getStringValue())); }
    catch(Throwable e) { return ValueSpecial.NIL; }
  }

  public Variable resolve(Variable[] keys) {
    try {
      Object[] oa = new Object[keys.length-1];
      for (int i=0; i<oa.length; i++)
        oa[i] = keys[i+1].getObjectValue();
      return JudoUtil.toVariable(dispatch.get(keys[0].getStringValue(), oa));
    } catch(Throwable e) { return ValueSpecial.NIL; }
  }

  public Variable setVariable(Variable idx, Variable val, int type) throws Exception {
    ExceptionRuntime.rte(RTERR_ILLEGAL_VALUE_SETTING,"COM/ActiveX do not support indexed value setting.");
    return null;
  }

  public Variable setVariable(Variable[] dims, Variable val, int type) throws Exception {
    ExceptionRuntime.rte(RTERR_ILLEGAL_VALUE_SETTING,"COM/ActiveX do not support indexed value setting.");
    return null;
  }

  public Iterator getIterator(int start, int end, int step, boolean upto, boolean backward) throws Exception {
    List list = null;
    try {
      list = JComUtil.getIEnumVARIANT(dispatch);
    } catch(Exception e) {
      ExceptionRuntime.rte(RTERR_ILLEGAL_ARGUMENTS, "No IEnumVARIANT interface found for this COM object.");
    }
    return backward ? new ListRangeIterator(list, start, end, step, upto, backward)
                    : RangeIterator.getIterator(list, start, end, step, upto);
  }

  static Object toObject(Variable v, int type) throws Throwable {
    return (type!=JAVA_CURRENCY) ? v.getObjectValue() : new VariantCurrency(v.getDoubleValue());
  }

} // end of class ComIDispatch.

/***
#define DISP_E_UNKNOWNINTERFACE 0x80020001L
#define DISP_E_MEMBERNOTFOUND   0x80020003L
#define DISP_E_PARAMNOTFOUND    0x80020004L
#define DISP_E_TYPEMISMATCH     0x80020005L
#define DISP_E_UNKNOWNNAME      0x80020006L
#define DISP_E_NONAMEDARGS      0x80020007L
#define DISP_E_BADVARTYPE       0x80020008L
#define DISP_E_EXCEPTION        0x80020009L
#define DISP_E_OVERFLOW         0x8002000AL
#define DISP_E_BADINDEX         0x8002000BL
#define DISP_E_UNKNOWNLCID      0x8002000CL
#define DISP_E_ARRAYISLOCKED    0x8002000DL
#define DISP_E_BADPARAMCOUNT    0x8002000EL
#define DISP_E_PARAMNOTOPTIONAL 0x8002000FL

#define TYPE_E_ELEMENTNOTFOUND  0x8002802BL
#define TYPE_E_CANTLOADLIBRARY  0x80029C4AL
***/
