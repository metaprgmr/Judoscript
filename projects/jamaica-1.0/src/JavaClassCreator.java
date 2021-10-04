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
 * 04-01-2004  JH   Added factory method getJavaClassCreator().
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/

package com.judoscript.jamaica;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.lang.reflect.Modifier;
import org.apache.commons.lang.ClassUtils;

/**
 * A convenient interface for creating Java classes with byte codes.
 * Refer to jamaica as this closely models that language.
 *<p>
 * All type names are in Java format, i.e., <code>java.lang.String</code>.
 * Class names must be fully-qualified class names.
 *<p>
 * The accessFlags used in this API are the logical-OR of constants
 * defined in <code>java.lang.reflect.Modifier</code>.
 */
public abstract class JavaClassCreator
{
  /////////////////////////////////////////////////////////////////
  //
  // Factory Method
  //
  /////////////////////////////////////////////////////////////////

  public static JavaClassCreator getJavaClassCreator() {
    try {
      String cc = System.getProperty("CreatorClass");
      if (cc == null) {
        if (MyUtils.existsClass("org.objectweb.asm.Constants"))
          cc = "com.judoscript.jamaica.ASMJavaClassCreator";
        else if (MyUtils.existsClass("org.apache.bcel.classfile.Constant"))
          cc = "com.judoscript.jamaica.BCELJavaClassCreator";
      }

      if (cc != null) {
        System.err.println("(JavaClassCreator implementation is " + cc + ")");
        return (JavaClassCreator)MyUtils.newInstance(cc);
      }
    } catch(Exception e) {
      e.printStackTrace();
    }
    System.err.println(
      "No JavaClassCreator implementation is found/specified.\n" +
      "Jamaica JavaClassCreator API has built-in support for ASM and BCEL only.\n" +
      "Download one of them and install: for ASM, http://asm.objectweb.org\n" +
      "and for BCEL, http://jakarta.apache.org\n" +
      "Simply put their binary class files into the classpath.\n\n" +
      "To provide your own implementation of JavaClassCreator, set the\n" +
      "system property CreatorClass on the java command line like this:\n\n" +
      "  java -DClassCreator=mypkg.MyJavaClassCreator the_main_class\n\n");
    return null;
  }

  

  /////////////////////////////////////////////////////////////////
  //
  // Class Creation
  //
  /////////////////////////////////////////////////////////////////

  /**
   * Sets the source file name as the SourceFile attribute for the class.
   */
  public abstract void setSourceFileName(String fileName);

  public abstract String getSourceFileName();

  /**
   * Must be called after <code>startClass()</code>
   * @return the complete class name.
   */
  public abstract String getClassName() throws JavaClassCreatorException;

  public abstract String getSuperclassName() throws JavaClassCreatorException;

  public abstract String[] getInterfaceNames() throws JavaClassCreatorException;

  /**
   * Must be called after <code>startClass()</code>
   * @return the last in the class name.
   */
  public String getClassRootName() throws JavaClassCreatorException {
    String clsName = getClassName();
    int idx = clsName.lastIndexOf('.');
    return idx < 0 ? clsName : clsName.substring(idx+1);
  }

  /**
   * Starts a new class creation process. The class is created by a call to either
   * <code>endClass()</code> or <code>endClassToFile()</code>.
   *
   * @param className should be a simple class name without package prefix.
   */
  public abstract void startClass(int accessFlags, String className, String superClassName, String[] itfNames)
    throws JavaClassCreatorException;

  /**
   * Starts a new interface creation process. The class is created by a call to either
   * <code>endClass()</code> or <code>endClassToFile()</code>.
   *<p>
   * Implementation must set itfs in this class.
   *
   * @param className should be a simple class name without package prefix.
   */
  public abstract void startInterface(String className, String[] itfNames) throws JavaClassCreatorException;

  /**
   * Concludes the class file and returns the bytes for the class.
   */
  public abstract byte[] endClass() throws JavaClassCreatorException;

  /**
   * Concludes the class file and writes the class into a file.
   */
  public void endClassToFile(String fileName) throws IOException, JavaClassCreatorException {
    FileOutputStream os = new FileOutputStream(fileName);
    os.write(endClass());
    os.close();
  }


  /////////////////////////////////////////////////////////////////
  //
  // Field Creation
  //
  /////////////////////////////////////////////////////////////////

  /**
   * Field creation.
   * Must be called after <code>startClass()</code> and before <code>endClass()</code>.
   */
  public abstract void addField(int accessFlags, String name, String type) throws JavaClassCreatorException;

  /**
   * Constant creation.
   * Must be called after <code>startClass()</code> and before <code>endClass()</code>.
   */
  public abstract void addConstant(int accessFlags, String name, String type, Object value)
    throws JavaClassCreatorException;

  /**
   * @return the type name of an added field or constant.
   */
  public abstract String getFieldType(String name) throws JavaClassCreatorException;

  /**
   * @return true if the added field is static.
   */
  public abstract boolean isStaticField(String name) throws JavaClassCreatorException;


  /////////////////////////////////////////////////////////////////
  //
  // Method Creation
  //
  /////////////////////////////////////////////////////////////////

  /**
   * Abstract method addition method. A method is created by a call to <code>endMethod()</code>.
   * Must be called after <code>startClass()</code> and before <code>endClass()</code>.
   */
  public abstract void addAbstractMethod(int accessFlags, String name, String[] argTypes, String[] argNames,
            String returnType, String[] exceptionNames) throws JavaClassCreatorException;

  /**
   * Add a default empty constructor.
   */
  public abstract void addDefaultConstructor(int accessFlags) throws JavaClassCreatorException;

  /**
   * Starts a new method construction process.
   * Must be called after <code>startClass()</code> and before <code>endClass()</code>.
   */
  public abstract void startMethod(int accessFlags, String name, String[] argTypes, String[] argNames,
            String returnType, String[] exceptionNames) throws JavaClassCreatorException;

  /**
   * Concludes the method and adds it to the class.
   */
  public abstract void endMethod() throws JavaClassCreatorException;

  /**
   *@return the access flags for the current method. If not in method, should return 0.
   */
  public abstract int getMethodAccessFlags();

  /**
   *@return the current method name.
   */
  public abstract String getMethodName();

  /**
   * Add a local variable to the current method.
   * Must be called after <code>startMethod()</code> and before adding byte codes
   * or catch clauses, and of course before the <code>endMethod()</code> call.
   */
  public abstract void addLocalVariable(String name, String type, Object init)
    throws JavaClassCreatorException;

  public abstract int getLocalVariableIndex(String name) throws JavaClassCreatorException;

