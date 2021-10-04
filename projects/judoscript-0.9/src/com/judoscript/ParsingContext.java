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
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript;

import java.io.*;
import java.util.Stack;
import java.util.ArrayList;
import org.apache.commons.lang.StringUtils;
import com.judoscript.gui.*;
import com.judoscript.parser.ParseException;
import com.judoscript.util.*;


public class ParsingContext extends RuntimeContext
{
  Script script;
  FrameRoot root;
  Variable envVars = null;

  public ParsingContext(Script s) {
    super();
    script = s;
    root = new FrameRoot();
  }

  public void setAssertion(boolean set) {}
  public boolean doAssertion() { return false; }

  public Script getScript() { return script; }
  public FrameRoot getRootFrame() { return root; }
  public void setConst(String name, Variable val) throws ParseException { script.setConst(name,val); }

  public BufferedReader getIn() { return null; }
  public LinePrintWriter getOut() { return null; }
  public LinePrintWriter getErr() { return null; }
  public LinePrintWriter getLog() { return null; }
//  public OutputStream getOutStream() { return null; }
//  public OutputStream getErrStream() { return null; }
//  public InputStream  getInStream() { return null; }
//  public void setOut(OutputStream os, Writer ow) {}
//  public void setErr(OutputStream os, Writer ow) {}
//  public void setIn(InputStream is) {}
  public void setIn(BufferedReader is) {}
  public void setOut(LinePrintWriter ow) {}
  public void setErr(LinePrintWriter ow) {}
  public void setLog(LinePrintWriter ow) {}
//  public InputStream getPipeInStream() { return null; }
  public BufferedReader getPipeIn() { return null; }
//  public OutputStream getPipeOutStream() { return null; }
  public LinePrintWriter getPipeOut() { return null; }
//  public void setPipeIn(InputStream is) {}
//  public void setPipeOut(OutputStream os) {}
  public void setPipeIn(BufferedReader is) {}
  public void setPipeOut(LinePrintWriter os) {}
  public void clearPipeIn() {}
  public void clearPipeOut() {}
  public SendMail getSendMail() { return null; }
  public void setSendMail(SendMail sm) {}
//  public void setCurrentDir(String s) {}
//  public String currentDir() { return null; }
  public void echoOn(String filename) throws Exception {}
  public void echoOff() {}
  public void echo(String msg) throws Exception {}
  public RegexEngine getRegexCompiler() throws Exception { return null; }
  public Object getAntFacade() throws Exception { return null; }
  public String getCharset() { return null; }
  public void setCharset(String cset) {}

  public Variable getEnvVars() {
    if (envVars == null)
      try { envVars = JudoUtil.toVariable(Lib.getEnvVars()); } catch(Exception e) {}
    return envVars;
  }

  public String getEnvVar(String name) { return Lib.getEnvVar(name); }

  Stack ns_stack = new Stack();

  public void setCurrentDefaultNS(String namespace) {
    if (StringUtils.isNotBlank(namespace)) ns_stack.push(namespace);
    else { try { ns_stack.pop(); } catch(Exception e) {} }
  }

  public String getCurrentDefaultNS() {
    return (ns_stack.isEmpty()) ? "" : (String)ns_stack.peek();
  }

//  public void compileEmbeddedJavaFiles(HashMap javaFiles) {}
//  public Class embeddedJavaClass(String name) { return null; }

  public void cryptFile(boolean encrypt, String password, String infile, String outfile) {}

  public RuntimeSubContext newSubContext() { return null; }
  public RuntimeGlobalContext getGlobalContext() { return RT.DEFAULT_RTC; } // may not be set.

  ////////////////////////////////////////////////////////////////////
  // GUI event handling

  public GuiContext getGuiContext() { return null; }
  public GuiListenerBase getGuiHandler(String clsName) { return null; }

  ////////////////////////////////////////////////////////////////////
  // Resolve class names during compile-time, using system classpath.
  //

  private ArrayList importList = new ArrayList();

  public void addImport(String s) {
    importList.add(s);
  }

  public String handleSysClassName(String name) {
    int idx = name.indexOf('.');
    if (idx > 0)
      return name;

    int len = importList.size();
    for (int i=0; i<len; ++i) {
      String s = (String)importList.get(i);
      if (s.endsWith(".")) { // import ----.*
        try {
          Class.forName(s + name);
          return s + name;
        } catch(Exception e) {}
      } else if (s.endsWith("." + name)) { // import ----.---
        return s;
      }
    }

    // try java.lang.*
    try {
      Class.forName("java.lang." + name);
      return "java.lang." + name;
    } catch(Exception e) {}

    // try java.util.*
    try {
      Class.forName("java.util." + name);
      return "java.util." + name;
    } catch(Exception e) {}

    // try java.io.*
    try {
      Class.forName("java.io." + name);
      return "java.io." + name;
    } catch(Exception e) {}

    return name;
  }


  public Class getClass(String clsName) throws Exception {
    return RT.getSysClass(handleSysClassName(clsName));
  }

} // end of class ParsingContext.
