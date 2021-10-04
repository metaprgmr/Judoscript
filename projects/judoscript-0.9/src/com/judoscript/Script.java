/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 03-17-2002  JH   Initial open source release.
 * 08-04-2002  JH   Added createTreeOutput system function.
 * 08-18-2002  JH   Added treeNode to the registeredTypes.
 * 08-29-2002  JH   Cleared 'decisions' and 'constDefs', and make most
 *                  data members private.
 * 08-29-2002  JH   Use allPurposes HashMap for embedded Java, adapters,
 *                  GUI event handlers and the new SGML/HTML/XML named
 *                  handlers.
 * 12-06-2002  JH   Defined usage -- predefined fields: "minArgs", "args",
 *                  "desc", "nextLine", "author", "created", "prog" and
 *                  "lastMod". The first 3 are used here; "prog" and
 *                  "nextLine" are set by parser. "prog" is the same as
 *                  #prog; this is useful for documentation.
 * 12-14-2002  JH   Added "createActiveXComponent()".
 * 12-17-2002  JH   Uses properties files for Java static sysrem functions.
 * 01-03-2003  JH   Added #year predefined constant.
 * 02-06-2003  JH   Major changes to included files handling: included
 *                  files will be referenced rather than absorbed during
 *                  parsing. This is good for debugging (file name, line
 *                  number, usage object, etc.) and smaller memory footage
 *                  reentrant situations such as JUSP. The pre-compiled
 *                  scripts can also be considered reentrant. Script-wide
 *                  object references will have to relay to included
 *                  scripts. This may have minor performance cost. Included
 *                  scripts can be sharable or non-sharable. Non-sharable
 *                  included scripts are those with conditional includes
 *                  and/or conditional constant definitions, or their
 *                  included scripts that are non-sharable. This affects
 *                  more the reentrant cases.
 * 07-20-2004  JH   Use text I/O only.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript;

import java.io.*;
import java.util.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import org.apache.commons.lang.StringUtils;
import com.judoscript.bio.*;
import com.judoscript.gui.*;
import com.judoscript.parser.ParseException;
import com.judoscript.util.*;


/**
 * This represents a Judo script.
 */
public class Script extends Block
{
  public static final boolean verbose = false;

  static transient boolean doExit = true;

  ///////////////////////////////////////////////////////////
  // Data members
  //

  public ArrayList included = new ArrayList();
  public static Sys sys = null; 
  private String progName = null;
  private String scriptPath = null;
  private HashMap fxns = new HashMap(); // String => Function/_Thread
  private HashMap objectTypes = null;     // String => UserDefined.Type
  private HashMap registeredTypes = new HashMap();
  private HashMap constants = new HashMap();
  private long lastModified = 0;
  private String localDataSrc = null;
  private Stack localDataStack = null;
  private String cryptoClass = "com.judoscript.util.PBEWithMD5AndDES";

  private HashMap allPurposes = new HashMap();
  // different purpose contents are with keys prefixed with its own.
  private static final String PREFIX_STATIC_JAVA   = "_??$j";
//  private static final String PREFIX_EMBEDDED_JAVA = "_??Ej";
//  private static final String PREFIX_JAVA_ADAPTER  = "_??j@";
  private static final String PREFIX_GUI_HANDLER   = "_??gH";
  private static final String PREFIX_SGML_HANDLER  = "_??sH";
  private static final String PREFIX_XML_HANDLER   = "_??xH";

  public void acceptDecls(Script scr) {
    Iterator itor = scr.fxns.keySet().iterator();
    while (itor.hasNext()) {
      Object k = itor.next();
      if (!fxns.containsKey(k))
        fxns.put(k, scr.fxns.get(k));
    }
    if (scr.objectTypes != null) {
      if (objectTypes == null)
        objectTypes = new HashMap();
      itor = scr.objectTypes.keySet().iterator();
      while (itor.hasNext()) {
        Object k = itor.next();
        if (!objectTypes.containsKey(k))
          objectTypes.put(k, scr.objectTypes.get(k));
      }
    }
    if (scr.constants != null) {
      if (constants == null)
        constants = new HashMap();
      itor = scr.constants.keySet().iterator();
      while (itor.hasNext()) {
        Object k = itor.next();
        if (!constants.containsKey(k))
          constants.put(k, scr.constants.get(k));
      }
    }
  }

