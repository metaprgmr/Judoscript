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

/**
 * Simple bytecode instructions: <code>
 * aconst_null iconst_m1 iconst_0 iconst_1 iconst_2 iconst_3 iconst_4 iconst_5 lconst_0 lconst_1
 * fconst_0 fconst_1 fconst_2 dconst_0 dconst_1
 * iload_0 iload_1 iload_2 iload_3 lload_0 lload_1 lload_2 lload_3
 * fload_0 fload_1 fload_2 fload_3 dload_0 dload_1 dload_2 dload_3
 * aload_0 aload_1 aload_2 aload_3 iaload laload faload daload aaload baload caload saload
 * istore_0 istore_1 istore_2 istore_3 lstore_0 lstore_1 lstore_2 lstore_3
 * fstore_0 fstore_1 fstore_2 fstore_3 dstore_0 dstore_1 dstore_2 dstore_3
 * astore_0 astore_1 astore_2 astore_3 iastore lastore fastore dastore aastore bastore castore sastore
 * pop pop2 dup dup_x1 dup_x2 dup2 dup2_x1 dup2_x2 swap
 * iadd ladd fadd dadd isub lsub fsub dsub imul lmul fmul dmul idiv ldiv fdiv ddiv
 * irem lrem frem drem ineg lneg fneg dneg ishl lshl ishr lshr iushr lushr iand land ior lor ixor lxor
 * i2l i2f i2d l2i l2f l2d f2i f2l f2d d2i d2l d2f i2b i2c i2s
 * lcmp fcmpl fcmpg dcmpl dcmpg
 * ireturn lreturn freturn dreturn areturn return
 * arraylength athrow monitorenter monitorexit nop</code>
 */
public class ASTCodeSimple extends SimpleNode {
  int opcode = -1;

  public ASTCodeSimple(int id) {
    super(id);
  }

  public ASTCodeSimple(int id, int opcode) {
    super(id);
    setOpcode(opcode);
  }

  public ASTCodeSimple(JamaicaParser p, int id) {
    super(p, id);
  }

  public void setOpcode(int cd) { opcode = cd; }
  public int getOpcode() { return opcode; }

  /** Accept the visitor. **/
  public Object jjtAccept(JamaicaParserVisitor visitor, Object data) throws Exception {
    return visitor.visit(this, data);
  }
}
