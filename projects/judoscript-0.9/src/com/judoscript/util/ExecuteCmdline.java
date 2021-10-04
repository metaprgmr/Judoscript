/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 03-17-2002  JH   Initial open source release.
 * 01-18-2003  JH   Support input and output blocks simultaneously.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/

package com.judoscript.util;

import java.io.*;
import java.util.*;

/**
 * This class takes a command line to run either one program
 * or more programs that form a chain of pipes.
 *<p>
 * A single program, whether in the pipe or not, can take
 * redirections: input redirection is by < file; output is:
 *<pre>
 *   -------------------------------------------------
 *   Output Redirection        Write To     Append To
 *   -------------------------------------------------
 *   output:                   > file       >> file
 *                             1> file      1>> file
 *   error:                    2> file      2>> file
 *   error to output:          2>&1
 *   error to output to file:  2>&1 file    2>>&1 file
 *   output to error:          1>&2
 *   output to error to file:  1>&2 file    1>>&2 file
 *   -------------------------------------------------
 *</pre>
 * Can have output to one file and error to another.
 * When >& occurs, this should be the only output redirection.
 */

public class ExecuteCmdline
{
  private static final int MERGE_FLAG  = 0x08;
  private static final int APPEND_FLAG = 0x04;
  private static final String OUT2ERR  = "1>&2";
  private static final String ERR2OUT  = "2>&1";

  Program[]       programs;
  LinePrintWriter firstOut = null;
  Process         lastProc = null;
  BufferedReader  lastIn   = null;

  public ExecuteCmdline(String cmdline) throws IllegalArgumentException {
    StringTokenizer st = new StringTokenizer(cmdline, "|");
    int cnt = st.countTokens();
    programs = new Program[cnt];
    for (int i=0; i<cnt; i++)
      programs[i] = new Program(st.nextToken(), (i==0), (i==cnt-1));
  }

