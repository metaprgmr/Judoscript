/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 03-17-2002  JH   Initial open source release.
 * 09-25-2002  JH   Changed to allow no-write on the output stream.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript.util;

import java.io.*;
import java.util.Date;
import java.util.zip.ZipEntry;


public interface StreamCopier
{
  public void copyStream(Src src, Dest dest, boolean close) throws IOException;

  public static class Src extends FilterInputStream
  {
    String name;
    long time;
    long size;
    boolean isFile;
    boolean fromZip;

    public Src(InputStream is, ZipEntry ze) {
      this(is, ze.getName(), ze.getTime(), ze.getSize(), !ze.isDirectory(), true);
    }

    public Src(InputStream is, File f) {
      this(is, f.getPath(), f.lastModified(), f.length(), f.isFile(), false);
    }

    public Src(InputStream is, String name, long time, long size, boolean isFile, boolean fromZip) {
      super(is);
      this.name = name;
      this.time = time;
      this.size = size;
      this.isFile = isFile;
      this.fromZip = fromZip;
    }

    public String getName() { return name; }
    public Date   getTime() { return new Date(time); }
    public long   getSize() { return size; }
    public boolean getIsFile() { return isFile; }
    public boolean getFromZip() { return fromZip; }

  } // end of inner class Src.

  public static abstract class Dest extends OutputStream
  {
    OutputStream os = null;

    public abstract void getOutputStream() throws IOException;
    public abstract String getName();
    public abstract void setName(String name) throws IllegalStateException;
    public abstract void setTime(long time) throws IllegalStateException;

    public long copy(InputStream is) throws IOException {
      getOutputStream();
      int ch;
      long cnt = 0;
      for (; ((ch = is.read()) != -1); ++cnt) {
        os.write(ch);
      }
      return cnt;
    }

    public void write(byte[] ba) throws IOException {
      try { os.write(ba); }
      catch(NullPointerException npe) {
        getOutputStream();
        os.write(ba);
      }
    }

    public void write(byte[] ba, int off, int len) throws IOException {
      try { os.write(ba,off,len); }
      catch(NullPointerException npe) {
        getOutputStream();
        os.write(ba,off,len);
      }
    }

    public void write(int b) throws IOException {
      try { os.write(b); }
      catch(NullPointerException npe) {
        getOutputStream();
        os.write(b);
      }
    }

    public void flush() throws IOException {
      try { os.flush(); } catch(NullPointerException npe) {}
    }

    public void close() throws IOException {
      try { os.close(); } catch(NullPointerException npe) {}
    }

  } // end of inner class Dest.

  public static class FileDest extends Dest
  {
    File file;

    public FileDest(File f) { file = f; }
    public void getOutputStream() throws IOException {
      if (os == null) os = new FileOutputStream(file);
    }
    public String getName() { return file.getPath(); }
    public void setName(String name) throws IllegalStateException {
      if (os != null) throw new IllegalStateException("Can't set file name because it is opened already.");
      file = new File(name);
    }
    public void setTime(long time) throws IllegalStateException {
      if (os != null) throw new IllegalStateException("Can't set file time because it is opened already.");
      file.setLastModified(time);
    }
  }

} // end of interface StreamCopier.
