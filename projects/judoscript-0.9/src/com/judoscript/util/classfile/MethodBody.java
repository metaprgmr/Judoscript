/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 11-9-2002  JH   Inception.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/

package com.judoscript.util.classfile;

import java.io.*;
import java.util.*;

//
// Support for JASM only!
//
public class MethodBody implements ClassFileConsts
{
  // The result.
  Attr.Code code;
  ClassFileWriter cfw;
  String methodName;

  // The interim instruction store.
  Vector insts = new Vector();
  Hashtable labels = new Hashtable(); // String -> Integer (index in insts)

  // For named local variables, including method call parameters.
  Hashtable localVars = new Hashtable(); // String -> Integer (local variable index)
  int curLocalVarIndex = 0;

  // For creating the try-catch-finally table.
  Vector tryCatchLabels = new Vector(); // "try", "catch (class:label),+" and "finally:label"

  ///////////////////////////////////////////////////////////////////
  // Public methods
  //

  public MethodBody(String methodName, ClassFileWriter w) { this(methodName,w,-1,0); }

  public MethodBody(String methodName, ClassFileWriter w, int max_stack, int max_locals) {
    this.methodName = methodName;
    cfw = w;
    code = new Attr.Code(cfw.cpUtf8("Code"), 0);
    code.max_stack = max_stack;
    code.max_locals = max_locals;
  }

  public void setMaxStack(int max)  { code.max_stack = max; }
  public void setMaxLocals(int max) { code.max_locals = max; }
  public void setMaxLocals() { if (curLocalVarIndex>0) code.max_locals = curLocalVarIndex; }

  public void addLocalVar(String name) { addLocalVar(name,false); }
  public void addLocalVar(String name, boolean isWide) {
    localVars.put(name, new Integer(curLocalVarIndex));
    curLocalVarIndex += isWide ? 2 : 1;
  }
  public int getLocalVarIndex(String name) throws BadClassFormatException {
    try { return ((Integer)localVars.get(name)).intValue(); }
    catch(NullPointerException npe) {
      throwBadClassFormatException("Local variable '" + name + "' does not exist");
      return 0;
    }
  }

  public void setLabel(String name) throws BadClassFormatException {
    if (labels.containsKey(name))
      throwBadClassFormatException("Label " + name + " already exists");
    labels.put(name, new Integer(insts.size()));
  }
  public int getLabelIndex(String name) throws BadClassFormatException {
    try { return ((Integer)labels.get(name)).intValue(); }
    catch(NullPointerException npe) {
      throwBadClassFormatException("Label " + name + " does not exist");
      return 0;
    }
  }

  public void setTry()           { setTryCatchLabel("try"); }
  public void setCatch(String s) { setTryCatchLabel("catch "+s); }
  public void setFinally()       { setTryCatchLabel("finally"); }

  private void setTryCatchLabel(String s) {
    tryCatchLabels.setSize(insts.size());
    tryCatchLabels.setElementAt(s,insts.size()-1);
  }

  public Attr.Code getCode() throws BadClassFormatException { finish(); return code; }

///////////////////////////////////////////////////////////////////
// Inner classes
//

  static class Inst
  {
    InstInfo ii;
    Inst(int opcode) { ii = instructions[opcode]; }
    int getEstimatedSize() { return (ii.operand_meaning==M_UNK) ? 0 : ii.operand_bytes; }
    public String toString() { return ii.mnemonic; }
    public void write(DataOutputStream os) throws IOException { os.write(ii.opcode); }
  }

  static class InstOne extends Inst
  {
    int param;
    InstOne(int opcode, int p) { super(opcode); this.param = p; }
    public String toString() { return ii.mnemonic + " " + param; }
    public void write(DataOutputStream os) throws IOException {
      os.write(ii.opcode);
      switch(ii.operand_types[0]) {
      case T_S1:
      case T_U1: os.write(param); break;
      case T_S2:
      case T_U2: os.writeShort(param); break;
      case T_S4:
      case T_U4: os.writeInt(param); break;
      }
    }
  }

