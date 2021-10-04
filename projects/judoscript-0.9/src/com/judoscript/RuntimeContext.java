/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 03-17-2002  JH   Initial open source release.
 * 08-08-2002  JH   Added get/setCurrentDefaultNS() to support namespace.
 *                  Feature implementation postponed. See judo.jj.
 * 07-20-2004  JH   Use text I/O only.
 * 07-23-2004  JH   Variable names can be anything, such as
 *                  including dots. If a variable is not resolved
 *                  in the context, it is attempted against the
 *                  system properties. This is very useful for
 *                  Ant tasks (within Ant build.xml).
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript;

import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;
import org.apache.commons.lang.StringUtils;
import com.judoscript.bio.*;
import com.judoscript.gui.*;
import com.judoscript.util.*;

public abstract class RuntimeContext implements Consts
{
  public void setAssertion(boolean set) { getGlobalContext().setAssertion(set); }
  public Script getScript() { return getGlobalContext().getScript(); }
  public FrameRoot getRootFrame() { return getGlobalContext().getRootFrame(); }
  public void setConst(String n, Variable v) throws Exception { getGlobalContext().setConst(n,v); }
  public BufferedReader getIn() { return getGlobalContext().getIn(); }
  public LinePrintWriter getOut() { return getGlobalContext().getOut(); }
  public LinePrintWriter getErr() { return getGlobalContext().getErr(); }
  public LinePrintWriter getLog() { return getGlobalContext().getLog(); }
  public void setIn(BufferedReader is) { getGlobalContext().setIn(is); }
  public void setOut(LinePrintWriter ow) { getGlobalContext().setOut(ow); }
  public void setErr(LinePrintWriter ow) { getGlobalContext().setErr(ow); }
  public void setLog(LinePrintWriter ow) { getGlobalContext().setLog(ow); }
  public BufferedReader getPipeIn() { return getGlobalContext().getPipeIn(); }
  public LinePrintWriter getPipeOut() { return getGlobalContext().getPipeOut(); }
  public void setPipeIn(BufferedReader is) { getGlobalContext().setPipeIn(is); }
  public void setPipeOut(LinePrintWriter os) { getGlobalContext().setPipeOut(os); }
  public void clearPipeIn() { getGlobalContext().clearPipeIn(); }
  public void clearPipeOut() { getGlobalContext().clearPipeOut(); }
  public void echoOn(String filename) throws Exception { getGlobalContext().echoOn(filename); }
  public void echoOff() { getGlobalContext().echoOff(); }
  public void echo(String msg) throws Exception { getGlobalContext().echo(msg); }
//  public SendMail getSendMail() { return getGlobalContext().getSendMail(); }
//  public void setSendMail(SendMail sm) { getGlobalContext().setSendMail(sm); }
  public RegexEngine getRegexCompiler() throws Exception { return getGlobalContext().getRegexCompiler(); }
  public Object getAntFacade() throws Exception { return getGlobalContext().getAntFacade(); }
  public String getCharset() { return getGlobalContext().getCharset(); }
  public void setCharset(String cset) { getGlobalContext().setCharset(cset); }
  public void setCurrentDefaultNS(String ns) { getGlobalContext().setCurrentDefaultNS(ns); }
  public String getCurrentDefaultNS() { return getGlobalContext().getCurrentDefaultNS(); }
  public Variable getEnvVars() throws Exception { return getGlobalContext().getEnvVars(); }
  public String getEnvVar(String name) { return getGlobalContext().getEnvVar(name); }
  public RuntimeSubContext newSubContext() { return getGlobalContext().newSubContext(); }

  public abstract RuntimeGlobalContext getGlobalContext();

  public boolean ignoreUnfoundMethods() { return false; } // TODO
  public boolean ignoreAssertions()     { return false; } // TODO

  ////////////////////////////////////////////////////////////////////
  // GUI event handling

  public abstract GuiContext getGuiContext();
  public abstract GuiListenerBase getGuiHandler(String clsName) throws ExceptionRuntime;
  public final GuiListenerBase getGuiEventHandler(String eventName) throws ExceptionRuntime {
    return getGuiHandler(GuiListenerCollection.getListenerImplClass(eventName));
  }
  public final AwtSwingListeners getGuiDefaultHandler() {
    try { return (AwtSwingListeners)getGuiHandler("com.judoscript.gui.AwtSwingListeners"); }
    catch(ExceptionRuntime rte) { RT.logger.error("(Should never happen. But...)", rte); }
    return null;
  }

