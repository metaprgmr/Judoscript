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


package com.judoscript.ext;

import java.io.*;
import java.util.*;
import org.apache.commons.lang.StringUtils;
import com.judoscript.RT;
import com.judoscript.util.Lib;

import mindbright.ssh.*;
import mindbright.terminal.Terminal;
import mindbright.security.Cipher;

public class SSHAPI extends SSHClient
{
  static final long DEFAULT_TIMEOUT = 10 * 60 * 1000; // 10 minutes
  static final long MIN_TIMEOUT = 3 * 1000; // 3 seconds

  static final String[] emptyStringArray = new String[0];
  static final String _START_MARK = "))))))))";
  static final String _END_MARK   = "((((((((";

  private PrintWriter errOut;
  private long        timeout;

  private boolean     discardResult  = false;
  private String      startMark      = null;
  private String      endMark        = null;
  private boolean     inProgress     = false;
  private boolean     cmdDone        = false;
  private Vector      results        = new Vector();
  private Hashtable   promptResponse = null;

  public SSHAPI(String server, String username, String password, String cipher,
                PrintWriter errOut, long timeout) throws IOException
  {
    this(server,22,username,password,cipher,errOut,timeout);
  }

  public SSHAPI(String server, int port, String username, String password, String cipher,
                PrintWriter errOut, long timeout) throws IOException
  {
    super(cipher == null ? new SSHPasswordAuthenticator(username,password)
                         : new SSHPasswordAuthenticator(username,password,cipher),
          new SSHClientUserAdaptor(server, port<=0 ? 22 : port));
    this.errOut = errOut;
    if (timeout < 0)                setTimeout(DEFAULT_TIMEOUT);
    else if (timeout < MIN_TIMEOUT) setTimeout(MIN_TIMEOUT);
    else                            setTimeout(timeout);
    setConsole(new MyConsole());
    activateTunnels = false;
    bootSSH(false);
  }

  public void close() { disconnect(); }

  public void disconnect() {
    try { forcedDisconnect(); } catch(Exception e) {}

    Thread[] ta = new Thread[SSH.getThreadGroup().activeCount()];
    SSH.getThreadGroup().enumerate(ta);
    for (int i=0; i<ta.length; ++i)
      try { ta[i].stop(); } catch(Exception e) {}
  }

  public void setTimeout(long val) {
    timeout = val;
  }

  public String[] getFileNames() throws IOException { return run("ls -A -L -p -1"); }
  public void cd()               throws IOException { justRun("cd"); }
  public void cd(String newdir)  throws IOException { justRun("cd " + StringUtils.defaultString(newdir)); }
  public void mkdir(String dir)  throws IOException { justRun("mkdir " + dir); }
  public void rmdir(String dir)  throws IOException { justRun("rmdir " + dir); }
  public void rm(String file)    throws IOException { justRun("rm " + file); }
  public void mv(String src, String dest) throws IOException { justRun("mv "+src+" "+dest); }
  public String pwd()  throws IOException { return (String)Lib.getFirst(run("pwd")); }
  public String date() throws IOException { return (String)Lib.getFirst(run("date")); }
  public String showEnv(String n) throws IOException {
    return (String)Lib.getFirst(run("echo $"+n));
  }

  public void justRun(String cmd) throws IOException { run(cmd,(Hashtable)null,true); }

  public String[] run(String cmd) throws IOException { return run(cmd,(Hashtable)null,false); }

  /* This is not working, at least for "su xxx". */
  public String[] run(String cmd, String[] promptResp) throws IOException
  {
    int len = (promptResp.length <= 0) ? 0 : promptResp.length;
    if (len == 0)
      return run(cmd,(Hashtable)null,false);

    promptResponse = new Hashtable();
    promptResponse.put(promptResp[0].trim(), promptResp[1]);
    return run(cmd,promptResponse,false);
  }

