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

import java.io.IOException;
import com.enterprisedt.net.ftp.*;
import com.judoscript.RT;

public class FactoryUtil
{
  public static void checkClass(String className, String hint) throws ClassNotFoundException
  {
    try { RT.getClass(className); }
    catch(Exception cce) { throw new ClassNotFoundException(hint); }
    catch(Error err) { throw new ClassNotFoundException(err.getMessage()+'\n'+hint); }
  }

  public static Acme.Syslog getSyslog(String a, int b, int c) throws Exception {
    return new Acme.Syslog(a,b,c);
  }

  public static Object getFTP(String host) throws Exception {
    return new FTP(host);
  }

  public static Object getFTP(String host, int port) throws Exception {
    return new FTP(host,port);
  }

//
// TODO: dir() when nothing returns 550 as an FTPException.
//       should try out all apis and override those.
//
  public static class FTP extends FTPClient
  {
    public FTP(String addr) throws IOException, FTPException { super(addr); }
    public FTP(String addr, int port) throws IOException, FTPException { super(addr,port); }
 
    public void cd(String dir) throws IOException, FTPException { chdir(dir); }

    public String getTransferMode() {
      return (FTPTransferType.BINARY == getType()) ? "binary" : "ascii";
    }

    public void setTransferType(String type) throws IOException, FTPException {
      type = type.toLowerCase();
      if (type.startsWith("asc")) setType(FTPTransferType.ASCII);
      if (type.startsWith("bin")) setType(FTPTransferType.BINARY);
      throw new FTPException("FTP transfer types are ASCII or BINARY.");
    }
  }

} // end of class FactoryUtil.
