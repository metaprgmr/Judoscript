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

public class ASTCatchClause extends SimpleNode
{
  String exception = "java.lang.Throwable";
  String startLabel = null;
  String endLabel = null;
  String actionLabel = null;
  int startLabelLineNum = 0;
  int endLabelLineNum = 0;

  public ASTCatchClause(int id) {
    super(id);
  }

  public ASTCatchClause(JamaicaParser p, int id) {
    super(p, id);
  }

  public void setException(String ex) { exception = ex; }
  public String getException() { return exception; }

  public void setStartLabel(String s, int line) { startLabel = s; startLabelLineNum = line; }
  public String getStartLabel() { return startLabel; }
  public int getStartLabelLineNum() { return startLabelLineNum; }

  public void setEndLabel(String s, int line) { endLabel = s; endLabelLineNum = line; }
  public String getEndLabel() { return endLabel; }
  public int getEndLabelLineNum() { return endLabelLineNum; }

  public void setActionLabel(String s, int line) { actionLabel = s; lineNum = line; }
  public String getActionLabel() { return actionLabel; }
  public int getActionLabelLineNum() { return lineNum; }

  /** Accept the visitor. **/
  public Object jjtAccept(JamaicaParserVisitor visitor, Object data) throws Exception {
    return visitor.visit(this, data);
  }
}
