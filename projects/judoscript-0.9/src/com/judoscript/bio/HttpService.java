/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 03-17-2002  JH   Initial open source release.
 *                  For HTTP server handler.
 * 05-07-2002  JH   Fixed a bug for serveFile() when the parameter is empty.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript.bio;

import java.io.*;
import java.net.*;
import java.util.*;
import org.apache.commons.lang.StringUtils;
import com.judoscript.*;
import com.judoscript.util.*;


public final class HttpService extends ObjectInstance
{
  Socket socket;
  LineInputStream instream;
  String method;
  String version;
  int responseStatus = 200;
  String responseMsg = null;
  Map url;
  Map requestHeaders = new HashMap();
  ArrayList responseHeaders = null;
  int port;

  public HttpService(Socket s, int port) throws IOException {
    super();
    checkMimeTypes();
    socket = s;
    this.port = port;
    instream = new LineInputStream(s.getInputStream());
    String line = instream.readLine();
    int idx = line.indexOf(' ');
    method = line.substring(0,idx);
    idx = line.lastIndexOf(' ');
    version = line.substring(idx+1);
    url = Lib.parseUrl(line.substring(method.length(),idx).trim(), null);
    while (true) {
      line = instream.readLine();
      if ((line==null) || (line.length()==0))
        break;
      idx = line.indexOf(':');
      requestHeaders.put(line.substring(0,idx),line.substring(idx+1).trim());
    }
  }

  OutputStream getOutputStream() throws IOException {
    byte[] crlf = { '\r', '\n' };
    OutputStream os = socket.getOutputStream();

    // response line
    os.write("HTTP/1.0 ".getBytes());
    os.write(String.valueOf(responseStatus).getBytes());
    os.write(' ');
    os.write(((responseMsg!=null) ? responseMsg : getHttpResponseMsg(responseStatus)).getBytes());
    os.write(crlf);

    // response headers, including cookies
    int i = (responseHeaders == null) ? -1 : responseHeaders.size()-1;
    for (; i>=0; --i) {
      os.write(((String)responseHeaders.get(i)).getBytes());
      os.write(crlf);
    }

    os.write(crlf); // closing empty line
    return os;
  }

  void serveError() throws IOException {
    int i = (responseHeaders==null) ? -1 : responseHeaders.size()-1;
    for (; i>=0; --i) {
      String s = ((String)responseHeaders.get(i)).toLowerCase();
      if (s.startsWith("content-length:")) {
        responseHeaders.set(i, "Content-Length: 0");
        break;
      }
    }
    if (i<0) {
      if (responseHeaders==null) responseHeaders = new ArrayList();
      responseHeaders.add("Content-Length: 0");
    }
    getOutputStream().close();
  }

  public String getTypeName() { return "HTTPService"; }

  public void close() {
    try { socket.close(); socket = null; } catch(Exception e) {}
  }

  public Variable resolveVariable(String name) throws Throwable {
    return JudoUtil.toVariable((String)requestHeaders.get(name));
  }

  public Variable setVariable(String name, Variable val, int type) throws Throwable {
    long time = JudoUtil.getTime(val);
    String v = (time > 0) ? new HttpDate(time).toString() : val.getStringValue();
    if (responseHeaders==null) responseHeaders = new ArrayList();
    responseHeaders.add(name + ": " + v);
    return val;
  }

  public int getContentLength() {
    try { return Integer.parseInt((String)requestHeaders.get("Content-Length")); }
    catch(Exception e) { return -1; }
  }

