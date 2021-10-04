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

import java.util.StringTokenizer;
import org.apache.commons.lang.StringUtils;
import com.judoscript.util.XMLWriter;
import com.judoscript.util.AssociateList;
import com.judoscript.util.SendMail;


public final class StmtSendMail extends StmtBase
{
  public final static int FLD_FROM     = SendMail.FLD_FROM;
  public final static int FLD_TO       = SendMail.FLD_TO;
  public final static int FLD_CC       = SendMail.FLD_CC;
  public final static int FLD_BCC      = SendMail.FLD_BCC;
  public final static int FLD_SUBJECT  = SendMail.FLD_SUBJECT;
  public final static int FLD_ATTACH   = SendMail.FLD_ATTACH;
  public final static int FLD_BODY     = SendMail.FLD_BODY;
  public final static int FLD_HTMLBODY = SendMail.FLD_HTMLBODY;
  Expr[] exprs = new Expr[8];
  Expr[] csets = new Expr[8];
  Expr textCharset = null;
  Expr htmlCharset = null;
  AssociateList headers = null;

  public StmtSendMail(int lineNum) { super(lineNum); }

  public void setExpr(int type, Expr expr, Expr extra) throws Exception {
    if (exprs[type] != null)
      throw new Exception("The "+getFieldName(type)+" clause is already specified.");
    exprs[type] = expr;
    csets[type] = extra;
  }

  public void setHeaders(AssociateList headers) {
    this.headers = headers;
  }

  String getFieldName(int type) {
    switch(type) {
    case FLD_FROM:    return "from";
    case FLD_TO:      return "to";
    case FLD_CC:      return "cc";
    case FLD_BCC:     return "bcc";
    case FLD_SUBJECT: return "subject";
    case FLD_ATTACH:  return "attach";
    case FLD_BODY:    return "body";
    case FLD_HTMLBODY:return "htmlBody";
    }
    return null;
  }

  public void exec() throws Throwable {
    if (RT.getSendMail() == null)
      ExceptionRuntime.rte(RTERR_MAILSERVER_NOT_CONNECTED, "Mail server is not connected yet.");
    if (exprs[FLD_FROM] == null)
      ExceptionRuntime.rte(RTERR_MAIL_MISSING_FIELD, "Missing from clause.");
    Object   from     = exprs[FLD_FROM].eval().getObjectValue();
    Object[] tos      = getObjects(FLD_TO);
    Object[] ccs      = getObjects(FLD_CC);
    Object[] bccs     = getObjects(FLD_BCC);
    String[] attaches = getStrings(FLD_ATTACH);
    String   subject  = (exprs[FLD_SUBJECT]==null) ? "" : exprs[FLD_SUBJECT].getStringValue();
    String   body     = (exprs[FLD_BODY]==null) ? null : exprs[FLD_BODY].getStringValue();
    String   htmlBody = (exprs[FLD_HTMLBODY]==null) ? null : exprs[FLD_HTMLBODY].getStringValue();
    String[] _csets = new String[8];
    for (int i=0; i<csets.length; ++i) {
      _csets[i] = (csets[i]==null) ? null : getCharset(csets[i]);
    }
    RT.getSendMail().send(from, tos, ccs, bccs, subject, body, htmlBody, attaches, _csets);
  }

  String getCharset(Expr expr) throws Throwable {
    String ret = null;
    if (expr != null) {
      ret = expr.getStringValue();
      if (StringUtils.isBlank(ret)) return null;
      if (ret.startsWith("charset=")) ret = ret.substring(8);
    }
    return (ret==null) ? RT.getCharset() : ret;
  }

  private String[] getStrings(int type) throws Throwable {
    Object[] oa = getObjects(type);
    int len = (oa==null) ? 0 : oa.length;
    if (len <= 0) return null;
    String[] sa = new String[len];
    System.arraycopy(oa, 0, sa, 0, len);
    return sa;
  }

  private Object[] getObjects(int type) throws Throwable {
    if (exprs[type] == null) return null;
    Object o = exprs[type].eval();
    if (o instanceof ConstString) {
      StringTokenizer st = new StringTokenizer(o.toString(), ",;");
      String[] sa = new String[st.countTokens()];
      int i=0;
      while (st.hasMoreTokens())
        sa[i++] = st.nextToken().trim();
      return sa;
    } else { // must be InternetAddress but I don't want to drag in the class.
      return new Object[]{ o };
    }
  }

  public void dump(XMLWriter out) {
    out.simpleTag("StmtSendMail");
    dumpField(out, FLD_FROM);
    dumpField(out, FLD_TO);
    dumpField(out, FLD_CC);
    dumpField(out, FLD_BCC);
    dumpField(out, FLD_SUBJECT);
    dumpField(out, FLD_ATTACH);
    dumpField(out, FLD_BODY);
    dumpField(out, FLD_HTMLBODY);
    out.endTag();
  }

  void dumpField(XMLWriter out, int type) {
    if (exprs[type] == null) return;
    out.simpleTag(getFieldName(type));
    exprs[type].dump(out);
    out.endTag();
  }

} // end of class StmtSendMail.
