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
 * Bytecode instructions with a constant (integer, long, float, double or string):
 * <code>ldc ldc_w ldc2_w</code>
 */
public class ASTCodeWithConstant extends ASTCodeWithText
{
  Object constant = null;

  public ASTCodeWithConstant(int id) {
    super(id);
  }

  public ASTCodeWithConstant(JamaicaParser p, int id) {
    super(p, id);
  }

  public void setType(String t) { text = t; }
  public String getType() { return text; }

  public void setConstant(Object val) { constant = val; }
  public Object getConstant() { return constant; }
  public int getInt() { return ((Number)constant).intValue(); }
  public long getLong() { return ((Number)constant).longValue(); }
  public float getFloat() { return ((Number)constant).floatValue(); }
  public double getDouble() { return ((Number)constant).doubleValue(); }
  public String getString() { return (String)constant; }

  /** Accept the visitor. **/
  public Object jjtAccept(JamaicaParserVisitor visitor, Object data) throws Exception {
    return visitor.visit(this, data);
  }
}