  /**
   * @return the variable type name of an added variable in the current method.
   */
  public abstract String getVariableType(String name) throws JavaClassCreatorException;


  /////////////////////////////////////////////////////////////////
  //
  // JVM Instruction Convenience
  //
  /////////////////////////////////////////////////////////////////

  public final void inst_aconst_null() throws JavaClassCreatorException { inst(1); }

  public final void inst_iconst(int val) throws JavaClassCreatorException {
    if (val >= -1 && val <= 5)
      inst(3 + val); // begins with iconst_0
    else
      inst_ldc(new Integer(val));
  }

  public final void inst_lconst(long val) throws JavaClassCreatorException {
    if (val==0 || val==1)
      inst(9 + (int)val); // lconst_0
    else
      inst_ldc(new Long(val));
  }

  public final void inst_fconst(float val) throws JavaClassCreatorException {
    if (val == 0)
      inst(11); // fconst_0
    else if (val == 1)
      inst(12); // fconst_1
    else if (val == 2)
      inst(13); // fconst_2
    else
      inst_ldc(new Float(val));
  }

  public final void inst_dconst(double val) throws JavaClassCreatorException {
    if (val == 0)
      inst(14); // dconst_0
    else if (val == 1)
      inst(15); // dconst_1
    else
      inst_ldc(new Double(val));
  }


  public final void inst_iastore() throws JavaClassCreatorException { inst(79); }
  public final void inst_lastore() throws JavaClassCreatorException { inst(80); }
  public final void inst_fastore() throws JavaClassCreatorException { inst(81); }
  public final void inst_dastore() throws JavaClassCreatorException { inst(82); }
  public final void inst_aastore() throws JavaClassCreatorException { inst(83); }
  public final void inst_bastore() throws JavaClassCreatorException { inst(84); }
  public final void inst_castore() throws JavaClassCreatorException { inst(85); }
  public final void inst_sastore() throws JavaClassCreatorException { inst(86); }

  public final void inst_iaload() throws JavaClassCreatorException { inst(46); }
  public final void inst_laload() throws JavaClassCreatorException { inst(47); }
  public final void inst_faload() throws JavaClassCreatorException { inst(48); }
  public final void inst_daload() throws JavaClassCreatorException { inst(49); }
  public final void inst_aaload() throws JavaClassCreatorException { inst(50); }
  public final void inst_baload() throws JavaClassCreatorException { inst(51); }
  public final void inst_caload() throws JavaClassCreatorException { inst(52); }
  public final void inst_saload() throws JavaClassCreatorException { inst(53); }

  public final void inst_iadd() throws JavaClassCreatorException { inst(96); }
  public final void inst_ladd() throws JavaClassCreatorException { inst(97); }
  public final void inst_fadd() throws JavaClassCreatorException { inst(98); }
  public final void inst_dadd() throws JavaClassCreatorException { inst(99); }
  public final void inst_isub() throws JavaClassCreatorException { inst(100); }
  public final void inst_lsub() throws JavaClassCreatorException { inst(101); }
  public final void inst_fsub() throws JavaClassCreatorException { inst(102); }
  public final void inst_dsub() throws JavaClassCreatorException { inst(103); }

  public final void inst_imul() throws JavaClassCreatorException { inst(104); }
  public final void inst_lmul() throws JavaClassCreatorException { inst(105); }
  public final void inst_fmul() throws JavaClassCreatorException { inst(106); }
  public final void inst_dmul() throws JavaClassCreatorException { inst(107); }
  public final void inst_idiv() throws JavaClassCreatorException { inst(108); }
  public final void inst_ldiv() throws JavaClassCreatorException { inst(109); }
  public final void inst_fdiv() throws JavaClassCreatorException { inst(110); }
  public final void inst_ddiv() throws JavaClassCreatorException { inst(111); }
  public final void inst_irem() throws JavaClassCreatorException { inst(112); }
  public final void inst_lrem() throws JavaClassCreatorException { inst(113); }
  public final void inst_frem() throws JavaClassCreatorException { inst(114); }
  public final void inst_drem() throws JavaClassCreatorException { inst(115); }

  public final void inst_ineg()  throws JavaClassCreatorException { inst(116); }
  public final void inst_lneg()  throws JavaClassCreatorException { inst(117); }
  public final void inst_fneg()  throws JavaClassCreatorException { inst(118); }
  public final void inst_dneg()  throws JavaClassCreatorException { inst(119); }
  public final void inst_ishl()  throws JavaClassCreatorException { inst(120); }
  public final void inst_lshl()  throws JavaClassCreatorException { inst(121); }
  public final void inst_ishr()  throws JavaClassCreatorException { inst(122); }
  public final void inst_lshr()  throws JavaClassCreatorException { inst(123); }
  public final void inst_iushr() throws JavaClassCreatorException { inst(124); }
  public final void inst_lushr() throws JavaClassCreatorException { inst(125); }
  public final void inst_iand()  throws JavaClassCreatorException { inst(126); }
  public final void inst_land()  throws JavaClassCreatorException { inst(127); }
  public final void inst_ior()   throws JavaClassCreatorException { inst(128); }
  public final void inst_lor()   throws JavaClassCreatorException { inst(129); }
  public final void inst_ixor()  throws JavaClassCreatorException { inst(130); }
  public final void inst_lxor()  throws JavaClassCreatorException { inst(131); }

  public final void inst_lcmp()  throws JavaClassCreatorException { inst(148); }
  public final void inst_fcmpl() throws JavaClassCreatorException { inst(149); }
  public final void inst_fcmpg() throws JavaClassCreatorException { inst(150); }
  public final void inst_dcmpl() throws JavaClassCreatorException { inst(151); }
  public final void inst_dcmpg() throws JavaClassCreatorException { inst(152); }

  public final void inst_ireturn() throws JavaClassCreatorException { inst(172); }
  public final void inst_lreturn() throws JavaClassCreatorException { inst(173); }
  public final void inst_freturn() throws JavaClassCreatorException { inst(174); }
  public final void inst_dreturn() throws JavaClassCreatorException { inst(175); }
  public final void inst_areturn() throws JavaClassCreatorException { inst(176); }
  public final void inst_return()  throws JavaClassCreatorException { inst(177); }

  public final void inst_arraylength()  throws JavaClassCreatorException { inst(190); }
  public final void inst_athrow()       throws JavaClassCreatorException { inst(191); }
  public final void inst_monitorenter() throws JavaClassCreatorException { inst(194); }
  public final void inst_monitorexit()  throws JavaClassCreatorException { inst(195); }
  public final void inst_nop()          throws JavaClassCreatorException { inst(0); }


