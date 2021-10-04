/*
 *
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

public class DataInputStreamEx extends DataInputStream
{
  public DataInputStreamEx(InputStream is) { super(is); }

  public short readS2() throws IOException { return (short)((read()<<8) | read() & 0x0FF); }
  
  public int readU2() throws IOException { return (read()<<8) | read() & 0x0FF; }

  // taking the risk of MSB being 1; actually same as readS4().
  public int readU4() throws IOException {
    return (read()<<24) | (read()<<16)&0x0FF0000 | (read()<<8)&0x0FF00 | read()&0x0FF;
  }

  // taking the risk of MSB being 1; actually same as readS8().
  public long readU8() throws IOException { return ((long)readU4() << 32) | readU4(); }

} // end of DataInputStreamEx.
