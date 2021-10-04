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

import java.util.*;
import com.judoscript.bio.*;
import com.judoscript.parser.helper.*;
import com.judoscript.util.XMLWriter;


public class ExprAssign extends StmtExpr
{
  int  op;
  Expr rhs;
  boolean isLocal;
  boolean isVar;
  boolean isRoot;

  protected ExprAssign(int line, boolean isLocal, boolean isVar, Expr lhs, int oprtor, Expr rhs)
  {
    super(line, lhs);
    op = oprtor;
    this.rhs = rhs;
    this.isLocal = isLocal;
    this.isVar = isVar;
    isRoot = false;
  }

  public ExprAssign(int line, Expr lhs, int oprtor, Expr rhs) {
    this(line, false, false, lhs, oprtor, rhs);
  }

  public ExprAssign(int line, Expr lhs, Expr rhs) {
    this(line, false, false, lhs, OP_ASSIGN, rhs);
  }

  public ExprAssign(int line, String name, Expr rhs) {
    this(line, false, false, new AccessVar(name), OP_ASSIGN, rhs);
  }

  public Variable eval() throws Throwable {
    int _op = op & 0x0FF;

    if (_op == OP_COPY) {
      Variable l = expr.eval();
      Variable r = rhs.eval();
      boolean leftMap  = (l instanceof UserDefined) || (l.getObjectValue() instanceof Map);
      boolean rightMap = (r instanceof UserDefined) || (r.getObjectValue() instanceof Map);
      if (l.isNil())
      	l = rightMap ? (Variable)new UserDefined() : new _Array();

      if (!(l instanceof ExprCollective))
        ExceptionRuntime.rte(RTERR_ILLEGAL_ARGUMENTS, "Left-hand side of copy operator := is not a collection.");
      if (!(r instanceof ExprCollective))
        ExceptionRuntime.rte(RTERR_ILLEGAL_ARGUMENTS, "Value side of copy operator := is not a collection.");
      if (leftMap && !rightMap)
        ExceptionRuntime.rte(RTERR_ILLEGAL_ARGUMENTS, "Can't copy an array or set into an Object (or map).");
      if (!leftMap && rightMap)
        ExceptionRuntime.rte(RTERR_ILLEGAL_ARGUMENTS, "Can't copy an Object (or map) into an array or set.");

      Iterator iter = ((ExprCollective)r).getIterator(0, 1000000, 1, false, false);
      if (leftMap) {
        if (l instanceof UserDefined) { // either UserDefined
          ((UserDefined)l).copy(r);
        } else {
          try {
            Map map = (Map)((JavaObject)l).getObjectValue(); // or java.util.Map
            UserDefined.copyToMap(map, r);
          } catch(ClassCastException cce) {
            ExceptionRuntime.rte(RTERR_ILLEGAL_ARGUMENTS, "Can't copy into this object.");
          }
        }
      } else { // collection copy
        ExprCollective _l = (ExprCollective)l;
        while (iter.hasNext()) {
          Variable v = JudoUtil.toVariable(iter.next());
          _l.addVariable(v, 0);
        }
      }
      return null;
    }

    ExprLValue lval = (ExprLValue)expr;
    Variable res = rhs.eval();
    int type = op >> 8;
    if (isRoot) {
      RT.getRootFrame().setVariable(lval.getName(), res, type);
      return res.cloneValue();
    }

    if (isLocal)
      RT.currentFrame().setLocal(lval.getName());
    // TODO: handle isVar case.
    switch (_op) {
    case OP_ASSIGN:
      return lval.setVariable(res, type);
    case OP_LOGIC_AND_ASSIGN:
    case OP_LOGIC_OR_ASSIGN:
      return lval.setVariable(ConstInt.getBool(
                               (_op == OP_LOGIC_AND_ASSIGN)
                               ? lval.getBoolValue() && res.getBoolValue()
                               : lval.getBoolValue() || res.getBoolValue()
                              ), 0);
    default:
      if (res.isNumber() && lval.eval().isNumber())
        return lval.selfNumericOp(_op, res.getDoubleValue(), type);
      if (_op != OP_PLUS_ASSIGN)
        return ValueSpecial.NaN;
      // otherwise, '+' is the same as '@'.
    case OP_CONCAT_ASSIGN:
      return lval.selfStringOp(res.getStringValue(), type);
    }
  }

