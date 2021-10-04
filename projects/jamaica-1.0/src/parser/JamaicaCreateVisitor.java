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
 * 04-01-2004  JH   Removed checkEndingReturn(); the logic is now
 *                  in the API (BCELJavaClassCreator, ...).
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/

package com.judoscript.jamaica.parser;

import java.lang.reflect.Modifier;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import org.apache.bcel.Constants;
import com.judoscript.jamaica.JavaClassCreator;
import com.judoscript.jamaica.JavaClassCreatorException;

/**
 * Creates the Java class.
 */
public class JamaicaCreateVisitor extends JamaicaVisitorBase
{
  ////////////////////////////////////////////////////////////////
  // Data Members
  ////////////////////////////////////////////////////////////////

  static final String STATIC_INIT_PREFIX = "__clinit__";

  JavaClassCreator creator = JavaClassCreator.getJavaClassCreator();
  String className     = null;
  String classRootName = null;
  byte[] classBytes    = null;
  int    staticInitCnt = 0;

  public String getClassName()     { return className; }
  public String getClassRootName() { return classRootName; }
  public byte[] getClassBytes()    { return classBytes; }

  public void setSourceFileName(String fileName) { creator.setSourceFileName(fileName); }

  ////////////////////////////////////////////////////////////////
  // High-Level Constructs
  ////////////////////////////////////////////////////////////////

  public Object visit(ASTClassDeclaration node, Object data) throws Exception {
    creator.startClass(node.getAccessFlags(), node.getName(), node.getSuper(), node.getInterfaces());
    collectFields(node);

    node.childrenAccept(this, data);

    // Static initializers
    if (staticInitCnt > 0) {
      creator.startMethod(Modifier.STATIC, "<clinit>", JavaClassCreator.NO_STRING_ARRAY,
                          JavaClassCreator.NO_STRING_ARRAY, "void", JavaClassCreator.NO_STRING_ARRAY);
      for (int i=1; i<=staticInitCnt; ++i)
        creator.inst_invokestatic(getClassName(), STATIC_INIT_PREFIX+i,
                                  JavaClassCreator.NO_STRING_ARRAY, "void");
      creator.inst_return();
      creator.endMethod();
    }

    className = creator.getClassName();
    classRootName = creator.getClassRootName();

    classBytes = creator.endClass();
    return null;
  }

  public Object visit(ASTInterfaceDeclaration node, Object data) throws Exception {
    creator.startInterface(node.getName(), node.getExtends());
    collectFields(node);

    node.childrenAccept(this, data);

    className = creator.getClassName();
    classRootName = creator.getClassRootName();
    classBytes = creator.endClass();
    return null;
  }

  private void collectFields(SimpleNode node) throws Exception {
    HashSet names = new HashSet();

    int len = node.jjtGetNumChildren();
    for (int i=0; i<len; ++i) {
      SimpleNode n = (SimpleNode)node.jjtGetChild(i);
      if (!(n instanceof ASTVariableDeclarator))
        continue;

      ASTVariableDeclarator varDecl = (ASTVariableDeclarator)n;
      int flags = varDecl.getAccessFlags();
      String name = varDecl.getName();
      if (names.contains(name))
        throw new JavaClassCreatorException("Field " + name + " already defined.");
      names.add(name);
      Object val = varDecl.getValue();
      if (Modifier.isStatic(flags) && Modifier.isFinal(flags) && val != null) { // constant
        creator.addConstant(flags, name, varDecl.getType(), val);
      } else {
        creator.addField(flags, name, varDecl.getType());
      }
    }
  }

  public Object visit(ASTDefaultCtor node, Object data) throws Exception {
    creator.addDefaultConstructor(node.getAccessFlags());
    return null;
  }

