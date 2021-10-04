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


package com.judoscript.util;

import java.util.*;


public final class MyCalendar extends GregorianCalendar
{
  public MyCalendar() {}
  public MyCalendar(long millis) { setTime(millis); }
  public MyCalendar(int year, int month, int day, int hr, int min, int sec) {
    super(year,month,day,hr,min,sec);
  }

  public void setTime(long millis) { setTimeInMillis(millis); }
  public long getTimeMillis() { return getTimeInMillis(); }
}