  public final void exec() throws Throwable { eval(); }

  public final boolean getBoolValue() throws Throwable   { return eval().getBoolValue(); }
  public final long    getLongValue() throws Throwable   { return eval().getLongValue(); }
  public final double  getDoubleValue() throws Throwable { return eval().getDoubleValue(); }
  public final String  getStringValue() throws Throwable { return eval().getStringValue(); }

  public final int getType() { return rhs.getType(); }
  public final boolean isNil() { return rhs.isNil(); }

  public final Expr reduce(Stack stack) {
    if (expr instanceof ExprPrimary)
      expr = ((ExprPrimary)expr).leftReduce(stack);
    rhs = rhs.reduce(stack);
    return this;
  }

  public void dump(XMLWriter out) {
    out.openTag("ExprAssign");
    if (isLocal) out.tagAttr("local","true");
    out.closeTag();
    expr.dump(out);
    out.print(' ');
    out.print(getOpName(true,op));
    out.print(' ');
    rhs.dump(out);
    out.endTag();
  }

  public static String getOpName(boolean isMarkup, int op) {
    int type = op >> 8;
    String ret;
    switch(op) {
    case OP_ASSIGN:           ret = "="; break;
    case OP_CONCAT_ASSIGN:    ret = "@="; break;
    case OP_PLUS_ASSIGN:      ret = "+="; break;
    case OP_MINUS_ASSIGN:     ret = "-="; break;
    case OP_MUL_ASSIGN:       ret = "*="; break;
    case OP_DIV_ASSIGN:       ret = "/="; break;
    case OP_MOD_ASSIGN:       ret = "%="; break;
    case OP_LSHIFT_ASSIGN:    ret = isMarkup ? "&lt;&lt;=" : "<<="; break;
    case OP_RSHIFT_ASSIGN:    ret = isMarkup ? "&gt;&gt;=" : ">>="; break;
    case OP_RUSHIFT_ASSIGN:   ret = isMarkup ? "&gt;&gt;&gt;=" : ">>>="; break;
    case OP_AND_ASSIGN:       ret = isMarkup ? "&amp;=" : "&="; break;
    case OP_OR_ASSIGN:        ret = "|="; break;
    case OP_XOR_ASSIGN:       ret = "^="; break;
    case OP_LOGIC_AND_ASSIGN: ret = isMarkup ? "&amp;&amp;=" : "&&="; break;
    case OP_LOGIC_OR_ASSIGN:  ret = "||="; break;
    default:                  ret = ""; break;
    }
    if (type != 0) ret += '(' + JudoUtil.getJavaPrimitiveTypeName(type) + ')';
    return ret;
  }

  public static ExprAssign createLocalVar(int line, Expr var, Expr rhs, boolean isVar) {
    return new ExprAssign(line, true, isVar, var, OP_ASSIGN, rhs);
  }
  public static ExprAssign createLocalVar(int line, Expr var, Expr rhs) {
    return new ExprAssign(line, true, false, var, OP_ASSIGN, rhs);
  }
  public static ExprAssign createLocalVar(int line, String name, Expr rhs, boolean isVar) {
    return new ExprAssign(line, true, isVar, new AccessVar(name), OP_ASSIGN, rhs);
  }
  public static ExprAssign createLocalVar(int line, String name, Expr rhs) {
    return new ExprAssign(line, true, false, new AccessVar(name), OP_ASSIGN, rhs);
  }
  public static ExprAssign createField(int line, String name, Expr rhs) {
    ExprPrimary ep = new ExprPrimary(new Object[]{ new AccessThis(), name });
    return new ExprAssign(line, false, false, ep, OP_ASSIGN, rhs);
  }

  public static ExprAssign createRootVar(int line, String name, Expr rhs) {
    ExprAssign ret = new ExprAssign(line, false, false, new AccessVar(name), OP_ASSIGN, rhs);
    ret.isRoot = true;
    return ret;
  }

} // end of class ExprAssign.
