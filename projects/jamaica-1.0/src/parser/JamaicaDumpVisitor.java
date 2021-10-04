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

import java.lang.reflect.Modifier;
import java.io.PrintStream;
import java.util.ArrayList;

import org.apache.commons.lang.StringEscapeUtils;

/**
 * Does nothing but dumping the parse tree.
 * Also serves as a source code template for any other visitors.
 */
public class JamaicaDumpVisitor extends JamaicaVisitorBase
{
  static PrintStream out = System.out;

  public Object visit(ASTClassDeclaration node, Object data) throws Exception {
    printAccessFlags(node.getAccessFlags());
    out.print("class " + node.getName());
    String s = node.getSuper();
    if (s != null)
      out.print(" extends " + s);
    String[] sa = node.getInterfaces();
    int i, len = sa == null ? 0 : sa.length;
    if (len>0)
      out.print(" implements");
    for (i=0; i<len; ++i) {
      if (i>0) out.print(',');
      out.println(" " + sa[i]);
    }
    out.println("\n{");
    node.childrenAccept(this, data);
    out.println('}');
    return null;
  }

  public Object visit(ASTInterfaceDeclaration node, Object data) throws Exception {
    out.print("interface " + node.getName());
    String[] sa = node.getExtends();
    int i, len = sa == null ? 0 : sa.length;
    if (len>0)
      out.print(" extends");
    for (i=0; i<len; ++i) {
      if (i>0) out.print(',');
      out.println(" " + sa[i]);
    }
    out.println();
    out.println('{');
    node.childrenAccept(this, data);
    out.println('}');
    return null;
  }

  public Object visit(ASTDefaultCtor node, Object data) throws Exception {
    out.print("\n  %default_constructor");
    int flags = node.getAccessFlags();
    if (Modifier.isPublic(flags)) out.print(" <public>");
    else if (Modifier.isProtected(flags)) out.print(" <protected>");
    else if (Modifier.isPrivate(flags)) out.print(" <private>");
    out.println();
    return null;
  }

  public Object visit(ASTMethodDeclaration node, Object data) throws Exception {
    boolean abs = Modifier.isAbstract(node.getAccessFlags());
    out.print("\n  ");
    printAccessFlags(node.getAccessFlags());
    out.print(node.getType());
    out.print(' ');
    out.print(node.getName());
    out.print('(');
    ((ASTFormalParameters)node.jjtGetChild(0)).jjtAccept(this, data);
    out.println(')');
    printStringArray("    throws ", node.getExceptions(), !abs);
    if (abs) {
      out.println(";\n");
    } else {
      int bodyLen = node.jjtGetNumChildren();
      out.println("  {");
      int state = 0;
      for (int i=1; i<bodyLen; ++i) {
        Node n = node.jjtGetChild(i);
        switch(state) {
        case 0:  if (n instanceof ASTVariableDeclarator)
                   state = 1;
                 else if (n instanceof ASTVariableDeclarator)
                   state = 2;
                 break;
        case 1:  if (!(n instanceof ASTVariableDeclarator)) {
                   state = 2;
                   out.println();
                 }
        case 2:  if (n instanceof ASTCatchClause) {
                   state = 3;
                   out.println();
                 }
                 break;
        }
        n.jjtAccept(this, data);
      }
      out.println("  }");
    }
    return null;
  }

  public Object visit(ASTFormalParameters node, Object data) throws Exception {
    int len = node.jjtGetNumChildren();
    for (int i=0; i<len; ++i) {
      if (i>0) out.print(", ");
      visit((ASTVariableDeclarator)node.jjtGetChild(i), data);
    }
    return null;
  }

  public Object visit(ASTInitializer node, Object data) throws Exception {
    out.println("  {");
    node.childrenAccept(this, data);
    out.println("  }\n");
    return null;
  }

  ////////////////////////////////////////////////////////////////
  // Fields, Variables or Parameters
  ////////////////////////////////////////////////////////////////

  public Object visit(ASTVariableDeclarator node, Object data) throws Exception {
    boolean newline = true;
    Node parent = node.jjtGetParent();
    if (parent instanceof ASTFormalParameters) { // a parameter
      newline = false;;
    } else if (parent instanceof ASTMethodDeclaration) { // a local variable
      out.print("    ");
    } else { // a class field or an interface constant
      out.print("  ");
      printAccessFlags(node.getAccessFlags());
    }
    out.print(node.getType());
    out.print(' ');
    out.print(node.getName());
    if (node.getValue() != null)
      out.print(" = " + node.getValue());
    if (newline) 
      out.println(";");
    return null;
  }

