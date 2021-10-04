/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 10-10-2002  JH   Inception.
 * 04-26-2002  JH   Added support for contains() to take regex's.
 * 06-03-2002  JH   Reimplemented equals() method; compare as string, double
 *                  or object in this order. This is important for struct keys.
 * 06-25-2002  JH   Added isOdd and isEven user methods.
 * 07-09-2002  JH   Added fmtRoman/formatRoman() and parseIntRoman() user methods.
 * 03-27-2005  JH   Added chomp(), replaceTags() and linesToArray() methods.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript;

import java.util.*;
import java.net.URLEncoder;
import java.net.URLDecoder;
import java.util.zip.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.text.NumberFormat;
import java.text.ParsePosition;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.StringEscapeUtils;
import com.judoscript.bio.*;
import com.judoscript.util.*;


public abstract class VariableAdapter implements Variable, MethodOrdinals
{
  public int hashCode() {
    try { return getObjectValue().hashCode(); }
    catch(Throwable e) { return 0; }
  }

  public boolean equals(Object o) {
    try {
      if (o instanceof Variable) {
        Variable rhs = (Variable)o;
        if (getStringValue().equals(rhs.getStringValue())) return true;
        if (getDoubleValue() == rhs.getDoubleValue()) return true;
        return getObjectValue().equals(rhs.getObjectValue());
      } else if (o instanceof Number) {
        return getDoubleValue() == ((Number)o).doubleValue();
      } else if (o instanceof String) {
        return getStringValue().equals(o.toString());
      } else {
        return getObjectValue().equals(o);
      }
    } catch(Throwable e) { return false; }
  }

  public int compareTo(Object o) {
    Object there;
    try {
      Object here = getObjectValue();
      if (o instanceof Variable)
        there = ((Variable)o).getObjectValue();
      else
        there = o;
      if (here instanceof Comparable)
        return ((Comparable)here).compareTo(there);
    } catch(Throwable t) {}
    return 0;
  }

  public Variable eval() throws Throwable { return this; }
  public int getJavaPrimitiveType() { return 0; }
  public void setJavaPrimitiveType(int type) {}

  public Expr reduce(Stack stack) { return this; }
  public Expr optimize() { return this; }
  public void close() {}
  public String toString() {
    try { return getStringValue(); } catch(Throwable e) { return ""; }
  }

  public boolean isNil() { return false; }
  public boolean isUnknownType() { return false; }
  public boolean isInt() {
    switch(getType()) {
    case TYPE_INT:
    case TYPE_DATE:   return true;
    case TYPE_STRING:
      try {
        ((ConstString)this).getLongValue();
        return true;
      } catch(Throwable e) { break; }
    case TYPE_JAVA:
      try {
        Object o = getObjectValue();
        return (o instanceof Integer) || (o instanceof Long) ||
               (o instanceof Byte) || (o instanceof Short) ||
               (o instanceof Boolean) || (o instanceof Character) ||
               (o instanceof java.util.Date);
      } catch(Throwable e) { break; }
    }
    return false;
  }
  public boolean isDouble() {
    int t = getType();
    switch(getType()) {
    case TYPE_DOUBLE:
    case TYPE_INT:
    case TYPE_DATE:   return true;
    case TYPE_STRING:
      if (StringUtils.isBlank(toString())) return false;
      try {
        ((ConstString)this).getDoubleValue();
        return true;
      } catch(Throwable e) { break; }
    case TYPE_JAVA:
      try {
        Object o = getObjectValue();
        return (o instanceof Number) || (o instanceof Boolean) || (o instanceof Character) ||
               (o instanceof java.util.Date);
      } catch(Throwable e) { break; }
    }
    return false;
  }
  public boolean isNumber() { return isDouble(); }
  public boolean isString() {
    switch (getType()) {
    case TYPE_STRING: return true;
    case TYPE_JAVA:
      try { return getObjectValue() instanceof java.lang.String; }
      catch(Throwable e) { return false; }
    }
    return false;
  }
  public boolean isValue()  { return !isObject(); }
  public boolean isInternal()  { return getType() == TYPE_INTERNAL; }
  public boolean isDate()   {
    try { return getObjectValue() instanceof java.util.Date; }
    catch(Throwable e) { return false; }
  }
  public boolean isObject() { return false; }
  public boolean isJava()   { return false; }
  public boolean isCOM()    { return false; }
  public boolean isFunction() { return false; }
  public boolean isArray() {
    if (this instanceof JavaObject) {
      try { return getObjectValue().getClass().isArray(); }
      catch(Throwable e) {}
    }
    return false;
  }
  public boolean isSet()    { return false; }
  public boolean isStack() {
    try { return getObjectValue() instanceof java.util.Stack; }
    catch(Throwable e) { return false; }
  }
  public boolean isQueue() {
    try { return getObjectValue() instanceof com.judoscript.util.Queue; }
    catch(Throwable e) { return false; }
  }
  public boolean isStruct()        { return this instanceof UserDefined; }
  public boolean isComplex()       { return false; }
  public boolean isWebService()    { return false; }
  public boolean isA(String name)  { return ExprAnyBase.isA(this,name); }
  public boolean isReadOnly()      { return false; }

  public static int getMethodOrdinal(String fxn) {
    Integer I = (Integer)bimMap.get(fxn);
    return (I==null) ? 0 : I.intValue();
  }

  public Variable invoke(String fxn, Expr[] params, int[] javaTypes) throws Throwable {
    return invoke(getMethodOrdinal(fxn), fxn, params);
  }

