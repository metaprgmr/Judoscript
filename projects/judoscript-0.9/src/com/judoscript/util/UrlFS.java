/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 09-02-2002  JH   Inception.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript.util;

import java.io.*;
import java.util.HashMap;
import java.net.URL;
import java.net.URLConnection;

/**
 * This class represents a read-only file system for a URL, which contains just one file.
 * If no file name is found, use "default.htm".
 */
public class UrlFS extends VirtualFS
{
  public UrlFS() {}

  public void close() {}

  public File getFile(String _url) throws Exception {
    return new UrlFile(_url);
  }

  //////////////////////////////////////////////
  // class UrlFile
  //
  public static class UrlFile extends VirtualFS.VirtualFile
  {
    URLConnection urlc = null;
    URL url;
    HashMap urlInfo;

    UrlFile(String _url) throws Exception {
      url = new URL(_url);
      urlc = url.openConnection();
      urlInfo = Lib.parseUrl(_url, null);
    }

    public InputStream getInputStream() throws IOException { return urlc.getInputStream(); }

    public BufferedReader getBufferedReader() throws IOException {
      return new BufferedReader(new InputStreamReader(urlc.getInputStream()));
    }

    public final boolean createNewFile() throws IOException {
      throw new IOException("Can not create new file to a URL.");
    }

    public final long getCompressedSize() { return -1; }
    public final File getParentFile() { return null; }
    public final String getParent() { return null; }
    public final URL toURL() { return url; }
    public final String getName() { return (String)urlInfo.get("file_name"); }
    public final String getPath() {
      String up = (String)urlInfo.get("path");
      int idx = up.lastIndexOf("/");
      if (idx > 0)
        return up.substring(0, idx+1);
      return "";
    }
    public final String getAbsolutePath() { return (String)urlInfo.get("url"); }
    public final File getAbsoluteFile() { return this; }
    public final String getCanonicalPath() { return getAbsolutePath(); }
    public final File getCanonicalFile() { return this; }
    public final long lastModified() { return -1; }
    public final long length() { return -1; }
    public final int compareTo(File f) { return toString().compareTo(f.toString()); }
    public final boolean equals(Object o) { return this == o; }
    public final int hashCode() { return url.hashCode(); }
    public final String toString() { return url.toString(); }
    public final boolean canRead() { return true; }
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

} // end of class UrlFS.

