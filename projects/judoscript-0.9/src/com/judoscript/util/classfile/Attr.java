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


public class Attr
{
  String name;
  int name_index; // u2
  int attr_len;   // u4

  protected Attr() {}

  protected Attr(String name, int name_i, int len) {
    this.name = name;
    name_index = name_i;
    attr_len = len;
  }

  public String getName(ClassFileReader cfr) { return name; }

  public void write(DataOutputStream out) throws IOException {
    out.writeShort(name_index);
    out.writeInt(attr_len);
  }

  public static Attr read(ClassFileReader cfr, DataInputStreamEx dis)
                     throws IOException, BadClassFormatException
  {
    int name_i = dis.readU2();
    String name = cfr.getCPString(name_i);
    if (name.equals("ConstantValue") ||    // 4.7.2
        name.equals("SourceFile"))         // 4.7.7
      { dis.readU4(); return new U2Value(name, name_i, dis.readU2()); }
    if (name.equals("Code"))               // 4.7.3
      return Code.read(name_i, cfr, dis);
    if (name.equals("Exceptions"))         // 4.7.4
      return Exceptions.read(name_i, cfr, dis);
    if (name.equals("InnerClasses"))       // 4.7.5
      return AttrInnerClasses.read(name_i, cfr, dis);
    if (name.equals("Synthetic") ||        // 4.7.6
        name.equals("Deprecated"))         // 4.7.10
      return new Attr(name, name_i, 0);
//  if (name.equals("LineNumberTable"))    // 4.7.8
//    return AttrLineNumberTable.read(name_i, cfr, dis);
//  if (name.equals("LocalVariableTable")) // 4.7.9
//    return AttrLocalVariableTable.read(name_i, cfr, dis);
    return Custom.read(name, name_i, dis); 
  }

  public void printHtml(PrintWriter out) throws IOException {
    out.print("<b>" + name + "</b> (length=" + attr_len + ")");
  }

  public static class U2Value extends Attr
  {
    int value_index;
    // for reader
    public U2Value(String name, int name_i, int val_i) {
      super(name,name_i,2);
      value_index = val_i;
    }
    // for writer
    public U2Value(ClassFileWriter cfw, String name, String val) {
      this.name = name;
      name_index = cfw.cpUtf8(name);
      attr_len = 2;
      value_index = cfw.cpUtf8(val);
    }
    public void write(DataOutputStream out) throws IOException {
      super.write(out);
      out.writeShort(value_index);
    }
  }

  public static class Custom extends Attr
  {
    byte[] data;
    Custom(String name, int name_i, int len) { super(name,name_i,len); }
    static Custom read(String name, int name_i, DataInputStreamEx dis) throws java.io.IOException {
      Custom ret = new Custom(name,name_i,dis.readU4());
      ret.data = new byte[ret.attr_len];
      dis.read(ret.data,0,ret.data.length);
      return ret;
    }
    public void write(DataOutputStream out) throws IOException {
      super.write(out);
      out.write(data);
    }
  }


  public static class Code extends Attr
  {
    int max_stack;   // u2
    int max_locals;  // u2
    byte[] code;
    int[]  addrs;    // internal
    int[]  exception_table; // u2[*4]: *  : start_pc
                            //         *+1: end_pc;
                            //         *+2: handler_pc
                            //         *+3: catch_type
    Attr[] attributes;

    // for reader
    public Code(int name_i, int len) { super("Code",name_i,len); }

    static Code read(int name_i, ClassFileReader cfr, DataInputStreamEx dis)
                    throws IOException, BadClassFormatException
    {
      int i;
      Code ret = new Code(name_i,dis.readU4());
      ret.max_stack  = dis.readU2();
      ret.max_locals = dis.readU2();

      // read byte code
      int cnt = dis.readU4(); // code length
      ret.code = new byte[cnt];
      dis.read(ret.code);
      ret.addrs = ClassFileUtil.checkCode(ret.code);

      // read exception table
      cnt = dis.readU2();
      if (cnt > 0) {
        cnt *= 4;
        ret.exception_table = new int[cnt];
        for (i=0; i<cnt; ) {
          ret.exception_table[i++] = dis.readU2();
          ret.exception_table[i++] = dis.readU2();
          ret.exception_table[i++] = dis.readU2();
          ret.exception_table[i++] = dis.readU2();
        }
      }

      // read attributes
      cnt = dis.readU2();
      if (cnt > 0) {
        ret.attributes = new Attr[cnt];
        for (i=0; i<cnt; ret.attributes[i++]=Attr.read(cfr,dis));
      }
      return ret;
    }

