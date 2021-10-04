/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 03-17-2002  JH   Initial open source release.
 * 08-10-2002  JH   Fixed a number of gzipped output streams in create().
 * 08-27-2002  JH   Fixed a text file appending not working.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript.bio;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.apache.commons.lang.StringUtils;
import com.judoscript.*;
import com.judoscript.util.*;


public final class IODevice extends JavaObject
{
  public static Variable create(String fname, int type, boolean append,
                                String randomMode, String encoding)
    throws ExceptionRuntime
  {
    long size = -1;
    long lastMod = -1;
    try {
      int idx = fname.indexOf(":/");
      if (idx > 1) {
        String prot = fname.substring(0,idx).toLowerCase();
        if (prot.equals("file")) {
          fname = fname.substring(idx+2);
        } else { // is a network url; handle it here.
          URLConnection urlc = new URL(fname).openConnection();
          InputStream is = urlc.getInputStream();
          size = urlc.getContentLength();
          lastMod = urlc.getDate();
          switch(type) {
          case IO_INPUTSTREAM:
            return new IODevice(new MyInputStream(is),size,lastMod);
          case IO_TEXTINPUTFILE:
            return new IODevice(new BufferedReader(new InputStreamReader(is)),size,lastMod);
          default:
            return ValueSpecial.UNDEFINED;
          }
        }
      }

      File f = new File(fname);
      size = f.length();
      lastMod = f.lastModified();

      switch(type) {
      case IO_INPUTSTREAM:
        return new IODevice(new MyInputStream(new FileInputStream(f)),size,lastMod);
      case IO_OUTPUTSTREAM:
        return new IODevice(new MyOutputStream(new FileOutputStream(fname,append)),size,lastMod);
      case IO_TEXTINPUTFILE:
        Reader isr = StringUtils.isBlank(encoding)
                     ? (Reader)new FileReader(f)
                     : new InputStreamReader(new FileInputStream(f), encoding);
        return new IODevice(new BufferedReader(isr),size,lastMod);
      case IO_TEXTOUTPUTFILE:
        Writer osw = StringUtils.isBlank(encoding)
                     ? new OutputStreamWriter(new FileOutputStream(fname,append))
                     : new OutputStreamWriter(new FileOutputStream(fname,append), encoding);
        return new IODevice(new PrintWriter(osw),size,lastMod);
      case IO_RANDOMACCESS:
        return new IODevice(new RandomAccessFile(f, randomMode),size,lastMod);
      case IO_GZIPPED_INPUTFILE:
      case IO_GZIPPED_TEXTINPUTFILE: // enc
        GZIPInputStream zis = new GZIPInputStream(new FileInputStream(f));
        if (type==IO_GZIPPED_INPUTFILE)
          return new IODevice(new MyInputStream(zis),size,lastMod);
        isr = StringUtils.isBlank(encoding) ? new InputStreamReader(zis)
                                    : new InputStreamReader(zis, encoding);
        return new IODevice(new BufferedReader(isr),size,lastMod);
      case IO_GZIPPED_OUTPUTFILE:
      case IO_GZIPPED_TEXTOUTPUTFILE: // enc
        GZIPOutputStream zos = new GZIPOutputStream(new FileOutputStream(f));
        if (type==IO_GZIPPED_OUTPUTFILE)
          return new IODevice(new MyOutputStream(zos),size,lastMod);
        osw = StringUtils.isBlank(encoding) ? new OutputStreamWriter(zos)
                                    : new OutputStreamWriter(zos, encoding);
        return new IODevice(new PrintWriter(osw),size,lastMod);
      default:
        return null; // never happen.
      }
    } catch (IOException ioe) {
      ExceptionRuntime.rte(RTERR_IO_FILE_OPEN_FAILURE,
                "File '" + fname + "' does not exist or can't be opened at the moment.",ioe);
    }
    return null;
  }

  long size;
  long lastMod;

  public IODevice(Object o, long siz, long lastMod) {
    super(o);
    this.size = siz;
    this.lastMod = lastMod;
  }

  public IODevice(InputStream is) {
    super(new MyInputStream(is));
    size = -1;
    lastMod = System.currentTimeMillis();
  }

  public IODevice(OutputStream os) {
    super(new MyOutputStream(os));
    size = -1;
    lastMod = System.currentTimeMillis();
  }

  public IODevice(BufferedReader br) {
    super(br);
    size = -1;
    lastMod = System.currentTimeMillis();
  }

  /////////////////////////////////////////////////////////
  // Methods
  //

