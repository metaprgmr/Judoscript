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

public final class ASTMacroIterate extends ASTMacroBase
{
  boolean isArray = false;
  String itervar = null;
  int itervar_line = 0;
  String id = null; // to match begin/end macros. Also used as label prefix.
  boolean isEnd = false;

  public ASTMacroIterate(int id) {
    super(id);
  }

  public ASTMacroIterate(JamaicaParser p, int id) {
    super(p, id);
  }

  public void setName(String n, int line) { setText(n, line); }
  public String getName() { return getText(); }

  public JavaClassCreator.VarAccess getCollection() {
    try { return (JavaClassCreator.VarAccess)params.get(0); } catch(Exception e) { return null; }
  }
  public JavaClassCreator.VarAccess getArray() { return getCollection(); }

  public void setIterateVar(String t, int line) { itervar = t; itervar_line = line; }
  public String getIterateVar() { return itervar; }
  public int getIterateVarLine() { return itervar_line; }

  public void setIsArray(boolean set) { isArray = set; }
  public boolean isArray() { return isArray; }

  public void setID(String s) { id = s; }
  public String getID() { return id; }

  public void setIsEnd(ASTMacroIterate opener) {
    id = opener.id;
    isArray = opener.isArray;
    isEnd = true;
    itervar = opener.itervar;
    itervar_line = opener.itervar_line;
  }

  public boolean isEnd() { return isEnd; }

  /** Accept the visitor. **/
  public Object jjtAccept(JamaicaParserVisitor visitor, Object data) throws Exception {
    return visitor.visit(this, data);
  }
}
