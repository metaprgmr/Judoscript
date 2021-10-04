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

public class ClassFileReader implements ClassFileConsts
{
  short minor_version;
  short major_version;

  CPInfo[] cp;      // constant_pool; cp[0] is not used by spec.

  int access_flags; // bit-wise ORs of Modifier access flags.

  int this_class;   // u2 index to cp
  int super_class;  // u2 index to cp

  int[]    interfaces; // u2[] indices to cp of CONSTANT_Class's.
  Member[] fields;
  Member[] methods;
  Attr[]   attributes;


  public ClassFileReader(String fname) throws IOException, BadClassFormatException {
    read(fname);
  }

  public String getCPString(int index) throws BadClassFormatException {
    try { return ((CPInfo.Utf8)cp[index]).toString(); }
    catch(Exception e) {
      throw new BadClassFormatException("Constant pool does not contain a string at "+index);
    }
  }

  private void read(String fname) throws IOException, BadClassFormatException {
    int i;
    DataInputStreamEx dis = new DataInputStreamEx(new FileInputStream(fname));

    // read magic and version numbers
    dis.readInt(); // magic
    minor_version = (short)dis.readU2();
    major_version = (short)dis.readU2();

    // read constant pool
    int cnt = dis.readU2(); // constant_pool_count
    cp = new CPInfo[cnt];
    for (i=1; i<cnt; i++) {
      cp[i] = CPInfo.read(dis);
      int type = cp[i].type;
      if ((type==CONSTANT_Long) || (type==CONSTANT_Double))
        ++i;
    }

    // read access flags, this and super classes
    access_flags = dis.readU2();
    this_class   = dis.readU2();
    super_class  = dis.readU2();

    // read interfaces
    cnt = dis.readU2();
    if (cnt <= 0) {
      interfaces = null;
    } else {
      interfaces = new int[cnt];
      for (i=0; i<cnt; interfaces[i++]=dis.readU2());
    }

    // read fields
    cnt = dis.readU2();
    if (cnt <= 0) {
      fields = null;
    } else {
      fields = new Member[cnt];
      for (i=0; i<cnt; fields[i++]=Member.read(this,dis));
    }

    // read methods
    cnt = dis.readU2();
    if (cnt <= 0) {
      methods = null;
    } else {
      methods = new Member[cnt];
      for (i=0; i<cnt; methods[i++]=Member.read(this,dis));
    }

    // read attributes
    cnt = dis.readU2();
    if (cnt <= 0) {
      attributes = null;
    } else {
      attributes = new Attr[cnt];
      for (i=0; i<cnt; attributes[i++]=Attr.read(this,dis));
    }
  }

  public void printHtml(String fileName) throws IOException, BadClassFormatException {
    printHtml( new PrintWriter( new FileWriter(fileName) ) );
  }

  public void printHtml(PrintWriter out) throws IOException, BadClassFormatException {
    int i;

    out.println("<html><body><table cellspacing=0 cellpadding=0 border=0><tr><td valign=top>");
    // LEFT PANE

    // Overview
    out.println("<h3>Overview</h3>");
    out.println("version: " + major_version + "." + minor_version + "<br>");
    out.println("access flags: " + ClassFileUtil.getAccessFlagNames(access_flags) + "<br>");
    out.println("this class: " + ClassFileUtil.getHtmlRefCP(this_class) + ", ");
    out.println("super class: " + ClassFileUtil.getHtmlRefCP(super_class) + "<br>");
    if (interfaces != null) {
      out.print  ("interfaces: ");
      for (i=0; i<interfaces.length; ++i)
        out.print(ClassFileUtil.getHtmlRefCP(interfaces[i]) + " ");
      out.println("<br>");
    }
    out.println("<a href=#fields>Fields</a>, ");
    out.println("<a href=#methods>Methods</a>, ");
    out.println("<a href=#attrs>Attributes</a>");

    // Fields
    if (fields != null) {
      out.println("<h3><a name=fields>Fields</a></h3>");
      for (i=0; i<fields.length; ++i) {
        out.println("<h4> Field #"+i+"</h4>");
        fields[i].printHtml(this,out);
      }
    }

    // Members
    if (methods != null) {
      out.println("<h3><a name=methods>Members</a></h3>");
      for (i=0; i<methods.length; ++i) {
        out.println("<h4> Method #"+i+"</h4>");
        methods[i].printHtml(this,out);
      }
    }

    // Attributes
    if (attributes != null) {
      out.println("<h3><a name=attrs>Attributes</a></h3>");
      ClassFileUtil.printAttrsHtml(out,attributes);
    }

    // RIGHT PANE
    out.println("</td><td>&nbsp;&nbsp;&nbsp;</td><td>");

    // Constant Pool
    out.println("<h3><a name=cp>Constant Pool</a></h3>");
    out.println("<table border=1>");
    for (i=1; i<cp.length; ++i) {
      if (cp[i] == null) continue;
      out.print("<tr><td valign=top><a name=cp_"+i+">"+i+"</a></td><td>");
      out.print(CONSTANT_Names[cp[i].type]);
      out.print("</td><td>");
      cp[i].printHtml(out);
      out.println("</td></tr>");
    }
    out.println("</table>");

    // Done.
    out.println("</td></tr></table></body></html>");
    out.flush();
  }

  public static void main(String[] args) {
    try {
      PrintWriter out = new PrintWriter(new FileWriter(args[0]+".html"));
      new ClassFileReader(args[0]).printHtml(out);
      out.close();
    } catch(Throwable t) {
      t.printStackTrace();
    }
  }

} // end of class ClassFileReader.


/*[judo]

// Test: takes a .class file and generates a .class.html file.

cfr = javanew com.judoscript.util.classfile.ClassFileReader(#args[0]);
cfr.printHtml( #args[0] @ '.html' );

catch: $_.printInternalStackTrace();
[judo]*/
