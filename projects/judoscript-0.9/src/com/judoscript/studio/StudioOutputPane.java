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


package com.judoscript.studio;

import java.io.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.text.*;
import com.judoscript.util.LinePrintWriter;


public final class StudioOutputPane extends JTextPane implements StudioIODevice
{
  static final int maxBufLen = 256;

  StringBuffer[] bufs = new StringBuffer[] { new StringBuffer(maxBufLen),
                                             new StringBuffer(maxBufLen),
                                             new StringBuffer(maxBufLen) };
  SimpleAttributeSet[] attrs;
  DefaultStyledDocument doc;

  ////////////////////////
  // c'tor.

  public StudioOutputPane() {
    attrs = new SimpleAttributeSet[5];

    attrs[OUT] = new SimpleAttributeSet();
    StyleConstants.setFontFamily(attrs[OUT], "Courier New");
    StyleConstants.setFontSize(attrs[OUT], 12);

    attrs[ERR] = new SimpleAttributeSet(attrs[OUT]);
    StyleConstants.setForeground(attrs[ERR], Color.red);

    attrs[LOG] = new SimpleAttributeSet(attrs[OUT]);
    StyleConstants.setBackground(attrs[LOG], new Color(0xEE,0xEE,0xEE));

    attrs[INFO] = new SimpleAttributeSet();
    StyleConstants.setFontFamily(attrs[INFO], "SanSerif");
    StyleConstants.setFontSize(attrs[INFO], 12);
    StyleConstants.setItalic(attrs[INFO], true);

    attrs[TITLE] = new SimpleAttributeSet(attrs[INFO]);
    StyleConstants.setUnderline(attrs[TITLE], true);

    doc = new DefaultStyledDocument();
    setStyledDocument(doc);

    setEditable(false);
    setMargin(new Insets(5,5,5,5));
  }

  public void clear() { try { doc.remove(0,doc.getLength()); } catch(BadLocationException ble) {} }

  void scrollToEnd() { setCaretPosition(doc.getLength()); }

  public void close(int type) { flush(type); }

  public void flush(int type) {
    private_write(type, bufs[type].toString());
    bufs[type].setLength(0);
  }

  public void write(int type, int ch) { bufs[type].append((char)ch); }

  public void write(int type, char[] cbuf, int off, int len) {
    int limit = off + len;
    for (; off<limit; off++)
      write(type,cbuf[off]);
    if (bufs[type].length() >= maxBufLen)
      flush(type);
  }

  public void write(int type, String str, int off, int len) {
    int limit = off + len;
    for (; off<limit; off++)
      write(type,str.charAt(off));
    if (bufs[type].length() >= maxBufLen)
      flush(type);
  }

  public void write(int type, String str) {
    if (str == null) return;
    write(type, str, 0, str.length());
  }

  public void writeln(int type, String str) {
    write(type, appendln(str));
    flush(type);
  }

  public LinePrintWriter getOutWriter() { return new LinePrintWriter(new _Writer(OUT)); }
  public LinePrintWriter getErrWriter() { return new LinePrintWriter(new _Writer(ERR)); }
  public LinePrintWriter getLogWriter() { return new LinePrintWriter(new _Writer(LOG)); }
  public BufferedReader getIn()  { return null; } // TODO

  class _Writer extends Writer
  {
    int type;
    public _Writer(int type) { this.type = type; }
    public void write(int ch) { StudioOutputPane.this.write(type,ch); }
    public void write(char[] chs, int start, int len) { StudioOutputPane.this.write(type,chs,start,len); }
    public void flush() { StudioOutputPane.this.flush(type); }
    public void close() {}
  }

/*
  class _OutputStream extends OutputStream
  {
    int type;
    public _OutputStream(int type) { this.type = type; }
    public void write(int ch) { StudioOutputPane.this.write(type,ch); }
  }
*/

  public void printOut(String str) { write(OUT, str); }
  public void printErr(String str) { write(ERR, str); }
  public void printLog(String str) { write(LOG, str); }
  public void printlnOut(String str) { writeln(OUT, str); scrollToEnd(); }
  public void printlnErr(String str) { writeln(ERR, str); scrollToEnd(); }
  public void printlnLog(String str) { writeln(LOG, str); scrollToEnd(); }

  public void printInfo(String str) { private_write(INFO, str); }
  public void printlnInfo(String str) { private_write(INFO, appendln(str)); scrollToEnd(); }
  public void printTitle(String str) { private_write(TITLE, str); }
  public void printlnTitle(String str) { private_write(TITLE, appendln(str)); scrollToEnd(); }

  public void println() {
    try { doc.insertString(doc.getLength(), "\n", null); } catch(BadLocationException ble) {}
    scrollToEnd();
  }

  private void private_write(int type, String str) {
    try { doc.insertString(doc.getLength(), str, attrs[type]); } catch(BadLocationException bpe) {}
  }

  private String appendln(String s) { return (s == null) ? "\n" : (s+"\n"); }

} // end of class StudioOutputPane.
