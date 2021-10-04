/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 06-08-2003  JH   Inception.
 * 07-20-2004  JH   Use text I/O only.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript;

import java.io.*;
import java.util.*;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import org.apache.commons.logging.*;
import org.apache.commons.logging.impl.SimpleLog;
import org.apache.commons.logging.impl.Log4JLogger;
import org.apache.log4j.Logger; // shielded.
import org.apache.log4j.Level; // shielded.

import com.judoscript.gui.*;
import com.judoscript.db.DBConnect;
import com.judoscript.hibernate.HibernateEnv;
import com.judoscript.bio.UserDefined;
import com.judoscript.bio.JavaObject;
import com.judoscript.bio._Timer;
import com.judoscript.bio.JavaExtension;
import com.judoscript.util.Lib;
import com.judoscript.util.SendMail;
import com.judoscript.util.LinePrintWriter;


public final class RT
{
  /////////////////////////////////////////////////////////////////////////////
  // Loggers
  //
  // Name            Location                       Description
  // ==============  =============================  =========================
  // judo            RT.logger                      JudoScript general logger
  // judo.user       RT.userLogger                  JudoScript user logger
  // judo.jdbc       db/DBConnect.logger            JDBC logger
  // judo.hibernate  hibernate/HibernateEnv.logger  Hibernate logger
  // judo.schedule   bio/_Timer.logger              Schedule logger
  // judo.jusp       jusp/JuspServlet.logger        JUSP logger
  //

  public static final Log logger = LogFactory.getLog("judo");
  public static final Log userLogger = LogFactory.getLog("judo.user");
  public static final int loggerType;

  public static final int LOGGERTYPE_UNKNOWN   = 0;
  public static final int LOGGERTYPE_JDK14     = 1;
  public static final int LOGGERTYPE_LOG4J     = 2;
  public static final int LOGGERTYPE_SIMPLELOG = 3;

  public static String judoPath;
  
  static {
    String logClzName = logger.getClass().getName();
    logger.debug("Logger: " + logClzName);
    if (logClzName.equals("org.apache.commons.logging.impl.Jdk14Logger"))
      loggerType = LOGGERTYPE_JDK14;
    else if (logClzName.equals("org.apache.commons.logging.impl.Log4JLogger"))
      loggerType = LOGGERTYPE_LOG4J;
    else if (logClzName.equals("org.apache.commons.logging.impl.SimpleLog"))
      loggerType = LOGGERTYPE_SIMPLELOG;
    else
      loggerType = LOGGERTYPE_UNKNOWN;
    
    judoPath = Lib.getEnvVar("JUDOPATH");
  }

  public static String getJudoPath() { return judoPath; }
  
  // All this trouble is only because org.apache.commons.loggins.Log
  // does not have the setLevel() method.
  // Here, we implement that for JDK1.4, Log4j and SimpleLog.
  public static void setAllLoggerLevel(String level) {
    level = level.toLowerCase();

    // Collect all the loggers used so far.
    int len = 4;
    boolean hasHib = false;
    try {
      Class.forName("net.sf.hibernate.Session");
      hasHib = true;
    } catch(ClassNotFoundException cnfe) {
      try {
        Class.forName("org.hibernate.Session");
        hasHib = true;
      } catch(ClassNotFoundException cnfe1) {
      }
    }
    if (hasHib)
      ++len;

    Log[] all = new Log[len];
    all[0] = logger;
    all[1] = userLogger;
    all[2] = DBConnect.logger;
    all[3] = _Timer.logger;
    if (hasHib)
      all[4] = HibernateEnv.logger;

    int i;
    switch(loggerType) {
    case LOGGERTYPE_JDK14:
      try {
        Class jdk14 = getSysClass("com.judoscript.jdk14.JDK14Thingy");
        Method mthd = jdk14.getMethod("setAllLoggerLevel", new Class[]{ String.class, Object[].class });
        mthd.invoke(null, new Object[]{ level, all });
      } catch(Exception eee) {
        logger.warn("Failed to set logging level for " + logger.getClass().getName(), eee);
      }
      break;

    case LOGGERTYPE_LOG4J:
      setAllLog4jLevel(level, all);
      break;

    case LOGGERTYPE_SIMPLELOG:
      int _level;
      if (level.equals("all"))           _level = SimpleLog.LOG_LEVEL_ALL;
      else if (level.equals("off"))      _level = SimpleLog.LOG_LEVEL_OFF;
      else if (level.equals("debug"))    _level = SimpleLog.LOG_LEVEL_DEBUG;
      else if (level.equals("error"))    _level = SimpleLog.LOG_LEVEL_ERROR;
      else if (level.equals("fatal"))    _level = SimpleLog.LOG_LEVEL_FATAL;
      else if (level.equals("info"))     _level = SimpleLog.LOG_LEVEL_INFO;
      else if (level.equals("trace"))    _level = SimpleLog.LOG_LEVEL_TRACE;
      else if (level.startsWith("warn")) { _level = SimpleLog.LOG_LEVEL_WARN; level = "warn"; }
      else return; // ignore unknowns.

      for (i=all.length-1; i>=0; --i)
        ((SimpleLog)all[i]).setLevel(_level);
      break;

    default:
      logger.warn("Unknown logger: " + logger.getClass().getName() +
                     "; don't know how to set logging level to " + level + '.');
    }
  }

