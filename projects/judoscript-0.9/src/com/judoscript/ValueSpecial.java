/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 03-17-2002  JH   Initial open source release.
 * 07-07-2003  JH   Changed to this name for more special values.
 * 06-24-2004  JH   Added UNDEFINED.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript;

import com.judoscript.util.XMLWriter;


public class ValueSpecial extends ValueBase
{
  public static final int ID_POSITIVE_INFINITY = 1;
  public static final int ID_NEGATIVE_INFINITY = 2;
  public static final int ID_MAX_NUMBER        = 3;
  public static final int ID_MIN_NUMBER        = 4;
  public static final int ID_NaN               = 5;
  public static final int ID_NIL               = 6;
  public static final int ID_UNDEFINED         = 7;

  public static final ValueSpecial NIL =
    new ValueSpecial("null", TYPE_NIL, "", null, ID_NIL); 
  public static final ValueSpecial UNDEFINED =
    new ValueSpecial("undefined", TYPE_UNDEFINED, "", null, ID_UNDEFINED); 
  public static final ValueSpecial NaN =
    new ValueSpecial("NaN", TYPE_NAN, "NaN", new Double(Double.NaN), ID_NaN); 
  public static final ValueSpecial POSITIVE_INFINITY =
    new ValueSpecial("positive_infinity", TYPE_DOUBLE, "infinity",
                     new Double(Double.POSITIVE_INFINITY), ID_POSITIVE_INFINITY); 
  public static final ValueSpecial NEGATIVE_INFINITY =
    new ValueSpecial("negative_infinity", TYPE_DOUBLE, "-infinity",
                     new Double(Double.NEGATIVE_INFINITY), ID_NEGATIVE_INFINITY); 
  public static final ValueSpecial MAX_NUMBER =
    new ValueSpecial("max_number", TYPE_DOUBLE, "max_number",
                     new Double(Double.MAX_VALUE), ID_MAX_NUMBER); 
  public static final ValueSpecial MIN_NUMBER =
    new ValueSpecial("min_number", TYPE_DOUBLE, "min_number",
                     new Double(-Double.MAX_VALUE), ID_MIN_NUMBER); 

  private String typeName;
  private int    type;
  private String stringValue;
  private Object objectValue;
  private int    id; // a performance-enhancing feature.


  private ValueSpecial(String typeName, int type, String strVal, Object objVal, int id) {
    this.typeName = typeName;
    this.type = type;
    this.stringValue = strVal;
    this.objectValue = objVal;
    this.id = id;
  }

  public final String getTypeName() { return typeName; }
  public final int getType() { return type; }
  public Variable cloneValue() { return this; } // always singleton!
  public int getID() { return id; }

  public final boolean isNil() { return id == ID_NIL || id == ID_UNDEFINED; }
  public final boolean isString() { return id == ID_NIL || id == ID_NaN; }
  public final boolean isNumber() { return id != ID_NaN; }
  public final boolean isInt() { return id != ID_NaN; }
  public final boolean isDouble() { return id != ID_NaN; }

  public final String  getStringValue() throws Throwable { return stringValue; }
  public final Object  getObjectValue() throws Throwable { return objectValue; }
  public final boolean getBoolValue() throws Throwable { return type!=TYPE_NIL && type!=TYPE_UNDEFINED; }
  public final long    getLongValue() throws ExceptionSpecialValue, Throwable {
    if (isNil()) return 0;
    throw ExceptionSpecialValue.get(this);
  }
  public final double  getDoubleValue() throws ExceptionSpecialValue, Throwable {
    if (isNil()) return 0.0;
    throw ExceptionSpecialValue.get(this);
  }

  public final java.util.Date getDateValue() throws Throwable { return new java.util.Date(0); }
  public final java.sql.Date getSqlDate() { return null; }
  public final java.sql.Time getSqlTime() { return null; }
  public final java.sql.Timestamp getSqlTimestamp() { return null; }

  public final String concat(String v) { return null; }
  public final void setStringValue(String v) {}
  public final void setBoolValue(boolean b) {}
  public final void setLongValue(long v) {}
  public final void setDoubleValue(double v) {}

  /////////////////////////////////////////////////////////////////////////////
  // Arithmic operators
  //

