/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 02-07-2005  JH   Inception.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript.jusp;

import java.io.IOException;
import java.io.Writer;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletResponse;

////////////////////////////////////////
// To make this a PrintWriter and also provide a controllable
// buffered writer, we have to use a static create() method.
//
public final class JuspWriter extends PrintWriter
{
  public static JuspWriter create(HttpServletResponse res) {
    _BufferedJuspWriter x= new _BufferedJuspWriter(res);
    JuspWriter ret = new JuspWriter(x);
    ret.bufWriter = x;
    return ret;
  }

  private _BufferedJuspWriter bufWriter;

  private JuspWriter(_BufferedJuspWriter w) { super(w); }

  public void setBufferSize(int size) { bufWriter.setBufferSize(size); }
  public boolean hasBufferFlushed() { return bufWriter.hasBufferFlushed(); }
  public void clearBuffer() throws JuspException { bufWriter.clearBuffer(); }
  public void setWriter(Writer wr) { bufWriter.setWriter(wr); }

  /**
   * Disable this writer.
   */
  public void setBinary() { bufWriter.setIsBinary(); }

  //
  // Inner class.
  //
  static class _BufferedJuspWriter extends Writer
  {
    // buf is used only before the first flush,
    // which will flush the content to wr and
    // set buf=null. The following writes will be
    // written directly to wr.
    StringBuffer buf = new StringBuffer();
    int bufSize = 8192;
    HttpServletResponse response;
    Writer wr = null;
    boolean isBinary = false;

    _BufferedJuspWriter(HttpServletResponse res) { response = res; }

    public void setWriter(Writer wr) { this.wr = wr; }
    public void setIsBinary() { isBinary = true; buf = null; }
    public void setBufferSize(int size) { bufSize = size; }
    public boolean hasBufferFlushed() { return buf == null; }

    public void clearBuffer() throws JuspException {
      if (hasBufferFlushed())
        throw new JuspException("Buffer has been flushed and is unable to be reset.");
      buf.setLength(0);
    }

    public void close() throws IOException {
      if (isBinary)
        return; // silently ignore.

			try {
	      buf = null;
				wr.close();
	      wr = null;
			} catch(Exception e) {}
    }

    public void flush() throws IOException {
      if (isBinary)
        return; // silently ignore.

      if (buf != null) {
      	if (wr == null)
      		wr = response.getWriter();
        wr.write(buf.toString());
        buf = null;
      }
      wr.flush();
    }

    public void write(char ch) throws IOException {
      if (isBinary)
        return; // silently ignore.

      if (buf != null)
      	buf.append(ch);

    	wr.write(ch);
    }

    public void write(char[] ch, int off, int len) throws IOException {
      if (isBinary)
        return; // silently ignore.

      if (buf != null) {
        buf.append(ch, off, len);
        if (bufSize > 0 && buf.length() > bufSize)
          flush();
      } else {
        wr.write(ch, off, len);
      }
    }

  }; // end of inner class _BufferedJuspWriter.

} // end of class JuspWriter.