    public void printHtml(PrintWriter out) throws IOException {
      out.println("<b>Code</b> (length=" + attr_len + ")<br>");
      out.println("max stack: " + max_stack + ", max locals: " + max_locals);
      out.println("<table border=0 cellpadding=0 cellspacing=0>");
      try {
        for (int i=0; i<addrs.length; ++i) {
          out.print("<tr><td valign=top align=right>" + addrs[i] +
                    "</td><td>&nbsp;&nbsp;</td><td><font face='courier new'>");
          out.println(ClassFileUtil.getInst(code,addrs[i]));
          out.print("</font></td></tr>");
        }
      } catch(BadClassFormatException bcfe) {}
      out.println("</table>");
      if (exception_table != null) {
        out.println("<h4>Exceptions:</h4>");
        out.println("<table border=0><tr><th>start</th><th>end</th><th>handler</th><th>exception</th></tr>");
        for (int i=0; i<exception_table.length; ) {
          out.print("<tr><td align=right>" + exception_table[i++] + "</td><td align=right>");
          out.print(             exception_table[i++] + "</td><td align=right>");
          out.print(             exception_table[i++] + "</td><td align=right>");
          out.println(ClassFileUtil.getHtmlRefCP(exception_table[i++]) + "</td></tr>");
        }
        out.println("</table>");
      }
      if (attributes != null) {
        out.println("<h4>Attributes:</h4>");
        for (int i=0; i<attributes.length; attributes[i++].printHtml(out));
      }
    }

    public void write(DataOutputStream out) throws IOException {
      super.write(out);
      out.writeShort(max_stack);
      out.writeShort(max_locals);
      out.writeInt(code.length);
      out.write(code);
      if (exception_table == null) out.writeShort(0);
      else {
        out.writeShort(exception_table.length);
        for (int i=0; i<exception_table.length; out.writeShort(exception_table[i++]));
      }
      if (attributes == null) out.writeShort(0);
      else {
        out.writeShort(attributes.length);
        for (int i=0; i<attributes.length; attributes[i++].write(out));
      }
    }

  } // end of inner class Code.


  public static class Exceptions extends Attr
  {
    int[] exception_index_table; // u2[]

    // for reader
    public Exceptions(int name_i, int len) { super("Exceptions",name_i,len); }
    // for writer
    public Exceptions(ClassFileWriter cfw, String[] exceptions) {
      name = "Exceptions";
      name_index = cfw.cpUtf8(name);
      attr_len = 2 + exceptions.length * 2;
      exception_index_table = new int[exceptions.length];
      for (int i=0; i<exceptions.length; ++i)
        exception_index_table[i] = cfw.cpClass(exceptions[i]);
    }

    static Exceptions read(int name_i, ClassFileReader cfr, DataInputStreamEx dis) throws IOException {
      Exceptions ret = new Exceptions(name_i,dis.readU4());
      int cnt = dis.readU2();
      ret.exception_index_table = new int[cnt];
      for (int i=0; i<cnt; ret.exception_index_table[i++]=dis.readU2());
      return ret;
    }

    public void printHtml(PrintWriter out) throws IOException {
      out.print("<b>Exceptions</b> (length=" + exception_index_table.length + ")<br>");
      for (int i=0; i<exception_index_table.length; ++i)
        out.print(ClassFileUtil.getHtmlRefCP(exception_index_table[i]));
    }

    public void write(DataOutputStream out) throws IOException {
      super.write(out);
      out.writeShort(exception_index_table.length);
      for (int i=0; i<exception_index_table.length; out.writeShort(exception_index_table[i++]));
    }

  } // end of inner class Exceptions.


  public static class AttrInnerClasses extends Attr
  {
    int[] inner_classes; // u2[*4]: *: inner_class_info_index,
                         //         *+1: outer_class_info_index,
                         //         *+2: inner_name_index,
                         //         *+3: inner_class_access_flags

    // for reader.
    public AttrInnerClasses(int name_i, int len) { super("InnerClasses",name_i,len); }

    static AttrInnerClasses read(int name_i, ClassFileReader cfr, DataInputStreamEx dis) throws IOException {
      AttrInnerClasses ret = new AttrInnerClasses(name_i,dis.readU4());
      int cnt = dis.readU2();
      ret.inner_classes = new int[cnt*4];
      int i=0;
      while(i<cnt*4) {
        ret.inner_classes[i++] = dis.readU2();
        ret.inner_classes[i++] = dis.readU2();
        ret.inner_classes[i++] = dis.readU2();
        ret.inner_classes[i  ] = dis.readU2();
      }
      return ret;
    }

    public void printHtml(PrintWriter out) throws IOException {
      out.print("<b>InnerClasses</b> (length=" + attr_len + ")");
    }

    public void write(DataOutputStream out) throws IOException {
      // TODO -- need to implement?
    }

  } // end of inner class AttrInnerClasses.


  public static class AttrLineNumberTable extends Attr
  {
    // for reader.
    public AttrLineNumberTable(int name_i, int len) { super("LineNumberTable",name_i,len); }

    static AttrLineNumberTable read(int name_i, ClassFileReader cfr, DataInputStreamEx dis) throws IOException
    {
      AttrLineNumberTable ret = new AttrLineNumberTable(name_i,dis.readU4());
      // TODO
      return ret;
    }

    public void printHtml(PrintWriter out) throws IOException {
      out.print("<b>AttrLineNumberTable</b> (length=" + attr_len + ")");
    }

    public void write(DataOutputStream out) throws IOException {
      // TODO -- need to implement?
    }

  } // end of inner class AttrLineNumberTable.


} // end of class Attr.