  public void setUsage(HashMap u) {
    try {
      if (progName != null)
        u.put("prog", progName);
      setConst("#usage", JudoUtil.toVariable(u));
    } catch(Exception e) {}
  }
  public HashMap getUsage() {
    Variable v = resolveConst("#usage");
    try {
      if (v != null && v instanceof JavaObject)
        return (HashMap)((JavaObject)v).getObjectValue();
    } catch(Throwable e) {}
    return null;
  }

  public String getCryptoClassName() { return cryptoClass; }
  public void setCryptoClassName(String cls) {
    cryptoClass = StringUtils.defaultString(cls, "com.judoscript.util.PBEWithMD5AndDES");
  }

  public void setLocalDataSource(String text) { localDataSrc = text; }
  public String getLocalData() { return localDataSrc; }
  public BufferedReader getLocalDataSource() {
    if (localDataSrc == null)
      return null;
    return new BufferedReader(new StringReader(localDataSrc));
  }

  // See takeDecls() and Sys.BIM_SYS_EVAL.
  public void addLocalDataSource(String text) {
    if (localDataSrc != null) {
      if (localDataStack == null)
        localDataStack = new Stack();
      localDataStack.push(localDataSrc);
    }
    localDataSrc = text;
  }
  public void restoreLocalDataSource() {
    if (localDataStack != null && !localDataStack.isEmpty())
      localDataSrc = (String)localDataStack.pop();
  }

  public void setLastModified(long lastMod) { lastModified = lastMod; }
  public long getLastModified() { return lastModified; }

  public String getProgramName() { return progName; }
  public void setProgramName(String name) {
    progName = name;
    try { setConst("#prog", name); } catch(Exception e) {}
  }

  public void setScriptPath(String path) {
    scriptPath = path;
  }
  public String getScriptPath() { return scriptPath; }

  public String getScriptBasePath() {
    try {
      String p = new File(scriptPath).getParentFile().getAbsolutePath().toString();
      if (Lib.isWindows())
        p = p.replace('\\', '/');
      return p;
    } catch(Exception e) { return null; }
  }

  public JavaObject getStaticJavaClass(String name) throws Exception {
    JavaObject jo = (JavaObject)allPurposes.get(PREFIX_STATIC_JAVA+name);
    if (jo == null) {
      jo = JavaObject.wrapClass(RT.getClass(name));
      allPurposes.put(PREFIX_STATIC_JAVA+name,jo);
    }
    return jo;
  }

  public void takeDecls(Script rhs) throws ExceptionRuntime {
    if (fxns == null)
      fxns = rhs.fxns;
    else if (rhs.fxns != null)
      fxns.putAll(rhs.fxns);

    if (objectTypes == null)
      objectTypes = rhs.objectTypes;
    else if (rhs.objectTypes != null)
      objectTypes.putAll(rhs.objectTypes);

    if (registeredTypes == null)
      registeredTypes = rhs.registeredTypes;
    else if (rhs.registeredTypes != null)
      registeredTypes.putAll(rhs.registeredTypes);

    if (rhs.localDataSrc != null)
      addLocalDataSource(rhs.localDataSrc);
  }

  ///////////////////////////////////////////////////////////
  // C'tor(s) and attribute set/get methods
  //

  public Script() {
    super();

    // pre-defined constants
    try {
      setConst("#versionInfo", VersionInfo.latest().toString());
      setConst("#versionID",   VersionInfo.latestID());
//      setConst("#pathSep",     SysFunLib.pathSeparator); // a.k.a. ${:}
//      setConst("#fileSep",     SysFunLib.fileSeparator); // a.k.a. ${/}
//      setConst("#home",        Lib.getHomeDir());        // a.k.a. ${~}
//      setConst("#here",        Lib.getCurrentDir());     // a.k.a. ${.}
//      setConst("#os",          Lib.osName());
      setConst("#user",        Lib.getUserName());
      setConst("#year",        ConstInt.getInt(Calendar.getInstance().get(Calendar.YEAR)));
    } catch(ParseException pe) {} // not happening.

    // pre-registered objects
    registeredTypes.put("date",       "com.judoscript.bio._Date");
    registeredTypes.put("Date",       "com.judoscript.bio._Date");
    registeredTypes.put("orderedMap", "com.judoscript.bio.OrderedMap");
    registeredTypes.put("OrderedMap", "com.judoscript.bio.OrderedMap");
    registeredTypes.put("sortedMap",  "com.judoscript.bio.SortedMap");
    registeredTypes.put("SortedMap",  "com.judoscript.bio.SortedMap");
    registeredTypes.put("array",      "com.judoscript.bio._Array");
    registeredTypes.put("Array",      "com.judoscript.bio._Array");
    registeredTypes.put("Set",        "com.judoscript.bio._Set");
    registeredTypes.put("LinkedList", "com.judoscript.bio._Array$_LinkedList");
    registeredTypes.put("tableData",  "com.judoscript.bio._TableData");
    registeredTypes.put("TableData",  "com.judoscript.bio._TableData");
    registeredTypes.put("stack",      "com.judoscript.util._Stack");
    registeredTypes.put("Stack",      "com.judoscript.util._Stack");
    registeredTypes.put("queue",      "com.judoscript.util.Queue");
    registeredTypes.put("Queue",      "com.judoscript.util.Queue");
    registeredTypes.put("db_connect", "com.judoscript.db.DBConnect");
    registeredTypes.put("db_handle",  "com.judoscript.db.DBHandle");
    registeredTypes.put("db_batch",   "com.judoscript.db.DBBatch");

    try {
      addObjectType(TreeNodeType.TYPE);
    } catch(ExceptionRuntime rte) {
      RT.logger.error("Internal: registering built-in data structures.");
    }
  }

