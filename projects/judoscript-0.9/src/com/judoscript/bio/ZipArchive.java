/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 03-17-2002  JH   Initial open source release.
 * 08-16-2002  JH   Added getInputStream() and getBufferedReader() methods.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript.bio;

import java.io.*;
import java.util.zip.*;
import org.apache.commons.lang.StringUtils;
import com.judoscript.*;
import com.judoscript.util.*;


public final class ZipArchive extends JavaObject
{
  public ZipArchive(ZipFile zf) { super(zf); }

  public Variable invoke(String fxn, Expr[] args, int[] javaTypes) throws Throwable
  {
    int len = (args==null) ? 0 : args.length;
    int ord = getMethodOrdinal(fxn);
    ZipEntry ze = null;
    if (ord > 0) {
      if (len == 0)
        ExceptionRuntime.rte(RTERR_ILLEGAL_ARGUMENTS,
          "Zip file " + fxn + "() method takes a parameter of file name.");
      ze = ((ZipFile)object).getEntry(args[0].getStringValue());
      if (ze == null) return ValueSpecial.UNDEFINED;
    }
    switch(ord) {
    case BIM_OPENTEXTFILE:
    case BIM_OPENFILE:
      Object is = ((ZipFile)object).getInputStream(ze);
      if (ord == BIM_OPENTEXTFILE) {
        String encoding = (len > 1) ? args[1].getStringValue() : null;
        is = StringUtils.isBlank(encoding)
             ? new BufferedReader(new InputStreamReader((InputStream)is))
             : new BufferedReader(new InputStreamReader((InputStream)is,encoding));
      }
      return new IODevice(is, ze.getSize(), ze.getTime());
    case BIM_FILEISDIRECTORY:
    case BIM_ISFILE:
      boolean isdir = ze.isDirectory();
      return ConstInt.getBool((ord==BIM_ISFILE) ? !isdir : isdir);
    case BIM_FILETIME:           return new _Date(ze.getTime());
    case BIM_FILELENGTH:         return ConstInt.getInt(ze.getSize());
    case BIM_FILECOMPRESSEDSIZE: return ConstInt.getInt(ze.getCompressedSize());
    case BIM_FILEEXISTS:         return ConstInt.TRUE; // otherwise, it is null.
      
    default:
      return super.invoke(fxn,args,javaTypes);
    }
  }

  public InputStream getInputStream(String entry) throws IOException {
    return ((ZipFile)object).getInputStream( ((ZipFile)object).getEntry(entry) );
  }

  public BufferedReader getBufferedReader(String entry, String encoding) throws IOException {
    InputStream is = getInputStream(entry);
    return StringUtils.isBlank(encoding)
           ? new BufferedReader(new InputStreamReader((InputStream)is))
           : new BufferedReader(new InputStreamReader((InputStream)is,encoding));
  }

} // end of class ZipArchive.
