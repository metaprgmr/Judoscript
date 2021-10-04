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

import java.util.EventObject;
import org.apache.commons.lang.StringUtils;
import com.judoscript.RT;
import com.judoscript.ExceptionExit;

public class GuiListenerBase
{
  GuiContext ctxt = null;

  public void setGuiContext(GuiContext c) { ctxt = c; }

  protected final void event(EventObject eo, String lsnr, String msg) {
    try {
      ctxt.handle(eo,lsnr,msg);
    } catch(ExceptionExit ee) {
      String s = ee.getMessage();
      if (StringUtils.isNotBlank(s))
        RT.logger.info("From GuiListener: Exits with " + s);
      System.exit(0);
    } catch(Throwable e) {
      RT.logger.error("GuiListenerBase exception.", e);
      e.printStackTrace();
    }
  }

} // end of class GuiListenerBase.

