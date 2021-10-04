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


package com.judoscript.studio;

import java.io.*;
import java.util.Vector;
import org.apache.commons.lang.StringUtils;
import com.judoscript.*;
import com.judoscript.parser.helper.ParserHelper;
import com.judoscript.util.Lib;

public class ConsoleDebugger extends Debugger
{
  private static void doHelp() {
    out.println("  ?            - print this help screen.");
    out.println("  g            - start the debugging session.");
    out.println("  w var_name   - watch the variable (on each break point).");
    out.println("  v var_name   - view the variable now.");
    out.println("  t var_name   - show the variable value's type.");
    out.println("  W            - list all the variables being watched.");
    out.println("  bp line_num  - set break point at line_num.");
    out.println("  bp -line_num - remove break point at line_num.");
    out.println("  BP           - list all the break point line numbers.");
    out.println("  n            - step over.");
    out.println("  i            - step into.");
    out.println("  c            - continue.");
//    out.println("  print [start_line] [,] [end_line] - print the current line or a range.");
//    out.println("  loadpref [file] - load the preference file.");
//    out.println("  savepref [file] - save the preference to the file.");
    out.println("  q or x       - quit the debugger.");
  }

  static final int CMD_ERROR     = 0;
  static final int CMD_GO        = 1;
  static final int CMD_WATCH     = 2;
  static final int CMD_VIEW      = 3;
  static final int CMD_TYPE      = 4;
  static final int CMD_LISTWACTH = 5;
  static final int CMD_BP        = 6;
  static final int CMD_LISTBP    = 7;
  static final int CMD_NEXT      = 8;
  static final int CMD_INTO      = 9;
  static final int CMD_CONT      = 10;
  static final int CMD_STOP      = 11;
  static final int CMD_PRINT     = 12;
  static final int CMD_HELP      = 13;
  static final int CMD_LOADPREF  = 14;
  static final int CMD_SAVEPREF  = 15;
  static final int CMD_QUIT      = 16;
  static final int CMD_EXIT      = 17;

  static final String[] cmds = {
  /* CMD_ERROR     */ "ERROR",
  /* CMD_GO        */ "g",
  /* CMD_WATCH     */ "w",
  /* CMD_VIEW      */ "v",
  /* CMD_TYPE      */ "t",
  /* CMD_LISTWACTH */ "W",
  /* CMD_BP        */ "bp",
  /* CMD_LISTBP    */ "BP",
  /* CMD_NEXT      */ "n",
  /* CMD_INTO      */ "i",
  /* CMD_CONT      */ "c",
  /* CMD_STOP      */ "stop",
  /* CMD_PRINT     */ "print",
  /* CMD_HELP      */ "?",
  /* CMD_LOADPREF  */ "loadpref",
  /* CMD_SAVEPREF  */ "savepref",
  /* CMD_QUIT      */ "q",
  /* CMD_EXIT      */ "x"
  };

  static PrintStream out = System.out;
  static PrintStream err = System.err;
  String scriptName;
  String[] args;
  Vector watches = new Vector();
  Thread debuggeeThread = null;
  Script script;

  ConsoleDebugger(String[] args) {
    VersionInfo.printHeader("JudoScript Console Debugger");

    scriptName = null;
    this.args = null;
    if (args.length > 0) {
      scriptName = args[0];
      if (args.length > 1) {
        this.args = new String[args.length-1];
        System.arraycopy(args, 1, this.args, 0, args.length-1);
      }
    }
    try { interpret(); } catch(Exception e) { e.printStackTrace(); }
  }

  /**
   * The driving force for the debugger.
   */
  public void interpret() throws Exception {
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    String line;
    int    intParam1;
    int    intParam2;
    String textParam;
    showPrompt();
loop0:
    while ( (line=br.readLine()) != null) {
      if (line.trim().length() > 0) {
        intParam1 = intParam2 = -1;
        textParam = null;
        int cmd;
loop:   for (cmd=1; cmd<cmds.length; cmd++) {
          if (line.startsWith(cmds[cmd])) {
            String x = line.substring(cmds[cmd].length()).trim();
            switch(cmd) {
            case CMD_GO:        doGo();        break loop;
            case CMD_WATCH:     doWatch(x);    break loop;
            case CMD_VIEW:      doView(x);     break loop;
            case CMD_TYPE:      doType(x);     break loop;
            case CMD_LISTWACTH: doListWatch(); break loop;
            case CMD_BP:        doBP(x);       break loop;
            case CMD_LISTBP:    doListBP();    break loop;
            case CMD_NEXT:      doNext();      break loop;
            case CMD_INTO:      doInto();      break loop;
            case CMD_CONT:      doCont();      break loop;
            case CMD_STOP:      doStop();      break loop;
            case CMD_PRINT:     doPrint(x);    break loop;
            case CMD_HELP:      doHelp();      break loop;
            case CMD_LOADPREF:  doLoadPref();  break loop;
            case CMD_SAVEPREF:  doSavePref();  break loop;
            case CMD_EXIT:
            case CMD_QUIT:      break loop0;
            }
          }
        }
        if (cmd >= cmds.length) {
          out.println("Unknown command: " + line);
        }
      }
      if (debuggeeThread == null) {
        showPrompt();
      }
    }
    System.exit(0);
  }