  /* This is not working, at least for "su xxx". */
  public String[] run(String cmd, Hashtable promptResp, boolean discard) throws IOException
  {
    promptResponse = promptResp;

    // Flow: A
    synchronized(this) {
      while (inProgress) {
        try { wait(500); } catch(InterruptedException ie) {}
      }
      long cmdID = System.currentTimeMillis();
      startMark = _START_MARK + cmdID;
      endMark   = _END_MARK   + cmdID;
      inProgress = true;
      cmdDone    = false;
      this.discardResult = discard;
      results.clear();
    }

    // Flow: B
    stdinWriteString("echo \"" + startMark); stdinWriteString("\"\n");
    stdinWriteString(cmd);                   stdinWriteString("\n");
    stdinWriteString("echo \"" + endMark);   stdinWriteString("\"\n");
 
    synchronized(this) {
      long starttime = System.currentTimeMillis();
      while (true) {
        if (System.currentTimeMillis() - starttime < timeout) {
          try { wait(500); } catch(InterruptedException ie1) {}
        }
        if (cmdDone) {
          // Flow: D
          String[] sa = results.size() == 0 ? emptyStringArray : new String[results.size()];
          for (int i=results.size()-1; i>=0; --i)
            sa[i] = (String)results.elementAt(i);
          results.clear();
          inProgress = false;
          startMark = endMark = null;
          promptResponse = null;
          notify();
          return sa;
        }
      }
    }
  }

  final class MyConsole implements SSHConsole
  {
    String output = "";
    String err_output = "";
    boolean gotStartMark = false;

    public void stdoutWriteString(byte[] str)
    {
      // Flow: C-stdout
      synchronized(SSHAPI.this)
      {
        if (!inProgress) return;

        output += new String(str);
        int idx;
        if (!gotStartMark) {
          idx = output.indexOf(startMark);
          if (idx < 0) {
            output = "";
            return;
          } else {
            gotStartMark = true;
            output = output.substring(idx+startMark.length()+1);
          }
        }

        boolean endWithNL = output.endsWith("\n");
        idx = output.indexOf(endMark);
        if (idx >= 0) {
          cmdDone = true;
          output = output.substring(0,idx);
          endWithNL = true;
        }

        StringTokenizer st = new StringTokenizer(output, "\n");
        while (st.hasMoreTokens()) {
          String line = st.nextToken();
          if (!discardResult)
            results.addElement(line);
          if (promptResponse != null) {
            String resp = (String)promptResponse.get(line.trim());
            if (resp != null) {
              try {
                stdinWriteString(resp);
                stdinWriteString("\n");
              } catch(Exception e) {}
            }
          }
        }
        output = "";
        if (!endWithNL && results.size()>0) {
          output = (String)results.elementAt(results.size()-1);
          results.removeElementAt(results.size()-1);
        }

        if (cmdDone) {
          gotStartMark = false;
          SSHAPI.this.notify();
        }

      } // synchronized(SSHAPI.this).
    }

    public void stderrWriteString(byte[] str)
    {
      // Flow: C-stderr
      synchronized(SSHAPI.this)
      {
        if (!inProgress) return;

        err_output += new String(str);
        boolean endWithNL = err_output.endsWith("\n");

        StringTokenizer st = new StringTokenizer(err_output, "\n");
        String lastLine = null;
        while (st.hasMoreTokens()) {
          lastLine = st.nextToken();
          if (errOut != null) errOut.println(lastLine);
          if (promptResponse != null) {
            String resp = (String)promptResponse.get(lastLine.trim());
            if (resp != null) {
              try {
                stdinWriteString(resp);
                stdinWriteString("\n");
              } catch(Exception e) {
                RT.logger.error("SSHAPI.", e);
              }
            }
          }
        }
        err_output = (!endWithNL && lastLine != null) ? lastLine : "";

      } // synchronized(SSHAPI.this).
    }

    public Terminal getTerminal() { return null; }
    public void print(String str) {}
    public void println(String str) {}
    public void serverConnect(SSHChannelController controller, Cipher sndCipher) {}
    public void serverDisconnect(String reason) {}

  } // end of inner class MyConsole.

} // end of class SSHAPI.
