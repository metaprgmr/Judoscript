/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 03-17-2002  JH   Initial open source release.
 * 06-23-2002  JH   <!--> can be handled differently from <!>;
 *                  <!> handles <!--> if no handler specified for <!-->,
 *                  for backward compatibility.
 * 07-17-2002  JH   Added "setName" user method to the $_ object.
 * 08-29-2002  JH   Added support for "with" and "extends" clauses.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript;

import java.io.*;
import java.util.*;
import java.util.zip.*;
import org.apache.commons.lang.StringUtils;
import com.judoscript.util.*;
import com.judoscript.bio.*;


/**
 *  If it extends a parent handler, parent's BEGIN/FINISH events are ignored.
 *  A tag handler can not call its parent's handler.
 *  Only if a tag is not handled by any of the ancestors, then it will be
 *  handled by the first any-tag handler.
 */
public final class StmtHtml extends EventDriven
{
  boolean hasTextHandler       = false;
  boolean hasDefaultTagHandler = false;
  boolean hasSpecialTagHandler = false;
  Expr src;
  Expr root = null; // must be zip archive.
  Expr encoding = null;
  String  useHandlerName = null;
  String  extendsHandlerName = null;
  boolean isJSP = false;
  public boolean caseSens = false;

  public StmtHtml(int lineNo, Expr src) {
    super(lineNo);
    this.src = src;
  }

  public StmtHtml(int lineNo, boolean caseSens) {
    super(lineNo);
    this.src = null;
    this.caseSens = caseSens;
  }

  public void setIsJSP() { isJSP = true; caseSens = true; }
  public void setCaseSensitive(boolean set) { caseSens = set; }
  public void setRoot(Expr r) { root = r; }
  public void setEncoding(Expr e) { encoding = e; }
  public void setHandler(String name) { useHandlerName = name; }
  public void setExtends(String name) { extendsHandlerName = name; }

  public void addTagHandler(int lineNo, boolean endTag, String name, Stmt[] stmts) {
    addHandler(lineNo, endTag ? "/" + name : name, stmts);
  }

  public void setDefaultTagHandler(int lineNo, Stmt[] stmts) {
    hasDefaultTagHandler = true;
    addHandler(lineNo, ANY_TAG_SYMBOL, stmts);
  }

  public void setTextHandler(int lineNo, Stmt[] stmts) {
    hasTextHandler = true;
    addHandler(lineNo, ANY_TEXT_SYMBOL, stmts);
  }

  public void setSpecialTagHandler(int lineNo, String type, Stmt[] stmts) {
    hasSpecialTagHandler = true;
    addHandler(lineNo, type, stmts);
    if (type.equals(TYPE1_SYMBOL) && !hasHandler(TYPE3_SYMBOL))
      addHandler(lineNo, TYPE3_SYMBOL, stmts);
  }

  public void pushNewFrame() throws Throwable {
  }

  public void start() throws Throwable {
    StmtHtml h = this;
    if (useHandlerName != null) {
      h = RT.getScript().getSgmlHandler(useHandlerName);
      if (h == null)
        ExceptionRuntime.rte(RTERR_ILLEGAL_ARGUMENTS,
                             "SGML handler '" + useHandlerName + "' not found.");
    }

    Object o;
    String enc = encoding==null ? null : encoding.getStringValue();
    if (root == null) {
      o = IODevice.getInputStream(src.eval(), "SGML handler", enc);
    } else {
      o = root.eval();
      try {
        if (o instanceof ZipArchive) {
          o = ((ZipArchive)o).getBufferedReader(src.getStringValue(), enc);
        } else {
          ZipFile zf = new ZipFile(((Variable)o).getStringValue());
          o = zf.getInputStream(zf.getEntry(src.getStringValue()));
        }
      } catch(NullPointerException npe) { return; } // entry does not exist.
    }

    MarkupParser mp;
    if (o instanceof InputStream)
      mp = new MarkupParser((InputStream)o, !h.hasTextHandler, false, false, h.caseSens);
    else
      mp = new MarkupParser((Reader)o, !h.hasTextHandler, false, false, h.caseSens);

    FrameBlock fb = new FrameBlock((String)null);
    fb.setVariable(PARSER_NAME, new MarkupParserObject(mp), 0);
    RT.pushFrame(fb, (Stmt[])null);

    h.start(mp);
  }

