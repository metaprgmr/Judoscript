/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 02-10-2005  JH   Inception.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript.jusp;

import java.io.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

public interface JuspContext
{
	public String getJuspWorkDir();
	
  public String getContextBasePath();

  public String getContextBaseURI();
  
  public void setCheckTimestamp(boolean set);
  
  public void setDefaultContentType(String type);
  
  public void setDefaultPageEncoding(String enc);

  // uri: relative to web app context base URI.
  public void setSystemErrorPageURI(String uri);
  
  // uri: relative to web app context base URI.
  public void setUserErrorPageURI(String uri);
  
 	public int getUploadMaxMemorySize();
 	public long getUploadMaxRequestSize();
 	public String getUploadTempDirectory();
 	public void setUploadLimits(int maxMemSize, long maxRequestSize);
 	public void setUploadTempDir(String tmpDir);

  public void invoke(String uri,
                     String refUri,
                     String refFilePath,
                     JuspRequest request,
                     JuspResponse response,
                     HttpSession session,
										 Object form,
										 Object error) throws ServletException, IOException;

  public void forward(String uri,
                      String refUri,
                      String refFilePath,
                      JuspRequest request,
                      JuspResponse response,
                      HttpSession session,
											Object form,
											Object error) throws ServletException, IOException;
  
  public void redirect(String uri, JuspResponse response) throws ServletException, IOException;

  public void error(JuspRequest request,
										JuspResponse response,
										HttpSession session,
										Throwable th) throws ServletException, IOException;
  
} // end of interface JuspContext.
