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

public final class MyInputStream extends InputStream
{
  private byte[] numBuf = new byte[8];

  InputStream is;
  boolean bigEndian;


  public MyInputStream(InputStream is) {
    this(is,true);
  }

  public MyInputStream(InputStream is, boolean bigEndian) {
    this.is = is;
    this.bigEndian = bigEndian;
  }

  public void setBigEndian(boolean set) { bigEndian = set; }
  public boolean isBigEndian() { return bigEndian; }

  public long skip(int cnt) throws IOException { return is.skip(cnt); }
  public void reset() throws IOException { is.reset(); }
  public void mark(int limit) { is.mark(limit); }
  public boolean markSupported() { return is.markSupported(); }
  public int  available() throws IOException { return is.available(); }

  public int skipBytes(int cnt) throws IOException { return (int)is.skip(cnt); }
  public void close() throws IOException { is.close(); }
  public int read() throws IOException { return is.read(); }
  public int read(byte[] buf) throws IOException { return is.read(buf); }
  public int read(byte[] buf, int off, int len) throws IOException { return is.read(buf,off,len); }
  public boolean readBoolean() throws IOException { return (read() != 0); }
  public int readByte() throws IOException { return read(); }
  public char readAscii() throws IOException { return (char)readUnsignedByte(); }
  public char readUnicode() throws IOException { return (char)readUnsignedShort(); }
  public int readUnsignedByte() throws IOException { return read() & 0x0FF; }
  public int readUnsignedShort() throws IOException { return ((int)readShort()) & 0x0FFFF; }
  public float readFloat() throws IOException { return Float.intBitsToFloat(readInt()); }
  public double readDouble() throws IOException { return Double.longBitsToDouble(readLong()); }

  public short readShort() throws IOException {
    is.read(numBuf, 0, 2);
    if (bigEndian)
      return (short) ( (((int)numBuf[0]) << 8) | (int)numBuf[1] );
    else
      return (short) ( (((int)numBuf[1]) << 8) | (int)numBuf[0] );
  }

  public int readInt() throws IOException {
    is.read(numBuf, 0, 4);
    if (bigEndian)
      return (numBuf[0] << 24) | (numBuf[1] << 16) | (numBuf[2] << 8) | numBuf[3];
    else
      return (numBuf[3] << 24) | (numBuf[2] << 16) | (numBuf[1] << 8) | numBuf[0];
  }

  public long readLong() throws IOException {
    is.read(numBuf, 0, 8);
    if (bigEndian)
      return (numBuf[0] << 56) | (numBuf[1] << 48) | (numBuf[2] << 40) | (numBuf[3] << 32) |
             (numBuf[4] << 24) | (numBuf[5] << 16) | (numBuf[6] <<  8) |  numBuf[7];
    else
      return (numBuf[7] << 56) | (numBuf[6] << 48) | (numBuf[5] << 40) | (numBuf[4] << 32) |
             (numBuf[3] << 24) | (numBuf[2] << 16) | (numBuf[1] <<  8) |  numBuf[0];
  }

} // end of class myInputStream.
