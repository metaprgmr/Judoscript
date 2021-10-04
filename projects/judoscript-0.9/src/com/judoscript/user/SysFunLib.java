/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 09-17-2002  JH   Inception.
 * 08-08-2004  JH   Moved to this package for user access.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript.user;

import java.io.*;
import java.util.*;
import java.util.zip.*;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetAddress;
import javax.naming.InitialContext;
import javax.naming.Context;
import javax.naming.NamingException;
import org.apache.commons.lang.StringUtils;
import com.judoscript.*;
import com.judoscript.util.*;

public final class SysFunLib extends JudoUtil
{
  public static String lineSep;
  public static ConstString lineSeparator;
  public static ConstString fileSeparator;
  public static ConstString pathSeparator;
  static {
    Properties props = System.getProperties();
    lineSep = (String)props.get("line.separator");
    lineSeparator = new ConstString(lineSep);
    fileSeparator = new ConstString(File.separator);
    pathSeparator = new ConstString(File.pathSeparator);
  }

  public static String getFileAsString(String fname) throws IOException {
    return getGZipFileAsString(fname,0,null,false);
  }
  public static String getFileAsString(String fname, int lim) throws IOException {
    return getGZipFileAsString(fname,lim,null,false);
  }
  public static String getFileAsString(String fname, int lim, String encoding) throws IOException {
    return getGZipFileAsString(fname,lim,encoding,false);
  }
  public static String getGZipFileAsString(String fname) throws IOException {
    return getGZipFileAsString(fname,0,null,true);
  }
  public static String getGZipFileAsString(String fname, int lim) throws IOException {
    return getGZipFileAsString(fname,lim,null,true);
  }
  public static String getGZipFileAsString(String fname, int lim, String encoding) throws IOException {
    return getGZipFileAsString(fname,lim,encoding,true);
  }
  static String getGZipFileAsString(String fname, int lim, String enc, boolean isGZip) throws IOException {
    if (lim <= 0) lim = MAX_READ_FILE_LENGTH;
    Reader r;
    if (isGZip) {
      InputStream is = new GZIPInputStream(new FileInputStream(fname));
      r = StringUtils.isBlank(enc) ? new InputStreamReader(is) : new InputStreamReader(is,enc);
    } else {
      r = StringUtils.isBlank(enc) ? (Reader)new FileReader(fname) : new InputStreamReader(new FileInputStream(fname),enc);
    }
    return readString(r,lim);
  }

  static String readString(Reader r, int lim) throws IOException {
    char[] cbuf = new char[1024];
    StringBuffer sb = new StringBuffer();
    while (true) {
      int len = r.read(cbuf);
      if (len < 0) break;
      if (sb.length() + len < lim) {
        sb.append(cbuf,0,len);
      } else {
        sb.append(cbuf,0,lim-sb.length());
        break;
      }
    }
    r.close();
    return sb.toString();
  }

  public static String getFileAsStringFromCP(String fname) throws IOException {
    return getFileAsStringFromCP(fname,0,null);
  }
  public static String getFileAsStringFromCP(String fname, int lim) throws IOException {
    return getFileAsStringFromCP(fname,lim,null);
  }
  public static String getFileAsStringFromCP(String fname, int lim, String enc) throws IOException {
    if (lim <= 0) lim = MAX_READ_FILE_LENGTH;
    InputStream is = ClassLoader.getSystemResourceAsStream(fname);
    Reader r = StringUtils.isBlank(enc) ? new InputStreamReader(is) : new InputStreamReader(is,enc);
    return readString(r,lim);
  }

  public static byte[] getFileAsBytes(String fname) throws IOException {
    return getFileAsBytes(fname,0,false);
  }
  public static byte[] getFileAsBytes(String fname, int lim) throws IOException {
    return getFileAsBytes(fname,lim,false);
  }
  public static byte[] getGZipFileAsBytes(String fname) throws IOException {
    return getFileAsBytes(fname,0,true);
  }
  public static byte[] getGZipFileAsBytes(String fname, int lim) throws IOException {
    return getFileAsBytes(fname,lim,true);
  }