  static final HashMap javaMethods = new HashMap();
  static void loadJavaStaticSystemFunctions(Properties p) {
    Iterator itor = p.keySet().iterator();
    while (itor.hasNext()) {
      String k = (String)itor.next();
      StringTokenizer st = new StringTokenizer((String)p.get(k), ":");
      String s1 = st.nextToken().trim();
      String s2 = st.nextToken().trim();
      javaMethods.put(k, new String[]{ s1, s2 });
      //javaMethods.put(k, new String[]{ st.nextToken().trim(), st.nextToken().trim() });
    }
  }
  static {
    Properties p;
    p = JudoUtil.loadProperties("jsysfxns", true);    // standard Java-static-method system functions
    if (p != null)
      loadJavaStaticSystemFunctions(p);
    p = JudoUtil.loadProperties("myjsysfxns", false); // custom Java-static-method system functions
    if (p != null)
      loadJavaStaticSystemFunctions(p);
  }

  public void registerType(String name, String className) {
    registeredTypes.put(name,className);
  }

  public String getRegisteredType(String name) {
    return (String)registeredTypes.get(name);
  }

  public void addFunction(Function f) throws ExceptionRuntime {
    addFunction(f, 0);
  }

  public static final int ADDFXN_IGNORE  = -1;
  public static final int ADDFXN_FAIL    = 0;
  public static final int ADDFXN_REPLACE = 1;
  
  public void addFunction(Function f, int mode) throws ExceptionRuntime {
    String name = f.getName();
    if (fxns.containsKey(name)) {
      switch(mode) {
      case ADDFXN_IGNORE:
        return;
      case ADDFXN_FAIL:
        ExceptionRuntime.rte(RTERR_FUNCTION_ALREADY_DEFINED,
                             "Function '" + name + "' already defined.");
      case ADDFXN_REPLACE:
        break;
      }
    }

    if (VariableAdapter.isShortcut(name)) {
      ExceptionRuntime.rte(RTERR_FUNCTION_ALREADY_DEFINED,
                           "Attempt to override system function '" + name + "'.");
    }
    fxns.put(name,f);
  }

  public Function getFunction(String name) throws Exception {
    if (name.startsWith("::"))
      name = name.substring(2);
    Function f = (Function)fxns.get(name);
    if (f == null) {
      String[] sa = (String[])javaMethods.get(name);
      if (sa != null) {
        f = new FunctionStaticJava(0, name, sa[0], false, sa[1], null);
        addFunction(f, -1);
      }
    }
    return f;
  }

  public String[] getFunctionThreads(String pattern, boolean forFunc) {
    WildcardMatcher wm = null;
    if (StringUtils.isNotBlank(pattern))
      wm = new WildcardMatcher(pattern,true);
    ArrayList v = new ArrayList();
    Iterator itor = fxns.values().iterator();
    while (itor.hasNext()) {
      Function f = (Function)itor.next();
      String n = f.getName();
      if (wm!=null && !wm.match(n))
        continue;
      if (f instanceof _Thread) {
        if (!forFunc)
          v.add(n);
      } else {
        if (forFunc)
          v.add(n);
      }
    }
    String[] sa = new String[v.size()];
    for (int i=0; i<sa.length; i++)
      sa[i] = (String)v.get(i);
    Arrays.sort(sa);
    return sa;
  }

  public void addThread(_Thread t) {
    if (fxns == null)
      fxns = new HashMap();
    fxns.put(t.getName(),t);
  }

