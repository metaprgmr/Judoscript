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

//
// NOT THREAD-SAFE !!!
//

public final class MyOutputStream extends OutputStream
{
  private byte[] numBuf = new byte[8];

  OutputStream os;
  boolean bigEndian;


  public MyOutputStream(OutputStream os) {
    this(os,true);
  }

  public MyOutputStream(OutputStream os, boolean bigEndian) {
    this.os = os;
    this.bigEndian = bigEndian;
  }

  public void setBigEndian(boolean set) { bigEndian = set; }
  public boolean isBigEndian() { return bigEndian; }

  public void flush() throws IOException { os.flush(); }
  public void close() throws IOException { os.close(); }

  public void write(int byt) throws IOException { os.write(byt); }
  public void write(byte[] buf) throws IOException { os.write(buf); }
  public void write(byte[] buf, int off, int len) throws IOException { os.write(buf,off,len); }
  public final void writeBoolean(boolean b) throws IOException { os.write(b ? 1 : 0); }
  public final void writeByte(int b) throws IOException { os.write(b); }
  public final void writeChar(int c) throws IOException { writeShort(c); }
  public final void writeFloat(float f) throws IOException { writeInt(Float.floatToIntBits(f)); }
  public final void writeDouble(double d) throws IOException { writeLong(Double.doubleToLongBits(d)); }

  public final void writeShort(int s) throws IOException {
    if (bigEndian) {
      numBuf[0] = (byte) ((s >> 8) & 0x0FF);
      numBuf[1] = (byte) s;
    } else {
      numBuf[1] = (byte) ((s >> 8) & 0x0FF);
      numBuf[0] = (byte) s;
    }
    os.write(numBuf,0,2);
  }

  public final void writeInt(int i) throws IOException {
    if (bigEndian) {
      numBuf[0] = (byte) ((i >> 24) & 0x0FF);
      numBuf[1] = (byte) ((i >> 16) & 0x0FF);
      numBuf[2] = (byte) ((i >>  8) & 0x0FF);
      numBuf[3] = (byte) i;
    } else {
      numBuf[3] = (byte) ((i >> 24) & 0x0FF);
      numBuf[2] = (byte) ((i >> 16) & 0x0FF);
      numBuf[1] = (byte) ((i >>  8) & 0x0FF);
      numBuf[0] = (byte) i;
    }
    os.write(numBuf,0,4);
  }

  public final void writeLong(long l) throws IOException {
    if (bigEndian) {
      numBuf[0] = (byte) ((l >> 56) & 0x0FF);
      numBuf[1] = (byte) ((l >> 48) & 0x0FF);
      numBuf[2] = (byte) ((l >> 40) & 0x0FF);
      numBuf[3] = (byte) ((l >> 32) & 0x0FF);
      numBuf[4] = (byte) ((l >> 24) & 0x0FF);
      numBuf[5] = (byte) ((l >> 16) & 0x0FF);
      numBuf[6] = (byte) ((l >>  8) & 0x0FF);
      numBuf[7] = (byte) l;
    } else {
      numBuf[7] = (byte) ((l >> 56) & 0x0FF);
      numBuf[6] = (byte) ((l >> 48) & 0x0FF);
      numBuf[5] = (byte) ((l >> 40) & 0x0FF);
      numBuf[4] = (byte) ((l >> 32) & 0x0FF);
      numBuf[3] = (byte) ((l >> 24) & 0x0FF);
      numBuf[2] = (byte) ((l >> 16) & 0x0FF);
      numBuf[1] = (byte) ((l >>  8) & 0x0FF);
      numBuf[0] = (byte) l;
    }
    os.write(numBuf,0,8);
  }

} // end of class MyOutputStream.
