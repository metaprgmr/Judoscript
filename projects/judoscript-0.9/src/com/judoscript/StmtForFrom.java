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

import com.judoscript.util.XMLWriter;


public class StmtForFrom extends BlockSimple implements StmtLoop
{
  Expr    from;
  Expr    to;
  Expr    step;
  boolean backward;
  boolean upto;
  String  label;
  String  varName;

  public StmtForFrom(int lineNo, String label, String varName,
                     Expr from, Expr to, Expr step, boolean backward)
  {
    this(lineNo, label, varName, from, to, step, false, backward);
  }

  public StmtForFrom(int lineNo, String label, String varName,
                     Expr from, Expr to, Expr step, boolean upto, boolean backward)
  {
    super(lineNo,null);
    this.label = label;
    this.varName = varName;
    this.from = from;
    this.to = to;
    this.step = step;
    this.upto = upto;
    this.backward = backward;
  }

  public final String getLabel() { return label; }

  public void pushNewFrame() throws Throwable {
    try { RT.pushFrame(new FrameBlock(label),(Stmt[])null); } catch(ExceptionControl ce) {}
  }

  protected int beginBlock() throws Throwable {
    if (!RT.currentFrame().hasVariable(varName)) { // first iteration
      Variable fromVar = (from==null) ? ConstInt.ZERO : from.eval();
      Variable toVar = to.eval();
      if (backward && (fromVar.getLongValue() < toVar.getLongValue()))
        return -1;
      if (!backward && (fromVar.getLongValue() > toVar.getLongValue()))
        return -1;
      RT.setLocalVariable(varName,fromVar,JAVA_INT);
      return 0;
    }
    // following iterations
    long stepValue = (step==null) ? 1 : step.getLongValue();
    if (stepValue < 0) stepValue = -stepValue;
    long curValue = RT.resolveVariable(varName).getLongValue();
    long toValue = to.getLongValue();
    if (backward) {
      curValue -= stepValue;
      if (upto) {
        if (curValue <= toValue)
          return -1;
      } else {
        if (curValue < toValue)
          return -1;
      }
    } else {
      curValue += stepValue;
      if (upto) {
        if (curValue >= toValue)
          return -1;
      } else {
        if (curValue > toValue)
          return -1;
      }
    }
    RT.setLocalVariable(varName, ConstInt.getInt(curValue), JAVA_INT);
    return 0;
  }

  protected boolean endBlock() throws Throwable {
    return true;
  }

  public void dump(XMLWriter out) {
    out.simpleTagLn("StmtForFrom");
    // TODO
    super.dump(out);
    out.endTagLn();
  }

} // end of class StmtForFrom.