  public static byte[] getFileAsBytesFromCP(String fname) throws IOException {
    return getFileAsBytesFromCP(fname,0);
  }
  public static byte[] getFileAsBytesFromCP(String fname, int lim) throws IOException {
    if (lim <= 0) lim = MAX_READ_FILE_LENGTH;
    InputStream is = ClassLoader.getSystemResourceAsStream(fname);
    return readBytes(is,lim);
  }

  static byte[] getFileAsBytes(String fname, int lim, boolean isGZip) throws IOException {
    if (lim <= 0) lim = MAX_READ_FILE_LENGTH;
    byte[] bbuf = null;
    File f = new File(fname);
    InputStream is = new FileInputStream(f);
    if (isGZip) {
      is = new GZIPInputStream(is);
      bbuf = readBytes(is,lim);
    } else {
      long flen = f.length();
      if (flen < lim) lim = (int)flen;
      bbuf = new byte[lim];
      is.read(bbuf);
    }
    is.close();
    return bbuf;
  }

  static byte[] readBytes(InputStream is, int lim) throws IOException {
    if (lim <= 0) lim = MAX_READ_FILE_LENGTH;
    byte[] bbuf = new byte[lim];
    int len = is.read(bbuf);
    if (len < bbuf.length) {
      byte[] ba = new byte[len];
      System.arraycopy(bbuf,0,ba,0,len);
      bbuf = ba;
    }
    return bbuf;
  }

  public static void sleep() { sleep(-1); }
  public static void sleep(long dur) {
    if (dur < 0) dur = FOREVER;
    wait(System.currentTimeMillis(), dur, false);
  }
  public static void wait(long starttime, long dur, boolean interruptable) {
    while (true) {
      long lapsed = System.currentTimeMillis() - starttime;
      if (lapsed >= dur) return;
      try { Thread.sleep(dur - lapsed); }
      catch(InterruptedException ie) { if (interruptable) break; }
    }
  }

  public static TableDump openTableDump(String fname) throws IOException, ClassNotFoundException {
    return new TableDump(fname);
  }

  public static ResourceBundle resourceBundle(String u) { return ResourceBundle.getBundle(u); }

  public static ServerSocket startServer(int port) throws IOException { return new ServerSocket(port); }
  public static ServerSocket startServer(int port, String addr) throws IOException {
    return new ServerSocket(port,50,InetAddress.getByName(addr));
  }
  public static ServerSocket startServer(int port, InetAddress addr) throws IOException {
    return new ServerSocket(port,50,addr);
  }
  public static ServerSocket startServer(int port, String addr, int backlog) throws IOException {
    return new ServerSocket(port,backlog,InetAddress.getByName(addr));
  }
  public static ServerSocket startServer(int port, InetAddress addr, int backlog) throws IOException {
    return new ServerSocket(port,backlog,addr);
  }
  public static Socket openSocket(String addr, int port) throws IOException { return new Socket(addr,port); }
  public static Socket openSocket(InetAddress addr, int port) throws IOException { return new Socket(addr,port); }
  public static Socket openSocket(String addr, int port, InetAddress localAddr, int localPort) throws IOException {
    return new Socket(addr, port, localAddr, localPort);
  }
  public static Socket openSocket(String addr, int port, String localAddr, int localPort) throws IOException {
    return new Socket(addr, port, InetAddress.getByName(localAddr), localPort);
  }
  public static Socket openSocket(InetAddress addr, int port, InetAddress localAddr, int localPort) throws IOException {
    return new Socket(addr, port, localAddr, localPort);
  }

  public static InitialContext initCtxt() throws NamingException {
    return initCtxt(null,null,null,null,null);
  }
  public static InitialContext initCtxt(String factory) throws NamingException {
    return initCtxt(factory,null,null,null,null);
  }
  public static InitialContext initCtxt(String factory, String url) throws NamingException {
    return initCtxt(factory,url,null,null,null);
  }
  public static InitialContext initCtxt(String factory, String url, String user) throws NamingException {
    return initCtxt(factory,url,user,null,null);
  }
  public static InitialContext initCtxt(String factory, String url, String user, String pwd)
    throws NamingException
  {
    return initCtxt(factory,url,user,null,null);
  }
  public static InitialContext initCtxt(String factory, String url, String user, String pwd, String auth)
    throws NamingException
  {
    Properties props = System.getProperties();
    if (factory != null) props.put(Context.INITIAL_CONTEXT_FACTORY, factory);
    if (url  != null)    props.put(Context.PROVIDER_URL, url);
    if (user != null)    props.put(Context.SECURITY_PRINCIPAL, user);
    if (pwd  != null)    props.put(Context.SECURITY_CREDENTIALS, pwd);
    if (auth != null)    props.put(Context.SECURITY_AUTHENTICATION, auth);
    return new InitialContext(props);
  }