  ////////////////////////////////////////////////////////////////
  // Bytecode Instructions
  ////////////////////////////////////////////////////////////////

  public Object visit(ASTCatchClause node, Object data) throws Exception {
    String exception = node.getException();
    out.print("  catch " + exception);
    out.print(" (");
    out.print(node.getStartLabel());
    out.print(", ");
    out.print(node.getEndLabel());
    out.print(") ");
    out.println(node.getActionLabel());
    return null;
  }

  public Object visit(ASTLabel node, Object data) throws Exception {
    out.println(node.getName() + ":");
    return null;
  }

  public Object visit(ASTCodeSimple node, Object data) throws Exception {
    printMnemonic(node, "\n");
    return null;
  }

  public Object visit(ASTCodeWithText node, Object data) throws Exception {
    printMnemonic(node, " ");
    out.println(node.getText());
    return null;
  }

  public Object visit(ASTCodeWithTextInt node, Object data) throws Exception {
    printMnemonic(node, " ");
    String s = node.getText();
    if (s != null)
      out.print(s + ' ');
    out.println(node.getAmount());
    return null;
  }

  public Object visit(ASTCodeWithConstant node, Object data) throws Exception {
    printMnemonic(node, " ");

    Object obj = node.getConstant();
    String s = StringEscapeUtils.escapeJava(obj.toString());
    if (obj instanceof String)
      out.println("\"" + s + '"');
    else if (obj instanceof Character)
      out.println("\'" + s + '\'');
    return null;
  }

  public Object visit(ASTCodeMemberAccess node, Object data) throws Exception {
    printMnemonic(node, " ");
    out.print(node.getClassName());
    out.print(' ');
    out.println(node.getName());
    return null;
  }

  public Object visit(ASTCodeInvoke node, Object data) throws Exception {
    printMnemonic(node, " ");
    out.print(node.getClassName());
    out.print('.');
    out.print(node.getName());
    out.print('(');

    String[] paramTypes = node.getParamTypes();
    int len = paramTypes == null ? 0 : paramTypes.length;
    for (int i=0; i<len; i++) {
      if (i>0) out.print(", ");
      out.print(paramTypes[i]);
    }

    out.print(')');
    out.println(node.getType());
    return null;
  }

  public Object visit(ASTCodeSwitch node, Object data) throws Exception {
    Object[] cases = node.getCases();
    int[]    values = (int[])cases[0];
    String[] caseLabels   = (String[])cases[1];
    String   defaultLabel = node.getDefault();

    out.println("    switch");
    for (int i=0; i<values.length; ++i)
      out.println("      " + values[i] + ": " + caseLabels[i]);
    if (defaultLabel != null)
      out.println("      default: " + defaultLabel);
    return null;
  }

  public Object visit(ASTMacroSet node, Object data) throws Exception {
    Object t = node.getTarget();
    Object v = node.getValue();
    if (t == null) {
      out.print("    %load ");
    } else if (v == null) {
      out.print("    %store ");
      out.print(t);
    } else {
      out.print("    %set ");
      out.print(t);
      out.print(" = ");
    }
    if (v != null)
      dumpParam(v);
    out.println();
    return null;
  }

  public Object visit(ASTMacroPrint node, Object data) throws Exception {
    out.print("    %");
    out.print(node.getName());
    out.print(" <");
    out.print(node.getTarget());
    out.print("> ");
    dumpParamList(node.getParams(), true);
    return null;
  }

  public Object visit(ASTMacroObject node, Object data) throws Exception {
    if (data == null)
      out.print("    ");
    out.print("%object ");
    out.print(node.getType());
    Object[] params = node.getParams();
    String[] paramTypes = node.getParamTypes();
    int len = params==null ? 0 : params.length;
    if (len > 0) {
      int i;
      out.print('(');
      for (i=0; i<len; ++i) {
        if (i>0) out.print(", ");
        out.print(paramTypes[i]);
      }
      out.print(")(");
      for (i=0; i<len; ++i) {
        if (i>0) out.print(", ");
        Object o = params[i];
        if (o instanceof String)
          o = "\"" + StringEscapeUtils.escapeJava(o.toString()) + '"';
        out.print(o);
      }
      out.print(')');
    }
    if (data == null)
      out.println();
    return null;
  }

