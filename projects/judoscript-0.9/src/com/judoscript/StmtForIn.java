/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 09-05-2002  JH   Incepted.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript;

import java.util.*;
import com.judoscript.util.RangeIterator;
import com.judoscript.util.XMLWriter;


public class StmtForIn extends StmtForFrom
{
  static final String forInVar = TEMPVAR_PREFIX + "forIn";

  Expr collection;

  public StmtForIn(int lineNo, String label, String varName,
                   Expr col, Expr from, Expr to, Expr step, boolean backward)
  {
    this(lineNo, label, varName, col, from, to, step, false, backward);
  }

  public StmtForIn(int lineNo, String label, String varName,
                   Expr col, Expr from, Expr to, Expr step, boolean upto, boolean backward)
  {
    super(lineNo, label, varName, from, to, step, upto, backward);
    this.collection = col;
  }

  public void pushNewFrame() throws Throwable {
    RuntimeContext rtc = RT.curCtxt();
    try { rtc.pushFrame(new FrameBlock(label),(Stmt[])null); } catch(ExceptionControl ce) {}

    int start = this.from==null ? 0  : (int)this.from.getLongValue();
    int end   = this.to  ==null ? -1 : (int)this.to.getLongValue();
    int step  = this.step==null ? 1  : (int)this.step.getLongValue();
    Variable col = collection.eval();
    Iterator iter = (col instanceof ExprCollective)
                    ? ((ExprCollective)col).getIterator(start, end, step, upto, backward)
                    : RangeIterator.getIterator(col.getObjectValue(), start, end, step, upto);
    rtc.setLocalVariable(forInVar, JudoUtil.toVariable(iter), TYPE_JAVA);
  }

  protected int beginBlock() throws Throwable {
    RuntimeContext rtc = RT.curCtxt();
    Iterator iter = (Iterator)rtc.resolveVariable(forInVar).getObjectValue();
    if (!iter.hasNext()) {
      rtc.setLocalVariable(varName, ValueSpecial.UNDEFINED, TYPE_UNDEFINED);
      return -1;
    }
    Variable v = JudoUtil.toVariable(iter.next());
    rtc.setLocalVariable(varName, v, v.getJavaPrimitiveType());
    return 0;
  }

  public void execHere() throws Throwable {
    super.execHere();

    try { // to call the close() method of the InputLines object.
      Object o = RT.resolveVariable(varName).getObjectValue();
      if (o instanceof ExprLines.LinesIterator)
        ((ExprLines.LinesIterator)o).close();
    } catch(Exception e) {}
  }

  public void dump(XMLWriter out) {
    out.simpleTagLn("StmtForIn");
    // TODO
    out.endTagLn();
  }

} // end of class StmtForIn.
