/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 03-17-2002  JH   Initial open source release.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript;

import com.judoscript.util.XMLWriter;

public final class ConstDouble extends ValueBase
{
  double val;
  int javaType;

  public ConstDouble() { this(0.0, 0); }
  public ConstDouble(double d) { this(d, 0); }
  public ConstDouble(double d, int type) { val = d; javaType = (type==0) ? JAVA_DOUBLE : type; }

  public String getTypeName() { return "Float"; }
  public int getType() { return TYPE_DOUBLE; }
  public int getJavaPrimitiveType() { return javaType; }

  public String  getStringValue() throws Throwable { return String.valueOf(val); }
  public Object  getObjectValue() throws Throwable { return new Double(val); }
  public boolean getBoolValue() throws Throwable { return (0 != (long)val); }
  public long    getLongValue() throws Throwable { return (long)val; }
  public double  getDoubleValue() throws Throwable { return val; }
  public Variable cloneValue() { ConstDouble d=new ConstDouble(val,javaType); d.javaType=javaType; return d; }
  public java.util.Date getDateValue() throws Throwable { return new java.util.Date((long)val); }
  public java.sql.Date  getSqlDate()   { return new java.sql.Date((long)val); }
  public java.sql.Time  getSqlTime()   { return new java.sql.Time((long)val); }
  public java.sql.Timestamp getSqlTimestamp() { return new java.sql.Timestamp((long)val); }

  public String concat(String v) { return null;}
  public void setStringValue(String v) {
    try { val = Double.parseDouble(v); } catch(Exception e) { val = 0; }
  }
  public void setBoolValue(boolean b) { val = b ? 1.0 : 0.0; }
  public void setLongValue(long v) { val = v; }
  public void setDoubleValue(double v) { val = v; }

  public boolean isReadOnly() { return true; }

  public static Variable getDouble(double val) {
    if (val == 0.0) return ConstInt.ZERO;
    if (val == 1.0) return ConstInt.ONE;
    if (val == -1.0) return ConstInt.MINUSONE;
    return new ConstDouble(val);
  }

  public void dump(XMLWriter out) { out.print(val); }
}
