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


package com.judoscript.bio;

import java.io.*;
import java.net.*;
import java.util.*;
import org.apache.commons.logging.*;
import org.apache.commons.lang.StringUtils;
import com.judoscript.*;
import com.judoscript.util.Lock;


public class _Timer extends ObjectInstance implements ShortcutInvokable
{
  public static Log logger = null;
  StmtSchedule stmt;

  boolean   isAbsolute;
  boolean   isStart = false;
  Expr      start_delay = null;
  Expr      period = null;
  long      periodValue = 0;
  long      count = 0;

  Timer     timer = null;
  Task      task = null;
  boolean   stopped = false;
  Throwable excpt = null;
  ServerSocket ss = null;
  Lock lock = new Lock();
  long timeLaunched = 0;
  long timeFirstRun = 0;
  long timeLatestRun = 0;
  int  numRuns = 0;
  PrintWriter htmlOut = null;
  String cmd = null;

  public _Timer(StmtSchedule stmt) {
    super();
    this.stmt = stmt;
    isAbsolute = stmt.isAbsolute;
    isStart = stmt.isStart;
    start_delay = stmt.start_delay;
    period = stmt.period;

    if (logger == null)
      logger = LogFactory.getLog("judo.schedule");
  }

  public String getTypeName() { return "SchedulerTimer"; }
  public boolean hasListener()  { return (stmt.port!=null) && isRepetitive(); }
  public boolean isOneTime()    { return (period==null); }
  public boolean isRepetitive() { return (period!=null); }
  public void close() {
    try { timer.cancel(); } catch(Exception e) {}
    try { RT.getRootFrame().removeVariable(TIMER_NAME); } catch(Exception e) {}
    try { ss.close(); } catch(Exception e) {}
    timer = null;
    task = null;
    ss = null;
    stopped = false;
  } 
  public Variable resolveVariable(String name) throws Throwable {
    if (name.equals("time"))
      return (task == null) ? (Variable)ConstInt.ZERO: new _Date(task.scheduledExecutionTime());
    if (name.equals("period"))
      return ConstInt.getInt(periodValue);
    if (name.equals("cmd"))
      return JudoUtil.toVariable(cmd);
    if (name.equals("htmlOut"))
      return JudoUtil.toVariable(htmlOut);
    if (name.equals("count") || name.equals("eventCount"))
      return ConstInt.getInt(count);
    return super.resolveVariable(name);
  }

  public Variable invoke(String fxn, Expr[] args, int[] javaTypes) throws Throwable
  {
    return super.invoke(getMethodOrdinal(fxn),fxn,args);
  }

  public Variable invoke(int ord, String fxn, Expr[] args, int[] javaTypes) throws Throwable
  {
/*
    switch(fxnOrd) {
    case BI_TIMER_RESCHEDULESTARTING:
    case BI_TIMER_RESCHEDULEAFTER:
    case BI_TIMER_RESCHEDULEABSOLUTESTARTING:
    case BI_TIMER_RESCHEDULEABSOLUTEAFTER:
      excpt = null;
      int len = (args==null) ? 0 : args.length;
      if (len == 0)
        ExceptionRuntime.rte(RTERR_ILLEGAL_ARGUMENTS,
          "reschedule statements need 1 to 2 parameters."); 
      isAbsolute = (fxnOrd==BI_TIMER_RESCHEDULEABSOLUTESTARTING) ||
                   (fxnOrd==BI_TIMER_RESCHEDULEABSOLUTEAFTER);
      isStart = (fxnOrd==BI_TIMER_RESCHEDULESTARTING) ||
                (fxnOrd==BI_TIMER_RESCHEDULEABSOLUTESTARTING);
      start_delay = args[0];
      period = (len>1) ? args[1] : null;
      start();
      break;

    default:
      return null;
    }
*/
    return super.invoke(ord,fxn,args);
  }

  public void start() throws Throwable {
    timeLaunched = System.currentTimeMillis();
    Variable x = RT.getRootFrame().resolveVariable(TIMER_NAME);
    if ((x != null) && !x.isNil())
      ExceptionRuntime.rte(RTERR_TIMER_ALREADY_EXISTS,
                "Timer already exists; embedded scheduling is not supported.");
    RT.getRootFrame().setVariable(TIMER_NAME, this, 0);

    periodValue = (period==null) ? 0 : period.getLongValue();
    Date start = null;
    long delay = 0;
    if (start_delay == null) {
      start = new Date(System.currentTimeMillis());
      isStart = true;
    } else if (isStart) {
      start = start_delay.getDateValue();
    } else {
      delay = start_delay.getLongValue();
    }
    if (task == null) task = new Task(RT.newSubContext());
    if (timer == null) timer = new Timer();
    else timer.cancel();
    switch ( (isAbsolute ? 4 : 0) | (isStart ? 2 : 0) | ((periodValue!=0) ? 1 : 0) ) {
    case 0: timer.schedule(task, delay); break;
    case 1: timer.schedule(task, delay, periodValue); break;
    case 2: timer.schedule(task, start); break;
    case 3: timer.schedule(task, start, periodValue); break;
    case 4:
    case 5: timer.schedule(task, delay, periodValue); break;
    case 6:
    case 7: timer.schedule(task, start, periodValue); break;
    }
    stopped = false;
    if (hasListener()) {
      ss = new ServerSocket((int)stmt.port.getLongValue());
      while (handleRequest(ss.accept()));
    } else {
      // pause the main thread until it is stopped.
      while (!stopped) {
        try { synchronized(task) { task.wait(); } } catch(InterruptedException ie) {}
      }
    }
    close();
    if (excpt != null) throw excpt;
  }