  /**
   *@param wait if true, and extInput and extOutput are both false, then it waits.
   *@param extInput if true, the caller wants to take over the input to the first program.
   *@param extOutput if true, the caller wants to take over the output to the last program.
   */
  public int exec(String[] env, String workdir, BufferedReader instrm,
                  LinePrintWriter  outstrm, LinePrintWriter errstrm,
                  boolean wait, int timeout, boolean extInput, boolean extOutput)
             throws IOException, IllegalArgumentException, InterruptedException
  {
    // some sanity check
    if ((instrm != null) && (programs[0].inFile != null))
      throw new IllegalArgumentException(
                  "Input stream clashes with the input file on the command line.");

    // 'instrm' is set to the first program.
    // 'outstrm' is for the very last program;
    // 'errstrm' typically for all the programs, except those
    // that redirect error output to files or output streams.

    ExecutableWatchdog wd = null;
    if (wait && (timeout > 0))
      wd = new ExecutableWatchdog(timeout);

    // Each executable is established and started.
    // We start from the back so the whole chain is pulled rather
    // than pushed, thus alleviate potential input overflow.

    Executable lastEx = null;

    // The intermediate executables in the pipe chain:
    // 1) stdout is always piped to the next; it may also be written to a file.
    // 2) stderr is written to errstrm or a file, if not merged.
    // 3) if stderr and strout are merged, they are always piped to the next.
    //    optionally they can write to a file as well.

    BufferedReader progIn = null;
    if (instrm != null)
      progIn = instrm;
    else if (programs[0].inFile != null)
      progIn = new BufferedReader(new FileReader(programs[0].inFile));
    int len = programs.length;
    LinePrintWriter pipeOutDst = outstrm;

    for (int i=len-1; i>=0; --i) {
      Program    pgm = programs[i];
      Executable ex = new Executable(pgm.cmd, workdir, env);

      PumpHandler pumpOut, pumpErr;
      BufferedReader pipeOutSrc = ex.getInput();
      BufferedReader pipeErrSrc = ex.getError();
      LinePrintWriter fileOutDst = null;
      boolean closeOut = true;
      boolean closeErr = true;

      if (OUT2ERR.equals(pgm.outFile)) {
        if (pgm.errFile != null) {
          fileOutDst = new LinePrintWriter(new FileWriter(pgm.errFile,pgm.errAppend));
          if (i==len-1) {
            pipeOutDst = fileOutDst;
            fileOutDst = null;
          }
        } else if (i==len-1) {
          pipeOutDst = errstrm;
          closeErr = false;
          closeOut = false;
        }
        pumpOut = new PumpHandler(pipeOutSrc,pipeErrSrc,pipeOutDst,fileOutDst,closeOut,closeErr);
        pumpErr = null;
      } else if (ERR2OUT.equals(pgm.errFile)) {
        if (pgm.outFile != null) {
          fileOutDst = new LinePrintWriter(new FileWriter(pgm.outFile,pgm.outAppend));
          if (i==len-1) {
            pipeOutDst = fileOutDst;
            fileOutDst = null;
          }
        } else if (i == len-1) {
          closeOut = false;
          closeErr = false;
        }
        pumpOut = new PumpHandler(pipeOutSrc,pipeErrSrc,pipeOutDst,fileOutDst,closeOut,closeErr);
        pumpErr = null;
      } else { // not a merge
        if (pgm.outFile != null) {
          fileOutDst = new LinePrintWriter(new FileWriter(pgm.outFile,pgm.outAppend));
          if (i==len-1) {
            pipeOutDst = fileOutDst;
            fileOutDst = null;
          }
        } else if (i==len-1) {
          closeOut = false;
        }
        LinePrintWriter pipeErrDst = errstrm;
        if (pgm.errFile != null) {
          pipeErrDst = new LinePrintWriter(new FileWriter(pgm.errFile,pgm.errAppend));
        } else if (i == len-1) {
          closeErr = false;
        }
        pumpOut = new PumpHandler(pipeOutSrc,null,pipeOutDst,fileOutDst,closeOut,closeErr);
        pumpErr = new PumpHandler(pipeErrSrc,null,pipeErrDst,null,closeOut,closeErr);
      }
      if ((i==len-1) && extOutput)
        pumpOut = null;

      if (i==0) {
        if (extInput) {
          ex.exec(progIn, false, pumpOut, pumpErr);
          firstOut = ex.getOutput();
          if (len == 1)
            lastIn = ex.getInput();
        } else {
          ex.exec(progIn, (progIn == null), pumpOut, pumpErr);
          if ((len==1) && extOutput)
            lastIn = ex.getInput();
        }
      } else {
        ex.exec(null, false, pumpOut, pumpErr);
        if ((i==len-1) && extOutput)
          lastIn = ex.getInput();
      }

      pipeOutDst = ex.getOutput();
      ex.setWatchdog(wd);
      if (i == len-1)
        lastEx = ex;  
    }

    if (!wait || extInput || extOutput)
      return 0;

    int ret = lastEx.waitFor();
    if (wd != null)
      wd.removeAll();
    return ret;
  }

  public int waitOn() throws InterruptedException {
    try { return (lastProc == null) ? 0 : lastProc.waitFor(); }
    finally { lastProc = null; }
  }

  public LinePrintWriter getOutput() { return firstOut; }
  public BufferedReader getInput() { return lastIn; }

  static class Program
  {
    String  cmd;
    String  inFile = null;
    String  outFile = null;   // "1>&2" means use errFile or stderr
    boolean outAppend = false;
    String  errFile = null;   // "2>&1" means use outFile or stdout
    boolean errAppend = false;
    boolean is_last;

