/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 03-17-2002  JH   Initial open source release.
 * 02-03-2005  JH   Major boost of JuSP technology!
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/

package com.judoscript.jusp;

import java.io.*;
import java.util.HashMap;
import java.lang.reflect.InvocationTargetException;
import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.judoscript.*;
import com.judoscript.user.JuspUtil;
import com.judoscript.user.JuspPageInfo;
import com.judoscript.parser.helper.ParserHelper;
import com.judoscript.util.Lib;


public final class JuspServlet extends HttpServlet implements JuspContext
{
  public static Log logger = LogFactory.getLog("judo.jusp");
  
  static HashMap staticPages = new HashMap(); // uri_qs => absolute_path
  
  static {
    RT.setLoggerLevel("info", logger);
  }
  
  /**
   * The one and only rgc for all the JuSP pages.
   */
  RuntimeGlobalContext rgc = null;

  /**
   * The one and only script object, starting with juspInitFile
   * and accepts generated JuSP functions.
   */
  Script script = null;

  /**
   * The one and only jusp_prep.judo instance.
   */
  Script preproc = null;

  /**
   * The JuSP work directory -- to hold the preprocessed JuSP pages.
   * Init param: "juspWorkDir".
   */
  String juspWorkDir = null;
  
  /**
   * The JuSP default content-type and page encoding.
   */
  String defaultPageEncoding = null;
  String defaultContentType = null;

  /**
   * The system and user error page URIs
   */
  String systemErrorPage = null;
  String userErrorPage = null;
  
  /**
   * When error occurs, the default handler should print stack trace or not:
   */
  boolean printStackTrace = true;

  /**
   * If set, the engine will check the timestamp of the generated files
   * against their source to re-process if needed.
   * Used during development; production systems should turn this off.
   */
  boolean checkTimestamp = true;

  /**
   * Upload parameters
   */
   int    uploadMaxMemorySize = 10*1024*1024;
   long   uploadMaxRequestSize = 10*1024*1024;;
   String uploadTempDirectory = System.getProperty("java.io.tmpdir");

  public String getJuspWorkDir() {
    return juspWorkDir;
  }

  public void setCheckTimestamp(boolean set) {
    checkTimestamp = set;
  }
  
  public void setDefaultContentType(String type) {
    defaultContentType = type;
  }

  public void setDefaultPageEncoding(String enc) {
    defaultPageEncoding = enc;
  }

  public void setSystemErrorPageURI(String uri) {
    systemErrorPage = uri;
  }

  public void setUserErrorPageURI(String uri) {
    userErrorPage = uri;
  }

  public void setPrintStackTrace(boolean set) { printStackTrace = set; }

   public int    getUploadMaxMemorySize() { return uploadMaxMemorySize; }
   public long   getUploadMaxRequestSize() { return uploadMaxRequestSize; }
   public String getUploadTempDirectory() { return uploadTempDirectory; }

   public void setUploadLimits(int maxMemSize, long maxRequestSize) {
     uploadMaxMemorySize = maxMemSize;
     uploadMaxRequestSize = maxRequestSize;
   }
   
   public void setUploadTempDir(String tmpDir) {
     uploadTempDirectory = tmpDir;
   }

