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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Enumeration;
import java.util.zip.*;
import java.net.URL;
import java.net.MalformedURLException;
import org.apache.commons.lang.StringUtils;

/**
 * This class represents a read-only file system for a zip file.
 */
public class ZipFS extends VirtualFS
{
  public static final String ROOT_NAME = "";

  public ZipFS(String zipFile) throws IOException { this(new ZipFile(zipFile)); }
  public ZipFS(ZipFile zipFile) { init(zipFile); }

  public void close() {
    try {
      zipFile.close();
      zipFile = null;
      root = null;
      nodeMap = null;
    } catch(Exception e) {}
  }

  public File getFile(String path) throws FileNotFoundException {
    File f = (File)nodeMap.get(path);
    if (f == null) {
      if (ROOT_NAME.equals(path))
        return root;
//      throw new FileNotFoundException(zipFile.getName()+" does not contain "+path+".");
    }
    return f;
  }

  ////////////////////////////////////////////////////////////////
  // Impl
  //

  ZipFile zipFile;
  ZippedDirectory root = new ZippedDirectory(null); // internal use only.
  HashMap nodeMap = new HashMap();

  void init(ZipFile zf) {
    zipFile = zf;
    Enumeration en = zf.entries();
    while (en.hasMoreElements()) {
      ZipEntry ze = (ZipEntry)en.nextElement();
      ZippedFile zfd = !ze.isDirectory() ? new ZippedFile(ze) : new ZippedDirectory(ze);
      addChild(ze.getName(), zfd);
    }
  }

  void addChild(String path, ZippedFile zfd) {
    nodeMap.put(path,zfd);
    if (StringUtils.isBlank(path)) {
      root.addChild(zfd);
      return;
    }
    String parent = Lib.getParentPath(path);
    ZippedDirectory zd = (ZippedDirectory)nodeMap.get(parent);
    if (zd == null) {
      zd = new ZippedDirectory(new ZipEntry(parent));
      addChild(parent,zd);
    }
    zd.addChild(zfd);
  }

  //////////////////////////////////////////////
  // class ZippedFile
  //
  public class ZippedFile extends VirtualFS.VirtualFile
  {
    ZipEntry ze;

    ZippedFile(ZipEntry ze) { this.ze = ze; }

    public final long getCompressedSize() { return ze.getCompressedSize(); }

    public final File getParentFile() { return null; }

    public InputStream getInputStream() throws IOException { return zipFile.getInputStream(ze); }

    public ZipFile getZipFile() { return zipFile; }

    public BufferedReader getBufferedReader() throws IOException {
      return new BufferedReader(new InputStreamReader(zipFile.getInputStream(ze)));
    }

    public final String getParent() {
      try { return getParentFile().getName(); } catch(Exception e) {}
      return null;
    }

    public final URL toURL() throws MalformedURLException {
      return new URL("zip:/"+zipFile.getName()+'/'+getName());
    }

    public final boolean createNewFile() throws IOException {
      throw new IOException("Can not create new file in a zip archive.");
    }

    public final String getName() { return ze.getName(); }
    public final String getPath() { return getName(); }
    public final String getAbsolutePath() { return getName(); }
    public final File getAbsoluteFile() { return this; }
    public final String getCanonicalPath() throws IOException { return getName(); }
    public final File getCanonicalFile() throws IOException { return this; }
    public final long lastModified() { return ze.getTime(); }
    public final long length() { return ze.getSize(); }
    public final int compareTo(File f) { return toString().compareTo(f.toString()); }
    public final boolean equals(Object o) { return this == o; }
    public final int hashCode() { return toString().hashCode(); }
    public final String toString() { return getName(); }
    public final boolean canRead() { return isFile(); }
    public final boolean canWrite() { return false; }
    public final boolean isAbsolute() { return true; } // always.
    public final boolean exists() { return true; }
    public final boolean isHidden() { return false; }
    public final boolean delete() { return false; }
    public final void deleteOnExit() {}
    public final boolean mkdir() { return false; }
    public final boolean mkdirs() { return false; }
    public final boolean renameTo(File f) { return false; }
    public final boolean setLastModified(long l) { return false; }
    public final boolean setReadOnly() { return true; }

    public boolean isDirectory() { return false; }
    public boolean isFile() { return true; }
    public String list()[] { return null; }
    public String list(FilenameFilter f)[] { return null; }
    public File listFiles()[] { return null; }
    public File listFiles(FilenameFilter f)[] { return null; }
    public File listFiles(FileFilter f)[] { return null; }
  }

  //////////////////////////////////////////////
  // class ZippedDirectory
  //
  public final class ZippedDirectory extends ZippedFile
  {
    ArrayList children = null;

    ZippedDirectory(ZipEntry ze) { super(ze); }

    void addChild(ZippedFile zp) {
      if (children == null)
        children = new ArrayList();
      children.add(zp);
    }

    public boolean isDirectory() { return true; }
    public boolean isFile() { return false; }

    public String[] list() {
      return list(null);
    }

    public String[] list(FilenameFilter filter) {
      int len = (children==null) ? 0 : children.size();
      if (len == 0)
        return null;
      ArrayList ar = new ArrayList();
      for (int i=0; i<len; i++) {
        String x = children.get(i).toString();
        if (filter == null || filter.accept(null,x))
          ar.add(x);
      }
      if (ar.size() == 0)
        return null;
      String[] sa = new String[ar.size()];
      ar.toArray(sa);
      return sa;
    }

    public File[] listFiles() {
      return listFiles((FilenameFilter)null);
    }

    public File[] listFiles(FilenameFilter filter) {
      int len = (children==null) ? 0 : children.size();
      if (len == 0)
        return null;
      ArrayList ar = new ArrayList();
      for (int i=0; i<len; i++) {
        File f = (File)children.get(i);
        if (filter == null || filter.accept(null,f.getPath()))
          ar.add(f);
      }
      if (ar.size() == 0)
        return null;
      File[] fa = new File[ar.size()];
      ar.toArray(fa);
      return fa;
    }

    public File[] listFiles(FileFilter filter) {
      int len = (children==null) ? 0 : children.size();
      if (len == 0)
        return null;
      ArrayList ar = new ArrayList();
      for (int i=0; i<len; i++) {
        File f = (File)children.get(i);
        if (filter.accept(f))
          ar.add(f);
      }
      if (ar.size() == 0)
        return null;
      File[] fa = new File[ar.size()];
      ar.toArray(fa);
      return fa;
    }
  }

/*
  public static void main(String[] args) {
    try {
      ZipFS zfs = new ZipFS(args[0]);
    } catch(Exception e) { e.printStackTrace(); }
  }
*/

} // end of class ZipFS.

