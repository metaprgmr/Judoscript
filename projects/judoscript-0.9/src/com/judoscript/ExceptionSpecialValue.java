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

import java.util.HashMap;


public class ExceptionSpecialValue extends Exception
{
  public static final ExceptionSpecialValue UNDEFINED =
      new ExceptionSpecialValue(ValueSpecial.UNDEFINED);

  public static final ExceptionSpecialValue NaN =
      new ExceptionSpecialValue(ValueSpecial.NaN);

  public static final ExceptionSpecialValue MAX_NUMBER =
      new ExceptionSpecialValue(ValueSpecial.MAX_NUMBER);

  public static final ExceptionSpecialValue MIN_NUMBER =
      new ExceptionSpecialValue(ValueSpecial.MIN_NUMBER);

  public static final ExceptionSpecialValue POSITIVE_INFINITY =
      new ExceptionSpecialValue(ValueSpecial.POSITIVE_INFINITY);

  public static final ExceptionSpecialValue NEGATIVE_INFINITY =
      new ExceptionSpecialValue(ValueSpecial.NEGATIVE_INFINITY);

  static final HashMap map = new HashMap();
  static {
    map.put(ValueSpecial.UNDEFINED, UNDEFINED);
    map.put(ValueSpecial.NaN, NaN);
    map.put(ValueSpecial.MAX_NUMBER, MAX_NUMBER);
    map.put(ValueSpecial.MIN_NUMBER, MIN_NUMBER);
    map.put(ValueSpecial.POSITIVE_INFINITY, POSITIVE_INFINITY);
    map.put(ValueSpecial.NEGATIVE_INFINITY, NEGATIVE_INFINITY);
  }
  public static ExceptionSpecialValue get(ValueSpecial val) { return (ExceptionSpecialValue)map.get(val); }

  ValueSpecial result;
  public ExceptionSpecialValue(ValueSpecial res) {
    super("Special Value: "+res);
    result = res;
  }
  public ValueSpecial getResult() { return result; }
}
