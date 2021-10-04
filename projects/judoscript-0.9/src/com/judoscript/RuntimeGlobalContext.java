/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 03-17-2002  JH   Initial open source release.
 * 08-08-2002  JH   Implemented get/setCurrentDefaultNS().
 * 07-20-2004  JH   Use text I/O only.
 * 07-23-2004  JH   Added system properties, and absorb environment
 *                  variables and ${~}, ${.}, ${:} and ${/} as
 *                  global variables.
 * 07-30-2004  JH   Added ant task support.
 * 08-06-2004  JH   Added classpath support.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript;

import java.io.*;
import java.util.*;
import org.apache.commons.lang.StringUtils;
import com.judoscript.bio.*;
import com.judoscript.gui.*;
import com.judoscript.util.*;

/**
 * This is the one and only global context for a run.
 * Use cases of runs include:
 *
 * - Run a script file
 * - Run from the console
 * - Run from the GUI workbench
 * - Run from eval/evalSeparate/evalFile/evalFileSeparate
 * - Run from JudoEngine
 * - Run from BSFJudoEngine
 * - Run from RemoteJudoEngine
 * - Run from JuspServlet
 *
 */
public class RuntimeGlobalContext extends RuntimeContext
{
  public static final SystemInput  SysIn  = new SystemInput(System.in);
  public static final SystemOutput SysOut = new SystemOutput(System.out);
  public static final SystemOutput SysErr = new SystemOutput(System.err);

  HashMap singletons = new HashMap();
  static final String KEY_DEBUGGER      = "debugger";         // => Debugger
  static final String KEY_PIPEIN        = "pipeIn";           // => BufferedReader
  static final String KEY_PIPEOUT       = "pipeOut";          // => LinePrintWriter
  static final String KEY_GUICONTEXT    = "guiContext";       // => GuiContext
  static final String KEY_ECHO          = "echo";             // => String | Boolean.TRUE
  static final String KEY_JAVACOMPILER  = "javaCompiler";     // => String
  static final String KEY_REGEXCOMPILER = "regex";            // => RegExEngine
  static final String KEY_CHARSET       = "charset";          // => String (default charset)
  static final String KEY_ENVVARS       = "envvars";          // => JavaObject(Properties)
  static final String KEY_ANTFACADE     = "antfacade";        // => AntFacade

  Thread mainThread;
  Stack dirStack = null;
  final UserClasspath userCP = new UserClasspath();

  // In/output streams are needed for external program redirections;
  // In/output reader/writers are used for text readLine() and print.
  // Originals are needed to allow programs temporarily direct output to a user file.

  private LinePrintWriter    origLog;

/*
  private OutputStream   origOutStream;
  private OutputStream   origErrStream;
  private InputStream    origInStream;
*/

  private LinePrintWriter origOut;
  private LinePrintWriter origErr;
  private BufferedReader  origIn;

/*
  private OutputStream   outStream;
  private OutputStream   errStream;
  private InputStream    inStream;
*/

  private BufferedReader  in;
  private LinePrintWriter out;
  private LinePrintWriter err;
  private LinePrintWriter log;

  // static objects -- the scripts and therein
  private Script script;

  private long timestamp;
  private FrameRoot root;
  private Map sysProps = null;

  private int undefinedAccessPolicy = ISSUE_LEVEL_IGNORE;
  private int assertAsPolicy = ISSUE_LEVEL_WARN;

  ////////////////////////////////////////////////////////////////////
  // Run-time environment
  //

  public RuntimeGlobalContext() { this((String[])null, null); }

  public RuntimeGlobalContext(String[] progArgs, Script script) {
    this(progArgs, script, null, null, null, null);
  }

  public RuntimeGlobalContext(Expr[] progArgs, Script script) throws Throwable {
    this((String[])null, script, null, null, null, null);
    _setConst("#args", JudoUtil.toArray(calcValues(progArgs, false)));
  }

//  public RuntimeGlobalContext(String[] progArgs, Script script, OutputStream os,
//                              OutputStream es, PrintWriter logw, InputStream is)
  public RuntimeGlobalContext(String[] progArgs, Script script, LinePrintWriter os,
                              LinePrintWriter es, LinePrintWriter logw, BufferedReader is)
  {
    mainThread = Thread.currentThread();
    timestamp = System.currentTimeMillis();
    this.script = script;

//    origOutStream = (os==null) ? System.out : os;
//    origErrStream = (es==null) ? System.err : es;
//    origInStream  = (is==null) ? System.in  : is;
    origOut = os==null ? SysOut : os;
    origErr = es==null ? SysErr : es;
    origIn  = is==null ? SysIn  : is;
/*
    if (logw == null)
      origLog = origErr;
    else
      origLog = logw;
*/

//    outStream = origOutStream;
//    errStream = origErrStream;
//    inStream  = origInStream;
    out       = origOut;
    err       = origErr;
    in        = origIn;

    root = new FrameRoot();
    try { pushFrame(root, (Stmt[])null); } catch(Throwable e) {} // shouldn't happen.
    whichFrame = root;

    root.setVariable(DEFAULT_CONNECTION_NAME, ValueSpecial.NIL, 0); // placeholder
    root.setVariable(SYS_NAME, Script.sys, 0);

    this.sysProps = System.getProperties();
    getSystemVariables(); // environment variables and system properties.

    setArguments(progArgs, null);
  }

