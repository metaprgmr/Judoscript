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
import java.util.Iterator;

public final class ASTMacroPrint extends ASTMacroBase
{
  public static final int TYPE_print   = 0;
  public static final int TYPE_println = 1;
  public static final int TYPE_flush   = 2;
  int type = TYPE_print;
  ArrayList params = null;


  public ASTMacroPrint(int id) {
    super(id);
  }

  public ASTMacroPrint(JamaicaParser p, int id) {
    super(p, id);
  }

  public String getName() {
    switch(type) {
    case TYPE_flush: return "flush";
    case TYPE_print: return "print";
    default:         return "println";
    }
  }

  public void setType(int t) { type = t; }
  public int getType() { return type; }

  public void setTarget(String t) { text = t; }
  public String getTarget() { return text==null ? "out" : text; }

  /** Accept the visitor. **/
  public Object jjtAccept(JamaicaParserVisitor visitor, Object data) throws Exception {
    return visitor.visit(this, data);
  }
}