  public final void inst_i2l() throws JavaClassCreatorException { inst(133); }
  public final void inst_i2f() throws JavaClassCreatorException { inst(134); }
  public final void inst_i2d() throws JavaClassCreatorException { inst(135); }
  public final void inst_l2i() throws JavaClassCreatorException { inst(136); }
  public final void inst_l2f() throws JavaClassCreatorException { inst(137); }
  public final void inst_l2d() throws JavaClassCreatorException { inst(138); }
  public final void inst_f2i() throws JavaClassCreatorException { inst(139); }
  public final void inst_f2l() throws JavaClassCreatorException { inst(140); }
  public final void inst_f2d() throws JavaClassCreatorException { inst(141); }
  public final void inst_d2i() throws JavaClassCreatorException { inst(142); }
  public final void inst_d2l() throws JavaClassCreatorException { inst(143); }
  public final void inst_d2f() throws JavaClassCreatorException { inst(144); }
  public final void inst_i2b() throws JavaClassCreatorException { inst(145); }
  public final void inst_i2c() throws JavaClassCreatorException { inst(146); }
  public final void inst_i2s() throws JavaClassCreatorException { inst(147); }

  public final void inst_pop()     throws JavaClassCreatorException { inst(87); }
  public final void inst_pop2()    throws JavaClassCreatorException { inst(88); }
  public final void inst_dup()     throws JavaClassCreatorException { inst(89); }
  public final void inst_dup_x1()  throws JavaClassCreatorException { inst(90); }
  public final void inst_dup_x2()  throws JavaClassCreatorException { inst(91); }
  public final void inst_dup2()    throws JavaClassCreatorException { inst(92); }
  public final void inst_dup2_x1() throws JavaClassCreatorException { inst(93); }
  public final void inst_dup2_x2() throws JavaClassCreatorException { inst(94); }
  public final void inst_swap()    throws JavaClassCreatorException { inst(95); }

  public final void inst_aload_0()          throws JavaClassCreatorException { inst(42); }

  public final void inst_iload(String var)  throws JavaClassCreatorException { instLoadStoreRet(21,var); }
  public final void inst_lload(String var)  throws JavaClassCreatorException { instLoadStoreRet(22,var); }
  public final void inst_fload(String var)  throws JavaClassCreatorException { instLoadStoreRet(23,var); }
  public final void inst_dload(String var)  throws JavaClassCreatorException { instLoadStoreRet(24,var); }
  public final void inst_aload(String var)  throws JavaClassCreatorException { instLoadStoreRet(25,var); }
  public final void inst_istore(String var) throws JavaClassCreatorException { instLoadStoreRet(54,var); }
  public final void inst_lstore(String var) throws JavaClassCreatorException { instLoadStoreRet(55,var); }
  public final void inst_fstore(String var) throws JavaClassCreatorException { instLoadStoreRet(56,var); }
  public final void inst_dstore(String var) throws JavaClassCreatorException { instLoadStoreRet(57,var); }
  public final void inst_astore(String var) throws JavaClassCreatorException { instLoadStoreRet(58,var); }
  public final void inst_ret(String var)    throws JavaClassCreatorException { instLoadStoreRet(169,var); }

  public final void inst_new(String type)        throws JavaClassCreatorException { instType(187,type); }
  public final void inst_newarray(String type)   throws JavaClassCreatorException { instType(188,type); }
  public final void inst_anewarray(String type)  throws JavaClassCreatorException { instType(189,type); }
  public final void inst_checkcast(String type)  throws JavaClassCreatorException { instType(192,type); }
  public final void inst_instanceof(String type) throws JavaClassCreatorException { instType(193,type); }

  public final void inst_ldc(Object cnst)    throws JavaClassCreatorException { instLdc(18,cnst,null); }
  public final void inst_ldc_w(Object cnst)  throws JavaClassCreatorException { instLdc(19,cnst,null); }
  public final void inst_ldc2_w(Object cnst) throws JavaClassCreatorException { instLdc(20,cnst,null); }

  public final void inst_ldc(Object c, String type)    throws JavaClassCreatorException {instLdc(18,c,type);}
  public final void inst_ldc_w(Object c, String type)  throws JavaClassCreatorException {instLdc(19,c,type);}
  public final void inst_ldc2_w(Object c, String type) throws JavaClassCreatorException {instLdc(20,c,type);}
  
  public final void inst_ifeq(String lbl)      throws JavaClassCreatorException { instJump(153,lbl); }
  public final void inst_ifne(String lbl)      throws JavaClassCreatorException { instJump(154,lbl); }
  public final void inst_iflt(String lbl)      throws JavaClassCreatorException { instJump(155,lbl); }
  public final void inst_ifge(String lbl)      throws JavaClassCreatorException { instJump(156,lbl); }
  public final void inst_ifgt(String lbl)      throws JavaClassCreatorException { instJump(157,lbl); }
  public final void inst_ifle(String lbl)      throws JavaClassCreatorException { instJump(158,lbl); }
  public final void inst_if_icmpeq(String lbl) throws JavaClassCreatorException { instJump(159,lbl); }
  public final void inst_if_icmpne(String lbl) throws JavaClassCreatorException { instJump(160,lbl); }
  public final void inst_if_icmplt(String lbl) throws JavaClassCreatorException { instJump(161,lbl); }
  public final void inst_if_icmpge(String lbl) throws JavaClassCreatorException { instJump(162,lbl); }
  public final void inst_if_icmpgt(String lbl) throws JavaClassCreatorException { instJump(163,lbl); }
  public final void inst_if_icmple(String lbl) throws JavaClassCreatorException { instJump(164,lbl); }
  public final void inst_if_acmpeq(String lbl) throws JavaClassCreatorException { instJump(165,lbl); }
  public final void inst_if_acmpne(String lbl) throws JavaClassCreatorException { instJump(166,lbl); }
  public final void inst_goto(String lbl)      throws JavaClassCreatorException { instJump(167,lbl); }
  public final void inst_goto_w(String lbl)    throws JavaClassCreatorException { instJump(200,lbl); }
  public final void inst_jsr(String lbl)       throws JavaClassCreatorException { instJump(168,lbl); }
  public final void inst_jsr_w(String lbl)     throws JavaClassCreatorException { instJump(201,lbl); }
  public final void inst_ifnull(String lbl)    throws JavaClassCreatorException { instJump(198,lbl); }
  public final void inst_ifnonnull(String lbl) throws JavaClassCreatorException { instJump(199,lbl); }


  public final void inst_getstatic(String cls, String fld, String type) throws JavaClassCreatorException {
    instGetPut(178, cls, fld, type);
  }

  public final void inst_putstatic(String cls, String fld, String type) throws JavaClassCreatorException {
    instGetPut(179, cls, fld, type);
  }

