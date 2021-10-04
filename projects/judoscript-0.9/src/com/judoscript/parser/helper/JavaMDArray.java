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


package com.judoscript.parser.helper;

import java.util.List;
import java.util.Stack;
import com.judoscript.*;
import com.judoscript.parser.ParseException;
import com.judoscript.util.XMLWriter;


public final class JavaMDArray extends ExprAnyBase implements Stmt
{
  private ParserHelper helper;
  Expr   clsName;
  int    mdim;
  List   inits0;
  int[]  dims;

  public JavaMDArray(int lineNo, ParserHelper helper, Expr clsName, int mdim, Object inits)
                    throws ParseException
  {
    setLineNumber(lineNo);
    this.helper = helper;
    this.clsName = clsName;
    this.mdim = mdim;
    this.inits0 = (List)inits;
    dims = new int[mdim];
    for (int i=0; i<mdim; ++i)
      dims[i] = 0;
    getDimInfo(inits0,0);
  }

  private void getDimInfo(List v, int level) throws ParseException {
    int sz = v.size();
    if (sz > dims[level])
      dims[level] = sz;
    for (int i=0; i<sz; ++i) {
      Object o = v.get(i);
      if (o instanceof List) {
        if (level+1 >= mdim)
          helper.throwParseException("Multi-dimensional array being initialized with " +
                                     "too many dimensions.", null);
        getDimInfo((List)o,level+1);
      } else if (mdim != level+1) {
        helper.throwParseException("Incomplete multi-dimensional Java array initialization.",
                                   "Only the last dimension may have incomplete initialziers.");
      }
    }
  }

  ////////////////////////////////////////
  // private data members to avoid excessive call
  // stack overhead for the recursive algorithm.
  private Stack reduce_stack = null;
  private int[] reduce_idcs = null;
  private Expr reduce_arrExpr = null;

  public Expr reduce(Stack stack) {
    // new the Java array
    int i;
    Expr[] ea = new Expr[mdim];
    for (i=mdim-1; i>=0; --i)
      ea[i] = ConstInt.getInt(dims[i]);
    stack.push( new NewJavaArray(clsName,ea) );
    reduce_arrExpr = new ExprReduced.TempVar(stack.size()-1);

    // initialize the Java array with the initializers
    reduce_stack = stack;
    reduce_idcs = new int[mdim];
    for (i=mdim-1; i>=0; --i)
      reduce_idcs[i] = 0;
    reduce_addInitializers(inits0,0);

    // return the created and initialized Java array
    return reduce_arrExpr; // reduce_arrExpr will replace 'this'.
  }

  private void reduce_addInitializers(List v, int level) {
    for (int i=0; i<v.size(); ++i) {
      reduce_idcs[level] = i;
      Object o = v.get(i);
      if (o instanceof List) {
        reduce_addInitializers((List)o,level+1);
      } else {
        Expr[] indices = new Expr[mdim];
        for (int j=0; j<mdim; ++j)
          indices[j] = ConstInt.getInt(reduce_idcs[j]);
        ExprLValue lval = new AccessMD(reduce_arrExpr,indices);
        reduce_stack.push( new ExprAssign(lineNum,lval,((Expr)o).reduce(reduce_stack)) );
      }
    }
  }

  public Variable eval() throws ExceptionRuntime, Throwable {
    ExceptionRuntime.rte(RTERR_INTERNAL_ERROR, "JavaMDArray should never appear at run-time.");
    return null;
  }

  public void dump(XMLWriter out) { // should not show up; nevertheless useful for debugging.
    out.simpleTag("JavaMDArray");
    out.tagAttr("mdim", ""+mdim);
    StringBuffer sb = new StringBuffer();
    for (int i=0; i<mdim; ++i) {
      if (i>0) sb.append(',');
      sb.append(dims[i]);
    }
    out.tagAttr("dims", sb.toString());
    out.closeTag();
    clsName.dump(out);
    out.endTag();
  }

  ///////////////////////////////////////
  // Stmt implementation
  //

  int lineNum;
  int fileIndex;

  public void setLineNumber(int line) { lineNum = line; }
  public int getLineNumber() { return lineNum; }

  public void setFileIndex(int findex) { fileIndex = findex; }
  public int getFileIndex() { return fileIndex; }

  public Stmt optimizeStmt() { return this; }
  public void pushNewFrame() {}
  public void popFrame() {}
  public void exec() throws ExceptionRuntime {
    ExceptionRuntime.rte(RTERR_INTERNAL_ERROR, "JavaMDArray should never appear at run-time.");
  }

} // end of class JavaMDArray.

