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
import java.util.HashMap;
import org.apache.commons.lang.StringUtils;
import com.judoscript.RT;


/**
 * A GUI listener is either an AWT or Swing or user-defined listener,
 * for example, "java.awt.event.MouseListener". For AWT and Swing listeners,
 * a nick name is used to represent that long name; in the example abolve,
 * "Mouse" works as a nick name.
 *
 * This class has a HashMap that registers GUI listener interfaces to
 * their implementation classes. The key is either the listener class name
 * or a nick name; the value is String[2] of the interface name and its
 * implementation class name.
 *
 * General Java event/listener can be used in JudoScript. But if you want
 * to use JudoScript's guiEvents {} statement, you need to register the
 * listener interface along with an implementation class.
 */
public class GuiListenerCollection
{
  static HashMap hash = new HashMap();

  public static void registerImpl(String lsnrNickname, String lsnrItf, String implCls) {
    String[] sa = new String[]{ lsnrItf, implCls };
    hash.put(lsnrItf, sa);
    if (lsnrNickname != null)
      hash.put(lsnrNickname, sa);
  }

  public static String getListenerInterface(String lsnrNickname) {
    String[] sa = (String[])hash.get(lsnrNickname);
    return (sa == null) ? null : sa[0];
  }

  public static String getListenerImplClass(String lsnrNickname) {
    String[] sa = (String[])hash.get(lsnrNickname);
    return (sa == null) ? null : sa[1];
  }

  public static boolean isEventMsgValid(String lsnr, String msg) {
    String itf = getListenerInterface(lsnr);
    if (itf == null) return false;
    try { // Use reflection to check on the method name for 'msg'.
      Class cls = RT.getSysClass(itf);
      Method[] mthds = cls.getDeclaredMethods();
      int len = (mthds == null) ? 0 : mthds.length;
      if (StringUtils.isBlank(msg) && (len > 1))
        return false;
      for (int i = 0; i < len; i++) {
        // looking for same name with 1 parameter
        if (mthds[i].getName().equals(msg) && (mthds[i].getParameterTypes().length == 1))
          return true;
      }
    } catch(Exception e) {}
    return false;
  }


  /////////////////////////////////////////////////////
  // Register these AWT, Swing and Bean Listeners

  static {
    registerAwtSwingDefaultImpl("Action",          0);
    registerAwtSwingDefaultImpl("Adjustment",      0);
    registerAwtSwingDefaultImpl("AWTEvent",        0);
    registerAwtSwingDefaultImpl("Component",       0);
    registerAwtSwingDefaultImpl("Container",       0);
    registerAwtSwingDefaultImpl("Focus",           0);
    registerAwtSwingDefaultImpl("HierarchyBounds", 0);
    registerAwtSwingDefaultImpl("Hierarchy",       0);
    registerAwtSwingDefaultImpl("InputMethod",     0);
    registerAwtSwingDefaultImpl("Item",            0);
    registerAwtSwingDefaultImpl("Key",             0);
    registerAwtSwingDefaultImpl("Mouse",           0);
    registerAwtSwingDefaultImpl("MouseMotion",     0);
    registerAwtSwingDefaultImpl("Text",            0);
    registerAwtSwingDefaultImpl("Window",          0);
    registerAwtSwingDefaultImpl("Ancestor",        1);
    registerAwtSwingDefaultImpl("Caret",           1);
    registerAwtSwingDefaultImpl("CellEditor",      1);
    registerAwtSwingDefaultImpl("Change",          1);
    registerAwtSwingDefaultImpl("Hyperlink",       1);
    registerAwtSwingDefaultImpl("InternalFrame",   1);
    registerAwtSwingDefaultImpl("ListData",        1);
    registerAwtSwingDefaultImpl("ListSelection",   1);
    registerAwtSwingDefaultImpl("MenuDragMouse",   1);
    registerAwtSwingDefaultImpl("MenuKey",         1);
    registerAwtSwingDefaultImpl("Menu",            1);
    registerAwtSwingDefaultImpl("PopupMenu",       1);
    registerAwtSwingDefaultImpl("TableColumnModel",1);
    registerAwtSwingDefaultImpl("TableModel",      1);
    registerAwtSwingDefaultImpl("TreeExpansion",   1);
    registerAwtSwingDefaultImpl("TreeModel",       1);
    registerAwtSwingDefaultImpl("TreeSelection",   1);
    registerAwtSwingDefaultImpl("TreeWillExpand",  1);
    registerAwtSwingDefaultImpl("UndoableEdit",    1);
    registerAwtSwingDefaultImpl("PropertyChange",  2);
    registerAwtSwingDefaultImpl("VetoableChange",  2);
  }
  private static void registerAwtSwingDefaultImpl(String nickname, int type) {
    String pkgName;
    switch(type) {
    case 0:  pkgName = "java.awt.event.";    break;
    case 1:  pkgName = "javax.swing.event."; break;
    default: pkgName = "java.beans.";        break;
    }
    registerImpl(nickname, pkgName+nickname+"Listener", "com.judoscript.gui.AwtSwingListeners");
  }

} // end of class GuiListenerCollection.