  void start(MarkupParser parser) throws Throwable {
    // get handler stack
    ArrayList handlers = new ArrayList();
    handlers.add(this);
    StmtHtml h;
    while (true) {
      h = (StmtHtml)handlers.get(handlers.size()-1);
      if (h.extendsHandlerName == null) break;
      h = RT.getScript().getSgmlHandler(h.extendsHandlerName);
      if (h == null) break;
      handlers.add(h);
    }
    parser.setWantSpecial(hasSpecialTagHandler);
    int i = 0;

    // run init (BEFORE)
    for (i=handlers.size()-1; i>=0; --i) {
      h = (StmtHtml)handlers.get(i);
      if (h.init != null)
        RT.execStmts(h.init);
    }

    Markup mu = null;
    boolean lastResume = false;
    boolean lastEndTag = false;
lp: while (true) {
      if (lastResume) {
        lastResume = false;
      } else if (lastEndTag) {
        mu = mu.getEndTag();
        lastEndTag = false;
        i = 0;
      } else {
        mu = isJSP ? parser.nextJsp() : parser.next();
        if (mu == null)
          break lp;
        lastEndTag = !mu.isText() && mu.isSelfClosed();
        i = 0;
      }
      try {
        if (mu.isText()) {
          for (; i<handlers.size(); i++) {
            h = (StmtHtml)handlers.get(i);
            if (h.event(ANY_TEXT_SYMBOL, null, JudoUtil.toVariable(mu.getText())) )
              continue lp;
          }
        } else {
          int j = i;
          for (; i<handlers.size(); i++) {
            h = (StmtHtml)handlers.get(i);
            if (h.event(mu.getName(), null, new MarkupValue(mu, caseSens)) )
              continue lp;
          }
          if (!mu.isSpecial()) {
            for (; j<handlers.size(); j++) {
              h = (StmtHtml)handlers.get(j);
              if (h.event(ANY_TAG_SYMBOL, null, new MarkupValue(mu, caseSens)) )
                continue lp;
            }
          }
        }
      } catch(ExceptionControl ce) {
        if (ce.isResume()) { lastResume = true; continue lp; }
        if (ce.isBreak()) break lp;
        if (ce.isReturn()) throw ce;
      }
    }

    // run finish (AFTER)
    for (i=0; i<handlers.size(); ++i) {
      h = (StmtHtml)handlers.get(i);
      if (h.finish != null)
        RT.execStmts(h.finish);
    }
  }

  final static class MarkupValue extends UserDefined
  {
    Markup mu;
    boolean caseSens = false;

    MarkupValue(Markup mu, boolean caseSens) {
      super();
      this.caseSens = caseSens;

      try { init(null); } catch(Throwable e) {}
      this.mu = mu;
      if (!mu.isTag() && !mu.isSpecial())
        return;
      try {
        ArrayList v = mu.getNameValues();
        int len = (v==null) ? 0 : v.size();
        for (int i=len-2; i>=0; i-=2)
          super.setVariable((String)v.get(i), JudoUtil.toVariable(v.get(i+1)), 0);
      } catch(Throwable e) {
        RT.logger.error("Failed to initialize SGMLType.", e);
      }
    }

    public String getTypeName() { return "SGMLTag"; }

    public Variable resolveVariable(Variable name) throws Throwable {
      if (caseSens)
        return super.resolveVariable(name);
      return super.resolveVariable(JudoUtil.toVariable(name.getStringValue().toLowerCase()));
    }

    public Variable setVariable(Variable name, Variable val, int type) throws Throwable {
      mu.setAttr(name.getStringValue(), val.getStringValue());
      return super.setVariable(name, val, type);
    }

    public void removeVariable(Variable name) {
      try {
        removeVariable(name.getStringValue());
      } catch(Throwable e) {}
    }

    public void removeVariable(String name) {
      mu.removeAttr(name);
      super.removeVariable(name);
    }

    public Variable invoke(String fxn, Expr[] params, int[] javaTypes) throws Throwable {
      int ord = getMethodOrdinal(fxn);
      switch(ord) {
      case BIM_GETNAME:     return JudoUtil.toVariable(mu.getName());
      case BIM_GETTEXT:     return JudoUtil.toVariable(mu.getText());
      case BIM_GETROW:      return ConstInt.getInt(mu.getRow());
      case BIM_GETCOLUMN:   return ConstInt.getInt(mu.getColumn());
      case BIM_ISCLOSED:    return ConstInt.getBool(mu.isClosed());
      case BIM_GETALLATTRS: return JudoUtil.toVariable(mu.getAllAttrs());

      case BIM_SETNAME:
        if (params.length > 0) {
          String s = params[0].getStringValue();
          if (StringUtils.isNotBlank(s)) mu.setName(s);
        }
        return ValueSpecial.UNDEFINED;

      default:
        if (fxn.equals("toStringInternal"))
          return JudoUtil.toVariable(mu.toStringInternal());
        else
          return super.invoke(ord, fxn, params);
      }
    }

    public String getStringValue() throws Throwable { return mu.toString(); }

  } // end of inner class MarkupValue.

  final static class MarkupParserObject extends JavaObject
  {
    MarkupParserObject(MarkupParser mp) {
      super(mp);
    }

    public Variable invoke(String fxn, Expr[] params, int[] javaTypes) throws Throwable {
      boolean isRush = fxn.equals("rushToTag");
      if (isRush || fxn.equals("skipToTag")) {
        int len = params==null ? 0 : params.length;
        String[] sa = new String[len];
        for (int i=0; i<len; ++i)
          sa[i] = params[i].getStringValue();
        String ret = ((MarkupParser)object).rushToTag(sa, isRush);
        return JudoUtil.toVariable(ret);
      }
      return ValueSpecial.UNDEFINED;
    }

  } // end of inner class MarkupParserObject.

} // end of class StmtHtml.