   /**
   * Does these steps:
   * <ol>
   * <li> Prepare the JuSP preprocessor, "com/judoscript/jusp/jusp_prep.judo".
   * <li> Parse "juspInit.judo" or what's specified by 'juspInitFile'.
   * <li> creates a global runtime context and add global variables, and
   * <li> runs the JuSP initialization script within.
   * </ol>
   */
  public void init(ServletConfig config) throws ServletException {
    super.init(config);

    logger.info("******************************************");
    logger.info("*                                        *");
    logger.info("*   The JuSP (Judo Server Page) Engine   *");
    logger.info("*                                        *");
    logger.info("******************************************");
    logger.info("Host Server version: " + config.getServletContext().getServerInfo());
    logger.info("Judo/JuSP version:   " + VersionInfo.latest());
    logger.info("Java version:        " + System.getProperty("java.runtime.version"));
    
    //
    // 1. Prepare the JuSP preprocessor, "jusp_prep.judo".
    //
    Reader reader;
    String path = "com/judoscript/jusp/jusp_prep.judo";
    try {
      reader = JudoUtil.findFile(path, null);
      preproc = ParserHelper.parse(path, path, reader, null, 0, false);
    } catch(Exception e) {
      logger.fatal("Failed to load JuSP preprocessor.", e);
      throw new ServletException(e);
    }

    path = getInitParameter("juspWorkDir");
    if (StringUtils.isBlank(path)) {
      path = System.getProperty("java.io.tmpdir");
      if (StringUtils.isNotBlank(path) && new File(path).isDirectory())
        ;
      else
        throw new ServletException("Init parameter `juspWorkDir' is not set!");
    }
    
    File file = new File(path);
    if (!file.isDirectory())
      throw new ServletException("JuSP work directory: '" + path + "' does not exist!");
    juspWorkDir = file.getAbsolutePath();
    if (!juspWorkDir.endsWith("/") && !juspWorkDir.endsWith("\\"))
      juspWorkDir += File.separatorChar;
    logger.info("JuSP work directory is set to '" + path + "'.");

    //
    // 2. Parse "juspInit.judo" or what's specified by 'juspInitFile'.
    //
    try {
      // set JudoPath:
      RT.judoPath = getContextBasePath() + "WEB-INF/judo/";
      logger.info("JUDOPATH is set to '" + RT.judoPath + "'.");
      
      path = getInitParameter("juspInitFile");
      if (StringUtils.isBlank(path))
        path = "juspInit.judo";
      file = new File(path);
      try {
        reader = JudoUtil.findFile(path, null);
        script = ParserHelper.parse(file.getName(),
            RT.judoPath,
            reader,
            null,
            file.lastModified(),
            false);
        logger.info("juspInit.judo is parsed from classpath.");
      } catch(FileNotFoundException fnfe) {
        logger.info("juspInit.judo is not present.");
      }
    } catch(Exception e) {
      logger.fatal("Failed to parse JuSP initialization script.", e);
      throw new ServletException(e);
    }

    //
    // 3. Initialize the global JuSP context.
    //
    rgc = new RuntimeGlobalContext((String[])null, script);
    RT.DEFAULT_RTC = rgc;
    try { // Add global variables:
      rgc.setVariable("juspContext", JudoUtil.toVariable(this), 0);
    } catch(Throwable e) {
      logger.fatal("Failed to set the JuSP context object.", e);
      throw new ServletException(e);
    }
    Script script1 = null; // for "jusp_builtin.judo".
    try { // Run the internal, built-in JuSP start-up script:
      script1 = ParserHelper.parse("jusp_builtin.judo",
          "com/judoscript/jusp/",
          JudoUtil.findFile("com/judoscript/jusp/jusp_builtin.judo", null),
          null,
          0,
          false);
      script1.startAllowException(rgc);
    } catch(Throwable e) {
      logger.fatal("Failed to run the built-in JuSP initialization.", e);
      throw new ServletException(e);
    }

    if (script == null) {
      script = script1;
    } else {
      try { // Run the user start-up script:
        script.takeDecls(script1);
        logger.info("[juspInit.judo]");
        script.startAllowException(rgc);
      } catch(Throwable e) {
        logger.fatal("Failed to run the JuSP initialization script.", e);
        throw new ServletException(e);
      } finally {
        logger.info("[/juspInit.judo]");
      }
    }
    
    RT.pushContext(rgc);
    // ready to go!
  }


  protected void service(HttpServletRequest request, HttpServletResponse response)
                        throws ServletException, IOException
  {
    _service(request, response, null, null, null, null, ACTION_NORMAL);
  }
  
  static final int ACTION_NORMAL  = 0;
  static final int ACTION_CALL    = 1;
  static final int ACTION_FORWARD = 2;
  
