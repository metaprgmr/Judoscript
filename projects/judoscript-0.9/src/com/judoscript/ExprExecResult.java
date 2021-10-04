/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 03-27-2005  JH   Inception.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript;

import java.util.Stack;
import java.util.ArrayList;
import java.io.BufferedReader;
import com.judoscript.bio.*;
import com.judoscript.util.XMLWriter;


public final class ExprExecResult extends ExprAnyBase
{
  StmtExec exec;

  public ExprExecResult(String cmd, boolean asLines) {
    exec = new StmtExec(-1);
    exec.setCmdLine(JudoUtil.parseString(cmd));
    exec.setAsIs(true);
    exec.setPipeOutPart(new ExecResultReader(asLines));
  }

  public int getType() { return ((ExecResultReader)exec.pipeIn).asLines ? TYPE_ARRAY : TYPE_STRING; }

  public Variable eval() throws Throwable {
    try {
      exec.exec();
    } catch(ExceptionControl.ExceptionReturn ret) {
      return ret.getReturnValue();
    }
    return ValueSpecial.NIL;
  }

  public void dump(XMLWriter out) {
    // TODO: dump().
  }

  public static class ExecResultReader extends StmtBase
  {
    boolean asLines;

    public ExecResultReader(boolean asLines) {
      super(-1);
      this.asLines = asLines;
    }

    public void exec() throws Throwable {
      Object ret;
      String line;
      BufferedReader br = RT.getPipeIn();
      if (asLines) {
        ArrayList list = new ArrayList();
        while ((line = br.readLine()) != null)
          list.add(line);
        ret = list;
      } else {
        StringBuffer sb = new StringBuffer();
        while ((line = br.readLine()) != null) {
          if (sb.length() > 0)
            sb.append('\n');
          sb.append(line);
        }
        ret = sb.toString();
      }
      throw new ExceptionControl.ExceptionReturn(JudoUtil.toVariable(ret));
    }

    public void dump(XMLWriter out) {
      // TODO: dump().
    }

  } // end of inner class ExecResultReader.

} // end of class ExprExecResult.
