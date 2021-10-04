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


package com.judoscript.gui;

import java.lang.reflect.Method;
import java.util.EventObject;
import java.util.Hashtable;
import com.judoscript.*;

/**
 *  This singleton keeps all the event handlers at runtime.
 *  For variable-based event handlers, a key of the object class name plus
 *  its hashcode, the event name and message is used to reference its handler.
 *  That's why singleton is good enough for any components/events/messages.
 *
 *  All GUI events are funneled to handle() for dispatching.
 */
public final class GuiContext implements Consts
{
  RuntimeContext RTC;
  final Hashtable eventHandlers = new Hashtable(); // varName:eventName[:msgName] => EventHandler
                                      // where varName is the variable Java Object.toString().

  public GuiContext(RuntimeContext rtc) { RTC = rtc; }

  /**
   * This method does two key things:
   * 1. Register the handler (statements) for this component, event and message.
   * 2. Add the listener to the variable (which should be a component that accepts a listener);
   */
  public void addHandler(Object var, String eventName, String msgName, StmtListStmt hdlr)
                        throws Exception
  {
    // Step 1. register the handler for it
    String key = getObjectID(var) + ":" + eventName + ":" + msgName;
    eventHandlers.put(key,hdlr);

    // Step 2. add the listener to the variable
    String itf = GuiListenerCollection.getListenerInterface(eventName);
    // to get the interface name w/o package path, and add "add" to form a method name.
    if (itf == null) return;
    int idx = itf.lastIndexOf(".");
    String mthdName = "add" + ((idx > 0) ? itf.substring(idx+1) : itf);
    Method[] mthds = var.getClass().getMethods();
    int len = 0;
    try { len = mthds.length; } catch(Exception e) {}
    boolean succ = false;
    int i;
    for (i=0; i<len; i++) {
      if (mthds[i].getName().equals(mthdName)) {
        Class[] paramTypes = mthds[i].getParameterTypes();
        int numParams = 0;
        try { numParams = paramTypes.length; } catch(Exception e) {}
        try {
          if ((numParams == 1) && RT.getSysClass(itf).equals(paramTypes[0])) {
            succ = true;
            break;
          }
        } catch(Exception e) { break; }
      }
    }
    if (!succ)
      ExceptionRuntime.rte(RTERR_ILLEGAL_ARGUMENTS,
                "'" + var.getClass().getName() + "' does not have '" + mthdName + "()'.");
    // now invoke it to add the listner for the variable
    mthds[i].invoke(var, new Object[]{ RT.getGuiEventHandler(eventName) });
  }

  // The EventObject.getSource() is used to identify the variable, which should be a Component.
  public void handle(EventObject eo, String lsnr, String msg) throws Throwable {
    RuntimeContext rtc = RT.curCtxt();
    if (rtc == null) RT.pushContext(RTC);

    Object src = eo.getSource();
    String key = getObjectID(src) + ":" + lsnr + ":" + msg;
    StmtListStmt hdlr = (StmtListStmt)eventHandlers.get(key);
    if (hdlr != null) {
      try {
        RT.setLocalVariable(THIS_NAME, JudoUtil.toVariable(eo), 0);
        hdlr.exec();
      } catch(ExceptionControl ce) {} // just continue
    }
  }

  public static String getObjectID(Object o) {
    return o.getClass().getName() + "~" + o.hashCode();
  }

} // end of class GuiContext.