  /**
   * This method must be called with at least one the values is a
   * ValueSpecial.POSITIVE_INFINITY, NEGATIVE_INFINITY, MAX_NUMBER or /MIN_NUMBER.
   * The result will be a thrown ExceptionSpecialValue.
   * If 0 is intended, ExceptionSpecialValue.UNDEFINED is thrown.
   */
  public static void handleMulDivMod(Variable[] va, int[] ops) throws Throwable {
    // Notice that, the final result is either 0 or ExceptionSpecialValue;
    ValueSpecial cur = null;
    int sign = 1; // can be -1, 1.
    double d;
    if (va[0] instanceof ValueSpecial) {
      cur = (ValueSpecial)va[0];
      switch(cur.getID()) {
      case ValueSpecial.ID_NEGATIVE_INFINITY: cur = ValueSpecial.POSITIVE_INFINITY; sign=-1; break;
      case ValueSpecial.ID_MIN_NUMBER:        cur = ValueSpecial.MAX_NUMBER;        sign=-1; break;
      }
    } else {
      d = va[0].getDoubleValue();
      if (d == 0.0)
        throw ExceptionSpecialValue.UNDEFINED;
      if (d < 0)
        sign = -1;
    }
    for (int i=1; i<va.length; ++i) {
      Variable x = va[i];
      int op = ops[i-1];

      //  Cell contains actions for (cur,sign):
      //
      //  tmp      0       1      2      3      4      5      6
      // -------+------+------+------+------+------+------+------+-----
      //  cur\x |  0   |  +1  |  -1  | INF  | -INF | MAX  | MIN  |
      // -------+------+------+------+------+------+------+------+-----
      //  null  |  0!  |      |   *-1|INF   |INF*-1|MAX   |MAX*-1| MUL  0
      //  INF   |  0!  |      |   *-1|INF   |   *-1|      |   *-1| MUL  1
      //  MAX   |  0!  |      |   *-1|INF   |INF*-1|      |   *-1| MUL  2
      // -------+------+------+------+------+------+------+------+-----
      //  null  |INF   |      |   *-1|  0!  |  0!  |  0!  |  0!  | DIV  3
      //  INF   |INF   |      |   *-1|  0!  |  0!  |      |   *-1| DIV  4
      //  MAX   |INF   |      |   *-1|  0!  |  0!  |  0!  |  0!  | DIV  5
      // -------+------+------+------+------+------+------+------+-----

      int tmp = 0;
      if (x instanceof ValueSpecial) {
        if (x.isNil())
          tmp = 0;
        else {
          switch(((ValueSpecial)x).getID()) {
          case ValueSpecial.ID_POSITIVE_INFINITY: tmp = 3; break;
          case ValueSpecial.ID_NEGATIVE_INFINITY: tmp = 4; break;
          case ValueSpecial.ID_MAX_NUMBER:        tmp = 5; break;
          case ValueSpecial.ID_MIN_NUMBER:        tmp = 6; break;
          default:                                throw ExceptionSpecialValue.NaN;
          }
        }
      } else {
        d = x.getDoubleValue();
        if (d == 0.0)
          tmp = 0;
        else
          tmp = (d>0) ? 1 : 2;
      }

      // situations(cur,tmp): (0-2,0), (3-5,3-4)
      if (tmp==0 && op==OP_MUL || (tmp==3 || tmp==4) && op!=OP_MUL)
        throw ExceptionSpecialValue.UNDEFINED;

      // situations(cur,tmp): (3-5,0), (0-2,3-4)
      if (tmp==0 && op!=OP_MUL || (tmp==3 || tmp==4) && op==OP_MUL)
        cur = ValueSpecial.POSITIVE_INFINITY;

      // situations(cur,tmp): (0,5-6)
      if (tmp>=5 && cur==null && op==OP_MUL)
        cur = ValueSpecial.MAX_NUMBER;

      // situations(cur,tmp): (5-6,3), (5-6,5)
      if (tmp>=5 && cur!=ValueSpecial.POSITIVE_INFINITY && op!=OP_MUL)
        throw ExceptionSpecialValue.UNDEFINED;

      // situations(cur,tmp): (0-5,2), (0-5,4), (0-5,6), sign part.
      switch (tmp) {
      case 2: case 4: case 6:  sign *= -1;
      }
    }

    throw ExceptionSpecialValue.get(sign<0 ? negate(cur) : cur);
  }

