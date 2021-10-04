/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 03-17-2002  JH   Initial open source release.
 * 11-13-2002  JH   Added ${XYZ} support in string literals.
 * 12-13-2004  JH   Added support for ExprCollective interface.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript;

import java.util.Iterator;
import org.apache.commons.lang.StringUtils;
import com.judoscript.bio.JavaArray;
import com.judoscript.util.XMLWriter;


public class ConstString extends ValueBase implements ExprCollective
{
  String val;
  int javaType = JAVA_STRING;

  public String toString() { return val; }
  public int hashCode() { return val==null ? 0 : val.hashCode(); }
  public boolean equals(Object o) { return o.toString().equals(val); }

  public ConstString() { val = ""; }
  public ConstString(String s) { val = StringUtils.defaultString(s); }
  ConstString(char c) { val = String.valueOf(c); }

  public String getTypeName() { return "String"; }
  public int getType() { return TYPE_STRING; }
  public int getJavaPrimitiveType() { return javaType; }
  public void setJavaPrimitiveType(int type) { javaType = type; }

  public boolean getBoolValue() throws Throwable {
    if (StringUtils.isBlank(val)) return false;
    try { return 0 != Long.parseLong(val); } catch(Exception e) { return true; }
  }

  public double getDoubleValue() throws Throwable {
    String x = val.trim();
    if (x.length() == 0) return 0;
    try {
      if (x.charAt(0) == '0') {
        if (x.length() == 1)
          return 0;
        switch(x.charAt(1)) {
        case 'x':
        case 'X':  return Long.parseLong(x.substring(2),16);  // Hex
        case '.':  break;
        default:   return Long.parseLong(x,8);  // Octal
        }
      } else if (x.endsWith("%")) {
        return Double.parseDouble(x.substring(0,x.length()-1)) / 100.0;
      }
      return Double.parseDouble(x);  // Decimal/Float
    } catch(NumberFormatException nfe) {
      throw ExceptionSpecialValue.NaN;
    }
  }

  public long getLongValue() throws Throwable {
    String x = val.trim();
    if (x.length() == 0) return 0;
    try {
      if (x.charAt(0) == '0') {
        if (x.length() == 1)
          return 0;
        char ch = x.charAt(1); // if exceptions, it's 0 anyway.
        if ((ch=='x') || (ch=='X'))
          return Long.parseLong(x.substring(2),16);  // Hex
        return Long.parseLong(x,8);  // Octal
      } else if (x.endsWith("%")) {
        return (long)getDoubleValue();
      }
      return Long.parseLong(x);  // Decimal
    } catch(NumberFormatException nfe) {
      throw ExceptionSpecialValue.NaN;
    }
  }
  public String getStringValue() throws Throwable { return val; }
  public Object getObjectValue() throws Throwable { return val; }
  public java.util.Date getDateValue() throws Throwable { return parseDate(null); }
  public java.sql.Date getSqlDate() throws Throwable {
    return new java.sql.Date(getDateValue().getTime());
  }
  public java.sql.Time getSqlTime() throws Throwable {
    return new java.sql.Time(getDateValue().getTime());
  }
  public java.sql.Timestamp getSqlTimestamp() throws Throwable {
    return new java.sql.Timestamp(getDateValue().getTime());
  }

  public Variable cloneValue() { return JudoUtil.toVariable(val); }
  public boolean isNil() { return (val==null); }

  public String concat(String v) {
    if (val==null) val = v;
    else val += v;
    return val;
  }
  public void setStringValue(String v) { val = v; }
  public void setBoolValue(boolean b) { val = b ? "1" : "0"; }
  public void setLongValue(long v) { val = String.valueOf(v); }
  public void setDoubleValue(double v) { val = String.valueOf(v); }

  public boolean isReadOnly() { return true; }

  public void dump(XMLWriter out) { out.printQuoted(val); }

  public static final String newline    = System.getProperty("line.separator");
  public static final ConstString NL    = new ConstString(newline);
  public static final ConstString TRUE  = new ConstString("true");
  public static final ConstString FALSE = new ConstString("false");
  public static final ConstString SPACE = new ConstString(" ");
  public static final ConstString EMPTY = new ConstString("");
  public static final ConstString EOF   = new ConstString("\000\000\000");

  public static char toChar(Variable var) {
    try {
      String s = var.getStringValue();
      if ((s != null) && (s.length() > 0))
        return s.charAt(0);
    } catch(Throwable e) {}
    return ' ';
  }
  public static char toChar(String s) {
    if ((s == null) || (s.length() == 0)) return ' ';
    return s.charAt(0);
  }


  // ExprCollective methods
  public Variable resolve(Variable idx) throws Throwable {
    int i = (int)idx.getLongValue();
    if (i < val.length())
      return new ConstString(val.charAt(i));
    return ValueSpecial.UNDEFINED;
  }

  public Variable resolveRange(Variable low, Variable hi) throws Throwable {
    int iLow = (int)low.getLongValue();
    if (iLow < 0)
      iLow = 0;
    int iHi = (int)hi.getLongValue();
    if (iHi >= val.length())
      iHi = val.length();
    else
      ++iHi;
    try {
      return JudoUtil.toVariable(val.substring(iLow, iHi));
    } catch(Exception e) {}
    return ValueSpecial.UNDEFINED;
  }

  public Variable resolve(Variable[] dims) throws Throwable {
    if (dims.length == 1)
      return resolve(dims[0]);
    ExceptionRuntime.rte(RTERR_ILLEGAL_ACCESS, "String doesn't have multi-dimensional access.");
    return null;
  }

  public Variable setVariable(Variable idx, Variable val, int type) throws Throwable {
    ExceptionRuntime.rte(RTERR_ILLEGAL_ACCESS, "Can't set values to string.");
    return null;
  }

  public Variable setVariable(Variable[] dims, Variable val, int type) throws Throwable {
    ExceptionRuntime.rte(RTERR_ILLEGAL_ACCESS, "Can't set values to string.");
    return null;
  }

  public Variable addVariable(Variable val, int type) throws Throwable {
    ExceptionRuntime.rte(RTERR_ILLEGAL_ACCESS, "Can't append value to string.");
    return null;
  }

  public Iterator getIterator(int start, int to, int step, boolean upto, boolean backward) throws Throwable {
    JavaArray ja = (JavaArray)JudoUtil.toVariable(val.toCharArray());
    return ja.getIterator(start, to, step, upto, backward);
  }

  public int size() { return val.length(); }


  public static final class InternalName extends ConstString
  {
    public InternalName(String name) { super(name); }
    public int getType() { return TYPE_INTERNAL; }
    public Variable cloneValue() { return this; } // always singleton!
  }

} // end of class ConstString.
