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

import java.io.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import org.apache.commons.lang.StringUtils;
import org.apache.xpath.XPathAPI; // Xalan
import org.w3c.dom.Node;
import org.w3c.dom.Document;
import org.w3c.dom.traversal.NodeIterator;
import org.xml.sax.InputSource;
import com.judoscript.util.*;


public class StmtXslt extends StmtBase
{
  public static final int MODE_FILE  = 0;
  public static final int MODE_XPATH = 1;
  public static final int MODE_COPY  = 2;

  public Expr transform;
  public int mode = MODE_FILE;
  public Expr src;
  public Expr dest = null;
  public Expr xslSysID = null;
  public Expr inSysID  = null;
  public Expr outSysID = null;
  public AssociateList outputProperties;
  public AssociateList parameters;

  public StmtXslt(int line) { super(line); }

  public void addOutputProperties(AssociateList al) {
    if (outputProperties == null) outputProperties = al;
    else outputProperties.append(al);
  }

  public void addParameters(AssociateList al) {
    if (parameters == null) parameters = al;
    else parameters.append(al);
  }

  public void exec() throws Throwable {
    String x;

    // get the input if not xpath (query)
    Source input = null;
    if (mode != MODE_XPATH)
      input = getSource(src.eval().getObjectValue(), inSysID);

    // get the output
    Result output = getResult(outSysID);

    // get the transform
    TransformerFactory tFactory = TransformerFactory.newInstance();
    Transformer xfmr = null;
    String text = null;
    switch(mode) {
    default:
      xfmr = tFactory.newTransformer(getSource(transform.eval().getObjectValue(),xslSysID));
      break;
    case MODE_XPATH:
      text = transform.getStringValue();
    case MODE_COPY:
      xfmr = tFactory.newTransformer();
      break;
    }

    // set the output properties and/or parameters.
    int i, len = (outputProperties==null) ? 0 : outputProperties.size();
    for (i=0; i<len; ++i)
      xfmr.setOutputProperty((String)outputProperties.getKeyAt(i),
                             ((Expr)outputProperties.getValueAt(i)).getStringValue());
    len = (parameters==null) ? 0 : parameters.size();
    for (i=0; i<len; ++i) {
      xfmr.setParameter((String)parameters.getKeyAt(i),
                        ((Expr)parameters.getValueAt(i)).eval().getObjectValue());
    }

    // do the transformation
    if (mode == MODE_XPATH) {
      Document doc = getDocument(src.eval().getObjectValue());
      NodeIterator nodeIter = XPathAPIWrapper.selectNodeIterator(doc, text);
      Node cur;
      while ((cur = nodeIter.nextNode()) != null)
        xfmr.transform(new DOMSource(cur), output);
    } else {
      xfmr.transform(input,output);
    }

    // Close the stream or return the DOM node as $_.
    if (output instanceof StreamResult) {
      StreamResult sr = (StreamResult)output;
      try {
        OutputStream os = sr.getOutputStream();
//        if (os != null && os != RT.getOutStream() && os != RT.getErrStream())
        if (os != null)
          os.close();
      } catch(Exception e1) {}
      try {
        Writer w = sr.getWriter();
//        if (w != null && w != RT.getOut() && w != RT.getErr())
        if (w != null)
          w.close();
      } catch(Exception e2) {}
    } else if (output instanceof DOMResult) {
      RT.setVariable("$_", JudoUtil.toVariable(((DOMResult)output).getNode()), 0);
    }
  }

  Source getSource(Object o, Expr sysid) throws Throwable {
    Source ret = null;
    if (o instanceof String)           ret = new StreamSource((String)o);
    else if (o instanceof Reader)      ret = new StreamSource((Reader)o);
    else if (o instanceof InputStream) ret = new StreamSource((InputStream)o);
    else if (o instanceof Node)        ret = new DOMSource((Node)o);
    else if (o instanceof File)        ret = new StreamSource((File)o);
    else if (o instanceof Source)      ret = (Source)o;
    else ExceptionRuntime.rte(RTERR_ILLEGAL_ARGUMENTS,
                              "Invalid XSLT source of class " + o.getClass().getName());
    if (sysid != null) {
      String x = sysid.getStringValue();
      if (StringUtils.isNotBlank(x)) ret.setSystemId(x);
    }
    return ret;
  }

  Result getResult(Expr sysid) throws Throwable {
    if (dest==null) return new DOMResult();
    Object o = dest.eval().getObjectValue();
    Result ret = null;
    if (o instanceof String)            ret = new StreamResult((String)o);
    else if (o instanceof OutputStream) ret = new StreamResult((OutputStream)o);
    else if (o instanceof Writer)       ret = new StreamResult((Writer)o);
    else if (o instanceof File)         ret = new StreamResult((File)o);
    else if (o instanceof Node)         ret = new DOMResult((Node)o);
    else if (o instanceof Result)       ret = (Result)o;
    else ExceptionRuntime.rte(RTERR_ILLEGAL_ARGUMENTS,
                              "Invalid XSLT result of class " + o.getClass().getName());
    if (sysid != null) {
      String x = sysid.getStringValue();
      if (StringUtils.isNotBlank(x)) ret.setSystemId(x);
    }
    return ret;
  }

  Document getDocument(Object o) throws Exception {
    if (o instanceof Document) return (Document)o;
    InputSource in = null;
    if (o instanceof String)           in = new InputSource((String)o);
    else if (o instanceof InputStream) in = new InputSource((InputStream)o);
    else if (o instanceof Reader)      in = new InputSource((Reader)o);
    else ExceptionRuntime.rte(RTERR_ILLEGAL_ARGUMENTS,
                              "Invalid XSLT source of class " + o.getClass().getName());
    return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
  }

  public void dump(XMLWriter out) {
    out.simpleSingleTagLn("StmtXslt");
    // TODO: dump().
  }

  //
  // To shield Xalan.
  //
  private static class XPathAPIWrapper
  {
    static NodeIterator selectNodeIterator(Document doc, String text) throws Exception {
      return XPathAPI.selectNodeIterator(doc, text);
    }
  }

} // end of class StmtXslt.
