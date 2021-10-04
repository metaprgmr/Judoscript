/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 03-17-2002  JH   Initial open source release.
 * 08-22-2002  JH   Added emptyExprs.
 * 12-17-2002  JH   Added loadProperties() and changed the way
 *                  built-in stuff is defined -- into properties.
 * 01-20-2003  JH   Shield com.judoscript.ext.* from the rest.
 * 06-06-2003  JH   Added toVariable() method, deprecating
 *                  ConstString.getString() and JavaObject.wrap().
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript;

import java.io.*;
import java.util.*;
import java.net.URL;
import java.lang.reflect.Method;
import org.apache.commons.lang.StringUtils;
import com.judoscript.util.*;
import com.judoscript.bio.*;
import com.judoscript.db.*;

public class JudoUtil implements Consts
{
  public static boolean searchCP = true;
  static { searchCP = !System.getProperties().containsKey("nocp"); }

  /**
   * If it is not found, and the extension is not ended with ".jud*",
   * append ".judo" and do the same search.
   */
  public static Reader findFile(String path, String name, String enc)
  	throws FileNotFoundException, IOException
	{
    String realName = name;
    for (int i=0; i<2; ++i) {
      try {
        File file = new File(path, name);
        if (file.exists())
          return new NamedReader(file);
        return findFileInJudoPath(name, realName, enc);
      } catch(FileNotFoundException fnfe) {
        if (i>0)
        	throw fnfe;
        if (Lib.getFileExt(name).indexOf(".jud") != 0)
        	throw fnfe;
        name += ".judo";
      }
    }
    return null;
  }

  public static Reader findFile(String name, String enc)
  	throws FileNotFoundException, IOException
	{
    String realName = name;
    for (int i=0; i<2; ++i) {
      try {
        File file = new File(name);
        if (file.exists())
          return new NamedReader(file);
        return findFileInJudoPath(name, realName, enc);
      } catch(FileNotFoundException fnfe) {
        if (i>0) throw fnfe;
        if (Lib.getFileExt(name).equals("judo"))
        	throw fnfe;
        name += ".judo";
      }
    }
    return null;
  }

  /**
   * The pseudocode for locating the file:
   *
   * If the file name has absolute or relative path information then
   *   return the file for that path;
   * Else
   *   return a found file in "JUDOPATH".
   * End if.
   *
   * If environment variable "JUDOPATH" is defined then
   *   use that;
   * Else
   *   If Windows then
   *     if $home is not null then
   *       JUDOPATH = "$home/.judobase;c:/judobase";
   *     Else
   *       JUDOPATH = "c:/judobase";
   *     End if.
   *   Eles
   *     if $home is not null then
   *       JUDOPATH = "$home/.judobase:/usr/judobase";
   *     Else
   *       JUDOPATH = "/usr/judobase";
   *     End if.
   *   End if.
   * End if.
   */
  public static Reader findFileInJudoPath(String name, String realName, String enc)
    throws FileNotFoundException, IOException
  {
    String sep = File.pathSeparator;
    File file;
    int idx = name.lastIndexOf("/");
    if (idx < 0)
      idx = name.lastIndexOf("\\");
    if (idx >= 0) {
      file = new File(name);
      if (file.exists())
        return new NamedReader(file);
    } else { // no path present, search JUDOPATH
      String judopath = RT.getJudoPath();
      if (judopath == null) {
        String home = Lib.getHomeDir();
        if (home != null)
          judopath = home + "/.judobase" + sep;
        else
          judopath = "";
        if (Lib.isWindows())
          judopath += "c:/judobase";
        else
          judopath += "/usr/judobase";
      }
      StringTokenizer st = new StringTokenizer(judopath,sep);
      while (st.hasMoreTokens()) {
        File path = new File(st.nextToken());
        if (!path.isDirectory()) continue;
        file = new File(path,name);
        if (file.exists())
          return new NamedReader(file);
      }
    }
    if (searchCP)
      return new NamedReader(findInClasspath(name, realName), enc);
    Lib.throwFileNotFoundException(realName);
    return null;
  }

