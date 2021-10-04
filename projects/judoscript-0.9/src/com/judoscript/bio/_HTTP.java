/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 03-17-2002  JH   Initial open source release.
 *                  For a HTTP client connection.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript.bio;

import java.io.*;
import java.net.*;
import java.util.*;
import com.judoscript.*;
import com.judoscript.bio.JavaObject;
import com.judoscript.util.*;


/**
 * A http client connection
 */
public final class _HTTP extends ObjectInstance
{
  URL     url;
  boolean connected = false;
  List cookies = new ArrayList();
  HttpURLConnection urlc;
  String  method;

  public _HTTP(String _url, String method) throws IOException {
    super();
    url = new URL(_url);
    urlc = (HttpURLConnection)url.openConnection();
    this.method = (method==null) ? "GET" : method;
    urlc.setRequestMethod(this.method);
    urlc.setInstanceFollowRedirects(true);
  }

  public String getTypeName() { return "HTTP"; }

  public void close() { urlc = null; }

  void checkConnect() throws IOException {
    if (connected) return;

    int len = cookies.size();
    if (len > 0) {
      StringBuffer sb = new StringBuffer();
      for (int i=0; i<len; ++i) {
        if (i>0) sb.append(';');
        Cookie c = (Cookie)cookies.get(i);
        sb.append(c.getName());
        sb.append('=');
        sb.append(c.getValue());
      }
      urlc.setRequestProperty("cookie", sb.toString());
    }

    urlc.connect();
    connected = true;
  }

  public Variable resolveVariable(String name) throws Throwable {
    checkConnect();
    return JudoUtil.toVariable(urlc.getHeaderField(name));
  }

  public Variable setVariable(String name, Variable val, int type) throws Throwable {
    if (!connected) {
      long time = JudoUtil.getTime(val);
      if (time > 0) {
        urlc.setRequestProperty(name,new HttpDate(time).toString());
      } else {
        urlc.setRequestProperty(name,val.getStringValue());
      }
    }
    return val;
  }

  String getDomain() {
    String s = url.getHost();
    int i = url.getPort();
    if (i != 80) s += ":" + i;
    return s.toLowerCase();
  }

  void checkAddCookie(Cookie cookie) {
    if (cookie.getMaxAge() <= 0) return;
    if (!getDomain().equalsIgnoreCase(cookie.getDomain())) return;
    String s = cookie.getPath();
    if ((s!=null) && !url.getPath().startsWith(s)) return;
    cookies.add(cookie);
  }

