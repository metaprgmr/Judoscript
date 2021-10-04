/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 05-03-2002  JH   Inception.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/

package com.judoscript.util;

import javax.naming.InitialContext;
import javax.naming.Context;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.Enumeration;
import org.apache.commons.lang.StringUtils;

/**
 * This class runs a command on a remote execute server.
 *<br>
 * All the system properties are put into the initial context
 * for locating the server.
 *<br>
 * The first parameter is the remote command, optionally
 * followed by more parameters.
 */
public class RemoteExecClient
{
  public static void main(String args[]) {
    if (args.length <= 1) printHelp();

    try {
      RemoteExecServer remoteServer = (RemoteExecServer)getContext().lookup(args[0]);
      String[] remote_args = new String[args.length-2];
      if (remote_args.length > 0)
        System.arraycopy(args,2,remote_args,0,remote_args.length);

/**
      public void exec(String exec,
      String[] args,
      RemoteInputStream  stdinstr,
      RemoteOutputStream stdoutstr,
      RemotePrintWriter  stdout,
      RemoteOutputStream stderrstr,
      RemotePrintWriter  stderr,
      RemotePrintWriter  stdlog,
      );
**/

    } catch(Exception ee) {
      ee.printStackTrace();
    }
  }

  static Context getContext() throws Exception {
    boolean verbose = "true".equalsIgnoreCase(System.getProperty("verbose"));

    Properties props = new Properties();

    // Default use Sun-Java's JNDI-RMI service provider.
    props.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.rmi.registry.RegistryContextFactory");

    // Load properties file from -D or classpath:
    String path = System.getProperty("propfile");
    if (StringUtils.isNotBlank(path)) {
      try {
      props.load(new FileInputStream(path));
      } catch(Exception e) {
      if (verbose)
        System.err.println("Properties file '" + path + "' can not be loaded: " + e.getMessage());
      }
    } else {
      try {
        props.load(ClassLoader.getSystemResourceAsStream("runexecclient.properties"));
        if (verbose)
          System.err.println("Loaded 'RemoteExecClient.properties' in classpath.");
      } catch(Exception e) {
        if (verbose)
          System.err.println("No 'RemoteExecClient.properties' found in classpath.");
      }
    }

    // Load system properties:
    Properties p = System.getProperties();
    Enumeration keys = p.keys();
    while (keys.hasMoreElements()) {
      Object k = keys.nextElement();
      props.put(k, p.get(k));
    }

    return new InitialContext(props);
  }

  public static String helpMsg =
    "This program executes a command on a remote server located via JNDI.\n" +
    "\n" +
    "Usage: java (-Dx=y)* com.judoscript.util.RemoteExecClient jdni_name command (param)*\n" +
    "\n" +
    "  The jndi_name should reference a RemoteExecServer object.\n" +
    "\n" +
    "  The command is anything that the server can accept.\n" +
    "\n" +
    "  The remote server is located via JNDI.\n" +
    "  The context information is located by these rules:\n" +
    "  1) if -Dpropfile=myconfig.properties is present, load it;\n" +
    "     otherwise, load 'runexecclient.properties in the classpath\n" +
    "     if present.\n" +
    "  2) system properties specified on the command-line override\n" +
    "     existing properties, e.g.:\n" +
    "     -Djava.naming.provider.url=rmi://localhost[:1099]/RemoteJudo\n" +
    "     -Djava.naming.security.principal=james\n" +
    "     -Djava.naming.security.credentials=james\n" +
    "     -Djava.naming.security.authentication=\n" +
    "\n" ;

  private static void printHelp() {
    System.err.println(helpMsg);
    System.err.flush();
    System.exit(0);
  }

} // end of class RemoteExecClient.
