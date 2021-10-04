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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;
import org.apache.commons.logging.Log;
import com.judoscript.util.Lib;
import com.judoscript.util.XMLWriter;


public class StmtPrint extends StmtBase
{
  public static final String printLock = "printLock";

  static final int LOGLEVEL_TRACE = 1; // see org.apache.commons.logging.Log.
  static final int LOGLEVEL_DEBUG = 2;
  static final int LOGLEVEL_INFO  = 3;
  static final int LOGLEVEL_WARN  = 4;
  static final int LOGLEVEL_ERROR = 5;
  static final int LOGLEVEL_FATAL = 6;

  boolean isFlush;
  boolean printLine;
  int     target; // or Err
  int     userLogLevel;
  Expr    logException;
  Expr    printer;
  Expr[]  params;
  Expr    cond;

  public StmtPrint(int line,
                   boolean printLn,
                   boolean flush,
                   int target,
                   String logLevel,
                   Expr logExcpt,
                   Expr printer,
                   Expr[] params,
                   Expr cond) {
    super(line);
    this.isFlush = flush;
    this.printLine = printLn;
    this.target = target;
    this.printer = printer;
    this.params = params;
    this.cond = cond;
    this.logException = logExcpt;

    if (logLevel == null) {
      userLogLevel = LOGLEVEL_INFO;
    } else {
      logLevel = logLevel.toLowerCase();
      if (logLevel.equals("trace"))      userLogLevel = LOGLEVEL_TRACE;
      else if (logLevel.equals("debug")) userLogLevel = LOGLEVEL_DEBUG;
      else if (logLevel.equals("info"))  userLogLevel = LOGLEVEL_INFO;
      else if (logLevel.equals("warn"))  userLogLevel = LOGLEVEL_WARN;
      else if (logLevel.equals("error")) userLogLevel = LOGLEVEL_ERROR;
      else if (logLevel.equals("fatal")) userLogLevel = LOGLEVEL_FATAL;
    }
  }

  public void exec() throws Throwable {
    if ((cond != null) && !cond.getBoolValue())
      return;

    int len = (params==null) ? 0 : params.length;
    PrintWriter pw = null;
    StringWriter sw = null;
    switch (target) {
    case RuntimeContext.PRINT_LOG:  break;
    case RuntimeContext.PRINT_ERR:  pw = RT.getErr(); break;
    case RuntimeContext.PRINT_OUT:  pw = RT.getOut(); break;
    case RuntimeContext.PRINT_PIPE: pw = RT.getPipeOut(); break;
    default:
      if (printer != null) {
        try {
          Variable var = printer.eval();
          try {
            Object o = var.getObjectValue();
            if (o instanceof PrintWriter)
              pw = (PrintWriter)o;
          } catch(ClassCastException cce) {
          } catch(NullPointerException npe) {
          }
          if (pw == null) {
            sw = new StringWriter();
            pw = new PrintWriter(sw);
            if (var.isString()) // save the current string content.
              pw.print(var.getStringValue());
          }
        } catch(ClassCastException cce) {
          ExceptionRuntime.rte(RTERR_BAD_PRINT_TARGET,"Unable to print to such an object.", cce);
        }
      }
    }

    if (target == RuntimeContext.PRINT_LOG) {
      Throwable t = null;
      if (logException != null)
        t = (Throwable)logException.getObjectValue();
      Log log = RT.userLogger;
      sw = new StringWriter();
      doPrint(new PrintWriter(sw), params, len);
      String x = sw.toString();
      switch(userLogLevel) {
      case LOGLEVEL_TRACE: if (t==null) log.trace(x); else log.trace(x, t); return;
      case LOGLEVEL_DEBUG: if (t==null) log.debug(x); else log.debug(x, t); return;
      case LOGLEVEL_WARN:  if (t==null) log.warn(x);  else log.warn(x, t);  return;
      case LOGLEVEL_ERROR: if (t==null) log.error(x); else log.error(x, t); return;
      case LOGLEVEL_FATAL: if (t==null) log.fatal(x); else log.fatal(x, t); return;
      case LOGLEVEL_INFO:
      default:             if (t==null) log.info(x);  else log.info(x, t);  return;
      }
    }

    if (pw != null) {
      if ((sw==null) && (printLine||isFlush)) {
        synchronized(printLock) { doPrint(pw, params, len); }
        if (printLine)
          pw.println();
        pw.flush();
      } else {
        doPrint(pw, params, len);
      }
    }
    if (sw != null) { // is a string
      pw.flush();
      RT.setVariable( ((AccessVar)printer).getName(), JudoUtil.toVariable(sw.toString()), 0 );
      pw.close();
    }
  }

