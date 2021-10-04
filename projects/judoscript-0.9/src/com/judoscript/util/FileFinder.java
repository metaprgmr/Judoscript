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


package com.judoscript.util;

import java.io.*;
import java.util.*;
import java.util.zip.ZipFile;
import org.apache.commons.lang.StringUtils;


public class FileFinder implements FileFilter
{
  public static final int LIST_EVERYTHING = 0;
  public static final int LIST_FILE_ONLY  = 1;
  public static final int LIST_DIR_ONLY   = 2;

  public static final int FST_UNKNOWN = 0;
  public static final int FST_LOCALFS = 1;
  public static final int FST_ZIPFILE = 2;
  public static final int FST_TARFILE = 3;

  Object  host; // null (for local fs), ZipFS or TarFS.
  String  base;
  boolean expandDirs = true;
  boolean recursive;
  boolean noHidden;
  boolean caseSens;

  ArrayList fileOrDirs;
  WildcardMatcher excludes;
  HashMap patterns;

  HashSet processed = null;


  protected FileFinder() {}

  public FileFinder(Object host, String base, String incls, String excls, boolean recursive,
                    boolean noHidden, boolean caseSens) throws IllegalArgumentException
  {
    this.host = host;
    this.base = base;
    this.recursive = recursive;
    this.noHidden  = noHidden;
    this.caseSens  = caseSens;

    // handle base:
    if (host == null) {
      if (StringUtils.isBlank(base))
        throw new IllegalArgumentException("Base directory is not specified.");
      base = base.replace('\\', '/');
      if (!base.endsWith("/")) base += "/";
    }

    init(incls, excls);
  }

  public FileFinder(String base, String incls, String excls, boolean recursive, boolean noHidden)
                   throws IllegalArgumentException
  {
    this(null, base, incls, excls, recursive, noHidden, !Lib.isWindows());
  }

  public FileFinder(ZipFile arch, String incls, String excls, boolean recursive, boolean noHidden)
                   throws IllegalArgumentException
  {
    this(new ZipFS(arch), "", incls, excls, recursive, noHidden, true);
  }

  public FileFinder(TarFS arch, String incls, String excls, boolean recursive, boolean noHidden)
                   throws IllegalArgumentException
  {
    this(arch, "", incls, excls, recursive, noHidden, true);
  }

  public FileFinder(UrlFS urlfs, String url) throws IllegalArgumentException
  {
    this(urlfs, "", url, null, false, false, true);
  }

  public void close() {
    if (host instanceof ZipFS)
      ((ZipFS)host).close();
    else if (host instanceof TarFS)
      ((TarFS)host).close();
  }

  public String getBaseDir() { return base; }
  public void setExpandDirs(boolean set) { expandDirs = set; }
  public boolean isLocal() { return host == null; }
  public boolean isZipFS() { try { return host instanceof ZipFS; } catch(Exception e) { return false; } }
  public boolean isTarFS() { try { return host instanceof TarFS; } catch(Exception e) { return false; } }
  public boolean isUrlFS() { try { return host instanceof UrlFS; } catch(Exception e) { return false; } }
  public String getTarFileName() {
    try { return ((TarFS)host).getTarFileName(); } catch(Exception e) {}
    return null;
  }
  public Object getHost() { return null; }

  void init(String incls, String excls) {
    fileOrDirs = null;

    StringTokenizer st;
    String s;

    // handle excludes: turn commas into vertical-bars,
    // make each pattern an absolute path, then concatenate them with '|'
    // and finally parse it into a WildcardMatcher.
    if (StringUtils.isBlank(excls)) {
      excludes = null;
    } else {
      st = new StringTokenizer(excls.replace('\\', '/'), ",");
      StringBuffer sb = new StringBuffer();
      boolean start = true;
      while (st.hasMoreTokens()) {
        s = st.nextToken().trim();
        if (!Lib.isAbsolutePath(s)) { // always make excludes all absolute
          if (isLocal())
            s = base + s;
        }
        if (start)
          start = false;
        else
          sb.append('|');
        sb.append(s);
      }
      excludes = new WildcardMatcher(sb.toString(), caseSens);
    }

    // handle patterns: collect the longest paths as keys into 'patterns';
    //                  if relative, prepend 'base'.
    patterns = new HashMap();
    if (StringUtils.isBlank(incls))
      incls = "*";
    st = new StringTokenizer(incls.replace('\\', '/'), ",");
    while (st.hasMoreTokens()) {
      s = st.nextToken().trim();
      if (!Lib.isAbsolutePath(s)) // always make it absolute.
        s = base + s; // for ZipFS/TarFS, it's "" anyways.
      if (s.indexOf("://") > 0 || (s.indexOf('*') < 0) && (s.indexOf('?') < 0)) {
        // if not a pattern, add to fileOrDirs as absolute path.
        if (fileOrDirs == null)
          fileOrDirs = new ArrayList();
        fileOrDirs.add(s);
      } else {
        // collect the longest root paths of each pattern for
        // listing and associate all patterns with that root path;
        // This association is not used yet but in the future may
        // for performance enhancement.
        WildcardMatcher wm = new WildcardMatcher(s, caseSens);
        int idx1 = s.indexOf("*");
        int idx2 = s.indexOf("?");
        if ((idx1>idx2) && (idx2>=0))
          idx1 = idx2;
        idx2 = (idx1<0) ? s.lastIndexOf("/") : s.lastIndexOf("/", idx1);
        if (idx2 >= 0) {
          s = s.substring(0, idx2+1);
        } else { // in local fs, base always prepended so it never comes here.
          s = ZipFS.ROOT_NAME;
        }
        if (!patterns.containsKey(s))
          patterns.put(s, wm);
        else
          ((WildcardMatcher)patterns.get(s)).append(wm);
      }
    }
  }

