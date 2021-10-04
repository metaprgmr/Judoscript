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
import java.util.zip.GZIPInputStream;
import java.net.URL;
import java.net.MalformedURLException;
import org.apache.commons.lang.StringUtils;
import com.ice.tar.*;

/**
 * This class represents a read-only file system for a tar file.
 * It is only good for listing and searching, not copying.
 */
public class TarFS extends VirtualFS
{
  static TarInputStream getTarInputStream(String tarFile) throws IOException {
    FileInputStream fis = new FileInputStream(tarFile);
    String x = tarFile.toLowerCase();
    return new TarInputStream( (x.endsWith(".tar.gz") || x.endsWith(".taz"))
                               ? (InputStream)new GZIPInputStream(fis) : fis );
  }

  public TarFS(String tarFile) throws IOException {
    this(getTarInputStream(tarFile), tarFile);
    tarFilename = tarFile;
  }

  public TarFS(TarInputStream tar, String tarFile) throws IOException {
    init(tar);
    tarFilename = tarFile;
  }

  public void close() {
    try {
      root = null;
      nodeMap = null;
    } catch(Exception e) {}
  }

  public File getFile(String path) throws FileNotFoundException {
    File f = (File)nodeMap.get(path);
    if (f == null) {
      if (ZipFS.ROOT_NAME.equals(path))
        return root;
//      throw new FileNotFoundException(tarFilename+" does not contain "+path+".");
    }
    return f;
  }

  ////////////////////////////////////////////////////////////////
  // Impl
  //

  String tarFilename;
  TarredDirectory root = new TarredDirectory(null); // internal use only.
  HashMap nodeMap = new HashMap();

  public String getTarFileName() { return tarFilename; }

  void init(TarInputStream tis) throws IOException {
    TarEntry te = tis.getNextEntry();
    while (te != null) {
      TarredFile tfd = !te.isDirectory() ? new TarredFile(te) : new TarredDirectory(te);
      addChild(te.getName(), tfd);
      te = tis.getNextEntry();
    }
  }

  void addChild(String path, TarredFile tfd) {
    nodeMap.put(path,tfd);
    if (StringUtils.isBlank(path)) {
      root.addChild(tfd);
      return;
    }
    String parent = Lib.getParentPath(path);
    TarredDirectory zd = (TarredDirectory)nodeMap.get(parent);
    if (zd == null) {
      zd = new TarredDirectory(new TarEntry(parent));
      addChild(parent,zd);
    }
    zd.addChild(tfd);
  }

  //////////////////////////////////////////////
  // class TarredFile
  //
  public class TarredFile extends VirtualFS.VirtualFile
  {
    TarEntry te;

    TarredFile(TarEntry te) { this.te = te; }

    public final long getCompressedSize() { return te.getSize(); }

    public final File getParentFile() { return null; }

    public InputStream getInputStream() throws IOException {
      throw new IOException("Tarred files can not be read.");
    }

    public final String getParent() {
      try { return getParentFile().getName(); } catch(Exception e) {}
      return null;
    }

    public final URL toURL() throws MalformedURLException {
      return new URL("tar:/"+tarFilename+'/'+getName());
    }

    public final boolean createNewFile() throws IOException {
      throw new IOException("Can not create new file in a tar archive.");
    }

    public final String getName() { return te.getName(); }
    public final String getPath() { return getName(); }
    public final String getAbsolutePath() { return getName(); }
    public final File getAbsoluteFile() { return this; }
    public final String getCanonicalPath() throws IOException { return getName(); }
    public final File getCanonicalFile() throws IOException { return this; }
    public final long lastModified() { return te.getModTime().getTime(); }
    public final long length() { return te.getSize(); }
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
  // class TarredDirectory
  //
  public final class TarredDirectory extends TarredFile
  {
    ArrayList children = null;

    TarredDirectory(TarEntry te) { super(te); }

    void addChild(TarredFile zp) {
      if (children == null) children = new ArrayList();
      children.add(zp);
    }

    public boolean isDirectory() { return true; }
    public boolean isFile() { return false; }

    public String[] list() {
      int len = (children==null) ? 0 : children.size();
      if (len == 0) return null;
      String[] sa = new String[len];
      for (int i=0; i<len; i++)
        sa[i] = children.get(i).toString();
      return sa;
    }

    public String[] list(FilenameFilter filter) {
      int len = (children==null) ? 0 : children.size();
      if (len == 0) return null;
      ArrayList ar = new ArrayList();
      for (int i=0; i<len; i++) {
        String x = children.get(i).toString();
        if (filter.accept(null,x))
          ar.add(x);
      }
      if (ar.size() == 0) return null;
      String[] sa = new String[ar.size()];
      ar.toArray(sa);
      return sa;
    }

    public File[] listFiles() {
      int len = (children==null) ? 0 : children.size();
      if (len == 0) return null;
      File[] fa = new File[len];
      for (int i=0; i<len; i++)
        fa[i] = (File)children.get(i);
      return fa;
    }

    public File[] listFiles(FilenameFilter filter) {
      int len = (children==null) ? 0 : children.size();
      if (len == 0) return null;
      ArrayList ar = new ArrayList();
      for (int i=0; i<len; i++) {
        File f = (File)children.get(i);
        if (filter.accept(null,f.getPath()))
          ar.add(f);
      }
      if (ar.size() == 0) return null;
      File[] fa = new File[ar.size()];
      ar.toArray(fa);
      return fa;
    }

    public File[] listFiles(FileFilter filter) {
      int len = (children==null) ? 0 : children.size();
      if (len == 0) return null;
      ArrayList ar = new ArrayList();
      for (int i=0; i<len; i++) {
        File f = (File)children.get(i);
        if (filter.accept(f))
          ar.add(f);
      }
      if (ar.size() == 0) return null;
      File[] fa = new File[ar.size()];
      ar.toArray(fa);
      return fa;
    }
  }

/*
  public static void main(String[] args) {
    try {
      TarFS zfs = new TarFS(args[0]);
    } catch(Exception e) { e.printStackTrace(); }
  }
*/

} // end of class TarFS.

