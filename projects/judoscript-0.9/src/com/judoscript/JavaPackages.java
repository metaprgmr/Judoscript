/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 03-17-2002  JH   Initial open source release.
 * 12-19-2002  JH   Uses properties files.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript;

import java.util.*;

public class JavaPackages
{
  static HashMap packages = new HashMap();
  static HashMap definedClasses = new HashMap();

  static List loadJavaPackageAlias(Properties p, String pkgAlias) {
    Iterator itor = p.keySet().iterator();
    while (itor.hasNext()) {
      String k = (String)itor.next();
      if (k.equals(pkgAlias)) {
        StringTokenizer st = new StringTokenizer((String)p.get(k), " ");
        List l = new ArrayList();
        while (st.hasMoreTokens())
          l.add(st.nextToken());
        packages.put(k,l);
        return l;
      }
    }
    return null;
  }

  public static List getPackageCollection(String pkgId) {
    List l = (List)packages.get(pkgId);
    if (l == null) {
      Properties p;
      p = JudoUtil.loadProperties("javapkgs", false);
      l = loadJavaPackageAlias(p,pkgId);
      if (l == null) {
        p = JudoUtil.loadProperties("myjavapkgs", false);
        if (p != null)
          l = loadJavaPackageAlias(p,pkgId);
      }
    }
    return l;
  }

  public static void addPackageDef(String pkgId, String javaPkg) {
    List l = getPackageCollection(pkgId);
    if (l == null) {
      l = new ArrayList();
      packages.put(pkgId,l);
    }
    if (!javaPkg.endsWith(".")) javaPkg += '.';
    l.add(javaPkg);
  }

  public static Class matchClass(String pkgId, String className) {
    List l = getPackageCollection(pkgId);
    if (l == null) return null;
    
    for (int i=0; i<l.size(); ++i) {
      try {
        String n = ((String)l.get(i)) + className;
        Class c = (Class)definedClasses.get(n);
        if (c != null) return c;
        c = RT.getSysClass(n);
        definedClasses.put(n,c);
        return c;
      } catch(Exception e) {}
    }
    return null;
  }

} // end of class JavaPackages.