  /**
   * This method must be called with at least one ValueSpecial and must be numerical.
   */
  public static Variable handleAddMinus(Variable lhs, int op, Variable rhs) throws Throwable {
    if (lhs instanceof ValueSpecial) {
      switch(((ValueSpecial)lhs).getID()) {
      case ID_POSITIVE_INFINITY:
      case ID_NEGATIVE_INFINITY: return lhs;
      case ID_MAX_NUMBER:
      case ID_MIN_NUMBER:
        return (rhs!=POSITIVE_INFINITY && rhs!=NEGATIVE_INFINITY)
               ? lhs : (op==OP_PLUS) ? rhs : negate((ValueSpecial)rhs);
      case ID_NIL:
      case ID_UNDEFINED:
        if (op == OP_PLUS) return rhs;
        if (rhs==POSITIVE_INFINITY || rhs==NEGATIVE_INFINITY) {
          return negate((ValueSpecial)rhs);
        } else {
          if (rhs.isInt())
            return ConstInt.getInt(-rhs.getLongValue());
          return ConstDouble.getDouble(-rhs.getDoubleValue());
        }
      }
    } else { // rhs must be a ValueSpecial.
      if (lhs instanceof ValueSpecial) {
        switch(((ValueSpecial)lhs).getID()) {
        case ID_POSITIVE_INFINITY:
        case ID_NEGATIVE_INFINITY:
        case ID_MAX_NUMBER:
        case ID_MIN_NUMBER:        return (op==OP_PLUS) ? rhs : negate((ValueSpecial)rhs);
        }
      }
      return lhs;
    }
    return null; // never happens.
  }

  /////////////////////////////////////////////////////////////////////////////
  // Negate operator
  //

  public static ValueSpecial negate(ValueSpecial val) {
    if (val == MAX_NUMBER) return MIN_NUMBER;
    if (val == MIN_NUMBER) return MAX_NUMBER;
    if (val == POSITIVE_INFINITY) return NEGATIVE_INFINITY;
    if (val == NEGATIVE_INFINITY) return POSITIVE_INFINITY;
    return val;
  }

  /////////////////////////////////////////////////////////////////////////////
  // Comparison operators
  //

  private static boolean ___ = false;

  // l\r       NIL    UND    NaN    MAX    MIN    INF    -INF   NUM
  private static boolean[][] compare_LT = {
  /* NIL */  { false, false, false, true,  false, true,  false, ___   }, // 0
  /* UND */  { false, false, false, true,  false, true,  false, ___   }, // 1
  /* NaN */  { false, false, false, false, false, false, false, false }, // 2
  /* MAX */  { false, false, false, false, false, true,  false, false }, // 3
  /* MIN */  { false, false, false, false, false, true,  false, false }, // 4
  /* INF */  { false, false, false, false, false, false, false, false }, // 5
  /*-INF */  { true,  true,  false, true,  true,  true,  false, true  }, // 6
  /* NUM */  { ___,   ___,   false, true,  false, false, false, ___   }  // 7
  };
  // l\r       NIL    UND    NaN    MAX    MIN    INF    -INF   NUM
  private static boolean[][] compare_LE = {
  /* NIL */  { true,  true,  false, true,  false, true,  false, ___   }, // 0
  /* UND */  { true,  true,  false, true,  false, true,  false, ___   }, // 1
  /* NaN */  { false, false, false, false, false, false, false, false }, // 2
  /* MAX */  { false, false, false, true,  false, true,  false, false }, // 3
  /* MIN */  { false, false, false, true,  true,  true,  false, false }, // 4
  /* INF */  { false, false, false, false, false, true,  false, false }, // 5
  /*-INF */  { true,  true,  false, true,  true,  true,  true,  true  }, // 6
  /* NUM */  { ___,   ___,   false, true,  false, false, false, ___   }  // 7
  };
  // l\r       NIL    UND    NaN    MAX    MIN    INF    -INF   NUM
  private static boolean[][] compare_GT = {
  /* NIL */  { false, false, false, false, true,  false, false, ___   }, // 0
  /* UND */  { false, false, false, false, true,  false, false, ___   }, // 1
  /* NaN */  { false, false, false, false, false, false, false, false }, // 2
  /* MAX */  { true,  true,  false, false, true,  true,  true,  true  }, // 3
  /* MIN */  { true,  true,  false, false, false, true,  true,  true  }, // 4
  /* INF */  { true,  true,  false, true,  true,  false, true,  true  }, // 5
  /*-INF */  { false, false, false, false, false, false, false, false }, // 6
  /* NUM */  { ___,   ___,   false, false, true,  false, true,  ___   }  // 7
  };
  // l\r       NIL    UND    NaN    MAX    MIN    INF    -INF   NUM
  private static boolean[][] compare_GE = {
  /* NIL */  { true,  true,  false, false, true,  false, false, ___   }, // 0
  /* UND */  { true,  true,  false, false, true,  false, false, ___   }, // 1
  /* NaN */  { false, false, false, false, false, false, false, false }, // 2
  /* MAX */  { true,  true,  false, true,  true,  true,  true,  true  }, // 3
  /* MIN */  { true,  true,  false, true,  true,  true,  true,  true  }, // 4
  /* INF */  { true,  true,  false, true,  true,  true,  true,  true  }, // 5
  /*-INF */  { false, false, false, false, false, false, true,  false }, // 6
  /* NUM */  { ___,   ___,   false, false, true,  false, true,  ___   }  // 7
  };

