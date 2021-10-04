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
import com.judoscript.util.XMLWriter;


public class StmtPrintTable extends StmtBase
{
  int    target; // or Err
  Expr   printer;
  Expr[] params;
  Expr   table;
  Expr   skip;
  Expr   limit;

  public StmtPrintTable(int line, int target, Expr printer, Expr table, Expr[] params,
                        Expr skip, Expr limit) {
    super(line);
    this.target = target;
    this.printer= printer;
    this.params = params;
    this.table  = table;
    this.skip   = skip;
    this.limit  = limit;
  }

  public void exec() throws Throwable {
    PrintWriter pw = null;
    StringWriter sw = null;
    Variable var = null;
    switch (target) {
    case RuntimeContext.PRINT_LOG:  pw = java.sql.DriverManager.getLogWriter(); break;
    case RuntimeContext.PRINT_ERR:  pw = RT.getErr(); break;
    case RuntimeContext.PRINT_OUT:  pw = RT.getOut(); break;
    case RuntimeContext.PRINT_PIPE: pw = RT.getPipeOut(); break;
    default:
      if (printer != null) {
        try {
          var = printer.eval();
          try { pw = (PrintWriter)var.getObjectValue();
          } catch(ClassCastException cce) {
          } catch(NullPointerException npe) {
          }
          if (pw == null) {
            sw = new StringWriter();
            pw = new PrintWriter(sw);
            if (var.isString())
              pw.print(var.getStringValue());
          }
        } catch(ClassCastException cce) {
          ExceptionRuntime.rte(RTERR_BAD_PRINT_TARGET,"Unable to print to such objects.",cce);
        }
      }
    }
    if (pw != null) {
      if (sw==null) {
        synchronized(StmtPrint.printLock) { doPrint(pw,table,params); }
        pw.flush();
      } else {
        doPrint(pw,table,params);
      }
    }
    if (sw != null) { // is a string
      pw.flush();
      RT.setVariable( ((AccessVar)printer).getName(), JudoUtil.toVariable(sw.toString()), 0 );
      pw.close();
    }
  }

  void doPrint(PrintWriter pw, Expr table, Expr[] params) throws Throwable {
    ExprTableData src = null;
    try { src = (ExprTableData)table.eval(); }
    catch(ClassCastException cce) {
      ExceptionRuntime.rte(RTERR_ILLEGAL_ARGUMENTS,
        "printTable requires a table data or result set.");
    }

    RT.setTableDataSource(src);
    try {
      int i = 0;
      if (skip != null) {
        i = (int)skip.getLongValue();
        while (i > 0 && src.nextRow()) ++i;
      }
      i = (limit==null) ? 10*1024*1024 : (int)limit.getLongValue();
      while(i-->0 && src.nextRow()) {
        for (int j=0; j<params.length; j++) {
          Expr exp = params[j];
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
          } else if (exp instanceof StmtPrint.Align) {
            // it would be cleaner to put this section into StmtPrint.Align but
            // in case of repease, we don't want to cache in memory.
            for (int reps=((StmtPrint.Align)exp).getReps(); reps>0; --reps) {
              pw.print(exp.getStringValue());
            }
          } else {
            pw.print(exp.getStringValue());
          }
        }
        pw.println();
      }
    } finally {
      RT.clearTableDataSource();
    }
  }

  public void dump(XMLWriter out) {
    out.openTag("StmtPrintTable");
    String targetName;
    switch(target) {
    case RuntimeContext.PRINT_ERR:  targetName = "err";  break;
    case RuntimeContext.PRINT_LOG:  targetName = "log";  break;
    case RuntimeContext.PRINT_PIPE: targetName = "pipe"; break;
    default:                        targetName = "out";  break;
    }
    out.tagAttr("target",targetName);
    out.closeTag();
    out.openTag("table");
    table.dump(out);
    out.endTagLn();
    dumpArguments(out,params);
    out.endTagLn();
  }

} // end of class StmtPrintTable.

