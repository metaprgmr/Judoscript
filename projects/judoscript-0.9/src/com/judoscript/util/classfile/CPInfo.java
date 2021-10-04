/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 11-9-2002  JH   Inception.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/

package com.judoscript.util.classfile;

import java.io.*;
import com.judoscript.util.DataInputStreamEx;
import com.judoscript.util.SimpleLib;

public abstract class CPInfo implements ClassFileConsts
{
  int type;

  public int getType() { return type; }

  public abstract void printHtml(PrintWriter out) throws IOException;
  public abstract void write(DataOutputStream out) throws IOException;

/////////////////////////////////////////////
// Concrete classes
//

  // CONSTANT_Class
  // CONSTANT_String
  public static class U2 extends CPInfo
  { int index;
    public U2(int t, int i) { type=t; index=i; }
    public void printHtml(PrintWriter out) throws IOException {
      out.print(ClassFileUtil.getHtmlRefCP(index));
    }
    public void write(DataOutputStream out) throws IOException {
      out.write(type);
      out.writeShort(index);
    }
  }

  // CONSTANT_Integer
  // CONSTANT_Float
  public static class U4 extends CPInfo
  { int value;
    public U4(int t, int i) { type=t; value=i; }
    public void printHtml(PrintWriter out) throws IOException {
      if (type==CONSTANT_Integer) out.print(value);
      else out.print(Float.intBitsToFloat(value));
    }
    public void write(DataOutputStream out) throws IOException {
      out.write(type);
      out.writeInt(value);
    }
  }

  // CONSTANT_NameAndType
  public static class NameAndType extends CPInfo
  { int name_index, type_index;
    public NameAndType(int i1, int i2) { type=CONSTANT_NameAndType; name_index=i1; type_index=i2; }
    public void printHtml(PrintWriter out) throws IOException {
      out.print("name: " + ClassFileUtil.getHtmlRefCP(name_index));
      out.print(", ");
      out.print("type: " + ClassFileUtil.getHtmlRefCP(type_index));
    }
    public void write(DataOutputStream out) throws IOException {
      out.write(type);
      out.writeShort(name_index);
      out.writeShort(type_index);
    }
  }

  // CONSTANT_InterfaceMethodref
  // CONSTANT_Methodref
  // CONSTANT_Fieldref
  public static class Ref extends CPInfo
  { int class_index, member_index;
    public Ref(int t, int i1, int i2) { type=t; class_index=i1; member_index=i2; }
    public void printHtml(PrintWriter out) throws IOException {
      out.print("class: " + ClassFileUtil.getHtmlRefCP(class_index));
      out.print(", ");
      out.print("member: " + ClassFileUtil.getHtmlRefCP(member_index));
    }
    public void write(DataOutputStream out) throws IOException {
      out.write(type);
      out.writeShort(class_index);
      out.writeShort(member_index);
    }
  }

  // CONSTANT_Long
  // CONSTANT_Double
  public static class U8 extends CPInfo
  { long value;
    public U8(int t, int i1, int i2) {
      type=t;
      value = (((long)i1) << 32) | (i2 & 0x0FFFFFFFF);
    }
    public U8(int t, long l) { type=t; value = l; }
    public void printHtml(PrintWriter out) throws IOException {
      if (type==CONSTANT_Long) out.print(value);
      else out.print(Double.longBitsToDouble(value));
    }
    public void write(DataOutputStream out) throws IOException {
      out.write(type);
      out.writeLong(value);
    }
  }

  public static class Utf8 extends CPInfo
  { String text;
    public Utf8(String s) { type=CONSTANT_Utf8; text=s; }
    public String toString() { return text; }
    public void printHtml(PrintWriter out) throws IOException {
      out.print("<b><code>"+SimpleLib.replace(false,text,"<","&lt;")+"</code></b>");
    }
    public void write(DataOutputStream out) throws IOException {
      out.write(type);
      try {
        byte[] data = text.getBytes("UTF-8");
        out.writeShort(data.length);
        out.write(data);
      } catch(UnsupportedEncodingException e) {} // should not happen
    }
  }


  public static CPInfo read(DataInputStreamEx dis) throws IOException {
    int type = dis.read();
    switch(type) {
    case CONSTANT_Class:
    case CONSTANT_String:      return new U2(type, dis.readU2());
    case CONSTANT_InterfaceMethodref:
    case CONSTANT_Methodref:
    case CONSTANT_Fieldref:    return new Ref(type, dis.readU2(), dis.readU2());
    case CONSTANT_NameAndType: return new NameAndType(dis.readU2(), dis.readU2());
    case CONSTANT_Integer:
    case CONSTANT_Float:       return new U4(type, dis.readU4());
    case CONSTANT_Long:
    case CONSTANT_Double:      return new U8(type, dis.readU4(), dis.readU4());

    case CONSTANT_Utf8:
      int len = dis.readU2();
      byte[] buf = new byte[len];
      dis.read(buf,0,len);
      return new Utf8(new String(buf,"UTF-8"));
    default:
      throw new IOException("Invalid constant pool entry type: " + type);
    }
  }

} // end of class CPInfo.
