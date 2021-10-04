/* Jamaica, The Java Virtual Machine (JVM) Macro Assembly Language
 * Copyright (C) 2004- James Huang,
 * http://www.judoscript.com/jamaica/index.html
 *
 * This is free software; you can embed, modify and redistribute
 * it under the terms of the GNU Lesser General Public License
 * version 2.1 or up as published by the Free Software Foundation,
 * which you should have received a copy along with this software.
 * In case you did not, please download it from the internet at
 * http://www.gnu.org/copyleft/lesser.html
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 03-14-2004  JH   Initial release.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/

package com.judoscript.jamaica.parser;

public abstract class SimpleNode implements Node
{
  protected int lineNum = 0;

  protected Node parent;
  protected Node[] children;
  protected int id;
//  protected JamaicaParser parser;


  public int getLineNum() { return lineNum; }

  public SimpleNode(int i) {
    id = i;
  }

  public SimpleNode(JamaicaParser p, int i) {
    this(i);
//    parser = p;
  }

  public void jjtOpen() {
  }

  public void jjtClose() {
  }
  
  public void jjtSetParent(Node n) { parent = n; }
  public Node jjtGetParent() { return parent; }

  public void jjtAddChild(Node n, int i) {
    if (children == null) {
      children = new Node[i + 1];
    } else if (i >= children.length) {
      Node c[] = new Node[i + 1];
      System.arraycopy(children, 0, c, 0, children.length);
      children = c;
    }
    children[i] = n;
  }

  public void jjtInsertChild(Node n, int i) {
    if (children == null) {
      children = new Node[i + 1];
    } else if (i >= children.length) {
      Node c[] = new Node[i + 1];
      System.arraycopy(children, 0, c, 0, children.length);
      children = c;
    } else {
      Node c[] = new Node[children.length + 1];
      System.arraycopy(children, 0, c, 0, i);
      System.arraycopy(children, i, c, i+1, children.length - i);
      children = c;
    }
    children[i] = n;
  }

  public Node jjtGetChild(int i) {
    return children[i];
  }

  public int jjtGetNumChildren() {
    return (children == null) ? 0 : children.length;
  }

  /** Accept the visitor. **/
//  public Object jjtAccept(JamaicaParserVisitor visitor, Object data) throws Exception {
//    return visitor.visit(this, data);
//  }

  /** Accept the visitor. **/
  public Object childrenAccept(JamaicaParserVisitor visitor, Object data) throws Exception {
    int len = children != null ? children.length : 0;
    for (int i=0; i<len; ++i)
      children[i].jjtAccept(visitor, data);
    return data;
  }

  /* You can override these two methods in subclasses of SimpleNode to
     customize the way the node appears when the tree is dumped.  If
     your output uses more than one line you should override
     toString(String), otherwise overriding toString() is probably all
     you need to do. */

  public String toString() { return JamaicaParserTreeConstants.jjtNodeName[id]; }
  public String toString(String prefix) { return prefix + toString(); }

  /* Override this method if you want to customize how the node dumps
     out its children. */

  public void dump(String prefix) {
    System.out.println(toString(prefix));
    if (children != null) {
      for (int i = 0; i < children.length; ++i) {
	SimpleNode n = (SimpleNode)children[i];
	if (n != null) {
	  n.dump(prefix + " ");
	}
      }
    }
  }

  /////////////////////////////////////////////////////////////////////////
  // Jamaica specific
  /////////////////////////////////////////////////////////////////////////

  public String getSuperClassName() {
    try {
      ASTClassDeclaration cls = (ASTClassDeclaration)getClassDecl();
      return cls.getSuper();
    } catch(Exception e) {
      return null;
    }
  }

  public String getClassName() {
    ASTInterfaceDeclaration cls = getClassDecl();
    return cls==null ? null : cls.getName();
  }

  public ASTInterfaceDeclaration getClassDecl() {
    SimpleNode n = (SimpleNode)parent;
    while (n != null && !(n instanceof ASTInterfaceDeclaration))
      n = (SimpleNode)n.jjtGetParent();
    return (ASTInterfaceDeclaration)n;
  }
}

