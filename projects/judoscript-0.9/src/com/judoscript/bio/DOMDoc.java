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


package com.judoscript.bio;

import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.w3c.dom.traversal.*;
import com.judoscript.*;


public final class DOMDoc extends JavaObject
{
  // Use Object to avoid package dependency.
  public DOMDoc(Object doc) { super(doc); }

  public Variable invoke(String fxn, Expr[] args, int[] javaTypes) throws Throwable {
    if (fxn.equals("createNodeIterator") && (args.length==4)) {
      NodeIterator ni = ((DocumentTraversal)object).createNodeIterator(
                          (Node)args[0].eval().getObjectValue(),
                          (int)args[1].getLongValue(),
                          getNodeFilter(args[2].eval()),
                          args[3].getBoolValue());
      return JudoUtil.toVariable(ni);
    } else if (fxn.equals("createTreeWalker") && (args.length==4)) {
      TreeWalker tw = ((DocumentTraversal)object).createTreeWalker(
                          (Node)args[0].eval().getObjectValue(),
                          (int)args[1].getLongValue(),
                          getNodeFilter(args[2].eval()),
                          args[3].getBoolValue());
      return JudoUtil.toVariable(tw);
    }
    return super.invoke(fxn, args, javaTypes);
  }

  NodeFilter getNodeFilter(Variable var) throws Throwable {
    Object o = var.getObjectValue();
    if (o instanceof NodeFilter) return (NodeFilter)o;
    if (!(var instanceof AccessFunction))
      ExceptionRuntime.rte(RTERR_ILLEGAL_ARGUMENTS,
        "createNodeIterator() and createTreeWalker() method requires a function variable.");
    return new UserFilter(((AccessFunction)var).getName());
  }

  static class UserFilter implements NodeFilter
  {
    String fxn;

    UserFilter(String fxn) { this.fxn = fxn; }

    // should return FILTER_ACCEPT, FILTER_REJECT or FILTER_SKIP.
    public short acceptNode(Node node) {
      try {
        return (short)RT.getScript().invoke(fxn,new Expr[]{JudoUtil.toVariable(node)},null)
                      .getLongValue();      
      } catch(Throwable e) {
        return FILTER_SKIP;
      }
    }
  }

  public static Document createDom() throws ParserConfigurationException {
    return DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
  }

} // end of class DOMDoc.
