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


package com.judoscript.jusp;

import java.io.*;
import javax.servlet.http.*;

/**
 * This class buffers textual content until a setXXX() method is called.
 */
public final class JuspResponse
{
  JuspWriter writer;
  HttpServletResponse response;

  public JuspResponse(HttpServletResponse res) throws IOException {
  	response = res;
    writer = JuspWriter.create(res);
  }

  public HttpServletResponse getHttpServletResponse() { return response; }
 
  public JuspWriter getWriter() { return writer; }

  public OutputStream getOutputStream() throws IOException {
  	return response.getOutputStream();
  }

  public void setIsErrorPage(boolean set) {
  	// TODO
  }
	
  public void setIsBinary() {
  	writer.setBinary();
  }

  public void setContentType(String ctype) {
  	response.setContentType(ctype);
  }

  public void setPageEncoding(String enc) {
		// TODO
  }

  // HttpServletResponse methods:
  public void addCookie(Cookie c) { response.addCookie(c); }
  public void addHeader(String n, String v) { response.addHeader(n, v); }
  public boolean containsHeader(String n) { return response.containsHeader(n); }
  public String encodeRedirectURL(String url) { return response.encodeRedirectURL(url); }
  public String encodeURL(String url) { return response.encodeURL(url); }
  public void sendError(int sc) throws IOException { response.sendError(sc); }
  public void sendError(int sc, String msg) throws IOException { response.sendError(sc, msg); }
  public void sendRedirect(String loc) throws IOException { response.sendRedirect(loc); }
  public void setHeader(String n, String v) { response.setHeader(n, v); }
  public void setStatus(int sc) throws IOException { response.setStatus(sc); }

} // end of class JuspResponse.