  public Variable invoke(String fxn, Expr[] params, int[] javaTypes) throws Throwable
  {
    int i;
    Variable v;
    String s;
    int len = (params==null) ? 0 : params.length;
    switch(getMethodOrdinal(fxn)) {
    case BIM_GETURL:          return JudoUtil.toVariable((String)url.get("url"));
    case BIM_GETHOST:         return JudoUtil.toVariable((String)url.get("host"));
    case BIM_GETPORT:         return JudoUtil.toVariable((String)url.get("port"));
    case BIM_GETDOMAIN:       return JudoUtil.toVariable((String)url.get("domain"));
    case BIM_GETPATH:         return JudoUtil.toVariable((String)url.get("path"));
    case BIM_GETFILENAME:     return JudoUtil.toVariable((String)url.get("file_name"));
    case BIM_GETQUERY:        return JudoUtil.toVariable((String)url.get("query_string"));
    case BIM_GETREF:          return JudoUtil.toVariable((String)url.get("ref"));
    case BIM_GETMETHOD:       return JudoUtil.toVariable(method);
    case BIM_GETINPUTSTREAM:  return new IODevice(instream);
    case BIM_GETOUTPUTSTREAM: return new IODevice(getOutputStream());
    case BIM_GETCONTENTLENGTH:return ConstInt.getInt(getContentLength());
    case BIM_GETTEXTINPUT:
      return new IODevice(new BufferedReader(new InputStreamReader(instream)));
    case BIM_GETTEXTOUTPUT:
      return JudoUtil.toVariable(new PrintWriter(getOutputStream()));

    case BIM_PARSEFORMVARS:
      return getParams("POST".equalsIgnoreCase(method)
                       ? parsePostData(getContentLength(),instream)
                       : parseQueryString((String)url.get("query_string")) );

    case BIM_GETDATEHEADER:
      if (len < 1) break;
      HttpDate hd = new HttpDate();
      hd.parse( (String)requestHeaders.get(params[0].getStringValue()) );
      return new _Date(hd.getTime());

    case BIM_GETALLHEADERS:
      _Array ar = new _Array();
      Iterator keys = requestHeaders.keySet().iterator();
      while (keys.hasNext())
        ar.append(JudoUtil.toVariable((String)keys.next()));
      return ar;

    case BIM_GETCOOKIES:
      ar = new _Array();
      s = (String)requestHeaders.get("cookie");
      if (s == null) return ar;
      StringTokenizer st = new StringTokenizer(s,";");
      while (st.hasMoreTokens()) {
        s = st.nextToken();
        i = s.indexOf('=');
        if ((i > 0) && (i < s.length()-1))
          ar.append(JudoUtil.toVariable(new Cookie(s.substring(0,i),s.substring(i+1))));
      }
      return ar;

    case BIM_ADDCOOKIE:
      if (len < 1) break;
      v = params[0].eval();
      if (v.getObjectValue() instanceof Cookie)
        responseHeaders.add(((Cookie)v.getObjectValue()).getCookieHeader());
      break;

    case BIM_RESPONSEMSG: // for setting
      if (len < 1) break;
      responseMsg = params[0].getStringValue();
      break;

    case BIM_STATUSCODE:  // for setting
      if (len < 1) break;
      responseStatus = (int)params[0].getLongValue();
      break;

    case BIM_GETSERVERNAME:
      return JudoUtil.toVariable(InetAddress.getLocalHost().getHostName());

    case BIM_GETSERVERPORT:
      return ConstInt.getInt(port);

    case BIM_SERVEERROR:
      if (len < 1) responseStatus = 500;
      else responseStatus = (int)params[0].getLongValue();
      serveError();
      break;

    case BIM_SERVEFILE:
      File f;
      s = (len<1) ? null : params[0].getStringValue();
      if (StringUtils.isBlank(s)) s = RT.getCurrentDir().toString();
      String path = (String)url.get("path");
      path = URLDecoder.decode(path);
      if ((StringUtils.isBlank(path)) || path.equals("/")) {
        f = new File(s);
      } else {
        if (path.startsWith("/")) path = path.substring(1);
        f = new File(s,path);
      }
      if (f.isDirectory()) {
        File f1 = new File(f,"index.html");
        if (f1.exists()) {
          f = f1;
        } else {
          f = new File(f,"index.htm");
        }
      }
      if (f.exists()) {
        s = Lib.getFileExt(f.getName()).toLowerCase();
        s = StringUtils.defaultString((String)mimeTypes.get(s), "text/plain");
        if (responseHeaders == null) responseHeaders = new ArrayList();
        responseHeaders.add("Content-Type: " + s);
        responseHeaders.add("Content-Length: " + f.length());
        Lib.copyStream(new FileInputStream(f),getOutputStream());
      } else {
        responseStatus = 404;
        serveError();
      }
      break;

    case BIM_CLOSE:
      socket.close();
      break;

    default: return super.invoke(fxn,params,javaTypes);

    //case _HTTP.BIM_CONNECT:
    //case _HTTP.BIM_LOADCOOKIES:
    //case _HTTP.BIM_SAVECOOKIES:
    }
    return ValueSpecial.UNDEFINED;
  }

