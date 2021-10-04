/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 03-17-2002  JH   Initial open source release.
 * 08-10-2002  JH   Fixed a bug: null should be used for a parameter if no value.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript;

import java.util.Map;
import java.util.HashMap;
import com.judoscript.bio._Array;
import com.judoscript.util.XMLWriter;


public class FunctionUser extends Block implements Function
{
  String name;
  boolean isMethod;
  int beginLine;
  String[] params;
  HashMap annotation;
  Expr[] defVals;
  int paramLen;

  public FunctionUser(int beginLine, int endLine, String name, boolean isMethod, String[] params,
            Expr[] defVals, Stmt[] stmts, HashMap labels)
  {
    this(beginLine, endLine, name, isMethod, params, defVals, stmts, labels, null);
  }
  public FunctionUser(int beginLine, int endLine, String name, boolean isMethod, String[] params,
            Expr[] defVals, Stmt[] stmts, HashMap labels, HashMap annotation)
  {
    this(beginLine, endLine, name, isMethod, params, defVals, new BlockSimple(stmts,labels), annotation);
  }
  public FunctionUser(int beginLine, int endLine, String name, boolean isMethod,
            String[] params, Expr[] defVals, Stmt block)
  {
    this(beginLine, endLine, name, isMethod, params, defVals, block, null);
  }
  public FunctionUser(int beginLine, int endLine, String name, boolean isMethod,
            String[] params, Expr[] defVals, Stmt block, HashMap annotation)
  {
    super(endLine, block);
    this.beginLine = beginLine;
    this.name = name;
    this.isMethod = isMethod;
    this.params = params;
    this.annotation = annotation;
    this.defVals = defVals;
    paramLen = (params==null) ? 0 : params.length;
    if ((paramLen > 0) && "..".equals(params[paramLen-1]))
      --paramLen;
  }

  public Map getAnnotation() {
  	if (annotation == null)
  		annotation = new HashMap();
  	return annotation;
  }

  public String getName() { return name; }
  public void   setName(String name) { this.name = name; }

  protected int beginBlock() throws Throwable {
    // set the parameters as local variables and let go!
    Variable[] args = RT.retrieveFunctionArguments();
    int len = (args==null) ? 0 : args.length;
    int deflen = (defVals==null) ? 0 : defVals.length;
    int i;
    for (i=0; i<paramLen; ++i) {
      if (i<len) {
        RT.setLocalVariable(params[i], args[i], args[i].getJavaPrimitiveType());
      } else if ((i<deflen) && (defVals[i]!=null)) { // consult the default values
        Variable v = defVals[i].eval();
        RT.setLocalVariable(params[i], v, v.getJavaPrimitiveType());
      } else {
        RT.setLocalVariable(params[i], ValueSpecial.NIL, 0);
      }
    }

    if (len > paramLen) { // Put arguments beyond declared parameters into $$args
      _Array ar = new _Array();
      for (i=paramLen; i<len; ++i)
        ar.append(args[i]);
      RT.setLocalVariable(ARGS_NAME, ar, 0);
    }

    RT.setLocalVariable(CONTEXT_NAME, (FrameBlock)RT.peekFrame(), 0);
    RT.setLocalVariable(ANNOTATION_NAME, JudoUtil.toVariable(annotation), 0);

    return 0;
  }

  public static final class FunctionFrame extends FrameBlock
  {
    boolean isFunc;
    FunctionFrame(boolean isFunc) throws Exception {
      super((String)null);
      this.isFunc = isFunc;
    }
    public boolean isTerminal() { return true; }
    public boolean isFunction() { return isFunc; }
  }

  public void pushNewFrame() throws Throwable {
    RT.pushFrame(new FunctionFrame(!isMethod), (Stmt[])null);
  }

  public Variable invoke(Expr[] args, int[] javaTypes) throws Throwable {
    RT.setFunctionArguments(args);
    try { exec(); }
    catch(ExceptionControl ce) {
    	if (ce.isReturn())
    		return ce.getReturnValue();
      ExceptionRuntime.rte(RTERR_ILLEGAL_JUMP, null, ce);
    }
    return ValueSpecial.NIL;
  }

  public void dump(XMLWriter out) {
    out.openTag(getTagName());
    out.tagAttr("name",name);
    out.closeTagLn();
    int len = (params==null) ? 0 : params.length;
    if (len > 0) {
      out.simpleTag("Arguments");
      for (int i=0; i<len; i++) {
        if (i>0) out.print(',');
        out.print(params[i]);
        if ((defVals != null) && (defVals.length >= i) && (defVals[i] != null)) {
          out.print('=');
          defVals[i].dump(out);
        }
      }
      out.endTagLn();
    }
    super.dump(out);
    out.endTagLn();
  }

  public String getTagName() { return "FunctionUser"; }

} // end of class FunctionUser.
