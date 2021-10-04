/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 08-08-2004  JH   Inception.
 * 03-27-2005  JH   Have loadProperties() take a 'eval' flag to evaluate ${}
 *                  tags in the values, including ${~}, ${.}, ${:} and ${/}.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/

package com.judoscript.user;

import java.io.*;
import java.util.List;
import java.util.Properties;
import java.util.Enumeration;
import com.judoscript.RT;
import com.judoscript.VersionInfo;
import com.judoscript.bio.HttpService;
import com.judoscript.bio.DOMDoc;
import com.judoscript.parser.JudoParser;
import com.judoscript.util.Lib;
import com.judoscript.ext.FactoryUtil;
import com.judoscript.ext.COMFactory;
import com.judoscript.ext.WindowsRegistry;

public class JavaSysFunLib
{
  public static Object latestVersion()   throws Exception { return VersionInfo.latest(); }
  public static String latestVersionID() throws Exception { return VersionInfo.latestID(); }
  public static Object getMimeTypeMap()  throws Exception { return HttpService.getMimeTypeMap(); }
  public static void addMimeType(String n, String v) throws Exception { HttpService.addMimeType(n, v); }
  public static String getHttpResponseMsg(int code) throws Exception { return HttpService.getHttpResponseMsg(code); }
  public static Object createDom() throws Exception { return DOMDoc.createDom(); }
  public static Object obtainUsage(String fileName) throws Exception { return JudoParser.parseUsage(fileName); }

  public static Object getWindowsRegistry() throws Exception { return WindowsRegistry.getWindowsRegistry(); }
  public static Object getSyslog(String a, int b, int c)  throws Exception { return FactoryUtil.getSyslog(a,b,c); }
  public static Object ftp(String host)            throws Exception { return FactoryUtil.getFTP(host); }
  public static Object ftp(String host, int port)  throws Exception { return FactoryUtil.getFTP(host, port); }
  public static Object getCOM(String prodId)       throws Exception { return COMFactory.getCOM(prodId); }
  public static Object loadTypeLib(String file)    throws Exception { return COMFactory.loadTypeLib(file); }
  public static Object progID2CLSID(String prgId)  throws Exception { return COMFactory.progID2CLSID(prgId); }
  public static String CLSID2ProgID(String clsId)  throws Exception { return COMFactory.CLSID2ProgID(clsId); }

// telnet                 com.judoscript.ext.TelnetFactory : getTelnet
// cvs                    com.judoscript.ext.CVSFactory : getCVS

  public static void encryptFile(String password, String srcFile, String destFile) throws Exception {
    RT.getScript().cryptFile(true, password, srcFile, destFile);
  }

  public static void decryptFile(String password, String srcFile, String destFile) throws Exception {
    RT.getScript().cryptFile(false, password, srcFile, destFile);
  }

  public static byte[] encrypt(String password, String src) throws Exception {
    return encrypt(password, src.getBytes());
  }
  public static byte[] encrypt(String password, byte[] src) throws Exception {
    return RT.getScript().crypt(true, password, src);
  }
  public static void encrypt(String password, InputStream in, OutputStream out) throws Exception {
    RT.getScript().crypt(true, password, in, out);
  }

  public static byte[] decrypt(String password, String src) throws Exception {
    return encrypt(password, src.getBytes());
  }
  public static byte[] decrypt(String password, byte[] src) throws Exception {
    return RT.getScript().crypt(false, password, src);
  }
  public static void decrypt(String password, InputStream in, OutputStream out) throws Exception {
    RT.getScript().crypt(false, password, in, out);
  }

  public static void setCryptoClassName(String clsName) {
    RT.getScript().setCryptoClassName(clsName);
  }

  public static Properties loadProperties(String filename) throws IOException {
    return loadProperties(filename, false);
  }

  // If eval is true, the values will be evaluated that 
  public static Properties loadProperties(String filename, boolean eval) throws IOException {
    InputStream is = new FileInputStream(filename);
    Properties p = new Properties();
    p.load(is);
    is.close();

    if (eval) {
      int cnt = 0; // number of unresolved tags for last iteration.
      do {
        boolean hasUnresolved = false;
        Enumeration keys = p.propertyNames();
        while (keys.hasMoreElements()) {
          String key = (String)keys.nextElement();
          List templ = Lib.stringTemplate(p.getProperty(key), "${", "}");
          if (templ == null) // no tags.
            continue;

          String sub;
          StringBuffer sb = new StringBuffer();
          for (int idx=0; idx<templ.size(); idx++) {
            sub = (String)templ.get(idx);
            if ((idx & 1) == 0) { // 0 or even
              sb.append(sub);
              continue;
            }
            if (sub.length() == 1) {
              // Check for special variables:
              switch(sub.charAt(0)) {
              case '~': sb.append(Lib.getHomeDir());    continue;
              case '.': sb.append(Lib.getCurrentDir()); continue;
              case ':': sb.append(File.pathSeparator);  continue;
              case '/': sb.append(File.separator);      continue;
              }
            }
            String val = p.getProperty(sub); // try the value.
            if (val != null) {
              sb.append(val);
            } else {
              sb.append("${"+sub+"}"); // keep the variable there!
            }
          }
          sub = sb.toString();
          if (sub.indexOf("${") >= 0)
            hasUnresolved = true;
          p.put(key, sub);
        }
        if (!hasUnresolved) {
          break;
        }
        ++cnt;
      } while (cnt < 5); // try up to 5 times.
    }

    return p;
  }

  public static void saveProperties(Properties p, String filename) throws IOException {
    saveProperties(p, filename, "");
  }

  public static void saveProperties(Properties p, String filename, String head) throws IOException {
    FileOutputStream fos = new FileOutputStream(filename);

    // first, turn all values into Strings
    Enumeration keys = p.propertyNames();
    while (keys.hasMoreElements()) {
      String k = (String)keys.nextElement();
      String v = p.get(k).toString();
      p.setProperty(k, v);
    }
    
    p.store(fos, head);
    fos.close();
  }

  public static Object QName(String n, String s) {
    return new javax.xml.namespace.QName(n, s);
  }
  
} // end of class JavaSysFunLib.
