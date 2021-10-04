/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 03-17-2002  JH   Initial open source release.
 * 08-10-2002  JH   Variables are not preceded with $ any more.
 * 06-06-2003  JH   Switch from IBM BSF (2.2) to Apache BSF (2.3).
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript;

import java.io.StringReader;
import java.util.Vector;
import com.judoscript.parser.JudoParser;

import org.apache.bsf.*;
import org.apache.bsf.util.BSFEngineImpl;
import org.apache.bsf.util.BSFFunctions;

/**
 * This is the interface for BSF.
 * Need to have IBM's BSF package in the classpath.
 *
 * @author   James Huang
 */

public class BSFJudoEngine extends BSFEngineImpl
{
  RuntimeGlobalContext rtc;
  Script judoscript = null;

  /**
   * Initializes the engine. Establishes the one and only <code>$$bsf</code>
   * object in JudoScript, and sets all the declared beans as global variables.
   *
   *@param mgr           the BSF manager.
   *@param lang          always "judoscript".
   *@param declaredBeans objects to set during initialization; elements are
   *                     instances of <code>com.ibm.bsf.DeclaredBean</code>.
   */
  public void initialize(BSFManager mgr, String lang, Vector declaredBeans) throws BSFException
  {
    super.initialize(mgr, lang, declaredBeans);

    rtc = new RuntimeGlobalContext((String[])null, null);
    RT.pushContext(rtc);

    try {
      // register the mgr
      RT.setVariable("$$bsf", JudoUtil.toVariable(new BSFFunctions(mgr,this)), 0);

      for (int i=declaredBeans.size()-1; i>=0; --i)
        declareBean ((BSFDeclaredBean)declaredBeans.elementAt(i));
    } catch(Throwable e) {
      throw getBSFException(e);
    }
  }

  /**
   * Evaluates an expression.
   *
   *@param source   the file name of the expression; not used.
   *@param lineNo   the line number in the file; not used.
   *@param columnNo the column number in the file; not used.
   *@param script   the expression; should be a String.
   *@return         the result of the expression.
   */
  public Object eval(String source, int lineNo, int columnNo, Object script) throws BSFException
  {
    try {
      // the script is expected to be an expression.
      String vname = "$_bsf_internal_";
      String assign = vname + "=" + script + ';';
      JudoParser.parse(null, null, new StringReader(assign), null, 0, false).start(rtc);
      Variable result = RT.resolveVariable(vname);
      return result.isNil() ? null : result.getObjectValue();
    } catch (Throwable e) {
      throw getBSFException(e);
    }
  }

  /**
   * Executes a script. 
   *
   *@param source   the file name of the expression; not used.
   *@param lineNo   the line number in the file; not used.
   *@param columnNo the column number in the file; not used.
   *@param script   the script code; should be a String.
   */
  public void exec (String source, int lineNo, int columnNo, Object script) throws BSFException {
    try {
      // save the script for call().
      judoscript = JudoParser.parse(null, null, new StringReader(script.toString()), null, 0, false);
      judoscript.start(rtc);
    } catch (Exception e) {
      RT.logger.error("Failed in BSF.", e);
      throw getBSFException(e);
    }
  }

  /**
   * Calls a function. (Calling object's methods is not supported.)
   *
   *@param object is the name of the object; not used.
   *@param fxn    the function name.
   *@param args   the arguments.
   *@return       the result.
   */
  public Object call (Object object, String fxn, Object[] args) throws BSFException
  {
    if (judoscript == null) return null;
    int len = (args==null)?0:args.length;
    Expr[] params = new Expr[len];
    try {
      for (int i=0; i<len; ++i)
        params[i] = (args[i]==null) ? ValueSpecial.NIL : JudoUtil.toVariable(args[i]);
      Variable result = judoscript.invoke(fxn, params, null);
      return result.isNil() ? null : result.getObjectValue();
    } catch(Throwable e) {
      throw getBSFException(e);
    }
  }

  /**
   * Declare a bean.
   * Sets the bean as a global variable in JudoScript.
   */
  public void declareBean (BSFDeclaredBean bean) throws BSFException {
    try {
      RT.setVariable(bean.name, JudoUtil.toVariable(bean.bean), 0);
    } catch(Throwable e) {
      throw getBSFException(e);
    }
  }

  /**
   * Undeclare a previously declared bean.
   * Removes the named global variable from JudoScript.
   */
  public void undeclareBean (BSFDeclaredBean bean) throws BSFException {
    try { RT.removeVariable(bean.name); }
    catch(Throwable e) { throw getBSFException(e); }
  }

  BSFException getBSFException(Throwable e) {
    return new BSFException(BSFException.REASON_EXECUTION_ERROR, "exception from JudoScript: "+e, e);
  }

} // end of class BSFJudoEngine.