  ////////////////////////////////////////////////////////////////////
  // essential runtime context

  Stack  frameStack;
  Frame  whichFrame;  // set by resolveVariable(), which is called first by setVariable().
  Stack  thisStack;   // used by UserDefined method calls.
  Stack  callStack;   // used by user-defined function/method calls.
  String contextName;

  // expression engine
  Stack exprStack;
  Stack exprMarkStack;

  // current statement -- when error occurs, report its line number
  IntStack stmtLineStack;

  // loop index stack
  int loopIndices[] = new int[20]; // no more than 20-fold of loops.
  int loopIndexTop = -1;

  // current TableData
  ExprTableData tdSrc = null;

  ////////////////////////////////////////////////////////////////////
  // Run-time environment
  //

  public RuntimeContext() {
    whichFrame = null;
    frameStack = new Stack();
    thisStack = new Stack();
    callStack = new Stack();
    exprStack = new Stack();
    exprMarkStack = new Stack();
    stmtLineStack = new IntStack();
    contextName = "ctx_" + Lib.createID();
  }

  public String getContextName() { return contextName; }

  public final Frame currentFrame() {
    return frameStack.isEmpty() ? getRootFrame() : (Frame)frameStack.peek();
  }

  public final void pushFrame(Frame frm, Stmt[] inits) throws Throwable {
    frameStack.push(frm);
    execStmts(inits);
  }

  public final void pushFrame(Frame frm, List inits) throws Throwable {
    frameStack.push(frm);
    execStmts(inits);
  }

  public final Frame popFrame() {
    Frame frm = (Frame)frameStack.pop();
    if (!(frm instanceof FrameRoot) && !(frm instanceof UserDefined))
      frm.clearVariables();
    return frm;
  }

  public final Frame peekFrame() {
    return (Frame)frameStack.peek();
  }

  public final void pushThis(Variable _this) { thisStack.push(_this); }
  public final void popThis() { try { thisStack.pop(); } catch(Exception e) {} }
  public final Variable getThisObject() {
    try { return (Variable)thisStack.peek(); } catch(Exception e) {}
    return null; // shouldn't happen.
  }

  public final int getLineNumber() {
    try {
      return stmtLineStack.peek();
    } catch(Exception e) {
      return -1;
    }
  }

  public final IntStack getLineStack() { return stmtLineStack; }
  
  public final String getSrcFileName() {
    return null; // TODO
  }

  public void execStmt(Stmt stmt) throws Throwable {
    try {
      stmtLineStack.push(stmt.getLineNumber());
      stmt.exec();
    } finally {
      stmtLineStack.pop();
    }
  }
  public final void execStmts(Stmt[] stmts) throws Throwable {
    int len = (stmts == null) ? 0 : stmts.length;
    for (int i=0; i<len; i++)
      execStmt(stmts[i]);
  }
  public final void execStmts(List stmts) throws Throwable {
    int len = (stmts == null) ? 0 : stmts.size();
    for (int i=0; i<len; i++)
      execStmt((Stmt)stmts.get(i));
  }

  public void setTableDataSource(ExprTableData etd) { tdSrc = etd; }
  public ExprTableData getTableDataSource() { return tdSrc; }
  public void clearTableDataSource() { tdSrc = null; }

  ///////////////////////////////////////////////////////////////////
  // The run-time expression engine
  //

  public final Variable tempVarAt(int idx) throws ExceptionRuntime {
    try {
      return (Variable)exprStack.elementAt(((Integer)exprMarkStack.peek()).intValue() + idx);
    } catch(Exception e) {
      String msg = e.getMessage();
      if (StringUtils.isBlank(msg))
        msg = e.getClass().getName() + ": index=" + idx;
      ExceptionRuntime.rte(RTERR_INTERNAL_ERROR, msg);
    }
    return null;
  }

  public final void pushExprStack(Expr expr) { exprStack.push(expr); }
  public final void markExprStack() { exprMarkStack.push(new Integer(exprStack.size())); }
  public final void resetExprStack() {
    exprStack.setSize(((Integer)exprMarkStack.pop()).intValue());
  }

