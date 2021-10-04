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

import java.io.PrintWriter;
import java.io.File;
import java.io.IOException;

public final class ListPrinter implements ListReceiver
{
  PrintWriter pw;

  public ListPrinter(PrintWriter pw) { this.pw = pw; }
  public void receive(Object file) {
    if (file instanceof File) {
      try {
        pw.println(((File)file).getCanonicalPath().replace('\\','/'));
      } catch(IOException ioe) {
        pw.println(((File)file).getAbsolutePath().replace('\\','/'));
      }
    }
    pw.flush();
  }
  public void finish() {}
}
