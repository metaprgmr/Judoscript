/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 03-17-2002  JH   Initial open source release.
 * 06-21-2002  JH   Overrode getValue() to return values in order.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript.bio;

import java.util.*;
import com.judoscript.*;


public class OrderedMap extends UserDefined
{
  ArrayList keys = new ArrayList();

  public OrderedMap() { super(); }
  public OrderedMap(Type t) { super(t); }
//  public OrderedMap(HashMap ht) throws Exception { super(ht); }

  public final String getTypeName() { return (ot == null) ? "OrderedMap" : ot.getName(); }

  public Variable setVariable(Variable name, Variable val, int type) throws Exception {
    if (val == null) {
      removeVariable(name);
      return ValueSpecial.UNDEFINED;
    }
    name = name.cloneValue();
    if (!keys.contains(name)) keys.add(name);
    storage.put(name,val.cloneValue());
    return val;
  }
  public void removeVariable(Variable name) {
    storage.remove(name);
    keys.remove(name);
  }
  public void clearVariables() { storage.clear(); keys.clear(); }

  public Iterator getKeys() { return keys.iterator(); }

  public Variable getValues() throws Throwable {
    _Array arr = new _Array();
    if (size() > 0) {
      for (int i=0; i<keys.size(); i++)
        arr.append(resolveVariable((Variable)keys.get(i)));
    }
    return arr;
  }

  public Variable invoke(String fxn, Expr[] params, int[] types) throws Throwable {
    int ord = getMethodOrdinal(fxn);
    if (ord != BIM_INDEXOF)
      return super.invoke(ord,fxn,params);

    // Implement "indexOf"
    int len = (params==null) ? 0 : params.length;
    if (len <= 0) return ConstInt.MINUSONE;
    int id = 0;
    if (len > 1) id = (int)params[1].getLongValue();
    String v = params[0].getStringValue();
    for (int i=id; i<keys.size(); i++) {
      if (v.equals(keys.get(i)))
        return ConstInt.getInt(i);
    }
    return ConstInt.MINUSONE;
  }

} // end of class OrderedMap.