  public final void inst_getfield(String cls, String fld, String type) throws JavaClassCreatorException {
    instGetPut(180, cls, fld, type);
  }

  public final void inst_putfield(String cls, String fld, String type) throws JavaClassCreatorException {
    instGetPut(181, cls, fld, type);
  }

  public final void inst_invokevirtual(String cls, String mthd, String[] params, String retType)
    throws JavaClassCreatorException { instInvoke(182,cls,mthd,params,retType); }

  public final void inst_invokestatic(String cls, String mthd, String[] params, String retType)
    throws JavaClassCreatorException { instInvoke(184,cls,mthd,params,retType); }

  public final void inst_invokeinterface(String cls, String mthd, String[] params, String retType)
    throws JavaClassCreatorException { instInvoke(185,cls,mthd,params,retType); }

  public final void inst_invokespecial(String cls, String mthd, String[] params, String retType)
    throws JavaClassCreatorException { instInvoke(183,cls,mthd,params,retType); }


  /////////////////////////////////////////////////////////////////
  //
  // JVM Instruction Implementation
  //
  /////////////////////////////////////////////////////////////////

  /**
   * Add a local variable to the current method.
   * Must be called after <code>startMethod()</code> and after adding byte codes,
   * and before <code>endMethod()</code> call.
   * @param type if null, this is a Finally clause.
   */
  public abstract void addCatchClause(String type, String startLabel, String endLabel, String actionLabel)
    throws JavaClassCreatorException;

  public abstract void setLabel(String label) throws JavaClassCreatorException;

  /**
   * For <code>nop aconst_null iconst_m1 iconst_0 iconst_1 iconst_2 iconst_3 iconst_4 iconst_5
   * lconst_0 lconst_1 fconst_0 fconst_1 fconst_2 dconst_0 dconst_1
   * iload_0 iload_1 iload_2 iload_3 lload_0 lload_1 lload_2 lload_3
   * fload_0 fload_1 fload_2 fload_3 dload_0 dload_1 dload_2 dload_3
   * aload_0 aload_1 aload_2 aload_3
   * iaload laload faload daload aaload baload caload saload
   * istore_0 istore_1 istore_2 istore_3 lstore_0 lstore_1 lstore_2 lstore_3
   * fstore_0 fstore_1 fstore_2 fstore_3 dstore_0 dstore_1 dstore_2 dstore_3
   * astore_0 astore_1 astore_2 astore_3
   * iastore lastore fastore dastore aastore bastore castore sastore
   * pop pop2 dup dup_x1 dup_x2 dup2 dup2_x1 dup2_x2 swap
   * iadd ladd fadd dadd isub lsub fsub dsub
   * imul lmul fmul dmul idiv ldiv fdiv ddiv irem lrem frem drem
   * ineg lneg fneg dneg ishl lshl ishr lshr iushr lushr iand land ior lor ixor lxor
   * i2l i2f i2d l2i l2f l2d f2i f2l f2d d2i d2l d2f i2b i2c i2s
   * lcmp fcmpl fcmpg dcmpl dcmpg ireturn lreturn freturn dreturn areturn return
   * arraylength athrow monitorenter monitorexit</code>.
   */
  public abstract void inst(int opcode) throws JavaClassCreatorException;

  /**
   * For <code>iload lload fload dload aload istore lstore fstore dstore astore ret</code>.
   */
  public abstract void instLoadStoreRet(int opcode, String varName) throws JavaClassCreatorException;

  /**
   * For <code>new newarray anewarray checkcast instanceof</code>.
   */
  public abstract void instType(int opcode, String type) throws JavaClassCreatorException;

  /**
   * For <code>getstatic putstatic getfield putfield</code>.
   */
  public abstract void instGetPut(int opcode, String clsName, String fldName, String type)
    throws JavaClassCreatorException;

  /**
   * For <code>ldc ldc_w ldc2_w</code>.
   * @param type can be null, "int", "long", "float", "double" or "java.lang.String".
   */
  public abstract void instLdc(int opcode, Object constant, String type) throws JavaClassCreatorException;

  public abstract void inst_bipush(byte value) throws JavaClassCreatorException;
  public abstract void inst_sipush(short value) throws JavaClassCreatorException;
  public abstract void inst_iinc(String varName, int inc) throws JavaClassCreatorException;
  public abstract void inst_multianewarray(String type, short dim) throws JavaClassCreatorException;

  /**
   * For <code>ifeq ifne iflt ifge ifgt ifle
   * if_icmpeq if_icmpne if_icmplt if_icmpge if_icmpgt if_icmple if_acmpeq if_acmpne
   * goto jsr ifnull ifnonnull goto_w jsr_w</code>.
   */
  public abstract void instJump(int opcode, String label) throws JavaClassCreatorException;

  /**
   * For <code>invokevirtual invokespecial invokestatic invokeinterface</code>.
   */
  public abstract void instInvoke(int opcode, String clsName, String mthdName, String[] paramTypes, String retType)
    throws JavaClassCreatorException;

  /**
   * A synonym for <code>inst_tableswitch()</code>, which is often times optimized
   * to lookupswitch when constants are consecutive anyway.
   */
  public final void inst_switch(int[] consts, String[] labels, String defaultLabel)
    throws JavaClassCreatorException
  {
    inst_tableswitch(consts, labels, defaultLabel);
  }

  /**
   * For <code>tableswitch</code>.
   */
  public abstract void inst_tableswitch(int[] consts, String[] labels, String defaultLabel)
    throws JavaClassCreatorException;

  /**
   * For <code>lookupswitch</code>.
   */
  public abstract void inst_lookupswitch(int startConst, String[] labels, String defaultLabel)
    throws JavaClassCreatorException;


  /////////////////////////////////////////////////////////////////
  //
  // Macro Implementation
  //
  /////////////////////////////////////////////////////////////////

  public void macroSet(Object dest, Object value) throws JavaClassCreatorException {
    macroSet(dest, value, null, null);
  }