  // Used by function/method calls.
  public final void setFunctionArguments(Expr[] _args) throws Throwable {
    CallFrame cf = new CallFrame();
    if (_args != null)
      cf.args = calcValues(_args, false);
    callStack.push(cf);
  }
  public final Variable[] retrieveFunctionArguments() {
    try {
      CallFrame cf = (CallFrame)callStack.peek();
      Variable[] x = cf.args;
      cf.args = null;
      return x;
    } catch(Exception e) {}
    return null;
  }
  
/*
  public final void setFunctionReturnValue(Variable var) {
    try {
      CallFrame cf = (CallFrame)callStack.peek();
      cf.retVal = var;
    } catch(Exception e) {
    }
  }
  public final Variable retrieveFunctionReturnValue() {
    try {
      CallFrame cf = (CallFrame)callStack.pop();
      Variable ret = cf.retVal;
      cf.clear();
      return ret;
    } catch(Exception e) {
      return null;
    }
  }
*/

  public void close() {
    frameStack = null;
    whichFrame = null;
    thisStack = null;
    callStack = null;
    contextName = null;
    exprStack = null;
    exprMarkStack = null;
    stmtLineStack = null;
    tdSrc = null;
  }

  ///////////////////////////////////////////////////////////////////
  // The run-time variable resolving
  //

  public Variable resolveVariable(String name) throws Throwable {
    if (name.startsWith("#"))
      return getScript().resolveConst(name);

    if (name.startsWith(LOCAL_NAME)) {
      if (name.equals(LOCALTEXT_NAME))
        return JudoUtil.toVariable(getScript().getLocalData());
      else
        return JudoUtil.toVariable(getScript().getLocalDataSource());
    }

    int stkPtr = frameStack.size()-1;
    if (stkPtr < 0) {
      whichFrame = getRootFrame();
      return whichFrame.resolveVariable(name);
    }
    for (; stkPtr>=0; stkPtr--) {
      whichFrame = (Frame)frameStack.elementAt(stkPtr);
      if (whichFrame.hasVariable(name))
        return whichFrame.resolveVariable(name);
      if (whichFrame.isLocal(name))
        return ValueSpecial.UNDEFINED;
      if (whichFrame.isTerminal()) {
        if (whichFrame.isFunction()) {
          whichFrame = getRootFrame();
          if (whichFrame.hasVariable(name))
            return whichFrame.resolveVariable(name);
        }
        whichFrame = currentFrame();
        break;
      }
    }

    Object o = getGlobalContext().getSystemProperties().get(name);
    return o!=null ? JudoUtil.toVariable(o) : ValueSpecial.UNDEFINED;
  }

  public final void setLocalVariable(String name) throws Throwable {
    currentFrame().setLocal(name);
  }

  public final Variable setLocalVariable(String name, Variable val, int type) throws Throwable {
    currentFrame().setLocal(name);
    return currentFrame().setVariable(name,val,type);
  }

  public final Variable setVariable(String name, Variable val, int type) throws Throwable {
//    val = val.cloneValue();
    Variable var = resolveVariable(name);
    if (JudoUtil.isNil(var)) {
      try {
        ObjectInstance _this = (ObjectInstance)getThisObject();
        if (_this != null && _this.hasVariable(name)) {
          _this.setVariable(name,val,type);
          return val;
        }
      } catch(Exception e) {}
      whichFrame.setLocal(name);
      whichFrame.setVariable(name,val,type);
    } else {
      int oldType = var.getType();
      int newType = val.getType();
      if ((oldType == newType) && ExprAnyBase.isValue(oldType) && (var instanceof ValueBase)) {
        if (ExprAnyBase.isInt(oldType))
          ((ValueBase)var).setLongValue(val.getLongValue());
        else if (ExprAnyBase.isDouble(oldType))
          ((ValueBase)var).setDoubleValue(val.getDoubleValue());
        else // string
          ((ValueBase)var).setStringValue(val.getStringValue());
      } else {
        whichFrame.setVariable(name,val,type);
      }
    }
    return val;
  }

  public void setLocal(String name) { currentFrame().setLocal(name); }

  public final void removeVariable(String name) throws Throwable {
    if (resolveVariable(name) != ValueSpecial.UNDEFINED) return;
    whichFrame.removeVariable(name);
  }