  public Thread getMainThread() { return mainThread; }

  public void setScript(Script _script) {
    if (_script != null) {
      script = _script;
      _setConst("#script", JudoUtil.toVariable(script.getScriptPath()));
    }
  }
  
  public void pushd() throws Throwable {
    if (dirStack == null)
      dirStack = new Stack();
    dirStack.push(RT.getCurrentDir().getStringValue());
  }

  public void popd() throws Throwable {
    if (dirStack != null && !dirStack.isEmpty())
      RT.setCurrentDir((String)dirStack.pop());
  }

  public void setArguments(String[] progArgs, Map sysprops) {
    if (sysprops != null)
      this.sysProps = sysprops;
    if (this.sysProps == null)
      this.sysProps = System.getProperties();

    _setConst("#classpath", JudoUtil.toVariable(userCP));

    Variable v = JudoUtil.toVariable(this.sysProps);
    _setConst("#systemProperties", v);
    _setConst("#sysprops", v);

    _Array cargs = new _Array();
    _Array args = new _Array();
    OrderedMap _options = new OrderedMap();
    try { _options.init(this,null); } catch(Throwable e) {}

    // command-line parameters and options
    try {
      int numArgs = 0;
      int len = (progArgs == null) ? 0 : progArgs.length;

      for (int i=0; i<len; i++) {
        String s = progArgs[i];
        if (StringUtils.isBlank(s)) {
        	++numArgs;
        	args.append(ValueSpecial.NIL);
        	cargs.append(ValueSpecial.NIL);
        	continue;
        }
        cargs.append(JudoUtil.toVariable(s));
        char ch = s.charAt(0);
        if (ch != '-') {
          ++numArgs;
          args.append(JudoUtil.toVariable(s));
        } else {
          if (s.startsWith("-?") || s.startsWith("--help"))
            script.displayUsage(); // exits the system.

          String name;
          Object value;
          int idx = s.indexOf('=');
          if (idx < 0)
            idx = s.indexOf(':');
          if (idx <= 0) {
            name = s.substring(1);
            value = ConstInt.TRUE;
          } else {
            name = s.substring(1,idx);
            value = s.substring(idx+1);
          }
          if (!_options.hasVariable(name)) {
            _options.setVariable(name, JudoUtil.toVariable(value), 0);
          } else {
            Object o = _options.get(name);
            if (o == ConstInt.TRUE) {
              _options.setVariable(name, JudoUtil.toVariable(value), 0);
            } else if (o instanceof Object[]) {
              Object[] oa = (Object[])o;
              Object[] oa1 = new Object[oa.length+1];
              System.arraycopy(oa, 0, oa1, 0, oa.length);
              oa1[oa.length] = value;
              _options.setVariable(name, JudoUtil.toVariable(oa1), 0);
            }
          }
        }
      }

      _setConst("#options", _options);
      _setConst("#cmd_args", cargs);
      _setConst("#args", args);

      if (script != null)
        script.checkMinArgs(numArgs); // may exit the system.

    } catch(Throwable e) {
      RT.logger.error("Failed to set arguments.", e);
    }
  }

  public Map getSystemProperties() { return sysProps; }

  /**
   * Get all environment variables and system properties as global variables.
   * Property names can contain dots.
   */
  public void getSystemVariables() {
    // set all env vars
    Map map = Lib.getEnvVars();
    Iterator iter = map.keySet().iterator();
    while (iter.hasNext()) {
      String name = (String)iter.next();
      root.setVariable(name, JudoUtil.toVariable(map.get(name)), 0);
    }

    // set special variables
    root.setVariable(":", JudoUtil.toVariable(File.pathSeparator), 0);
    root.setVariable("/", JudoUtil.toVariable(File.separator), 0);

    String s = Lib.getHomeDir();
    if (Lib.isWindows())
      s = s.replace('\\','/');
    root.setVariable("~", JudoUtil.toVariable(s), 0);

    s = Lib.getCurrentDir();
    if (Lib.isWindows())
      s = s.replace('\\','/');
    root.setVariable(".", JudoUtil.toVariable(s), 0);
  }

