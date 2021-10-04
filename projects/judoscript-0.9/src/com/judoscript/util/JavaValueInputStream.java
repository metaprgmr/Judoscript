/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 05-31-2002  JH   Incepted.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript.util;

import java.io.*;
import java.util.Date;


public class JavaValueInputStream extends FilterInputStream
{
  public JavaValueInputStream(InputStream is) { super(is); }

  /**
   *@exception EOFException We force this exception when -1 is returned
   *                        because this is used to get specific values.
   */
  public int read() throws IOException {
    int ret = super.read();
    if (ret < 0) throw new EOFException();
    return ret;
  }
  
  public byte readByte(byte b) throws IOException { return (byte)read(); }

  public short readShort() throws IOException { return (short) ( (read() << 8) | read() ); }

  public char readChar() throws IOException { return (char) ( (read() << 8) | read() ); }

  public int readInt() throws IOException {
    return (read() << 24) | (read() << 16) | (read() << 8) | read();
  }

  public long readLong() throws IOException {
    return (((long)read()) << 56) |
           (((long)read()) << 48) |
           (((long)read()) << 40) |
           (((long)read()) << 32) |
           (read() << 24) | (read() << 16) | (read() <<  8) | read();
  }

  public float readFloat() throws IOException { return Float.intBitsToFloat(readInt()); }

  public double readDouble() throws IOException { return Double.longBitsToDouble(readLong()); }

  public String readString() throws IOException {
    int len = readInt();
    char[] ca = new char[len];
    for (int i=0; i<len; i++)
      ca[i] = readChar();
    return new String(ca);
  }

  public Date readDate() throws IOException { return new Date(readLong()); }

  public byte[] readBytes() throws IOException {
    int len = readInt();
    byte[] ba = new byte[len];
    for (int i=0; i<len; i++)
      ba[i] = (byte)read();
    return ba;
  }

  public short[] readShorts() throws IOException {
    int len = readInt();
    short[] sa = new short[len];
    for (int i=0; i<len; i++)
      sa[i] = readShort();
    return sa;
  }

  public char[] readChars() throws IOException {
    int len = readInt();
    char[] ca = new char[len];
    for (int i=0; i<len; i++)
      ca[i] = readChar();
    return ca;
  }

  public int[] readInts() throws IOException {
    int len = readInt();
    int[] ia = new int[len];
    for (int i=0; i<len; i++)
      ia[i] = readInt();
    return ia;
  }

  public long[] readLongs() throws IOException {
    int len = readInt();
    long[] ia = new long[len];
    for (int i=0; i<len; i++)
      ia[i] = readLong();
    return ia;
  }

  public float[] readFloats() throws IOException {
    int len = readInt();
    float[] fa = new float[len];
    for (int i=0; i<len; i++)
      fa[i] = readFloat();
    return fa;
  }

  public double[] readDoubles() throws IOException {
    int len = readInt();
    double[] da = new double[len];
    for (int i=0; i<len; i++)
      da[i] = readDouble();
    return da;
  }

  public String[] readStrings() throws IOException {
    int len = readInt();
    String[] sa = new String[len];
    for (int i=0; i<len; i++)
      sa[i] = readString();
    return sa;
  }

  public Date[] readDates() throws IOException {
    int len = readInt();
    Date[] da = new Date[len];
    for (int i=0; i<len; i++)
      da[i] = readDate();
    return da;
  }

  public static class EOFException extends IOException {
    public EOFException() {}
    public EOFException(String msg) { super(msg); }
  }

  public static JavaValueInputStream createLittleEndian(InputStream is) {
    return new LittleEndian(is);
  }

  private static class LittleEndian extends JavaValueInputStream
  {
    LittleEndian(InputStream is) { super(is); }

    public short readShort() throws IOException {
      return (short) ( (read() & 0xFF) | ((read() & 0xFF) << 8) );
    }

    public char readChar() throws IOException {
      return (char) ( (read() & 0xFF) | ((read() & 0xFF) << 8) );
    }

    public int readInt() throws IOException {
      return (read() & 0xFF) | ((read() & 0xFF) << 8) |
             ((read() & 0xFF) << 16) | ((read() & 0xFF) << 24);
    }

    public long readLong() throws IOException {
      return (read() & 0xFF) |
             ((read() & 0xFF) << 8) |
             ((read() & 0xFF) << 16) |
             ((read() & 0xFF) << 24) |
             (((long)(read() & 0xFF)) << 32) |
             (((long)(read() & 0xFF)) << 40) |
             (((long)(read() & 0xFF)) << 48) |
             (((long)(read() & 0xFF)) << 56) ;
    }

  } // end of static inner class LittleEndian.

} // end of class JavaValueInputStream.