  private void doGo() {
    try {
    	String enc = null;
      script = ParserHelper.parse(scriptName, scriptName, JudoUtil.findFile(scriptName, enc), enc, 0, false);;
    } catch(Exception e) {
      out.println(e.getMessage());
      return;
    }
    Runnable r = new Runnable() {
      public void run() {
        init(args,script);
        start();
      }
    };
    debuggeeThread = new Thread(r);
    debuggeeThread.start();
    showPrompt();
  }

  private void doWatch(String x) {
    if (StringUtils.isBlank(x))
      out.println("Error: 'watch' takes a variable or data member name.");
    else
      watches.addElement(x);
  }

  private void doView(String x) {
    if (StringUtils.isBlank(x)) {
      out.println("Error: 'view' takes a variable or data member name.");
    } else {
      try {
        Variable v = watch(x);
        out.println(x + " = " + v.getStringValue());
      } catch(Throwable e) {
        out.println("Exceptioned: " + e.getMessage());
      }
    }
  }

  private void doType(String x) {
    if (StringUtils.isBlank(x)) {
      out.println("Error: 'type' takes a variable or data member name.");
    } else {
      try {
        Variable v = watch(x);
        out.println(x + "'s type is " + v.getTypeName());
      } catch(Throwable e) {
        out.println("Exceptioned: " + e.getMessage());
      }
    }
  }

  private void doListWatch() {
    out.println("" + watches.size() + " watch(es):");
    for (int i=0; i<watches.size(); ++i)
      out.println(watches.elementAt(i));
  }

  private void doBP(String x) {
    try {
      int i = Integer.parseInt(x);
      if (i>0) setBP(i);
      else removeBP(-i);
      return;
    } catch(NullPointerException npe) {
    } catch(NumberFormatException nfe) {
    }
    out.println("Error: invalid line number for command 'bp'.");
  }

  private void doListBP() {
    int[] ia = BPs.intKeysSorted();
    out.println("" + ia.length + " break points:");
    for (int i=0; i<ia.length; ++i)
      out.println("line " + ia[i]);
  }

  private void doNext() {
    synchronized(debuggeeThread) {
      setSingleStep(true);
      setStepInto(false);
      debuggeeThread.notify();
    }
  }

  private void doInto() {
    synchronized(debuggeeThread) {
      setSingleStep(true);
      setStepInto(true);
      debuggeeThread.notify();
    }
  }

  private void doCont() {
    synchronized(debuggeeThread) {
      setSingleStep(false);
      setStepInto(false);
      debuggeeThread.notify();
    }
  }

  private void doStop() {
    debuggeeThread.stop();
    debuggeeThread = null;
  }

  private void doPrint(String x) {
    // TODO
  }

  private void doLoadPref() {
    // TODO
  }

  private void doSavePref() {
    // TODO
  }

  static void showPrompt() {
    out.print("> ");
    out.flush();
  }

  /**
   * Debugger required method.
   */
  protected void breakPoint(Stmt stmt) {
    synchronized(debuggeeThread) {
      out.println("Stopped at line " + stmt.getLineNumber());
      for (int i=0; i<watches.size(); ++i) {
        String x = (String)watches.elementAt(i);
        String v = "? not available ?";
        try { v = watch(x).getStringValue(); } catch(Throwable e) {}
        out.print(Lib.leftAlign(x, 20, false, ' ', null));
        out.print(" = ");
        out.println(v);
      }
      out.println();
      out.flush();
      showPrompt();
      boolean waiting = true;
      while (waiting) {
        try {
          debuggeeThread.wait();
          waiting = false;
        } catch(InterruptedException ie) {}
      }
    }
  }

  public void finish() {
    debuggeeThread = null;
    out.print("\nProgram finished running.\n> ");
    out.flush();
  }

  public static void main(String[] args) { new ConsoleDebugger(args); }

} // end of class ConsoleDebugger.