  /**
   * @param dest is a String or a VarAccess.
   * @param value is a constant of type of Boolean, Character, Number or String,
   *              or a VarAccess, or an AssignableMacro.
   * @param visitor is an instance of JamaicaParserVisitor used by the value when it
   *                is an AssignableMacro.
   * @param visitor is the extra data for the visitor when value is an AssignableMacro.
   */
  public void macroSet(Object dest, Object value, Object visitor, Object data)
    throws JavaClassCreatorException
  {
    VarAccess target = null;
    if (dest != null) {
      target = (dest instanceof String) ? new VarAccess((String)dest,0) : (VarAccess)dest;
      target.findParamType(this);

      if (target.isArray()) {
        if (target.isVar)
          inst_aload(target.name);
        else
//          instGetPut(isStaticField(target.name)?178:180, // getstatic/getfield
//                     getClassName(), target.name, target.getFullTypeName());
          macroLoadVarOrField(target.name, target.getFullTypeName());

        if (target.dim == 1) {
          macroLoadConstantOrVarOrField(target.index, "int");
        } else {
          Object[] oa = (Object[])target.index;
          for (int i=0; i<oa.length; ++i) {
            if (i>0)
              inst_aaload();
            macroLoadConstantOrVarOrField(oa[i], "int");
          }
        }
      } else if (!target.isVar && !isStaticField(target.name)) {
        inst_aload_0();
      }
    }

    if (value instanceof AssignableMacro) {
      try {
        ((AssignableMacro)value).instantiate(visitor, data, target==null ? null : target.type);
      } catch(JavaClassCreatorException jcce) {
        throw jcce;
      } catch(Exception e) {
        throw new JavaClassCreatorException(e.getMessage());
      }
    } else {
      macroLoadConstantOrVarOrField(value, target==null ? null : target.type);
    }

    if (target != null) {
      if (target.isArray())
        inst(getArrayLoadStoreInstruction(target.type, true));
      else if (target.isVar)
        instLoadStoreRet(getStoreInstruction(target.type), target.name);
      else
        instGetPut(isStaticField(target.name)?179:181, // putstatic/putfield
                   getClassName(), target.name, target.getFullTypeName());
    }
  }

  /**
   *@param cmd    is one of the following: <code>print println flush</code>
   *@param target is one of these: <code>out err</code>, or any variable/field name.
   *              The named variable/field must hold either an object, e.g.,
   *              java.io.PrintWriter or java.io.PrintStream, that supports the three
   *              above methods.
   */
  public void macroPrint(String cmd, String target, Object[] params) throws JavaClassCreatorException {
    inst_getstatic("java.lang.System", target==null ? "out" : target, "java.io.PrintStream");

    int len = params==null ? 0 : params.length;
    if (len <= 0) { // println and flush
      inst_invokevirtual("java.io.PrintStream", cmd, NO_STRING_ARRAY, "void");
    } else {
      if (cmd.equals("flush"))
        inst_dup();

      String[] oneparam = new String[1];
      for (int i=0; i<len; ++i) {
        if (i<len-1)
          inst_dup();
        oneparam[0] = cleanupType(macroLoadConstantOrVarOrField(params[i], null));
        inst_invokevirtual("java.io.PrintStream",
                           (i==len-1 && cmd.equals("println")) ? "println" : "print",
                           oneparam, "void");
      }

      if (cmd.equals("flush")) {
        inst_invokevirtual("java.io.PrintStream", "flush", NO_STRING_ARRAY, "void");
      }
    }
  }

  /**
   * Creates an object on the stack.
   * It may take parameters as constants, local variables and/or fields.
   */
  public void macroObject(String type, String[] types, Object[] params)
    throws JavaClassCreatorException
  {
    inst_new(type);
    inst_dup();

    int i, len = types==null ? 0 : types.length;
    for (i=0; i<len; ++i)
      macroLoadConstantOrVarOrField(params[i], types[i]);

    inst_invokespecial(type, "<init>", types, "void");
  }

  /**
   * Creates an array on the stack.
   * It may take initializers as constants, local variables and/or fields.
   *@param dim the number of dimensions.If &gt; 0, params are the sizes of the (first) dimensions.
   *           If &gt; 0, params are the sizes of all or the leading dimensions.
   *           If &lt;= 0, params are initializers.
   *@param params see dim.
   */
  public void macroArray(String type, int dim, Object[] params) throws JavaClassCreatorException {
    int i, len = params==null ? 0 : params.length;
    int dim1_size = -1;

    if (dim <= 0) {
      dim = 1;
      if (params != null)
        dim1_size = params.length;
      else
        throw new JavaClassCreatorException("No size or initializers are provided for macro %array.");
    }


    if (dim == 1) { // 1-dimentional

      if (dim1_size > 0) {     // with initializers
        inst_iconst(dim1_size);
      } else {                 // without initializers
        if (len <= 0)
          throw new JavaClassCreatorException("First dimension size must be specified for macro %array.");
        macroLoadConstantOrVarOrField(params[0], "int");
        len = 0; // no init's.
      }

      int xastoreOpcode = getArrayLoadStoreInstruction(type, true);
      if (xastoreOpcode == 83) // aastore
        inst_anewarray(type);
      else
        inst_newarray(type);

      if (len > dim1_size)
        len = dim1_size;

      for (i=0; i<len; ++i) {
        inst_dup();
        inst_iconst(i);
        macroLoadConstantOrVarOrField(params[i], type);
        inst(xastoreOpcode);
      }

    } else { // multi-dimentional

      if (len > dim)
        len = dim;
      for (i=0; i<len; ++i)
        macroLoadConstantOrVarOrField(params[i], "int");
      for (i=0; i<dim; ++i)
        type += "[]";
      inst_multianewarray(type, (short)len);

    }
  }

  public void macroStringConcat(Object[] params) throws JavaClassCreatorException {
    macroObject("java.lang.StringBuffer", NO_STRING_ARRAY, null);

    String[] oneparam = new String[1];
    int len = params==null ? 0 : params.length;
    for (int i=0; i<len; ++i) {
      // No need to dup
      oneparam[0] = cleanupType(macroLoadConstantOrVarOrField(params[i], null));
      inst_invokevirtual("java.lang.StringBuffer", "append", oneparam, "java.lang.StringBuffer");
      // Nor need to pop
    }

    inst_invokevirtual("java.lang.StringBuffer", "toString", NO_STRING_ARRAY, "java.lang.String");
  }

  public void macroEndIterate(String id) throws JavaClassCreatorException {
    macroIterate(null, null, id);
  }

