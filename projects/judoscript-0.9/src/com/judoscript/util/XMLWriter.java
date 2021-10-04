/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 03-17-2002  JH   Initial open source release.
 * 06-02-2002  JH   Added printPrelude() and close().
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript.util;

import java.util.Stack;
import java.io.PrintWriter;
import java.io.OutputStream;
import org.apache.commons.lang.StringUtils;

public class XMLWriter
{
  private Stack stack = new Stack();
  private boolean lastNL = false;
  private PrintWriter out;

  public XMLWriter(OutputStream os) { out = new PrintWriter(os); }
  public XMLWriter(PrintWriter out) { this.out = out; }

  public void close() { try { out.close(); } catch(Exception e) {} }

  public final void printPrelude() { print("<?xml version=\"1.0\" ?>"); }

  public final void simpleTag(String name) { openTag(name); closeTag(); }

  public final void simpleTagLn(String name) { openTag(name); closeTagLn(); }

  public final void simpleSingleTag(String name) { openTag(name); closeSingleTag(); }

  public final void simpleSingleTagLn(String name) { openTag(name); closeSingleTagLn(); }

  public final void openTagLn(String name) {
    openTag(name);
    out.println();
  }

  public final void openTag(String name) {
    stack.push(name);

    if (lastNL) writeTabs();
    out.print("<");
    out.print(name);
  }

  public final void closeTag() { out.print(">"); lastNL = false; }
  public final void closeTagLn() { out.println(">"); lastNL = true; }

  public final void closeSingleTag() { out.print("/>"); stack.pop(); lastNL = false; }
  public final void closeSingleTagLn() { closeSingleTag(); out.println(); lastNL = true; }

  public final void writeLine(String line) { writeTabs(); out.println(line); lastNL = true; }

  public final void endTag() {
    if (lastNL) writeTabs();
    out.print("</");
    out.print(stack.pop());
    out.print(">");
    lastNL = false;
  }

  public final void endTagLn() { endTag(); out.println(); lastNL = true; }

  public void tagAttr(String name, boolean val) {
    writeAttrName(name,'"'); out.print(val ? "true" : "false"); out.print('"');
  }

  public void tagAttr(String name, byte val) {
    writeAttrName(name,'"'); out.print(val); out.print('"');
  }

  public final void tagAttr(String name, char val) {
    writeAttrName(name,'"'); out.print(val); out.print('"');
  }

  public final void tagAttr(String name, int val) {
    writeAttrName(name,'"'); out.print(val); out.print('"');
  }

  public final void tagAttr(String name, long val) {
    writeAttrName(name,'"'); out.print(val); out.print('"');
  }

  public final void tagAttr(String name, float val) {
    writeAttrName(name,'"'); out.print(val); out.print('"');
  }

  public final void tagAttr(String name, double val) {
    writeAttrName(name,'"'); out.print(val); out.print('"');
  }

  public final void tagAttr(String name, String val) {
    if (!StringUtils.isBlank(val)) { writeAttrName(name); printQuoted(val); }
  }

  public final void print(String v) { out.print(v); }
  public final void println(String v) { out.println(v); lastNL = true; }
  public final void print(char v) { out.print(v); }
  public final void println(char v) { out.println(v); lastNL = true; }
  public final void print(int v) { out.print(v); }
  public final void println(int v) { out.println(v); lastNL = true; }
  public final void print(long v) { out.print(v); }
  public final void println(long v) { out.println(v); lastNL = true; }
  public final void print(float v) { out.print(v); }
  public final void println(float v) { out.println(v); lastNL = true; }
  public final void print(double v) { out.print(v); }
  public final void println(double v) { out.println(v); lastNL = true; }
  public final void println() { out.println(); lastNL = true; }
  public final void flush() { out.flush(); }

  public final void printQuoted(String v) {
    if (StringUtils.isBlank(v)) return;
    char quot = (v.indexOf('"')>=0) ? '"' : '"';
    out.print(quot);
    out.print(v);
    out.print(quot);
  }

  public final void printQuoted(int    v) { out.print('"'); out.print(v); out.print('"'); }
  public final void printQuoted(long   v) { out.print('"'); out.print(v); out.print('"'); }
  public final void printQuoted(float  v) { out.print('"'); out.print(v); out.print('"'); }
  public final void printQuoted(double v) { out.print('"'); out.print(v); out.print('"'); }

  private final void writeTabs() {
    for (int i=stack.size()-1; i>=1; i--)
      out.print("  ");
  }

  private final void writeAttrName(String name) { writeAttrName(name,(char)0); }

  private final void writeAttrName(String name, char quote) {
    out.print(" ");
    out.print(name);
    out.print("=");
    if (quote > 0) out.print(quote);
  }

} // end of class XMLWriter.