  public static InitialContext weblogicInitCtxt() throws NamingException {
    return weblogicInitCtxt(null,null,null);
  }
  public static InitialContext weblogicInitCtxt(String url) throws NamingException {
    return weblogicInitCtxt(url,null,null);
  }
  public static InitialContext weblogicInitCtxt(String url, String user) throws NamingException {
    return weblogicInitCtxt(url,user,null);
  }
  public static InitialContext weblogicInitCtxt(String url, String user, String pwd) throws NamingException {
    Properties props = System.getProperties();
    props.put(Context.INITIAL_CONTEXT_FACTORY, "weblogic.jndi.WLInitialContextFactory");
    if (url  != null) props.put(Context.PROVIDER_URL, url);
    if (user != null) props.put(Context.SECURITY_PRINCIPAL, user);
    if (pwd  != null) props.put(Context.SECURITY_CREDENTIALS, pwd);
    return new InitialContext(props);
  }

  public static InitialContext sunInitCtxt() throws NamingException {
    return sunInitCtxt(null,null,null);
  }
  public static InitialContext sunInitCtxt(String url) throws NamingException {
    return sunInitCtxt(url,null,null);
  }
  public static InitialContext sunInitCtxt(String url, String user) throws NamingException {
    return sunInitCtxt(url,user,null);
  }
  public static InitialContext sunInitCtxt(String url, String user, String pwd) throws NamingException {
    Properties props = System.getProperties();
    props.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.cosnaming.CNCtxFactory");
    if (url  != null) props.put(Context.PROVIDER_URL, url);
    if (user != null) props.put(Context.SECURITY_PRINCIPAL, user);
    if (pwd  != null) props.put(Context.SECURITY_CREDENTIALS, pwd);
    return new InitialContext(props);
  }

  public static InitialContext websphereInitCtxt() throws NamingException {
    return websphereInitCtxt(null,null,null);
  }
  public static InitialContext websphereInitCtxt(String url) throws NamingException {
    return websphereInitCtxt(url,null,null);
  }
  public static InitialContext websphereInitCtxt(String url, String user) throws NamingException {
    return websphereInitCtxt(url,user,null);
  }
  public static InitialContext websphereInitCtxt(String url, String user, String pwd) throws NamingException {
    Properties props = System.getProperties();
    props.put(Context.INITIAL_CONTEXT_FACTORY, "com.ibm.websphere.naming.WsnInitialContextFactory");
    if (url  != null) props.put(Context.PROVIDER_URL, url);
    if (user != null) props.put(Context.SECURITY_PRINCIPAL, user);
    if (pwd  != null) props.put(Context.SECURITY_CREDENTIALS, pwd);
    return new InitialContext(props);
  }

  public static InitialContext apacheInitCtxt() throws NamingException {
    return apacheInitCtxt(null,null,null);
  }
  public static InitialContext apacheInitCtxt(String url) throws NamingException {
    return apacheInitCtxt(url,null,null);
  }
  public static InitialContext apacheInitCtxt(String url, String user) throws NamingException {
    return apacheInitCtxt(url,user,null);
  }
  public static InitialContext apacheInitCtxt(String url, String user, String pwd) throws NamingException {
    Properties props = System.getProperties();
    props.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.naming.java.javaURLContextFactory");
    if (url  != null) props.put(Context.PROVIDER_URL, url);
    if (user != null) props.put(Context.SECURITY_PRINCIPAL, user);
    if (pwd  != null) props.put(Context.SECURITY_CREDENTIALS, pwd);
    return new InitialContext(props);
  }