  public _Thread getThread(String name) {
    if (fxns == null) return null;
    return (_Thread)fxns.get(name);
  }

  public void addObjectType(UserDefined.UserType ot) throws ExceptionRuntime {
    if (objectTypes == null)
      objectTypes = new HashMap();
    String name = ot.getName();
    if (objectTypes.containsKey(name))
      ExceptionRuntime.rte(RTERR_INTERNAL_ERROR, "User class '"+name+"' already defined.");
    objectTypes.put(name,ot);
  }

  public UserDefined.UserType getObjectType(String name) throws ExceptionRuntime {
    if (objectTypes == null)
      ExceptionRuntime.rte(RTERR_UNDEFINED_OBJECT_TYPE,"Undefined class name " + name + '.');
    UserDefined.UserType ut = (UserDefined.UserType)objectTypes.get(StringUtils.defaultString(name));
    if (ut == null)
      ExceptionRuntime.rte(RTERR_UNDEFINED_OBJECT_TYPE,"Undefined class name " + name + '.');
    return ut;
  }

  public void setConst(String name, int val) throws ParseException {
    setConst(name,ConstInt.getInt(val));
  }

  public void setConst(String name, String val) throws ParseException {
    try { setConst(name, JudoUtil.toVariable(val)); }
    catch(Exception e) { throw new ParseException(e.getMessage()); }
  }

  public void setConst(String name, Variable val) throws ParseException {
    if (constants.containsKey(name))
      throw new ParseException("Constant '"+name+"' cannot be re-defined.");
    constants.put(name,val);
  }

  public void _setConst(String name, Variable val) {
    if (val != null && !val.isNil())
      constants.put(name, val);
  }

  public Variable resolveConst(String name) {
    Variable ret = (Variable)constants.get(name);
    if (ret == null)
      return ValueSpecial.UNDEFINED;
    return ret;
  }

  public boolean hasConst(String name) {
    return constants.get(name) != null;
  }

  public boolean existsSgmlHandler(String name) {
    return allPurposes.containsKey(PREFIX_SGML_HANDLER+name);
  }

  public void addSgmlHandler(String name, StmtHtml stmt) {
    allPurposes.put(PREFIX_SGML_HANDLER+name, stmt);
  }

  public StmtHtml getSgmlHandler(String name) {
    return (StmtHtml)allPurposes.get(PREFIX_SGML_HANDLER+name);
  }

  public void addGuiNamedHandler(int lineNo, Triplet eventID, StmtListStmt sls)
                                throws ExceptionRuntime
  {
    String name = (String)eventID.o1;
    eventID.o1 = eventID.o2;
    eventID.o2 = eventID.o3;
    eventID.o3 = sls;
    if (allPurposes.containsKey(PREFIX_GUI_HANDLER+name))
      ExceptionRuntime.rte(RTERR_INTERNAL_ERROR, "Named GUI handler '"+name+"' already defined.");
    allPurposes.put(PREFIX_GUI_HANDLER+name, eventID);
  }

  public void attachGuiHandler(String qName, Object obj) throws Exception {
    try {
      Triplet tri = (Triplet)allPurposes.get(PREFIX_GUI_HANDLER+qName);
      if (tri == null)
        ExceptionRuntime.rte(RTERR_ILLEGAL_ARGUMENTS,
                  "Named GUI handler '" + qName + "' not found.");
      Collection mthds = null;
      if (tri.o2 instanceof Collection)
        mthds = (Collection)tri.o2;
      GuiContext gctxt = RT.getGuiContext();
      if (mthds == null) { // a single event case
        gctxt.addHandler(obj, (String)tri.o1, (String)tri.o2, (StmtListStmt)tri.o3);
      } else { // multiple messages sharing a same handler
        Iterator iter = mthds.iterator();
        while (iter.hasNext())
          gctxt.addHandler(obj, (String)tri.o1, (String)iter.next(), (StmtListStmt)tri.o3);
      }
    } catch(ExceptionRuntime rte) {
      throw rte;
    } catch(Exception e) {
      ExceptionRuntime.rte(RTERR_JAVA_METHOD_CALL,
        "Invalid AWT/Swing components to set event listener.");
    }
  }

  private void checkForGui() {
    int len = stmts.length;
    if (stmts[len-1] instanceof ExprCall) {
      if (((ExprCall)stmts[len-1]).name.equals("sleep"))
        return;
    }
    Stmt[] sa = new Stmt[len+1];
    System.arraycopy(stmts,0,sa,0,len);
    sa[len] = new ExprCall(null,"sleep",null,null);
    stmts = sa;
  }

