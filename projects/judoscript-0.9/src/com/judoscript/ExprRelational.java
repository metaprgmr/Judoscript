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

import com.judoscript.bio.JavaObject;
import com.judoscript.util.XMLWriter;


public class ExprRelational extends ExprMultiBase
{
  // ops: OP_EQ/NE/GT/LT/GE/LE

  public ExprRelational(Expr first, int[] ops, Expr[] rest) { super(first,ops,rest); }

  public int getType() { return TYPE_INT; }
  public int getJavaPrimitiveType() { return JAVA_BOOLEAN; }

  public Variable eval() throws Throwable {
    return ConstInt.getBool(getBoolValue());
  }

  public boolean getBoolValue() throws Throwable {

    boolean ret = true;
    Expr lhs = expr;
    Variable lhs_val = lhs.eval();
    boolean lhs_num = lhs_val.isNumber();
    if (!lhs_num) lhs_num = JudoUtil.isDate(lhs_val);
    String sl=null, sr=null;
    double dl=0, dr=0;
    boolean doNum = true;

    for (int i=0; i<parts.length; i++) {

      Expr rhs = parts[i];
      Variable rhs_val = rhs.eval();
      boolean result = false;

      if ((lhs_val instanceof ValueSpecial) || (rhs_val instanceof ValueSpecial)) {
        result = ValueSpecial.compare(ops[i], lhs_val, rhs_val);
      } else {
        boolean rhs_num = rhs_val.isNumber();
        if (!rhs_num)
          rhs_num = JudoUtil.isDate(rhs_val);

        if (lhs_num && rhs_num) {
          doNum = true;
          dl = lhs_val.getDoubleValue();
          dr = rhs_val.getDoubleValue();
        } else {
          doNum = false;
          sl = lhs_val.getStringValue();
          sr = rhs_val.getStringValue();
        }

        switch(ops[i]) {
        case OP_EQ: // this part is copied by StmtSwitch.
          if (lhs_val instanceof JavaObject) {
            try { result = lhs_val.getObjectValue().equals(rhs_val.getObjectValue()); }
            catch(Exception e) { ret = false; }
          } else if (doNum) {
            result = (dl == dr);
          } else {
            try { result = sl.equals(sr); } catch(Exception e) {}
          }
          break;
        case OP_NE:
          if (lhs_val instanceof JavaObject) {
            try { result = !lhs_val.getObjectValue().equals(rhs_val.getObjectValue()); }
            catch(Exception e) {}
          } else if (doNum) {
            result = (dl != dr);
          } else {
            try { result = !sl.equals(sr); } catch(Exception e) {}
          }
          break;
        case OP_GT:
          if (doNum) result = (dl > dr);
          else result = (sl.compareTo(sr) > 0);
          break;
        case OP_LT:
          if (doNum) result = (dl < dr);
          else result = (sl.compareTo(sr) < 0);
          break;
        case OP_GE:
          if (doNum) result = (dl >= dr);
          else result = (sl.compareTo(sr) >= 0);
          break;
        case OP_LE:
          if (doNum) result = (dl <= dr);
          else result = (sl.compareTo(sr) <= 0);
          break;
        default: result = false;
        }
        lhs_num = rhs_num;
      }

      ret &= result;
      lhs = rhs;
      lhs_val = rhs_val;
    }
    return ret;
  }

  public long   getLongValue() throws Throwable   { return getBoolValue() ? 1 : 0; }
  public double getDoubleValue() throws Throwable { return getBoolValue() ? 1 : 0; }
  public String getStringValue() throws Throwable { return getBoolValue() ? "1" : "0"; }

  public void dump(XMLWriter out) {
    out.simpleTag("ExprRelational");
    expr.dump(out);
    for (int i=0; i<parts.length; i++) {
      switch(ops[i]) {
      case OP_EQ: out.print(" == "); break;
      case OP_NE: out.print(" != "); break;
      case OP_GT: out.print(" &gt; "); break;
      case OP_LT: out.print(" &lt; "); break;
      case OP_GE: out.print(" &gt;= "); break;
      case OP_LE: out.print(" &lt;= "); break;
      }
      parts[i].dump(out);
    }
    out.endTag();
  }

} // end of class ExprRelational.