  public static void setLoggerLevel(String level, Log logger) {
    level = level.toLowerCase();

    switch(loggerType) {
    case LOGGERTYPE_JDK14:
      try {
        Class jdk14 = getSysClass("com.judoscript.jdk14.JDK14Thingy");
        Method mthd = jdk14.getMethod("setLoggerLevel", new Class[]{ String.class, Object.class });
        mthd.invoke(null, new Object[]{ level, logger });
      } catch(Exception eee) {
        logger.warn("Failed to set logging level for " + logger.getClass().getName(), eee);
      }
      break;

    case LOGGERTYPE_LOG4J:
      setLog4jLevel(level, logger);
      break;

    case LOGGERTYPE_SIMPLELOG:
      int _level;
      if (level.equals("all"))        _level = SimpleLog.LOG_LEVEL_ALL;
      else if (level.equals("off"))   _level = SimpleLog.LOG_LEVEL_OFF;
      else if (level.equals("debug")) _level = SimpleLog.LOG_LEVEL_DEBUG;
      else if (level.equals("error")) _level = SimpleLog.LOG_LEVEL_ERROR;
      else if (level.equals("fatal")) _level = SimpleLog.LOG_LEVEL_FATAL;
      else if (level.equals("info"))  _level = SimpleLog.LOG_LEVEL_INFO;
      else if (level.equals("trace")) _level = SimpleLog.LOG_LEVEL_TRACE;
      else if (level.equals("warn"))  _level = SimpleLog.LOG_LEVEL_WARN;
      else return; // ignore unknowns.

      ((SimpleLog)logger).setLevel(_level);
      break;

    default:
      logger.warn("Unknown logger: " + logger.getClass().getName() +
                  "; don't know how to set logging level to " + level + '.');
    }
  }

  // to shield log4j.
  static void setAllLog4jLevel(String level, Object[] all) {
    Log4jWrapper.setAllLog4jLevel(level, all);
  }

  // to shield log4j.
  static void setLog4jLevel(String level, Log logger) {
    Log4jWrapper.setAllLog4jLevel(level, new Object[]{ logger });
  }

  static class Log4jWrapper { // to shield Log4j.
    static void setAllLog4jLevel(String level, Object[] all) {
      Level _level;
      if (level.equals("all"))        _level = Level.ALL;
      else if (level.equals("off"))   _level = Level.OFF;
      else if (level.equals("debug")) _level = Level.DEBUG;
      else if (level.equals("error")) _level = Level.ERROR;
      else if (level.equals("fatal")) _level = Level.FATAL;
      else if (level.equals("info"))  _level = Level.INFO;
      else if (level.equals("trace")) _level = Level.DEBUG;
      else if (level.equals("warn"))  _level = Level.WARN;
      else return; // ignore unknowns.

      for (int i=all.length-1; i>=0; --i) {
        if (all[i] != null)
          ((Logger)(((Log4JLogger)all[i]).getLogger())).setLevel(_level);
      }
    }
  }

  //
  // Loggers.
  /////////////////////////////////////////////////////////////////////////////

  private static final RTData rtdata = new RTData();
  public static RuntimeGlobalContext DEFAULT_RTC = null;
  
  public static void pushContext(RuntimeContext rtc) {
  	rtdata.pushContext(rtc);
  }
  public static RuntimeContext popContext() {
  	RuntimeContext r = rtdata.popContext();
  	return r;
  }