  // FileFilter method
  public final boolean accept(File file) { return accept((Object)file); }

  public final boolean accept(Object file) {
    String path = getPath(file);
    if (processed.contains(path))
      return false;
    processed.add(path);
    if (noHidden && isHidden(file))
      return false;
    if ((excludes != null) && excludes.match(path))
      return false;
    if (recursive && isFolder(file))
      return true;
    Iterator values = patterns.values().iterator();
    if (!values.hasNext())
      return true;
    while (values.hasNext()) {
      WildcardMatcher wm = (WildcardMatcher)values.next();
      if (wm.match(path))
        return true;
    }
    return false;
  }

  private ListReceiver listReceiver = null;
  private int listOption = 0;
  private int proc_count = 0;

  public int list(ListReceiver lr, int listOption, int limit) throws Throwable {
    try {
      listReceiver = lr;
      this.listOption = listOption;

      Object file;
      proc_count = 0;
      HashSet set = new HashSet();
      Stack stack = new Stack();
      processed = new HashSet();

      // for specified files and/or directories:
      if (fileOrDirs != null) {
        for (int vi = fileOrDirs.size()-1; vi >= 0; --vi) {
          file = getFile((String)fileOrDirs.get(vi));
          if (file==null)
            continue;
          if (!isFolder(file) || !expandDirs) {
            stack.push(file);
          } else {
            Object[] fa = listChildren(file,this);
            for (int i=0; i<fa.length; stack.push(fa[i++]));
          }
        }
        if (!processStack(stack, set, limit))
          return proc_count;
      }

      // for patterns
      Iterator bases = patterns.keySet().iterator();
      while (bases.hasNext()) {
        file = getFile((String)bases.next());
        if (file==null)
          continue;
        if (!isFolder(file)) {
          stack.push(file);
        } else {
          Object[] fa = listChildren(file,this);
          for (int i=0; i<fa.length; stack.push(fa[i++]));
        }
      }
      processStack(stack, set, limit);
    } catch(BreakException be) {
    } finally {
      processed.clear();
      processed = null;
      if (listReceiver != null) {
        listReceiver.finish();
        listReceiver = null;
      }
    }

    return proc_count;
  }

  private boolean processStack(Stack stack, HashSet set, int limit) throws Throwable {
    while (!stack.empty()) {
      Object file = (Object)stack.pop();
      if (exists(file)) {
        if (!process(file, limit))
          return false;
        if (limit > 0 && proc_count >= limit)
          return false;
        String abspath = getPath(file);
        if (set.contains(abspath))
          continue;
        set.add(abspath);
        if (recursive && isFolder(file)) {
          Object[] fa = listChildren(file,this);
          for (int i=0; i<fa.length; stack.push(fa[i++]));
        }
      }
    }
    return true;
  }

  protected boolean process(Object src, int limit) throws Throwable {
    if (limit > 0 && proc_count >= limit)
      return false;

    if (isFolder(src)) {
      if (listOption==LIST_FILE_ONLY)
        return true;
    } else if (listOption==LIST_DIR_ONLY)
      return true;

    if (listReceiver != null) {
      listReceiver.receive(src);
      ++proc_count;
    }
    return true;
  }

  protected boolean isFolder(Object file) {
    try { return ((File)file).isDirectory(); } catch(Exception e) {}
    return false;
  }

  protected String getPath(Object file) {
    try { return ((File)file).getCanonicalPath().replace('\\','/'); } catch(IOException e) {}
    return null;
  }

  protected boolean exists(Object file) {
    try { return ((File)file).exists(); } catch(Exception e) {}
    return false;
  }

  protected boolean isHidden(Object file) {
    try { return ((File)file).isHidden(); } catch(Exception e) {}
    return false;
  }

  protected Object[] listChildren(Object file, FileFinder filter) {
    Object[] fa = null;
    try {
      fa = (Object[])((File)file).listFiles((FileFilter)filter);
    } catch(Exception e) {}
    return (fa==null) ? new Object[0] : fa;
  }

  public File getFile(String path) throws Exception {
    if (host == null)
      return new File(path);
    return ((VirtualFS)host).getFile(path);
  }

  public static void breakSearch() throws BreakException {
    throw new BreakException();
  }

  static class BreakException extends Exception
  {
    BreakException() { super(); }
  }

/***
  public static void main(String[] args) {
    try {
       ZipFile zf = new ZipFile(args[0]);
       FileFinder ff = new FileFinder(zf, "L*.java", null, false, false);
       ff.list(new ListPrinter(new java.io.PrintWriter(System.out)), 0);
    } catch(Exception e) {
      e.printStackTrace();
    }
  }
***/

} // end of class FileFinder.