  void doPrint(PrintWriter pw, Expr[] params, int len) throws Throwable {
    for (int i=0; i<len; i++) {
      Expr exp = params[i];
      if (exp instanceof ExprConcat) {        // directly print all components rather
        ExprConcat concat = (ExprConcat)exp;  // than concat them in memory first.
        Iterator iter = concat.getOperands().iterator();
        while (iter.hasNext())
          pw.print(iter.next());
/*
        pw.print(concat.expr.getStringValue());
        int jlen = (concat.parts==null) ? 0 : concat.parts.length;
        for (int j=0; j<jlen; ++j)
          pw.print(concat.parts[j].getStringValue());
*/
      } else if (exp instanceof Align) {
        // it would be cleaner to put this section into Align but
        // in case of repeats, we don't want to cache in memory.
        for (int reps=((Align)exp).getReps(); reps>0; --reps) {
          pw.print(exp.getStringValue());
        }
      } else {
        pw.print(exp.getStringValue());
      }
    }
  }

  public void dump(XMLWriter out) {
    out.openTag(printLine ? "StmtPrintLn" : "StmtPrint");
    String targetName;
    switch(target) {
    case RuntimeContext.PRINT_ERR:  targetName = "err";  break;
    case RuntimeContext.PRINT_LOG:  targetName = "log";  break;
    case RuntimeContext.PRINT_PIPE: targetName = "pipe"; break;
    default:                        targetName = "out";  break;
    }
    out.tagAttr("target", targetName);
    if (cond != null) out.tagAttr("cond", "true");
    out.closeTag();
    dumpArguments(out, params);
    out.endTagLn();
  }

  public static class Align extends ExprAnyBase
  {
    Expr subj;
    int  align;
    Expr width;
    boolean force;
    Expr repeating;

    public Align(Expr subj, int align, Expr width, Expr repeating, boolean force) {
      this.subj  = subj;
      this.align = align;
      this.width = width;
      this.repeating = repeating;
      this.force = force;
    }

    public Variable eval() throws Throwable { return JudoUtil.toVariable(getStringValue()); }

    public int getReps() {
      try { return (int)repeating.getLongValue(); } catch(Throwable e) {}
      return 1;
    }

    // This is the only method to be called.
    public String getStringValue() throws Throwable {
      try {
        if (width == null)
          return subj.getStringValue(); // for "repeat".
        String x;
        int intPart = 0;
        int decPart = -1;
        Variable widthVal = width.eval();
        if (widthVal.isDouble()) {
          intPart = (int)Math.floor(widthVal.getDoubleValue());
          x = widthVal.getStringValue();
          int idx = x.indexOf('.');
          if (idx >= 0) {
            x = x.substring(idx+1);
            try { decPart = Integer.parseInt(x); } catch(Exception e) {}
          }
        } else {
          intPart = (int)widthVal.getLongValue();
        }
        if (decPart > 0) { // number xxx.xx format -- no left/right/center
          String sx = Lib.decimalDigitsAsString(subj.getDoubleValue(), decPart);
          int idx = sx.indexOf('.');
          if (idx < 0) {
            if ((sx.length() > intPart) && force) {
              sx = "*" + sx.substring(sx.length()-intPart+1);
            } else {
              for (int i=sx.length(); i<intPart; ++i)
                sx = " " + sx;
            }
            sx += ' ';
            for (idx=0; idx<decPart; ++idx)
              sx += ' ';
            return sx;
          } else {
            if (idx <= intPart)
              for (int i=idx; i<intPart; ++i)
                sx = " " + sx;
            else if (force)
              sx = "*" + sx.substring(idx-intPart+1);
            for (int i=sx.length()-sx.indexOf('.'); i<=decPart; ++i)
              sx += ' ';
            return sx;
          }
        } else { // general width format
          x = subj.getStringValue();
          boolean isNum = subj.isNumber();
          switch(align) {
          case 3:  return Lib.centerAlign(x, intPart, force, isNum);
          case 2:  return Lib.rightAlign(x, intPart, force, isNum);
          default: return Lib.leftAlign(x, intPart, force, isNum);
          }
        }
      } catch(ExceptionSpecialValue esv) { return esv.getResult().getStringValue(); }
    }

    public void dump(XMLWriter out) {
      out.openTag("StmtPrint.Align");
      out.tagAttr("align", getAlignName());
      out.tagAttr("force", ""+force);
      out.closeTag();
      subj.dump(out);
      width.dump(out);
      out.endTagLn();
    }

    String getAlignName() {
      switch(align) {
      case 3:  return "center";
      case 2:  return "right";
      default: return "left";
      }
    }
  }

} // end of class StmtPrint.