  public Object visit(ASTMacroArray node, Object data) throws Exception {
    Object[] params = node.getParams();
    int i, len = params==null ? 0 : params.length;

    if (data == null)
      out.print("    ");
    out.print("%array ");
    out.print(node.getType());

    int dim = node.getDim();
    if (dim <= 0 && len > 0) { // single-dim, initialized.
      out.print("[] { ");
      dumpParamList(params, false);
      out.print(" }");
    } else {
      for (i=0; i<len; ++i) {
        out.print('[');
        dumpParam(params[i]);
        out.print(']');
      }
      for (; i<dim; ++i)
        out.print("[]");
    }
    if (data == null)
      out.println();
    return null;
  }

  public Object visit(ASTMacroStringConcat node, Object data) throws Exception {
    out.print("    %string_concat ");
    dumpParamList(node.getParams(), data == null);
    return null;
  }

  public Object visit(ASTMacroIterate node, Object data) throws Exception {
    if (node.isEnd()) {
      out.println("    %end_iterate");
    } else {
      out.print(node.isArray() ? "    %array_iterate " : "    %iterate ");
      dumpParam(node.getCollection());
      String s = node.getIterateVar();
      if (s != null) {
        out.print(' ');
        out.print(s);
      }
      out.println();
    }
    return null;
  }

  public Object visit(ASTMacroIf node, Object data) throws Exception {
    if (node.isEnd()) {
      out.println("    %end_if");
    } else if (node.isElse()) {
      out.println("    %else");
    } else {
      Object[] params = node.getParams();
      out.print  ("    %if ");
      dumpParam(params[0]);
      out.print  (' ');
      out.print  (node.getOperator());
      out.print  (' ');
      dumpParam(params[1]);
      out.println();
    }
    return null;
  }  

  public Object visit(ASTMacroBase node, Object data) throws Exception {
    if (node instanceof ASTMacroObject) return visit((ASTMacroObject)node, data);
    if (node instanceof ASTMacroArray)        return visit((ASTMacroArray)node, data);
    if (node instanceof ASTMacroStringConcat) return visit((ASTMacroStringConcat)node, data);
    if (node instanceof ASTMacroSet)          return visit((ASTMacroSet)node, data);
    if (node instanceof ASTMacroPrint)        return visit((ASTMacroPrint)node, data);
    if (node instanceof ASTMacroIterate)      return visit((ASTMacroIterate)node, data);
    if (node instanceof ASTMacroIf)           return visit((ASTMacroIf)node, data);
    return data;
  }

  ////////////////////////////////////////////////////////////////
  // Helpers
  ////////////////////////////////////////////////////////////////

  static void printAccessFlags(int flags) {
    if      (Modifier.isPublic(flags))    out.print("public ");
    else if (Modifier.isProtected(flags)) out.print("protected ");
    else if (Modifier.isPrivate(flags))   out.print("private ");

    if (Modifier.isAbstract(flags))     out.print("abstract ");
    if (Modifier.isFinal(flags))        out.print("final ");
    if (Modifier.isNative(flags))       out.print("native ");
    if (Modifier.isStatic(flags))       out.print("static ");
    if (Modifier.isStrict(flags))       out.print("strict ");
    if (Modifier.isSynchronized(flags)) out.print("synchronized ");
    if (Modifier.isTransient(flags))    out.print("transient ");
    if (Modifier.isVolatile(flags))     out.print("volative ");
  }

  static void printStringArray(String prefix, String[] sa, boolean newline) {
    int len = sa == null ? 0 : sa.length;
    if (len <= 0) return;
    out.print(prefix);
    for (int i=0; i<len; ++i) {
      if (i>0) out.print(", ");
      out.print(sa[i]);
    }
    if (newline)
      out.println();
  }

  static void printMnemonic(ASTCodeSimple code, String postfix) {
    out.print("    ");
    out.print(getMnemonic(code.getOpcode()));
    if (postfix != null)
      out.print(postfix);
  }

