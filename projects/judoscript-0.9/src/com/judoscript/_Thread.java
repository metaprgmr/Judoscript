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


package com.judoscript;

//import com.judoscript.util.XMLWriter;

public class _Thread extends FunctionUser
{
  public _Thread(Stmt block) {
    super(0, 0, JudoUtil.genTempVarName(), false, null, null, block);
  }

  public _Thread(int beginLine, int endLine, String name, boolean isMethod,
                 String[] params, Expr[] defVals, Stmt block)
  {
    super(beginLine, endLine, name, isMethod, params, defVals, block);
  }

  public void start(Expr[] args, boolean daemon) throws Throwable {
    RuntimeSubContext rttc = RT.newSubContext();
    int len = (args==null) ? 0 : args.length;
    Variable[] params = null;
    if (len > 0) {
      params = new Variable[len];
      for (int i=0; i<len; ++i) { // evaluate args in the caller's context.
        params[i] = args[i].eval();
      }
    }
    rttc.setFunctionArguments(params);
    if (isMethod)
      rttc.pushThis(RT.getThisObject());

    Thread t = new Thread(RuntimeGlobalContext.JudoScriptThreadGroup, new Instance(rttc));
    t.setDaemon(daemon);
    t.start();
  }

  public String getTagName() { return "_Thread"; }

  ////////////////////////////////////////////
  // need a new class because need to keep runtime stuff
  //
  class Instance implements Runnable
  {
    RuntimeContext RTC;

    Instance(RuntimeContext rtc) { RTC = rtc; }

    public void run() {
      try {
        RT.pushContext(RTC);
        _Thread.this.exec();
      } catch(Throwable e) {
        RT.logger.error("Failed in thread.", e);
      } finally {
        RT.popContext();
      }
    }
  }

} // end of class _Thread.