  public static UserDefined getParams(Map ht) throws Throwable {
    UserDefined ret = new UserDefined();
    if (ht != null) {
      Iterator keys = ht.keySet().iterator();
      while (keys.hasNext()) {
        String n = (String)keys.next();
        Object v = ht.get(n);
        if (v instanceof String[]) {
          String[] sa = (String[])v;
          if (sa.length == 1) {
            ret.setVariable(n,sa[0],0);
            continue; // while()
          }
        }
        ret.setVariable(n,JudoUtil.toVariable(v),0);
      }
    }
    return ret;
  }
  
  public static HashMap mimeTypes = null;
  static void checkMimeTypes() {
    try {
      if (mimeTypes != null) return;
      mimeTypes = new HashMap();
      mimeTypes.put("html", "text/html");
      mimeTypes.put("htm",  "text/html");
      mimeTypes.put("txt",  "text/plain");
      mimeTypes.put("java", "text/plain");
      mimeTypes.put("h",    "text/plain");
      mimeTypes.put("c",    "text/plain");
      mimeTypes.put("cpp",  "text/plain");
      mimeTypes.put("cxx",  "text/plain");;
      mimeTypes.put("rtx",  "text/richtext");
      mimeTypes.put("rtf",  "text/rtf");
      mimeTypes.put("pdf",  "application/pdf");
      mimeTypes.put("ps",   "application/x-postscript");
      mimeTypes.put("eps",  "application/postscript");
      mimeTypes.put("ai",   "application/postscript");

      mimeTypes.put("gif",  "image/gif");
      mimeTypes.put("jpg",  "image/jpeg");
      mimeTypes.put("jpe",  "image/jpeg");
      mimeTypes.put("jpeg", "image/jpeg");
      mimeTypes.put("tif",  "image/tiff");
      mimeTypes.put("tiff", "image/tiff");
      mimeTypes.put("ras",  "image/x-cmu-raster");
      mimeTypes.put("pbm",  "image/x-portable-bitmap");
      mimeTypes.put("pgm",  "image/x-portable-graymap");
      mimeTypes.put("rgb",  "image/x-rgb");
      mimeTypes.put("xbm",  "image/x-xbitmap");
      mimeTypes.put("xpm",  "image/x-xpitmap");
      mimeTypes.put("xwd",  "image/x-xwindowdump");

      mimeTypes.put("au",   "audio/basic");
      mimeTypes.put("snd",  "audio/basic");
      mimeTypes.put("aif",  "audio/x-aiff");
      mimeTypes.put("aiff", "audio/x-aiff");
      mimeTypes.put("aifc", "audio/x-aiff");
      mimeTypes.put("wav",  "audio/x-wav");
      mimeTypes.put("mpeg", "video/mpeg");
      mimeTypes.put("mpe",  "video/mpeg");
      mimeTypes.put("mpg",  "video/mpeg");
      mimeTypes.put("qt",   "video/quicktime");
      mimeTypes.put("mov",  "video/quicktime");
      mimeTypes.put("avi",  "video/x-msvideo");
      mimeTypes.put("movie","video/x-sgi-movie");
      mimeTypes.put("avx",  "video/x-rad-screenplay");
      mimeTypes.put("mpv2", "video/mpeg2");
      mimeTypes.put("wrl",  "x-world/x-vrml");

      mimeTypes.put("zip",  "application/zip");
      mimeTypes.put("gz",   "application/x-gzip");
      mimeTypes.put("z",    "application/x-compress");
      mimeTypes.put("shar", "application/x-shar");
      mimeTypes.put("tar",  "application/x-tar");

      mimeTypes.put("bin",  "application/octet-stream");
      mimeTypes.put("exe",  "application/octet-stream");
    } catch(Exception e) {}
  }

