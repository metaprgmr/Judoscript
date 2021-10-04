/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 03-17-2002  JH   Initial open source release.
 * 05-13-2002  JH   Fixed a null-pointer in c'tor.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript.util;

import java.io.*;
import java.util.*;
import java.util.zip.GZIPOutputStream;
import com.ice.tar.*;


public final class TarWriter
{
  TarOutputStream tos;
  HashSet entries = new HashSet(); // for dupOk.
  StreamCopier sc;
  public int groupID = 0;
  public int userID  = 0;
  public String groupName = null;
  public String userName  = null;

  public TarWriter(TarOutputStream tos, StreamCopier sc) { this.tos = tos; this.sc = sc; }
  public TarWriter(File f, StreamCopier sc) throws IOException { this(f.getAbsolutePath(),sc); }
  public TarWriter(String fname, StreamCopier sc) throws IOException {
    this.sc = sc;
    String x = fname.toLowerCase();
    if (x.endsWith(".tar.gz") || x.endsWith(".taz"))
      tos = new TarOutputStream(new GZIPOutputStream(new FileOutputStream(fname)));
    else
      tos = new TarOutputStream(new FileOutputStream(fname));
  }

  public void close() {
    try { tos.close(); } catch(IOException ieo) {}
    paths = null;
  }

  public static class Dest extends StreamCopier.Dest
  {
    TarEntry te;
    TarOutputStream tos;

    public Dest(TarOutputStream tos, TarEntry te) { this.te = te; this.tos = tos; }
    public void getOutputStream() throws IOException { tos.putNextEntry(te); }
    public String getName() { return te.getName(); }
    public void close() throws IOException { tos.closeEntry(); super.close(); }
    public void setName(String n) { te.setName(n); }
    public void setTime(long t) { te.setModTime(t); }
  }

  public void writeFile(File f, String name, boolean dupOk) throws IOException {
    checkPath(name);
    if (dupOk) {
      if (entries.contains(name))
        return;
      else
        entries.add(name);
    }
    InputStream is;
    File tempFile = null;

    if (f instanceof VirtualFS.VirtualFile)
      is = ((VirtualFS.VirtualFile)f).getInputStream();
    else
      is = new FileInputStream(f);
    TarEntry te = new TarEntry(name);
    long x = f.lastModified();
    te.setModTime(x>0 ? x : System.currentTimeMillis());
    if (groupID != 0)
      te.setGroupId(groupID);
    if (userID != 0)
      te.setUserId(userID);
    if (groupName != null)
      te.setGroupName(groupName);
    if (userName != null)
      te.setUserName(userName);

    x = f.length();
    if (x > 0)
      te.setSize(x);
    else {
      // We have to copy the stream into a temp file first,
      // get the byte counts, then serialize into this tar entry.
      tempFile = File.createTempFile("judotar", null);
      FileOutputStream fos = new FileOutputStream(tempFile);
      Lib.copyStream(is, fos, true);

      te.setSize(tempFile.length());
      is = new FileInputStream(tempFile);
    }
    try {
      if (sc != null) {
        sc.copyStream(new StreamCopier.Src(is, f), new Dest(tos, te), false);
      } else {
        tos.putNextEntry(te);
        Lib.copyStream(is, tos, false);
        tos.closeEntry();
      }
    } finally {
      try { is.close(); } catch(Exception e) {}
      try {
        if (tempFile != null)
          tempFile.delete();
      } catch(Exception e) {}
    }
  }

  public void writeFileFromTar(TarInputStream tis, TarEntry te, String name) throws IOException {
    checkPath(name);
    TarEntry nte = new TarEntry(name);
    nte.setSize(te.getSize());
    nte.setModTime(te.getModTime());
    tos.putNextEntry(nte);
    tis.copyEntryContents(tos);
    tos.closeEntry();
  }

  void checkPath(String name) throws IOException {
    String path = Lib.getParentPath(name);
    if (path.length() <= 0) return;
    if (paths == null) paths = new HashSet();
    if (paths.contains(path)) return;
    Stack stack = new Stack();
    while (path.length() > 0) {
      stack.push(path);
      path = Lib.getParentPath(path);
    }
    while (!stack.empty()) {
      path = (String)stack.pop();
      if (!paths.contains(path)) {
        paths.add(path);
        TarEntry te = new TarEntry(path);
        te.setSize(0);
        te.setModTime(System.currentTimeMillis());
        tos.putNextEntry(te);
        tos.closeEntry();
      }
    }
  }

  private HashSet paths = null;
}