    Program(String cmdline, boolean isFirst, boolean isLast) throws IllegalArgumentException {
      is_last = isLast;
      int endIdx = cmdline.length();
      String x;
      for (int i=endIdx-1; i>=0; i--) {
        switch(cmdline.charAt(i)) {
        case '<':
          x = cmdline.substring(i+1, endIdx).trim();
          if (x.length() > 0)
            inFile = x;
          endIdx = i;
          break;
        case '>':
          int type = 0;
          int j = i + 1;
          char ch = cmdline.charAt(i-1);
          if (ch == '>') {
            type |= APPEND_FLAG;
            --i;
          }
          ch = cmdline.charAt(i-1);
          if ((ch == '2') || (ch == '1'))
            --i;
          type |= (ch == '2') ? 2 : 1;
          try {
            ch = cmdline.charAt(j);
            if (ch == '&') {
              type |= MERGE_FLAG;
              j += 2;
            }
          } catch(Exception e) {}
          x = cmdline.substring(j,endIdx).trim();
          if (x.length() <= 0) {
            switch(type) {
            case 1|MERGE_FLAG:             setOutFile(OUT2ERR); break;
            case 1|MERGE_FLAG|APPEND_FLAG: setOutFile(OUT2ERR); outAppend = true; break;
            case 2|MERGE_FLAG:             setErrFile(ERR2OUT); break;
            case 2|MERGE_FLAG|APPEND_FLAG: setErrFile(ERR2OUT); errAppend = true; break;
            }
          } else {
            switch(type) {
            case 1:                        setOutFile(x); break;
            case 2:                        setErrFile(x); break;
            case 1|MERGE_FLAG:             setOutFile(OUT2ERR);
                                           setErrFile(x);
                                           break;
            case 1|MERGE_FLAG|APPEND_FLAG: setOutFile(OUT2ERR);
                                           setErrFile(x);
                                           outAppend=errAppend=true;
                                           break;
            case 2|MERGE_FLAG:             setErrFile(ERR2OUT);
                                           setOutFile(x);
                                           break;
            case 2|MERGE_FLAG|APPEND_FLAG: setErrFile(ERR2OUT);
                                           setOutFile(x);
                                           outAppend=errAppend=true;
                                           break;
            }
          }
          endIdx = i;
          break;
        }
        cmd = cmdline.substring(0,endIdx).trim();
        if (cmd.length() <= 0)
          throw new IllegalArgumentException("Missing program name.");
        if (!isFirst && (inFile != null))
          throw new IllegalArgumentException(
            "Illegal input file '"+inFile+"' since the program input is piped in.");
      }
    }

    public boolean isLast() { return is_last; }

    void setOutFile(String name) throws IllegalArgumentException {
      if (outFile != null)
        throw new IllegalArgumentException("Too many output redirections.");
      outFile = name;
    }

    void setErrFile(String name) throws IllegalArgumentException {
      if (errFile != null)
        throw new IllegalArgumentException("Too many error output redirections.");
      errFile = name;
    }

    // for debugging purpose
    public void dump() {
      PrintStream out = System.out;
      out.println("------------------------------");
      out.print("      cmd:"); out.println(cmd);
      out.print("   inFile:"); if (inFile!=null) out.println(inFile); else out.println();
      out.print("  outFile:"); if (outFile!=null) out.println(outFile); else out.println();
      out.print("outAppend:"); out.println(outAppend);
      out.print("  errFile:"); if (errFile!=null) out.println(errFile); else out.println();
      out.print("errAppend:"); out.println(errAppend);
    }

  } // end of inner class Program.

  static class Executable
  {
    Process proc;

    ExecutableWatchdog watchdog = null;
    LinePrintWriter    procout;
    BufferedReader procin;
    BufferedReader procerr;

    public Executable(String cmdline, String workDir, String[] env) throws IOException {
      proc = Runtime.getRuntime().exec(cmdline, env, (workDir==null) ? null : new File(workDir));
      procout = new LinePrintWriter(proc.getOutputStream());
      procerr = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
      procin  = new BufferedReader(new InputStreamReader(proc.getInputStream()));
    }

    public LinePrintWriter getOutput() { return procout; }
    public BufferedReader getError() { return procerr; }
    public BufferedReader getInput() { return procin; }
    public void kill() { proc.destroy(); }
    public int waitFor() throws InterruptedException { return proc.waitFor(); }
    public int getExitValue() throws IllegalThreadStateException { return proc.exitValue(); }
    public void setWatchdog(ExecutableWatchdog wdog) {
      watchdog = wdog;
      if (wdog != null)
        wdog.add(this);
    }