  static class InstTwo extends Inst
  {
    int param1, param2;
    InstTwo(int opcode, int p1, int p2) { super(opcode); param1=p1; param2=p2; }
    public String toString() { return ii.mnemonic + " " + param1 + ", " + param2; }
    public void write(DataOutputStream os) throws IOException {
      os.write(ii.opcode);
      if (ii.opcode == OPCODE_invokeinterface) {
        os.writeShort(param1);
        os.write(param2);
        os.write(0);
        return;
      }
      switch(ii.operand_types[0]) {
      case T_S1:
      case T_U1: os.write(param1); break;
      case T_S2:
      case T_U2: os.writeShort(param1); break;
      case T_S4:
      case T_U4: os.writeInt(param1); break;
      }
      switch(ii.operand_types[1]) {
      case T_S1:
      case T_U1: os.write(param2); break;
      case T_S2:
      case T_U2: os.writeShort(param2); break;
      case T_S4:
      case T_U4: os.writeInt(param2); break;
      }
    }
  }

  static class InstJump extends Inst
  {
    boolean resolved = false;
    boolean wide = false;
    int whereIam = -1;
    String label;
    InstJump(int opcode, String label) { super(opcode); this.label = label; }
    public String toString() { return ii.mnemonic + " " + label; }
    int getEstimatedSize() { return (resolved&&!wide) ? 2 : 4; }
    public void write(DataOutputStream os) throws IOException {
      if (wide) {
        os.write(OPCODE_wide);
        os.write(ii.opcode);
        // TODO -- wide jump
      } else {
        os.write(ii.opcode);
        // TODO -- jump
      }
    }
  }

  static class InstIINC extends InstTwo
  {
    InstIINC(int var, int val) { super(OPCODE_iinc,var,val); }
    int getEstimatedSize() { return ((param2&0x01FF) < 0x0100) ? 2 : 4; }
    public void write(DataOutputStream os) throws IOException {
      if ((param2&0x01FF) < 0x0100) {
        os.write(OPCODE_iinc);
        os.write(param1);
        os.write(param2);
      } else {
        os.write(OPCODE_wide);
        os.write(OPCODE_iinc);
        os.writeShort(param1);
        os.writeShort(param2);
      }
    }
  }

  static class InstTABLESWITCH extends Inst
  {
    InstTABLESWITCH() { super(OPCODE_tableswitch); }
    // TODO -- class InstTABLESWITCH
  }

  static class InstLOOKUPSWITCH extends Inst
  {
    InstLOOKUPSWITCH() { super(OPCODE_lookupswitch); }
    // TODO -- class InstLOOKUPSWITCH
  }

///////////////////////////////////////////////////////////////////
// Internal methods
//

  public void addInst(int opcode) { insts.addElement(new Inst(opcode)); }
  public void addInst(int opcode, int p1) {
    if (p1<4) {
      switch(opcode) {
      case OPCODE_iload:  addInst(OPCODE_iload_0+p1); return;
      case OPCODE_lload:  addInst(OPCODE_lload_0+p1); return;
      case OPCODE_fload:  addInst(OPCODE_fload_0+p1); return;
      case OPCODE_dload:  addInst(OPCODE_dload_0+p1); return;
      case OPCODE_aload:  addInst(OPCODE_aload_0+p1); return;
      case OPCODE_istore: addInst(OPCODE_istore_0+p1); return;
      case OPCODE_lstore: addInst(OPCODE_lstore_0+p1); return;
      case OPCODE_fstore: addInst(OPCODE_fstore_0+p1); return;
      case OPCODE_dstore: addInst(OPCODE_dstore_0+p1); return;
      case OPCODE_astore: addInst(OPCODE_astore_0+p1); return;
      }
    }
    insts.addElement(new InstOne(opcode,p1));
  }
  public void addInst(int opcode, int p1, int p2) { insts.addElement(new InstTwo(opcode,p1,p2)); }
  public void addInst(int opcode, String label) { insts.addElement(new InstJump(opcode,label)); }

  private void finish() throws BadClassFormatException {
    if (code.max_stack < 0) throwBadClassFormatException("Stack size is not set");
    if ((code.max_locals <= 0) && (localVars!=null)) setMaxLocals();

    // To resolve jump widths
    int i;
    for (i=0; i<insts.size(); ++i) {
      Inst inst = (Inst)insts.elementAt(i);
      if (inst instanceof InstJump) {
        InstJump ij = (InstJump)inst;
        int target = getLabelIndex(ij.label);
        if (Math.abs(target-i) > 240) ij.wide = true;
        ij.resolved = true;
      }
    }
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    DataOutputStream dos = new DataOutputStream(baos);
    for (i=0; i<insts.size(); ++i) {
      try { ((Inst)insts.elementAt(i)).write(dos); }
      catch(IOException ioe) { throwBadClassFormatException(ioe.getMessage()); }
    }
    code.code = baos.toByteArray();
    code.addrs = ClassFileUtil.checkCode(code.code);
    code.attr_len = 12 + code.code.length;

    // exceptions attribute -- TODO
  }

