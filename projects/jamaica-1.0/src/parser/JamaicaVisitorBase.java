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

public abstract class JamaicaVisitorBase implements JamaicaParserVisitor
{
  public Object visit(SimpleNode node, Object data) throws Exception {
    if (node instanceof ASTMacroObject)          return visit((ASTMacroObject)node, data);
    if (node instanceof ASTMacroArray)           return visit((ASTMacroArray)node, data);
    if (node instanceof ASTMacroStringConcat)    return visit((ASTMacroStringConcat)node, data);
    if (node instanceof ASTMacroSet)             return visit((ASTMacroSet)node, data);
    if (node instanceof ASTMacroPrint)           return visit((ASTMacroPrint)node, data);
    if (node instanceof ASTMacroIterate)         return visit((ASTMacroIterate)node, data);
    if (node instanceof ASTMacroIf)              return visit((ASTMacroIf)node, data);
    if (node instanceof ASTLabel)                return visit((ASTLabel)node, data);
    if (node instanceof ASTCodeSwitch)           return visit((ASTCodeSwitch)node, data);
    if (node instanceof ASTCodeInvoke)           return visit((ASTCodeInvoke)node, data);
    if (node instanceof ASTCodeMemberAccess)     return visit((ASTCodeMemberAccess)node, data);
    if (node instanceof ASTCodeWithTextInt)      return visit((ASTCodeWithTextInt)node, data);
    if (node instanceof ASTCodeWithText)         return visit((ASTCodeWithText)node, data);
    if (node instanceof ASTCodeWithConstant)     return visit((ASTCodeWithConstant)node, data);
    if (node instanceof ASTCodeSimple)           return visit((ASTCodeSimple)node, data);
    if (node instanceof ASTCatchClause)          return visit((ASTCatchClause)node, data);
    if (node instanceof ASTVariableDeclarator)   return visit((ASTVariableDeclarator)node, data);
    if (node instanceof ASTMethodDeclaration)    return visit((ASTMethodDeclaration)node, data);
    if (node instanceof ASTFormalParameters)     return visit((ASTFormalParameters)node, data);
    if (node instanceof ASTClassDeclaration)     return visit((ASTClassDeclaration)node, data);
    if (node instanceof ASTInterfaceDeclaration) return visit((ASTInterfaceDeclaration)node, data);
    if (node instanceof ASTDefaultCtor)          return visit((ASTDefaultCtor)node, data);
    return data;
  }

/******************************************************************
  Code Template
 ******************************************************************/

  ////////////////////////////////////////////////////////////////
  // High-Level Constructs
  ////////////////////////////////////////////////////////////////

  public Object visit(ASTClassDeclaration node, Object data) throws Exception {
/*
    // node.getAccessFlags()
    // node.getName()
    String s = MyUtils.neverNull(node.getSuper(), "java.lang.Object");

    String[] sa = node.getInterfaces();
    int i, len = sa == null ? 0 : sa.length;
    for (i=0; i<len; ++i) {
      // todo
    }
*/
    node.childrenAccept(this, data);
    return null;
  }

  public Object visit(ASTInterfaceDeclaration node, Object data) throws Exception {
/*
    // node.getName()
    String[] sa = node.getExtends();
    int i, len = sa == null ? 0 : sa.length;
    for (i=0; i<len; ++i) {
      // todo
    }
*/
    node.childrenAccept(this, data);
    return null;
  }

  public Object visit(ASTDefaultCtor node, Object data) throws Exception {
    // todo
    return null;
  }

  public Object visit(ASTMethodDeclaration node, Object data) throws Exception {
/*
    // node.getAccessFlags()
    // node.getType()
    // node.getName()
    ((ASTFormalParameters)node.jjtGetChild(0)).jjtAccept(this, data);
    // node.getExceptions()
    int bodyLen = node.jjtGetNumChildren() - 1;
    if (bodyLen <= 0) {
      // todo
    } else {
      // todo
    }
*/
    node.childrenAccept(this, data);
    return null;
  }

  public Object visit(ASTFormalParameters node, Object data) throws Exception {
/*
    int len = node.jjtGetNumChildren();
    for (int i=0; i<len; ++i) {
      visit((ASTVariableDeclarator)node.jjtGetChild(i), data);
    }
*/
    node.childrenAccept(this, data);
    return null;
  }

  public Object visit(ASTInitializer node, Object data) throws Exception {
    node.childrenAccept(this, data);
    return null;
  }

  ////////////////////////////////////////////////////////////////
  // Fields, Variables or Parameters
  ////////////////////////////////////////////////////////////////

  public Object visit(ASTVariableDeclarator node, Object data) throws Exception {
/*
    // node.getType()
    // node.getName()
    Node parent = node.jjtGetParent();
    if (parent instanceof ASTFormalParameters) { // a parameter
      // todo
    } else if (parent instanceof ASTMethodDeclaration) { // a local variable
      // todo
    } else { // a class field or an interface constant
      // todo
    }
*/
    return null;
  }

  ////////////////////////////////////////////////////////////////
  // Bytecode Instructions
  ////////////////////////////////////////////////////////////////

//  public Object visit(ASTCatchClause node, Object data) throws Exception {
//    // todo
//    return null;
//  }
//
//  public Object visit(ASTLabel node, Object data) throws Exception {
//    // todo
//    return null;
//  }
//
//  public Object visit(ASTCodeSimple node, Object data) throws Exception {
//    // todo
//    return null;
//  }
//
//  public Object visit(ASTCodeWithText node, Object data) throws Exception {
//    // todo
//    return null;
//  }
//
//  public Object visit(ASTCodeWithTextInt node, Object data) throws Exception {
//    // todo
//    return null;
//  }
//
//  public Object visit(ASTCodeWithConstant node, Object data) throws Exception {
//    // todo
//    return null;
//  }
//
//  public Object visit(ASTCodeMemberAccess node, Object data) throws Exception {
//    // todo
//    return null;
//  }
//
//  public Object visit(ASTCodeInvoke node, Object data) throws Exception {
//    // todo
//    return null;
//  }
//
//  public Object visit(ASTCodeSwitch node, Object data) throws Exception {
//    // todo
//    return null;
//  }
//
//  public Object visit(ASTMacroPrint node, Object data) throws Exception {
//    // todo
//    return null;
//  }
//
//  public Object visit(ASTMacroObject node, Object data) throws Exception {
//    // todo
//    return null;
//  }
//
//  public Object visit(ASTMacroArray node, Object data) throws Exception {
//    // todo
//    return null;
//  }
//
//  public Object visit(ASTMacroStringConcat node, Object data) throws Exception {
//    // todo
//    return null;
//  }
//
//  public Object visit(ASTMacroIterate node, Object data) throws Exception {
//    // todo
//    return null;
//  }
//
//  public Object visit(ASTMacroIf node, Object data) throws Exception {
//    // todo
//    return null;
//  }

  ////////////////////////////////////////////////////////////////
  // Helpers
  ////////////////////////////////////////////////////////////////


} // end of class.