    /**
     *  Run with System.in, System.out, System.err.
     */
    public void exec(final BufferedReader in, boolean noInput, PumpHandler outHdlr, PumpHandler errHdlr) {
      // start the pump threads
      if (outHdlr != null)
        outHdlr.start();
      if (errHdlr != null)
        errHdlr.start();

      // start suck thread
      if (in == null) {
        if (noInput) {
          // hopefully this sends a 0-length input to a program that waits for anything.
          try { procout.close(); } catch(Exception e) {}
        }
      } else {
        Runnable r = new Runnable() {
          public void run() {
            try {
              String line;
              while ((line = in.readLine()) != null) {
                procout.println(line);
                procout.flush();
              }
            } catch(Exception e) {
              // anything wrong quits.
            } finally {
              try { procout.close(); } catch(Exception e) {}
            }
          }
        };
        Thread t = new Thread(r);
        //t.setDaemon(true);
        t.start();
      }
    }

  } // end of inner class Executable.

  /**
   * This pump handler takes up to 2 input streams and up to 2 output streams.
   * Each input stream is read and pumped to all output streams if not null.
   * Callers never provide both valid inputs and both valid outs.
   */
  static class PumpHandler
  {
    Handler hndlr1;
    Handler hndlr2;
    LinePrintWriter out1;
    LinePrintWriter out2;
    boolean closeOs1;
    boolean closeOs2;
    boolean started = false;

    /**
     * If out1/2 is System.out/err, close1/2 must be false!
     */
    public PumpHandler(BufferedReader  in1,    BufferedReader  in2,
                       LinePrintWriter out1,   LinePrintWriter out2,
                       boolean         close1, boolean         close2)
    {
      hndlr1 = (in1 != null) ? new Handler(in1) : null;
      hndlr2 = (in2 != null) ? new Handler(in2) : null;
      this.out1 = out1;
      this.out2 = out2;
      closeOs1 = close1;
      closeOs2 = close2;
    }

    public void start() {
      if (started)
        return;
      started = true;
      if (hndlr1 != null)
        hndlr1.start();
      if (hndlr2 != null)
        hndlr2.start();
    }
  
    class Handler implements Runnable
    {
      BufferedReader in;

      Handler(BufferedReader in) { this.in = in; }

      public void start() {
        Thread t = new Thread(this);
        //t.setDaemon(true);
        t.start();
      }

      public void run() {
        String line;
        try {
          while ((line = in.readLine()) != null) {
            if (out1 != null)
              out1.println(line);
            if (out2 != null)
              out2.println(line);
          }
        } catch(Exception e) {
          // anything wrong quits.
        } finally {
          try {
            out1.flush();
            if (closeOs1)
              out1.close();
          } catch(Exception e) {}
          try {
            out2.flush();
            if (closeOs2)
              out2.close();
          } catch(Exception e) {}
          try { in.close(); } catch(Exception e) {}
          out1 = null;
          out2 = null;
          in = null;
        }
      }

    } // end of inner class Handler.

  } // end of inner class PumpHandler.

  static class ExecutableWatchdog implements Runnable
  {
    long timeout;
    ArrayList watched = new ArrayList();

    /**
     * @param timeout in seconds.
     */
    public ExecutableWatchdog(int timeout) {
      this.timeout = timeout * 1000;
    }

    public void add(Executable ex) {
      watched.add(ex);
    }

    public void removeAll() {
      watched.clear();
      notifyAll();
    }

    public void remove(Executable ex) {
      watched.remove(ex);
      if (watched.size() == 0)
        notifyAll();
    }

    public void run() {
      try {
        long starttime = System.currentTimeMillis() + 500;
        while (true) {
          long lapsed = System.currentTimeMillis() - starttime;
          if (lapsed >= timeout) break;
          if (watched.size() == 0) break;
          try { Thread.sleep(starttime - lapsed); } catch(InterruptedException ie) {}
        }

        for (int i=watched.size()-1; i>=0; --i) {
          Executable ex = null;
          try {
            ex = (Executable)watched.get(i);
            ex.getExitValue();
          } catch (IllegalThreadStateException e) {
            try { ex.kill(); } catch(Exception ee) {}
            watched.remove(i);
          }
        }
      } catch(Exception e) {}
    }

  } // end of inner class ExecutableWatchdog.

} // end of class ExecuteCmdline.
