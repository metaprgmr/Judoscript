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
import mindbright.ssh.*;
import com.judoscript.*;
import com.judoscript.bio.JavaObject;
import com.judoscript.util.Lib;

// different from other ext factories.
public final class SSHFactory extends FactoryUtil
{
  public static Variable getSSH(String host, int port,
                                String user, String pass, String cipher)
                         throws Exception
  {
    checkClass(mainClass, hint);
    checkSSHVersion();
    return new SSHConnection(private_getSSH(host,port,user,pass,cipher));
  }

  private static Object private_getSSH(String host, int port,
                                       String user, String pass, String cipher)
                        throws Exception
  {
    return new SSHAPI(host,port,user,pass,cipher,null,10000);
  }

  public static void scp(String host, int port, String username, String password, String cipher,
                         String[] src, String dest, boolean recursive, boolean toRemote, boolean verbose)
                     throws Exception
  {
    checkClass(mainClass, hint);
    checkSSHVersion();
    private_scp(host,port,username,password,cipher,src,dest,recursive,toRemote,verbose);
  }

  private static void private_scp(String   remoteHost,
                                  int      remotePort,
                                  String   username,
                                  String   password,
                                  String   cipher,
                                  String[] localFileList,
                                  String   remoteFile,
                                  boolean  recursive,
                                  boolean  toRemote,
                                  boolean  verbose) throws Exception
  {
    if (!toRemote && localFileList.length > 1)
      throw new Exception("Ambiguous local SSH target."); 
    SSHPasswordAuthenticator auth = (cipher==null)
                                    ? new SSHPasswordAuthenticator(username,password)
                                    : new SSHPasswordAuthenticator(username,password,cipher);

    if (remotePort < 0) remotePort = SSH.DEFAULTPORT;
    SSHSCP scp = new SSHSCP(remoteHost, remotePort, auth, new File("."), recursive, verbose);
    scp.setClientUser(new SSHClientUserAdaptor(remoteHost,remotePort));
    scp.setInteractor(new SSHInteractorAdapter());

    if (toRemote) scp.copyToRemote(localFileList, remoteFile);
    else          scp.copyToLocal (localFileList, remoteFile);

    Lib.killAllThreads(SSH.getThreadGroup());
  }


  public static void checkSSHVersion() throws Exception {
    if ("v1.2.1".compareTo(SSH.VER_SSHPKG) >= 0)
      throw new Exception(
        "Version of Mindbright/ISNetworks Java SSH package must be higher than v1.2.1");
  }

  static final String mainClass = "mindbright.ssh.SSH";
  static final String hint =
  "\nJudoScript's SSH/SCP feature is optional and requires third-party software --\n\n" +
  "     Software: The MindTerm Java SSH/SCP, ISNetworks distribution \n" +
  "        Class: " + mainClass + '\n' +
  "        Where: http://www.isnetworks.net/ssh/ \n" +
  "   Search For: 'mindbright java ssh isnetworks' \n" +
  "     Download: ISNetworksMindTerm1.2.1SCP3.jar or up; \n" +
  "               unzip and put ISNetworksMindTerm123SCP.jar in classpath.";

  static final class SSHConnection extends JavaObject
  {
    SSHConnection(Object o) { super(o); }

    public Variable invoke(String fxn, Expr[] params, int[] javaTypes) throws Throwable
    {
      if (fxn.equals("run") || fxn.equals("justRun")) {
        int len = params==null ? 0 : params.length;
        if (len <= 0) return ValueSpecial.NIL;
        StringBuffer sb = new StringBuffer();
        for (int i=0; i<len; ++i) {
          if (i>0) sb.append('\n');
          sb.append(params[i].getStringValue());
        }
        if (fxn.equals("run"))
          return JudoUtil.toVariable(((SSHAPI)object).run(sb.toString()));

        ((SSHAPI)object).justRun(sb.toString());
        return ValueSpecial.NIL;
      }

      return super.invoke(fxn,params,javaTypes);
    }
  }

} // end of class SSHFactory.
