/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 03-17-2002  JH   Initial open source release.
 * 08-12-2002  JH   Removed the statement-wide curStmt for multi-threadedness.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import com.judoscript.bio.JavaObject;
import com.judoscript.util.XMLWriter;
import com.judoscript.util.XMLDumpable;


public class StmtSwitch extends BlockSimple implements StmtBreakable
{
  static final class Case implements XMLDumpable
  {
    Expr caseValue;
    int index;

    Case(Expr expr, int idx) { caseValue = expr; index = idx; }
    public void dump(XMLWriter out) {
      out.simpleTag("Case");
      caseValue.dump(out);
      out.endTagLn();
    }
  }

  Expr expr;
  int defaultIndex = -1;
  //private boolean labelsReady = false;

  public StmtSwitch(int lineNum, Expr expr) {
    super(lineNum,null);
    this.expr = expr;
  }

  public void setDefaultIndex(int idx) { defaultIndex = idx; }
  public void addCase(Expr value, int idx) { 
    // we use the Block.labels differently here.
    if (labels == null) labels = new HashMap();
    Case cas = new Case(value,idx);
    labels.put(cas,cas);
  }
  public void setStmts(ArrayList stmtv) {
    Stmt[] stmts = new Stmt[stmtv.size()];
    for (int i=stmtv.size()-1; i>=0; --i)
      stmts[i] = (Stmt)stmtv.get(i);
    super.setStmts(stmts,labels); // keep the existing labels.
  }

  public void exec() throws Throwable { execHere(); }

  protected int beginBlock() throws Throwable {
    String s;
    int idx = defaultIndex;
    if (labels != null) {
      Iterator keys = labels.keySet().iterator();
      while (keys.hasNext()) {
        Case cas = (Case)keys.next();
        boolean match = false;
        Variable lhs_val = expr.eval();
        Variable rhs_val = cas.caseValue.eval();
        if (lhs_val instanceof JavaObject) {
          try { match = lhs_val.getObjectValue().equals(rhs_val.getObjectValue()); }
          catch(Exception e) {}
        } else {
          boolean lhs_num = lhs_val.isNumber();
          if (!lhs_num) lhs_num = JudoUtil.isDate(lhs_val);
          boolean rhs_num = rhs_val.isNumber();
          if (!rhs_num) rhs_num = JudoUtil.isDate(rhs_val);
          if (lhs_num && rhs_num) {
            match = (lhs_val.getDoubleValue() == rhs_val.getDoubleValue());
          } else {
            try { match = lhs_val.getStringValue().equals(rhs_val.getStringValue()); }
            catch(Exception e) {}
          }
        }

        if (match) {
          idx = cas.index;
          break;
        }
      }
    }

    return idx;
  }

  public void dump(XMLWriter out) {
    out.simpleTagLn("StmtSwitch");
    expr.dump(out);
    out.println();
    super.dump(out);
    out.endTagLn();
  }

} // end of class StmtSwitch.