  void throwBadClassFormatException(String s) throws BadClassFormatException {
    throw new BadClassFormatException(s + " in method '" + methodName + "'.");
  }

///////////////////////////////////////////////////////////////////
// Non-created instruction methods.
//

  public void sysPrintln(String dest, String s) {
    _getstatic("java/lang/System", dest, "Ljava/io/PrintStream;");
    _ldc(s);
    _invokevirtual("java/io/PrintStream", "println", "(Ljava/lang/String;)V");
  }
  public void sysout(String s) { sysPrintln("out",s); }
  public void syserr(String s) { sysPrintln("err",s); }

  public void _tableswitch() {}  // TODO
  public void _lookupswitch() {} // TODO

///////////////////////////////////////////////////////////////////
// Created instruction methods.
//

  public void _nop() { addInst(OPCODE_nop); }
  public void _aconst_null() { addInst(OPCODE_aconst_null); }
  public void _iconst_m1() { addInst(OPCODE_iconst_m1); }
  public void _iconst_0() { addInst(OPCODE_iconst_0); }
  public void _iconst_1() { addInst(OPCODE_iconst_1); }
  public void _iconst_2() { addInst(OPCODE_iconst_2); }
  public void _iconst_3() { addInst(OPCODE_iconst_3); }
  public void _iconst_4() { addInst(OPCODE_iconst_4); }
  public void _iconst_5() { addInst(OPCODE_iconst_5); }
  public void _lconst_0() { addInst(OPCODE_lconst_0); }
  public void _lconst_1() { addInst(OPCODE_lconst_1); }
  public void _fconst_0() { addInst(OPCODE_fconst_0); }
  public void _fconst_1() { addInst(OPCODE_fconst_1); }
  public void _fconst_2() { addInst(OPCODE_fconst_2); }
  public void _dconst_0() { addInst(OPCODE_dconst_0); }
  public void _dconst_1() { addInst(OPCODE_dconst_1); }
  public void _bipush(int val) {
    if ((val>=-1) && (val<=5)) addInst(OPCODE_iconst_0+val);
    else addInst(OPCODE_bipush,val);
  }
  public void _sipush(int val) { addInst(OPCODE_sipush, val); }
  public void _ldc(int i)      { addInst(((i & 0x0FF00)==0) ? OPCODE_ldc : OPCODE_ldc_w, i); }
  public void _ldc(float f)    { _ldc(cfw.cpFloat(f)); }
  public void _ldc(String s)   { _ldc(cfw.cpString(s)); }
  public void _ldc(long l)     { addInst(OPCODE_ldc2_w, cfw.cpLong(l)); }
  public void _ldc(double d)   { addInst(OPCODE_ldc2_w, cfw.cpDouble(d)); }
  public void _iload(String v) throws BadClassFormatException { addInst(OPCODE_iload,getLocalVarIndex(v)); }
  public void _lload(String v) throws BadClassFormatException { addInst(OPCODE_lload,getLocalVarIndex(v)); }
  public void _fload(String v) throws BadClassFormatException { addInst(OPCODE_fload,getLocalVarIndex(v)); }
  public void _dload(String v) throws BadClassFormatException { addInst(OPCODE_dload,getLocalVarIndex(v)); }
  public void _aload(String v) throws BadClassFormatException { addInst(OPCODE_aload,getLocalVarIndex(v)); }
  public void _iload(int v) throws BadClassFormatException { addInst(OPCODE_iload,v); }
  public void _lload(int v) throws BadClassFormatException { addInst(OPCODE_lload,v); }
  public void _fload(int v) throws BadClassFormatException { addInst(OPCODE_fload,v); }
  public void _dload(int v) throws BadClassFormatException { addInst(OPCODE_dload,v); }
  public void _aload(int v) throws BadClassFormatException { addInst(OPCODE_aload,v); }
  public void _iload_0() { addInst(OPCODE_iload_0); }
  public void _iload_1() { addInst(OPCODE_iload_1); }
  public void _iload_2() { addInst(OPCODE_iload_2); }
  public void _iload_3() { addInst(OPCODE_iload_3); }
  public void _lload_0() { addInst(OPCODE_lload_0); }
  public void _lload_1() { addInst(OPCODE_lload_1); }
  public void _lload_2() { addInst(OPCODE_lload_2); }
  public void _lload_3() { addInst(OPCODE_lload_3); }
  public void _fload_0() { addInst(OPCODE_fload_0); }
  public void _fload_1() { addInst(OPCODE_fload_1); }
  public void _fload_2() { addInst(OPCODE_fload_2); }
  public void _fload_3() { addInst(OPCODE_fload_3); }
  public void _dload_0() { addInst(OPCODE_dload_0); }
  public void _dload_1() { addInst(OPCODE_dload_1); }
  public void _dload_2() { addInst(OPCODE_dload_2); }
  public void _dload_3() { addInst(OPCODE_dload_3); }
  public void _aload_0() { addInst(OPCODE_aload_0); }
  public void _aload_1() { addInst(OPCODE_aload_1); }
  public void _aload_2() { addInst(OPCODE_aload_2); }
  public void _aload_3() { addInst(OPCODE_aload_3); }
  public void _iaload() { addInst(OPCODE_iaload); }
  public void _laload() { addInst(OPCODE_laload); }
  public void _faload() { addInst(OPCODE_faload); }
  public void _daload() { addInst(OPCODE_daload); }
  public void _aaload() { addInst(OPCODE_aaload); }
  public void _baload() { addInst(OPCODE_baload); }
  public void _caload() { addInst(OPCODE_caload); }
  public void _saload() { addInst(OPCODE_saload); }
  public void _istore(String v) throws BadClassFormatException { addInst(OPCODE_istore,getLocalVarIndex(v)); }
  public void _lstore(String v) throws BadClassFormatException { addInst(OPCODE_lstore,getLocalVarIndex(v)); }
  public void _fstore(String v) throws BadClassFormatException { addInst(OPCODE_fstore,getLocalVarIndex(v)); }
  public void _dstore(String v) throws BadClassFormatException { addInst(OPCODE_dstore,getLocalVarIndex(v)); }
  public void _astore(String v) throws BadClassFormatException { addInst(OPCODE_astore,getLocalVarIndex(v)); }
  public void _istore(int v) throws BadClassFormatException { addInst(OPCODE_istore,v); }
  public void _lstore(int v) throws BadClassFormatException { addInst(OPCODE_lstore,v); }
  public void _fstore(int v) throws BadClassFormatException { addInst(OPCODE_fstore,v); }
  public void _dstore(int v) throws BadClassFormatException { addInst(OPCODE_dstore,v); }
  public void _astore(int v) throws BadClassFormatException { addInst(OPCODE_astore,v); }
  public void _istore_0() { addInst(OPCODE_istore_0); }
  public void _istore_1() { addInst(OPCODE_istore_1); }
  public void _istore_2() { addInst(OPCODE_istore_2); }
  public void _istore_3() { addInst(OPCODE_istore_3); }
  public void _lstore_0() { addInst(OPCODE_lstore_0); }
  public void _lstore_1() { addInst(OPCODE_lstore_1); }
  public void _lstore_2() { addInst(OPCODE_lstore_2); }
  public void _lstore_3() { addInst(OPCODE_lstore_3); }
  public void _fstore_0() { addInst(OPCODE_fstore_0); }
  public void _fstore_1() { addInst(OPCODE_fstore_1); }
  public void _fstore_2() { addInst(OPCODE_fstore_2); }
  public void _fstore_3() { addInst(OPCODE_fstore_3); }
  public void _dstore_0() { addInst(OPCODE_dstore_0); }
  public void _dstore_1() { addInst(OPCODE_dstore_1); }
  public void _dstore_2() { addInst(OPCODE_dstore_2); }
  public void _dstore_3() { addInst(OPCODE_dstore_3); }
  public void _astore_0() { addInst(OPCODE_astore_0); }
  public void _astore_1() { addInst(OPCODE_astore_1); }
  public void _astore_2() { addInst(OPCODE_astore_2); }
  public void _astore_3() { addInst(OPCODE_astore_3); }
  public void _iastore() { addInst(OPCODE_iastore); }
  public void _lastore() { addInst(OPCODE_lastore); }
  public void _fastore() { addInst(OPCODE_fastore); }
  public void _dastore() { addInst(OPCODE_dastore); }
  public void _aastore() { addInst(OPCODE_aastore); }
  public void _bastore() { addInst(OPCODE_bastore); }
  public void _castore() { addInst(OPCODE_castore); }
  public void _sastore() { addInst(OPCODE_sastore); }
  public void _pop() { addInst(OPCODE_pop); }
  public void _pop2() { addInst(OPCODE_pop2); }
  public void _dup() { addInst(OPCODE_dup); }
  public void _dup_x1() { addInst(OPCODE_dup_x1); }
  public void _dup_x2() { addInst(OPCODE_dup_x2); }
  public void _dup2() { addInst(OPCODE_dup2); }
  public void _dup2_x1() { addInst(OPCODE_dup2_x1); }
  public void _dup2_x2() { addInst(OPCODE_dup2_x2); }
  public void _swap() { addInst(OPCODE_swap); }
  public void _iadd() { addInst(OPCODE_iadd); }
  public void _ladd() { addInst(OPCODE_ladd); }
  public void _fadd() { addInst(OPCODE_fadd); }
  public void _dadd() { addInst(OPCODE_dadd); }
  public void _isub() { addInst(OPCODE_isub); }
  public void _lsub() { addInst(OPCODE_lsub); }
  public void _fsub() { addInst(OPCODE_fsub); }
  public void _dsub() { addInst(OPCODE_dsub); }
  public void _imul() { addInst(OPCODE_imul); }
  public void _lmul() { addInst(OPCODE_lmul); }
  public void _fmul() { addInst(OPCODE_fmul); }
  public void _dmul() { addInst(OPCODE_dmul); }
  public void _idiv() { addInst(OPCODE_idiv); }
  public void _ldiv() { addInst(OPCODE_ldiv); }
  public void _fdiv() { addInst(OPCODE_fdiv); }
  public void _ddiv() { addInst(OPCODE_ddiv); }
  public void _irem() { addInst(OPCODE_irem); }
  public void _lrem() { addInst(OPCODE_lrem); }
  public void _frem() { addInst(OPCODE_frem); }
  public void _drem() { addInst(OPCODE_drem); }
  public void _ineg() { addInst(OPCODE_ineg); }
  public void _lneg() { addInst(OPCODE_lneg); }
  public void _fneg() { addInst(OPCODE_fneg); }
  public void _dneg() { addInst(OPCODE_dneg); }
  public void _ishl() { addInst(OPCODE_ishl); }
  public void _lshl() { addInst(OPCODE_lshl); }
  public void _ishr() { addInst(OPCODE_ishr); }
  public void _lshr() { addInst(OPCODE_lshr); }
  public void _iushr() { addInst(OPCODE_iushr); }
  public void _lushr() { addInst(OPCODE_lushr); }
  public void _iand() { addInst(OPCODE_iand); }
  public void _land() { addInst(OPCODE_land); }
  public void _ior() { addInst(OPCODE_ior); }
  public void _lor() { addInst(OPCODE_lor); }
  public void _ixor() { addInst(OPCODE_ixor); }
  public void _lxor() { addInst(OPCODE_lxor); }
  public void _iinc(String var, int val) throws BadClassFormatException { addInst(OPCODE_iinc, getLocalVarIndex(var), val); }
  public void _i2l() { addInst(OPCODE_i2l); }
  public void _i2f() { addInst(OPCODE_i2f); }
  public void _i2d() { addInst(OPCODE_i2d); }
  public void _l2i() { addInst(OPCODE_l2i); }
  public void _l2f() { addInst(OPCODE_l2f); }
  public void _l2d() { addInst(OPCODE_l2d); }
  public void _f2i() { addInst(OPCODE_f2i); }
  public void _f2l() { addInst(OPCODE_f2l); }
  public void _f2d() { addInst(OPCODE_f2d); }
  public void _d2i() { addInst(OPCODE_d2i); }
  public void _d2l() { addInst(OPCODE_d2l); }
  public void _d2f() { addInst(OPCODE_d2f); }
  public void _i2b() { addInst(OPCODE_i2b); }
  public void _i2c() { addInst(OPCODE_i2c); }
  public void _i2s() { addInst(OPCODE_i2s); }
  public void _lcmp() { addInst(OPCODE_lcmp); }
  public void _fcmpl() { addInst(OPCODE_fcmpl); }
  public void _fcmpg() { addInst(OPCODE_fcmpg); }
  public void _dcmpl() { addInst(OPCODE_dcmpl); }
  public void _dcmpg() { addInst(OPCODE_dcmpg); }
  public void _ifeq(String label) { addInst(OPCODE_ifeq, label); }
  public void _ifne(String label) { addInst(OPCODE_ifne, label); }
  public void _iflt(String label) { addInst(OPCODE_iflt, label); }
  public void _ifge(String label) { addInst(OPCODE_ifge, label); }
  public void _ifgt(String label) { addInst(OPCODE_ifgt, label); }
  public void _ifle(String label) { addInst(OPCODE_ifle, label); }
  public void _if_icmpeq(String label) { addInst(OPCODE_if_icmpeq, label); }
  public void _if_icmpne(String label) { addInst(OPCODE_if_icmpne, label); }
  public void _if_icmplt(String label) { addInst(OPCODE_if_icmplt, label); }
  public void _if_icmpge(String label) { addInst(OPCODE_if_icmpge, label); }
  public void _if_icmpgt(String label) { addInst(OPCODE_if_icmpgt, label); }
  public void _if_icmple(String label) { addInst(OPCODE_if_icmple, label); }
  public void _if_acmpeq(String label) { addInst(OPCODE_if_acmpeq, label); }
  public void _if_acmpne(String label) { addInst(OPCODE_if_acmpne, label); }
  public void _goto(String label) { addInst(OPCODE_goto, label); }
  public void _jsr(String label) { addInst(OPCODE_jsr, label); }
  public void _ret(String var) throws BadClassFormatException { addInst(OPCODE_ret, getLocalVarIndex(var)); }
  public void _ireturn() { addInst(OPCODE_ireturn); }
  public void _lreturn() { addInst(OPCODE_lreturn); }
  public void _freturn() { addInst(OPCODE_freturn); }
  public void _dreturn() { addInst(OPCODE_dreturn); }
  public void _areturn() { addInst(OPCODE_areturn); }
  public void _return()  { addInst(OPCODE_return);  }
  public void _getstatic(String cls, String fld, String desc) {
    addInst(OPCODE_getstatic, cfw.cpFieldRef(cls,fld,desc));
  }
  public void _putstatic(String cls, String fld, String desc) {
    addInst(OPCODE_putstatic, cfw.cpFieldRef(cls,fld,desc));
  }
  public void _getfield(String cls, String fld, String desc) {
    addInst(OPCODE_getfield, cfw.cpFieldRef(cls,fld,desc));
  }
  public void _putfield(String cls, String fld, String desc) {
    addInst(OPCODE_putfield, cfw.cpFieldRef(cls,fld,desc));
  }
  public void _invokevirtual(String cls, String mthd, String desc) {
    addInst(OPCODE_invokevirtual, cfw.cpMethodRef(cls,mthd,desc));
  }
  public void _invokespecial(String cls, String mthd, String desc) {
    addInst(OPCODE_invokespecial, cfw.cpMethodRef(cls,mthd,desc));
  }
  public void _invokestatic(String cls, String mthd, String desc) {
    addInst(OPCODE_invokestatic, cfw.cpMethodRef(cls,mthd,desc));
  }
  public void _invokeinterface(String cls, String mthd, String desc) throws BadClassFormatException {
    addInst( OPCODE_invokeinterface, cfw.cpMethodRef(cls,mthd,desc),
             1 + ClassFileUtil.getInvokeStackSizes(desc)[0] );
  }
  public void _new(String cls) { addInst(OPCODE_new, cfw.cpClass(cls)); }
  public void _newarray(int prim_type) { addInst(OPCODE_newarray, prim_type); }
  public void _anewarray(String cls) { addInst(OPCODE_anewarray, cfw.cpClass(cls)); }
  public void _arraylength() { addInst(OPCODE_arraylength); }
  public void _athrow() { addInst(OPCODE_athrow); }
  public void _checkcast(String cls) { addInst(OPCODE_checkcast, cfw.cpClass(cls)); }
  public void _instanceof(String cls) { addInst(OPCODE_instanceof, cfw.cpClass(cls)); }
  public void _monitorenter() { addInst(OPCODE_monitorenter); }
  public void _monitorexit() { addInst(OPCODE_monitorexit); }
  public void _multianewarray(String cls, int val) { addInst(OPCODE_multianewarray, cfw.cpClass(cls), val); }
  public void _ifnull(String label) { addInst(OPCODE_ifnull, label); }
  public void _ifnonnull(String label) { addInst(OPCODE_ifnonnull, label); }
  public void _goto_w(String label) { addInst(OPCODE_goto_w, label); }
  public void _jsr_w(String label) { addInst(OPCODE_jsr_w, label); }

} // end of class MethodBody.
