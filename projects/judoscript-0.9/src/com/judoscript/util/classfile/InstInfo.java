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

public class InstInfo implements ClassFileConsts
{
  public String  mnemonic;
  public short   opcode;
  public short   operand_bytes;   // can be UNKNOWN
  public short[] operand_types;
  public short   stack_consume;
  public short   stack_produce;
  public short   operand_meaning; // for JASM

  InstInfo(String m, int opc, int operands, short[] optypes, int stack_c, int stack_p) {
    this(m,opc,operands,optypes,stack_c,stack_p,0);
  }

  InstInfo(String m, int opc, int operands, short[] optypes, int stack_c, int stack_p, int meaning) {
    mnemonic = m;
    opcode = (short)opc;
    operand_bytes = (short)operands;
    operand_types = optypes;
    stack_consume = (short)stack_c;
    stack_produce = (short)stack_p;
    operand_meaning = (short)meaning;
  }
}
