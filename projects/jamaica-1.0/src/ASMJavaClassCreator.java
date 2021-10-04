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
 * Authors: EB  = Eric Bruneton, Eric.Bruneton@rd.francetelecom.com
 *          JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 03-31-2004  EB   Inception based on BCELJavaClassCreator.
 * 04-05-2004  JH   First finished version; all test cases tested.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/

package com.judoscript.jamaica;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.lang.reflect.Modifier;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.CodeVisitor;
import org.objectweb.asm.Constants;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;

public final class ASMJavaClassCreator extends JavaClassCreator implements Constants
{
  private String      fileName;
  private int         accessFlags;
  private String      className;
  private String      superClassName;
  private String[]    interfaceNames;
  private Map         fieldTypes = new HashMap();
  private boolean     headerVisited;
  private int         methodAccessFlags;
  private String      methodName;
  private String      methodRetType;
  private int         locals;
  private boolean     lastInstIsReturn;
  private Map         varIndices    = new HashMap();
  private Map         varTypes      = new HashMap();
  private Map         labels        = new HashMap();
  private ClassWriter cv;
  private CodeVisitor mv;

  public ASMJavaClassCreator() {}
  
  public void setSourceFileName(String fileName) {
    this.fileName = fileName;
  }

  public String getSourceFileName() {
    return fileName;
  }

  public String getClassName() throws JavaClassCreatorException {
    return className;
  }

  public String getSuperclassName() throws JavaClassCreatorException {
    return superClassName;
  }

  public String[] getInterfaceNames() throws JavaClassCreatorException {
    return interfaceNames;
  }

  public void startClass(int accessFlags, String className, String superClassName, String[] itfNames)
    throws JavaClassCreatorException
  {
    this.accessFlags = accessFlags;
    this.className = className;
    this.superClassName = superClassName;
    this.interfaceNames = itfNames;
    
    cv = new ClassWriter(true);
    headerVisited = false;
  }

  public void startInterface(String className, String[] itfNames) throws JavaClassCreatorException {
    startClass(ACC_PUBLIC | ACC_INTERFACE, className, "java.lang.Object", itfNames);
  }

  public byte[] endClass() throws JavaClassCreatorException {
    if (!headerVisited) {
      int len = interfaceNames == null ? 0 : interfaceNames.length;
      String[] itfs = new String[len];
      for (int i=0; i < len; ++i) {
        itfs[i] = interfaceNames[i].replace('.', '/');
      }
      cv.visit(
        accessFlags, 
        toJVMClassName(className), 
        toJVMClassName(superClassName), 
        toJVMClassNames(itfs), 
        fileName);
      headerVisited = true;
    }

    fieldTypes.clear();
    fileName = null;
    accessFlags = 0;
    className = null;
    superClassName = null;
    interfaceNames = null;

    return cv.toByteArray();
  }

  public void addField(int accessFlags, String name, String type) throws JavaClassCreatorException {
    cv.visitField(accessFlags, name, stringToDescriptor(type), null, null);
    String isStaticPrefix = Modifier.isStatic(accessFlags) ? "?" : "";
    fieldTypes.put(name, isStaticPrefix + type);
  }

  public void addConstant(int accessFlags, String name, String type, Object value)
    throws JavaClassCreatorException
  {
    cv.visitField(accessFlags, name, stringToDescriptor(type), value, null);
    fieldTypes.put(name, "?" + type);
  }

  public String getFieldType(String name) throws JavaClassCreatorException {
    String typ = (String)fieldTypes.get(name);
    if (typ != null && typ.startsWith("?"))
      typ = typ.substring(1);
    return typ;
  }

  public boolean isStaticField(String name) throws JavaClassCreatorException {
    String typ = (String)fieldTypes.get(name);
    return typ != null && typ.startsWith("?");
  }

  public void addAbstractMethod(int accessFlags, String name, String[] argTypes,
                                String[] argNames, String returnType, String[] exceptionNames)
    throws JavaClassCreatorException
  {
    startMethod(accessFlags, name, argTypes, argNames, returnType, exceptionNames);
  }

