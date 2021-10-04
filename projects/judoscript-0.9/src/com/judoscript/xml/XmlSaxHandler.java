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

import java.io.PrintWriter;
import java.util.*;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.ext.DeclHandler;
import org.xml.sax.ext.LexicalHandler;
import org.apache.commons.lang.StringUtils;
import com.judoscript.*;
import com.judoscript.bio.*;


public final class XmlSaxHandler extends DefaultHandler
                                 implements Consts, LexicalHandler, DeclHandler, DTDHandler
{
  /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    This is the runtime SAX handler, a state machine that supports the spec'ed XML
    document processing. Followin is the spec and how the state machine works.

    The Specification
    ^^^^^^^^^^^^^^^^^
    1. For any tag, including end tag, user can specify a handler.
    2. For unhandled tags, user can specify a default handler.
    3. For the textural content enclosed within a tag, user can specify a handler.
       The opening and end tags for the text can still be assigned a handler.
    4. A default text handler can also be specified for any text.
    5. XML tags may contain other tags and mixture of text and tags.
       When specifying a text content tag handler, user can pick one of these
       options for the embedded tags and text content therein:
       5.a copy the tag and content into the resultant text.
       5.b ignore tags but copy the textural content.
       5.c ignore tags and their textural content.
       5.d treat tags like a regular tag.
       In 5.d, user handler code can access the enclosing (parent) tags
       through a read-only stack object.

    Implementation
    ^^^^^^^^^^^^^^
    An integer depth stack is used for '5.c'.
    A user-accessible stack object is used for '5.d'.

    The State Machine -- spec has been degraded -- 11/7/2001.
    ^^^^^^^^^^^^^^^^^
    A state machine of 4 states are defined. The 4 states are:
       [0]:   the basic state for the tag on the stack top.
       [1]:   for '5.a' tags.
       [2]:   for '5.b' tags.
       [3]:   for '5.c' tags.
       [3.1]: for '5.c' tags.  When embedded tags occur, texts therein are ignored.

    Let <T>   be an opening Tag,
        </T>  be an closing Tag,
        </T>* be the matching closing Tag, and
        <C>   be CData text.
    Let TH  be Tag Handler,
        DTH be Default Tag Handler,
        CH  be CData text Handler, and
        DCH be Default CData text Handler.
    Let [C]  be the embedded tag option of Copying embedded tags ('5.a'),
        [R]  be the embedded tag option of Reporting embedded tags ('5.d'),
        [IT] be the embedded tag option of Ignoring embedded tags ('5.b'), and
        [IA] be the embedded tag option of Ignoring embedded all ('5.c').

    The state transitions are:    

           |from |     | with  | to  |
       line|state|input|handler|state|           action
       ----+-----+-----+-------+-----+------------------------------
         1 |  0  | <C> |       |     | if stack.empty DCH()
           |     |     |       |     | else XmlText.append()
         2 |  0  | <T> |       |     | DTH()
         3 |  0  |</T> |       |     | DTH()
         4 |  0  | <T> |TH     |     | TH()
         5 |  0  |</T> |TH     |     | TH()
         6 |  0  |</T>*|       |     | CH(XmlText)
         7 |  0  | <T> |CH[R]  |     | stack.push(new XmlText)
         8 |  0  | <T> |CH[C]  |  1  | stack.push(new XmlText)
         9 |  0  | <T> |CH[IT] |  2  | stack.push(new XmlText)
        10 |  0  | <T> |CH[IA] |  3  | stack.push(new XmlText)

        11 |  1  | <C> |       |     | XmlText.append()
        12 |  1  | <T> |       |     | XmlText.append()
        13 |  1  |</T> |       |     | XmlText.append()
        14 |  1  |</T>*|       |  0  | XmlText.append(); CH(XmlText)

        15 |  2  | <C> |       |     | XmlText.append()
        16 |  2  | <T> |       |     |
        17 |  2  |</T> |       |     |
        18 |  2  |</T>*|       |  0  | CH(XmlText)

        19 |  3  | <C> |       |     |
        20 |  3  | <T> |       | 3.1 |
        21 |  3  |</T>*|       |  0  | CH(XmlText)
        22 | 3.1 | <C> |       |     |
        23 | 3.1 | <T> |       |     |
        24 | 3.1 |</T> |       |     |
        25 | 3.1 |</T>*|       |  3  | (this matches current+1)

   * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */


  //////////////////////////////////
  // Data
  //

  static final int ST_0 = 0;
  static final int ST_1 = 1;
  static final int ST_2 = 2;
  static final int ST_3 = 3;
  static final int ST_3_1 = 31;

  int state = ST_0;
  Stack stack;        // of XmlText's.
  int depthStack = 0; // records every tag's depth, to identify matching end tags.

  XmlHandler handler;
  private Locator locator = null;

  final XmlText curText() { return (XmlText)stack.peek(); }
  final boolean inTextTag() { return !stack.isEmpty(); }
  final int getCurDepth() {
    return stack.isEmpty() ? 0 : ((XmlText)stack.peek()).depth;
  }

  // C'tor.
  public XmlSaxHandler(XmlHandler hndlr) {
    handler = hndlr;
    stack = new Stack();
  }

  //////////////////////////////////
  // DocumentHandler methods
  //

  public void setDocumentLocator(Locator locator)  {
    this.locator = locator;
  }

  public void startElement(String uri, String local, String raw, Attributes attrs) throws SAXException {
    ++depthStack;
    try {
      String tagName = handler.formTagName(uri,local,raw);
      int opt = handler.getRegisteredTextTag(tagName);

      switch (state) {
      case ST_0:
        Variable var = new XmlTag(handler.doNamespace,uri,local,raw,attrs);
        if (opt == 0) { // lines 2, 4
          handler.event(tagName, XmlHandler.ANY_TAG_SYMBOL, var);
        } else { // lines 7, 8, 9, 10
          handler.event(tagName, null, var); // in case a tag handler is also specified.
          stack.push(new XmlText(depthStack,handler.doNamespace,uri,local,raw,attrs));
          switch (opt) {
          case XmlHandler.COPY_EMBEDDED_TAG:   // line 8
            state = ST_1;
            break;
          case XmlHandler.IGNORE_EMBEDDED_TAG: // line 9
            state = ST_2;
            break;
          case XmlHandler.IGNORE_EMBEDDED_ALL: // line 10
            state = ST_3;
            break;
          case XmlHandler.REPORT_EMBEDDED_TAG: // line 7
            break;
          }
        }
        break;
      case ST_1: // line 12
        textAppend(false, tagName, attrs);
        break;
      //case ST_2: // line 16
      //  break;
      case ST_3: // line 20
        state = ST_3_1;
        break;
      //case ST_3_1: // line 23
      //  break;
      }
    } catch(Throwable e) {
      if (e instanceof SAXException)
        throw (SAXException)e;
      throwSAXException(e);
    }
  }

  public void endElement(String uri, String local, String raw) throws SAXException {
    try {
      String tagName = handler.formTagName(uri,local,raw);
      int curDepth = getCurDepth();
      if (curDepth == depthStack) { // lines 6, 14, 18, 21
        handler.event(XmlHandler.TEXT_PREFIX+tagName, XmlHandler.ANY_TEXT_SYMBOL, (XmlText)stack.pop());
        state = ST_0;
        Variable var = new XmlEndTag(handler.doNamespace, uri, local, raw);
        handler.event("/"+tagName, null, var); // in case a tag handler is also specified.
        return;
      }

      switch(state) {
      case ST_0:  // lines 3, 5
        Variable var = new XmlEndTag(handler.doNamespace, uri, local, raw);
        handler.event("/"+tagName, XmlHandler.ANY_TAG_SYMBOL, var);
        break;
      case ST_1: // line 13
        textAppend(true, tagName, null);
        break;
      //case ST_2: // line 17
      //case ST_3: // does not happen.
      case ST_3_1:
        if (curDepth == (depthStack=1)) // line 25
          state = ST_3;
        break;
      }
    } catch(Throwable e) {
      if (e instanceof SAXException)
        throw (SAXException)e;
      throwSAXException(e);
    } finally {
      --depthStack;
    }
  }

  public void characters(char ch[], int start, int length) throws SAXException {
    try {
      if (stack.isEmpty()) { // line 0
        Variable var = JudoUtil.toVariable(new String(ch,start,length));
        handler.event(XmlHandler.ANY_TEXT_SYMBOL, null, var);
        return;
      }
      switch(state) { case ST_0: // line 1
      case ST_1: // line 11
      case ST_2: // line 15
        curText().append(ch,start,length);
        break;
      //case ST_3:
      //case ST_3_1:
      }
    } catch(Throwable e) {
      if (e instanceof SAXException)
        throw (SAXException)e;
      throwSAXException(e);
    }
  }

  //
  // Other SAX handlers.
  //

  public void startDocument() throws SAXException {
    try {
      handler.event(XML_EVENT_PREFIX+"START_DOC",null,null);
    } catch(Throwable e) { throwSAXException(e); }
  }

  public void endDocument() throws SAXException {
    try {
      handler.event(XML_EVENT_PREFIX+"END_DOC",null,null);
    } catch(Throwable e) { throwSAXException(e); }
  }

  public void startPrefixMapping(String prefix, String uri) throws SAXException {
    UserDefined udo = new UserDefined();
    try {
      udo.setVariable("prefix", JudoUtil.toVariable(prefix), 0);
      udo.setVariable("uri", JudoUtil.toVariable(uri), 0);
      handler.event(XML_EVENT_PREFIX+"START_NS_MAP",null,udo);
    } catch(Throwable e) { throwSAXException(e); }
  }

  public void endPrefixMapping(String prefix) throws SAXException {
    try {
      handler.event(XML_EVENT_PREFIX+"END_NS_MAP",null,JudoUtil.toVariable(prefix));
    } catch(Throwable e) { throwSAXException(e); }
  }

  public void processingInstruction(String instruction, String data) throws SAXException {
    UserDefined udo = new UserDefined();
    try {
      udo.setVariable("instruction", JudoUtil.toVariable(instruction), 0);
      udo.setVariable("data", JudoUtil.toVariable(data), 0);
      handler.event(XML_EVENT_PREFIX+"PI",null,udo);
    } catch(Throwable e) { throwSAXException(e); }
  }

  public void ignorewhitespace(char[] ch, int start, int len) throws SAXException {
    try {
      String s = new String(ch,start,len);
      handler.event(XML_EVENT_PREFIX+"WHITESPACE",null,JudoUtil.toVariable(s));
    } catch(Throwable e) { throwSAXException(e); }
  }

  public void skippedEntity(String name) throws SAXException {
    try {
      handler.event(XML_EVENT_PREFIX+"SKIPPED_ENTITY",null,JudoUtil.toVariable(name));
    } catch(Throwable e) { throwSAXException(e); }
  }

  public void comment(char[] ch, int start, int length) throws SAXException {
    try {
      String s = new String(ch, start, length);
      handler.event(XML_EVENT_PREFIX+"COMMENT",null,JudoUtil.toVariable(s));
    } catch(Throwable e) { throwSAXException(e); }
  }

  public void startCDATA() throws SAXException {
    namePubSysIDEvent(null,null,null,"START_CDATA");
  }

  public void endCDATA() throws SAXException {
    namePubSysIDEvent(null,null,null,"END_CDATA");
  }

  public void startDTD(String name, String publicID, String systemID) throws SAXException {
    namePubSysIDEvent(name,publicID,systemID,"START_DTD");
  }

  public void endDTD() throws SAXException {
    namePubSysIDEvent(null,null,null,"END_DTD");
  }

  public void startEntity(String name) throws SAXException {
    try {
      handler.event(XML_EVENT_PREFIX+"START_ENTITY",null,JudoUtil.toVariable(name));
    } catch(Throwable e) { throwSAXException(e); }
  }

  public void endEntity(String name) throws SAXException {
    try {
      handler.event(XML_EVENT_PREFIX+"END_ENTITY",null,JudoUtil.toVariable(name));
    } catch(Throwable e) { throwSAXException(e); }
  }

  public void elementDecl(String name, String model) throws SAXException {
    UserDefined udo = new UserDefined();
    try {
      udo.setVariable("name", JudoUtil.toVariable(name), 0);
      udo.setVariable("model", JudoUtil.toVariable(model), 0);
      handler.event(XML_EVENT_PREFIX+"ELEMENT_DECL",null,udo);
    } catch(Throwable e) { throwSAXException(e); }
  }

  public void attributeDecl(String eName, String aName, String type, String defValue, String value)
                           throws SAXException
  {
    UserDefined udo = new UserDefined();
    try {
      udo.setVariable("element", JudoUtil.toVariable(eName), 0);
      udo.setVariable("name", JudoUtil.toVariable(aName), 0);
      udo.setVariable("type", JudoUtil.toVariable(type), 0);
      udo.setVariable("default", JudoUtil.toVariable(defValue), 0);
      udo.setVariable("value", JudoUtil.toVariable(value), 0);
      handler.event(XML_EVENT_PREFIX+"ATTR_DECL",null,udo);
    } catch(Throwable e) { throwSAXException(e); }
  }

  public void externalEntityDecl(String name, String publicID, String systemID) throws SAXException
  {
    namePubSysIDEvent(name,publicID,systemID,"EXT_ENTITY_DECL");
  }

  public void internalEntityDecl(String name, String value) throws SAXException {
    UserDefined udo = new UserDefined();
    try {
      udo.setVariable("name", JudoUtil.toVariable(name), 0);
      udo.setVariable("value", JudoUtil.toVariable(value), 0);
      handler.event(XML_EVENT_PREFIX+"ENTITY_DECL",null,udo);
    } catch(Throwable e) { throwSAXException(e); }
  }

  public void notationDecl(String name, String publicID, String systemID) throws SAXException
  {
    namePubSysIDEvent(name,publicID,systemID,"NOTATION_DECL");
  }

  public void unparsedEntityDecl(String name, String publicID, String systemID, String notationName)
                                throws SAXException
  {
    UserDefined udo = namePubSysIDEvent(name,publicID,systemID,"UNPARSED_ENTITY_DECL");
    if (StringUtils.isNotBlank(notationName)) {
      try { udo.setVariable("notationName",JudoUtil.toVariable(notationName), 0); }
      catch(Throwable e) { throwSAXException(e); }
    }
  }

  private final UserDefined namePubSysIDEvent(String name, String pubID, String sysID, String event)
                                             throws SAXException
  {
    UserDefined udo = new UserDefined();
    try {
      if (StringUtils.isNotBlank(name))
        udo.setVariable("name", JudoUtil.toVariable(name), 0);
      if (StringUtils.isNotBlank(pubID))
        udo.setVariable("publicID", JudoUtil.toVariable(pubID), 0);
      if (StringUtils.isNotBlank(sysID))
        udo.setVariable("systemID", JudoUtil.toVariable(sysID), 0);
      handler.event(XML_EVENT_PREFIX+event,null,udo);
      return udo;
    } catch(Throwable e) { throwSAXException(e); }
    return null;
  }

  //
  // Helpers
  //

  protected void writeTag(PrintWriter pw, boolean start, String raw, String attrs) {
    pw.write('<');
    if (!start) pw.write('/');
    pw.write(raw);
    if (StringUtils.isNotBlank(attrs)) { pw.write(' '); pw.write(attrs); }
    pw.write('>');
  }

  protected void writeTag(PrintWriter pw, boolean end, String raw, Attributes attrs) {
    pw.write('<');
    if (end) pw.write('/');
    pw.write(raw);
    int len = (attrs==null) ? 0 : attrs.getLength();
    for (int i=0; i<len; i++) {
      pw.write(' ');
      pw.write(attrs.getQName(i));
      pw.write('=');
      String x = attrs.getValue(i);
      char quot = (x.indexOf('"') >= 0) ? '\'' : '"';
      pw.write(quot);
      pw.write(x);
      pw.write(quot);
    }
    pw.write('>');
  }

  void textAppend(boolean endTag, String tagName, Attributes attrs) {
    StringBuffer buf = curText().buf;
    buf.append('<');
    if (endTag) buf.append('/');
    buf.append(tagName);
    int len = (attrs==null) ? 0 : attrs.getLength();
    for (int i=0; i<len; i++) {
      buf.append(' ');
      buf.append(attrs.getQName(i));
      buf.append('=');
      String x = attrs.getValue(i);
      char quot = (x.indexOf('"') >= 0) ? '\'' : '"';
      buf.append(quot);
      buf.append(x);
      buf.append(quot);
    }
    buf.append('>');
  }

  //
  // ErrorHandler
  //

  public void warning(SAXParseException ex) throws SAXException {
    try {
      handler.event(XML_EVENT_PREFIX+"WARNING",null,JudoUtil.toVariable(ex));
    } catch(Throwable e) { throwSAXException(e); }
  }
  public void error(SAXParseException ex) throws SAXException {
    try {
      handler.event(XML_EVENT_PREFIX+"ERROR",null,JudoUtil.toVariable(ex));
    } catch(Throwable e) { throwSAXException(e); }
  }
  public void fatalError(SAXParseException ex) throws SAXException {
    throw new SAXException("SAX Fatal Error: "+getLocationString(ex)+": "+ex.getMessage());
  }

  // Returns a string of the location
  public static final String getLocationString(SAXParseException ex) {
    StringBuffer str = new StringBuffer();
    String systemId = ex.getSystemId();
    if (systemId != null) {
      int index = systemId.lastIndexOf('/');
      if (index >= 0) systemId = systemId.substring(index+1);
      str.append(systemId);
    }
    str.append(':');
    str.append(ex.getLineNumber());
    if (ex.getColumnNumber() > 0) {
      str.append(':');
      str.append(ex.getColumnNumber());
    }
    return str.toString();
  }

  public static final void throwSAXException(Throwable t) throws SAXException {
    if (t instanceof SAXException)
      throw (SAXException)t;

    if (t instanceof ExceptionControl) {
      ExceptionControl exCtrl = (ExceptionControl)t;
      if (exCtrl.isResume() || exCtrl.isContinue())
        return;
    }

    XmlHandler.throwSAXException(t);
  }

} // end of class XmlSaxHandler.