  public void start(String[] args) {
    start(new RuntimeGlobalContext(args, this), false);
  }
  public void start(String[] args, boolean doX) {
    start(new RuntimeGlobalContext(args, this), doX);
  }
  public void start(RuntimeGlobalContext rtc) {
    start(rtc, false);
  }

  public void start(RuntimeGlobalContext rtc, boolean doExit) {
    if (Lib.existsClass("javax.net.ssl.HttpsURLConnection")) { // is JDK1.4 or higher
      try {
        Class jdk14 = RT.getSysClass("com.judoscript.jdk14.JDK14Thingy");
        Method mthd = jdk14.getMethod("disableSSLCertificateValidation", new Class[0]);
        mthd.invoke(null, new Object[0]);
      } catch(Exception eee) {}
    }

    Object ret = null;
    try {
      ret = startAllowException(rtc, null, doExit, false);
      if (ret != null)
        System.err.println("Exit with: " + ret);
    } catch(ExceptionRuntime rte) {
      RT.logger.fatal(rte.toString());
    } catch(Throwable e) {
      Throwable t = (e instanceof java.lang.reflect.InvocationTargetException)
                    ? ((java.lang.reflect.InvocationTargetException)e).getTargetException()
                    : e;
      String clsName = e.getClass().getName();
      if (clsName.startsWith("java"))
        clsName = clsName.substring(clsName.lastIndexOf(".") + 1);

      String msg = "Unhandled " + clsName;
      int line = rtc.getLineNumber();
      if (line > 0)
        msg += " at line #" + line;
      RT.logger.fatal(msg, t);
    } finally {
      RT.popContext();
//      rtc.close();
    }
  }


  public Object startAllowException(String[] args) throws Throwable {
    return startAllowException(new RuntimeGlobalContext(args, this), null, false, true);
  }

  public Object startAllowException(Expr[] args, RuntimeContext callerRtc) throws Throwable {
    return startAllowException(new RuntimeGlobalContext(args, this), callerRtc, false, true);
  }

  public Object startAllowException(String[] args, RuntimeContext callerRtc) throws Throwable {
    return startAllowException(new RuntimeGlobalContext(args, this), callerRtc, false, true);
  }

  public Object startAllowException(RuntimeGlobalContext rtc) throws Throwable {
    return startAllowException(rtc, null, false, true);
  }

  public Object startAllowException(RuntimeGlobalContext rtc,
                                    RuntimeContext callerRtc, // for evalSeparate/evalFileSeparate
                                    boolean doExit,
                                    boolean popCtxt) throws Throwable
  {
    Script.doExit = doExit;
    JudoUtil.registerToBSF();
    Object ret = null;
    try {
      sys = new Sys();
      rtc.setScript(this);

      if (callerRtc != null) {
        rtc.setOut(callerRtc.getOut());
        rtc.setErr(callerRtc.getErr());
        rtc.setIn(callerRtc.getIn());
        rtc.setLog(callerRtc.getLog());
      }

      RT.pushContext(rtc);
      exec();
    } catch(ExceptionExit xx) {
      ret = xx.getMessage();
      if (doExit)
        xx.doExit();
    } catch(ExceptionControl ec) {
      if (ec.isReturn()) { // This is never true, seems.
        return ec.getReturnValue();
      }
    } finally {
      if (rtc instanceof RuntimeGlobalContextDebug)
        ((RuntimeGlobalContextDebug)rtc).finish();
      //RT.setCurrentContext(null);

      if (popCtxt)
        RT.popContext();
    }

    return ret;
  }

  public void pushNewFrame() {} // root frame already set.
  public void popFrame() {}

  ////////////////////////////////////////////////////////////////////
  // Built-in Functions
  //

  public void startThread(String name, boolean daemon, Expr[] args) throws Throwable
  {
    getThread(name).start(args,daemon);
  }