  public void addDefaultConstructor(int accessFlags) throws JavaClassCreatorException {
    mv = cv.visitMethod(accessFlags, "<init>", "()V", null, null);
    mv.visitVarInsn(ALOAD, 0);
    mv.visitMethodInsn(INVOKESPECIAL, superClassName.replace('.', '/'), "<init>", "()V");
    mv.visitInsn(RETURN);
    mv.visitMaxs(0, 0);
  }

  public void startMethod(int accessFlags, String name, String[] argTypes, String[] argNames,
                          String returnType, String[] exceptionNames)
    throws JavaClassCreatorException
  {
    methodAccessFlags = accessFlags;
    methodName = name;
    methodRetType = returnType;
    lastInstIsReturn = false;
    
    locals = 0;
    if (!Modifier.isStatic(accessFlags)) {
      varIndices.put("this", new Integer(locals++));
      varTypes.put("this", className);
    }
    
    StringBuffer desc = new StringBuffer();
    desc.append('(');
    int len = argTypes == null ? 0 : argTypes.length;
    for (int i=0; i < len; ++i) {
      Type t = Type.getType(stringToDescriptor(argTypes[i]));
      desc.append(t.getDescriptor());
      varIndices.put(argNames[i], new Integer(locals));
      varTypes.put(argNames[i], argTypes[i]);
      locals += t.getSize();
    }
    desc.append(')');
    desc.append(stringToDescriptor(returnType));
    
    len = exceptionNames == null ? 0 : exceptionNames.length;
    String[] exceptions = new String[len];
    for (int i=0; i < len; ++i) {
      exceptions[i] = stringToDescriptor(exceptionNames[i]);
    }
    
    mv = cv.visitMethod(accessFlags, name, desc.toString(), exceptions, null);
  }

  public void endMethod() throws JavaClassCreatorException {
    if (!Modifier.isAbstract(methodAccessFlags) && methodRetType.equals("void") && !lastInstIsReturn)
      inst(177);

    mv.visitMaxs(0, 0);

    varIndices.clear();
    varTypes.clear();
    labels.clear();
    methodName = null;
    methodRetType = null;
  }

  public int getMethodAccessFlags() {
    return methodAccessFlags;
  }

  public String getMethodName() {
    return methodName;
  }

  public void addLocalVariable(String name, String type, Object init) throws JavaClassCreatorException {
    Type t = Type.getType(stringToDescriptor(type));
    varIndices.put(name, new Integer(locals));
    varTypes.put(name, type);
    locals += t.getSize();
    if (init != null)
      macroSet(name, init);
  }

  public int getLocalVariableIndex(String name) throws JavaClassCreatorException {
    return ((Integer)varIndices.get(name)).intValue();
  }

  public String getVariableType(String name) throws JavaClassCreatorException {
    return (String)varTypes.get(name);
  }

  public void addCatchClause(String type, String startLabel, String endLabel, String actionLabel)
    throws JavaClassCreatorException
  {
    // all labels should have been resolved!
    mv.visitTryCatchBlock(getLabel(startLabel), getLabel(endLabel), getLabel(actionLabel),
                          type==null ? "java/lang/Throwable" : type.replace('.', '/'));
  }

  public void setLabel(String label) throws JavaClassCreatorException {
    mv.visitLabel(getLabel(label));
  }

  public Label getLabel(String label) throws JavaClassCreatorException {
    if (label == null)
      throw new JavaClassCreatorException("Label cannot be null.");

    Label l = (Label)labels.get(label);
    if (l == null) {
      l = new Label();
      labels.put(label, l);
    }
    return l;
  }

  public void inst(int opcode) throws JavaClassCreatorException {
    lastInstIsReturn = opcode==RETURN;
    int base = 0;
    if (opcode >= 26 && opcode <= 45) // xLOADy
      base = 26;
    else if (opcode >= 59 && opcode <= 78) // xSTOREy
      base = 59;

    if (base > 0) {
      mv.visitVarInsn(base-5+(opcode-base)/4, (opcode-base)%4);
    } else {
      mv.visitInsn(opcode);
    }
  }

