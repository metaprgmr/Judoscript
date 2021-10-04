/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 05-02-2002  JH   Inception.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/

package com.judoscript.util;

public interface RemoteExecServer extends java.rmi.Remote
{
  public void exec(String scriptName,
                   String[] args,
                   RemoteReader       stdin,
                   RemotePrintWriter  stdout,
                   RemotePrintWriter  stderr,
                   RemotePrintWriter  stdlog
                  ) throws java.rmi.RemoteException;
}

