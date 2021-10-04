/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 08-06-2004  JH   Inception.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript;

import java.io.*;
import java.util.*;
import java.net.URL;
import java.net.MalformedURLException;
import com.judoscript.util.Lib;
import com.judoscript.util.UserClassLoader;


// TODO: load resources!
public final class UserClasspath implements Consts
{
  private UserClassLoader ucl = new UserClassLoader(null);
  private ArrayList importList = null;
  private HashMap handledNames = new HashMap();

  UserClasspath() {
    if (UserClassLoader.isSystemLoader) {
      // This must be in the main thread
      Thread.currentThread().setContextClassLoader(ucl);
    }
  }

  public void addImport(String s) {
    if (importList == null)
      importList = new ArrayList();

    if (!importList.contains(s)) {
      importList.add(s);
      handledNames.clear();
    }
  }

  public Class getClass(String name) throws ClassNotFoundException {
    // filter out internal classes.
    if (name.startsWith("com.judoscript.") &&
        !name.startsWith("com.judoscript.util.") &&
        !name.startsWith("com.judoscript.user.") &&
        !name.startsWith("com.judoscript.hibernate."))
    {
      throw new ClassNotFoundException("Class " + name + " is never found.");
    }

    return ucl.loadClass(handleClassName(name));
  }

  /**
   * Add a user classpath to the end of the list.
   * If the classpath exist in the system classpath, it is not affected.
   * @param cp a classpath component, either a path name or a jar file path.
   */
  public UserClasspath add(String cp) throws ExceptionRuntime, MalformedURLException {
    URL url = new File(cp).toURL();

    // make sure no duplicate
    URL[] cur = ucl.getURLs();
    for (int i=cur.length-1; i>=0; --i)
      if (cur[i].equals(url))
        return this;

    ucl.add(url);
    return this;
  }

  public UserClasspath __vargs__add(Object cps) throws ExceptionRuntime, MalformedURLException {
    if (cps == null)
      return this;

    if (cps instanceof Collection)
      cps = ((Collection)cps).toArray();

    Object[] oa;
    int len;
    if (cps instanceof Object[]) {
      oa = (Object[])cps;
      len = oa==null ? 0 : oa.length;
      for (int i=0; i<len; ++i)
        add(oa[i].toString());
    } else if (cps instanceof Variable) {
      try {
        Variable v = (Variable)cps;
        oa = v.getObjectArrayValue();
        len = oa==null ? 0 : oa.length;
        for (int i=0; i<oa.length; ++i)
          add(oa[i].toString());
      } catch(Throwable e) {}
    } else {
      add(cps.toString());
    }
    return this;
  }

  /**
   * @return the user classpaths. Immutable.
   */
  public String[] getUserClasspaths() {
    URL[] urls = ucl.getURLs();
    String[] ret = new String[urls.length];
    for (int i=ret.length-1; i>=0; --i)
      ret[i] = urls[i].toString();
    return ret;
  }

  /**
   * @return the system classpaths as the "java.class.path" system property.
   *         Immutable.
   */
  public String[] getSystemClasspaths() {
    StringTokenizer st = new StringTokenizer(System.getProperty("java.class.path"),
                                             File.pathSeparator);
    String[] ret = new String[st.countTokens()];
    for (int i=0; i<ret.length; ++i)
      ret[i] = st.nextToken();

    return ret;
  }

  /**
   * @return all user and system classpaths.
   */
  public String toString() {
    String[] user = getUserClasspaths();
    StringBuffer sb = new StringBuffer(System.getProperty("java.class.path"));
    char sep = File.pathSeparatorChar;

    for (int i=0; i<user.length; ++i) {
      String s = user[i].toString();
      if (s.startsWith("file:/"))
        s = s.substring(6);
      sb.append(sep);
      sb.append(Lib.toOSPath(s));
    }

    return sb.toString();
  }

  public String handleClassName(String name) {
    String s = (String)handledNames.get(name);
    if (s != null)
      return s;

    try {
      int idx = name.indexOf('.');
      if (idx > 0) {
        s = name;
        return name;
      }

      int len = importList == null ? 0 : importList.size();
      for (int i=0; i<len; ++i) {
        s = (String)importList.get(i);
        if (s.endsWith(".")) { // import ----.*
          try {
            s += name;
            ucl.loadClass(s);
            return s;
          } catch(Exception e) {}
        } else if (s.endsWith("." + name)) { // import ----.---
          return s;
        }
      }

      // try java.lang.*
      try {
        s = "java.lang." + name;
        Class.forName(s);
        return s;
      } catch(Exception e) {}

      // try java.util.*
      try {
        s = "java.util." + name;
        Class.forName(s);
        return s;
      } catch(Exception e) {}

      // try java.io.*
      try {
        s = "java.io." + name;
        Class.forName(s);
        return s;
      } catch(Exception e) {}

      s = name;
      return name;

    } finally {
      handledNames.put(name, s);
    }
  }

} // end of class UserClasspath.