  public void instLoadStoreRet(int opcode, String varName) throws JavaClassCreatorException {
    lastInstIsReturn = false;
    mv.visitVarInsn(opcode, getLocalVariableIndex(varName));
  }

  public void instType(int opcode, String type) throws JavaClassCreatorException {
    lastInstIsReturn = false;
    if (opcode == NEWARRAY) {
      mv.visitIntInsn(opcode, getNewArrayType(type));
      return;
    }
    String desc = stringToDescriptor(type);
    if (desc.startsWith("L")) {
      desc = desc.substring(1, desc.length() - 1); 
    }
    mv.visitTypeInsn(opcode, desc);
  }

  public void instGetPut(int opcode, String clsName, String fldName, String type)
    throws JavaClassCreatorException
  {
    lastInstIsReturn = false;
    mv.visitFieldInsn(opcode, clsName.replace('.', '/'), fldName, stringToDescriptor(type));
  }

  public void instLdc(int opcode, Object constant, String type) throws JavaClassCreatorException {
    lastInstIsReturn = false;
    if (type != null) {
      if (type.equals("int")) {
        if (inst_smallInt(constant,true))
          return;
      }
      else if (type.equals("long")) {
        long l = 0;
        try {
          try {
            l = ((Number)constant).longValue();
          } catch(ClassCastException cce1) {
            try {
              l = ((Character)constant).charValue();
            } catch(ClassCastException cce2) {
              l = ((Boolean)constant).booleanValue() ? 1 : 0;
            }
          }
          if (l==0 || l==1) {
            inst(LCONST_0+(int)l);
            return;
          }
        } catch(Exception x) {}
      }
      else if (type.equals("float")) {
        float f = 0;
        try {
          try {
            f = ((Number)constant).floatValue();
          } catch(ClassCastException cce1) {
            try {
              f = ((Character)constant).charValue();
            } catch(ClassCastException cce2) {
              f = ((Boolean)constant).booleanValue() ? 1 : 0;
            }
          }
          if (f==0 || f==1 || f==2) {
            inst(FCONST_0+(int)f);
            return;
          }
        } catch(Exception x) {}
      }
      else if (type.equals("double")) {
        double d = 0;
        try {
          try {
            d = ((Number)constant).doubleValue();
          } catch(ClassCastException cce1) {
            try {
              d = ((Character)constant).charValue();
            } catch(ClassCastException cce2) {
              d = ((Boolean)constant).booleanValue() ? 1 : 0;
            }
          }
          if (d==0 || d==1) {
            inst(DCONST_0+(int)d);
            return;
          }
        } catch(Exception x) {}
      }
    }

    if (type == null && inst_smallInt(constant,false))
      return;

    mv.visitLdcInsn(checkConst(constant,type));
  }

  public void inst_bipush(byte value) throws JavaClassCreatorException {
    lastInstIsReturn = false;
    mv.visitIntInsn(BIPUSH, value);
  }

  public void inst_sipush(short value) throws JavaClassCreatorException {
    lastInstIsReturn = false;
    mv.visitIntInsn(SIPUSH, value);
  }

  public void inst_iinc(String varName, int inc) throws JavaClassCreatorException {
    lastInstIsReturn = false;
    mv.visitIincInsn(getLocalVariableIndex(varName), inc);
  }

  public void inst_multianewarray(String type, short dim) throws JavaClassCreatorException {
    lastInstIsReturn = false;
    mv.visitMultiANewArrayInsn(stringToDescriptor(type), dim);
  }

  public void instJump(int opcode, String label) throws JavaClassCreatorException {
    lastInstIsReturn = false;
    if (opcode == 200)      // GOTO_W automatically handled by ASM
      opcode = GOTO;
    else if (opcode == 201) // JSR_W automatically handled by ASM
      opcode = JSR;
    mv.visitJumpInsn(opcode, getLabel(label));
  }