  /**
   * 
   * @param request is either HttpServletRequest or JuspRequest
   * @param response is either HttpServletResponse or JuspResponse
   * @param uri_src_fxn
   * @param form
   * @param session
   * @param action
   * @throws ServletException
   * @throws IOException
   */
  private void _service(Object request,
                        Object response,
                        String[] path_info,
                        HttpSession session,
                        Object form,
                        Object error,
                        int action) throws ServletException, IOException
  {
    //
    // 1. Get the URI and the absolute path to the page.
    //
    String[] pathInfo;
    if (action == ACTION_NORMAL)
      pathInfo = getUriAndPath(request);
    else
      pathInfo = path_info;

    //
    // 2. Preprocess the page, if need to.
    //
    Function jusp = null;
    try {
      jusp = script.getFunction(pathInfo[JuspUtil.PI_FUNCTIONNAME]);

      if (jusp == null) { // the JuSP page is not parsed yet.
        jusp = getJuspFunction(pathInfo);
      } else if (checkTimestamp) {
        long juspTime = ((Expr)jusp.getAnnotation().get("generated")).getLongValue();
        long fileTime = new File(pathInfo[JuspUtil.PI_FILEPATH]).lastModified();
        if (fileTime > juspTime) { // reload.
          if (logger.isDebugEnabled())
            logger.debug("Source for file \"" + pathInfo[JuspUtil.PI_FILEPATH] + "\" has changed; re-process.");
          jusp = getJuspFunction(pathInfo);
        } else if (logger.isDebugEnabled()) {
          logger.debug("Use existing file \"" + pathInfo[JuspUtil.PI_FILEPATH] + "\".");
        }
      }
    } catch(Throwable ex) {
      // system error handler
      JuspResponse res = (response instanceof HttpServletResponse)
                          ? new JuspResponse((HttpServletResponse)response)
                          : (JuspResponse)response;

      if (StringUtils.isNotBlank(systemErrorPage)) {
        JuspRequest req = (request instanceof HttpServletRequest)
                          ? new JuspRequest((HttpServletRequest)request, this)
                          : (JuspRequest)request;
        if (req.getAttribute("exception") == null) { // not an exception handler failure
           req.setAttribute("exception", ex);
           forward(getErrorPagePathInfo(req, systemErrorPage, "systemErrorPage"), req, res, null, null);
           return;
        }
      }

      JuspWriter w = res.getWriter();
      w.print("<html><head><title>JuSP Compilation Error</title><body><h2><u>JuSP Compilation Error:</u></h2><pre>Failed to process the request \"");
      w.print(pathInfo[JuspUtil.PI_URI]);
      w.println("\":");
      w.println(ex.getMessage());
      w.println("</pre></body></html>");
      w.flush();
      w.close();
      return;
    }

    //
    // 3. If the page is static, process accordingly.
    //    Otherwise, prepare the parameters and call the page.
    HttpServletRequest httpreq = getHttpServletRequest(request);
    HttpServletResponse httpres = getHttpServletResponse(response);
    if (request instanceof HttpServletRequest)
      request = new JuspRequest((HttpServletRequest)request, this);

    if (response instanceof HttpServletResponse)
      response = new JuspResponse((HttpServletResponse)response);

    if (action == ACTION_FORWARD)
      ((JuspResponse)response).getWriter().clearBuffer();

    Expr[] params;
    try {
      Variable pi = (Variable)jusp.getAnnotation().get("pageInfo");
      JuspPageInfo pageInfo = (pi == null) ? null : (JuspPageInfo)pi.getObjectValue();
      if (pageInfo != null && pageInfo.isStatic_refresh >= 0) { // IS STATIC
        String uri_qs = ((JuspRequest)request).getURIWithQueryString();
        String path = (String)staticPages.get(uri_qs);
        File f;
        if (path != null) {
          f = new File(path);
        } else {
          f = File.createTempFile("juspcache_", null, new File(juspWorkDir));
          path = f.getAbsolutePath();
          staticPages.put(uri_qs, path);
        }

        if (!f.exists() || f.length() == 0) {
          synchronized(this) {
            if (!f.exists() || f.length() == 0)
              generateCachedFile((JuspRequest)request, (JuspResponse)response, form, pageInfo, jusp, f);
          }
        } else {
          long refreshPeriod = pageInfo.isStatic_refresh * 1000; // milliseconds
          if (refreshPeriod > 0) {
            if (System.currentTimeMillis() - f.lastModified() > refreshPeriod) {
              synchronized(this) {
                if (System.currentTimeMillis() - new File(path).lastModified() > refreshPeriod)
                  generateCachedFile((JuspRequest)request, (JuspResponse)response, form, pageInfo, jusp, f);
              }
            }
          }
        }

        //
        // Now, write the file out.
        //
        httpres.setContentLength((int)f.length());

        if (pageInfo != null && pageInfo.contentType != null)
          httpres.setContentType(pageInfo.contentType);
        else if (defaultContentType != null)
          httpres.setContentType(defaultContentType);

        Lib.copyStream(new FileInputStream(f), httpres.getOutputStream());

      } else { // NOT STATIC
        if (pageInfo != null && pageInfo.contentType != null)
          httpres.setContentType(pageInfo.contentType);
        else if (defaultContentType != null)
          httpres.setContentType(defaultContentType);

        params = new Expr[] {
          JudoUtil.toVariable(request),
          JudoUtil.toVariable(response),
          JudoUtil.toVariable(getContextBaseURI()),
          new JuspSession(session==null ? httpreq.getSession(true) : session),
          JudoUtil.toVariable(form),
          JudoUtil.toVariable(error),
          action==ACTION_CALL ? ConstInt.TRUE : ConstInt.FALSE
        };
  
        try {
          RT.pushContext(RT.newSubContext());
          jusp.invoke(params, null);
        } finally {
          RT.popContext();
        }
      }
    } catch(Throwable e) {
      // If the user error handler fail
//      JuspRequest req1 = (request instanceof HttpServletRequest)
//                         ? new JuspRequest((HttpServletRequest)request, this)
//                         : (JuspRequest)request;
//      if (req1.getAttribute("exception") != null) { // an exception handler failure
      JuspResponse jres = (response instanceof HttpServletResponse)
                          ? new JuspResponse((HttpServletResponse)response)
                          : (JuspResponse)response;
      printErrorPage(jres.getWriter(), e);
    }

  } // service().

