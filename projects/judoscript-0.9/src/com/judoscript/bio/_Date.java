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


package com.judoscript.bio;

import java.util.*;
import java.text.SimpleDateFormat;
import org.apache.commons.lang.StringUtils;
import com.judoscript.*;
import com.judoscript.util.MyCalendar;


public final class _Date extends ObjectInstance
{
  long time;

  public _Date() { super(); time = System.currentTimeMillis(); }
  public _Date(long time) { super(); this.time = time; }
  public _Date(Date time) { super(); this.time = time.getTime(); }

  public void init(Object inits) throws Throwable {
    if (inits instanceof Expr[]) {
      Expr[] ea = (Expr[])inits;
      int len = (ea==null) ? 0 : ea.length;
      if (len <= 0) {
        time = System.currentTimeMillis();
      } else {
        int year= (int)ea[0].getLongValue();
        int mon = (len < 2) ? 0 : (int)ea[1].getLongValue()-1;
        int day = (len < 3) ? 1 : (int)ea[2].getLongValue();
        int hr  = (len < 4) ? 0 : (int)ea[3].getLongValue();
        int min = (len < 5) ? 0 : (int)ea[4].getLongValue();
        int sec = (len < 6) ? 0 : (int)ea[5].getLongValue();
        if (mon<0) mon = 0;
        time = new MyCalendar(year,mon,day,hr,min,sec).getTimeMillis();
      }
    } else super.init(inits);
  }

  public void initTime(Object inits) throws Throwable {
    if (inits instanceof Expr[]) {
      Expr[] ea = (Expr[])inits;
      int len = (ea==null) ? 0 : ea.length;
      if (len == 0)
        time = 0;
      else {
        int hr  = (len < 1) ? 0 : (int)ea[0].getLongValue();
        int min = (len < 2) ? 0 : (int)ea[1].getLongValue();
        int sec = (len < 3) ? 0 : (int)ea[2].getLongValue();
        time = new MyCalendar(0,0,1,hr,min,sec).getTimeMillis();
      }
    } else super.init(inits);
  }

  public void initTimeToday(Object inits) throws Throwable {
    if (inits instanceof Expr[]) {
      Expr[] ea = (Expr[])inits;
      int len = (ea==null) ? 0 : ea.length;
      if (len == 0)
        time = System.currentTimeMillis();
      else {
        GregorianCalendar cal = new GregorianCalendar();
        int year= cal.get(Calendar.YEAR);
        int mon = cal.get(Calendar.MONTH);
        int date= cal.get(Calendar.DATE);
        int hr  = (len < 1) ? 0 : (int)ea[0].getLongValue();
        int min = (len < 2) ? 0 : (int)ea[1].getLongValue();
        int sec = (len < 3) ? 0 : (int)ea[2].getLongValue();
        time = new MyCalendar(year,mon,date,hr,min,sec).getTimeMillis();
      }
    } else super.init(inits);
  }

  public java.sql.Date getSqlDate() { return new java.sql.Date(time); }
  public java.sql.Time getSqlTime() { return new java.sql.Time(time); }
  public java.sql.Timestamp getSqlTimestamp() { return new java.sql.Timestamp(time); }
  public void close() {}

  public int getType() { return TYPE_DATE; }
  public boolean isDate() { return true; }
  public String getTypeName() { return "Date"; }

  public void removeVariable(String name) {}
  public void clearVariables() {}

  public long getLongValue() throws Throwable { return time; }
  public double getDoubleValue() throws Throwable { return (double)time; }
  public String getStringValue() throws Throwable { return RT.getDefaultDateFormat().format(new Date(time)); }
  public Object getObjectValue() throws Throwable { return new java.util.Date(time); }
  public java.util.Date getDateValue() throws Throwable { return new java.util.Date(time); }
  public String toString() {
    try { return getStringValue(); }
    catch(Throwable e) {
      return "";   
    }
  }