  public static URL findInClasspath(String name, String realName)
    throws FileNotFoundException
  {
    ClassLoader cl = JudoUtil.class.getClassLoader();
    URL url = cl.getResource(name);
    if (url == null)
      url = ClassLoader.getSystemResource(name);
    if (url == null)
      Lib.throwFileNotFoundException(realName);
    return url;
  }

  public static InputStream findFileInClasspath(String name, String realName)
    throws FileNotFoundException, IOException
  {
    return findInClasspath(name, realName).openStream();
  }

  public static ExprRelational createSimpleRelation(String op, Expr lhs, Expr rhs) {
    int rop = OP_EQ;
    if (op.equals("<"))       rop = OP_LT;
    else if (op.equals("<=")) rop = OP_LE;
    else if (op.equals(">"))  rop = OP_GT;
    else if (op.equals(">=")) rop = OP_GE;
    return new ExprRelational( lhs, new int[]{rop}, new Expr[]{rhs} );
  }

  public static Expr createSimpleArith(String op, Expr lhs, Expr rhs) {
    Expr[] ea = new Expr[]{rhs};
    int ops[] = new int[]{OP_CONCAT};
    if (op.equals("+"))      ops[0] = OP_PLUS;
    else if (op.equals("-")) ops[0] = OP_MINUS;
    else if (op.equals("*")) ops[0] = OP_MUL;
    else if (op.equals("/")) ops[0] = OP_DIV;
    else if (op.equals("%")) ops[0] = OP_MOD;
    switch(ops[0]) {
    case OP_PLUS:
    case OP_MINUS: return new ExprAddMinus(lhs,ops,ea);
    case OP_MUL:
    case OP_DIV:
    case OP_MOD:   return new ExprMulDivMod(lhs,ops,ea);
    default:       return new ExprConcat(lhs,ea);
    }
  }

  public static _Array arrayToJudoArray(Object[] oa) throws Exception {
    _Array ar = new _Array();
    for (int i=0; i<oa.length; i++)
      ar.append(toVariable(oa[i]));
    return ar;
  }

  public static void registerToBSF() {
    try {
      Class[] params = new Class[]{ String.class, String.class, RT.getSysClass("[Ljava.lang.String;") };
      Method m = RT.getClass("org.apache.bsf.BSFManager").getMethod("registerScriptingEngine", params);
      Object[] vals = new Object[]{ "judoscript",
                                    "com.judoscript.BSFJudoEngine",
                                    new String[]{ "judo", "jud" }
                                  };
      m.invoke(null, vals);
    } catch(Throwable e) {} // if BSF is not there, so be it.
  }

  public static final Expr[] emptyExprs = new Expr[]{};

  private static int tempVarCnt = 1;

  public static String genTempVarName() { return genTempVarName(TEMPVAR_PREFIX); }

  public static String genTempVarName(String prefix) { return prefix + tempVarCnt++; }

  public static String getJavaPrimitiveTypeName(int type) {
    switch(type) {
    case JAVA_BOOLEAN:  return "boolean";
    case JAVA_BYTE:     return "byte";
    case JAVA_CHAR:     return "char";
    case JAVA_SHORT:    return "short";
    case JAVA_INT:      return "int";
    case JAVA_LONG:     return "long";
    case JAVA_FLOAT:    return "float";
    case JAVA_DOUBLE:   return "double";
    case JAVA_STRING:   return "String";
    case JAVA_CURRENCY: return "currency";
    default:            return "Any";
    }
  }

  public static Properties loadProperties(String name, boolean mandatory) {
    name = "com/judoscript/" + name + ".properties";
    try {
      Properties props = new Properties();
      props.load(findFileInClasspath(name, name));
      return props;
    } catch(Exception e) {
      if (mandatory)
        RT.logger.warn(name + " not found.");
      return null;
    }
  }

  ////////////////////////////////////////////////////////////////////
  // ext package shielding
  //

