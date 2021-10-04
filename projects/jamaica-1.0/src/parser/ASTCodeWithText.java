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
 * Bytecode instructions for
 * <ol>
 * <li> <em>branch_instruction &lt;LABLE></em>:<code>
 *      ifeq ifne iflt ifge ifgt ifle
 *      if_icmpeq if_icmpne if_icmplt if_icmpge if_icmpgt if_icmple if_acmpeq if_acmpne
 *      goto jsr ifnull ifnonnull goto_w jsr_w
 * <li> <em>variable_instruction &lt;VARIABLE></em>:<code>
 *      iload lload fload dload aload istore lstore fstore dstore astore ret</code>
 * <li> <em>type_instruction &lt;TYPE></em>:<code>
 *      new newarray anewarray checkcast instanceof</code>
 * </ol>
 */
public class ASTCodeWithText extends ASTCodeSimple
{
  String text = null;

  public ASTCodeWithText(int id) {
    super(id);
  }

  public ASTCodeWithText(JamaicaParser p, int id) {
    super(p, id);
  }

  public void setText(String t, int line) { text = t; lineNum = line; }
  public String getText() { return text; }

  /** Accept the visitor. **/
  public Object jjtAccept(JamaicaParserVisitor visitor, Object data) throws Exception {
    return visitor.visit(this, data);
  }
}
