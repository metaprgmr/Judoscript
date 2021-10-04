/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 04-15-2004  JH   Inception.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript.util;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import org.apache.commons.lang.StringUtils;

public class NamedReader extends Reader
{
  String name;
  Reader reader;

  public NamedReader(boolean isBuffered, Reader r, String name) {
    this.name = name;
    reader = (isBuffered && !(r instanceof BufferedReader)) ? new BufferedReader(r) : r;
  }

  public NamedReader(boolean isBuffered, InputStream is, String name) {
    this(isBuffered, new InputStreamReader(is), name);
  }

  public NamedReader(boolean isBuffered, InputStream is, String name, String enc)
  	throws UnsupportedEncodingException
  {
    this(isBuffered, StringUtils.isBlank(enc) ? new InputStreamReader(is) : new InputStreamReader(is, enc), name);
  }

  public NamedReader(boolean isBuffered, URL url) throws IOException {
    this(isBuffered, url, url.openConnection());
  }

  private NamedReader(boolean isBuffered, URL url, URLConnection urlc) throws IOException {
  	this(isBuffered, urlc.getInputStream(), url.toString(), urlc.getContentEncoding());
  }

  public NamedReader(boolean isBuffered, File f) throws FileNotFoundException {
    this(isBuffered, new FileReader(f), f.getAbsolutePath().replace('\\','/'));
  }

  public NamedReader(boolean isBuffered, File f, String enc) throws IOException {
    this(isBuffered,
         StringUtils.isBlank(enc) ? (Reader)new FileReader(f) : new InputStreamReader(new FileInputStream(f), enc),
         f.getAbsolutePath().replace('\\','/'));
  }

  public NamedReader(boolean isBuffered, String fn) throws FileNotFoundException {
    this(isBuffered, new File(fn));
  }

  public NamedReader(URL url, String enc) throws IOException { this(url.openStream(), enc); }

  public NamedReader(Reader r, String name) { this(false,r,name); }
  public NamedReader(InputStream is, String name) { this(false,is,name); }
  public NamedReader(URL url) throws IOException { this(false,url); }
  public NamedReader(File f) throws FileNotFoundException { this(false, f); }
  public NamedReader(String fn) throws FileNotFoundException { this(false,fn); }
  public NamedReader(String fn, String enc) throws IOException { this(false, new File(fn), enc); }

  public String getName() { return name; }
  public Reader getReader() { return reader; }

  public int read() throws IOException { return reader.read(); }
  public int read(char[] a) throws IOException { return reader.read(a); }
  public int read(char[] a, int b, int c) throws IOException { return reader.read(a,b,c); }
  public long skip(long off) throws IOException { return reader.skip(off); }
  public boolean ready() throws IOException { return reader.ready(); }
  public boolean markSupported() { return reader.markSupported(); }
  public void mark(int m) throws IOException { reader.mark(m); }
  public void reset() throws IOException { reader.reset(); }
  public void close() throws IOException { reader.close(); }

} // end of class NamedReader.
