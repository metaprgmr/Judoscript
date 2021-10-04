/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 03-17-2002  JH   Initial open source release.
 * 02-16-2005  JH   Major boost of JUSP technology!
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/

package com.judoscript.jusp;

import java.io.*;
import java.util.*;
import java.net.*;
import java.security.Principal;
import javax.servlet.http.*;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUpload;

public final class JuspRequest
{
	private JuspContext context;
	private HttpServletRequest request;
	private Map form = null;
	private List uploads = null;
	
  public JuspRequest(HttpServletRequest req, JuspContext ctxt) {
  	request = req;
  	context = ctxt;
  }

  public HttpServletRequest getHttpServletRequest() { return request; }
  
	public Map getForm() throws Exception {
		if (form == null) {
			form = new HashMap();
	    if (FileUpload.isMultipartContent(request)) { // uploads
	     	DiskFileUpload upload = new DiskFileUpload();
	
				// Set upload parameters
	     	upload.setSizeThreshold(context.getUploadMaxMemorySize());
	     	upload.setSizeMax(context.getUploadMaxRequestSize());
	     	upload.setRepositoryPath(context.getUploadTempDirectory());
	
	     	uploads = new ArrayList();
	     	Iterator iter = upload.parseRequest(request).iterator();
	     	while (iter.hasNext()) {
	     		FileItem fi = (FileItem)iter.next();
	     		if (fi.isFormField())
	     			form.put(fi.getFieldName(), fi.getString());
	     		else if (StringUtils.isNotBlank(fi.getName()))
	     			uploads.add(new JuspUploadedFile(fi));
	     	}
	    } else { // regular form
	      Enumeration paramNames = request.getParameterNames();
	      while (paramNames.hasMoreElements()) {
	        String name = (String)paramNames.nextElement();
	        String[] vals = request.getParameterValues(name);
	        if (vals != null) {
	          if (vals.length==1) {
	            if (StringUtils.isNotBlank(vals[0]))
	              form.put(name, vals[0]);
	          } else {
	            form.put(name, vals);
	          }
	        }
	      }
	    }
		}
    return form;
	}

  public List getUploads() throws Exception {
  	if (form == null)
  		getForm();
  	return uploads;
  }

	public String getAbsoluteURL() {
  	String file = getURIWithQueryString();
  	try {
	  	return new URL(request.getScheme(), request.getServerName(), request.getServerPort(), file).toString();
  	} catch(MalformedURLException e) {
  		return file;
  	}
  }

	public String getURIWithQueryString() {
  	String file = request.getRequestURI();
  	if (request.getQueryString() != null)
  	   file += '?' + request.getQueryString();
  	return file;
	}
	
  // This really belongs to JuspServlet. Limited by the Servlet API.
//  public String getContextBaseURI() { return request.getContextPath(); }

  // javax.servlet.http.HttpServletRequest
  public String getAuthType() { return request.getAuthType(); }
  public Cookie[] getCookies(){ return request.getCookies(); }
  public Date getDateHeader(String name) { return new Date(request.getDateHeader(name)); }
  public String getHeader(String name) { return request.getHeader(name); }
  public Enumeration getHeaderNames() { return request.getHeaderNames(); }
  public Enumeration getHeaders(String name) { return request.getHeaders(name); }
  public String getMethod() { return request.getMethod(); }
  public String getPathInfo() { return request.getPathInfo(); }
  public String getPathTranslated() { return request.getPathTranslated(); }
  public String getQueryString() { return request.getQueryString(); }
  public String getRemoteUser() { return request.getRemoteUser(); }
  public String getRequestedSessionId() { return request.getRequestedSessionId(); }
  public String getRequestURI() { return request.getRequestURI(); }
  public String getServletPath() { return request.getServletPath(); }
  public HttpSession getSession() { return request.getSession(); }
  public HttpSession getSession(boolean create) { return request.getSession(create); }
  public Principal getUserPrincipal() { return request.getUserPrincipal(); }
  public boolean isRequestedSessionIdFromCookie() { return request.isRequestedSessionIdFromCookie(); }
  public boolean isRequestedSessionIdFromURL() { return request.isRequestedSessionIdFromURL(); }
  public boolean isRequestedSessionIdValid() { return request.isRequestedSessionIdValid(); }
  public boolean isUserInRole(String role) { return request.isUserInRole(role); }
  
  // javax.servlet.ServletRequest
	public Object getAttribute(String name) { return request.getAttribute(name); }
	public Enumeration getAttributeNames() { return request.getAttributeNames(); }
	public String getCharacterEncoding() { return request.getCharacterEncoding(); }
	public int getContentLength() { return request.getContentLength(); }
	public String getContentType() { return request.getContentType(); }
	public InputStream getInputStream() throws IOException { return request.getInputStream(); }
	public Locale getLocale() { return request.getLocale(); }
	public Enumeration getLocales() { return request.getLocales(); }
	public String getParameter(String name) { return request.getParameter(name); }
	public Enumeration getParameterNames() { return request.getParameterNames(); }
	public String[] getParameterValues(String name) { return request.getParameterValues(name); }
	public String getProtocol() { return request.getProtocol(); }
	public BufferedReader getReader() throws IOException { return request.getReader(); }
	public String getRemoteAddr() { return request.getRemoteAddr(); }
	public String getRemoteHost() { return request.getRemoteHost(); }
	public String getScheme() { return request.getScheme(); }
	public String getServerName() { return request.getServletPath(); }
	public int getServerPort() { return request.getServerPort(); }
	public boolean isSecure() { return request.isSecure(); }
	public void removeAttribute(String name) { request.removeAttribute(name); }
	public void setAttribute(String name, Object val) { request.setAttribute(name, val); }
	
} // end of class JuspRequest
