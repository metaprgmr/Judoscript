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


public final class RandomAccessFileLE implements DataOutput, java.io.DataInput
{
  private byte[] numBuf = new byte[8];
  RandomAccessFile raf;

  public RandomAccessFileLE(RandomAccessFile raf) {
    this.raf = raf;
  }

  public RandomAccessFile getOriginal() { return raf; }

  public FileDescriptor getFD() throws IOException { return raf.getFD(); }
  public long getFilePointer() throws IOException { return raf.getFilePointer(); }
  public int read() throws IOException { return raf.read(); }
  public int read(byte[] buf, int off, int len) throws IOException { return raf.read(buf, off, len); }
  public int read(byte[] buf) throws IOException { return raf.read(buf); }
  public void readFully(byte[] buf) throws IOException { raf.readFully(buf); }
  public void readFully(byte[] buf,int off,int len) throws IOException { raf.readFully(buf,off,len); }
  public int skipBytes(int cnt) throws IOException { return raf.skipBytes(cnt); }
  public void write(int byt) throws IOException { raf.write(byt); }
  public void write(byte[] buf) throws IOException { raf.write(buf); }
  public void write(byte[] buf, int off, int len) throws IOException { raf.write(buf, off, len); }
  public void seek(long pos) throws IOException { raf.seek(pos); }
  public long length() throws IOException { return raf.length(); }
  public void setLength(long len) throws IOException { raf.setLength(len); }
  public void close() throws IOException { raf.close(); }
  public boolean readBoolean() throws IOException { return raf.readBoolean(); }
  public byte readByte() throws IOException { return raf.readByte(); }
  public int readUnsignedByte() throws IOException { return raf.readUnsignedByte(); }
  public int readUnsignedShort() throws IOException { return ((int)readShort()) & 0x0FFFF; }
  public char readChar() throws IOException { return (char) readUnsignedShort(); }
  public float readFloat() throws IOException { return Float.intBitsToFloat(readInt()); }
  public double readDouble() throws IOException { return Double.longBitsToDouble(readLong()); }
  public String readLine() throws IOException { return raf.readLine(); }
  public String readUTF() throws IOException { return raf.readUTF(); }
  public void writeBoolean(boolean b) throws IOException {  raf.writeBoolean(b); }
  public void writeByte(int b) throws IOException { raf.writeByte(b); }
  public void writeChar(int c) throws IOException { writeShort(c); }
  public void writeFloat(float f) throws IOException { writeLong(Float.floatToIntBits(f)); }
  public void writeDouble(double d) throws IOException { writeLong(Double.doubleToLongBits(d)); }
  public void writeBytes(String str) throws IOException { raf.writeBytes(str); }
  public void writeChars(String str) throws IOException { raf.writeBytes(str); }
  public void writeUTF(String str) throws IOException { raf.writeBytes(str); }

  public short readShort() throws IOException {
    raf.read(numBuf, 0, 2);
    return (short) ( (((int)numBuf[1]) << 8) | (int)numBuf[0] );
  }

  public int readInt() throws IOException {
    raf.read(numBuf, 0, 4);
    return (numBuf[3] << 24) | (numBuf[2] << 16) | (numBuf[1] << 8) | numBuf[0];
  }

  public long readLong() throws IOException {
    raf.read(numBuf, 0, 8);
    return (numBuf[7] << 56) | (numBuf[6] << 48) | (numBuf[5] << 40) | (numBuf[4] << 32) |
           (numBuf[3] << 24) | (numBuf[2] << 16) | (numBuf[1] <<  8) |  numBuf[0];
  }

  public final void writeShort(int s) throws IOException {
    numBuf[1] = (byte) ((s >> 8) & 0x0FF);
    numBuf[0] = (byte) s;
    raf.write(numBuf,0,2);
  }

  public final void writeInt(int i) throws IOException {
    numBuf[3] = (byte) ((i >> 24) & 0x0FF);
    numBuf[2] = (byte) ((i >> 16) & 0x0FF);
    numBuf[1] = (byte) ((i >>  8) & 0x0FF);
    numBuf[0] = (byte) i;
    raf.write(numBuf,0,4);
  }

  public final void writeLong(long l) throws IOException {
    numBuf[7] = (byte) ((l >> 56) & 0x0FF);
    numBuf[6] = (byte) ((l >> 48) & 0x0FF);
    numBuf[5] = (byte) ((l >> 40) & 0x0FF);
    numBuf[4] = (byte) ((l >> 32) & 0x0FF);
    numBuf[3] = (byte) ((l >> 24) & 0x0FF);
    numBuf[2] = (byte) ((l >> 16) & 0x0FF);
    numBuf[1] = (byte) ((l >>  8) & 0x0FF);
    numBuf[0] = (byte) l;
    raf.write(numBuf,0,8);
  }

} // end of class RandomAccessFileLE.
