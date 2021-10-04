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
import org.apache.commons.lang.StringUtils;

public class FileChopUtil
{
  static final int min_size = 20 * 1024;
  static final int buf_size = min_size;
  static final long floppy_sz = 1457664;
  static final long zip100_sz = 100 * 1024 * 1024;
  static final long zip250_sz = 250 * 1024 * 1024;
  static final long CD_sz = 650 * 1024 * 1024;

  static long getSize(String siz) throws NumberFormatException
  {
    if (StringUtils.isBlank(siz)) return floppy_sz;
    if ("floppy".equalsIgnoreCase(siz)) return floppy_sz;
    if ("CD".equalsIgnoreCase(siz)) return CD_sz;
    if ("zip100".equalsIgnoreCase(siz)) return zip100_sz;
    if ("zip250".equalsIgnoreCase(siz)) return zip250_sz;

    int f = 1;
    if (siz.endsWith("K") || siz.endsWith("k")) {
      f = 1024;
      siz = siz.substring(0,siz.length()-1);
    } else if (siz.endsWith("M") || siz.endsWith("m")) {
      f = 1024 * 1024;
      siz = siz.substring(0,siz.length()-1);
    }
    return f * Long.parseLong(siz);
  }

  /**
   *return { num_sub_files, size_last_subfile }
   */
  public static long[] check(String filename) throws Exception {
    return check(filename,"floppy");
  }

  public static long[] check(String filename, String sub_size) throws Exception {
    return check(filename,getSize(sub_size));
  }

  static long[] check(String filename, long sub_size) throws Exception
  {
    File infile = new File(filename);
    long fsize = infile.length();
    System.out.println("File '" + filename + "' size: " + fsize);
    long num_subs = fsize/sub_size;
    long last_sub_size = fsize - num_subs * sub_size;
    if (last_sub_size > 0) ++num_subs;
    return new long[]{ num_subs, last_sub_size };
  }

  /**
   *return { num_sub_files, size_last_subfile }
   */
  public static long[] chop(String filename) throws Exception {
    return chop(filename,"floppy");
  }

  public static long[] chop(String filename, String sub_size) throws Exception {
    return chop(filename,null,getSize(sub_size));
  }

  public static long[] chop(String filename, String subbase, String sub_size) throws Exception {
    return chop(filename,subbase,getSize(sub_size));
  }

  static long[] chop(String filename, String subbase, long sub_size) throws Exception
  {
    if (subbase == null) subbase = filename;

    FileInputStream fis = new FileInputStream(filename);
    byte[] buf = new byte[buf_size];

    boolean done = false;
loop:
    for (int i=1; !done; i++) {
      String subname = subbase + "." + i;
      System.out.print("Writing: " + subname + " ... ");
      FileOutputStream fos = new FileOutputStream(subname);
      long this_sz = 0;
      while (true) {
        int read_sz;
        // figure out to read a whole buffer or only a part.
        int this_read_sz = (int)(sub_size - this_sz);
        if (this_read_sz >= buf.length) read_sz = fis.read(buf);
        else read_sz = fis.read(buf,0,this_read_sz);
        if (read_sz <= 0) { // then the in file has been all read in.
          done = true;
          fos.flush();
          fos.close();
          fos = null;
          System.out.print(this_sz);
          System.out.println(" bytes written.");
          continue loop;
        } else { // write to the out file
          fos.write(buf,0,read_sz);
          this_sz += read_sz;
        }
        if (this_sz >= sub_size) { // this subfile has reached its size.
          fos.flush();
          fos.close();
          fos = null;
          System.out.print(this_sz);
          System.out.println(" bytes written.");
          continue loop;
        }
      }
    }
    fis.close();

    return check(filename,sub_size);
  }

  /**
   *
   */
  public static void unchop(String subbase) { unchop(subbase,null); }

  public static void unchop(String subbase, String outfile)
  {
    if (outfile == null) outfile = subbase;
    try {
      FileOutputStream fos = new FileOutputStream(outfile);
      byte[] buf = new byte[buf_size];

      long written_sz = 0;
loop: for (int i=1; ; i++) {
        FileInputStream fis = null;
        String subname = subbase + "." + i;
        try { fis = new FileInputStream(subname); }
        catch(FileNotFoundException e) { break loop; }
        System.out.println("Reading: " + subname + " ...");
        while (true) {
          int read_sz = fis.read(buf);
          if (read_sz > 0) { // write to the out file
            fos.write(buf,0,read_sz);
          } else { // then the in file has been all read in.
            fis.close();
            fis = null;
            continue loop;
          }
        }
      }
      fos.close();

    } catch(Exception e) { e.printStackTrace(); }
  }

} // end of class FileChop.