  private SimpleDateFormat defDateFmt = new SimpleDateFormat();
  public final SimpleDateFormat getDefaultDateFormat() { return defDateFmt; }
  public final void setDefaultDateFormat(String fmt) { defDateFmt.applyPattern(fmt); }

  ///////////////////////////////////////////////////////////////////
  // Helpers
  //

  // filename can be absolute, relative or starts with "~/" for home directory.
  public final File getFile(String filename) { return getFile(null,filename); }

  public final File getFile(String base, String filename) {
    if (StringUtils.isBlank(filename)) return new File(StringUtils.isBlank(base)?RT.getCurrentDir().toString():base);
    if (filename.startsWith("~/")) filename = Lib.getHomeDir()+File.separator+filename.substring(2);
    if (Lib.isAbsolutePath(filename)) return new File(filename);
    return new File((base==null) ? RT.getCurrentDir().toString() : base,filename);
  }

  public final String getFilePath(String filename) { return getFilePath(null,filename); }

  public final String getFilePath(String base, String filename) {
    try {
      return getFile(base,Lib.getPath(filename)).getCanonicalPath().replace('\\','/');
    } catch(IOException ioe) { return null; }
  }

  public Variable[] calcValues(Expr[] vals) throws Throwable { return calcValues(vals, false); }

  public Variable[] calcValues(Expr[] vals, boolean expand) throws Throwable {
    if (vals == null) return null;
    int len = vals.length;
    Variable[] va;
    if (expand) {
      ArrayList v = new ArrayList();
      int i;
      for (i=0; i<len; ++i) {
        Variable var = vals[i].eval();
        if (var instanceof _Array) {
          List list = ((_Array)var).getStorage();
          for (int j=0; j<list.size(); ++j)
            v.add(list.get(j));
        } else {
          v.add(var);
        }
      }
      va = new Variable[v.size()];
      for (i=0; i<va.length; ++i)
        va[i] = (Variable)v.get(i);
    } else {
      va = new Variable[len];
      for (int i=0; i<len; i++)
        va[i] = vals[i].eval();
    }
    return va;
  }

  public String[] calcValuesAsStrings(Expr[] vals, boolean expand) throws Throwable {
    if (vals == null) return null;
    int len = vals.length;
    String[] sa;
    if (expand) {
      ArrayList v = new ArrayList();
      int i;
      for (i=0; i<len; ++i) {
        Variable var = vals[i].eval();
        if (var instanceof _Array) {
          List list = ((_Array)var).getStorage();
          for (int j=0; j<list.size(); ++j)
            v.add(((Variable)list.get(j)).getStringValue());
        } else {
          v.add(var.getStringValue());
        }
      }
      sa = new String[v.size()];
      for (i=0; i<sa.length; ++i)
        sa[i] = (String)v.get(i);
    } else {
      sa = new String[len];
      for (int i=0; i<len; i++)
        sa[i] = vals[i].getStringValue();
    }
    return sa;
  }

  public void runStmts(Stmt[] stmts) throws Throwable {
    int len = (stmts==null) ? 0 : stmts.length;
    for (int i=0; i<len; i++)
      execStmt(stmts[i]);
  }

  /**
   * @param level is 0 for current, -1 for the enclosing loop, -2 ...
   */
  public int curLoopIndex(int level) {
    try {
      return loopIndices[loopIndexTop+level];
    } catch(ArrayIndexOutOfBoundsException aoobe) {
      return -1;
    }
  }

  public void incLoopIndex() {
    try { ++loopIndices[loopIndexTop]; } catch(Exception e) {}
  }

  public void pushLoopIndex() throws ExceptionRuntime {
    if (loopIndexTop < 0)
      loopIndexTop = 0;
    else
      ++loopIndexTop;
    if (loopIndexTop >= loopIndices.length)
      ExceptionRuntime.rte(RTERR_ILLEGAL_STATEMENT, "loop depth beyond limit of " + loopIndices.length);
    loopIndices[loopIndexTop] = -1;
  }

  public void popLoopIndex() { --loopIndexTop; }

  static class CallFrame
  {
    Variable[] args = null;
    Variable retVal = ValueSpecial.UNDEFINED;

    void clear() { args = null; retVal = null; }
  }

} // end of class RuntimeContext.