  synchronized Function getJuspFunction(String[] pathInfo) throws Throwable {
    // Preprocess the JuSP into a JUDO file:
    Variable retVal = (Variable)preproc.startAllowException(pathInfo);

    // Parse the preprocessed JUDO file:
    String path = juspWorkDir + pathInfo[JuspUtil.PI_FUNCTIONNAME];
    Reader reader = JudoUtil.findFile(path, defaultPageEncoding);
    Script s = ParserHelper.parse(pathInfo[JuspUtil.PI_URI], juspWorkDir, reader, defaultPageEncoding, 0, false);
    script.takeDecls(s); // Take the generated JuSP function
    Function jusp = script.getFunction(pathInfo[JuspUtil.PI_FUNCTIONNAME]);
    if (logger.isDebugEnabled())
      logger.debug("Generated file \"" + pathInfo[JuspUtil.PI_FILEPATH] + "\".");

    if (retVal != null)
      jusp.getAnnotation().put("pageInfo", retVal);

    return jusp;
  }

  void generateCachedFile(JuspRequest request, JuspResponse response, Object form,
                          JuspPageInfo pageInfo, Function jusp, File f)
    throws Throwable
  {
    if (logger.isDebugEnabled()) {
      logger.debug("Caching static page for \"" + request.getURIWithQueryString() + "\"");
      logger.debug("                     as \"" + f.getAbsolutePath() + "\".");
    }

    Writer wr;
    if (pageInfo == null || pageInfo.pageEncoding == null)
      wr = new FileWriter(f);
    else
      wr = new OutputStreamWriter(new FileOutputStream(f), pageInfo.pageEncoding);
    response.getWriter().setWriter(wr);

    Expr[] params = new Expr[] {
        JudoUtil.toVariable(request),
        JudoUtil.toVariable(response),
        JudoUtil.toVariable(getContextBaseURI()),
        ValueSpecial.NIL,
        JudoUtil.toVariable(form),
        ValueSpecial.NIL,
        ConstInt.FALSE
      };
    jusp.invoke(params, null);
    try { wr.close(); } catch(Exception e) {}
  }

  String _basePath = null;
  public String getContextBasePath() {
    if (_basePath == null)
      _basePath = getServletContext().getRealPath("/");
    return _basePath;
  }

  // hack
  String _baseURI = null;
  public String getContextBaseURI() {
    if (_baseURI == null) {
      String x = getContextBasePath();
      x = x.substring(0, x.length()-1);
      int idx = x.lastIndexOf("/");
      if (idx < 0)
        idx = x.lastIndexOf("\\");
      _baseURI = "/" + x.substring(idx+1);
    }
    return _baseURI;
  }