  static Method comWrap = null;
  public static Variable comWrap(Object val) throws Exception {
    if (comWrap == null) {
      Class cls = RT.getSysClass("com.judoscript.ext.win32.JComUtil");
      comWrap = cls.getDeclaredMethod("wrap",
                  new Class[]{ java.lang.Object.class });
    }
    return (Variable)comWrap.invoke(null, new Object[]{val});
  }

  static Method sshWrap = null;
  public static Variable getSSH(String host, int port, String user, String pass, String cipher)
                         throws Exception
  {
    if (sshWrap == null) {
      Class cls = RT.getSysClass("com.judoscript.ext.SSHFactory");
      sshWrap = cls.getDeclaredMethod("getSSH",
                  new Class[]{java.lang.String.class,
                              java.lang.Integer.TYPE,
                              java.lang.String.class,
                              java.lang.String.class,
                              java.lang.String.class
                             });
    }
    return (Variable)sshWrap.invoke(null, new Object[]{ host, new Integer(port), user, pass, cipher});
  }

  static Method scpWrap = null;
  public static void scp(String host, int port, String user, String pass, String cipher,
                         String[] src, String dest, boolean recursive, boolean toRemote, boolean verbose)
                        throws Exception
  {
    if (scpWrap == null) {
      Class cls = RT.getSysClass("com.judoscript.ext.SSHFactory");
      scpWrap = cls.getDeclaredMethod("scp",
                  new Class[]{java.lang.String.class,
                              java.lang.Integer.TYPE,
                              java.lang.String.class,
                              java.lang.String.class,
                              java.lang.String.class,
                              java.lang.String[].class,
                              java.lang.String.class,
                              java.lang.Boolean.TYPE,
                              java.lang.Boolean.TYPE,
                              java.lang.Boolean.TYPE
                             });
    }
    scpWrap.invoke(null, new Object[]{ host, new Integer(port), user, pass, cipher, src, dest,
                           recursive ? Boolean.TRUE : Boolean.FALSE,
                           toRemote ? Boolean.TRUE : Boolean.FALSE,
                           verbose ? Boolean.TRUE :  Boolean.FALSE
                         });
  }

  public static Expr parseString(String s) {
    Object o = parseStringAsObject(s);
    if (o instanceof Expr)
      return (Expr)o;
    return toVariable(o);
  }

  public static void processMarkup(Markup mu) {
    // Process mu for embedded ${..} in attribute values and/or the text content.
    if (mu.isText())
      mu.setText( parseStringAsObject(mu.getText().toString()) );

    int i;
    for (i=mu.numAttrs()-1; i>=0; --i) {
      Object o = mu.getAttrValue(i);
      o = parseStringAsObject((String)o);
      mu.setAttrValue(i, o);
    }

    for (i=mu.numChildren()-1; i>=0; --i)
      processMarkup( mu.getChild(i) );
  }

  /**
   * Returns a list of String's or AccessVar's.
   */
  public static Object parseStringAsObject(String s) {
    if (s.indexOf("${") < 0 && s.indexOf("$_") < 0)
      return s;

    List list = new ArrayList();
    String val = StringUtils.defaultString(s);
    while (val.length() > 0) {
      int startIdx = 0;
      int idx = val.indexOf("\\$");
      while (idx >= 0) {
        val = val.substring(0, idx) + val.substring(idx+1); // remove the '\\'.
        startIdx = idx + 1;
        idx = val.indexOf("\\$", startIdx);
      }

      idx = val.indexOf("$_", startIdx);
      if (idx >= 0) {
        if (idx > 0 && val.charAt(idx-1) == '\\') {
          ; // leave it as-is.
        } else {
          if (idx > 0)
            list.add(val.substring(0,idx));
          list.add(new AccessVar(AccessVar.THIS_NAME, AccessVar.ACCESS_VAR_ENV));
          val = val.substring(idx+2);
          continue;
        }
      }

      idx = val.indexOf("${", startIdx);
      if (idx < 0) {
        list.add(val);
        break;
      }
      int idx1 = val.indexOf("}",idx);
      if (idx1 < 0) {
        list.add(val);
        break;
      }
      if (idx > 0)
        list.add(val.substring(0,idx));
      s = val.substring(idx+2,idx1).trim();
      boolean isGlobal = s.startsWith("::");
      if (isGlobal)
        s = s.substring(2).trim();
      if (s.length() > 0)
        list.add(new AccessVar(s, isGlobal ? AccessVar.ACCESS_GLOBAL : AccessVar.ACCESS_VAR_ENV));
      val = val.substring(idx1+1);
    }

    if (list.size() == 1)
      return list.get(0);
    else
      return new ExprConcat(list);
  }