  public static HashMap getMimeTypeMap() { return mimeTypes; }
  public static void addMimeType(String name, String value) { mimeTypes.put(name,value); }

  // only used by HTTP stuff so better not in Lib.
  public static String getHttpResponseMsg(int code) {
    switch(code) {
    case 100: return "Continue";
    case 101: return "Switching Protocols";
    case 200: return "OK";
    case 201: return "Created";
    case 202: return "Accepted";
    case 203: return "Non-Authoritative Information";
    case 204: return "No Content";
    case 205: return "Reset Content";
    case 206: return "Partial Content";
    case 300: return "Multiple Choices";
    case 301: return "Moved Permanently";
    case 302: return "Moved Temporarily";
    case 303: return "See Other";
    case 304: return "Not Modified";
    case 305: return "Use Proxy";
    case 400: return "Bad Request";
    case 401: return "Unauthorized";
    case 402: return "Payment Required";
    case 403: return "Forbidden";
    case 404: return "Not Found";
    case 405: return "Method Not Allowed";
    case 406: return "Not Acceptable";
    case 407: return "Proxy Authentication Required";
    case 408: return "Request Time-out";
    case 409: return "Conflict";
    case 410: return "Gone";
    case 411: return "Length Required";
    case 412: return "Percondition Failed";
    case 413: return "Request Entity Too Large";
    case 414: return "Request-URI Too Large";
    case 415: return "Unsupported Media Type";
    case 500: return "Internal Server Error";
    case 501: return "Not Implemented";
    case 502: return "Bad Gateway";
    case 503: return "Service Unavailable";
    case 504: return "Gate Timeout";
    case 505: return "HTTP Version not supported";
    }
    if (code < 200) return "Informational";
    if (code < 300) return "Success";
    if (code < 400) return "Redirection";
    if (code < 500) return "Client Error";
    return "Server Error";
  }

  static private String parseName(String s, StringBuffer sb) {
    sb.setLength(0);
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i); 
      switch (c) {
      case '+': sb.append(' '); break;
      case '%':
        try {
          sb.append((char) Integer.parseInt(s.substring(i+1, i+3), 16));
          i += 2;
        } catch (NumberFormatException e) {
          sb.append('%');
        } catch (StringIndexOutOfBoundsException e) {
          String rest = s.substring(i);
          sb.append(rest);
          if (rest.length()==2)
          i++;
        }
        break;
      default: sb.append(c); break;
      }
    }
    return sb.toString();
  }
  static HashMap parseQueryString(String s) {
    if (s == null) return null;
    String valArray[] = null;
    HashMap ht = new HashMap();
    StringBuffer sb = new StringBuffer();
    StringTokenizer st = new StringTokenizer(s,"&");
    while (st.hasMoreTokens()) {
      String pair = (String)st.nextToken();
      String key;
      String val;
      int pos = pair.indexOf('=');
      if ((pos == -1) || (pos == pair.length()-1))
        continue;
      key = parseName(pair.substring(0, pos), sb);
      val = parseName(pair.substring(pos+1, pair.length()), sb);
      if (ht.containsKey(key)) {
        String oldVals[] = (String[]) ht.get(key);
        valArray = new String[oldVals.length + 1];
        System.arraycopy(oldVals, 0, valArray, 0, oldVals.length);
        valArray[oldVals.length] = val;
      } else {
        valArray = new String[1];
        valArray[0] = val;
      }
      ht.put(key, valArray);
    }
    return ht;
  }
  static public HashMap parsePostData(int len, InputStream in) throws IOException {
    if ((len<=0) || (in==null)) return new HashMap();
    byte[] postedBytes = new byte[len];
    in.read (postedBytes, 0, len);
    return parseQueryString( new String(postedBytes,0,len) );
  }

} // end of class HttpService.