  public Variable invoke(int fxn, String name, Expr[] params) throws Throwable {
    String x, y;
    int idx;
    char ch;
    String s = isStringMethod(fxn) ? getStringValue() : null;
    int len = (params == null) ? 0 : params.length;

    switch(fxn) {
    case BIM_ISINT:      return ConstInt.getBool(isInt());
    case BIM_ISDOUBLE:   return ConstInt.getBool(isDouble());
    case BIM_ISNUMBER:   return ConstInt.getBool(isNumber());
    case BIM_ISSTRING:   return ConstInt.getBool(isString());
    case BIM_ISDATE:     return ConstInt.getBool(isDate());
    case BIM_ISJAVA:     return ConstInt.getBool(isJava());
    case BIM_ISARRAY:    return ConstInt.getBool(isArray());
    case BIM_ISSET:      return ConstInt.getBool(isSet());
    case BIM_ISSTRUCT:   return ConstInt.getBool(isStruct());
    case BIM_ISSTACK:    return ConstInt.getBool(isStack());
    case BIM_ISQUEUE:    return ConstInt.getBool(isQueue());
    case BIM_ISFUNCTION: return ConstInt.getBool(isFunction());
    case BIM_ISOBJECT:   return ConstInt.getBool(isObject());
    case BIM_ISCOMPLEX:  return ConstInt.getBool(isComplex());
    case BIM_ISA:        try { return ConstInt.getBool(isA(params[0].getStringValue())); }
                         catch(Exception e) { return ConstInt.FALSE; }
    case BIM_ISNULL:     return ConstInt.getBool(isNil());
    case BIM_TYPENAME:   return JudoUtil.toVariable(getTypeName());
    case BIM_TOSTRING:   return JudoUtil.toVariable(this);
    case BIM_TOARRAY:    if (isArray() || isNil()) return this;
                         _Array ar = new _Array();
                         ar.append(this);
                         return ar;

    case BIM_SYS_INT:    return ConstInt.getInt(getLongValue());
    case BIM_SYS_DOUBLE: return ConstDouble.getDouble(getDoubleValue());
    case BIM_SYS_FLOAT:  return ConstDouble.getDouble(getDoubleValue());
    case BIM_DATE:       return new _Date(getLongValue());
    case BIM_SQRT:       return ConstDouble.getDouble(Math.sqrt(getDoubleValue()));
    case BIM_LOG:        return ConstDouble.getDouble(Math.log(getDoubleValue()));
    case BIM_LOG10:      return ConstDouble.getDouble(Math.log(getDoubleValue())/ln10);
    case BIM_LOG2:       return ConstDouble.getDouble(Math.log(getDoubleValue())/ln2);
    case BIM_EXP:        return ConstDouble.getDouble(Math.exp(getDoubleValue()));
    case BIM_FLOOR:      return ConstDouble.getDouble(Math.floor(getDoubleValue()));
    case BIM_CEIL:       return ConstDouble.getDouble(Math.ceil(getDoubleValue()));
    case BIM_SIN:        return ConstDouble.getDouble(Math.sin(getDoubleValue()));
    case BIM_COS:        return ConstDouble.getDouble(Math.cos(getDoubleValue()));
    case BIM_TAN:        return ConstDouble.getDouble(Math.tan(getDoubleValue()));
    case BIM_ASIN:       return ConstDouble.getDouble(Math.asin(getDoubleValue()));
    case BIM_ACOS:       return ConstDouble.getDouble(Math.acos(getDoubleValue()));
    case BIM_ATAN:       return ConstDouble.getDouble(Math.atan(getDoubleValue()));
    case BIM_SIN_DEG:    return ConstDouble.getDouble(Math.sin(getDoubleValue() / 180.0 * Math.PI));
    case BIM_COS_DEG:    return ConstDouble.getDouble(Math.cos(getDoubleValue() / 180.0 * Math.PI));
    case BIM_TAN_DEG:    return ConstDouble.getDouble(Math.tan(getDoubleValue() / 180.0 * Math.PI));
    case BIM_ASIN_DEG:   return ConstDouble.getDouble(Math.asin(getDoubleValue()) / Math.PI * 180.0);
    case BIM_ACOS_DEG:   return ConstDouble.getDouble(Math.acos(getDoubleValue()) / Math.PI * 180.0);
    case BIM_ATAN_DEG:   return ConstDouble.getDouble(Math.atan(getDoubleValue()) / Math.PI * 180.0);
    case BIM_DEGREE:     return ConstDouble.getDouble(Math.toDegrees(getDoubleValue()));
    case BIM_RADIAN:     return ConstDouble.getDouble(Math.toRadians(getDoubleValue()));

    case BIM_ABS:
      if (isInt()) return new ConstInt(Math.abs(getLongValue()));
      return ConstDouble.getDouble(Math.abs(getDoubleValue()));
    case BIM_POW:
      if (len == 0) return ConstInt.ONE;
      return ConstDouble.getDouble(Math.pow(getDoubleValue(), params[0].getDoubleValue()));

    case BIM_TRIM:
      x = (len<1) ? null : params[0].getStringValue();
      return JudoUtil.toVariable(Lib.trim(s, x));
    case BIM_CHR:        return JudoUtil.toVariable("" + (char)getLongValue());
    case BIM_SIZE:       return ConstInt.getInt(s.length());
    case BIM_ENCODEURL:  return JudoUtil.toVariable(URLEncoder.encode(s));
    case BIM_DECODEURL:  return JudoUtil.toVariable(URLDecoder.decode(s));
    case BIM_PARSEURL:
      x = (len<1) ? null : params[0].getStringValue();
      return new UserDefined(Lib.parseUrl(s, x));
    case BIM_TOABSOLUTEURL:
      x = (len<1) ? null : params[0].getStringValue();
      return JudoUtil.toVariable(Lib.toAbsoluteUrl(s, x));
    case BIM_ISEMPTY:    return ConstInt.getBool(StringUtils.isBlank(s));
    case BIM_ISNOTEMPTY: return ConstInt.getBool(StringUtils.isNotBlank(s));
    case BIM_GETREADER:  return JudoUtil.toVariable(new BufferedReader(new StringReader(s)));
    case BIM_TOLOWER:    return JudoUtil.toVariable(s.toLowerCase());
    case BIM_TOUPPER:    return JudoUtil.toVariable(s.toUpperCase());
    case BIM_STARTSWITH:
    case BIM_ENDSWITH:
      if (len>0) {
        for (idx=0; idx<len; ++idx) {
          x = params[idx].getStringValue();
          if (fxn == BIM_STARTSWITH) {
            if (s.startsWith(x))
              return ConstInt.TRUE;
          } else {
            if (s.endsWith(x))
              return ConstInt.TRUE;
          }
        }
      }
      return ConstInt.FALSE;

    case BIM_EQUALSIGNORECASE:
      return (len<1) ? ConstInt.FALSE
                     : ConstInt.getBool(s.equalsIgnoreCase(params[0].getStringValue()));

    case BIM_UNICODE:
      return (s.length()>0) ? ConstInt.getInt((long)s.charAt(0)) : ConstInt.ZERO;

    case BIM_ASCII:
      return (s.length()>0) ? ConstInt.getInt(0x0FFL & (long)s.charAt(0)) : ConstInt.ZERO;

    case BIM_CHARAT:
      idx = (len == 0) ? 0 : (int)params[0].getLongValue();
      return (idx>=s.length())
             ? (Variable)ValueSpecial.UNDEFINED
             : JudoUtil.toVariable(String.valueOf(s.charAt(idx)));

    case BIM_SUBSTR:
      if (len <= 0) return ValueSpecial.UNDEFINED;
      idx = (int)params[0].getLongValue();
      try {
        if (len == 1) {
          s = s.substring(idx);
        } else {
          int idx1 = (int)params[1].getLongValue();
          if (s.length() < idx1)
            idx1 = s.length();
          s = s.substring(idx, idx1);
        }
        return JudoUtil.toVariable(s);
      } catch(Exception e) { return ValueSpecial.UNDEFINED; }

    case BIM_TRUNCATE:
      if (len <= 0) return ValueSpecial.UNDEFINED;
      idx = (int)params[0].getLongValue();
      try {
        return JudoUtil.toVariable(s.substring(0, s.length()-idx));
      } catch(Exception e) {
        return ValueSpecial.NIL;
      }

    case BIM_CSV:
      ar = new _Array();
      char sep = ',';
      if (len > 0) {
        x = params[0].getStringValue();
        try { sep = x.charAt(0); } catch(Exception e) {}
      }
      String[] sa = Lib.string2Array(s, sep,
                                     (len > 1) ? params[1].getBoolValue() : false,
                                     (len > 2) ? (int)params[2].getLongValue() : 0,
                                     (len > 3) ? params[3].getStringValue() : null);
      for (int j=0; j<sa.length; j++)
        ar.append(JudoUtil.toVariable(sa[j]));
      return ar;

    case BIM_INDEXOF:
      if (len < 1) return ConstInt.MINUSONE;
      if (len < 2) return ConstInt.getInt( s.indexOf( params[0].getStringValue() ) );
      return ConstInt.getInt( s.indexOf( params[0].getStringValue(), (int)params[1].getLongValue() ) );

    case BIM_LASTINDEXOF:
      if (len < 1) return ConstInt.MINUSONE;
      if (len < 2) return ConstInt.getInt( s.lastIndexOf( params[0].getStringValue() ) );
      return ConstInt.getInt( s.lastIndexOf( params[0].getStringValue(), (int)params[1].getLongValue() ) );

    case BIM_REGIONMATCHES:
    case BIM_REGIONMATCHESIGNORECASE:
      if (len < 3) return ConstInt.FALSE;
      boolean ignoreCase = false;
      int toffset = -1;
      String other = null;
      int ooffset = -1;
      int cmplen = -1;
      toffset = (int)params[0].getLongValue();
      other = params[1].getStringValue();
      ooffset = (int)params[2].getLongValue();
      cmplen = (len >= 4) ? ((int)params[3].getLongValue()) : (other.length() - ooffset);
      if (toffset > 0) {
        return ConstInt.getBool(s.regionMatches((fxn==BIM_REGIONMATCHESIGNORECASE),
                                                toffset,other,ooffset,cmplen));
      }
      return ConstInt.FALSE;

    case BIM_CONTAINS:
      for (idx=0; idx<len; ++idx) {
        Variable v = params[idx].eval();
        try {
          if (v instanceof JavaObject) {
            if ( v.getObjectValue().getClass().getName().indexOf("Regex") > 0 )
              return RT.getRegexCompiler().matches(s, new Expr[]{v});
          }
        } catch(Exception e) {}
        if (s.indexOf(v.getStringValue()) >= 0)
          return ConstInt.TRUE;
      }
      return ConstInt.FALSE;

    case BIM_GETBYTES:
      return JudoUtil.toVariable( (len>=1) ? s.getBytes(params[0].getStringValue()) : s.getBytes());

    case BIM_GETCHARS:
      return JudoUtil.toVariable(s.toCharArray());

    case BIM_BASE64DECODE:
      return JudoUtil.toVariable(Lib.base64Decode(s));

    case BIM_UNQUOTE:
      x = (len==0) ? null : params[0].getStringValue();
      y = (len<1)  ? null : params[1].getStringValue();
      return JudoUtil.toVariable(Lib.unquote(s, x, y));

    case BIM_LEFT:
    case BIM_RIGHT:
      if (len < 1)
        return cloneValue();
      idx = (int)params[0].getLongValue();
      if (idx <= 0)
        return cloneValue();
      cmplen = s.length();
      if (idx >= cmplen)
        return cloneValue();
      return JudoUtil.toVariable((fxn==BIM_LEFT) ? s.substring(0,idx) : s.substring(cmplen-idx));

    case BIM_LEFTOF:
    case BIM_RIGHTOF:
      if (len < 1)
        return cloneValue();
      x = params[0].getStringValue();
      idx = s.indexOf(x);
      if (idx < 0) {
        return cloneValue();
      } else {
        x = (fxn == BIM_LEFTOF) ? s.substring(0, idx) : s.substring(idx+x.length());
        return JudoUtil.toVariable(x);
      }

    case BIM_LEFTOFFIRSTWHITE:
    case BIM_RIGHTOFFIRSTWHITE:
      for (idx=0; idx<s.length(); ++idx) {
        if (Character.isWhitespace(s.charAt(idx)))
          break;
      }
      if (fxn == BIM_LEFTOFFIRSTWHITE)
        return idx >= s.length() ? (Variable)cloneValue() : JudoUtil.toVariable(s.substring(0,idx));
      else
        return idx >= s.length() ? (Variable)ValueSpecial.NIL : JudoUtil.toVariable(s.substring(idx+1));

    case BIM_REPLACE:
    case BIM_REPLACEIGNORECASE:
      if (len < 1) return cloneValue();
      String _old = params[0].getStringValue();
      String _new = (len > 1) ? params[1].getStringValue() : null;
      return JudoUtil.toVariable(Lib.replace((fxn==BIM_REPLACE), s, _old, _new));

    case BIM_REPLACETAGS:
      if (len < 2) return this;
      String left = params[1].getStringValue();
      String right = (len > 2) ? params[2].getStringValue() : left;
      Variable var = params[0].eval();
      if (!(var instanceof ObjectInstance))
        return this;
      List templ = Lib.stringTemplate(s, left, right);
      if (templ == null)
        return this;
      StringBuffer sb = new StringBuffer();
      for (idx=0; idx<templ.size(); ++idx) {
        String sub = (String)templ.get(idx);
        if ((idx & 1) == 0)
          sb.append(sub);
        else
          sb.append(var.resolveVariable(sub).getStringValue());
      }
      return JudoUtil.toVariable(sb.toString());

    case BIM_GETFILEEXT: return JudoUtil.toVariable(Lib.getFileExt(s));

    case BIM_GETFILENAME:
    case BIM_GETFILEPATH:
      idx = s.lastIndexOf("/");
      if (idx < 0)
        idx = s.lastIndexOf("\\");
      if (fxn==BIM_GETFILENAME) {
        if (idx < 0) return this;
        return JudoUtil.toVariable((idx<s.length()-1) ? s.substring(idx+1) : null);
      } else {
        if (idx < 0) return ValueSpecial.UNDEFINED;
        return (idx==s.length()-1) ? this : JudoUtil.toVariable(s.substring(0, idx+1));
      }

    case BIM_TOABSOLUTEPATH:
      return JudoUtil.toVariable(Lib.cleanupPath(new File(s).getAbsolutePath()));

    case BIM_STRINGCOMPARE:
      return (len < 1) ? ConstInt.ONE : ConstInt.getInt(s.compareTo(params[0].getStringValue()));

    case BIM_COUNT:
      return (len < 1) ? ConstInt.ZERO : ConstInt.getInt(Lib.count(s, params[0].getStringValue()));

    case BIM_GROUPNUMBER:
      if (len == 0)
        return JudoUtil.toVariable( Lib.groupNumber(getLongValue()) );
      ch = ',';
      idx = 3;
      if (len > 0) idx = (int)params[0].getLongValue();
      if (len > 1) {
        x = params[1].getStringValue();
        if (x.length() > 0) ch = x.charAt(0);
      }
      return JudoUtil.toVariable( Lib.groupNumber(getLongValue(),idx,ch) );

    case BIM_FRACTIONDIGITS:  // deprecated.
    case BIM_ROUND:
      double d = getDoubleValue();
      idx = (len<=0) ? 0 : (int)params[0].getLongValue();
      if (idx <= 0)
        return ConstInt.getInt(Math.round(d));
      return ConstDouble.getDouble(Lib.decimalDigits(d,idx));

    case BIM_ISODD:
    case BIM_ISEVEN:
      if (!isInt()) return ConstInt.FALSE;
      idx = (int)getLongValue();
      return ConstInt.getBool( (fxn==BIM_ISEVEN) ^ ((idx & 0x01)==1) );

    case BIM_ISALPHA:
    case BIM_ISALNUM:
    case BIM_ISDIGIT:
    case BIM_ISUPPER:
    case BIM_ISLOWER:
    case BIM_ISWHITE:
      x = getStringValue();
      if (x.length() == 0) return ConstInt.FALSE;
      char c = x.charAt(0);
      boolean b = false;
      switch(fxn) {
      case BIM_ISALPHA: b = Character.isLetter(c); break;
      case BIM_ISALNUM: b = Character.isLetterOrDigit(c); break;
      case BIM_ISDIGIT: b = Character.isDigit(c); break;
      case BIM_ISUPPER: b = Character.isUpperCase(c); break;
      case BIM_ISLOWER: b = Character.isLowerCase(c); break;
      case BIM_ISWHITE: b = Character.isWhitespace(c); break;
      }
      return ConstInt.getBool(b);

    case BIM_NUMOFDIGITS:
    case BIM_NUMOFOCTALDIGITS:
    case BIM_NUMOFHEXDIGITS:
      long l = getLongValue();
      int base;
      switch(fxn) {
      case BIM_NUMOFOCTALDIGITS: base = 8; break;
      case BIM_NUMOFHEXDIGITS:   base = 16; break;
      default:                   base = 10; break;
      }
      for (idx=(l<0)?1:0; l!=0; ++idx) l /= base;
      return ConstInt.getInt(idx);

    case BIM_PARSEINT:
        return ConstInt.getInt(Lib.parseLong(getStringValue()));

    case BIM_PARSEINTROMAN:
        return ConstInt.getInt(Roman.toLong(getStringValue()));

    case BIM_PARSEDATE:
      SimpleDateFormat fmt = (len>0) ? new SimpleDateFormat(params[0].getStringValue())
                                     : RT.getDefaultDateFormat();
      return new _Date(parseDate(fmt));

    case BIM_FORMATBOOL:
      if (len < 2)
        return getBoolValue() ? ConstString.TRUE : ConstString.FALSE;
      return getBoolValue() ? params[1].eval() : params[0].eval();

    case BIM_FORMATHEX:
      return formatHex((len>0) ? params[0].getStringValue() : null);

    case BIM_FORMATOCTAL:
      return JudoUtil.toVariable(Long.toOctalString(getLongValue()));

    case BIM_FORMATROMAN:
      s = Roman.toRoman(getLongValue());
      if ((params.length <= 0) || !params[0].getBoolValue())
        s = s.toLowerCase();
      return JudoUtil.toVariable(s);

    case BIM_FORMATCURRENCY:
      NumberFormat nf = NumberFormat.getCurrencyInstance(JudoUtil.getLocale(params));
      return JudoUtil.toVariable(nf.format(getDoubleValue()));

    case BIM_FORMATDURATION:
      return JudoUtil.toVariable( Lib.formatDuration((int)(getLongValue() / 1000)) );

    case BIM_UNIT:
      if (len==0) return this;
      return JudoUtil.toVariable(Lib.unit(getLongValue(),
               params[0].getStringValue(),
               (len==1) ? null : params[1].getStringValue()
             ));
    case BIM_CAPITALIZEFIRSTLETTER:
      return JudoUtil.toVariable(Lib.capitalizeFirstLetter(s));

    case BIM_CAPITALIZEALLFIRSTLETTERS:
      return JudoUtil.toVariable(Lib.capitalizeAllFirstLetters(s));

    case BIM_NEVEREMPTY:
      if (StringUtils.isNotBlank(s))
      	return this;
      return (len < 1) ? ConstString.SPACE : params[0].eval();

    case BIM_FILEEXISTS:
    case BIM_FILEISWRITABLE:
    case BIM_FILEISREADABLE:
    case BIM_FILELENGTH:
    case BIM_FILETIME:
    case BIM_ISFILE:
    case BIM_FILEISHIDDEN:
    case BIM_FILEISDIRECTORY:
      x = getStringValue();
      if (StringUtils.isBlank(x))
        return ValueSpecial.NIL;
      File f = Lib.isAbsolutePath(x) ? new File(x) : new File(RT.getCurrentDir().toString(), x);
      if (!f.exists()) return ConstInt.ZERO;
      switch(fxn) {
      case BIM_FILEEXISTS:      return ConstInt.TRUE;
      case BIM_FILEISWRITABLE:  return ConstInt.getBool(f.canWrite());
      case BIM_FILEISREADABLE:  return ConstInt.getBool(f.canRead());
      case BIM_FILELENGTH:      return ConstInt.getInt(f.length());
      case BIM_FILETIME:        return new _Date(f.lastModified());
      case BIM_ISFILE:          return ConstInt.TRUE;
      case BIM_FILEISHIDDEN:    return ConstInt.getBool(f.isHidden());
      case BIM_FILEISDIRECTORY: return ConstInt.getBool(f.isDirectory());
      case BIM_FILEISREGULAR:   return ConstInt.getBool(!f.isDirectory());
      }
      return null; // should not happen.

    case BIM_ENSUREENDSWITHFILESEP:
      if (s.endsWith("/") || s.endsWith("\\"))
        return this;
      return JudoUtil.toVariable(s + File.separator);

    case BIM_TOOSPATH:      return JudoUtil.toVariable(Lib.toOSPath(s));

    case BIM_TOBOOLEAN:     return new JavaObject(getBoolValue()?Boolean.TRUE:Boolean.FALSE);
    case BIM_TOBYTE:        return new JavaObject(new Byte((byte)getLongValue()));
    case BIM_TOSHORT:       return new JavaObject(new Short((short)getLongValue()));
    case BIM_TOINTEGER:     return new JavaObject(new Integer((int)getLongValue()));
    case BIM_TOLONG:        return new JavaObject(new Long(getLongValue()));
    case BIM_TOFLOAT:       return new JavaObject(new Float((float)getDoubleValue()));
    case BIM_TODOUBLE:      return new JavaObject(new Double(getDoubleValue()));
    case BIM_TOCHARACTER:   return new JavaObject(new Character(
                                   (this instanceof ConstInt || this instanceof ConstDouble)
                                   ? ((char)getLongValue()) : (s.length() == 0) ? ' ' : s.charAt(0) ) );

    case BIM_PARSEFIXEDPOSITION:
      if (len < 1) return this;
      int[] ia = new int[len];
      for (int i=0; i<len; ++i) ia[i] = (int)params[i].getLongValue();
      return JudoUtil.toVariable( Lib.parseFixedPosition(s,ia));

    case BIM_WRITETOFILE:
      if (len < 1) break;
      x = params[0].getStringValue();
      if (StringUtils.isNotBlank(x)) {
        y = (len > 1) ? params[1].getStringValue() : null;
        Writer w = StringUtils.isBlank(y) ? (Writer)new FileWriter(x)
                                  : new OutputStreamWriter(new FileOutputStream(x), y);
        PrintWriter pw = new PrintWriter(w);
        pw.print(s);
        pw.close();
      }
      break;

    case BIM_WRITETOZIP:
      if (len < 2) break;
      x = params[0].getStringValue();
      if (StringUtils.isNotBlank(x)) {
        ZipWriter zw = null;
        try {
          zw = (ZipWriter)params[1].getObjectValue();
        } catch(ClassCastException cce) {
          ExceptionRuntime.rte(RTERR_ILLEGAL_ARGUMENTS, "writeToZip() method must take a zip archive.");
        }
        ZipEntry ze = zw.createZipEntry(x);
        ZipOutputStream zos = zw.getZipOutputStream();
        zos.putNextEntry(ze);
        y = (len > 2) ? params[2].getStringValue() : null;
        Writer w = StringUtils.isBlank(y) ? new OutputStreamWriter(zos) : new OutputStreamWriter(zos, y);
        PrintWriter pw = new PrintWriter(w);
        pw.print(s);
        pw.flush();
      }
      break;

    case BIM_ISASCIIONLY:
      for (idx=s.length()-1; idx>=0; --idx) {
        if ((0xFFFFFF00 & (int)s.charAt(idx)) != 0)
          return ConstInt.FALSE;
      }
      return ConstInt.TRUE;

    case BIM_ESCAPEJAVA:
      if ((len<1) || !params[0].getBoolValue()) {
        s = StringEscapeUtils.escapeJava(s);
      } else { // don't escape non-whites:
        sb = new StringBuffer();
        for (idx=0; idx<s.length(); ++idx) {
          ch = s.charAt(idx);
          if ( 0 == (((int)ch) & 0xFF00)) {
            sb.append(ch);
          } else {
            sb.append("\\u");
            sb.append(Integer.toHexString(ch).toUpperCase());
          }
        }
        s = sb.toString();
      }
      return JudoUtil.toVariable(s);

    case BIM_ESCAPEHTML:         return JudoUtil.toVariable(StringEscapeUtils.escapeHtml(s));
    case BIM_ESCAPEJAVASCRIPT:   return JudoUtil.toVariable(StringEscapeUtils.escapeJavaScript(s));
    case BIM_ESCAPESQL:          return JudoUtil.toVariable(StringEscapeUtils.escapeSql(s));
    case BIM_ESCAPEXML:          return JudoUtil.toVariable(StringEscapeUtils.escapeXml(s));
    case BIM_UNESCAPEHTML:       return JudoUtil.toVariable(StringEscapeUtils.unescapeHtml(s));
    case BIM_UNESCAPEJAVA:       return JudoUtil.toVariable(StringEscapeUtils.unescapeJava(s));
    case BIM_UNESCAPEJAVASCRIPT: return JudoUtil.toVariable(StringEscapeUtils.unescapeJavaScript(s));
    case BIM_UNESCAPEXML:        return JudoUtil.toVariable(StringEscapeUtils.unescapeXml(s));

    case BIM_MATCHER:
      if (len < 1) break;
      return RT.getRegexCompiler().matcher(s, params);
    case BIM_MATCHES:
      if (len < 1) break;
      return RT.getRegexCompiler().matches(s, params);
    case BIM_MATCHESSTART:
      if (len < 1) break;
      return RT.getRegexCompiler().matchesStart(s, params);
    case BIM_REPLACEALL:
      if (len < 1) break;
      return RT.getRegexCompiler().replaceAll(s, params);
    case BIM_REPLACEFIRST:
      if (len < 1) break;
      return RT.getRegexCompiler().replaceFirst(s, params);
    case BIM_SPLIT:
      if (len < 1) break;
      return RT.getRegexCompiler().split(s, params);
    case BIM_SPLITWITHMATCHES:
    case BIM_SPLITWITHMATCHESONLY:
      if (len < 1) break;
      return RT.getRegexCompiler().splitWithMatches(s, params, fxn==BIM_SPLITWITHMATCHES);

    case BIM_CHOMP:
      idx = s.length();
      while(--idx >= 0) {
        ch = s.charAt(idx);
        if (ch != '\n' && ch != '\r')
          break;
      }
      if (idx < s.length()-1)
        return JudoUtil.toVariable(s.substring(0, idx+1));

    case BIM_LINESTOARRAY: return JudoUtil.toVariable(Lib.linesToArray(s));

    default:
      if (this instanceof JavaObject) {
        throw new NoSuchMethodException("Method " + name + "() not found in class " +
                                        ((JavaObject)this).getClassName() + ".");
      } else {
        ExceptionRuntime.methodNotFound(getTypeName(),name);
      }
    }
    return ValueSpecial.UNDEFINED;
  }

