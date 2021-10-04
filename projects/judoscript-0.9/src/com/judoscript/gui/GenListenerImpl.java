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
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Hashtable;


/**
 * This tool generates an implementation class for a number of listener interfaces.
 */
public class GenListenerImpl
{
  public static void main(String[] names) {
    int len = names.length;
    if (len < 2) {
      System.out.println("Usage: java com.judoscript.gui.GenListenerImpl implCls itf1 itf2 ...");
      System.exit(0);
    }

    try {
      Hashtable hash = new Hashtable(); // records method names to check on duplicate.
      String pkg = null;
      String implCls = names[0];
      int idx = implCls.lastIndexOf(".");
      if (idx > 0) {
        pkg = implCls.substring(0,idx);
        implCls = implCls.substring(idx+1);
      }
      PrintWriter pw = new PrintWriter(new FileWriter(implCls + ".java"));
      if (pkg != null)
        pw.println("package " + pkg + ";");
      pw.println();
      pw.println("public final class " + implCls);
      pw.println("  extends com.judoscript.gui.GuiListenerBase");
      pw.print  ("  implements ");
      int i;
      for (i=1; i<len; ++i) {
        if (i==1) pw.print(names[i]);
        else      pw.print("             " + names[i]);
        if (i<len-1) pw.println(",");
        else         pw.println();
      }
      pw.println("{");
      Class clsEventObject = Class.forName("java.util.EventObject");
      for (i=1; i<len; ++i) {
        Class cls = Class.forName(names[i]);
        pw.println("  // " + names[i] + " methods");
        Method[] mthds = cls.getDeclaredMethods();
        int cnt = (mthds == null) ? 0 : mthds.length;
        for (int j=0; j<cnt; ++j) {
          Class[] types = mthds[j].getParameterTypes();
          boolean isEventObject = false;
          if (types.length == 1) {
            Class c = types[0];
            while (true) {
              if (c == null) break;
              if (c == clsEventObject) { isEventObject = true; break; }
              c = c.getSuperclass();
            }
          }
          if (!isEventObject) continue;
          pw.println("  public void "+mthds[j].getName()+"("+types[0].getName()+" eo) {");
          pw.println("    event(eo.getSource(),\""+names[i]+"\",\""+mthds[j].getName()+"\");");
          pw.println("  }");
        }
        pw.println();
      }
      pw.println("} // end of class.");
      pw.flush();
      pw.close();
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

}
