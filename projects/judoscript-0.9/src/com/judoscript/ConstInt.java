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


public final class ConstInt extends ValueBase
{
  long val;
  int  javaType;

  public ConstInt() { val = 0; javaType = JAVA_INT; }
  public ConstInt(int i) { val = i; javaType = JAVA_INT; }
  public ConstInt(long i) {
    val = i;
    javaType = ((i>=Integer.MIN_VALUE)&&(i<=Integer.MAX_VALUE)) ? JAVA_INT : JAVA_LONG;
  }
  public ConstInt(int i, int type) { val = i; javaType = type; }

  public String getTypeName() { return "Integer"; }
  public int getType() { return TYPE_INT; }
  public int getJavaPrimitiveType() { return javaType; }
  public void setJavaPrimitiveType(int type) { javaType = type; }

  public String  getStringValue() throws Throwable {
    switch(javaType) {
    case JAVA_BOOLEAN: return (val==0) ? "false": "true";
    case JAVA_CHAR:    return String.valueOf((char)val);
    case JAVA_BYTE:
    case JAVA_SHORT:
    case JAVA_INT:
    default:           return String.valueOf(val);
    }
  }
  public boolean getBoolValue() throws Throwable { return (val != 0); }
  public double  getDoubleValue() throws Throwable { return (double)val; }
  public long    getLongValue() throws Throwable { return val; }
  public Variable cloneValue() { ConstInt ci = new ConstInt(val); ci.javaType=javaType; return ci; }
  public Object  getObjectValue() throws Throwable {
    switch(javaType) {
    case JAVA_BOOLEAN: return (val==0) ? Boolean.FALSE: Boolean.TRUE;
    case JAVA_BYTE:    return new Byte((byte)val);
    case JAVA_CHAR:    return new Character((char)val);
    case JAVA_SHORT:   return new Short((short)val);
    case JAVA_INT:     return new Integer((int)val);
    case JAVA_LONG:    return new Long(val);
    default:           int x = (int)(val >> 32);
                       if (x == 0 || x == 0x0FFFFFFFF)
                         return new Integer((int)val);
                       return new Long(val);
    }
  }
  public java.util.Date getDateValue() throws Throwable { return new java.util.Date(val); }
  public java.sql.Date getSqlDate() { return new java.sql.Date(val); }
  public java.sql.Time getSqlTime() { return new java.sql.Time(val); }
  public java.sql.Timestamp getSqlTimestamp() { return new java.sql.Timestamp(val); }

  public String concat(String v) { return null; }
  public void setStringValue(String v) {
    try { val = Integer.parseInt(v); } catch(Exception e) { val = 0; }
  }
  public void setBoolValue(boolean b) { val = b ? 1 : 0; }
  public void setLongValue(long v) { val = v; }
  public void setDoubleValue(double v) { val = (long)v; }

  public boolean isReadOnly() { return true; }

  public void dump(XMLWriter out) { out.print(val); }
  
  public static ConstInt getBool(boolean b) { return b ? TRUE : FALSE; }
  public static ConstInt getInt(byte i)     { return new ConstInt(i,JAVA_BYTE); }
  public static ConstInt getInt(char i)     { return new ConstInt(i,JAVA_CHAR); }
  public static ConstInt getInt(short i)    { return new ConstInt(i,JAVA_SHORT); }
  public static ConstInt getInt(int i) {
    if (i==0)  return ZERO;
    if (i==1)  return ONE;
    if (i==-1) return MINUSONE;
    return new ConstInt(i,JAVA_SHORT);
  }
  public static ConstInt getInt(long i) {
    if (i==0)  return ZERO;
    if (i==1)  return ONE;
    if (i==-1) return MINUSONE;
    return new ConstInt(i);
  }

  public static final ConstInt ZERO     = new ConstInt(0, JAVA_INT);
  public static final ConstInt ONE      = new ConstInt(1, JAVA_INT);
  public static final ConstInt MINUSONE = new ConstInt(-1, JAVA_INT);
  public static final ConstInt MAX      = new ConstInt(Integer.MAX_VALUE, JAVA_INT);
  public static final ConstInt TRUE     = new ConstInt(1, JAVA_BOOLEAN);
  public static final ConstInt FALSE    = new ConstInt(0, JAVA_BOOLEAN);
}