  public Variable resolveVariable(String name) throws Throwable {
    if (name.equals("length"))
       try { return ConstInt.getInt(getStringValue().length()); } catch(Exception e) {}
    return ValueSpecial.UNDEFINED;
  }

  public Variable resolveVariable(Variable name) throws Throwable {
    return resolveVariable(name.getStringValue());
  }

  /**
   * Array objects should override this method!
   */
  public Object[] getObjectArrayValue() throws Throwable {
    return new Object[] { getObjectValue() };
  }

  public final Date parseDate(SimpleDateFormat fmt) throws Throwable {
    ParsePosition pos = new ParsePosition(0);
    String x = getStringValue();
    try {
      Date ret = fmt.parse(x, pos);
      if (ret != null)
      return ret;
    } catch(Exception e) {
      try { // one more try -- HTTP date.
        return HttpDate.parseHttpDate(x);
      } catch(Exception hde) {}
    }
    throw new Exception("Cannot parse '"+x+"' to date using '"+fmt.toPattern()+"'.");
  }

  public final Variable formatHex(String fmt) throws Throwable {
    String s = Long.toHexString(getLongValue());
    boolean upper = true;
    int width = 0;
    if (StringUtils.isNotBlank(fmt)) {
      char c = fmt.charAt(fmt.length()-1);
      if (!Character.isDigit(c)) {
        fmt = fmt.substring(0,fmt.length()-1);
        upper = Character.isUpperCase(c);
      }
      try { width = Integer.parseInt(fmt); } catch(Exception e) {}
    }
    if (upper) s = s.toUpperCase();
    if (width > 0) s = Lib.rightAlign(s, width, false, '0', "");
    return JudoUtil.toVariable(s);
  }