  /**
   * This method must be called with at least one of lhs or rhs is a ValueSpecial.
   */
  public static boolean compare(int op, Variable lhs, Variable rhs) throws Throwable {
    int lidx, ridx;
    if      (lhs == NIL)               lidx = 0;
    else if (lhs == UNDEFINED)         lidx = 1;
    else if (lhs == NaN)               lidx = 2;
    else if (lhs == MAX_NUMBER)        lidx = 3;
    else if (lhs == MIN_NUMBER)        lidx = 4;
    else if (lhs == POSITIVE_INFINITY) lidx = 5;
    else if (lhs == NEGATIVE_INFINITY) lidx = 6;
    else if (lhs.isNumber())           lidx = 7;
    else                               lidx = 2;
    if      (rhs == NIL)               ridx = 0;
    else if (rhs == UNDEFINED)         ridx = 1;
    else if (rhs == NaN)               ridx = 2;
    else if (rhs == MAX_NUMBER)        ridx = 3;
    else if (rhs == MIN_NUMBER)        ridx = 4;
    else if (rhs == POSITIVE_INFINITY) ridx = 5;
    else if (rhs == NEGATIVE_INFINITY) ridx = 6;
    else if (rhs.isNumber())           ridx = 7;
    else                               ridx = 2;

    if (op==OP_EQ || op==OP_NE)
      return op == ((lhs == rhs || lidx <= 1 && ridx <= 1) ? OP_EQ : OP_NE);

    boolean [][] barr = null;
    switch(op) {
    case OP_GT: barr = compare_GT; break;
    case OP_GE: barr = compare_GE; break;
    case OP_LT: barr = compare_LT; break;
    case OP_LE: barr = compare_LE; break;
    }

    double lval=0, rval=0;
    if (lidx == 7) {
      lval = lhs.getDoubleValue();
      if (ridx <= 1) {
        rval = 0.0;
        barr = null;
      } else if (ridx == 7) {
        rval = rhs.getDoubleValue();
        barr = null;
      }
    } else if (ridx == 7) {
      rval = rhs.getDoubleValue();
      if (lidx <= 1) {
        lval = 0.0;
        barr = null;
      } else if (lidx == 7) {
        lval = rhs.getDoubleValue();
        barr = null;
      }
    }

    if (barr == null) {
      switch(op) {
      case OP_GT: return lval >  rval;
      case OP_GE: return lval >= rval;
      case OP_LT: return lval <  rval;
      case OP_LE: return lval <= rval;
      }
    }

    return barr[lidx][ridx];
  }
    
  public final void dump(XMLWriter out) { out.simpleSingleTag("ValueSpecial"); }

} // end of class ValueSpecial.