  public static RuntimeContext curCtxt() {
    try {
      RuntimeContext rtc = rtdata.curCtxt();
      if (rtc != null)
        return rtc; // most of the time, if not always.
    } catch(Throwable th) {}
    logger.debug("Use default global context.");
    return DEFAULT_RTC;
  }

  // Convenience methods --

  public static RuntimeGlobalContext getGlobalContext() { return curCtxt().getGlobalContext(); }
  public static Frame currentFrame() { return curCtxt().currentFrame(); }
  public static String getContextName() { return curCtxt().getContextName(); }
  public static RuntimeSubContext newSubContext() { return curCtxt().newSubContext(); }
  public static FrameRoot getRootFrame() { return curCtxt().getRootFrame(); }
  public static Script getScript() { return curCtxt().getScript(); }
  public static boolean ignoreUnfoundMethods() { return curCtxt().ignoreUnfoundMethods(); }
  public static boolean ignoreAssertions()     { return curCtxt().ignoreAssertions(); }
  public static Variable setVariable(String name, Variable val, int type) throws Throwable {
    return curCtxt().setVariable(name, val, type);
  }
  public static Variable resolveVariable(String n) throws Throwable { return curCtxt().resolveVariable(n); }
  public static void removeVariable(String n) throws Throwable { curCtxt().removeVariable(n); }
  public static void setLocalVariable(String name) throws Throwable { curCtxt().setLocalVariable(name); }
  public static Variable setLocalVariable(String name, Variable val, int type) throws Throwable {
    return curCtxt().setLocalVariable(name, val, type);
  }
  public static Variable setGlobalVariable(String name, Variable val, int type) throws Exception {
    return getRootFrame().setVariable(name, val, type);
  }
  public static Variable setGlobalVariable(String name, Object val, int type) throws Exception {
    return getRootFrame().setVariable(name, JudoUtil.toVariable(val), type);
  }
  public static Variable resolveGlobalVariable(String name) throws Throwable {
    return getRootFrame().resolveVariable(name);
  }

  public static Variable call(String fxn, Expr[] args, int[] javaTypes) throws Throwable {
    return call(fxn, args, javaTypes, false);
  }

  public static Variable call(String fxn, Expr[] args, int[] javaTypes, boolean fxnNameOnly) throws Throwable
  {
    return call(fxn, args, javaTypes, fxnNameOnly, true);
  }

  public static Variable call(String fxn, Expr[] args, int[] javaTypes, boolean fxnNameOnly, boolean checkNS)
    throws Throwable
  {
    if (checkNS) {
      int idx = fxn.indexOf("::");
      if (idx > 0)
        return getNamespace(fxn.substring(0, idx)).invoke(fxn.substring(idx+2), args, javaTypes);
    }

    Variable _this = getThisObject();
    if (_this != null) {
      if (_this instanceof UserDefined) {
        if ( ((UserDefined)_this).hasMethod(fxn) )
          return _this.invoke(fxn, args, javaTypes);
        else if (!fxnNameOnly) {
          Variable v = ((UserDefined)_this).resolveVariable(fxn);
          if (v instanceof AccessFunction) {
            return _this.invoke(((AccessFunction)v).getName(), args, javaTypes);
          }
        }
      } else if (_this instanceof JavaExtension) {
        try { return _this.invoke(fxn, args, javaTypes); }
        catch(NoSuchMethodException nsme) {}
      }
    }

    if (!fxnNameOnly) {
      Variable v = resolveVariable(fxn);
      if (v instanceof AccessFunction)
        fxn = ((AccessFunction)v).getName();
    }

    // global or shortcut functions, either built-in or customer
    return getScript().invoke(fxn, args, javaTypes);
  }

  public static Map getSystemProperties() throws Exception { return getGlobalContext().getSystemProperties(); }
  public static void setConst(String n, Variable v) throws Exception { curCtxt().setConst(n,v); }
  public static LinePrintWriter getOut() { return curCtxt().getOut(); }
  public static LinePrintWriter getErr() { return curCtxt().getErr(); }
  public static LinePrintWriter getLog() { return curCtxt().getLog(); }
  public static BufferedReader getIn() { return curCtxt().getIn(); }
  public static void setOut(LinePrintWriter ow) { curCtxt().setOut(ow); }
  public static void setErr(LinePrintWriter ow) { curCtxt().setErr(ow); }
  public static void setLog(LinePrintWriter ow) { curCtxt().setLog(ow); }
  public static void setIn(BufferedReader is) { curCtxt().setIn(is); }
  public static BufferedReader getPipeIn() { return curCtxt().getPipeIn(); }
  public static LinePrintWriter getPipeOut() { return curCtxt().getPipeOut(); }
  public static void setPipeIn(BufferedReader is) { curCtxt().setPipeIn(is); }
  public static void setPipeOut(LinePrintWriter os) { curCtxt().setPipeOut(os); }
  public static void clearPipeIn() { curCtxt().clearPipeIn(); }
  public static void clearPipeOut() { curCtxt().clearPipeOut(); }
  public static SendMail getSendMail() {
    try {
      return (SendMail)resolveVariable(Consts.SENDMAIL_NAME).getObjectValue();
    } catch(Throwable e) {
      return null;
    }
  }
  public static void setSendMail(SendMail sm) {
    try {
      setLocalVariable(Consts.SENDMAIL_NAME, JudoUtil.toVariable(sm), 0);
    } catch(Throwable e) {}
  }