  static final Variable[] monthNames = {
    new ConstString("January"),
    new ConstString("February"),
    new ConstString("March"),
    new ConstString("April"),
    new ConstString("May"),
    new ConstString("June"),
    new ConstString("July"),
    new ConstString("August"),
    new ConstString("September"),
    new ConstString("October"),
    new ConstString("November"),
    new ConstString("December"),
    new ConstString("Undecimber")
  };
  static final Variable[] monthShortNames = {
    new ConstString("Jan"),
    new ConstString("Feb"),
    new ConstString("Mar"),
    new ConstString("Apr"),
    new ConstString("May"),
    new ConstString("June"),
    new ConstString("July"),
    new ConstString("Aug"),
    new ConstString("Sept"),
    new ConstString("Oct"),
    new ConstString("Nov"),
    new ConstString("Dec"),
    new ConstString("Undec")
  };
  static final Variable[] weekNames = {
    new ConstString("Sunday"),
    new ConstString("Monday"),
    new ConstString("Tuesday"),
    new ConstString("Wednesday"),
    new ConstString("Thurday"),
    new ConstString("Friday"),
    new ConstString("Saturday")
  };
  static final Variable[] weekShortNames = {
    new ConstString("Sun"),
    new ConstString("Mon"),
    new ConstString("Tue"),
    new ConstString("Wed"),
    new ConstString("Thu"),
    new ConstString("Fri"),
    new ConstString("Sat")
  };

  Variable getMonthName(int cal_mon) {
    try { return monthNames[cal_mon]; } catch(Exception e) { return ValueSpecial.UNDEFINED; }
  }
  Variable getMonthShortName(int cal_mon) {
    try { return monthShortNames[cal_mon]; } catch(Exception e) { return ValueSpecial.UNDEFINED; }
  }
  Variable getWeekName(int wk_mon) {
    try { return weekNames[wk_mon]; } catch(Exception e) { return ValueSpecial.UNDEFINED; }
  }
  Variable getWeekShortName(int wk_mon) {
    try { return weekShortNames[wk_mon]; } catch(Exception e) { return ValueSpecial.UNDEFINED; }
  }

  public Variable resolveVariable(String name) throws Throwable {
    long ival = 0;
    MyCalendar myCal = new MyCalendar(time);
    int ord = getMethodOrdinal(name);
    switch(ord) {
    case BIM_MONTH_NAME:           return getMonthName(myCal.get(Calendar.MONTH));
    case BIM_MONTH_SHORT_NAME:     return getMonthShortName(myCal.get(Calendar.MONTH));
    case BIM_WEEK_NAME:            return getWeekName(myCal.get(Calendar.DAY_OF_WEEK)-1);
    case BIM_WEEK_SHORT_NAME:      return getWeekShortName(myCal.get(Calendar.DAY_OF_WEEK)-1);
    case BIM_IS_AM:                return ConstInt.getBool(Calendar.AM == myCal.get(Calendar.AM_PM));
    case BIM_IS_PM:                return ConstInt.getBool(Calendar.PM == myCal.get(Calendar.AM_PM));
    case BIM_EPOCH:                return ConstInt.getInt(time);
    case BIM_YEAR:                 return ConstInt.getInt(myCal.get(Calendar.YEAR));
    case BIM_MONTH:                return ConstInt.getInt(myCal.get(Calendar.MONTH)+1);
    case BIM_DATE:                 return ConstInt.getInt(myCal.get(Calendar.DATE));
    case BIM_HOUR:                 return ConstInt.getInt(myCal.get(Calendar.HOUR_OF_DAY));
    case BIM_MINUTE:               return ConstInt.getInt(myCal.get(Calendar.MINUTE));
    case BIM_SECOND:               return ConstInt.getInt(myCal.get(Calendar.SECOND));
    case BIM_MILLISECOND:          return ConstInt.getInt(myCal.get(Calendar.MILLISECOND));
    case BIM_ZONE_OFFSET:          return ConstInt.getInt(myCal.get(Calendar.ZONE_OFFSET));
    case BIM_DST_OFFSET:           return ConstInt.getInt(myCal.get(Calendar.DST_OFFSET));
    case BIM_WEEK_OF_YEAR:         return ConstInt.getInt(myCal.get(Calendar.WEEK_OF_YEAR));
    case BIM_WEEK_OF_MONTH:        return ConstInt.getInt(myCal.get(Calendar.WEEK_OF_MONTH));
    case BIM_DAY_OF_MONTH:         return ConstInt.getInt(myCal.get(Calendar.DAY_OF_MONTH)+1);
    case BIM_DAY_OF_YEAR:          return ConstInt.getInt(myCal.get(Calendar.DAY_OF_YEAR));
    case BIM_DAY_OF_WEEK:          return ConstInt.getInt(myCal.get(Calendar.DAY_OF_WEEK));
    case BIM_DAY_OF_WEEK_IN_MONTH: return ConstInt.getInt(myCal.get(Calendar.DAY_OF_WEEK_IN_MONTH));
    default:                       return ConstInt.ZERO;
    }
  }

