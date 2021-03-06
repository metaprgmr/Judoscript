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

import java.util.ArrayList;
import com.judoscript.jamaica.MyUtils;

/**
 * Bytecode instructions for <code>invokevirtual invokespecial invokestatic invokeinterface</code>.
 */
public class ASTCodeInvoke extends ASTCodeMemberAccess
{
  ArrayList paramTypes = new ArrayList();

  public ASTCodeInvoke(int id) {
    super(id);
  }

  public ASTCodeInvoke(JamaicaParser p, int id) {
    super(p, id);
  }

  public void addParamType(String ptype) { paramTypes.add(ptype); }
  public String[] getParamTypes() { return MyUtils.toStringArray(paramTypes); }

  /** Accept the visitor. **/
  public Object jjtAccept(JamaicaParserVisitor visitor, Object data) throws Exception {
    return visitor.visit(this, data);
  }
}