  void dumpParam(Object o) {
    if (o instanceof ASTMacroBase) {
      try {
        visit((ASTMacroBase)o, Boolean.TRUE);
        return;
      } catch(Exception e) {}
    } else {
      String s = StringEscapeUtils.escapeJava(o.toString());
      if (o instanceof String)
        o = "\"" + s + '"';
      else if (o instanceof Character)
        o = '\'' + s + '\'';
    }
    out.print(o);
  }

  void dumpParamList(Object[] oa, boolean newline) {
    int len = oa==null ? 0 : oa.length;
    for (int i=0; i<len; ++i) {
      if (i>0) out.print(", ");
      dumpParam(oa[i]);
    }
    if (newline)
      out.println();
  }

  //////////////////////////////////////////////////
  // Constants -- copied from BCEL.

  public static String getMnemonic(int opcode) {
    try { return OPCODE_NAMES[opcode]; }
    catch(Exception e) { return "???"; }
  }

  public static final String[] OPCODE_NAMES = {
    "nop", "aconst_null", "iconst_m1", "iconst_0", "iconst_1",
    "iconst_2", "iconst_3", "iconst_4", "iconst_5", "lconst_0",
    "lconst_1", "fconst_0", "fconst_1", "fconst_2", "dconst_0",
    "dconst_1", "bipush", "sipush", "ldc", "ldc_w", "ldc2_w", "iload",
    "lload", "fload", "dload", "aload", "iload_0", "iload_1", "iload_2",
    "iload_3", "lload_0", "lload_1", "lload_2", "lload_3", "fload_0",
    "fload_1", "fload_2", "fload_3", "dload_0", "dload_1", "dload_2",
    "dload_3", "aload_0", "aload_1", "aload_2", "aload_3", "iaload",
    "laload", "faload", "daload", "aaload", "baload", "caload", "saload",
    "istore", "lstore", "fstore", "dstore", "astore", "istore_0",
    "istore_1", "istore_2", "istore_3", "lstore_0", "lstore_1",
    "lstore_2", "lstore_3", "fstore_0", "fstore_1", "fstore_2",
    "fstore_3", "dstore_0", "dstore_1", "dstore_2", "dstore_3",
    "astore_0", "astore_1", "astore_2", "astore_3", "iastore", "lastore",
    "fastore", "dastore", "aastore", "bastore", "castore", "sastore",
    "pop", "pop2", "dup", "dup_x1", "dup_x2", "dup2", "dup2_x1",
    "dup2_x2", "swap", "iadd", "ladd", "fadd", "dadd", "isub", "lsub",
    "fsub", "dsub", "imul", "lmul", "fmul", "dmul", "idiv", "ldiv",
    "fdiv", "ddiv", "irem", "lrem", "frem", "drem", "ineg", "lneg",
    "fneg", "dneg", "ishl", "lshl", "ishr", "lshr", "iushr", "lushr",
    "iand", "land", "ior", "lor", "ixor", "lxor", "iinc", "i2l", "i2f",
    "i2d", "l2i", "l2f", "l2d", "f2i", "f2l", "f2d", "d2i", "d2l", "d2f",
    "i2b", "i2c", "i2s", "lcmp", "fcmpl", "fcmpg",
    "dcmpl", "dcmpg", "ifeq", "ifne", "iflt", "ifge", "ifgt", "ifle",
    "if_icmpeq", "if_icmpne", "if_icmplt", "if_icmpge", "if_icmpgt",
    "if_icmple", "if_acmpeq", "if_acmpne", "goto", "jsr", "ret",
    "tableswitch", "lookupswitch", "ireturn", "lreturn", "freturn",
    "dreturn", "areturn", "return", "getstatic", "putstatic", "getfield",
    "putfield", "invokevirtual", "invokespecial", "invokestatic",
    "invokeinterface", "???", "new", "newarray", "anewarray",
    "arraylength", "athrow", "checkcast", "instanceof", "monitorenter",
    "monitorexit", "wide", "multianewarray", "ifnull", "ifnonnull",
    "goto_w", "jsr_w", "breakpoint", "???", "???", "???", "???", "???",
    "???", "???", "???", "???", "???", "???", "???", "???", "???", "???",
    "???", "???", "???", "???", "???", "???", "???", "???", "???", "???",
    "???", "???", "???", "???", "???", "???", "???", "???", "???", "???",
    "???", "???", "???", "???", "???", "???", "???", "???", "???", "???",
    "???", "???", "???", "???", "???", "???", "impdep1", "impdep2"
  };

} // end of class.
