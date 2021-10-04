/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 03-17-2002  JH   Initial open source release.
 * 08-08-2002  JH   Implemented get/setCurrentDefaultNS().
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript;

import java.util.*;
import org.apache.commons.lang.StringUtils;
import com.judoscript.gui.*;
import com.judoscript.util.*;


public class RuntimeSubContext extends RuntimeContext
{
  private RuntimeGlobalContext global;
  Stack ns_stack = new Stack();

  public RuntimeSubContext(RuntimeGlobalContext rgc) {
    global = rgc;
    try {
      pushFrame(rgc.getRootFrame(), (Stmt[])null);
    } catch(Throwable e) {}
  }

  public RuntimeGlobalContext getGlobalContext() { return global; }

  public void setCurrentDefaultNS(String namespace) {
    if (StringUtils.isNotBlank(namespace))
      ns_stack.push(namespace);
    else {
      try { ns_stack.pop(); }
      catch(Exception e) {}
    }
  }

  public String getCurrentDefaultNS() {
    return (ns_stack.isEmpty()) ? "" : (String)ns_stack.peek();
  }

//  public void compileEmbeddedJavaFiles(HashMap javaFiles) throws ExceptionRuntime {
//    global.compileEmbeddedJavaFiles(javaFiles);
//  }
//  public Class embeddedJavaClass(String name) { return global.embeddedJavaClass(name); }

  ////////////////////////////////////////////////////////////////////
  // GUI event handling

  public GuiContext getGuiContext() { return global.getGuiContext(); }
  public GuiListenerBase getGuiHandler(String clsName) throws ExceptionRuntime {
    return global.getGuiHandler(clsName);
  }

} // end of class RuntimeSubContext.
