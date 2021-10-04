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


package com.judoscript;

import java.io.InputStream;
import java.io.Reader;
import java.util.HashMap;
import javax.xml.parsers.*;
import org.xml.sax.*;
import org.apache.commons.lang.StringUtils;
import com.judoscript.bio.DOMDoc;
import com.judoscript.util.XMLWriter;


public class ExprDom extends ExprSingleBase
{
  boolean doNamespace;
  boolean doValidate;
  boolean doSchema;
  boolean ignoreWhitespace;
  boolean ignoreComment;
  HashMap xmlns;
  Expr    systemID;

  public ExprDom(Expr src, Expr sysid, HashMap xmlns, boolean ns, boolean validate,
                 boolean schema, boolean ignoreWS, boolean ignoreCmt)
  {
    super(src);
    this.systemID = sysid;
    this.xmlns  = xmlns;
    doNamespace = ns;
    doValidate  = validate;
    doSchema    = schema;
    ignoreWhitespace = ignoreWS;
    ignoreComment = ignoreCmt;
  }

  public int getType() { return TYPE_JAVA; }

  public Variable eval() throws Throwable {
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setNamespaceAware(doNamespace);
    dbf.setValidating(doValidate);
    dbf.setIgnoringComments(ignoreComment);
    dbf.setIgnoringElementContentWhitespace(ignoreWhitespace);
    dbf.setCoalescing(true);
    dbf.setExpandEntityReferences(true);

    try { 
      DocumentBuilder db = dbf.newDocumentBuilder();
      //db.setErrorHandler(???);
      Object o = expr.eval().getObjectValue();
      InputSource in;
      if (o instanceof InputStream)      in = new InputSource((InputStream)o);
      else if (o instanceof Reader)      in = new InputSource((Reader)o);
      else if (o instanceof InputSource) in = (InputSource)o;
      else                               in = new InputSource(o.toString());
      if (systemID != null) {
        String x = systemID.getStringValue();
        if (StringUtils.isNotBlank(x)) in.setSystemId(x);
      }
      return new DOMDoc(db.parse(in));
    } catch(Exception e) {
      ExceptionRuntime.rte(RTERR_XML_PARSING_ERROR,null,e);
    }
    return null;
  }

  public void dump(XMLWriter out) {
    // TODO: dump().
  }

} // end of class ExprDom.