  // returns: String[]{ uri, filePath, functionName, juspWorkDir }
  // The JuSP preprocessor only uses the first three.
  public String[] getUriAndPath(Object request) {
    HttpServletRequest req = getHttpServletRequest(request);
    String uri = req.getRequestURI();
    String fxnName = JuspUtil.uriToFunctionName(uri);
    String filePath = getServletContext().getRealPath(req.getServletPath());
    String ctxPath = req.getContextPath();
    return new String[] { uri, filePath, fxnName, ctxPath, juspWorkDir };
  }

  private HttpServletRequest getHttpServletRequest(Object request) {
    return (request instanceof JuspRequest)
             ? ((JuspRequest)request).getHttpServletRequest()
             : (HttpServletRequest)request;
  }

  private HttpServletResponse getHttpServletResponse(Object response) {
    return (response instanceof JuspResponse)
             ? ((JuspResponse)response).getHttpServletResponse()
             : (HttpServletResponse)response;
  }

  //
  // JuSP context methods
  //
  public void invoke(String uri,
                     String refUri,
                     String refFilePath,
                     JuspRequest request,
                     JuspResponse response,
                     HttpSession session,
                     Object form,
                     Object error) throws ServletException, IOException
  {
    String[] pathInfo = JuspUtil.uriToFilePath(uri, refUri, refFilePath, getContextBaseURI(), juspWorkDir);
    _service(request, response, pathInfo, session, form, error, ACTION_CALL);
  }

  public void forward(String uri,
                      String refUri,
                      String refFilePath,
                      JuspRequest request,
                      JuspResponse response,
                      HttpSession session,
                      Object form,
                      Object error) throws ServletException, IOException
  {
    String[] pathInfo = JuspUtil.uriToFilePath(uri, refUri, refFilePath, getContextBaseURI(), juspWorkDir);
    _service(request, response, pathInfo, session, form, error, ACTION_FORWARD);
  }
  
  void forward(String[] pathInfo,
               JuspRequest request,
               JuspResponse response,
               HttpSession session,
               Object error) throws ServletException, IOException
  {
    _service(request, response, pathInfo, session, null, error, ACTION_FORWARD);
  }
  
  // only "uri" and "response" are used.
  public void redirect(String uri, JuspResponse response) throws ServletException, IOException
  {
    response.sendRedirect(uri);
  }

  // app error handler
  public void error(JuspRequest request,
                    JuspResponse response,
                    HttpSession session,
                     Throwable ex) throws ServletException, IOException
  {
    if (StringUtils.isNotBlank(userErrorPage)) {
      request.setAttribute("exception", ex);
      forward(getErrorPagePathInfo(request, userErrorPage, "userErrorPage"), request, response, session, null);
      return;
    }

    JuspWriter w = response.getWriter();
    try { w.clearBuffer(); } catch(Exception e) {} // if not, just let the page mess up.
    printErrorPage(w, ex);
  }

  private String[] getErrorPagePathInfo(JuspRequest req, String pageURI, String fxnName) {
    String ctxtPath = getContextBaseURI();
    return new String[]{
      ctxtPath + pageURI,
      getServletContext().getRealPath(pageURI),
      fxnName,
      ctxtPath,
      juspWorkDir
    };
  }
  
  private void printErrorPage(JuspWriter w, Throwable ex) {
    w.println("<html><head><title>JuSP Error</title><body><h2><u>JuSP Error:</u></h2><pre>");
    if (ex instanceof ExceptionRuntime)
      w.println(ex.toString());
    else {
      if (ex instanceof InvocationTargetException)
        ex = ((InvocationTargetException)ex).getTargetException();

      if (ex instanceof ServletException)
        ex = ((ServletException)ex).getRootCause();

      String clsName = ex.getClass().getName();
      if (clsName.startsWith("java"))
        clsName = clsName.substring(clsName.lastIndexOf(".") + 1);
  
      w.println("Unhandled " + clsName);
      int[] stmtLines = RT.curCtxt().getLineStack().toIntArray();
      for (int i=stmtLines.length-1; i>=0; --i)
        w.println("  at line #" + stmtLines[i]);
      
      w.println();
      if (printStackTrace)
        w.println(Lib.getStackTrace(ex));
      else
        w.println(ex.getMessage());
    }
    w.println("</pre></body></html>");
    w.flush();
    w.close();
  }

} // end of class JuspServlet.