  public Variable invoke(String fxn, Expr[] args, int[] javaTypes) throws Throwable
  {
    // user function takes precedence.
    Function fun = getFunction(fxn);
    if (fun != null)
      return fun.invoke(args, javaTypes);

    // otherwise, do the built-in functions
    if (fxn.startsWith("::"))
      fxn = fxn.substring(2);
    int ord = VariableAdapter.getShortcutOrdinal(fxn);
    ShortcutInvokable sci = null;

    try {
      switch(ord & MethodOrdinals.BIM_ALL__MASK) {
      case MethodOrdinals.BIM_FS__MASK:
      case MethodOrdinals.BIM_SYS__MASK:
        if (sys==null)
          sys = new Sys();
        return sys.invoke(ord, fxn, args, null);
      case MethodOrdinals.BIM_DBCON__MASK:
        sci = (ShortcutInvokable)RT.resolveVariable(DEFAULT_CONNECTION_NAME);
        break;
      }
      if (sci != null)
        return sci.invoke(ord, fxn, args, javaTypes);

      ExceptionRuntime.methodNotFound(null, fxn);
    } catch(ExceptionExit xx) {
//    if (RT.isMainThread())  // why?
//      throw xx;
      if (doExit)
        xx.doExit();
    }

    return ValueSpecial.UNDEFINED;
  }

  ////////////////////////////////////////////////////////////////////
  // The Dumpable interface implementation
  //

  public void dump(XMLWriter out) {
    out.simpleTagLn("Script");

    dumpObjectTypes(out);

    dumpFunctions(out,fxns);

    out.simpleTagLn("Statements");
    dumpConsts(out);
    super.dump(out); // Block
    out.endTagLn();

    out.endTagLn();
    out.flush();
  }

  void dumpObjectTypes(XMLWriter out) {
    String[] names = Lib.getKeys(objectTypes);
    int sz = (names==null) ? 0 : names.length;
    for (int i=0; i<sz; i++) {
      UserDefined.UserType ut = (UserDefined.UserType)objectTypes.get(names[i]);
      if ((ut instanceof UserDefined.Type) && !((UserDefined.Type)ut).isBuiltin())
        ut.dump(out);
    }
    if (sz > 0) out.println();
  }

  void dumpConsts(XMLWriter out) {
  }

  PBEBase getCryptoClass(String password) throws Exception {
    Class cls = RT.getClass(getCryptoClassName());
    Constructor ctor = cls.getConstructor(new Class[]{ String.class });
    return (PBEBase)ctor.newInstance(new Object[]{ password });
  }

  public void cryptFile(boolean encrypt, String password, String infile, String outfile) throws Exception {
    PBEBase crypto = getCryptoClass(password);
    FileInputStream fis = new FileInputStream(infile);
    FileOutputStream fos = new FileOutputStream(outfile);
    if (encrypt)
      crypto.encrypt(fis,fos);
    else
      crypto.decrypt(fis,fos);
    fis.close();
    fos.close();
  }

  public byte[] crypt(boolean encrypt, String password, byte[] input) throws Exception {
    PBEBase crypto = getCryptoClass(password);
    if (encrypt)
      return crypto.encrypt(input);
    else
      return crypto.decrypt(input);
  }

  public void crypt(boolean encrypt, String password, InputStream in, OutputStream out) throws Exception {
    PBEBase crypto = getCryptoClass(password);
    if (encrypt)
      crypto.encrypt(in, out);
    else
      crypto.decrypt(in, out);
  }

  public static void dumpFunctions(XMLWriter out, HashMap fxns) {
    String[] names = Lib.getKeys(fxns);
    int sz = (names==null) ? 0 : names.length;
    for (int i=0; i<sz; i++)
      ((Function)fxns.get(names[i])).dump(out);
    if (sz > 0)
      out.println();
  }

  public static int getErrorType(String name) {
    for (int i=rterr_names.length-1; i>=0; --i) {
      if (name.equals(rterr_names[i]))
        return i;
    }
    return -1;
  }

  public void checkMinArgs(int numargs) {
    try {
      if ( numargs < ((Integer)getUsage().get("minArgs")).intValue() )
        displayUsage();
    } catch(Exception e) {}
  }


  public void displayUsage() {
    PrintStream out = System.out;
    HashMap u = getUsage();
    out.println();
    if (u == null) {
      out.println("Script " + progName + " has no further help information available.");
    } else {
      try {
        Object _args = u.get("args");
        if (_args != null) {
          if (_args instanceof String) {
            out.println("Usage: java judo " + progName + ' ' + _args);
          } else {
            String[] sa = (String[])_args;
            for (int i=0; i<sa.length; ++i) {
              out.println( ((i==0) ? "Usage" : "   or") + ": java judo " + progName + ' ' + sa[i]);
            }
          }
        }
        String desc = (String)u.get("desc");
        if (!StringUtils.isBlank(desc)) {
          out.println();
          out.println(desc);
        }
      } catch(Exception e) {}
    }
    System.exit(0);
  }

} // end of class Script.
