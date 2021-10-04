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
 * 04-01-2004  JH   Added checking on ending return instruction
 *                  for non-abstract void methods. This used to
 *                  be done by the language and lacks in the API.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/

package com.judoscript.jamaica;

import java.lang.reflect.Modifier;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

import org.apache.bcel.*;
import org.apache.bcel.generic.*;
import org.apache.bcel.classfile.*;

public final class BCELJavaClassCreator extends JavaClassCreator implements Constants
{
  /*************************************************
   * Data Members For Class File Generation
   *************************************************/

  private String sourceFileName = "<unknown_file>";
  private ClassGen classGen = null;
  private ConstantPoolGen cpGen = null;

  private InstructionList initializer = null;

  // method generation data members:

  private static final String firstLabel = "?l??0?";
  private int      methodAccessFlags;
  private String   methodName;
  private String[] methodArgTypes; private String[] methodArgNames;
  private String   methodRetType;
  private String[] methodExceptionNames;
  private boolean  lastInstIsReturn; // <04-01-2004 mods>

  private InstructionList instList = new InstructionList();
  private int       localVarStart = 0; // to discounts the parameters
  private ArrayList localVarList = new ArrayList();
  private HashMap   localVarTypes = new HashMap();
  private HashMap   labels = new HashMap();
  private String    lastLabel = null;
  private HashMap   unresolvedJumps = new HashMap();
  private HashMap   switchPlaceholders = new HashMap();
  private ArrayList catchClauses = new ArrayList();

  private void reinitMethod() {
    methodAccessFlags = 0;
    methodName = null;
    methodArgTypes = null;
    methodArgNames = null;
    methodRetType = null;
    methodExceptionNames = null;
    lastInstIsReturn = false;

    instList.dispose();
    localVarStart = 0;
    localVarList.clear();
    localVarTypes.clear();
    labels.clear();
    lastLabel = null;
    unresolvedJumps.clear();
    switchPlaceholders.clear();
    catchClauses.clear();
  }

  public InstructionHandle addInst(Instruction i) {
    lastInstIsReturn = i.getOpcode() == RETURN;

    InstructionHandle ih;
    if (i instanceof BranchInstruction)
      ih = instList.append((BranchInstruction)i);
    else
      ih = instList.append(i);

    if (lastLabel != null) {
      labels.put(lastLabel, ih);
      lastLabel = null;
    }

    return ih;
  }