  // The Task Class
  class Task extends TimerTask
  {
    boolean console;
    RuntimeContext rtc;

    public Task(RuntimeContext rtc) { this(rtc, false); }

    public Task(RuntimeContext rtc, boolean console) {
      this.rtc = rtc;
      this.console = console;
    }

    public void run() {
      try {
        RT.pushContext(rtc);
        synchronized(lock) {
          ++count;
          timeLatestRun = System.currentTimeMillis();
          if (timeFirstRun <= 0)
            timeFirstRun = timeLatestRun;
          try {
            if (console) {
              stmt.listener.exec();
            } else {
              ++numRuns;
              stmt.block.exec();
            }
          } catch(ExceptionControl ce) {
            stopped = true;
            if (ce.isForSchedule()) {
              if (ce.isContinue())
                stopped = false;
            } else if (ce.isResume()) {
              stopped = false;
            } else if (ce.isReturn()) {
              excpt = ce;
            } else {
              try {
                ExceptionRuntime.rte(RTERR_ILLEGAL_STATEMENT,
                  "Invalid "+ce.getTypeName()+" statement within a schedule statement.");
              } catch(ExceptionRuntime ee) {
                excpt = ee;
              }
            }
          } catch(Throwable e) {
            excpt = e;
            stopped = true;
          } finally {
            if (stopped && (ss!=null)) {
              try { ss.close(); } catch(Exception e) {}
              ss = null;
            }
          }
        }
        if (isOneTime()) { // server socket never opened for one time tasks.
          stopped = true;
          synchronized(this) { notifyAll(); }
        }
      } finally {
        RT.popContext();
      }
    }

  } // end of inner class Task.


  boolean handleRequest(Socket s) {
    boolean stop1 = false;
    boolean stop2 = false;
    cmd = null;
    try {
      BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
      String line = br.readLine();
      int idx = line.indexOf(' ');
      int idx1 = line.indexOf(' ',idx+1);
      line = line.substring(idx+1,idx1).trim(); // the request URI; only care about ?...
      while (StringUtils.isNotBlank(br.readLine()));
      //br.close();
      idx = line.indexOf('?');
      if (idx >= 0) {
        StringTokenizer st = new StringTokenizer(line.substring(idx+1),"&");
        while (st.hasMoreTokens()) {
          line = st.nextToken();
          if (line.startsWith("cmd="))
            cmd = URLDecoder.decode(line.substring(4));
          else if (line.startsWith("stop1"))
            stop1 = true;
          else if (line.startsWith("stop2"))
            stop2 = true;
        }
      }
      htmlOut = new PrintWriter(s.getOutputStream());
      synchronized(lock) {
        handleCommand(htmlOut, stop1 && stop2);
      }
      try { htmlOut.flush(); } catch(Exception e) {}
      try { htmlOut.close(); } catch(Exception e) {}
      htmlOut = null;
    } catch(Exception e) {
      logger.error("Handling request.", e);
    } finally {
      try { s.close(); } catch(Exception e) {}
      cmd = null;
    }
    return (!stop1 || !stop2);
  }

  void handleCommand(PrintWriter out, boolean doStop) {
    // HTML output
    String t = null;
    try { t = stmt.title.getStringValue(); } catch(Throwable e) {}
    t = StringUtils.defaultString(t,"<font face=arial><h2>JudoScript Scheduler<br>Control Panel</h2></font>");
    out.println("HTTP/1.0 200 OK");
    out.println("Content-Type: text/html");
    out.println();
    out.println("<html><body><center>");
    out.println(t);
    out.println("<table border=0>");
    out.println("<tr><td align=right><b>Launched:</b> &nbsp; </td><td nowrap>");
    out.println(new Date(timeLaunched));
    out.println("</td></tr>");
    if (timeFirstRun > 0) {
      out.println("<tr><td align=right><b>First Run:</b> &nbsp; </td><td nowrap>");
      out.println(new Date(timeFirstRun));
      out.println("</td></tr>");
    }
    if (timeLatestRun > 0) {
      out.println("<tr><td align=right><b>Latest Run:</b> &nbsp; </td><td nowrap>");
      out.println(new Date(timeLatestRun));
      out.println("</td></tr>");
    }
    out.println("<tr><td align=right><b>Number of Runs:</b> &nbsp; </td><td nowrap>");
    out.println(numRuns);
    out.println("</td></tr><tr><td colspan=2>&nbsp;</td></tr>");
    out.println("<tr><td colspan=2 nowrap align=center>");
    out.println("<form><input type=text name=cmd size=40> &nbsp;");
    out.println("<input type=submit value=Submit><br>&nbsp;<br>");
    out.println("<input type=checkbox name=stop1> Stop the job ");
    out.println("<input type=checkbox name=stop2> Please do stop the job!");
    out.println("</form></td></tr><tr><td colspan=2> <hr> </td></tr>");
    out.println("<tr><td colspan=2 align=left>");

    try {
      new Task(RT.newSubContext(), true).run(); // code may output stuff here; they can mess up HTML!
    } catch(Exception e) {}

    if (doStop)
      out.println("<br>&nbsp;<br>Schedule job has been stopped and closed.");

    out.println("</td></tr></table></center></body></html>");
  }

} // end of class _Timer.