  public void macroIterate(VarAccess coll, String var, String id) throws JavaClassCreatorException {
    String label_begin = "?l?a?" + id;
    String label_end   = "?l?z?" + id;

    if (coll != null) { // begin
      coll.findParamType(this);
      String type = macroLoadVarOrField(coll, null);
      String mthd1, mthd2;
      if (!type.equals("java.util.Iterator") || !type.equals("java.util.Enumeration")) {
        try {
          Class cls = Class.forName(type);
          if (ClassUtils.isAssignable(cls, java.util.Iterator.class))
            type = "java.util.Iterator";
          else if (ClassUtils.isAssignable(cls, java.util.Enumeration.class))
            type = "java.util.Enumeration";
        } catch(Exception e) {}
      }
      if (type.equals("java.util.Iterator")) {
        mthd1 = "hasNext";
        mthd2 = "next";
      } else if (type.equals("java.util.Enumeration")) {
        mthd1 = "hasMoreElements";
        mthd2 = "nextElement";
      } else {
        throw new JavaClassCreatorException(
          "Subject " + type + " in iterate macro is neither java.util.Iterator nor java.util.Emnueration.");
      }

      setLabel(label_begin);
      inst_dup();
      inst_invokeinterface(type, mthd1, NO_STRING_ARRAY, "boolean");
      inst_ifeq(label_end);
      inst_dup();
      inst_invokeinterface(type, mthd2, NO_STRING_ARRAY, "java.lang.Object");
      if (var != null)
        inst_astore(var);

    } else { // end
    
      inst_goto(label_begin);
      setLabel(label_end);
      inst_pop();

    }
  }

  public void macroEndArrayIterate(String var, String id) throws JavaClassCreatorException {
    macroArrayIterate(null, var, id);
  }

  public void macroArrayIterate(VarAccess arr, String var, String id) throws JavaClassCreatorException {
    String label_begin = "?l?a?" + id;
    String label_end   = "?l?z?" + id;

    if (arr != null) { // begin
      inst_iconst(0);
      inst_istore(var);
      arr.findParamType(this);
      macroLoadVarOrField(arr, null);
      setLabel(label_begin);
      inst_dup();
      inst_arraylength();
      inst_iload(var);
      inst_if_icmple(label_end);
    } else {           // end
      inst_iinc(var, 1);
      inst_goto(label_begin);
      setLabel(label_end);
      inst_pop();
    }
  }

  public void macroElse(String id) throws JavaClassCreatorException {
    macroIf("else", null, null, id, false);
  }

  public void macroEndIf(String id) throws JavaClassCreatorException {
    macroIf(null, null, null, id, false);
  }

  public void macroIf(String op, Object left, Object right, String id, boolean hasElse)
    throws JavaClassCreatorException
  {
    String label_else = "?i?e?" + id;
    String label_end  = "?i?f?" + id;

    if (op == null) { // %end_if

      setLabel(label_end);

    } else if ("else".equals(op)) { // %else

      inst_goto(label_end);
      setLabel(label_else);

    } else { // %if

      int optype = 0;
      if (op.equals("=="))      optype = 1; // reversed to !=
      else if (op.equals("!=")) optype = 0; // reversed to ==
      else if (op.equals("<"))  optype = 3; // reversed to >=
      else if (op.equals(">=")) optype = 2; // reversed to <
      else if (op.equals(">"))  optype = 5; // reversed to <=
      else if (op.equals("<=")) optype = 4; // reversed to >

      String label = hasElse ? label_else : label_end;
      String type1;

      if (left == null || right == null) {
        if (left == null && right == null)
          throw new JavaClassCreatorException("Macro %if can't take 2 nulls.");
        type1 = macroLoadConstantOrVarOrField(left!=null ? left : right, null);
        if (isPrimitiveType(type1))
          throw new JavaClassCreatorException(left + " can't be compared to null.");
        switch(optype) {
        case 0:  // "=="
          inst_ifnull(label);
          break;
        case 1:  // "!="
          inst_ifnonnull(label);
          break;
        default:
          throw new JavaClassCreatorException("Comparator " + op + " can't be applied to null.");
        }
      } else { // both values are not null.
        type1 = getType(left);
        String type2 = getType(right);
        boolean flag1 = isPrimitiveType(type1);
        boolean flag2 = isPrimitiveType(type2);
        if (flag1 ^ flag2)
          throw new JavaClassCreatorException("Can't compare " + left + " and " + right + ": type incompatible.");

        if (flag1) { // both are primitives
          // If either one is 0, use ifeq/...
          flag1 = false;
          flag2 = false;
          if (left instanceof Integer || left instanceof Short || left instanceof Byte)
            flag1 = 0 == ((Number)left).intValue();
          else if (left instanceof Character)
            flag1 = 0 == ((Character)left).charValue();
          else if (left instanceof Boolean)
            flag1 = ((Boolean)left).booleanValue();

          if (right instanceof Integer || right instanceof Short || right instanceof Byte)
            flag2 = 0 == ((Number)right).intValue();
          else if (right instanceof Character)
            flag2 = 0 == ((Character)right).charValue();
          else if (right instanceof Boolean)
            flag2 = ((Boolean)right).booleanValue();

          if (flag1 || flag2) { // one is 0; load the other and call ifeq/...
            if (flag1) {
              left = right;
              switch(optype) {
              case 2:  optype = 3; break; // <
              case 3:  optype = 2; break; // >=
              case 4:  optype = 5; break; // >
              case 5:  optype = 4; break; // <=
              }
            }
            macroLoadConstantOrVarOrField(left, "int");
            instJump(153+optype, label); // ifeq, ...
          } else if (isInt(type1) && isInt(type2)) { // both are int's
            macroLoadConstantOrVarOrField(left, "int");
            macroLoadConstantOrVarOrField(right, "int");
            instJump(159+optype, label); // if_icmpeq, ...
          } else {
            if (type1.equals("double") || type2.equals("double")) { // at least one is double
              macroLoadConstantOrVarOrField(left, "double");
              macroLoadConstantOrVarOrField(right, "double");
              inst_dcmpl();
            } else if (type1.equals("float") || type2.equals("float")) { // at least one is float
              macroLoadConstantOrVarOrField(left, "float");
              macroLoadConstantOrVarOrField(right, "float");
              inst_fcmpl();
            } else { // at least one is long
              macroLoadConstantOrVarOrField(left, "long");
              macroLoadConstantOrVarOrField(right, "long");
              inst_lcmp();
            }
            instJump(153+optype, label); // ifeq, ...
          }
        } else { // both are references
          macroLoadConstantOrVarOrField(left, null);
          macroLoadConstantOrVarOrField(right, null);
          switch(optype) {
          case 0:  // "=="
            inst_if_acmpeq(label);
            return;
          case 1:  // "!="
            inst_if_acmpne(label);
            return;
          default:
            throw new JavaClassCreatorException("Comparator " + op + " can't be applied to objects.");
          }
        }
      }
    }
  }

  /////////////////////////////////////////////////////////////////
  //
  // Auxiliary
  //
  /////////////////////////////////////////////////////////////////

  public final String getType(Object o) throws JavaClassCreatorException {
    if (o instanceof VarAccess) {
      VarAccess va = (VarAccess)o;
      va.findParamType(this);
      return va.type;
    }
    return getConstTypeName(o);
  }

