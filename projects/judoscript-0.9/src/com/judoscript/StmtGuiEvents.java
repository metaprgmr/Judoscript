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

import java.util.*;
import com.judoscript.gui.*;
import com.judoscript.util.*;


public final class StmtGuiEvents extends StmtBase
{
  public HashMap eventHandlers = new HashMap();
                 // { varName : eventName : msgName } => StmtListStmt
                 // varName is like '$alfa'.

  public StmtGuiEvents(int lineNo) { super(lineNo); }

  public void exec() throws Throwable {
    int i;
    Iterator keys = eventHandlers.keySet().iterator();
    GuiContext gctxt = RT.getGuiContext();
    while (keys.hasNext()) {
      try {
        Triplet eMsg = (Triplet)keys.next();
        StmtListStmt h = (StmtListStmt)eventHandlers.get(eMsg);
        String event = (String)eMsg.o2;
        Collection mthds = null;
        if (eMsg.o3 instanceof Collection)
          mthds = (Collection)eMsg.o3;
        Object obj = (((Expr)eMsg.o1).eval()).getObjectValue();
        if (mthds == null) { // a single event case
          gctxt.addHandler(obj, event, (String)eMsg.o3, h);
        } else { // multiple messages sharing a same handler
          Iterator iter = mthds.iterator();
          while (iter.hasNext())
            gctxt.addHandler(obj, event, (String)iter.next(), h);
        }
      } catch(Exception e) {
        ExceptionRuntime.rte(RTERR_JAVA_METHOD_CALL,
          "Invalid AWT/Swing components to set event listener.",e);
      }
    }
  }

  // TODO: lineNo not used here yet.
  public void addHandler(int lineNo, Triplet varEventMsg, StmtListStmt eh) {
    eventHandlers.put(varEventMsg,eh);
  }
  
  public void dump(XMLWriter out) {
    out.simpleTagLn("StmtGuiEvents");
    // TODO: dump().
    out.endTagLn();
  }

} // end of class StmtGuiEvents.