  public static final HashMap bimMap = new HashMap();

  public static int getShortcutOrdinal(String fxn) {
    int ord = getMethodOrdinal(fxn);
    return ((ord & BIM_ALL__MASK) != 0) ? ord : 0;
  }

  public static boolean isShortcut(String fxn) {
    return (BIM_ALL__MASK & getMethodOrdinal(fxn)) != 0;
  }

  private static ArrayList v = new ArrayList();

  static void addMap(String name, Integer ord) throws Exception {
//    if (bimMap.get(name) != null)
//      throw new Exception("Function " + name + "() already exists for some other object.");
    if (bimMap.get(name) != null) {
      name = name + " " + bimMap.get(name) + " " + ord;
      v.add(name);
      return;
    }
    bimMap.put(name, ord);
  }

  static void addMap(String name, int ord) throws Exception {
//    if (bimMap.get(name) != null)
//      throw new Exception("Function " + name + "() already exists for some other object.");
//    bimMap.put(name, new Integer(ord));
    addMap(name,new Integer(ord));
  }

  static {
    Integer I;
    try {

/////////////////////////////////////////////////////////////////////
// Common
//
      I = new Integer(BIM_ISINT);
      addMap("isInt",     I);
      addMap("isInteger", I);
      I = new Integer(BIM_ISDOUBLE);
      addMap("isFloat",   I);
      addMap("isDouble",  I);
      addMap("isNumber",  new Integer(BIM_ISNUMBER));
      addMap("isString",  new Integer(BIM_ISSTRING));
      addMap("isDate",    new Integer(BIM_ISDATE));
      addMap("isJava",    new Integer(BIM_ISJAVA));
      addMap("isArray",   new Integer(BIM_ISARRAY));
      addMap("isSet",     new Integer(BIM_ISSET));
      addMap("isStruct",  new Integer(BIM_ISSTRUCT));
      addMap("isStack",   new Integer(BIM_ISSTACK));
      addMap("isQueue",   new Integer(BIM_ISQUEUE));
      addMap("isFunction",new Integer(BIM_ISFUNCTION));
      addMap("isObject",  new Integer(BIM_ISOBJECT));
      addMap("isComplex", new Integer(BIM_ISCOMPLEX));
      addMap("isA",       new Integer(BIM_ISA));
      addMap("isNull",    new Integer(BIM_ISNULL));
      I = new Integer(BIM_TYPENAME);
      addMap("typeName",    I);
      addMap("getTypeName", I);
      addMap("getClassName",I);
      addMap("className",   I);
      addMap("toString",    new Integer(BIM_TOSTRING));

/////////////////////////////////////////////////////////////////////
// ValueBase
//
      addMap("toBoolean",          new Integer(BIM_TOBOOLEAN));
      addMap("toByte",             new Integer(BIM_TOBYTE));
      addMap("toCharacter",        new Integer(BIM_TOCHARACTER));
      addMap("toShort",            new Integer(BIM_TOSHORT));
      addMap("toInteger",          new Integer(BIM_TOINTEGER));
      addMap("toLong",             new Integer(BIM_TOLONG));
      addMap("toFloat",            new Integer(BIM_TOFLOAT));
      addMap("toDouble",           new Integer(BIM_TODOUBLE));
      addMap("parseFixedPosition", new Integer(BIM_PARSEFIXEDPOSITION));
      addMap("writeToFile",        new Integer(BIM_WRITETOFILE));
      addMap("writeToZip",         new Integer(BIM_WRITETOZIP));
      addMap("isAsciiOnly",        new Integer(BIM_ISASCIIONLY));
      addMap("escapeHtml",         new Integer(BIM_ESCAPEHTML));
      addMap("escapeJava",         new Integer(BIM_ESCAPEJAVA));
      I = new Integer(BIM_ESCAPEJAVASCRIPT);
      addMap("escapeJavaScript",   I);
      addMap("escapeJavascript",   I);
      addMap("escapeSql",          new Integer(BIM_ESCAPESQL));
      addMap("escapeXml",          new Integer(BIM_ESCAPEXML));
      addMap("unescapeHtml",       new Integer(BIM_UNESCAPEHTML));
      addMap("unescapeJava",       new Integer(BIM_UNESCAPEJAVA));
      I = new Integer(BIM_UNESCAPEJAVASCRIPT);
      addMap("unescapeJavaScript", I);
      addMap("unescapeJavascript", I);
      addMap("unescapeXml",        new Integer(BIM_UNESCAPEXML));

      I = new Integer(BIM_FILEISDIRECTORY);
      addMap("fileIsDirectory", I);
      addMap("fileIsDir",       I);
      addMap("isDirectory",     I);
      addMap("isDir",           I);
      I = new Integer(BIM_FILEISREADABLE);
      addMap("fileIsReadable",  I);
      addMap("fileReadable",    I);
      addMap("fileCanRead",     I);
      addMap("fileIsRegular",   new Integer(BIM_FILEISREGULAR));
      I = new Integer(BIM_FILEEXISTS);
      addMap("fileExists",      I);
      addMap("existsFile",      I);
      I = new Integer(BIM_FILEISWRITABLE);
      addMap("fileIsWritable",  I);
      addMap("fileWritable",    I);
      addMap("fileCanWrite",    I);
      I = new Integer(BIM_FILELENGTH);
      addMap("fileLength",      I);
      addMap("fileSize",        I);
      addMap("fileTime",        new Integer(BIM_FILETIME));
      addMap("isFile",          new Integer(BIM_ISFILE));
      I = new Integer(BIM_FILEISHIDDEN);
      addMap("fileIsHidden",    I);
      addMap("isFileHidden",    I);
      addMap("toOSPath",        new Integer(BIM_TOOSPATH));

      addMap("abs",    new Integer(BIM_ABS));
      addMap("sqrt",   new Integer(BIM_SQRT));
      addMap("log",    new Integer(BIM_LOG));
      addMap("log10",  new Integer(BIM_LOG10));
      addMap("log2",   new Integer(BIM_LOG2));
      addMap("exp",    new Integer(BIM_EXP));
      addMap("floor",  new Integer(BIM_FLOOR));
      addMap("ceil",   new Integer(BIM_CEIL));
      addMap("round",  new Integer(BIM_ROUND));
      addMap("pow",    new Integer(BIM_POW));
      addMap("sin",    new Integer(BIM_SIN));
      addMap("cos",    new Integer(BIM_COS));
      addMap("tan",    new Integer(BIM_TAN));
      addMap("asin",   new Integer(BIM_ASIN));
      addMap("acos",   new Integer(BIM_ACOS));
      addMap("atan",   new Integer(BIM_ATAN));
      addMap("sin_d",  new Integer(BIM_SIN_DEG));
      addMap("cos_d",  new Integer(BIM_COS_DEG));
      addMap("tan_d",  new Integer(BIM_TAN_DEG));
      addMap("asin_d", new Integer(BIM_ASIN_DEG));
      addMap("acos_d", new Integer(BIM_ACOS_DEG));
      addMap("atan_d", new Integer(BIM_ATAN_DEG));
      addMap("degree", new Integer(BIM_DEGREE));
      addMap("radian", new Integer(BIM_RADIAN));
      addMap("groupNumber",   new Integer(BIM_GROUPNUMBER));
      addMap("fractionDigits",new Integer(BIM_FRACTIONDIGITS));
      addMap("isOdd",         new Integer(BIM_ISODD));
      addMap("isEven",        new Integer(BIM_ISEVEN));
      I = new Integer(BIM_ISALPHA);
      addMap("isAlpha", I);
      addMap("isDigit",new Integer(BIM_ISDIGIT));
      addMap("isLetter", I);
      I = new Integer(BIM_ISALNUM);
      addMap("isAlnum", I);
      addMap("isLetterOrDigit", I);
      I = new Integer(BIM_ISUPPER);
      addMap("isUpper", I);
      addMap("isUpperCase", I);
      I = new Integer(BIM_ISLOWER);
      addMap("isLower", I);
      addMap("isLowerCase", I);
      I = new Integer(BIM_ISWHITE);
      addMap("isWhite", I);
      addMap("isWhitespace",      I);
      addMap("numOfDigits",       new Integer(BIM_NUMOFDIGITS));
      addMap("numOfHexDigits",    new Integer(BIM_NUMOFHEXDIGITS));
      addMap("numOfOctalDigits",  new Integer(BIM_NUMOFOCTALDIGITS));

      addMap("chr",               BIM_CHR);
      addMap("ascii",             new Integer(BIM_ASCII));
      addMap("unicode",           new Integer(BIM_UNICODE));
      addMap("charAt",            new Integer(BIM_CHARAT));
      I = new Integer(BIM_SUBSTR);
      addMap("substr",            I);
      addMap("substring",         I);
      I = new Integer(BIM_TRUNCATE);
      addMap("trunc",             I);
      addMap("truncate",          I);
      addMap("csv",               new Integer(BIM_CSV));
      addMap("trim",              new Integer(BIM_TRIM));
      addMap("indexOf",           new Integer(BIM_INDEXOF));
      addMap("lastIndexOf",       new Integer(BIM_LASTINDEXOF));
      I = new Integer(BIM_TOLOWER);
      addMap("toLower",     I);
      addMap("toLowerCase", I);
      I = new Integer(BIM_TOUPPER);
      addMap("toUpper",     I);
      addMap("toUpperCase", I);
      addMap("startsWith",        new Integer(BIM_STARTSWITH));
      addMap("endsWith",          new Integer(BIM_ENDSWITH));
      addMap("regionMatches",     new Integer(BIM_REGIONMATCHES));
      addMap("regionMatchesIgnoreCase", new Integer(BIM_REGIONMATCHESIGNORECASE));
      addMap("replace",           new Integer(BIM_REPLACE));
      addMap("replaceIgnoreCase", new Integer(BIM_REPLACEIGNORECASE));
      addMap("replaceTags",       new Integer(BIM_REPLACETAGS));
      I = new Integer(BIM_ENCODEURL);
      addMap("encodeUrl", I);
      addMap("urlEncode", I);
      I = new Integer(BIM_DECODEURL);
      addMap("decodeUrl", I);
      addMap("urlDecode", I);
      addMap("parseUrl",          new Integer(BIM_PARSEURL));
      addMap("getFileName",       new Integer(BIM_GETFILENAME));
      addMap("getFilePath",       new Integer(BIM_GETFILEPATH));
      I = new Integer(BIM_GETFILEEXT);
      addMap("getFileExt",        I);
      addMap("getFileExtension",  I);
      addMap("isEmpty",           new Integer(BIM_ISEMPTY));
      addMap("isNotEmpty",        new Integer(BIM_ISNOTEMPTY));
      addMap("neverEmpty",        new Integer(BIM_NEVEREMPTY));
      addMap("stringCompare",     new Integer(BIM_STRINGCOMPARE));
      addMap("count",             new Integer(BIM_COUNT));
      addMap("getReader",         new Integer(BIM_GETREADER));
      addMap("equalsIgnoreCase",  new Integer(BIM_EQUALSIGNORECASE));
      addMap("contains",          new Integer(BIM_CONTAINS));
      addMap("left",              new Integer(BIM_LEFT));
      addMap("right",             new Integer(BIM_RIGHT));
      addMap("leftOf",            new Integer(BIM_LEFTOF));
      addMap("rightOf",           new Integer(BIM_RIGHTOF));
      addMap("leftOfFirstWhite",  new Integer(BIM_LEFTOFFIRSTWHITE));
      addMap("rightOfFirstWhite", new Integer(BIM_RIGHTOFFIRSTWHITE));
      addMap("getBytes",          new Integer(BIM_GETBYTES));
      addMap("getChars",          new Integer(BIM_GETCHARS));
      addMap("base64Decode",      new Integer(BIM_BASE64DECODE));
      addMap("unquote",           new Integer(BIM_UNQUOTE));
      addMap("toAbsolutePath",    new Integer(BIM_TOABSOLUTEPATH));
      addMap("toAbsoluteUrl",     new Integer(BIM_TOABSOLUTEURL));

      addMap("parseInt",          new Integer(BIM_PARSEINT));
      addMap("parseIntRoman",     new Integer(BIM_PARSEINTROMAN));
      addMap("parseDate",         new Integer(BIM_PARSEDATE));
      I = new Integer(BIM_FORMATBOOL);
      addMap("fmtBool",       I);
      addMap("formatBool",    I);
      I = new Integer(BIM_FORMATHEX);
      addMap("fmtHex",        I);
      addMap("formatHex",     I);
      I = new Integer(BIM_FORMATOCTAL);
      addMap("fmtOctal",      I);
      addMap("formatOctal",   I);
      I = new Integer(BIM_FORMATROMAN);
      addMap("fmtRoman",      I);
      addMap("formatRoman",   I);
      I = new Integer(BIM_FORMATCURRENCY);
      addMap("fmtCurrency",   I);
      addMap("formatCurrency",I);
      I = new Integer(BIM_FORMATDURATION);
      addMap("formatDuration",I);
      addMap("fmtDuration",   I);
      addMap("unit",          new Integer(BIM_UNIT));
      I = new Integer(BIM_CAPITALIZEFIRSTLETTER);
      addMap("capitalizeFirstLetter", I);
      addMap("capFirst", I);
      I = new Integer(BIM_CAPITALIZEALLFIRSTLETTERS);
      addMap("capitalizeAllFirstLetters", I);
      addMap("capAllFirst", I);

      addMap("matcher",           new Integer(BIM_MATCHER));
      addMap("matches",           new Integer(BIM_MATCHES));
      addMap("matchesStart",      new Integer(BIM_MATCHESSTART));
      addMap("replaceAll",        new Integer(BIM_REPLACEALL));
      addMap("replaceFirst",      new Integer(BIM_REPLACEFIRST));
      addMap("split",             new Integer(BIM_SPLIT));
      addMap("splitWithMatches",  new Integer(BIM_SPLITWITHMATCHES));
      addMap("splitWithMatchesOnly",  new Integer(BIM_SPLITWITHMATCHESONLY));
      addMap("ensureEndsWithFileSep", new Integer(BIM_ENSUREENDSWITHFILESEP));
      addMap("chomp",             new Integer(BIM_CHOMP));
      addMap("linesToArray",      new Integer(BIM_LINESTOARRAY));

/////////////////////////////////////////////////////////////////////
// bio.IODevice
//
      I = new Integer(BIM_SIZE);
      addMap("size",              I);
      addMap("length",            I);
      addMap("lastModified",      new Integer(BIM_LASTMODIFIED));
      addMap("lastMod",           new Integer(BIM_LASTMODIFIED));
      addMap("setBigEndian",      new Integer(BIM_SETBIGENDIAN));
      addMap("isBigEndian",       new Integer(BIM_ISBIGENDIAN));
      addMap("setLittleEndian",   new Integer(BIM_SETLITTLEENDIAN));
      addMap("isLittleEndian",    new Integer(BIM_ISLITTLEENDIAN));
      addMap("readBytesAsString", new Integer(BIM_READBYTESASSTRING));
      addMap("toTextInput",       new Integer(BIM_TOTEXTINPUT));
      addMap("toTextOutput",      new Integer(BIM_TOTEXTOUTPUT));

/////////////////////////////////////////////////////////////////////
// bio.UserDefined
//
      addMap("copy",              new Integer(BIM_COPY));
      addMap("clear",             new Integer(BIM_CLEAR));
      addMap("keys",              new Integer(BIM_KEYS));
      addMap("keysSorted",        new Integer(BIM_KEYSSORTED));
      addMap("keysFiltered",      new Integer(BIM_KEYSFILTERED));
      addMap("values",            new Integer(BIM_VALUES));
      addMap("has",               new Integer(BIM_HAS));
      addMap("hasMethod",         new Integer(BIM_HASMETHOD));
      addMap("assertHas",         new Integer(BIM_ASSERTHAS));
      I = new Integer(BIM_REMOVE);
      addMap("remove",I);
      addMap("delete",I);
      addMap("get",               new Integer(BIM_GET));
      I = new Integer(BIM_SET);
      addMap("set",         I);
      addMap("transpose",         new Integer(BIM_TRANSPOSE));
      addMap("keysSortedByValue", new Integer(BIM_KEYSSORTEDBYVALUE));
      addMap("keysFilteredByValue",          new Integer(BIM_KEYSFILTEREDBYVALUE));
      addMap("keysFilteredAndSortedByValue", new Integer(BIM_KEYSFILTEREDANDSORTEDBYVALUE));

/////////////////////////////////////////////////////////////////////
// bio.ZipArchive
//
      addMap("fileCompressedSize", new Integer(BIM_FILECOMPRESSEDSIZE));

/////////////////////////////////////////////////////////////////////
// bio._Array
//
      addMap("lastIndex",            new Integer(BIM_LASTINDEX));
      addMap("insert",               new Integer(BIM_INSERT));
      addMap("prepend",              new Integer(BIM_PREPEND));
      I = new Integer(BIM_APPEND);
      addMap("add",                  I);
      addMap("append",               I);
      addMap("subarray",             new Integer(BIM_SUBARRAY));
      addMap("exists",               new Integer(BIM_EXISTS));
      addMap("sum",                  new Integer(BIM_SUM));
      I = new Integer(BIM_AVG);
      addMap("avg",                  I);
      addMap("averag",               I);
      addMap("range",                new Integer(BIM_RANGE));
      addMap("sort",                 new Integer(BIM_SORT));
      addMap("sortAsString",         new Integer(BIM_SORT_AS_STRING));
      addMap("sortAsNumber",         new Integer(BIM_SORT_AS_NUMBER));
      addMap("sortAsDate",           new Integer(BIM_SORT_AS_DATE));
      addMap("reverse",              new Integer(BIM_REVERSE));
      addMap("filter",               new Integer(BIM_FILTER));
      addMap("convert",              new Integer(BIM_CONVERT));
      I = new Integer(BIM_CONCAT);
      addMap("concat",               I);
      addMap("toCsv",                I);
      addMap("toFixedPositionString",new Integer(BIM_TOFIXEDPOSITIONSTRING));
      addMap("prependArray",         new Integer(BIM_PREPENDARRAY));
      addMap("appendArray",          new Integer(BIM_APPENDARRAY));
      addMap("setSize",              new Integer(BIM_SETSIZE));
      addMap("toArray",              new Integer(BIM_TOARRAY));
      addMap("toStringArray",        new Integer(BIM_TOSTRINGARRAY));
      addMap("toObjectArray",        new Integer(BIM_TOOBJECTARRAY));
      addMap("toBooleanArray",       new Integer(BIM_TOBOOLEANARRAY));
      addMap("toByteArray",          new Integer(BIM_TOBYTEARRAY));
      addMap("toCharArray",          new Integer(BIM_TOCHARARRAY));
      addMap("toShortArray",         new Integer(BIM_TOSHORTARRAY));
      addMap("toIntArray",           new Integer(BIM_TOINTARRAY));
      addMap("toLongArray",          new Integer(BIM_TOLONGARRAY));
      addMap("toFloatArray",         new Integer(BIM_TOFLOATARRAY));
      addMap("toDoubleArray",        new Integer(BIM_TOLONGARRAY));
      addMap("toBooleanObjectArray", new Integer(BIM_TOBOOLEANOBJECTARRAY));
      addMap("toByteObjectArray",    new Integer(BIM_TOBYTEOBJECTARRAY));
      addMap("toCharObjectArray",    new Integer(BIM_TOCHAROBJECTARRAY));
      addMap("toShortObjectArray",   new Integer(BIM_TOSHORTOBJECTARRAY));
      addMap("toIntObjectArray",     new Integer(BIM_TOINTOBJECTARRAY));
      addMap("toLongObjectArray",    new Integer(BIM_TOLONGOBJECTARRAY));
      addMap("toFloatObjectArray",   new Integer(BIM_TOFLOATOBJECTARRAY));
      addMap("toDoubleObjectArray",  new Integer(BIM_TOLONGOBJECTARRAY));
      addMap("subset",               new Integer(BIM_SUBSET));
      addMap("toJavaSet",            new Integer(BIM_TOJAVASET));
      addMap("first",                new Integer(BIM_FIRST));
      addMap("last",                 new Integer(BIM_LAST));
      addMap("saveAsLines",          new Integer(BIM_SAVEASLINES));
      addMap("loadAsLines",          new Integer(BIM_LOADASLINES));
      addMap("getOne",               new Integer(BIM_GETONE));

/////////////////////////////////////////////////////////////////////
// bio._Date
//
      addMap("epoch",            new Integer(BIM_EPOCH));
      addMap("year",             new Integer(BIM_YEAR));
      addMap("month",            new Integer(BIM_MONTH));
      addMap("hour",             new Integer(BIM_HOUR));
      addMap("minute",           new Integer(BIM_MINUTE));
      addMap("second",           new Integer(BIM_SECOND));
      addMap("milliSecond",      new Integer(BIM_MILLISECOND));
      addMap("zoneOffset",       new Integer(BIM_ZONE_OFFSET));
      addMap("dstOffset",        new Integer(BIM_DST_OFFSET));
      addMap("weekOfYear",       new Integer(BIM_WEEK_OF_YEAR));
      addMap("weekOfMonth",      new Integer(BIM_WEEK_OF_MONTH));
      addMap("dayOfMonth",       new Integer(BIM_DAY_OF_MONTH));
      addMap("dayOfYear",        new Integer(BIM_DAY_OF_YEAR));
      addMap("dayOfWeek",        new Integer(BIM_DAY_OF_WEEK));
      addMap("dayOfWeekInMonth", new Integer(BIM_DAY_OF_WEEK_IN_MONTH));
      addMap("isAM",             new Integer(BIM_IS_AM));
      addMap("isPM",             new Integer(BIM_IS_PM));
      addMap("monthName",        new Integer(BIM_MONTH_NAME));
      addMap("monthShortName",   new Integer(BIM_MONTH_SHORT_NAME));
      addMap("weekDayName",      new Integer(BIM_WEEK_NAME));
      addMap("weekDayShortName", new Integer(BIM_WEEK_SHORT_NAME));
      I = new Integer(BIM_FORMATDATE);
      addMap("fmtDate",          I);
      addMap("formatDate",       I);
      addMap("before",           new Integer(BIM_BEFORE));
      addMap("after",            new Integer(BIM_AFTER));
      addMap("getTime",          new Integer(BIM_GETTIME));
      addMap("setTime",          new Integer(BIM_SETTIME));
      addMap("ensureDate",       new Integer(BIM_ENSUREDATE));

/////////////////////////////////////////////////////////////////////
// bio._TableData
//
      addMap("addRow",            new Integer(BIM_ADDROW));
      addMap("setRow",            new Integer(BIM_SETROW));
      addMap("setTitles",         new Integer(BIM_SETTITLES));

/////////////////////////////////////////////////////////////////////
// bio._HTTP
//
      addMap("getUrl",            new Integer(BIM_GETURL));
      addMap("getHost",           new Integer(BIM_GETHOST));
      addMap("getPort",           new Integer(BIM_GETPORT));
      addMap("getDomain",         new Integer(BIM_GETDOMAIN));
      addMap("getPath",           new Integer(BIM_GETPATH));
      addMap("getFile",           new Integer(BIM_GETFILENAME));
      addMap("getQuery",          new Integer(BIM_GETQUERY));
      addMap("getRef",            new Integer(BIM_GETREF));
      addMap("getMethod",         new Integer(BIM_GETMETHOD));
      addMap("getOutputStream",   new Integer(BIM_GETOUTPUTSTREAM));
      addMap("getTextOutput",     new Integer(BIM_GETTEXTOUTPUT));
      addMap("getDateHeader",     new Integer(BIM_GETDATEHEADER));
      addMap("getAllHeaders",     new Integer(BIM_GETALLHEADERS));
      I = new Integer(BIM_STATUSCODE);
      addMap("getStatusCode",     I);
      addMap("getStatus",         I);
      addMap("setStatusCode",     I); // for HttpService.
      I = new Integer(BIM_RESPONSEMSG);
      addMap("getResponseMsg",    I);
      addMap("getResponseMessage",I);
      addMap("setResponseMsg",    I); // for HttpService.
      addMap("setResponseMessage",I); // for HttpService.
      addMap("getInputStream",    new Integer(BIM_GETINPUTSTREAM));
      addMap("getTextInput",      new Integer(BIM_GETTEXTINPUT));
      addMap("connect",           new Integer(BIM_CONNECT));
      addMap("addCookie",         new Integer(BIM_ADDCOOKIE));
      addMap("loadCookies",       new Integer(BIM_LOADCOOKIES));
      addMap("getCookies",        new Integer(BIM_GETCOOKIES));
      addMap("saveCookies",       new Integer(BIM_SAVECOOKIES));
      addMap("getContentLength",  new Integer(BIM_GETCONTENTLENGTH));
      addMap("getContentType",    new Integer(BIM_GETCONTENTTYPE));
      addMap("getContentBytes",   new Integer(BIM_GETCONTENTBYTES));
      addMap("parseFormVars",     new Integer(BIM_PARSEFORMVARS));
      addMap("getServerName",     new Integer(BIM_GETSERVERNAME));
      addMap("getServerPort",     new Integer(BIM_GETSERVERPORT));
      addMap("serveFile",         new Integer(BIM_SERVEFILE));
      addMap("serveError",        new Integer(BIM_SERVEERROR));

/////////////////////////////////////////////////////////////////////
// db.DBMetaData
//
      addMap("getSchemas",          new Integer(BIM_GETSCHEMAS));
      addMap("getCatalogs",         new Integer(BIM_GETCATALOGS));
      addMap("getTableTypes",       new Integer(BIM_GETTABLETYPES));
      addMap("getTypeInfo",         new Integer(BIM_GETTYPEINFO));
      addMap("getProcedures",       new Integer(BIM_GETPROCEDURES));
      addMap("getProcedureColumns", new Integer(BIM_GETPROCEDURECOLUMNS));
      addMap("getTables",           new Integer(BIM_GETTABLES));
      addMap("getColumns",          new Integer(BIM_GETCOLUMNS));
      addMap("getColumnPrivileges", new Integer(BIM_GETCOLUMNPRIVILEGES));
      addMap("getTablePrivileges",  new Integer(BIM_GETTABLEPRIVILEGES));
      addMap("getBestRowIdentifier",new Integer(BIM_GETBESTROWIDENTIFIER));
      addMap("getVersionColumns",   new Integer(BIM_GETVERSIONCOLUMNS));
      addMap("getPrimaryKeys",      new Integer(BIM_GETPRIMARYKEYS));
      addMap("getImportedKeys",     new Integer(BIM_GETIMPORTEDKEYS));
      addMap("getExportedKeys",     new Integer(BIM_GETEXPORTEDKEYS));
      addMap("getCrossReference",   new Integer(BIM_GETCROSSREFERENCE));
      addMap("getIndexInfo",        new Integer(BIM_GETINDEXINFO));
      addMap("getUDTs",             new Integer(BIM_GETUDTS));

/////////////////////////////////////////////////////////////////////
// db.DBHandle, db.QueryResult and db.UpdateResult
//
      addMap("getSQL",              new Integer(BIM_GETSQL));
      addMap("getResult",           new Integer(BIM_GETRESULT));
      addMap("getOneResult",        new Integer(BIM_GETONERESULT));
      addMap("getResultSetType",    new Integer(BIM_GETRESULTSETTYPE));
      addMap("getResultSet",        new Integer(BIM_GETRESULTSET));
      addMap("getResultSetMetaData",new Integer(BIM_GETRESULTSETMETADATA));
      addMap("getPreparedStatement",new Integer(BIM_GETPREPAREDSTATEMENT));
      I = new Integer(BIM_GETCOLUMNATTRS);
      addMap("getColumnAttrs",      I);
      addMap("getColumnAttributes", I);
      addMap("dumpResult",          new Integer(BIM_DUMPRESULT));

/////////////////////////////////////////////////////////////////////
// db.DBBatch
//
      addMap("execute",           new Integer(BIM_EXECUTE));

/////////////////////////////////////////////////////////////////////
// xml.XmlTag
//
      addMap("getName",           new Integer(BIM_GETNAME));
      addMap("getUri",            new Integer(BIM_GETURI));
      addMap("getLocal",          new Integer(BIM_GETLOCAL));
      addMap("getLocalText",      new Integer(BIM_GETLOCALTEXT));
      addMap("getRaw",            new Integer(BIM_GETRAW));
      I = new Integer(BIM_HASATTRS);
      addMap("hasAttrs",      I);
      addMap("hasAttributes", I);
      I = new Integer(BIM_COUNTATTRS);
      addMap("countAttrs",        I);
      addMap("countAttributes",   I);
      I = new Integer(BIM_GETATTRNAME);
      addMap("getAttrName",       I);
      addMap("getAttributeName",  I);
      I = new Integer(BIM_GETATTRVALUE);
      addMap("getAttrValue",      I);
      addMap("getAttributeValue", I);
      addMap("isEndTag",          new Integer(BIM_ISENDTAG));

/////////////////////////////////////////////////////////////////////
// StmtHtml$MarkupValue
//
      addMap("getRow",            new Integer(BIM_GETROW));
      addMap("getColumn",         new Integer(BIM_GETCOLUMN));
      addMap("isClosed",          new Integer(BIM_ISCLOSED));
      addMap("setName",           new Integer(BIM_SETNAME));
      addMap("getText",           new Integer(BIM_GETTEXT));
      addMap("getAllAttrs",       new Integer(BIM_GETALLATTRS));

/////////////////////////////////////////////////////////////////////
// shortcuts
//
      // '$$con' methods
      addMap("close",          BIM_CLOSE);
      addMap("commit",         BIM_COMMIT);
      addMap("rollback",       BIM_ROLLBACK);
      addMap("nativeSQL",      BIM_NATIVESQL);
      addMap("clearWarnings",  BIM_CLEARWARNINGS);
      addMap("reportWarnings", BIM_REPORTWARNINGS);
      addMap("executeUpdate",  BIM_EXECUTEUPDATE);
      addMap("executeQuery",   BIM_EXECUTEQUERY);
      addMap("executeSQL",     BIM_EXECUTESQL);
      addMap("executeSQLFile", BIM_EXECUTESQLFILE);
      addMap("executeBatch",   BIM_EXECUTEBATCH);
      addMap("getMetaData",    BIM_GETMETADATA);
      I = new Integer(BIM_DESCRIBE);
      addMap("describe",       I);
      addMap("desc",           I);
      I = new Integer(BIM_TABLEEXISTS);
      addMap("tableExists",    I);
      addMap("existsTable",    I);
      I = new Integer(BIM_OBJECTEXISTS);  // only for Oracle for now
      addMap("databaseObjectExists",   I);
      addMap("existsDatabaseObject",   I);
      addMap("getDatabaseObjectType", BIM_GETOBJECTTYPE); // only for Oracle for now
      I = new Integer(BIM_PROCEXISTS);
      addMap("procedureExists",I);
      addMap("procExists",     I);
      addMap("existsProcedure",I);
      addMap("existsProc",     I);
      I = new Integer(BIM_UDTEXISTS);
      addMap("userTypeExists",        I);
      addMap("userDefinedTypeExists", I);
      addMap("existsUserType",        I);
      addMap("existsUserDefinedType", I);
      I = new Integer(BIM_COUNTROWS);
      addMap("countRows",      I);
      addMap("countTableRows", I);
      addMap("addTypeMap",     BIM_ADDTYPEMAP);
      addMap("createBatch",    BIM_CREATEBATCH);

      addMap("next",           BIM_NEXT);
//      addMap("hasResultSet",   BIM_HASRESULTSET);

      // '$$sys' methods
      I = new Integer(BIM_DATE);
      addMap("day",                  I);
      addMap("date",                 I);
      addMap("Date",                 I);
      addMap("time",                 BIM_SYS_TIME);
      addMap("timeToday",            BIM_SYS_TIMETODAY);
      I = new Integer(BIM_SYS_RAND);
      addMap("rand",                 I);
      addMap("random",               I);
      addMap("getIn",                BIM_SYS_GETIN);
      addMap("getOut",               BIM_SYS_GETOUT);
      addMap("getErr",               BIM_SYS_GETERR);
      addMap("getLog",               BIM_SYS_GETLOG);
      addMap("setOut",               BIM_SYS_SETOUT);
      addMap("setErr",               BIM_SYS_SETERR);
      addMap("setLog",               BIM_SYS_SETLOG);
      addMap("setIn",                BIM_SYS_SETIN);
      addMap("max",                  BIM_MAX);
      addMap("min",                  BIM_MIN);
      I = new Integer(BIM_SYS_TIMERHANDLER);
      addMap("guiHandler",           I);
      addMap("timerHandler",         I);
      addMap("getGuiHandler",        I);
      addMap("setGuiListener",       BIM_SYS_SETGUILISTENER);
      I = new Integer(BIM_SYS_CONNECTMAILSERVER);
      addMap("connectMailServer",    I);
      addMap("mail::connect",        I);
      I = new Integer(BIM_SYS_DISCONNECTMAILSERVER);
      addMap("disconnectMailServer", I);
      addMap("mail::disconnect",     I);
      I = new Integer(BIM_SYS_GETDEFAULTDATEFORMAT);
      addMap("getDefaultDateFormat", I);
      addMap("defaultDateFormat",    I);
      addMap("setDefaultDateFormat", BIM_SYS_SETDEFAULTDATEFORMAT);
      addMap("compare",              BIM_SYS_COMPARE);
      addMap("lock",                 BIM_SYS_LOCK);
      addMap("unlock",               BIM_SYS_UNLOCK);
      addMap("waitFor",              BIM_SYS_WAITFOR);
      addMap("notify",               BIM_SYS_NOTIFY);
      addMap("notifyAll",            BIM_SYS_NOTIFYALL);
      addMap("secret",               BIM_SYS_SECRET);
      addMap("acceptHttp",           BIM_SYS_ACCEPTHTTP);
      addMap("httpGet",              BIM_SYS_HTTPGET);
      addMap("httpPost",             BIM_SYS_HTTPPOST);
      addMap("cookie",               BIM_SYS_COOKIE);
      I = new Integer(BIM_SYS_COPYSTREAM);
      addMap("copyStreams",          I);
      addMap("copyStream",           I);
      addMap("regex",                BIM_SYS_REGEX);
      addMap("setCharset",           BIM_SYS_SETCHARSET);
      addMap("getCharset",           BIM_SYS_GETCHARSET);
//      addMap("getInStream",          BIM_SYS_GETINSTREAM);
//      addMap("getOutStream",         BIM_SYS_GETOUTSTREAM);
      addMap("assert",               BIM_SYS_ASSERT);
      addMap("thisLine",             BIM_SYS_THISLINE);
      addMap("thisFile",             BIM_SYS_THISFILE);
      addMap("encode",               BIM_SYS_ENCODE);
      addMap("ssh",                  BIM_SYS_SSH);
      addMap("getFunctions",         BIM_SYS_GETFUNCTIONS);
      addMap("getThreads",           BIM_SYS_GETTHREADS);
      I = new Integer(BIM_SYS_GETENVVARS);
      addMap("getEnvVars",           I);
      addMap("getenvs",              I);
      I = new Integer(BIM_SYS_GETENVVAR);
      addMap("getEnvVar",            I);
      addMap("getenv",               I);
      addMap("diff",                 new Integer(BIM_SYS_DIFF));
      addMap("convertToVariables",   new Integer(BIM_SYS_CONVERTTOVARIABLES));
      addMap("setVariable",          new Integer(BIM_SYS_SETVARIABLE));
      addMap("getVariable",          new Integer(BIM_SYS_GETVARIABLE));
      addMap("exit",                 BIM_SYS_EXIT);
      addMap("getScriptPath",        BIM_SYS_GETSCRIPTPATH);
      addMap("loopIndex",            BIM_SYS_LOOPINDEX);
      addMap("echo",                 BIM_SYS_ECHO);
      addMap("boolean",              BIM_SYS_BOOLEAN);
      addMap("byte",                 BIM_SYS_BYTE);
      addMap("char",                 BIM_SYS_CHAR);
      addMap("short",                BIM_SYS_SHORT);
      addMap("int",                  BIM_SYS_INT);
      addMap("long",                 BIM_SYS_LONG);
      addMap("float",                BIM_SYS_FLOAT);
      addMap("double",               BIM_SYS_DOUBLE);
      addMap("antcall",              BIM_SYS_ANTCALL);
      addMap("eval",                 BIM_SYS_EVAL);
      addMap("evalSeparate",         BIM_SYS_EVALSEPARATE);
      addMap("evalFile",             BIM_SYS_EVALFILE);
      addMap("evalFileSeparate",     BIM_SYS_EVALFILESEPARATE);
      I = new Integer(BIM_SYS_DB_DISCONNECT);
      addMap("disconnect",           I);
      addMap("db::disconnect",       I);

      I = new Integer(BIM_PWD);
      addMap("pwd",                  I);
      addMap("curDir",               I);
      addMap("currentDir",           I);
      addMap("cd",                   BIM_CD);
      addMap("pushd",                BIM_PUSHD);
      addMap("popd",                 BIM_POPD);
      addMap("createTempFile",       BIM_CREATETEMPFILE);
      addMap("openFile",             BIM_OPENFILE);
      addMap("readLine",             BIM_READLINE);
      addMap("readPipe",             BIM_READPIPE);
      addMap("openTextFile",         BIM_OPENTEXTFILE);
      addMap("openRandomAccessFile", BIM_OPENRANDOMACCESSFILE);
      addMap("openGZippedFile",      BIM_OPENGZIPPEDFILE);
      addMap("openGZippedTextFile",  BIM_OPENGZIPPEDTEXTFILE);
      addMap("createTreeOutput",     BIM_CREATETREEOUTPUT);

      addMap("hib::addClass",        BIM_HIB_ADD_CLASS);
      addMap("hib::addResource",     BIM_HIB_ADD_RESOURCE);

      // '$$timer' methods
//      addMap("rescheduleStarting",         BIM_TIMER_RESCHEDULESTARTING);
//      addMap("rescheduleAfter",            BIM_TIMER_RESCHEDULEAFTER);
//      addMap("rescheduleAbsoluteStarting", BIM_TIMER_RESCHEDULEABSOLUTESTARTING);
//      addMap("rescheduleAbsoluteAfter",    BIM_TIMER_RESCHEDULEABSOLUTEAFTER);

    } catch(Exception e) {
      RT.logger.fatal("System failed to initilize ordinals.", e);
      System.exit(0);
    } finally {
      if (v.size() > 0) {
        RT.logger.error("Method assigned multiple times:");
        for (int i=0; i<v.size(); ++i)
          RT.logger.error("  " + v.get(i));
        System.exit(0);
      }
    }

  } // for setting bimMap.
 