  /**
   * Inserts a instruction that loads a constant or a named variable or field onto the stack.
   * @return the type string.
   */
  public final String macroLoadConstantOrVarOrField(Object o, String intendedType)
    throws JavaClassCreatorException
  {
    if (o == null) {
      inst_aconst_null();
      return null;
    }

    if (o instanceof VarAccess)
      return macroLoadVarOrField((VarAccess)o, intendedType);

    String type = intendedType == null ? getConstTypeName(o) : intendedType;
    if (type.equals("long") || type.equals("double"))
      inst_ldc2_w(o, type);
    else
      inst_ldc(o, type);
    return type;
  }

  public final String macroLoadVarOrField(String name, String intendedType) throws JavaClassCreatorException {
    return macroLoadVarOrField(new VarAccess(name,0), intendedType);
  }

  /**
   * Inserts a instruction that loads the named variable or field onto the stack.
   * @return the type string.
   */
  public final String macroLoadVarOrField(VarAccess value, String intendedType) throws JavaClassCreatorException
  {
    value.findParamType(this);

    if (value.name.equals("this")) {
       inst_aload_0();
    } else if (value.isArray()) {
      // multi-dim array value access
      if (value.isVar)
        inst_aload(value.name);
      else
        macroGetField(value.name);

      if (value.index instanceof Object[]) {
        Object[] oa = (Object[])value.index;
        for (int i=0; i<value.dim; ++i) {
          macroLoadConstantOrVarOrField(oa[i], "int");
          if (i < value.dim - 1)
            inst_aaload();
        }
      } else {
        macroLoadConstantOrVarOrField(value.index, "int");
      }
      inst(getArrayLoadStoreInstruction(value.type, false));

    } else if (value.isVar) {
      instLoadStoreRet(getLoadInstruction(value.type), value.name);
    } else {
      macroGetField(value.name);
    }
    if (intendedType != null)
      macroTypeCast(value.type, intendedType);
    return value.type;
  }

  public final String macroGetField(String name) throws JavaClassCreatorException {
    boolean isStatic = isStaticField(name);
    if (!isStatic && Modifier.isStatic(getMethodAccessFlags()))
      throw new JavaClassCreatorException("Instance data member " + name +
        " can not be accessed from static method " + getMethodName() + "().");
    if (!isStatic)
      inst_aload_0();
    String type = getFieldType(name);
    instGetPut(isStatic?178:180, getClassName(), name, type);
    return type;
  }

/*
  public final void storeVarOrField(String name) throws JavaClassCreatorException {
    String type = getVariableType(name);
    if (type != null) { // is a variable.
      instLoadStoreRet(getStoreInstruction(type), name);
    } else {            // is a field.
      type = getFieldType(name);
      if (type == null)
        throw new JavaClassCreatorException("Variable/field " + name + " is not found.");
      instGetPut(isStaticField(name) ? 179 : 181, getClassName(), name, type);
    }
  }
*/

  /**
   * Inserts needed instructions to cast the data types on the stack.
   */
  public void macroTypeCast(String curType, String intendedType) throws JavaClassCreatorException {
    if (curType.equals(intendedType))
      return;

    char srcType = '?';
    char tgtType = '?';

    if (curType.equals("int")   ||
        curType.equals("short") ||
        curType.equals("char")  ||
        curType.equals("byte")  ||
        curType.equals("boolean"))
      srcType = 'i';
    else if (curType.equals("long"))
      srcType = 'l';
    else if (curType.equals("float"))
      srcType = 'f';
    else if (curType.equals("double"))
      srcType = 'd';

    if (intendedType.equals("int"))
      tgtType = 'i';
    else if (intendedType.equals("short"))
      tgtType = 's';
    else if (intendedType.equals("char"))
      tgtType = 'c';
    else if (intendedType.equals("byte") || intendedType.equals("boolean"))
      tgtType = 'b';
    else if (intendedType.equals("short"))
      tgtType = 's';
    else if (intendedType.equals("long"))
      tgtType = 'l';
    else if (intendedType.equals("float"))
      tgtType = 'f';
    else if (intendedType.equals("double"))
      tgtType = 'd';
    else { // an object type. cast it and done.
      if (srcType != '?')
        throw new JavaClassCreatorException("Can't cast " + curType + " to " + intendedType + '.');
      //inst_checkcast(intendedType);
      return;
    }

    switch(srcType) {
    case 'l': switch(tgtType) {
              case 'f':  inst_l2f(); return;
              case 'd':  inst_l2d(); return;
              default:   inst_l2i(); return;
              }
    case 'f': switch(tgtType) {
              case 'l':  inst_f2l(); return;
              case 'd':  inst_f2d(); return;
              default:   inst_f2i(); return;
              }
    case 'd': switch(tgtType) {
              case 'l':  inst_d2l(); return;
              case 'f':  inst_d2f(); return;
              default:   inst_d2i(); return;
              }
    default:  switch(tgtType) {
              case 'l':  inst_i2l(); return;
              case 'f':  inst_i2f(); return;
              case 'd':  inst_i2d(); return;
              case 'b':  inst_i2b(); return;
              case 'c':  inst_i2c(); return;
              case 's':  inst_i2s(); return;
              }
    }
  }

  protected boolean inst_smallInt(Object constant, boolean anyNumber) throws JavaClassCreatorException {
    int i;

    if (constant instanceof Boolean)
      i = ((Boolean)constant).booleanValue() ? 1 : 0;
    else if (constant instanceof Character)
      i = ((Character)constant).charValue();
    else if (constant instanceof Number && (anyNumber ||
               constant instanceof Byte || constant instanceof Short || constant instanceof Integer))
      i = ((Number)constant).intValue();
    else
      return false;

    if (i >= -1 && i <= 5) {
      inst(3 + i); // ICONST_0
    } else if (i >= -128 && i <= 127) {
      inst_bipush((byte)i);
    } else if (i >= -32768 && i <= 32767) {
      inst_sipush((short)i);
    } else
      return false;

    return true;
  }

  /////////////////////////////////////////////////////////////////
  //
  // Implementational
  //
  /////////////////////////////////////////////////////////////////

  public static final String[] NO_STRING_ARRAY = new String[0];


  static String cleanupType(String t) {
    if (t != null) {
      if (t.equals("int") || t.equals("long") || t.equals("float") || t.equals("double") ||
          t.equals("boolean") || t.equals("char") || t.equals("char[]") || t.equals("java.lang.String"))
        return t;
      if (t.equals("short") || t.equals("byte"))
        return "int";
    }
    return "java.lang.Object";
  }

  public static VarAccess createVarAccess(String name) {
    return new VarAccess(name);
  }

