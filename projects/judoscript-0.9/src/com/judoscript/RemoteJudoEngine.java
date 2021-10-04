/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * ??-??-2004  JH   Inception. -- TODO never really started yet.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript;

import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.*;
import com.judoscript.util.*;


public class RemoteJudoEngine extends UnicastRemoteObject implements RemoteExecServer
{
  ScriptCache scriptCache = new ScriptCache();

  public RemoteJudoEngine(String scriptBase) throws RemoteException
  {
    super();
    // TODO: set script base.
  }

/*
  public void exec(String scriptName,
                   String[] args,
                   RemoteInputStream  stdinstr,
                   RemoteOutputStream stdoutstr,
                   RemotePrintWriter  stdout,
                   RemoteOutputStream stderrstr,
                   RemotePrintWriter  stderr,
                   RemotePrintWriter  stdlog
                  ) throws RemoteException
  {
    try {
      Script script = scriptCache.getScript(scriptName);

      RuntimeGlobalContext rtc = new RuntimeGlobalContext(args,script);
      if (stdoutstr != null || stdout != null) rtc.setOut(stdoutstr,stdout);
      if (stderrstr != null || stderr != null) rtc.setErr(stderrstr,stderr);
      if (stdinstr != null)  rtc.setIn(stdinstr);
      if (stdlog != null) rtc.setLog(stdlog);

      script.startAllowException(rtc);

    } catch(Exception e) { throw new RemoteException("Remote JudoScript Engine", e); }
  }
*/
  public void exec(String scriptName,
                   String[] args,
                   RemoteReader      stdin,
                   RemotePrintWriter stdout,
                   RemotePrintWriter stderr,
                   RemotePrintWriter stdlog
                  ) throws RemoteException
  {
    try {
      Script script = scriptCache.getScript(scriptName);

      RuntimeGlobalContext rtc = new RuntimeGlobalContext(args,script);
      if (stdin  != null) rtc.setIn(stdin);
      if (stdout != null) rtc.setOut(stdout);
      if (stderr != null) rtc.setErr(stderr);
      if (stdlog != null) rtc.setLog(stdlog);

      script.startAllowException(rtc);

    } catch(Throwable e) { throw new RemoteException("Remote JudoScript Engine", e); }
  }


  public static void start(String scriptBase, int port) {
    try {
      System.setSecurityManager(new RMISecurityManager());

      Registry reg = LocateRegistry.createRegistry(port);

      reg.bind("REMOTE_JUDO", new RemoteJudoEngine(scriptBase));

      System.out.println("REMOTE_JUDO Server waiting.....");

    } catch (RemoteException re)  {
       System.out.println("Remote exception: " + re.toString());
    } catch (Exception e) {
       RT.logger.error("RemoteJudoEngine failed to run.", e);
    }
  }

} // end of class RemoteJudoEngine.

