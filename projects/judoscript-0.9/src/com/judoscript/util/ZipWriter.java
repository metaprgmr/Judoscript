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
import java.util.zip.*;
import java.util.jar.*;
import com.ice.tar.*;


public final class ZipWriter
{
  ZipOutputStream zos;
  HashSet entries = new HashSet(); // for dupLeave.
  boolean isJar;
  int method = ZipOutputStream.DEFLATED;
  StreamCopier sc;

  public ZipWriter(ZipOutputStream zos, StreamCopier sc) {
    this.zos = zos;
    this.sc = sc;
    isJar = zos instanceof JarOutputStream;
  }
  public ZipWriter(File f, StreamCopier sc) throws FileNotFoundException {
    this(new ZipOutputStream(new FileOutputStream(f)),sc);
  }
  public ZipWriter(String fname, StreamCopier sc) throws FileNotFoundException {
    this(new ZipOutputStream(new FileOutputStream(fname)),sc);
  }

  public ZipOutputStream getZipOutputStream() { return zos; }

  // ZipOutputStream method delegation
  public void setCompress(boolean set) {
    method = set ? ZipOutputStream.DEFLATED : ZipOutputStream.STORED;
    zos.setMethod(method);
  }
  public void setLevel(int level) { zos.setLevel(level); }
  public void setComment(String cmt) { zos.setComment(cmt); }

  public void close() {
    try { zos.close(); } catch(IOException ieo) {}
    paths = null;
  }

  public static class Dest extends StreamCopier.Dest
  {
    ZipEntry ze;
    ZipOutputStream zos;

    public Dest(ZipOutputStream zos, ZipEntry ze) { this.ze = ze; }
    public void getOutputStream() throws IOException { zos.putNextEntry(ze); }
    public String getName() { return ze.getName(); }
    public void close() throws IOException { zos.closeEntry(); super.close(); }
    public void setName(String n) throws IllegalStateException {
      throw new IllegalStateException("Can not set file names within ZIP archive.");
    }
    public void setTime(long t) { ze.setTime(t); }
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
    if (f instanceof VirtualFS.VirtualFile)
      is = ((VirtualFS.VirtualFile)f).getInputStream();
    else
      is = new FileInputStream(f);
    try {
      ZipEntry ze = createZipEntry(name);
      if (method == ZipOutputStream.STORED) { // Must set these for STORED entries.
        CRC32 crc = new CRC32();
        crc.reset();
        byte[] buf = new byte[1024];
        while (true) {
          int len = is.read(buf);
          if (len <= 0) break;
          crc.update(buf,0,len);
        }
        is.close();
        ze.setCrc(crc.getValue());
        // re-open is:
        if (f instanceof ZipFS.ZippedFile)
          is = ((ZipFS.ZippedFile)f).getInputStream();
        else
          is = new FileInputStream(f);
      }
      long x = f.length();
      if (x > 0)
        ze.setSize(x);
      x = f.lastModified();
      if (x > 0)
        ze.setTime(x);
      ze.setMethod(method);
      if (sc != null)
        sc.copyStream(new StreamCopier.Src(is, ze), new Dest(zos, ze), false);
      else {
        zos.putNextEntry(ze);
        Lib.copyStream(is, zos, false);
        zos.closeEntry();
      }
    } finally {
      try { is.close(); } catch(Exception e) {}
    }
  }

  public void writeFileFromTar(TarInputStream tis, TarEntry te, String name) throws IOException {
    checkPath(name);
    ZipEntry ze = createZipEntry(name);
    ze.setSize(te.getSize());
    ze.setTime(te.getModTime().getTime());
    ze.setMethod(method);
    zos.putNextEntry(ze);
    tis.copyEntryContents(zos);
    zos.closeEntry();
  }

  public ZipEntry createZipEntry(String name) {
    return !isJar ? new ZipEntry(name) : new JarEntry(name);
  }

  void checkPath(String name) throws IOException {
    String path = Lib.getParentPath(name);
    if (path.length() <= 0)
      return;
    if (paths == null)
      paths = new HashSet();
    if (paths.contains(path))
      return;
    Stack stack = new Stack();
    while (path.length() > 0) {
      stack.push(path);
      path = Lib.getParentPath(path);
    }
    while (!stack.empty()) {
      path = (String)stack.pop();
      if (!paths.contains(path)) {
        paths.add(path);
        ZipEntry ze = createZipEntry(path);
        ze.setSize(0);
        ze.setTime(System.currentTimeMillis());
        ze.setMethod(method);
        ze.setCrc(0);
        zos.putNextEntry(ze);
        zos.closeEntry();
      }
    }
  }

  private HashSet paths = null;
}