  public Script getScript() { return script; }
  public FrameRoot getRootFrame() { return root; }
  public RuntimeGlobalContext getGlobalContext() { return this; }
  public RuntimeSubContext newSubContext() { return new RuntimeSubContext(this); }

  public void setUndefinedAccessPolicy(int policy) { undefinedAccessPolicy = policy; }
  public int getUndefinedAccessPolicy() { return undefinedAccessPolicy; }

  public void setAssertAs(int policy) { assertAsPolicy = policy; }
  public int getAssertAs() { return assertAsPolicy; }

  ///////////////////////////////////////////////////////////////////
  // The run-time expression engine
  //

  public File getTempJavaDir() { return new File(JudoUtil.getTempDir(), "__emb__" + timestamp); }

  public String getJavaCompiler() {
    return StringUtils.defaultString((String)singletons.get(KEY_JAVACOMPILER),"javac");
  }

  public Variable getEnvVars() {
    Variable v = (Variable)singletons.get(KEY_ENVVARS);
    if (v == null) {
      try {
        v = JudoUtil.toVariable(Lib.getEnvVars());
        singletons.put(KEY_ENVVARS,v);
      } catch(Exception e) { v = ValueSpecial.UNDEFINED; }
    }
    return v;
  }

  public String getEnvVar(String name) {
    return name.equals(".") ? RT.getCurrentDir().toString() : Lib.getEnvVar(name);
  }

  public void close() {
    super.close();

    root.close();
    root = null;
    origLog = null;
    origOut = null;
    origErr = null;
    origIn = null;
    out = null;
    err = null;
    in = null;
    script = null;
  }

  public void setConst(String name, Variable val) throws Exception {
    if (!val.isNil())
      getScript().setConst(name,val);
  }

  void _setConst(String name, Variable val) {
    if (script != null)
      script._setConst(name,val);
  }

  ///////////////////////////////////////////////////////////////////
  // Output redirection
  //

  public BufferedReader  getIn()  { return in; }
  public LinePrintWriter getOut() { return out; }
  public LinePrintWriter getErr() { return err; }
  public LinePrintWriter getLog() { return null/*log*/; }

  public void setIn(BufferedReader is) {
    if (is==null) {
      in = origIn;
    } else {
      in = is;
    }
  }

  public void setOut(LinePrintWriter ow) {
    if (ow==null)
      out = origOut;
    else
      out = ow;
  }

  public void setErr(LinePrintWriter ow) {
    if (ow==null)
      err = origErr;
    else
      err = ow;
  }

  public void setLog(LinePrintWriter ow) {
    if (ow==null)
      log = origLog;
    else
      log = ow;
    java.sql.DriverManager.setLogWriter(log);
  }

  public Object getSingleton(String name) { return singletons.get(name); }
  public void setSingleton(String name, Object val) { singletons.put(name, val); }
  public void removeSingleton(String name) { singletons.remove(name); }

  public BufferedReader getPipeIn() { return (BufferedReader)singletons.get(KEY_PIPEIN); }
  public LinePrintWriter getPipeOut() { return (LinePrintWriter)singletons.get(KEY_PIPEOUT); }

  public void setPipeIn(BufferedReader is) {
    singletons.put(KEY_PIPEIN, is);
  }

  public void setPipeOut(LinePrintWriter os) {
    singletons.put(KEY_PIPEOUT, os);
  }

  public void clearPipeIn() {
    try { getPipeIn().close(); } catch(Exception e) {}
    singletons.remove(KEY_PIPEIN);
  }

  public void clearPipeOut() {
    try { getPipeOut().close(); } catch(Exception e) {}
    singletons.remove(KEY_PIPEOUT);
  }

  public RegexEngine getRegexCompiler() throws ExceptionRuntime {
    RegexEngine re = (RegexEngine)singletons.get(KEY_REGEXCOMPILER);
    if (re == null) {
      try {
        re = (RegexEngine)RT.getSysClass("com.judoscript.jdk14.RegexCompiler").newInstance();
        singletons.put(KEY_REGEXCOMPILER, re);
      } catch(Exception e) {
        ExceptionRuntime.rte(RTERR_ENVIRONMENT_ERROR, "Regex is only supported for JDK1.4 and up.");
      }
    }
    return re;
  }