  public Variable setVariable(String name, Variable val, int type) throws Throwable {
    checkWritable();
    MyCalendar myCal = new MyCalendar(time);
    long ival = val.getLongValue();
    try {
      switch(getMethodOrdinal(name)) {
      case BIM_EPOCH:       time = ival; return this;
      case BIM_YEAR:        myCal.set(Calendar.YEAR,       (int)ival);   break;
      case BIM_MONTH:       myCal.set(Calendar.MONTH,      (int)ival-1); break;
      case BIM_DATE:        myCal.set(Calendar.DATE,       (int)ival);   break;
      case BIM_HOUR:        myCal.set(Calendar.HOUR_OF_DAY,(int)ival);   break;
      case BIM_MINUTE:      myCal.set(Calendar.MINUTE,     (int)ival);   break;
      case BIM_SECOND:      myCal.set(Calendar.SECOND,     (int)ival);   break;
      case BIM_MILLISECOND: myCal.set(Calendar.MILLISECOND,(int)ival);   break;
      case BIM_ZONE_OFFSET: myCal.set(Calendar.ZONE_OFFSET,(int)ival);   break;
      case BIM_DST_OFFSET:  myCal.set(Calendar.DST_OFFSET, (int)ival);   break;
      default:              ExceptionRuntime.rte(RTERR_ILLEGAL_VALUE_SETTING);
      }
      time = myCal.getTimeMillis();
      return resolveVariable(name);
    } catch(Exception e) {
      ExceptionRuntime.rte(RTERR_ILLEGAL_VALUE_SETTING,"Date field '"+name+"' cannot be set.");
    }
    return null;
  }

  /////////////////////////////////////////////////////////
  // Methods
  //

  public Variable invoke(String fxn, Expr[] params, int[] javaTypes) throws Throwable
  {
    int len = (params==null) ? 0 : params.length;
    int ord = getMethodOrdinal(fxn);

    String fmt = null;
    switch(ord) {
    case BIM_FORMATDATE:
      if (len > 0) fmt = params[0].eval().getStringValue();
      SimpleDateFormat sdf = StringUtils.isBlank(fmt) ? RT.getDefaultDateFormat() : new SimpleDateFormat(fmt);
      if (len > 1)
        sdf.setTimeZone(TimeZone.getTimeZone(params[1].getStringValue()));
      return JudoUtil.toVariable( sdf.format(getDateValue()) );

    case BIM_NEXT:
      if (len==0)
        return new _Date(time+24*3600*1000); // 1 day
      long i = (len==1) ? 1 : params[1].getLongValue();
      fmt = params[0].getStringValue();
      if (fmt == null || fmt.length() == 0)
        fmt = "d";
      switch (fmt.charAt(0)) {
      case 'd': return new _Date(time+i*24*3600*1000);
      case 'h': return new _Date(time+i*3600*1000);
      case 's': return new _Date(time+i*1000);
      case 'm': if (fmt.length()>1 && fmt.charAt(1)=='i')
                  return new _Date(time+i*60*1000);
      case 'y': MyCalendar myCal = new MyCalendar(time);
                if (fmt.charAt(0)=='y')
                  myCal.set(Calendar.YEAR, myCal.get(Calendar.YEAR)+(int)i);
                else
                  myCal.set(Calendar.MONTH, myCal.get(Calendar.MONTH)+(int)i);
                return new _Date(myCal.getTimeMillis());
      default: ExceptionRuntime.rte(RTERR_ILLEGAL_ARGUMENTS,
                                    "Invalid next() option: " + fmt);
      }

    case BIM_BEFORE:
    case BIM_AFTER:
    case BIM_SETTIME:
      long x = (len>0) ? params[0].getLongValue() : System.currentTimeMillis();
      switch(ord) {
      case BIM_BEFORE: return ConstInt.getBool(time < x);
      case BIM_AFTER:  return ConstInt.getBool(time > x);
      default:         time = x; return ValueSpecial.UNDEFINED;
      }

    case BIM_GETTIME: return ConstInt.getInt(getLongValue());

    case BIM_ENSUREDATE:
      MyCalendar myCal = new MyCalendar(time);
      myCal.set(Calendar.HOUR_OF_DAY, 0);
      myCal.set(Calendar.MINUTE,      0);
      myCal.set(Calendar.SECOND,      0);
      myCal.set(Calendar.MILLISECOND, 0);
      time = myCal.getTimeMillis();
      return this;
    }
//    return super.invoke(fxn,params,javaTypes);
    return super.invoke(ord,fxn,params);
  }

  public Variable cloneValue() { return new _Date(time); }

} // end of class _Date.