  public Object visit(ASTMethodDeclaration node, Object data) throws Exception {
    ASTFormalParameters params = ((ASTFormalParameters)node.jjtGetChild(0));
    
    int len = params.jjtGetNumChildren();
    String[] paramTypes = new String[len];
    String[] paramNames = new String[len];
    for (int i=0; i<len; ++i) {
      ASTVariableDeclarator param = (ASTVariableDeclarator)params.jjtGetChild(i);
      paramTypes[i] = param.getType();
      paramNames[i] = param.getName();
    }

//    if (!Modifier.isAbstract(node.getAccessFlags()) && node.getType().equals("void"))
//      checkEndingReturn(node);
    creator.startMethod(node.getAccessFlags(),
                        node.getName(),
                        paramTypes,
                        paramNames,
                        node.getType(),
                        node.getExceptions());
    node.childrenAccept(this, data);
    creator.endMethod();
    return null;
  }

  public Object visit(ASTFormalParameters node, Object data) throws Exception {
    return null; // nothing to do
  }

  public Object visit(ASTInitializer node, Object data) throws Exception {
    creator.startMethod(Modifier.STATIC | Modifier.PRIVATE, STATIC_INIT_PREFIX + (++staticInitCnt),
                        JavaClassCreator.NO_STRING_ARRAY, JavaClassCreator.NO_STRING_ARRAY,
                        "void", JavaClassCreator.NO_STRING_ARRAY);
//    checkEndingReturn(node);
    node.childrenAccept(this, data);
    creator.endMethod();
    return null;
  }

  ////////////////////////////////////////////////////////////////
  // Fields, Variables or Parameters
  ////////////////////////////////////////////////////////////////

  public Object visit(ASTVariableDeclarator node, Object data) throws Exception {
    HashSet names = new HashSet();
    // node.getType()
    // node.getName()
    Node parent = node.jjtGetParent();
    if (parent instanceof ASTMethodDeclaration) { // a local variable
      String name = node.getName();
      if (names.contains(name))
        throw new JavaClassCreatorException("Variable " + name + " already defined.");
      names.add(name);
      creator.addLocalVariable(name, node.getType(), null);
      Object v = node.getValue();
      if (v != null)
        creator.macroSet(name, v, this, data);
/*
    } else if (parent instanceof ASTFormalParameters) { // a parameter
      ; // already handled in ASTMethodDeclarator.
    } else { // a class field or an interface constant
      ; // already handled in ASTClass/InterfaceDeclaration.
*/
    }

    return null;
  }

  ////////////////////////////////////////////////////////////////
  // Bytecode Instructions
  ////////////////////////////////////////////////////////////////

  public Object visit(ASTCatchClause node, Object data) throws Exception {
    creator.addCatchClause(node.getException(),
                           node.getStartLabel(),
                           node.getEndLabel(),
                           node.getActionLabel());
    return null;
  }

  public Object visit(ASTLabel node, Object data) throws Exception {
    creator.setLabel(node.getName());
    return null;
  }

  public Object visit(ASTCodeSimple node, Object data) throws Exception {
    creator.inst(node.getOpcode());
    return null;
  }

  /**
   * <ol>
   * <li> ifeq ifne iflt ifge ifgt ifle
   *      if_icmpeq if_icmpne if_icmplt if_icmpge if_icmpgt if_icmple if_acmpeq if_acmpne
   *      goto jsr ifnull ifnonnull goto_w jsr_w
   * <li> iload lload fload dload aload istore lstore fstore dstore astore ret
   * <li> new newarray anewarray checkcast instanceof
   * </ol>
   */
  public Object visit(ASTCodeWithText node, Object data) throws Exception {
    String s = node.getText();
    int opcode = node.getOpcode();

    switch(opcode) {
    case 25:  // aload
      if (s.equals("this")) {
        creator.inst_aload_0();
        break;
      }
      // else, fall thru

    case 21:  // iload
    case 22:  // lload
    case 23:  // fload
    case 24:  // dload
    case 54:  // istore
    case 55:  // lstore
    case 56:  // fstore
    case 57:  // dstore
    case 58:  // astore
    case 169: // ret
      creator.instLoadStoreRet(opcode, s);
      break;

    case 187: // new
    case 188: // newarray
    case 189: // anewarray
    case 192: // checkcast
    case 193: // instanceof
      creator.instType(opcode, s);
      break;

    default: // branch instructions
      creator.instJump(opcode, s);
    }
    return null;
  }

