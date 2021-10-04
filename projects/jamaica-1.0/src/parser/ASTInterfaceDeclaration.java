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

public class ASTInterfaceDeclaration extends SimpleNode
{
  String name = null;
  String[] interfaces = null;

  public ASTInterfaceDeclaration(int id) {
    super(id);
  }

  public ASTInterfaceDeclaration(JamaicaParser p, int id) {
    super(p, id);
  }

  public void setName(String n, int line) { name = n; lineNum = line; }
  public String getName() { return name; }

  public void setExtends(String[] sa) { interfaces = sa; }
  public String[] getExtends() { return interfaces; }

  /** Accept the visitor. **/
  public Object jjtAccept(JamaicaParserVisitor visitor, Object data) throws Exception {
    return visitor.visit(this, data);
  }

  //////////////////////////////////////////////////////////////////////////
  //
  // The creation method.
  //
  //////////////////////////////////////////////////////////////////////////

  public JamaicaCreateVisitor createClass(String srcFileName) throws Exception {
    JamaicaCreateVisitor visitor = new JamaicaCreateVisitor();
    visitor.setSourceFileName(srcFileName);
    try {
      jjtAccept(visitor, null);
      return visitor;
    } catch(Exception e) {
      jjtAccept(new JamaicaVerifyVisitor(), null);
      throw e;
    }
  }
}