  public Object getAntFacade() throws ExceptionRuntime {
    AntFacade ant = (AntFacade)singletons.get(KEY_ANTFACADE);
    if (ant == null) {
      try {
        userCP.getClass("org.apache.tools.ant.Project");
      } catch(Exception cnfe) {
        ExceptionRuntime.rte(RTERR_ENVIRONMENT_ERROR, "Ant is not available (in the CLASSPATH).");
      }
      try {
        ant = new AntFacade(getOut(), getErr());
        singletons.put(KEY_ANTFACADE, ant);
      } catch(Exception e) {
        ExceptionRuntime.rte(RTERR_ENVIRONMENT_ERROR, "Ant is not available (in the CLASSPATH).");
      }
    }
    return ant;
  }

  public UserClasspath getClasspath() { return userCP; }

  public String getCharset() { return (String)singletons.get(KEY_CHARSET); }
  public void setCharset(String cset) {
    if (StringUtils.isBlank(cset)) singletons.remove(KEY_CHARSET);
    else singletons.put(KEY_CHARSET,cset);
  }

  Stack ns_stack = new Stack();

  public void setCurrentDefaultNS(String namespace) {
    if (StringUtils.isNotBlank(namespace))
      ns_stack.push(namespace);
    else {
      try { ns_stack.pop(); }
      catch(Exception e) {}
    }
  }

  public String getCurrentDefaultNS() {
    if (ns_stack.isEmpty())
      return "";
    return (String)ns_stack.peek();
  }

  ///////////////////////////////////////////////////////////////////
  // Debugger support
  //
/*
  public void setDebugger(Debugger debugger) {
    singletons.put(KEY_DEBUGGER, debugger);
    setOut(debugger.getOutStream(), debugger.getOutWriter());
    setErr(debugger.getErrStream(), debugger.getErrWriter());
    setLog(debugger.getLogWriter());
  }

  public void debuggerSetBP() {
    try { ((Debugger)singletons.get(KEY_DEBUGGER)).setBP(); }
    catch(Exception e) {}
  }
  public void debuggerClearDisplay() {
    try { ((Debugger)singletons.get(KEY_DEBUGGER)).clearDisplay(); }
    catch(Exception e) {}
  }
  public void debuggerAddWatch(boolean toAdd, String name) {
    try { ((Debugger)singletons.get(KEY_DEBUGGER)).setWatch(toAdd,name); }
    catch(Exception e) {}
  }
  public void debuggerPrintStatus(String msg) {
    try { ((Debugger)singletons.get(KEY_DEBUGGER)).printStatus(msg); }
    catch(Exception e) {}
  }
  public void debuggerSetShow(int type, boolean enabled) {
    try { ((Debugger)singletons.get(KEY_DEBUGGER)).setShow(type, enabled); }
    catch(Exception e) {}
  }
  public void debuggerIgnoreUserStreams(boolean enabled) {
    try { ((Debugger)singletons.get(KEY_DEBUGGER)).ignoreUserStreams(enabled); }
    catch(Exception e) {}
  }
*/

  public void echoOn(String filename) throws Exception {
    echoOff();
    if (filename == null)
      singletons.put(KEY_ECHO,Boolean.TRUE);
    else
      singletons.put(KEY_ECHO, new LinePrintWriter(new FileWriter(filename)));
  }

  public void echoOff() {
    Object o = singletons.get(KEY_ECHO);
    if (o == null)
      return;
    if (o instanceof PrintWriter)
      ((PrintWriter)o).close();
    singletons.remove(KEY_ECHO);
  }

  public void echo(String msg) {
    Object o = singletons.get(KEY_ECHO);
    if (o == null)
      return;
    PrintWriter pw = (o instanceof PrintWriter) ? (PrintWriter)o : out;
    pw.println(msg.trim());
    pw.flush();
  }

  ////////////////////////////////////////////////////////////////////
  // GUI event handling

  public GuiListenerBase getGuiHandler(String clsName) throws ExceptionRuntime {
    GuiListenerBase x = (GuiListenerBase)singletons.get(clsName);
    if (x == null) {
      try {
        x = (GuiListenerBase)RT.getSysClass(clsName).newInstance();
        x.setGuiContext(getGuiContext());
        singletons.put(clsName,x);
      } catch(Exception e) {
        ExceptionRuntime.rte(RTERR_ILLEGAL_ARGUMENTS, "Gui handler class '"+clsName+"' is not found.",e);
      }
    }
    return x;
  }

  public GuiContext getGuiContext() {
    GuiContext guiCtxt = (GuiContext)singletons.get(KEY_GUICONTEXT);
    if (guiCtxt == null) {
      guiCtxt = new GuiContext(this);
      singletons.put(KEY_GUICONTEXT, guiCtxt);
    }
    return guiCtxt;
  }

  public static final ThreadGroup JudoScriptThreadGroup = new ThreadGroup("JudoScript Threads");

} // end of class RuntimeGlobalContext.
