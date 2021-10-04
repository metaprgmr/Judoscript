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


public class FileClassLoader extends ClassLoader
{
  public FileClassLoader() {}

  public Class getClass(String name, String path) throws ClassNotFoundException {
    try {
      File f = new File(path);
      int len = (int)f.length();
      FileInputStream fis = new FileInputStream(f);
      byte[] b = new byte[len];
      int bytes = (int)fis.read(b,0,len);
      fis.close();
      return defineClass(name, b, 0, bytes);
    } catch(IOException e) {
      throw new ClassNotFoundException("Class file for '" + name + "' is not found.");
    } catch(ClassFormatError cfe) {
      throw new ClassNotFoundException("Class file for '" + name + "' has format errors.");
    }
  }

} // end of class FileClassLoader.

