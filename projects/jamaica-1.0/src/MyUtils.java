/* Jamaica, The Java Virtual Machine (JVM) Macro Assembly Language
 * Copyright (C) 2004- James Huang,
 * http://www.judoscript.com/jamaica/index.html
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
 * 03-14-2004  JH   Initial release.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/

package com.judoscript.jamaica;

import org.apache.commons.lang.StringEscapeUtils;
import java.util.Collection;
import java.util.Iterator;

public class MyUtils extends StringEscapeUtils
{
  public static String[] toStringArray(Collection col) {
    int len = (col==null) ? 0 : col.size();
    String[] sa = new String[len];
    Iterator iter = col.iterator();
    int i=0;
    while (iter.hasNext())
      sa[i++] = iter.next().toString();
    return sa;
  }

  public static String processStringLiteral(String lit) {
    return unescapeJava(lit.substring(1, lit.length()-1));
  }

  public static Object parseCharLiteral(String lit, String typeHint) {
    lit = unescapeJava(lit);
    char ch = lit.charAt(1);
    return (typeHint != null) ? number2object(ch, typeHint) : new Character(ch);
  }

  public static Object parseIntObject(String x, String typeHint) {
    boolean isLong = x.endsWith("l") || x.endsWith("L");
    if (isLong) {
      x = x.substring(0, x.length()-1);
      if (typeHint == null)
        typeHint = "long";
    }

    long l;
    try {
      if (x.charAt(0) != '0') {
        l = Long.parseLong(x);  // Decimal
      } else if (x.length() == 1) {
        l = 0;
      } else {
        char ch = x.charAt(1); // if exceptions, it's 0 anyway.
        if ((ch=='x') || (ch=='X'))
          l = Long.parseLong(x.substring(2),16);  // Hex
        else
          l = Long.parseLong(x,8);  // Octal
      }
    } catch(Exception e) { l = 0; }

    if (typeHint != null)
      return number2object(l, typeHint);
    else if (isLong)
      return new Long(l);
    else
      return new Integer((int)l);
  }

  public static Object parseFloatObject(String x, String typeHint) {
    char ch = x.charAt(x.length()-1);
    boolean isDouble = ch=='d' || ch=='D';
    if (ch=='f' || ch=='F' || ch=='d' || ch=='D')
      x = x.substring(0,x.length()-1);
    isDouble |= "double".equals(typeHint);
    if (isDouble && typeHint == null)
      typeHint = "double";

    double d;
    try { d = Double.parseDouble(x); } catch(Exception e) { d = 0; }
    if (typeHint != null)
      return number2object(d, typeHint);
    if (isDouble)
      return new Double(d);
    else
      return new Float((float)d);
  }

  public static Object number2object(long val, String typeHint) {
    if (typeHint != null) {
      if (typeHint.equals("int"))
        return new Integer((int)val);
      if (typeHint.equals("long"))
        return new Long(val);
      if (typeHint.equals("short"))
        return new Short((short)val);
      if (typeHint.equals("char"))
        return new Character((char)val);
      if (typeHint.equals("byte"))
        return new Byte((byte)val);
      if (typeHint.equals("double"))
        return new Double(val);
      if (typeHint.equals("float"))
        return new Float(val);
    }
    return new Long(val);
  }

  public static Object number2object(double val, String typeHint) {
    if (typeHint != null) {
      if (typeHint.equals("double"))
        return new Double(val);
      if (typeHint.equals("float"))
        return new Float((float)val);
      if (typeHint.equals("int"))
        return new Integer((int)val);
      if (typeHint.equals("long"))
        return new Long((long)val);
      if (typeHint.equals("short"))
        return new Short((short)val);
      if (typeHint.equals("char"))
        return new Character((char)val);
      if (typeHint.equals("byte"))
        return new Byte((byte)val);
    }
    return new Double(val);
  }

  public static Object value2object(Object val, String typeHint) {
    if (typeHint == null)
      return val;

    if (typeHint.equals("boolean") || val instanceof Boolean)
      return val;  // leave type check to caller.

    double v;
    if (val instanceof Character)
      v = ((Character)val).charValue();
    else if (val instanceof Number)
      v = ((Number)val).doubleValue();
    else
      return val;  // leave type check to caller.

    if (typeHint.equals("int"))
      return new Integer((int)v);
    if (typeHint.equals("long"))
      return new Long((long)v);
    if (typeHint.equals("short"))
      return new Short((short)v);
    if (typeHint.equals("char"))
      return new Character((char)v);
    if (typeHint.equals("byte"))
      return new Byte((byte)v);
    if (typeHint.equals("float"))
      return new Float((float)v);
//  if (typeHint.equals("double"))
      return new Double(v);
  }

  public static int object2int(Object o) throws Exception {
    if (o instanceof Number) return ((Number)o).intValue();
    if (o instanceof Character) return ((Character)o).charValue();
    if (o instanceof Boolean) return ((Boolean)o).booleanValue() ? 1 : 0;
    throw new Exception(o.toString() + " is not a number.");
  }

  public static int parseInt(String x) {
    try {
      if (x.endsWith("l") || x.endsWith("L"))
        x = x.substring(0, x.length()-1);

      if (x.charAt(0) == '0') {
        if (x.length() == 1)
          return 0;
        char ch = x.charAt(1); // if exceptions, it's 0 anyway.
        if ((ch=='x') || (ch=='X'))
          return Integer.parseInt(x.substring(2),16);  // Hex
        return Integer.parseInt(x,8);  // Octal
      }
      return Integer.parseInt(x);  // Decimal
    } catch(Exception e) { return 0; }
  }

  public static boolean existsClass(String className) {
    try { Class.forName(className); return true; }
    catch(ClassNotFoundException cnfe) { return false; }
  }

  public static Object newInstance(String className)
    throws ClassNotFoundException, InstantiationException, IllegalAccessException
  {
    Class cls = Class.forName(className);
    return cls.newInstance();
  }

  public static String getExceptionMessage(Throwable t) {
    String msg = t.getMessage();
    return msg != null ? msg : t.getClass().getName();
  }

}