  private static final double ln2 = Math.log(2.0);
  private static final double ln10 = Math.log(10.0);

  public static final boolean isStringMethod(int bim) {
    return (bim > MethodOrdinals.BIM_STRING_BASE) && (bim <= MethodOrdinals.BIM_FORMAT_BASE) ||
           (bim == BIM_TOCHARACTER) || (bim == BIM_SIZE) ||
           (bim == BIM_CAPITALIZEFIRSTLETTER) || (bim == BIM_CAPITALIZEALLFIRSTLETTERS);
  }

} // end of class VariableAdapter.

/*[judo]
//
// Saved in "examples/1.1-core-basics/eval/value_types.judo".
//
. '            Legend:';
. '              NUL  - isNull()    CLX  - isComplex()  INT  - isInt()    FLT  - isFloat()';
. '              STR  - isString()  DAT  - isDate()     ARR  - isArray()  SET  - isSet()';
. '              STT  - isStruct()  STK  - isStack()    QUE  - isQueue()  JAV  - isJava()';
. '              OBJ  - isObject()  FUN  - isFunction()';
.;
. '                                          N C I F N S D A S S S Q J O F';
. '                                          U L N L U T A R E T T U A B U';
. 'Expression                                L X T T M R T R T T K E V J N  Type Name';
. '----------------------------------------- - - - - - - - - - - - - - - -  ---------';

exprs = {
  [[* a = null; *]],
  [[* a = true; *]],
  [[* a = 1; *]],
  [[* a = 1.0; *]],
  [[* a = '1'; *]],
  [[* a = '1.0'; *]],
  [[* a = '0xabc'; *]],
  [[* a = '0777'; *]],
  [[* a = '1e34'; *]],
  [[* a = 'abc'; *]],
  [[* a = 'xyz'; *]],
  [[* a = javanew java.lang.String('Xyz'); *]],
  [[* a = javanew java.lang.Boolean(true); *]],
  [[* a = javanew java.lang.Byte(1); *]],
  [[* a = javanew java.lang.Character('c'); *]],
  [[* a = javanew java.lang.Short(1); *]],
  [[* a = javanew java.lang.Float(1.01); *]],
  [[* a = javanew java.lang.Double(1.01); *]],
  [[* a = date(); *]],
  [[* a = javanew java.util.Date; *]],
  [[* a = new array; *]],
  [[* a = {}; *]],
  [[* a = linkedList{}; *]],
  [[* a = javanew byte[4]; *]],
  [[* a = javanew java.util.Date[2]; *]],
  [[* a = javanew java.util.Hashtable; *]],
  [[* a = createDom(); *]],
  [[* a = createDom().cast('org.w3c.dom.Node'); *]],
  [[* a = new set; *]],
  [[* a = set<number>{}; *]],
  [[* a = new struct; *]],
  [[* a = new orderedMap; *]],
  [[* a = new sortedMap<string>; *]],
  [[* a = new treeNode; *]],
  [[* a = new stack; *]],
  [[* a = new queue; *]],
  [[* a = new tableData; *]],
  [[* a = openFile(#prog); *]],
  [[* a = openTextFile(#prog); *]],
  [[*
      class C {}
      a = new C;
  *]],
  [[*
      class C extends orderedMap {}
      a = new C;
  *]],
  [[*
      class C extends treeNode {}
      a = new C;
  *]],
  [[*
      class D extends C {}
      a = new D;
  *]],
  [[* a = lambda {}; *]],
  [[*
      do ('<a>').getReader() as sgml {
      <a>: a = $_;
      }
  *]],
  [[*
      do ('</a>').getReader() as sgml {
      </a>: a = $_;
      }
  *]],
  [[*
      do ('</a>  ').getReader() as sgml {
      TEXT: a = $_;
      }
  *]],

};

for expr in exprs {
  eval expr;
  firstLine = true;
  do expr.getReader() as lines {
    if firstLine {
      . $_ :<41,
        a.isNull()    .fmtBool('.','X') :> 2,
        a.isComplex() .fmtBool('.','X') :> 2,
        a.isInt()     .fmtBool('.','X') :> 2,
        a.isFloat()   .fmtBool('.','X') :> 2,
        a.isNumber()  .fmtBool('.','X') :> 2,
        a.isString()  .fmtBool('.','X') :> 2,
        a.isDate()    .fmtBool('.','X') :> 2,
        a.isArray()   .fmtBool('.','X') :> 2,
        a.isSet()     .fmtBool('.','X') :> 2,
        a.isStruct()  .fmtBool('.','X') :> 2,
        a.isStack()   .fmtBool('.','X') :> 2,
        a.isQueue()   .fmtBool('.','X') :> 2,
        a.isJava()    .fmtBool('.','X') :> 2,
        a.isObject()  .fmtBool('.','X') :> 2,
        a.isFunction().fmtBool('.','X') :> 2,
        '  ', a.getTypeName();
      firstLine = false;
    } else {
      . $_;
    }
  }
}
[judo]*/