  public static String getTempDir() {
    String ret = Lib.getHomeDir();
    if (StringUtils.isNotBlank(ret))
      return ret;
    return Lib.getCurrentDir();
  }

  public static boolean isNil(Variable var) {
    if (var == null) return true;
    return var.isNil();
  }

  public static boolean isDate(Variable v) {
    try {
      if (v instanceof _Date) return true;
      if (v instanceof JavaObject) {
        Object o = v.getObjectValue();
        return o instanceof java.util.Date;
      }
    } catch(Throwable e) {}
    return false;
  }

  public static long getTime(Variable v) {
    try { if (isDate(v)) return v.getLongValue(); } catch(Throwable e) {}
    return 0;
  }

  public static Locale getLocale(Expr[] params) {
    Object o1 = null;
    if (params == null || params.length == 0) {
      try {
        o1 = params[0].getObjectValue();
        if (o1 instanceof Locale) return (Locale)o1;
        return new Locale(o1.toString(), (params.length > 1) ? params[1].getStringValue() : "");
      } catch(Throwable e) {}
    }
    return Locale.getDefault();
  }

  public static _Array toArray(Variable[] a) throws Throwable {
    _Array arr = new _Array();
    arr.init(a);
    return arr;
  }

  public static Variable toVariable(Object o) { return toVariable(o,null); }

  public static Variable toVariable(Object val, Class type) { 
    if (val == null) return ValueSpecial.NIL;
    if (val instanceof Variable) return (Variable)val;

    Class cls = val.getClass();
    if (cls.isArray()) {
      try {
        return JavaArray.wrapArray(val);
      } catch(Exception e) {
        RT.logger.error("Failed to convert an array to JudoScript variable.", e);
        return ValueSpecial.UNDEFINED;
      }
    }
    if (cls.getName().indexOf(".jcom.") >= 0) {
      try {
        return comWrap(val);
      } catch(Exception e) {
        RT.logger.error("Failed to convert JCom object to JudoScript variable.", e);
        return ValueSpecial.UNDEFINED;
      }
    }
    if (cls == Character.class)
      return new ConstString("" + ((Character)val).charValue());
    if (cls == Boolean.class)
      return ConstInt.getBool(((Boolean)val).booleanValue());
    if (Number.class.isInstance(val)) {
      if (Double.class.isInstance(val) || Float.class.isInstance(val))
        return ConstDouble.getDouble(((Number)val).doubleValue());
      else
        return ConstInt.getInt(((Number)val).longValue());
    }
    if (cls == String.class)
      return new ConstString((String)val);
    if (val instanceof java.util.Date)
      return new _Date(((java.util.Date)val).getTime());
    if (val instanceof java.sql.ResultSet) {
      try {
        return new QueryResult((java.sql.ResultSet)val);
      } catch(Exception e) {
        RT.logger.error("Failed to convert a ResultSet to JudoScript variable.", e);
        return ValueSpecial.UNDEFINED;
      }
    }
    if (val instanceof TableData)
      return new _TableData((TableData)val);
    if (val instanceof java.sql.Connection)
      return new DBConnect((java.sql.Connection)val);
    if (val instanceof java.util.zip.ZipFile)
      return new ZipArchive((java.util.zip.ZipFile)val);
    if (val instanceof java.io.InputStream || val instanceof java.io.OutputStream)
      return new IODevice(val,0,0);
    try {
      Class docCls = RT.getClass("org.w3c.dom.Document");
      if (docCls.isInstance(val))
        return new DOMDoc(val);
    } catch(Exception e) {}

    return new JavaObject(type,val);
  }