  public int addConst(Object val, String type) {
    if (type == null) {
      if (val instanceof Float)
        return cpGen.addFloat(((Number)val).floatValue());
      if (val instanceof Double)
        return cpGen.addDouble(((Number)val).doubleValue());
      if (val instanceof Long)
        return cpGen.addLong(((Number)val).longValue());
      if (val instanceof Number)
        return cpGen.addInteger(((Number)val).intValue());
      if (val instanceof Boolean)
        return cpGen.addInteger(((Boolean)val).booleanValue() ? 1 : 0);
      if (val instanceof Character)
        return cpGen.addInteger(((Character)val).charValue());
      return cpGen.addString(val.toString());
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
    else if (type.equals("utf8"))   return cpGen.addUtf8(val.toString());

    if (tcode == 0) // String
      return cpGen.addString(val.toString());

    if (val instanceof Number) {
      switch(tcode) {
      case 1: // double
        return cpGen.addDouble(((Number)val).doubleValue());
      case 2: // float
        return cpGen.addFloat(((Number)val).floatValue());
      case 3: // long
        return cpGen.addLong(((Number)val).longValue());
      default: // int, short, char, byte, boolean
        return cpGen.addInteger(((Number)val).intValue());
      }
    }

    int i = 0;
    if (val instanceof Character)
      i = ((Character)val).charValue();
    else if (val instanceof Boolean)
      i = ((Boolean)val).booleanValue() ? 1 : 0;
    switch(tcode) {
    case 1: // double
      return cpGen.addDouble(i);
    case 2: // float
      return cpGen.addFloat(i);
    case 3: // long
      return cpGen.addLong(i);
    default: // int, short, char, byte, boolean
      return cpGen.addInteger(i);
    }
  }
 
  /*************************************************
   * JavaClassCreator Methods
   *************************************************/

  public void setSourceFileName(String fileName) {
    sourceFileName = fileName;
  }

  public String getSourceFileName() {
    return sourceFileName;
  }

  public String getClassName() throws JavaClassCreatorException {
    try {
      return classGen.getClassName().replace('/','.');
    } catch(Exception e) {
      throw new JavaClassCreatorException(e.getMessage());
    }
  }

  public String getSuperclassName() throws JavaClassCreatorException {
    try {
      String ret = classGen.getSuperclassName();
      if (ret == null) return null;
      return ret.replace('/','.');
    } catch(Exception e) {
      throw new JavaClassCreatorException(e.getMessage());
    }
  }

  public String[] getInterfaceNames() throws JavaClassCreatorException {
    try {
      String[] sa = classGen.getInterfaceNames();
      int len = sa==null ? 0 : sa.length;
      for (int i=0; i<len; ++i)
        sa[i] = sa[i].replace('/','.');
      return sa;
    } catch(Exception e) {
      throw new JavaClassCreatorException(e.getMessage());
    }
  }

  public void startClass(int accessFlags, String clsName, String superName, String[] itfNames) {
    classGen = new ClassGen(toJVMClassName(clsName), toJVMClassName(superName),
                            sourceFileName, accessFlags, toJVMClassNames(itfNames));
    cpGen = classGen.getConstantPool();
  }

  public void startInterface(String clsName, String[] itfNames) {
    startClass(Modifier.ABSTRACT | Modifier.PUBLIC | Modifier.INTERFACE,
               toJVMClassName(clsName), null, toJVMClassNames(itfNames));
  }

  public byte[] endClass() throws JavaClassCreatorException{
    byte[] ret = classGen.getJavaClass().getBytes();

    classGen = null;
    cpGen = null;
    initializer = null;
    reinitMethod();

    return ret;
  }

  public void addField(int accessFlags, String name, String type) throws JavaClassCreatorException {
    FieldGen fg = new FieldGen(accessFlags, stringToType(type), name, cpGen);
    classGen.addField(fg.getField());
  }

  public void addConstant(int accessFlags, String name, String type, Object value)
    throws JavaClassCreatorException
  {
    FieldGen fg = new FieldGen(accessFlags, stringToType(type), name, cpGen);
    fg.addAttribute(
      new ConstantValue(addConst("ConstantValue","utf8"), 2, addConst(value,type), cpGen.getConstantPool()));
    classGen.addField(fg.getField());
  }

  public String getFieldType(String name) {
    try {
      return Utility.signatureToString(classGen.containsField(name).getSignature(), false);
    } catch(Exception e) {
      return null;
    }
  }

  public boolean isStaticField(String name) {
    try {
      return classGen.containsField(name).isStatic();
    } catch(Exception e) {
      return false;
    }
  }

  public void addAbstractMethod(int accessFlags, String name, String[] argTypes, String[] argNames,
                                String retType, String[] exceptionNames) throws JavaClassCreatorException
  {
    MethodGen mg = new MethodGen(accessFlags | ACC_ABSTRACT, stringToType(retType),
                                 stringsToTypes(argTypes), argNames,
                                 name, classGen.getClassName(), null, cpGen);
    int len = exceptionNames == null ? 0 : exceptionNames.length;
    for (int i=0; i<len; ++i)
      mg.addException(exceptionNames[i]);
    classGen.addMethod(mg.getMethod());
  }

  public void addDefaultConstructor(int accessFlags) throws JavaClassCreatorException {
    classGen.addEmptyConstructor(accessFlags);
  }

  /**
   * Starts a new method construction process.
   * Must be called after <code>startClass()</code> and before <code>endClass()</code>.
   */
  public void startMethod(int accessFlags, String name, String[] argTypes, String[] argNames,
                          String retType, String[] exceptionNames) throws JavaClassCreatorException
  {
    methodAccessFlags = accessFlags;
    methodName = name;
    methodArgTypes = argTypes;
    methodArgNames = argNames;
    methodRetType = retType;
    methodExceptionNames = exceptionNames;
    lastInstIsReturn = false;

    instList.dispose();
    labels.clear();
    unresolvedJumps.clear();
    localVarList.clear();
    localVarTypes.clear();

    localVarStart = argTypes == null ? 0 : argTypes.length;
    for (int i=0; i<localVarStart; ++i)
      addLocalVariable(argNames[i], argTypes[i], null);

    setLabel(firstLabel);
  }

  public void endMethod() throws JavaClassCreatorException {
    int i;

    // Check for ending return
    if (!Modifier.isAbstract(methodAccessFlags) && methodRetType.equals("void") && !lastInstIsReturn)
      inst(177);

    // Resolve unsettled jump targets
    if (unresolvedJumps.size() > 0) {
      Iterator iter = unresolvedJumps.keySet().iterator();
      while (iter.hasNext()) {
        BranchHandle branch = (BranchHandle)iter.next();
        String label = (String)unresolvedJumps.get(branch);
        branch.setTarget((InstructionHandle)labels.get(label));
      }
    }

    // Resolve unsettled switch targets -- see inst_switch()
    Iterator iter = switchPlaceholders.keySet().iterator();
    while (iter.hasNext()) {
      InstructionHandle placeholder = (InstructionHandle)iter.next();
      InstructionHandle target = (InstructionHandle)labels.get((String)switchPlaceholders.get(placeholder));
      instList.redirectBranches(placeholder, target);
      try { instList.delete(placeholder); } catch(Exception e) {}
    }

    MethodGen mg = new MethodGen(methodAccessFlags, stringToType(methodRetType),
                                 stringsToTypes(methodArgTypes), methodArgNames,
                                 methodName, classGen.getClassName(), instList, cpGen);

    // Add exceptions
    int len = methodExceptionNames == null ? 0 : methodExceptionNames.length;
    for (i=0; i<len; ++i)
      mg.addException(methodExceptionNames[i]);

    // Add local variables
    len = localVarList.size();
    for (i=localVarStart; i<len; ++i) {
      String name = (String)localVarList.get(i);
      mg.addLocalVariable(name, (Type)localVarTypes.get(name), null, null);
    }

    // Resolve exception handler targets
    len = catchClauses.size();
    for (i=0; i<len; ++i) {
      String[] hdlr = (String[])catchClauses.get(i); // type, startLabel, endLabel, actionLabel
      InstructionHandle start  = (InstructionHandle)labels.get(hdlr[1]);
      InstructionHandle end    = (InstructionHandle)labels.get(hdlr[2]);
      InstructionHandle action = (InstructionHandle)labels.get(hdlr[3]);
      if (start == null || end == null || action == null) {
        String x = null;
        if (start == null)  x = hdlr[1];
        if (end == null)    x = hdlr[2];
        if (action == null) x = hdlr[3];
        throw new JavaClassCreatorException("Exception label '" + x + "' is not found.");
      }
      mg.addExceptionHandler(start, end, action, (ObjectType)stringToType(hdlr[0]));
    }

    // Finalize
    mg.setMaxStack();
    classGen.addMethod(mg.getMethod());

    reinitMethod();
  }

  public int getMethodAccessFlags() { return methodAccessFlags; }

  public String getMethodName() { return methodName; }

  public void addLocalVariable(String name, String type, Object init) throws JavaClassCreatorException {
    localVarList.add(name);
    localVarTypes.put(name, stringToType(type));
    if (init != null)
      macroSet(name, init);
  }

  public int getLocalVariableIndex(String name) throws JavaClassCreatorException {
    if ("this".equals(name))
      return 0;
    int i, idx = Modifier.isStatic(methodAccessFlags) ? 0 : 1;
    for (i=0; i<localVarList.size(); ++i) {
      String s = (String)localVarList.get(i);
      if (s.equals(name))
        break;
      idx += ((Type)localVarTypes.get(s)).getSize();
    }
    if (i >= localVarList.size())
      throw new JavaClassCreatorException("Variable " + name + " is not found.");
    return idx;
  }

  public String getVariableType(String name) {
    try {
      return name.equals("this") ? getClassName() : ((Type)localVarTypes.get(name)).toString();
    } catch(Exception e) {
      return null;
    }
  }

  public void addCatchClause(String type, String startLabel, String endLabel, String actionLabel) {
    catchClauses.add(new String[]{ type, startLabel, endLabel, actionLabel });
  }

  public void setLabel(String label) {
    lastLabel = label;
  }

  public void inst(int opcode) throws JavaClassCreatorException {
    Instruction i = singletons[opcode];
    if (i == null)
      throw new JavaClassCreatorException("Opcode " + opcode + " takes parameters.");
    addInst(i);
  }

  public void instLoadStoreRet(int opcode, String varName) throws JavaClassCreatorException {
    int idx = getLocalVariableIndex(varName);
    Instruction inst = null;
    switch(opcode) {
    case ILOAD:  inst = new ILOAD(idx);  break;
    case LLOAD:  inst = new LLOAD(idx);  break;
    case FLOAD:  inst = new FLOAD(idx);  break;
    case DLOAD:  inst = new DLOAD(idx);  break;
    case ALOAD:  inst = new ALOAD(idx);  break;
    case ISTORE: inst = new ISTORE(idx); break;
    case LSTORE: inst = new LSTORE(idx); break;
    case FSTORE: inst = new FSTORE(idx); break;
    case DSTORE: inst = new DSTORE(idx); break;
    case ASTORE: inst = new ASTORE(idx); break;
    case RET:    inst = new RET(idx);    break;
    default:     badOpcode(opcode, "load/store/ret");
    }
    addInst(inst);
  }

  public void instType(int opcode, String type) throws JavaClassCreatorException {
    Instruction inst = null;
    switch(opcode) {
    case NEW:        inst = new NEW(cpGen.addClass(toJVMClassName(type))); break;
    case ANEWARRAY:  inst = new ANEWARRAY(cpGen.addClass(toJVMClassName(type))); break;
    case NEWARRAY:   inst = new NEWARRAY(getNewArrayType(type)); break;
    case CHECKCAST:  inst = new CHECKCAST(cpGen.addClass(stringToDescriptor(type))); break;
    case INSTANCEOF: inst = new INSTANCEOF(cpGen.addClass(stringToDescriptor(type))); break;
    default:         badOpcode(opcode, "type-related");
    }
    addInst(inst);
  }

  public void instGetPut(int opcode, String clsName, String fldName, String type)
    throws JavaClassCreatorException
  {
    int idx = cpGen.addFieldref(clsName==null ? classGen.getClassName() : toJVMClassName(clsName),
                                fldName, stringToDescriptor(type));
    Instruction inst = null;
    switch(opcode) {
    case GETSTATIC: inst = new GETSTATIC(idx); break;
    case PUTSTATIC: inst = new PUTSTATIC(idx); break;
    case GETFIELD:  inst = new GETFIELD(idx);  break;
    case PUTFIELD:  inst = new PUTFIELD(idx);  break;
    default:        badOpcode(opcode, "get/put");
    }
    addInst(inst);
  }

  public void instLdc(int opcode, Object constant, String type) throws JavaClassCreatorException {
    Instruction inst = null;
    if (type != null) {
      if (type.equals("int")) {
        if (inst_smallInt(constant,true))
          return;
      }
      else if (type.equals("long")) {
        opcode = LDC2_W;
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
            inst = singletons[LCONST_0+(int)l];
            opcode = 0;
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
            inst = singletons[FCONST_0+(int)f];
            opcode = 0;
          }
        } catch(Exception x) {}
      }
      else if (type.equals("double")) {
        opcode = LDC2_W;
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
            inst = singletons[DCONST_0+(int)d];
            opcode = 0;
          }
        } catch(Exception x) {}
      }
    }

    if (opcode != 0) {
      if (type == null && inst_smallInt(constant,false))
        return;

      int idx = addConst(constant, type);
      switch(opcode) {
      case LDC:    inst = new LDC(idx); break;
      case LDC_W:  inst = new LDC_W(idx); break;
      case LDC2_W: inst = new LDC2_W(idx); break;
      default:     badOpcode(opcode, "ldc");
      }
    }
    addInst(inst);
  }

  public void inst_bipush(byte value) {
    addInst(new BIPUSH(value));
  }

  public void inst_sipush(short value) {
    addInst(new SIPUSH(value));
  }

  public void inst_iinc(String varName, int inc) throws JavaClassCreatorException {
    addInst(new IINC(getLocalVariableIndex(varName), inc));
  }

  public void inst_multianewarray(String type, short dim) {
    addInst(new MULTIANEWARRAY(cpGen.addClass(stringToDescriptor(type)), dim));
  }

  public void instJump(int opcode, String label) throws JavaClassCreatorException {
    InstructionHandle target = (InstructionHandle)labels.get(label);

    BranchInstruction inst = null;
    switch(opcode) {
    case IFEQ:      inst = new IFEQ(target);      break;
    case IFNE:      inst = new IFNE(target);      break;
    case IFLT:      inst = new IFLT(target);      break;
    case IFGE:      inst = new IFGE(target);      break;
    case IFGT:      inst = new IFGT(target);      break;
    case IFLE:      inst = new IFLE(target);      break;
    case IF_ICMPEQ: inst = new IF_ICMPEQ(target); break;
    case IF_ICMPNE: inst = new IF_ICMPNE(target); break;
    case IF_ICMPLT: inst = new IF_ICMPLT(target); break;
    case IF_ICMPGE: inst = new IF_ICMPGE(target); break;
    case IF_ICMPGT: inst = new IF_ICMPGT(target); break;
    case IF_ICMPLE: inst = new IF_ICMPLE(target); break;
    case IF_ACMPEQ: inst = new IF_ACMPEQ(target); break;
    case IF_ACMPNE: inst = new IF_ACMPNE(target); break;
    case GOTO:      inst = new GOTO(target);      break;
    case JSR:       inst = new JSR(target);       break;
    case IFNULL:    inst = new IFNULL(target);    break;
    case IFNONNULL: inst = new IFNONNULL(target); break;
    case GOTO_W:    inst = new GOTO_W(target);    break;
    case JSR_W:     inst = new JSR_W(target);     break;
    default:        badOpcode(opcode, "jump");
    }
    InstructionHandle ih = addInst(inst);

    if (target == null)
      unresolvedJumps.put(ih, label);
  }

  public void instInvoke(int opcode, String clsName, String mthdName, String[] paramTypes, String retType)
    throws JavaClassCreatorException
  {
    String sig = Utility.methodTypeToSignature(retType, paramTypes);
    clsName = clsName==null ? classGen.getClassName() : toJVMClassName(clsName);
    int idx = opcode==INVOKEINTERFACE ? cpGen.addInterfaceMethodref(clsName, mthdName, sig)
                                      : cpGen.addMethodref(clsName, mthdName, sig);
    Instruction inst = null;
    switch(opcode) {
    case INVOKEVIRTUAL:   inst = new INVOKEVIRTUAL(idx); break;
    case INVOKESPECIAL:   inst = new INVOKESPECIAL(idx); break;
    case INVOKESTATIC:    inst = new INVOKESTATIC(idx);  break;
    case INVOKEINTERFACE: inst = new INVOKEINTERFACE(idx, getParamSizePlus1(sig)); break;
    default:              badOpcode(opcode, "invoke");
    }
    addInst(inst);
  }

  public void inst_tableswitch(int[] consts, String[] caseLabels, String defaultLabel)
    throws JavaClassCreatorException
  {
    // BCEL API requires InstructionHandle's for the labels, which
    // are probably not available when this instruction is called.
    //
    // The solution is to create placeholder NOPs first for the labels here,
    // and in endMethod() redirect them to the real targets and the NOPs be
    // deleted.

    int len = caseLabels.length;
    InstructionHandle[] casePCs = new InstructionHandle[len];
    for (int i=0; i<len; ++i) {
      casePCs[i] = instList.append(singletons[NOP]);
      switchPlaceholders.put(casePCs[i], caseLabels[i]);
    }
    InstructionHandle defLabel = instList.append(singletons[NOP]);
    switchPlaceholders.put(defLabel, defaultLabel);

    addInst(new SWITCH(consts, casePCs, defLabel).getInstruction());
  }

  public void inst_lookupswitch(int startConst, String[] caseLabels, String defaultLabel)
    throws JavaClassCreatorException
  {
    int[] consts = new int[caseLabels.length];
    for (int i=0; i<consts.length; i++)
      consts[i] = startConst++;
    inst_switch(consts, caseLabels, defaultLabel);
  }

  /*************************************************
   * Singletons
   *************************************************/

  static final Instruction singletons[];

  static {
    singletons = new Instruction[256];

    singletons[NOP         ] = new NOP();
    singletons[ACONST_NULL ] = new ACONST_NULL();
    singletons[ICONST_M1   ] = new ICONST(-1);
    singletons[ICONST_0    ] = new ICONST(0);
    singletons[ICONST_1    ] = new ICONST(1);
    singletons[ICONST_2    ] = new ICONST(2);
    singletons[ICONST_3    ] = new ICONST(3);
    singletons[ICONST_4    ] = new ICONST(4);
    singletons[ICONST_5    ] = new ICONST(5);
    singletons[LCONST_0    ] = new LCONST(0);
    singletons[LCONST_1    ] = new LCONST(1);
    singletons[FCONST_0    ] = new FCONST(0);
    singletons[FCONST_1    ] = new FCONST(1);
    singletons[FCONST_2    ] = new FCONST(2);
    singletons[DCONST_0    ] = new DCONST(0);
    singletons[DCONST_1    ] = new DCONST(1);
    singletons[ILOAD_0     ] = new ILOAD(0);
    singletons[ILOAD_1     ] = new ILOAD(1);
    singletons[ILOAD_2     ] = new ILOAD(2);
    singletons[ILOAD_3     ] = new ILOAD(3);
    singletons[LLOAD_0     ] = new LLOAD(0);
    singletons[LLOAD_1     ] = new LLOAD(1);
    singletons[LLOAD_2     ] = new LLOAD(2);
    singletons[LLOAD_3     ] = new LLOAD(3);
    singletons[FLOAD_0     ] = new FLOAD(0);
    singletons[FLOAD_1     ] = new FLOAD(1);
    singletons[FLOAD_2     ] = new FLOAD(2);
    singletons[FLOAD_3     ] = new FLOAD(3);
    singletons[DLOAD_0     ] = new DLOAD(0);
    singletons[DLOAD_1     ] = new DLOAD(1);
    singletons[DLOAD_2     ] = new DLOAD(2);
    singletons[DLOAD_3     ] = new DLOAD(3);
    singletons[ALOAD_0     ] = new ALOAD(0);
    singletons[ALOAD_1     ] = new ALOAD(1);
    singletons[ALOAD_2     ] = new ALOAD(2);
    singletons[ALOAD_3     ] = new ALOAD(3);
    singletons[IALOAD      ] = new IALOAD();
    singletons[LALOAD      ] = new LALOAD();
    singletons[FALOAD      ] = new FALOAD();
    singletons[DALOAD      ] = new DALOAD();
    singletons[AALOAD      ] = new AALOAD();
    singletons[BALOAD      ] = new BALOAD();
    singletons[CALOAD      ] = new CALOAD();
    singletons[SALOAD      ] = new SALOAD();
    singletons[ISTORE_0    ] = new ISTORE(0);
    singletons[ISTORE_1    ] = new ISTORE(1);
    singletons[ISTORE_2    ] = new ISTORE(2);
    singletons[ISTORE_3    ] = new ISTORE(3);
    singletons[LSTORE_0    ] = new LSTORE(0);
    singletons[LSTORE_1    ] = new LSTORE(1);
    singletons[LSTORE_2    ] = new LSTORE(2);
    singletons[LSTORE_3    ] = new LSTORE(3);
    singletons[FSTORE_0    ] = new FSTORE(0);
    singletons[FSTORE_1    ] = new FSTORE(1);
    singletons[FSTORE_2    ] = new FSTORE(2);
    singletons[FSTORE_3    ] = new FSTORE(3);
    singletons[DSTORE_0    ] = new DSTORE(0);
    singletons[DSTORE_1    ] = new DSTORE(1);
    singletons[DSTORE_2    ] = new DSTORE(2);
    singletons[DSTORE_3    ] = new DSTORE(3);
    singletons[ASTORE_0    ] = new ASTORE(0);
    singletons[ASTORE_1    ] = new ASTORE(1);
    singletons[ASTORE_2    ] = new ASTORE(2);
    singletons[ASTORE_3    ] = new ASTORE(3);
    singletons[IASTORE     ] = new IASTORE();
    singletons[LASTORE     ] = new LASTORE();
    singletons[FASTORE     ] = new FASTORE();
    singletons[DASTORE     ] = new DASTORE();
    singletons[AASTORE     ] = new AASTORE();
    singletons[BASTORE     ] = new BASTORE();
    singletons[CASTORE     ] = new CASTORE();
    singletons[SASTORE     ] = new SASTORE();
    singletons[POP         ] = new POP();
    singletons[POP2        ] = new POP2();
    singletons[DUP         ] = new DUP();
    singletons[DUP_X1      ] = new DUP_X1();
    singletons[DUP_X2      ] = new DUP_X2();
    singletons[DUP2        ] = new DUP2();
    singletons[DUP2_X1     ] = new DUP2_X1();
    singletons[DUP2_X2     ] = new DUP2_X2();
    singletons[SWAP        ] = new SWAP();
    singletons[IADD        ] = new IADD();
    singletons[LADD        ] = new LADD();
    singletons[FADD        ] = new FADD();
    singletons[DADD        ] = new DADD();
    singletons[ISUB        ] = new ISUB();
    singletons[LSUB        ] = new LSUB();
    singletons[FSUB        ] = new FSUB();
    singletons[DSUB        ] = new DSUB();
    singletons[IMUL        ] = new IMUL();
    singletons[LMUL        ] = new LMUL();
    singletons[FMUL        ] = new FMUL();
    singletons[DMUL        ] = new DMUL();
    singletons[IDIV        ] = new IDIV();
    singletons[LDIV        ] = new LDIV();
    singletons[FDIV        ] = new FDIV();
    singletons[DDIV        ] = new DDIV();
    singletons[IREM        ] = new IREM();
    singletons[LREM        ] = new LREM();
    singletons[FREM        ] = new FREM();
    singletons[DREM        ] = new DREM();
    singletons[INEG        ] = new INEG();
    singletons[LNEG        ] = new LNEG();
    singletons[FNEG        ] = new FNEG();
    singletons[DNEG        ] = new DNEG();
    singletons[ISHL        ] = new ISHL();
    singletons[LSHL        ] = new LSHL();
    singletons[ISHR        ] = new ISHR();
    singletons[LSHR        ] = new LSHR();
    singletons[IUSHR       ] = new IUSHR();
    singletons[LUSHR       ] = new LUSHR();
    singletons[IAND        ] = new IAND();
    singletons[LAND        ] = new LAND();
    singletons[IOR         ] = new IOR();
    singletons[LOR         ] = new LOR();
    singletons[IXOR        ] = new IXOR();
    singletons[LXOR        ] = new LXOR();
    singletons[I2L         ] = new I2L();
    singletons[I2F         ] = new I2F();
    singletons[I2D         ] = new I2D();
    singletons[L2I         ] = new L2I();
    singletons[L2F         ] = new L2F();
    singletons[L2D         ] = new L2D();
    singletons[F2I         ] = new F2I();
    singletons[F2L         ] = new F2L();
    singletons[F2D         ] = new F2D();
    singletons[D2I         ] = new D2I();
    singletons[D2L         ] = new D2L();
    singletons[D2F         ] = new D2F();
    singletons[I2B         ] = new I2B();
    singletons[I2C         ] = new I2C();
    singletons[I2S         ] = new I2S();
    singletons[LCMP        ] = new LCMP();
    singletons[FCMPL       ] = new FCMPL();
    singletons[FCMPG       ] = new FCMPG();
    singletons[DCMPL       ] = new DCMPL();
    singletons[DCMPG       ] = new DCMPG();
    singletons[IRETURN     ] = new IRETURN();
    singletons[LRETURN     ] = new LRETURN();
    singletons[FRETURN     ] = new FRETURN();
    singletons[DRETURN     ] = new DRETURN();
    singletons[ARETURN     ] = new ARETURN();
    singletons[RETURN      ] = new RETURN();
    singletons[ARRAYLENGTH ] = new ARRAYLENGTH();
    singletons[ATHROW      ] = new ATHROW();
    singletons[MONITORENTER] = new MONITORENTER();
    singletons[MONITOREXIT ] = new MONITOREXIT();
  }

  /*************************************************
   * Helpers
   *************************************************/

  public void badOpcode(int opcode, String expectedType) throws JavaClassCreatorException {
    throw new JavaClassCreatorException("Internal error: opcode " + opcode +
      " (" + getMnemonic(opcode) + ") is not a " + expectedType + " instruction.");
  }

  public static String getMnemonic(int opcode) {
    try {
      String ret = OPCODE_NAMES[opcode];
      if (!ret.equals(ILLEGAL_OPCODE))
        return ret;
    } catch(Exception e) {}
    return "???";
  }

  public static byte getNewArrayType(String type) throws JavaClassCreatorException {
    try {
      if (type.equals("boolean")) return (byte)4;
      if (type.equals("char"))    return (byte)5;
      if (type.equals("float"))   return (byte)6;
      if (type.equals("double"))  return (byte)7;
      if (type.equals("byte"))    return (byte)8;
      if (type.equals("short"))   return (byte)9;
      if (type.equals("int"))     return (byte)10;
      if (type.equals("long"))    return (byte)11;
    } catch(Exception e) {}
    throw new JavaClassCreatorException("Invalid type for newarray instruction: " + type);
  }

  private static HashMap classTypes = new HashMap();

  /**
   *@param type is like <code>int String java.lang.String String[]</code> etc.
   */
  public Type stringToType(String type) {
    try {
      type = stringToDescriptor(type);
      Type ret = (Type)classTypes.get(type);
      if (ret == null) {
        ret = Type.getType(type);
        classTypes.put(type, ret);
      }
      return ret;
    } catch(NullPointerException npe) {
      return Type.VOID;
    }
  }

  public Type[] stringsToTypes(String[] types) {
    int len = types==null ? 0 : types.length;
    Type[] ret = new Type[len];
    for (int i=0; i<len; ++i)
      ret[i] = stringToType(types[i]);
    return ret;
  }

  public int getParamSizePlus1(String methodSig) {
    int x = methodSig.lastIndexOf(')');
    StringTokenizer st = new StringTokenizer(methodSig.substring(1, x));
    x = 1;
    while (st.hasMoreTokens()) {
      String s = st.nextToken();
      x += s.equals("L") || s.equals("D") ? 2 : 1;
    }
    return x;
  }

} // end of class.