  /**
   * <ol>
   * <li> iinc multianewarray
   * <li> bipush sipush
   * </ol>
   */
  public Object visit(ASTCodeWithTextInt node, Object data) throws Exception {
    int amt = node.getAmount();
    switch(node.getOpcode()) {
    case 16:  // bipush
      creator.inst_bipush((byte)amt);
      break;
    case 17:  // sipush
      creator.inst_sipush((short)amt);
      break;
    case 132: // iinc
      creator.inst_iinc(node.getText(), amt);
      break;
    case 197: // multianewarray
      creator.inst_multianewarray(node.getText(), (short)amt);
    }
    return null;
  }

  public Object visit(ASTCodeWithConstant node, Object data) throws Exception {
    creator.instLdc(node.getOpcode(), node.getConstant(), node.getType());
    return null;
  }

  public Object visit(ASTCodeMemberAccess node, Object data) throws Exception {
    String cls = node.getClassName();
    String fld = node.getName();
    creator.instGetPut(node.getOpcode(), node.getClassName(), node.getName(), node.getType());
    return null;
  }

  public Object visit(ASTCodeInvoke node, Object data) throws Exception {
    creator.instInvoke(node.getOpcode(), node.getClassName(), node.getName(),
                       node.getParamTypes(), node.getType());
    return null;
  }

  public Object visit(ASTCodeSwitch node, Object data) throws Exception {
    Object[] cases = node.getCases();
    creator.inst_switch((int[])cases[0], (String[])cases[1], node.getDefault());
    return null;
  }

  public Object visit(ASTMacroSet node, Object data) throws Exception {
    creator.macroSet(node.getTarget(), node.getValue(), this, data);
    return null;
  }

  public Object visit(ASTMacroPrint node, Object data) throws Exception {
    creator.macroPrint(node.getName(), node.getTarget(), node.getParams());
    return null;
  }

  public Object visit(ASTMacroObject node, Object data) throws Exception {
    creator.macroObject(node.getType(), node.getParamTypes(), node.getParams());
    return null;
  }

  public Object visit(ASTMacroArray node, Object data) throws Exception {
    creator.macroArray(node.getType(), node.getDim(), node.getParams());
    return null;
  }

  public Object visit(ASTMacroStringConcat node, Object data) throws Exception {
    creator.macroStringConcat(node.getParams());
    return null;
  }

  public Object visit(ASTMacroIterate node, Object data) throws Exception {
    if (node.isArray())
      creator.macroArrayIterate(node.getArray(), node.getIterateVar(), node.getID());
    else
      creator.macroIterate(node.getCollection(), node.getIterateVar(), node.getID());
    return null;
  }

  public Object visit(ASTMacroIf node, Object data) throws Exception {
    String id = node.getID();
    if (node.isEnd())
      creator.macroEndIf(id);
    else if (node.isElse())
      creator.macroElse(id);
    else {
      Object[] oa = node.getParams();
      creator.macroIf(node.getOperator(), oa[0], oa[1], id, node.hasElse());
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////
  // Helpers
  ////////////////////////////////////////////////////////////////

/*
  void checkEndingReturn(SimpleNode node) {
    int i;
    for (i = node.jjtGetNumChildren() - 1; i>=0; --i) {
      Node n = node.jjtGetChild(i);
      if (n instanceof ASTCodeSimple) {
        if (((ASTCodeSimple)n).getOpcode() == 177)
          return;
        break;
      }
    }
    node.jjtInsertChild(new ASTCodeSimple(JamaicaParserTreeConstants.JJTCODESIMPLE,177), i<0 ? 0 : i+1);
  }
*/


} // end of class.
