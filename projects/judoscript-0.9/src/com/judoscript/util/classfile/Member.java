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
import org.apache.commons.lang.StringUtils;
import com.judoscript.util.SimpleLib;
import com.judoscript.util.DataInputStreamEx;


public class Member implements ClassFileConsts
{
  int access_flags; // u2
  int name_index;   // u2
  int desc_index;   // u2
  Attr[] attributes;

  int cp_index;
 
  Member() {}

  public Member(int name, int desc, int access, Attr[] attrs, int cp_index) {
    name_index = name;
    desc_index = desc;
    access_flags = access;
    attributes = attrs;
    this.cp_index = cp_index;
  }

  public void setAttr(Attr attr) {
    if (attributes==null) attributes = new Attr[]{ attr };
    else {
      for (int i=0; i<attributes.length; ++i) {
        if (attr.name_index == attributes[i].name_index) {
          attributes[i] = attr;
          return;
        }
      }
      Attr[] aa = new Attr[attributes.length+1];
      System.arraycopy(attributes,0,aa,0,attributes.length);
      aa[attributes.length] = attr;
      attributes = aa;
    }
  }

  public static Member read(ClassFileReader cfr, DataInputStreamEx dis)
                       throws IOException, BadClassFormatException
  {
    Member m = new Member();

    m.access_flags = dis.readU2();
    m.name_index = dis.readU2();
    m.desc_index = dis.readU2();
    int cnt = dis.readU2();
    if (cnt <= 0) {
      m.attributes = null;
    } else {
      m.attributes = new Attr[cnt];
      for (int i=0; i<cnt; ++i)
        m.attributes[i]=Attr.read(cfr,dis);
    }
    return m;
  }
  

  public void printHtml(ClassFileReader cfr, PrintWriter out) throws IOException, BadClassFormatException {
    out.println("<table border=1>");
    out.println("<tr><td>access flags</td><td>" +
                StringUtils.defaultString(ClassFileUtil.getAccessFlagNames(access_flags),"&nbsp;")+"</td></tr>");
    out.println("<tr><td>name index</td><td>" + ClassFileUtil.getHtmlRefCP(name_index) +
                " <b>" + SimpleLib.replace(false,cfr.getCPString(name_index),"<","&lt;") + "</b></td></tr>");
    out.println("<tr><td>descriptor index</td><td>" + ClassFileUtil.getHtmlRefCP(desc_index) +
                " <b>" + cfr.getCPString(desc_index) + "</b></td></tr>");
    if (attributes != null) {
      out.println("<tr><td>attributes</td><td>");
      ClassFileUtil.printAttrsHtml(out,attributes);
      out.println("</td></tr>");
    }
    out.println("</table>");
  }

  public void write(DataOutputStream out) throws IOException {
    out.writeShort(access_flags);
    out.writeShort(name_index);
    out.writeShort(desc_index);
    if (attributes == null) out.writeShort(0);
    else {
      out.writeShort(attributes.length);
      for ( int i=0; i<attributes.length; attributes[i++].write(out) );
    }
  }

} // end of class Member.