  public static InitialContext oracle9iASInitCtxt() throws NamingException {
    return oracle9iASInitCtxt(null,null,null);
  }
  public static InitialContext oracle9iASInitCtxt(String url) throws NamingException {
    return oracle9iASInitCtxt(url,null,null);
  }
  public static InitialContext oracle9iASInitCtxt(String url, String user) throws NamingException {
    return oracle9iASInitCtxt(url,user,null);
  }
  public static InitialContext oracle9iASInitCtxt(String url, String user, String pwd) throws NamingException
  {
    Properties props = System.getProperties();
    props.put(Context.INITIAL_CONTEXT_FACTORY, "com.evermind.server.rmi.RIMInitialContextFactory");
    if (url  != null) props.put(Context.PROVIDER_URL, url);
    if (user != null) props.put(Context.SECURITY_PRINCIPAL, user);
    if (pwd  != null) props.put(Context.SECURITY_CREDENTIALS, pwd);
    return new InitialContext(props);
  }

  public static InitialContext jbossInitCtxt() throws NamingException {
    return jbossInitCtxt(null,null,null);
  }
  public static InitialContext jbossInitCtxt(String url) throws NamingException {
    return jbossInitCtxt(url,null,null);
  }
  public static InitialContext jbossInitCtxt(String url, String user) throws NamingException {
    return jbossInitCtxt(url,user,null);
  }
  public static InitialContext jbossInitCtxt(String url, String user, String pwd) throws NamingException {
    Properties props = System.getProperties();
    props.put(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");
    props.put(Context.URL_PKG_PREFIXES,        "org.jboss.naming:org.jnp.interfaces");
    if (url  != null) props.put(Context.PROVIDER_URL, url);
    if (user != null) props.put(Context.SECURITY_PRINCIPAL, user);
    if (pwd  != null) props.put(Context.SECURITY_CREDENTIALS, pwd);
    return new InitialContext(props);
  }

  public static InitialContext ionaInitCtxt() throws NamingException {
    return ionaInitCtxt(null,null,null);
  }
  public static InitialContext ionaInitCtxt(String url) throws NamingException {
    return ionaInitCtxt(url,null,null);
  }
  public static InitialContext ionaInitCtxt(String url, String user) throws NamingException {
    return ionaInitCtxt(url,user,null);
  }
  public static InitialContext ionaInitCtxt(String url, String user, String pwd) throws NamingException {
    Properties props = System.getProperties();
    props.put(Context.INITIAL_CONTEXT_FACTORY, "com.ejbhome.naming.spi.rmi.RMIInitCtxFactory");
    if (url  != null) props.put(Context.PROVIDER_URL, url);
    if (user != null) props.put(Context.SECURITY_PRINCIPAL, user);
    if (pwd  != null) props.put(Context.SECURITY_CREDENTIALS, pwd);
    return new InitialContext(props);
  }

  public static void setHttpProxy() {}
  public static void setHttpProxy(String host) { setHttpProxy(host,80); }
  public static void setHttpProxy(String host, int port) {
    Properties props = System.getProperties();
    if (StringUtils.isBlank(host)) {
      props.put("http.proxySet", "false");
      return;
    }
    props.put("http.proxySet", "true");
    props.put("http.proxyHost", host);
    props.put("http.proxyPort", String.valueOf(port));
  }

  public static ZipFile openZip() { return null; }
  public static ZipFile openZip(String fname) throws IOException { return new ZipFile(fname); }

  public static Variable lineSep() { return lineSeparator; }
  public static Variable fileSep() { return fileSeparator; }
  public static Variable pathSep() { return pathSeparator; }

  public static void gzip() {}
  public static void gzip(String inF) throws IOException { Lib.gzip(inF,null,true); }
  public static void gzip(String inF, String outF) throws IOException { Lib.gzip(inF,outF,true); }
  public static void gunzip() {}
  public static void gunzip(String inF) throws IOException { Lib.gzip(inF,null,false); }
  public static void gunzip(String inF, String outF) throws IOException { Lib.gzip(inF,outF,false); }


  public static ZipWriter createJar(String fname) throws IOException { return createJar(fname,null); }
  public static ZipWriter createJar(String fname, String manifest) throws IOException {
    JarOutputStream jos;
    if (StringUtils.isBlank(manifest)) {
      jos = new JarOutputStream(new FileOutputStream(fname));
    } else {
      StringBufferInputStream sbis = new StringBufferInputStream(manifest);
      jos = new JarOutputStream(new FileOutputStream(fname), new Manifest(sbis));
    }
    return new ZipWriter(jos,null);
  }

  public static ZipWriter createZip(String fname) throws IOException {
    return new ZipWriter(new ZipOutputStream(new FileOutputStream(fname)),null);
  }

  public static TarWriter createTar(String fname) throws IOException {
    return new TarWriter(fname,null);
  }

  public static String unit(long cnt, String single) { return Lib.unit(cnt,single,null); }
  public static String unit(long cnt, String single, String plural) { return Lib.unit(cnt,single,plural); }

//  public static ClassFile genClassFile(String name) { return new ClassFile(name); }
//  public static ClassFile genClassFile(String name, String sname) { return new ClassFile(name,sname); }

  public static Map systemProperties() { return RT.getGlobalContext().getSystemProperties(); }
  public static Object systemProperty(String name) { return systemProperties().get(name); }

  public static void move(String s, String d) throws ExceptionRuntime { move(new File(s), new File(d)); }
  public static void move(String s, File d)   throws ExceptionRuntime { move(new File(s), d); }
  public static void move(File s,   String d) throws ExceptionRuntime { move(s, new File(d)); }
  public static void move(File src, File dest) throws ExceptionRuntime {
    if (dest.isDirectory())
      dest = new File(dest, src.getName());
    if (!src.renameTo(dest))
      ExceptionRuntime.rte(RTERR_FS_COPY_MOVE_FAILED,
          "Faild to move " + getPathName(src) + " to " + getPathName(dest) + '.');
  }

  public static void mkdir(String f) throws ExceptionRuntime {
    if (StringUtils.isNotBlank(f))
      mkdir(new File(f));
  }
  public static void mkdir(File f) throws ExceptionRuntime {
    if (f == null)
      return;

    if (f.exists()) {
      if (f.isDirectory())
        return;
      ExceptionRuntime.rte(RTERR_FS_CREATE_DIR_FAILED,
                "Failed to create directory '" + getPathName(f) + "' because a file exists.");
    }
    try { f.mkdirs(); }
    catch(Exception e) { 
      ExceptionRuntime.rte(RTERR_FS_CREATE_DIR_FAILED,
          Lib.getExceptionMsg("Failed to create directory '" + getPathName(f) + "'", e));
    }
  }

  public static void rmdir(String f, boolean force) throws ExceptionRuntime { rmdir(new File(f), force); }
  public static void rmdir(File f, boolean force) throws ExceptionRuntime {
    if (!f.exists())
      return;
    if (!f.isDirectory()) {
      ExceptionRuntime.rte(RTERR_FS_REMOVE_DIR_FAILED,
          "Failed to remove directory '" + getPathName(f)+"' because it is a file.");
    }
    // so that directory does exist
    if (f.delete())
      return;
    if (!force) {
      ExceptionRuntime.rte(RTERR_FS_REMOVE_DIR_FAILED,
          "Failed to remove directory '" + getPathName(f) + "', probably not empty.");
    }
    // recursively delete all
    File[] fa = f.listFiles();
    int len = fa==null ? 0 : fa.length;
    for (int i=0; i<len; ++i) {
      if (fa[i].isDirectory()) {
        rmdir(fa[i], true);
      } else if (!fa[i].delete()) {
        ExceptionRuntime.rte(RTERR_FS_REMOVE_DIR_FAILED,
          "Failed to remove directory '" + getPathName(f) + "' because file '" +
          getPathName(fa[i]) + "' can not be removed.");
      }
    }
    if (f.delete())
      return;
    ExceptionRuntime.rte(RTERR_FS_REMOVE_DIR_FAILED,
              "Failed to remove directory '" + getPathName(f) + "'.");
  }

  public static void printInternalStackTrace() {
    try {
      Variable var = RT.resolveVariable("$_");
      var.invoke("pist", null, null);
    } catch(Throwable e) {}
  }

} // end of class SysFunLib.