  public Variable invoke(String fxn, Expr[] params, int[] javaTypes) throws Throwable
  {
    int i;
    Variable v;
    String s;
    int len = (params==null) ? 0 : params.length;
    switch(getMethodOrdinal(fxn)) {
    case BIM_GETURL:          return JudoUtil.toVariable(url.toString());
    case BIM_GETHOST:         return JudoUtil.toVariable(url.getHost());
    case BIM_GETPORT:         return ConstInt.getInt(url.getPort());
    case BIM_GETDOMAIN:       return JudoUtil.toVariable(getDomain());
    case BIM_GETPATH:         return JudoUtil.toVariable(url.getPath());
    case BIM_GETFILENAME:     return JudoUtil.toVariable(url.getFile());
    case BIM_GETQUERY:        return JudoUtil.toVariable(url.getQuery());
    case BIM_GETREF:          return JudoUtil.toVariable(url.getRef());
    case BIM_GETMETHOD:       return JudoUtil.toVariable(method);
    case BIM_RESPONSEMSG:     checkConnect(); return JudoUtil.toVariable(urlc.getResponseMessage());
    case BIM_STATUSCODE:      checkConnect(); return ConstInt.getInt(urlc.getResponseCode());
    case BIM_GETINPUTSTREAM:  checkConnect(); return new IODevice(urlc.getInputStream());
    case BIM_GETOUTPUTSTREAM: checkConnect(); return new IODevice(urlc.getOutputStream());
    case BIM_GETCONTENTLENGTH:checkConnect(); return ConstInt.getInt(urlc.getContentLength());
    case BIM_GETCONTENTTYPE:  checkConnect(); return JudoUtil.toVariable(urlc.getContentType());

    case BIM_GETCONTENTBYTES:
      checkConnect();
      int limit = (len > 0) ? limit = (int)params[0].getLongValue() : -1;
      len = urlc.getContentLength();
      InputStream is = urlc.getInputStream();
      byte[] data;
      if (len > 0) {
        if (limit > 0 && len > limit) len = limit;
        data = new byte[len];
        is.read(data);
      } else {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        data = new byte[2048];
        while (true) {
          len = is.read(data);
          if (len <= 0) break;
          baos.write(data,0,len);
          if (limit > 0 && baos.size() >= limit) break;
        }
        data = baos.toByteArray();
      }
      return JudoUtil.toVariable(data);

    case BIM_GETTEXTINPUT:
      checkConnect();
      return new IODevice(new BufferedReader(new InputStreamReader(urlc.getInputStream())));

    case BIM_GETTEXTOUTPUT:
      checkConnect();
      return JudoUtil.toVariable(new PrintWriter(urlc.getOutputStream()));

    case BIM_GETDATEHEADER:
      if (len < 1) break;
      checkConnect();
      long time = urlc.getHeaderFieldDate(params[0].getStringValue(),0);
      if (time == 0) break;
      return new _Date(time);

    case BIM_GETALLHEADERS:
      checkConnect();
      _Array ar = new _Array();
      for (i=1; ; ++i) {
        String k = urlc.getHeaderFieldKey(i);
        if (k == null) break;
        ar.append(JudoUtil.toVariable(k));
      }
      return ar;

    case BIM_CONNECT:
      checkConnect();
      break;

    case BIM_ADDCOOKIE:
      if (connected || (len < 1)) break;
      if (len == 2) { // name = value pair
        cookies.add(new Cookie(params[0].getStringValue(), params[1].getStringValue()));
      } else {
        v = params[0].eval();
        if ((v instanceof JavaObject) && (((JavaObject)v).object instanceof Cookie)) {
          cookies.add(((JavaObject)v).object);
        } else 
          ExceptionRuntime.badParams("addCookie","cookies");
      }
      return ValueSpecial.UNDEFINED;

    case BIM_LOADCOOKIES:
      if (connected || (len < 1)) break;
      CookieJar cj = new CookieJar((len>0) ? params[0].getStringValue() : "cookies.txt");
      for (i=cj.size()-1; i>=0; --i)
        checkAddCookie(cj.getAt(i));
      break;

    case BIM_GETCOOKIES:
      checkConnect();
      ar = new _Array();
      for (i=1; ; ++i) {
        String k = urlc.getHeaderFieldKey(i);
        if (k == null) break;
        k = k.toLowerCase();
        if (k.startsWith("set-cookie")) {
          Cookie cookie = Cookie.parseSetCookie(urlc.getHeaderField(i));
          if (cookie.getDomain() == null)
            cookie.setDomain(getDomain());
          ar.append(JudoUtil.toVariable(cookie));
        }
      }
      return ar;

    case BIM_SAVECOOKIES:
      checkConnect();
      s = (len>0) ? params[0].getStringValue() : "cookies.txt";
      cj = new CookieJar(s);
      for (i=1; ; ++i) {
        String k = urlc.getHeaderFieldKey(i);
        if (k == null) break;
        k = k.toLowerCase();
        if (k.startsWith("set-cookie")) {
          Cookie cookie = Cookie.parseSetCookie(urlc.getHeaderField(i));
          if (cookie != null) cj.put(cookie);
        }
      }
      cj.save(s);
      break;

    default: return super.invoke(fxn,params,javaTypes);
    }

    return ValueSpecial.UNDEFINED;
  }

} // end of class _HTTP.

