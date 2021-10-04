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

import com.judoscript.jamaica.JavaClassCreator;
import java.util.ArrayList;

public final class ASTMacroObject extends ASTMacroBase implements JavaClassCreator.AssignableMacro
{
  ArrayList paramTypes = null;

  public ASTMacroObject(int id) {
    super(id);
  }

  public ASTMacroObject(JamaicaParser p, int id) {
    super(p, id);
  }

  public void setType(String t) { text = t; }
  public String getType() { return text; }

  public void addParamType(String type) {
    if (paramTypes == null)
      paramTypes = new ArrayList();
    paramTypes.add(type);
  }

  public String[] getParamTypes() {
    if (paramTypes==null)
      return null;
    String[] sa = new String[paramTypes.size()];
    paramTypes.toArray(sa);
    return sa;
  }

  /** Accept the visitor. **/
  public Object jjtAccept(JamaicaParserVisitor visitor, Object data) throws Exception {
    return visitor.visit(this, data);
  }

  /**
   * JavaClassCreator$AssignableMacro method.
   *@param p1 should be a JamaicaVisitor.
   *@param p2 is the extra data for the visitor.
   *@param p3 is the type as String.
   */
  public void instantiate(Object p1, Object p2, Object p3) throws Exception {
    jjtAccept((JamaicaParserVisitor)p1, p2);
  }

}
