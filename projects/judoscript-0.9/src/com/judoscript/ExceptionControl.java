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

public class ExceptionControl extends Exception implements Consts
{
  public static final int CTL_INVALID  = 0;
  public static final int CTL_BREAK    = 1;
  public static final int CTL_CONTINUE = 2;
  public static final int CTL_RESUME   = 3;
  public static final int CTL_RETURN   = 4;
  public static final String[] names = { "invalid", "break", "continue", "resume", "return" };

  int type;
  String label;

  protected ExceptionControl(int type, String label) {
    this.type = type;
    this.label = label;
  }
  public int getType() { return type; }
  public String getTypeName() {
    try {
      return names[type];
    } catch(Exception e) {
      return names[CTL_INVALID];
    }
  }
  public String  getLabel()   { return label; }
  public boolean isBreak()    { return (type==CTL_BREAK); }
  public boolean isContinue() { return (type==CTL_CONTINUE); }
  public boolean isResume()   { return (type==CTL_RESUME); }
  public boolean isReturn()   { return (type==CTL_RETURN); }
  public boolean isForSchedule() { return LABEL_SCHEDULE.equals(label); }
  public Variable getReturnValue() { return null; } // See ExceptionReturn.

  public static void throwReturnException(Variable v) throws ExceptionControl { throw new ExceptionReturn(v); }
  public static final ExceptionControl INVALID  = new ExceptionControl(CTL_INVALID,null);
  public static final ExceptionControl BREAK    = new ExceptionControl(CTL_BREAK,null);
  public static final ExceptionControl CONTINUE = new ExceptionControl(CTL_CONTINUE,null);
  public static final ExceptionControl RESUME   = new ExceptionControl(CTL_RESUME,null);

  public String toString() {
    if (label == null)
      return getTypeName();
    else
      return getTypeName () + " " + label;
  }

  public static class ExceptionReturn extends ExceptionControl
  {
    Variable returnValue;

    ExceptionReturn(Variable retVal) {
      super(CTL_RETURN, null);
      returnValue = retVal;
    }
    
    public Variable getReturnValue() { return returnValue; }
  }

} // end of class ExceptionControl.
