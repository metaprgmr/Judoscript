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


public class JavaValueOutputStream extends FilterOutputStream
{
  public JavaValueOutputStream(OutputStream os) { super(os); }

  public void writeByte(byte b) throws IOException { write((int)b); }

  public void writeShort(short i) throws IOException {
    write( (i>>8) & 0xFF );
    write( i & 0xFF );
  }

  public void writeChar(char c) throws IOException {
    write( (c>>8) & 0xFF );
    write( c & 0xFF );
  }

  public void writeInt(int i) throws IOException {
    write( (i>>>24) & 0xFF );
    write( (i>>>16) & 0xFF );
    write( (i>>>8)  & 0xFF );
    write( i & 0xFF );
  }

  public void writeLong(long i) throws IOException {
    write( (int) ((i>>>56) & 0xFF) );
    write( (int) ((i>>>48) & 0xFF) );
    write( (int) ((i>>>40) & 0xFF) );
    write( (int) ((i>>>32) & 0xFF) );
    write( (int) ((i>>>24) & 0xFF) );
    write( (int) ((i>>>16) & 0xFF) );
    write( (int) ((i>>>8)  & 0xFF) );
    write( (int) (i & 0xFF) );
  }

  public void writeFloat(float f) throws IOException { writeInt(Float.floatToRawIntBits(f)); }

  public void writeDouble(double d) throws IOException { writeLong(Double.doubleToRawLongBits(d)); }

  public void writeString(String s) throws IOException {
    if (s == null) { writeInt(0); return; }
    writeInt(s.length());
    for (int i=0; i<s.length(); i++)
      writeChar(s.charAt(i));
  }

  public void writeDate(Date d) throws IOException { writeLong(d.getTime()); }

  public void writeBytes(byte[] ba) throws IOException {
    if (ba == null) { writeInt(0); return; }
    writeInt(ba.length);
    write(ba);
  }

  public void writeShorts(short[] ia) throws IOException {
    if (ia == null) { writeInt(0); return; }
    writeInt(ia.length);
    for (int i=0; i<ia.length; i++)
      writeShort(ia[i]);
  }

  public void writeChars(char[] ca) throws IOException {
    if (ca == null) { writeInt(0); return; }
    writeInt(ca.length);
    for (int i=0; i<ca.length; i++)
      writeChar(ca[i]);
  }

  public void writeInts(int[] ia) throws IOException {
    if (ia == null) { writeInt(0); return; }
    writeInt(ia.length);
    for (int i=0; i<ia.length; i++)
      writeInt(ia[i]);
  }

  public void writeInts(IntVector iv) throws IOException {
    if (iv == null) { writeInt(0); return; }
    writeInt(iv.size());
    for (int i=0; i<iv.size(); i++)
      writeInt(iv.getAt(i));
  }

  public void writeLongs(long[] ia) throws IOException {
    if (ia == null) { writeInt(0); return; }
    writeInt(ia.length);
    for (int i=0; i<ia.length; i++)
      writeLong(ia[i]);
  }

  public void writeFloats(float[] fa) throws IOException {
    if (fa == null) { writeInt(0); return; }
    writeInt(fa.length);
    for (int i=0; i<fa.length; i++)
      writeFloat(fa[i]);
  }

  public void writeDoubles(double[] da) throws IOException {
    if (da == null) { writeInt(0); return; }
    writeInt(da.length);
    for (int i=0; i<da.length; i++)
      writeDouble(da[i]);
  }

  public void writeStrings(String[] sa) throws IOException {
    if (sa == null) { writeInt(0); return; }
    writeInt(sa.length);
    for (int i=0; i<sa.length; i++)
      writeString(sa[i]);
  }

  public void writeDates(Date[] da) throws IOException {
    if (da == null) { writeInt(0); return; }
    writeInt(da.length);
    for (int i=0; i<da.length; i++)
      writeDate(da[i]);
  }

  public static JavaValueOutputStream createLittleEndian(OutputStream os) {
    return new LittleEndian(os);
  }

  private static class LittleEndian extends JavaValueOutputStream
  {
    LittleEndian(OutputStream os) { super(os); }

    public void writeShort(short i) throws IOException {
      write( i & 0xFF );
      write( (i>>8) & 0xFF );
    }

    public void writeChar(char c) throws IOException {
      write( c & 0xFF );
      write( (c>>8) & 0xFF );
    }

    public void writeInt(int i) throws IOException {
      write( i & 0xFF );
      write( (i >> 8)  & 0xFF );
      write( (i >> 16) & 0xFF );
      write( (i >> 24) & 0xFF );
    }

    public void writeLong(long i) throws IOException {
      write( ((int)i) & 0xFF );
      write( (((int)i) >> 8)  & 0xFF );
      write( (((int)i) >> 16) & 0xFF );
      write( (((int)i) >> 24) & 0xFF );
      write( (((int)i) >> 32) & 0xFF );
      write( (((int)i) >> 40) & 0xFF );
      write( (((int)i) >> 48) & 0xFF );
      write( (((int)i) >> 56) & 0xFF );
    }

  } // end of static inner class LittleEndian.

} // end of class JavaValueOutputStream.
