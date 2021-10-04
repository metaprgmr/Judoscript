/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 05-09-2004  JH   Inception.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/

//
// Note! Compile with jdk1.4 only!!
//

package com.judoscript.jdk14;

import java.util.regex.Matcher;
import java.util.logging.Level;
import java.security.cert.X509Certificate;
import javax.net.ssl.*;

import org.apache.commons.logging.impl.Jdk14Logger;


public class JDK14Thingy
{
  public static void disableSSLCertificateValidation() {
    TrustManager[] trustAllCerts = new TrustManager[]{
      new X509TrustManager() { // does not validate certificate chains
        public X509Certificate[] getAcceptedIssuers() { return null; }
        public void checkClientTrusted(X509Certificate[] certs, String authType) {}
        public void checkServerTrusted(X509Certificate[] certs, String authType) {}
      }
    };
    
    try {
      SSLContext sc = SSLContext.getInstance("SSL");
      sc.init(null, trustAllCerts, new java.security.SecureRandom());
      HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
    } catch (Exception e) {
    }
  }

  public static void setAllLoggerLevel(String level, Object[] all) {
    Level _level;
    if (level.equals("all"))        _level = Level.ALL;
    else if (level.equals("off"))   _level = Level.OFF;
    else if (level.equals("trace")) _level = Level.FINEST;
    else if (level.equals("debug")) _level = Level.FINER;
    else if (level.equals("error")) _level = Level.FINE;
    else if (level.equals("info"))  _level = Level.INFO;
    else if (level.equals("warn"))  _level = Level.WARNING;
    else if (level.equals("fatal")) _level = Level.SEVERE;
    else return; // ignore unknowns.

    for (int i=all.length-1; i>=0; --i) {
      if (all[i] != null)
        ((Jdk14Logger)all[i]).getLogger().setLevel(_level);
    }
  }

  public static void setLoggerLevel(String level, Object logger) {
    setAllLoggerLevel(level, new Object[]{ logger });
  }

  public static String[] regexGetGroups(Object matcher) throws Exception {
    Matcher m = (Matcher)matcher;
    String[] ret = new String[m.groupCount()];
    for (int i=0; i<ret.length; ++i) {
      try {
        ret[i] = m.group(i+1);
      } catch(Exception e) {
        ret[i] = null;
      }
    }
    return ret;
  }

} // end of class JDK14Thingy.
