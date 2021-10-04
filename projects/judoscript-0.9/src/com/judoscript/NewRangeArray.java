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

import java.util.Stack;
import com.judoscript.bio._Array;
import com.judoscript.util.XMLWriter;


/**
 *@see com.judoscript.ObjectInstance
 */
public class NewRangeArray extends ExprNewBase
{
  Expr left;
  Expr right;

  public NewRangeArray(Expr right, Expr left) {
    super(-1);
    this.left = left;
    this.right = right;
  }

  public Variable eval() throws Throwable {
    // TODO: Also support 'a' .. 'z' ?
    long lval = left.getLongValue();
    long rval = right.getLongValue();

    _Array ar = new _Array();
    if (lval >= rval) {
      for (; lval >= rval; --lval)
        ar.append(ConstInt.getInt(lval));
    } else {
      for (; lval < rval; ++lval)
        ar.append(ConstInt.getInt(lval));
    }
    return ar;
  }

  public int getType() { return TYPE_ARRAY; }
  public boolean isArray() { return true; }

  public Expr reduce(Stack stack) { return this; }

  public void dump(XMLWriter out) {
    out.openTag("NewRangeArray");
    left.dump(out);
    right.dump(out);
    out.endTag();
  }

} // end of class NewRangeArray.