  // used by fs commands.
  public static String fixFilePaths(String paths) {
    if (paths != null) {
      int idx = paths.indexOf("~/");
      if (idx >= 0) {
        idx = paths.indexOf(",");
        if (idx < 0) { // a single path
          paths = fixFilePath(paths);
        } else {
          StringTokenizer st = new StringTokenizer(paths, ",");
          StringBuffer sb = new StringBuffer(paths.length());
          while (st.hasMoreTokens()) {
            if (sb.length() > 0) sb.append(',');
            sb.append(fixFilePath(st.nextToken()));
          }
          paths = sb.toString();
        }
      }
    }
    return paths;
  }

  // First of all, handles leading ~/. Then, handles ${name}'s.
  public static String fixIncludeFilePath(String path, String refPath) {
    if (path.indexOf("${") >= 0) {
      Object ps = parseStringAsObject(path);
      if (ps instanceof ExprConcat) {
        List list = ((ExprConcat)ps).getOperands();
        StringBuffer sb = new StringBuffer();
        for (int i=0; i<list.size(); ++i) {
          Object o = list.get(i);
          if (o instanceof AccessVar)
            sb.append(Lib.getEnvVar(((AccessVar)o).getName()));
          else
            sb.append(o);
        }
        path = sb.toString();
      } else {
        // ps, the parsed string, can be AccessVar or other object.
        path = ps.toString();
      }
    }
    
    if (StringUtils.isBlank(refPath) || path.startsWith("~/"))
    	return fixFilePath(path);
    
    refPath = refPath.replace('\\', '/');
    if (Lib.isAbsolutePath(path))
    	return path;

    int idx = refPath.lastIndexOf('/');
    if (idx < 0)
    	return path;
    else
    	return refPath.substring(0, idx+1) + path;
  }

  public static String fixFilePath(String path) {
    if (path.startsWith("~/"))
      return Lib.getHomeDir() + File.separator + path.substring(2);
    return path;
  }

  /**
   * Parse a string to be a number of parameters.
   * The params string can be:
   *<pre>
   *  -a b c
   *  -a=1 -b='1 3 5'
   *</pre>
   * TODO: for now, simply StringTokenize it.
   */
  public static String[] parseParams(String s) {
    if (StringUtils.isBlank(s))
      return new String[0];

    StringTokenizer st = new StringTokenizer(s, " ");
    String[] ret = new String[st.countTokens()];
    for (int i=0; st.hasMoreTokens(); ++i)
      ret[i] = st.nextToken();

    return ret;
  }

  public static char toChar(Expr var) {
    try {
      if (var instanceof ConstInt || var instanceof ConstDouble)
        return (char)var.getLongValue();
      String s = var.getStringValue();
      return (s.length()==0)? '\0' : s.charAt(0);
    } catch(Throwable t) {
      return '\0';
    }
  }

  public static String getPathName(File f) {
    return f.getAbsolutePath().replace('\\', '/');
  }

  public static String toParameterNameString(Object o) throws Throwable {
    if (o instanceof Expr)
      return ((Expr)o).getStringValue();
    return o.toString();
  }

  public static void checkJCE() throws ExceptionRuntime {
    try { RT.getClass("javax.crypto.SecretKeyFactory"); }
    catch(Exception e) {
      ExceptionRuntime.rte(RTERR_JAVA_OBJECT_CREATION, "JCE package is not available.");
    }
  }
  
  public static Throwable getJavaException(Throwable th) {
    if (th instanceof ExceptionRuntime)
      return ((ExceptionRuntime)th).getRealException();
    return th;
  }

  public static LinePrintWriter toLinePrintWriter(Object output) throws Exception {
    return toLinePrintWriter(output, false);
  }

  public static LinePrintWriter toLinePrintWriter(Object output, boolean append) throws Exception {
    if (output instanceof OutputStream)
      return new LinePrintWriter((OutputStream)output);
    else if (output instanceof Writer)
      return (output instanceof LinePrintWriter)
             ? (LinePrintWriter)output : new LinePrintWriter((Writer)output);

    return new LinePrintWriter(new FileOutputStream(output.toString(), append));
  }

} // end of class JudoUtil.

