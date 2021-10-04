/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 03-17-2002  JH   Initial open source release.
 * 08-10-2002  JH   Objects not preceded by $ no more.
 * 03-03-2004  JH   Added setOut(), setErr(), setLog() and setIn().
 * 07-20-2004  JH   Use text I/O only.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript;

import java.io.*;
import java.util.Date;
import java.util.Map;
import com.judoscript.Script;
import com.judoscript.parser.JudoParser;
import com.judoscript.bio._Date;
import com.judoscript.util.LinePrintWriter;

/**
 * <p>This is the JudoEngine for Java to invoke and interact with JudoScript.
 * It allows Java code to invoke JudoScript functions, or evaluate expression,
 * as well as passing objects between Java and JudoScript.</p>
 *
 * <p>It is easier and lighter-weight than BSF and needs no extra packages.</p>
 *
 * @author   James Huang
 */

public class JudoEngine
{
  private RuntimeGlobalContext rtc;
  private Script judoscript = null;

  public JudoEngine() {
    rtc = new RuntimeGlobalContext();
    RT.pushContext(rtc);
  }

  public void setOut(LinePrintWriter pw) { rtc.setOut(pw); }
  public void setErr(LinePrintWriter pw) { rtc.setErr(pw); }
  public void setLog(LinePrintWriter pw) { rtc.setLog(pw); }
  public void setIn(BufferedReader   br) { rtc.setIn(br); }

  /**
   *  Puts an object into the JudoScript root context as a global variable.
   * @param name  the name of the global variable in JudoScript.
   * @param value the variable value.
   */
  public void putBean(String name, Object bean) throws Throwable {
    Variable v;
    if (bean == null) v = ValueSpecial.NIL;
    else if (bean instanceof Boolean)   v = ConstInt.getBool(((Boolean)bean).booleanValue());
    else if (bean instanceof Character) v = JudoUtil.toVariable(bean.toString());
    else if (bean instanceof Byte)      v = ConstInt.getInt((long)((Byte)bean).byteValue());
    else if (bean instanceof Short)     v = ConstInt.getInt((long)((Short)bean).shortValue());
    else if (bean instanceof Integer)   v = ConstInt.getInt((long)((Integer)bean).intValue());
    else if (bean instanceof Long)      v = ConstInt.getInt(((Long)bean).longValue());
    else if (bean instanceof Float)     v = ConstDouble.getDouble((double)((Float)bean).floatValue());
    else if (bean instanceof Double)    v = ConstDouble.getDouble(((Double)bean).doubleValue());
    else if (bean instanceof String)    v = JudoUtil.toVariable(bean.toString());
    else if (bean instanceof Date)      v = new _Date((Date)bean);
    else v = JudoUtil.toVariable(bean);
    rtc.setVariable(name, v, 0);
  }

  /**
   *  Removes the named global variable in JudoScript.
   * @param name  the name of the global variable.
   */
  public void clearBean(String name) {
    try {
      RT.removeVariable(name);
    } catch(Throwable e) {
      RT.logger.warn("JudoEngine.cleanBean()", e);
    }
  }

  /**
   * @param name  the name of the global variable.
   * @return      the named global variable in JudoScript.
   */
  public Object getBean(String name) {
    try {
      Variable v = RT.getRootFrame().resolveVariable(name);
      if (v.isNil()) return null;
      return v.getObjectValue();
    } catch(Throwable e) {
      RT.logger.warn("JudoEngine.getBean()", e);
      return null;
    }
  }

  /**
   * @param path  the path of a script file.
   * @return      a "script" object. This can be passed to <code>runScript()</code> method.
   * @see         #runScript(java.lang.Object)
   */
  public final static Object parseScript(String path) throws Exception {
    return JudoParser.parse(null, null, new FileReader(path), null, 0, false);
  }

  /**
   * @param code  the script code.
   * @return      a "script" object. This can be passed to <code>runScript()</code> method.
   * @see         #runScript(java.lang.Object)
   */
  public final static Object parseCode(String code) throws Exception {
    return JudoParser.parse(null, null, new StringReader(code.toString()), null, 0, false);
  }

  /**
   *  Executes the script.
   *  The script object is saved for future <code>call()</code> uses.
   * @param script the "script" object returned by <code>parseScript()</code>.
   */
  public final void runScript(Object script, String[] args, Map sysprops) throws Exception {
    if (judoscript != null)
      ((Script)script).acceptDecls(judoscript);
    judoscript = (Script)script;

    rtc.setScript(judoscript);
    rtc.setArguments(args, sysprops);
    judoscript.start(rtc);
  }

  /**
   *  Executes the script.
   *  The script object is saved for future <code>call()</code> uses.
   * @param path  the path for a script file.
   */
  public final void runScript(String path, String[] args, Map sysprops) throws Exception {
    runScript(JudoParser.parse(null, null, new FileReader(path), null, 0, false), args, sysprops);
  }

  /**
   *  Executes the script code.
   *  The script object is saved for future <code>call()</code> uses.
   * @param code  the script code.
   */
  public final void runCode(String code, String[] args, Map sysprops) throws Exception {
    runScript(JudoParser.parse(null, null, new StringReader(code.toString()), null, 0, false), args, sysprops);
  }

  /**
   *  Call the named function in the saved script object.
   * @param fxn   the function name.
   * @param args  the parameters to the function.
   * @return      the return value of the function.
   * @see         #runScript(java.lang.Object)
   * @see         #runScript(java.lang.String)
   * @see         #runCode(java.lang.String)
   */
  public Object call(String fxn, Object[] args) throws Throwable {
    if (judoscript == null)
      return null;
    int len = (args==null) ? 0 : args.length;
    Expr[] params = new Expr[len];
    for (int i=0; i<len; ++i)
      params[i] = (args[i]==null) ? ValueSpecial.NIL : JudoUtil.toVariable(args[i]);
    Variable result = judoscript.invoke(fxn, params, null);
    return result.isNil() ? null : result.getObjectValue();
  }

} // end of class JudoEngine.