  public static void pushd() throws Throwable { getGlobalContext().pushd(); }
  public static void popd() throws Throwable { getGlobalContext().popd(); }
  public static void setCurrentDir(String s) throws Exception { setGlobalVariable(".", s, 0); }
  public static Variable getCurrentDir() {
    try { return resolveGlobalVariable("."); }
    catch(Throwable e) { return ValueSpecial.UNDEFINED; }
  }
  public static void echoOn(String filename) {
    try { curCtxt().echoOn(filename); } catch(Exception e) {}
  }
  public static void echoOff() { curCtxt().echoOff(); }
  public static void echo(String msg) throws Exception { curCtxt().echo(msg); }
  public static RegexEngine getRegexCompiler() throws Exception { return curCtxt().getRegexCompiler(); }
  public static Object getAntFacade() throws Exception { return curCtxt().getAntFacade(); }
  public static String getCharset() { return curCtxt().getCharset(); }
  public static void setCharset(String cset) { curCtxt().setCharset(cset); }
  public static void setCurrentDefaultNS(String ns) { curCtxt().setCurrentDefaultNS(ns); }
  public static String getCurrentDefaultNS() { return curCtxt().getCurrentDefaultNS(); }
  public static Variable getEnvVars() throws Exception { return curCtxt().getEnvVars(); }
  public static String getEnvVar(String name) { return curCtxt().getEnvVar(name); }
  public static File getFile(String filename) { return curCtxt().getFile(null, filename); }
  public static File getFile(String base, String fname) { return curCtxt().getFile(base, fname); }
  public static String getFilePath(String fname) { return curCtxt().getFilePath(null, fname); }
  public static String getFilePath(String base, String fname) { return curCtxt().getFilePath(base, fname); }
  public static Variable[] calcValues(Expr[] vals) throws Throwable { return curCtxt().calcValues(vals, false); }
  public static Variable[] calcValues(Expr[] vals, boolean expand) throws Throwable {
    return curCtxt().calcValues(vals, expand);
  }
  public static String[] calcValuesAsStrings(Expr[] vals, boolean expand) throws Throwable {
    return curCtxt().calcValuesAsStrings(vals, expand);
  }
  public static void runStmts(Stmt[] sa) throws Throwable { curCtxt().runStmts(sa); }
  public static void pushFrame(Frame frm, Stmt[] inits) throws Throwable { curCtxt().pushFrame(frm, inits); }
  public static void pushFrame(Frame frm, List inits) throws Throwable { curCtxt().pushFrame(frm, inits); }
  public static Frame popFrame() { return curCtxt().popFrame(); }
  public static Frame peekFrame() { return curCtxt().peekFrame(); }
  public static void pushThis(Variable _this) { curCtxt().pushThis(_this); }
  public static void popThis() { curCtxt().popThis(); }
  public static Variable getThisObject() { return curCtxt().getThisObject(); }
  public static int getLineNumber() { return curCtxt().getLineNumber(); }
  public static String getSrcFileName() { return curCtxt().getSrcFileName(); }
  public static void execStmt(Stmt stmt) throws Throwable { curCtxt().execStmt(stmt); }
  public static void execStmts(Stmt[] stmts) throws Throwable { curCtxt().execStmts(stmts); }
  public static void execStmts(List stmts) throws Throwable { curCtxt().execStmts(stmts); }
  public static void setTableDataSource(ExprTableData etd) { curCtxt().setTableDataSource(etd); }
  public static ExprTableData getTableDataSource() { return curCtxt().getTableDataSource(); }
  public static void clearTableDataSource() { curCtxt().clearTableDataSource(); }

