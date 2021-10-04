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

import java.util.Comparator;
import com.judoscript.*;


/**
 * User-defined comparator; also filter and converter for arrays.
 */
public class UserDefinedComparator implements Comparator, Consts
{
  AccessFunction af;

  public UserDefinedComparator() {
    af = null;
  }

  public UserDefinedComparator(AccessFunction af) { setAccessFunction(af); }

  public void setAccessFunction(AccessFunction af) { this.af = af; }

  // expects af $lhs, $rhs : 1|0|-1 ;
  public int compare(Object o1, Object o2) {
    try {
      if (af != null) {
        try {
          if (o1 == null) o1 = ValueSpecial.NIL;
          if (o2 == null) o2 = ValueSpecial.NIL;
          Expr[] ea = new Expr[] { (Variable)o1, (Variable)o2 };
          return (int)RT.call(af.getName(), ea, null).getLongValue();
        } catch(Throwable e) {}
      }
    } catch(ClassCastException cce) {}
    return 0;
  }

  // expects af $v : boolean ;
  public boolean filter(Variable v) {
    try {
      Expr[] ea = new Expr[]{ v };
      return RT.call(af.getName(), ea, null).getBoolValue();
    } catch(Throwable e) {}
    return false;
  }

  // expects af $v : Variable ;
  public Variable convert(Variable v) {
    try {
      return RT.call(af.getName(), new Expr[]{ v }, null);
    } catch(Throwable e) {}
    return ValueSpecial.UNDEFINED;
  }

  public static Comparator getComparator(Object cptr) throws Throwable {
    if (cptr == null)
      return null;
    if (cptr instanceof Comparator)
      return (Comparator)cptr;
    try {
      UserDefinedComparator c= new UserDefinedComparator();
      c.setAccessFunction((AccessFunction)((Expr)cptr).eval());
      return c;
    } catch(ClassCastException cce) {
      ExceptionRuntime.rte(RTERR_ILLEGAL_ARGUMENTS, "A comparator function is required.");
      return null;
    }
  }

  //////////////////////////////////////////////////
  //

  public static Comparator theNumberComparator = new Comparator() {
    public int compare(Object o1, Object o2) {
      if (o1 == null) return -1;
      if (o2 == null) return 1;
      try {
        double d1 = ((Variable)o1).getDoubleValue();
        double d2 = ((Variable)o2).getDoubleValue();
        return (d1==d2) ? 0 : ((d1>d2) ? 1 : -1);
      } catch(Throwable e) {
        return 0;
      }
    }
  };

  public static Comparator theDescendingNumberComparator = new Comparator() {
    public int compare(Object o1, Object o2) {
      if (o1 == null) return -1;
      if (o2 == null) return 1;
      try {
        double d1 = ((Variable)o1).getDoubleValue();
        double d2 = ((Variable)o2).getDoubleValue();
        return (d1==d2) ? 0 : ((d1>d2) ? -1 : 1);
      } catch(Throwable e) {
        return 0;
      }
    }
  };

  public static Comparator theDateComparator = new Comparator() {
    public int compare(Object o1, Object o2) {
      if (o1 == null) return -1;
      if (o2 == null) return 1;
      try {
        long l1 = ((Variable)o1).getLongValue();
        long l2 = ((Variable)o2).getLongValue();
        return (l1==l2) ? 0 : ((l1>l2) ? 1 : -1);
      } catch(Throwable e) {
        return 0;
      }
    }
  };

  public static Comparator theDescendingDateComparator = new Comparator() {
    public int compare(Object o1, Object o2) {
      if (o1 == null) return -1;
      if (o2 == null) return 1;
      try {
        long l1 = ((Variable)o1).getLongValue();
        long l2 = ((Variable)o2).getLongValue();
        return (l1==l2) ? 0 : ((l1>l2) ? -1 : 1);
      } catch(Throwable e) {
        return 0;
      }
    }
  };

  public static Comparator theStringComparator = new Comparator() {
    public int compare(Object o1, Object o2) {
      if (o1 == null) return -1;
      if (o2 == null) return 1;
      try {
        return ((Variable)o1).getStringValue().compareTo( ((Variable)o2).getStringValue() );
      } catch(Throwable e) {
        return 0;
      }
    }
  };

  public static Comparator theDescendingStringComparator = new Comparator() {
    public int compare(Object o1, Object o2) {
      if (o1 == null) return -1;
      if (o2 == null) return 1;
      try {
        return ((Variable)o2).getStringValue().compareTo( ((Variable)o1).getStringValue() );
      } catch(Throwable e) {
        return 0;
      }
    }
  };

  public static Comparator theNaturalComparator = new Comparator() {
    public int compare(Object o1, Object o2) {
      if (o1 == null) return -1;
      if (o2 == null) return 1;
      try {
        o1 = ((Variable)o1).getObjectValue();
        o2 = ((Variable)o2).getObjectValue();
        if (o1 instanceof Comparable) return ((Comparable)o1).compareTo(o2);
        return o1.equals(o2) ? 0 : 1;
      } catch(Throwable e) {
        return 1;
      }
    }
  };

} // end of class UserDefinedComparator.