  public static VarAccess createArrayAccess(String name, Object index) {
    VarAccess va = new VarAccess(name);
    va.setIndex(index);
    return va;
  }

  public static VarAccess createArrayAccess(String name, Object index1, Object index2) {
    VarAccess va = new VarAccess(name);
    va.setIndex(new Object[]{ index1, index2 });
    return va;
  }

  public static String getConstTypeName(Object val) {
    if (val instanceof Long)
      return "long";
    if (val instanceof Float)
      return "float";
    if (val instanceof Double)
      return "double";
    if (val instanceof Character)
      return "char";
    if (val instanceof Boolean)
      return "boolean";
    if (val instanceof Number)
      return "int";
    if (val instanceof String)
      return "java.lang.String";
    else
      return val.getClass().getName();
  }

  public static boolean isPrimitiveType(String type) {
    return isInt(type)          ||
           type.equals("long")  ||
           type.equals("float") ||
           type.equals("double");
  }

  public static boolean isInt(String type) {
    return type.equals("int")     ||
           type.equals("char")    ||
           type.equals("short")   ||
           type.equals("byte")    ||
           type.equals("boolean");
  }

  public static int getLoadInstruction(String type) {
    if (type.equals("int")   ||
        type.equals("short") ||
        type.equals("char")  ||
        type.equals("byte")  ||
        type.equals("boolean"))
      return 21;                           // iload
    if (type.equals("long"))   return 22;  // lload
    if (type.equals("float"))  return 23;  // fload
    if (type.equals("double")) return 24;  // dload
    return 25;                             // aload
  }

  public static int getStoreInstruction(String type) {
    if (type.equals("int")   ||
        type.equals("short") ||
        type.equals("char")  ||
        type.equals("byte")  ||
        type.equals("boolean"))
      return 54;                           // istore
    if (type.equals("long"))   return 55;  // lstore
    if (type.equals("float"))  return 56;  // fstore
    if (type.equals("double")) return 57;  // dstore
    return 58;                             // astore
  }

  public static int getArrayLoadStoreInstruction(String type, boolean store) {
    int ret;
    if      (type.equals("int"))     ret = 46; // iastore : iaload
    else if (type.equals("char"))    ret = 52; // castore : caload
    else if (type.equals("short"))   ret = 53; // sastore : saload
    else if (type.equals("byte"))    ret = 51; // bastore : baload
    else if (type.equals("boolean")) ret = 51; // bastore : baload
    else if (type.equals("long"))    ret = 47; // lastore : laload
    else if (type.equals("float"))   ret = 48; // fastore : faload
    else if (type.equals("double"))  ret = 49; // dastore : daload
    else ret = 50;                             // aastore : aaload

    return store ? (ret+33) : ret;
  }

  public static String stringToDescriptor (final String type) {
    int idx = type.lastIndexOf('[');
    if (idx > 0)
      return "[" + stringToDescriptor(type.substring(0,idx));
    if (type.equals("void"))    return "V";
    if (type.equals("boolean")) return "Z";
    if (type.equals("char"))    return "C";
    if (type.equals("byte"))    return "B";
    if (type.equals("short"))   return "S";
    if (type.equals("int"))     return "I";
    if (type.equals("float"))   return "F";
    if (type.equals("long"))    return "J";
    if (type.equals("double"))  return "D";
    return "L" + type.replace('.', '/') + ";";
  }

  public static String toJVMClassName(String clsName) {
    if (clsName == null) return "java/lang/Object";
    if (Character.isLowerCase(clsName.charAt(0))) {
      if (clsName.equals("int"))     return "I";
      if (clsName.equals("long"))    return "L";
      if (clsName.equals("double"))  return "D";
      if (clsName.equals("float"))   return "F";
      if (clsName.equals("byte"))    return "B";
      if (clsName.equals("boolean")) return "Z";
      if (clsName.equals("char"))    return "C";
      if (clsName.equals("short"))   return "S";
    }
    return clsName.replace('.','/');
  }

  public static String[] toJVMClassNames(String[] clsNames) {
    int len = clsNames == null ? 0 : clsNames.length;
    for (int i=0; i<len; ++i)
      clsNames[i] = toJVMClassName(clsNames[i]);
    return clsNames;
  }

  public static class VarAccess
  {
    public String name;
    public Object index; // can be Object[] for multi-dim access.
    public int line;

    transient String type = null;
    transient int dim = 0;
    transient boolean isVar = true;

    public VarAccess(String n, int l) { name = n; line = l; }
    public VarAccess(String n) { this(n,-1); }

    void findParamType(JavaClassCreator creator) throws JavaClassCreatorException {
      if (type != null)
        return;

      type = creator.getVariableType(name);
      isVar = type != null;
      if (!isVar) {
        type = creator.getFieldType(name);
        if (type == null)
          throw new JavaClassCreatorException("Variable/field " + name + " is not found.");
      }
      if (index != null) {
        int idx = type.indexOf('[');
        if (idx < 0)
          dim = 0;
        else {
          dim = (index instanceof Object[]) ? ((Object[])index).length : 1;
          int typeDim = 0;
          for (int i=idx; i<type.length(); ++i)
            if (type.charAt(i) == '[')
              ++typeDim;
          if (dim > typeDim)
            throw new JavaClassCreatorException("Invalid " + dim + "-dimentional access for " + type);
          type = type.substring(0, type.length() - 2 * dim);
        }
      }
    }

    public boolean isArray() { return index != null; }

    String getFullTypeName() {
      if (dim <= 0)
        return type;
      StringBuffer sb = new StringBuffer(type);
      for (int i=0; i<dim; ++i)
        sb.append("[]");
      return sb.toString();
    }

    public void setIndex(Object o) {
      if (o instanceof Object[]) {
        Object[] oa = (Object[])o;
        if (oa.length == 1) {
          index = oa[0];
          return;
        }
      } else if (o instanceof List) {
        List list = (List)o;
        if (list.size() == 1) {
          index = list.get(0);
          return;
        }
      }
      index = o;
    }

    public String toString() {
      if (index == null)
        return name;
      StringBuffer sb = new StringBuffer(name);
      sb.append('[');
      if (index instanceof Object[]) {
        Object[] oa = (Object[])index;
        for (int i=0; i<oa.length; ++i) {
          if (i>0) sb.append(',');
          sb.append(oa[i]);
        }
      } else {
        sb.append(index);
      }
      sb.append(']');
      return sb.toString();
    }
  }

  /**
   *@param visitor should be a JamaicaVisitor.
   *@param data is the extra data for the visitor.
   *@param type is a String.
   */
  public static interface AssignableMacro {
    public void instantiate(Object visitor, Object data, Object type) throws Exception;
  }

} // end of class.

