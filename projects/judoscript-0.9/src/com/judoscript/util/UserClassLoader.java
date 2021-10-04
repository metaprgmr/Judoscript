/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 08-08-2004  JH   Inception.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/

package com.judoscript.util;

import java.net.URL;
import java.net.URLClassLoader;


public class UserClassLoader extends URLClassLoader 
{
  // Set to true if this classloader is set to the main thread.
  //
  public static transient boolean isSystemLoader = false;

  private boolean used = false;

  public UserClassLoader(URL[] bases) { super(bases==null ? new URL[0] : bases); }

  public void add(URL url) { super.addURL(url); }

  /**
   * Load class from URLs first; if not, load by the parent.
   */
  public synchronized Class loadClass(String name) throws ClassNotFoundException {
    used = true;

    Class c = findLoadedClass(name);
    if (c != null)
      return c;

    try {
      return super.loadClass(name);
    } catch ( ClassNotFoundException e ) {}

    if (isSystemLoader)
      return getParent().loadClass(name);

    try { return getParent().loadClass(name); } catch ( ClassNotFoundException e ) {}
    return Thread.currentThread().getContextClassLoader().loadClass(name);
  }

  /**
   * Find the resource from URLs first; if not, find by the parent.
   */
  public URL getResource(String name) {
    used = true;

    URL url = super.getResource(name);
    if (url != null)
      return url;

    url = getParent().getResource(name);
    if (url != null)
      return url;

    if (!isSystemLoader)
      url = Thread.currentThread().getContextClassLoader().getResource(name);

    return url;
  }

  public boolean isUsed() { return used; }

} // end of private class UserClassLoader.
