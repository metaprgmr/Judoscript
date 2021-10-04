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

public abstract class VirtualFS
{
  public abstract File getFile(String url) throws Exception;

  public InputStream getInputStream(VirtualFile vf) throws IOException {
    return vf.getInputStream();
  }

  // Inner class
  public static abstract class VirtualFile extends File
  {
    protected VirtualFile() { super(""); }

    public abstract InputStream getInputStream() throws IOException;
  }
}
