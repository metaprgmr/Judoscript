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

import com.judoscript.util.XMLWriter;


public class StmtBreakContinueResume extends StmtBase
{
  int type;
  String label;

  public StmtBreakContinueResume(int line, String label, int type) {
    super(line);
    this.type = type;
    this.label = label;
  }

  public void exec() throws ExceptionControl {
    switch(type) {
    case ExceptionControl.CTL_BREAK:
      throw (label != null) ? new ExceptionControl(ExceptionControl.CTL_BREAK,label) 
                            : ExceptionControl.BREAK;
    case ExceptionControl.CTL_CONTINUE:
      throw (label != null) ? new ExceptionControl(ExceptionControl.CTL_CONTINUE,label) 
                            : ExceptionControl.CONTINUE;
    case ExceptionControl.CTL_RESUME:
      throw ExceptionControl.RESUME;
    }
  }

  public String getName() {
    switch(type) {
    case ExceptionControl.CTL_BREAK:    return "break";
    case ExceptionControl.CTL_CONTINUE: return "continue";
    case ExceptionControl.CTL_RESUME:   return "resume";
    default:                            return "";
    }
  }

  public void dump(XMLWriter out) {
    out.openTag("StmtBreakContinue");
    out.tagAttr("type", getName());
    out.closeSingleTagLn();
  }

} // end of class StmtBreakContinueResume.