  public static GuiContext getGuiContext() { return curCtxt().getGuiContext(); }
  public static GuiListenerBase getGuiHandler(String clsName) throws ExceptionRuntime {
    return curCtxt().getGuiHandler(clsName);
  }
  public static GuiListenerBase getGuiEventHandler(String eventName) throws ExceptionRuntime {
    return curCtxt().getGuiEventHandler(eventName);
  }
  public static AwtSwingListeners getGuiDefaultHandler() { return curCtxt().getGuiDefaultHandler(); }
  public static Variable tempVarAt(int i) throws ExceptionRuntime { return curCtxt().tempVarAt(i); }
  public static void pushExprStack(Expr expr) { curCtxt().pushExprStack(expr); }
  public static void markExprStack() { curCtxt().markExprStack(); }
  public static void resetExprStack() { curCtxt().resetExprStack(); }
  public static void setFunctionArguments(Expr[] _args) throws Throwable { curCtxt().setFunctionArguments(_args); }
  public static Variable[] retrieveFunctionArguments() { return curCtxt().retrieveFunctionArguments(); }
//  public static void setFunctionReturnValue(Variable var) { curCtxt().setFunctionReturnValue(var); }
//  public static Variable retrieveFunctionReturnValue() { return curCtxt().retrieveFunctionReturnValue(); }

  public static SimpleDateFormat getDefaultDateFormat() { return curCtxt().getDefaultDateFormat(); }
  public static void setDefaultDateFormat(String fmt) { curCtxt().setDefaultDateFormat(fmt); }

  public static int  curLoopIndex(int level) { return curCtxt().curLoopIndex(level); }
  public static void incLoopIndex()  { curCtxt().incLoopIndex();  }
  public static void pushLoopIndex() throws ExceptionRuntime { curCtxt().pushLoopIndex(); }
  public static void popLoopIndex()  { curCtxt().popLoopIndex();  }

  public static void setUndefinedAccessPolicy(int policy) { getGlobalContext().setUndefinedAccessPolicy(policy); }
  public static int getUndefinedAccessPolicy() { return getGlobalContext().getUndefinedAccessPolicy(); }
  
  public static void setAssertAs(int policy) { getGlobalContext().setAssertAs(policy); }
  public static int getAssertAs() { return getGlobalContext().getAssertAs(); }
  
  public static UserClasspath getClasspath() throws Exception { return getGlobalContext().getClasspath(); }
  public static Class getSysClass(String name) throws ClassNotFoundException { return Class.forName(name); }
  public static Class getClass(String name) throws Exception {
    RuntimeGlobalContext rgc = getGlobalContext();
    if (rgc != null)
      return rgc.getClasspath().getClass(name);
    return ((ParsingContext)curCtxt()).getClass(name);
  }

  public static boolean isMainThread() {
    try {
      return getGlobalContext().getMainThread().equals(Thread.currentThread());
    } catch(Exception e) {
      return false;
    }
  }

  /**
   * For now, only for Java class static member accesses.
   */
  public static Namespace getNamespace(String name) throws Exception {
    Namespace ns= (Namespace)namespaces.get(name);
    if (ns == null) {
      Class cls = getClass(name);
      ns = new JavaObject(cls);
      namespaces.put(cls.getName(), ns);
      namespaces.put(name, ns);
    }
    return ns;
  }

  private static HashMap namespaces = new HashMap();
  static {
    namespaces.put("db",   new Namespace.Adapter("db"));
    namespaces.put("mail", new Namespace.Adapter("mail"));
    namespaces.put("hib",  new Namespace.Adapter("hib"));
  }

} // end of public class RT.


final class RTData extends InheritableThreadLocal
{
  public void pushContext(RuntimeContext rtc) {
    Stack stack = (Stack)get();
    if (stack == null)
      set(stack = new Stack());
    stack.push(rtc);
  }
  public RuntimeContext popContext() {
    return (RuntimeContext)((Stack)get()).pop();
  }
  public RuntimeContext curCtxt() {
    return (RuntimeContext)((Stack)get()).peek();
  }

  /**
   * Make sure only the RuntimeGlobalContext is in the stack.
   * This overrides InheritableThreadLocal's method.
   */
  protected Object childValue(Object parentObject) {
    Stack ret = new Stack();
    Stack stack = (Stack)parentObject;
    if (stack.size() >= 1)
      ret.push(stack.elementAt(0));
    return ret;
  }

} // end of class RTData.