  public void instInvoke(int opcode, String clsName, String mthdName, String[] paramTypes, String retType)
    throws JavaClassCreatorException
  {
    lastInstIsReturn = false;
    if (clsName == null)
      clsName = getClassName();

    StringBuffer desc = new StringBuffer();
    desc.append('(');
    int len = paramTypes==null ? 0 : paramTypes.length;
    for (int i=0; i<len; ++i)
      desc.append(stringToDescriptor(paramTypes[i]));
    desc.append(')');
    desc.append(stringToDescriptor(retType));
    mv.visitMethodInsn(opcode, clsName.replace('.', '/'), mthdName, desc.toString());
  }

  // tableswitch and lookupswitch names are inverted !!!
  
  public void inst_tableswitch(int[] consts, String[] labels, String defaultLabel)
    throws JavaClassCreatorException
  {
    lastInstIsReturn = false;
    int len = labels==null ? 0 : labels.length;
    Label[] lbls = new Label[len];
    for (int i=0; i<len; ++i)
      lbls[i] = getLabel(labels[i]);
    mv.visitLookupSwitchInsn(getLabel(defaultLabel), consts, lbls);
  }

  public void inst_lookupswitch(int startConst, String[] labels, String defaultLabel)
    throws JavaClassCreatorException
  {
    lastInstIsReturn = false;
    int len = labels==null ? 0 : labels.length;
    Label[] lbls = new Label[len];
    for (int i=0; i<len; ++i)
      lbls[i] = getLabel(labels[i]);
    mv.visitTableSwitchInsn(startConst, startConst+labels.length-1, getLabel(defaultLabel), lbls);
  }

  public static Object checkConst(Object val, String type) {
    if (type == null) {
      if (val instanceof Integer || val instanceof Long ||
          val instanceof Float || val instanceof Double)
        return val;
      if (val instanceof Number)
        return new Integer(((Number)val).intValue());
      if (val instanceof Boolean)
        return new Integer(((Boolean)val).booleanValue() ? 1 : 0);
      if (val instanceof Character)
        return new Integer(((Character)val).charValue());

      return val.toString();
    }

    int tcode = 0;
    if (type.equals("boolean"))     tcode = 8;
    else if (type.equals("byte"))   tcode = 7;
    else if (type.equals("char"))   tcode = 6;
    else if (type.equals("short"))  tcode = 5;
    else if (type.equals("int"))    tcode = 4;
    else if (type.equals("long"))   tcode = 3;
    else if (type.equals("float"))  tcode = 2;
    else if (type.equals("double")) tcode = 1;

    if (tcode == 0) // String
      return val.toString();

    if (val instanceof Number) {
      switch(tcode) {
      case 1: // double
        return new Double(((Number)val).doubleValue());
      case 2: // float
        return new Float(((Number)val).floatValue());
      case 3: // long
        return new Long(((Number)val).longValue());
      default: // int, short, char, byte, boolean
        return new Integer(((Number)val).intValue());
      }
    }

    int i=0;
    if (val instanceof Character)
      i = ((Character)val).charValue();
    else if (val instanceof Boolean)
      i = ((Boolean)val).booleanValue() ? 1 : 0;
    switch(tcode) {
    case 1: // double
      return new Double(i);
    case 2: // float
      return new Float(i);
    case 3: // long
      return new Long(i);
    default: // int, short, char, byte, boolean
      return new Integer(i);
    }
  }

  public static int getNewArrayType(String type) throws JavaClassCreatorException {
    try {
      if (type.equals("boolean")) return 4;
      if (type.equals("char"))    return 5;
      if (type.equals("float"))   return 6;
      if (type.equals("double"))  return 7;
      if (type.equals("byte"))    return 8;
      if (type.equals("short"))   return 9;
      if (type.equals("int"))     return 10;
      if (type.equals("long"))    return 11;
    } catch(Exception e) {}
    throw new JavaClassCreatorException("Invalid type for newarray instruction: " + type);
  }

} // end of class.