  public Variable invoke(String fxn, Expr[] args, int[] javaTypes) throws Throwable
  {
    String s;
    int len = (args==null) ? 0 : args.length;
    switch(getMethodOrdinal(fxn)) {

    case BIM_SIZE:           return ConstInt.getInt(size);
    case BIM_LASTMODIFIED:   return new _Date(lastMod);
    case BIM_SETBIGENDIAN:
      if (object instanceof RandomAccessFileLE) {
        setObject( ((RandomAccessFileLE)object).getOriginal() );
        return ValueSpecial.UNDEFINED;
      } else if (object instanceof MyInputStream) {
        ((MyInputStream)object).setBigEndian(true);
        return ValueSpecial.UNDEFINED;
      } else if (object instanceof MyOutputStream) {
        ((MyOutputStream)object).setBigEndian(true);
        return ValueSpecial.UNDEFINED;
      }
      break;
    case BIM_SETLITTLEENDIAN:
      if (object instanceof RandomAccessFile) {
        setObject( new RandomAccessFileLE((RandomAccessFile)object) );
        return ValueSpecial.UNDEFINED;
      } else if (object instanceof MyInputStream) {
        ((MyInputStream)object).setBigEndian(false);
        return ValueSpecial.UNDEFINED;
      } else if (object instanceof MyOutputStream) {
        ((MyOutputStream)object).setBigEndian(false);
        return ValueSpecial.UNDEFINED;
      }
      break;
    case BIM_ISBIGENDIAN:
      if (object instanceof MyInputStream)
        return ConstInt.getBool(((MyInputStream)object).isBigEndian());
      else if (object instanceof MyOutputStream)
        return ConstInt.getBool(((MyOutputStream)object).isBigEndian());
      else
        return ConstInt.getBool(object instanceof RandomAccessFile);
    case BIM_ISLITTLEENDIAN:
      if (object instanceof MyInputStream)
        return ConstInt.getBool(!((MyInputStream)object).isBigEndian());
      else if (object instanceof MyOutputStream)
        return ConstInt.getBool(!((MyOutputStream)object).isBigEndian());
      else
        return ConstInt.getBool(object instanceof RandomAccessFileLE);
    case BIM_READBYTESASSTRING:
      int cnt = (len == 0) ? 1 : (int)args[0].getLongValue();
      s = (len>1) ? null : args[1].getStringValue().trim();
      byte[] buf = new byte[cnt];
      if (object instanceof MyInputStream) {
        cnt = ((MyInputStream)object).read(buf);
      } else if (object instanceof RandomAccessFile) {
        cnt = ((RandomAccessFile)object).read(buf);
      } else if (object instanceof RandomAccessFileLE) {
        cnt = ((RandomAccessFileLE)object).read(buf);
      }
      s = (s==null) ? new String(buf,0,cnt) : new String(buf,0,cnt,s);
      return JudoUtil.toVariable(s);
    case BIM_TOTEXTINPUT:
      s = (len==0) ? null : args[0].getStringValue().trim();
      if (!(object instanceof BufferedReader)) {
        InputStreamReader isr = StringUtils.isBlank(s)
                                ? new InputStreamReader((InputStream)object)
                                : new InputStreamReader((InputStream)object,s);
        reInit(new BufferedReader(isr));
      }
      return this;
    case BIM_TOTEXTOUTPUT:
      s = (len==0) ? null : args[0].getStringValue().trim();
      if (!(object instanceof PrintWriter)) {
        OutputStreamWriter osw = StringUtils.isBlank(s)
                                 ? new OutputStreamWriter((OutputStream)object)
                                 : new OutputStreamWriter((OutputStream)object,s);
        reInit(new PrintWriter(osw));
      }
      return this;
    }
    return super.invoke(fxn,args,javaTypes);
  }

  // a helper
  // returns an InputStream or Reader.
  public static Object getInputStream(Variable doc, String name, String encoding) throws Throwable {
    return getInputStream(doc, name, encoding, true);
  }

  public static Object getInputStream(Variable doc, String name, String encoding, boolean readerOk)
    throws Throwable
  {
    if (doc instanceof JavaObject) {
      Object o = doc.getObjectValue();
      if (o instanceof InputStream)
        return (InputStream)o;
      if (o instanceof Reader)
        return (Reader)o;
    } else {
      String s = doc.getStringValue();
      int idx = s.indexOf(":/");
      if (idx > 1) {
        String prot = s.substring(0,idx).toLowerCase();
        if ("file".equals(prot)) {  // it is a file url
          s = s.substring(idx+2);
        } else {                    // it is a network url
          InputStream is = new URL(s).openStream();
          if (readerOk)
            return new InputStreamReader(is);
          return is;
        }
      }
      File f = RT.getFile(s);
      InputStream is = new FileInputStream(f);
      if (readerOk) {
        if (StringUtils.isBlank(encoding))
          return new InputStreamReader(is);
        else
          return new InputStreamReader(is, encoding);
      }
      return is;
    }

    String msg = (name==null) ? "E" : (name + " e");
    msg += "xpects an InputStream";
    if (readerOk) msg += ", Reader";
    msg += " or a String as its source; what we got is: " + doc.getObjectValue().getClass().getName();
    ExceptionRuntime.rte(RTERR_ILLEGAL_ARGUMENTS, msg);
    return null;
  }

  public static Object getOutputStream(Variable out, String name, String encoding, boolean writerOk)
    throws Throwable
  {
    if (out instanceof JavaObject) {
      Object o = out.getObjectValue();
      if ((o instanceof OutputStream) || (o instanceof Writer))
        return o;
    } else {
      File f = RT.getFile(out.getStringValue());
      OutputStream os = new FileOutputStream(f);
      if (writerOk) {
        if (StringUtils.isBlank(encoding))
          return new PrintWriter(new OutputStreamWriter(os));
        else
          return new PrintWriter(new OutputStreamWriter(os, encoding));
      }
      return os;
    }

    String msg = (name==null) ? "E" : (name + " e");
    msg += "xpects an OutputStream";
    if (writerOk)
      msg += ", Writer";
    msg += " or a String as its source.";
    ExceptionRuntime.rte(RTERR_ILLEGAL_ARGUMENTS, msg);
    return null;
  }

} // end of class IODevice.
