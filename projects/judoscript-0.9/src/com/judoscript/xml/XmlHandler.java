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


package com.judoscript.xml;

import java.util.*;
import java.util.zip.*;
import java.io.*;
import javax.xml.parsers.*;
import org.xml.sax.*;
import org.apache.commons.lang.StringUtils;
import com.judoscript.bio.ZipArchive;
import com.judoscript.util.*;
import com.judoscript.*;


public final class XmlHandler extends EventDriven
{
  public static final int COPY_EMBEDDED_TAG   = 1;
  public static final int IGNORE_EMBEDDED_TAG = 2;
  public static final int IGNORE_EMBEDDED_ALL = 3;
  public static final int REPORT_EMBEDDED_TAG = 4;

  public boolean doNamespace = false;
  public boolean doValidate = false;
  public boolean doSchema = false;
  public Expr systemID;
  HashMap textTags = new HashMap(); // tag-name => embedded-option
  Expr src;
  Expr root = null;
  Expr encoding = null;
  public HashMap xmlns = null;
//  AssociateList inits;

  public XmlHandler(int lineNo, Expr src, AssociateList inits) {
    super(lineNo);
    this.src   = src;
//    this.inits = inits;
  }

  public void setRoot(Expr r) { root = r; }
  public void setEncoding(Expr e) { encoding = e; }
  public void registerTextTag(String uri, String local, int opt) {
    textTags.put(Lib.formTag(uri,local), new Integer(opt));
  }

  public void registerTextTag(String raw, int opt) {
    textTags.put(raw, new Integer(opt));
  }

  public int getRegisteredTextTag(String uri, String local) {
    return getRegisteredTextTag(Lib.formTag(uri,local));
  }

  public int getRegisteredTextTag(String tagName) {
    Integer I = (Integer)textTags.get(tagName);
    return (I==null) ? 0 : I.intValue();
  }

  public boolean isRegisteredTextTag(String tagName) {
    return (null != textTags.get(tagName));
  }

  public void start() throws Throwable {
    XmlSaxHandler sh = new XmlSaxHandler(this);
    RT.setLocalVariable(PARENT_NAME, JudoUtil.toVariable(sh.stack), 0);
    SAXParserFactory spf = SAXParserFactory.newInstance();
    spf.setValidating(doValidate);
    spf.setNamespaceAware(doNamespace);
    XMLReader xmlrdr = spf.newSAXParser().getXMLReader();
    // XMLReader xmlrdr = new org.apache.xerces.parsers.SAXParser();
    //                                   ^-------- !!!

    xmlrdr.setProperty("http://xml.org/sax/properties/declaration-handler", sh);
    xmlrdr.setProperty("http://xml.org/sax/properties/lexical-handler", sh);
    xmlrdr.setContentHandler(sh);
    xmlrdr.setDTDHandler(sh);
    xmlrdr.setErrorHandler(sh);

    Object o;
    if (root == null) {
      o = src.getObjectValue();
    } else {
      o = root.eval();
      try {
        if (o instanceof ZipArchive) {
          o = ((ZipArchive)o).getBufferedReader(src.getStringValue(),
                encoding==null ? null : encoding.getStringValue());
        } else {
          ZipFile zf = new ZipFile(((Variable)o).getStringValue());
          o = zf.getInputStream(zf.getEntry(src.getStringValue()));
        }
      } catch(NullPointerException npe) { return; } // entry does not exist.
    }

    InputSource in;
    if (o instanceof Reader)
      in = new InputSource((Reader)o);
    else if (o instanceof InputStream)
      in = new InputSource((InputStream)o);
    else
      in = new InputSource(o.toString());
    if (systemID != null) {
      String x = systemID.getStringValue();
      if (StringUtils.isNotBlank(x))
        in.setSystemId(x);
    }

    try {
      xmlrdr.parse(in);
    } catch(SAXException saxEx) {
      if (saxEx instanceof SAXControlException) {
        Throwable ex = ((SAXControlException)saxEx).getThrowable();

        if (ex instanceof ExceptionControl) {
          ExceptionControl exCtrl = (ExceptionControl)ex;
          if (exCtrl.isBreak() || exCtrl.isReturn())
            return; // break, quietly.
        }

        throw ex;   // can be ExceptionExit.
      } else {
        ExceptionRuntime.rte(RTERR_XML_PARSING_ERROR, null, saxEx);
      }
    }
  }

  /**
   *@param name [0] is uri, [1] is local, [2] is raw;
   *            [0] and [1] may be null, even if using namespace.
   *            [2] never null; if namespace is used, it may have ':'.
   */
  public void addTagHandler(int lineNo, boolean endTag, String[] name, Stmt[] stmts) {
    String tag = formTagName(name[0],name[1],name[2],true);
    if (endTag) tag = "/" + tag;
    addHandler(lineNo, tag, stmts);
  }

  /**
   *@param name [0] is uri, [1] is local, [2] is raw;
   *            [0] and [1] may be null, even if using namespace.
   *            [2] never null; if namespace is used, it may have ':'.
   */
  public void addTextHandler(int lineNo, int embedOption, String[] name, Stmt[] stmts) {
    String tagName = formTagName(name[0],name[1],name[2],true);
    registerTextTag(tagName,embedOption);
    addHandler(lineNo, TEXT_PREFIX + tagName, stmts);
  }

  public String formTagName(String uri, String local, String raw) {
    return formTagName(uri,local,raw,false);
  }

  String formTagName(String uri, String local, String raw, boolean mapPrefix) {
    if (!doNamespace)
      return raw;

    // uri and local may be null, even if namespace is being used.
    // they may happen like this: <news:article xmlns:news="http://whatever">
    // the jaxp parse seem to process them in that order, so "names:article"
    // fires an error event.
    //
    // I think xmlns;news should take precedence, so that prefix "news" is
    // valid even for this tag. Nevertheless, cope with the current code by
    // doing this mapping ourselves, using out HashMap xmlns.
    // This may not work if a different prefix is used in the document than
    // in the do...as xml {} statement.

    if (uri==null || local==null) {
      // first, make sure we have our xmlns mapping.
      if (xmlns == null) return raw;
      int idx = raw.indexOf(':');
      if (idx < 0) {
        // No prefix -- use default namespace, if available
        return Lib.formTag((String)xmlns.get(DEFAULT_NS_SYMBOL),raw);
      }

      // separate prefix and local in raw
      uri = raw.substring(0,idx);
      local = raw.substring(idx+1);

      // attempt to resolve the prefix using our own xmlns.
      return Lib.formTag((String)xmlns.get(uri),local);
    }
    // uri is not null, it's a valid namespace name.
    if (mapPrefix) {
      try { uri = (String)xmlns.get(uri); } catch(Exception e) {}
    }
    return Lib.formTag(uri,local);
  }


  public static void throwSAXException(Throwable t) throws SAXException {
    throw new SAXControlException(t);
  }

  private static class SAXControlException extends SAXException
  {
    Throwable t;

    public SAXControlException(Throwable t) {
      super("");
      this.t = t;
    }

    public Throwable getThrowable() { return t; }
  }

} // end of class XmlHandler.

