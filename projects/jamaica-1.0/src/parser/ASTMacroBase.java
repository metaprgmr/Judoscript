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

public abstract class ASTMacroBase extends ASTCodeWithText
{
  protected ArrayList params = null;

  public ASTMacroBase(int id) {
    super(id);
  }

  public ASTMacroBase(JamaicaParser p, int id) {
    super(p, id);
  }

  public void addParam(Object value) {
    if (params == null) params = new ArrayList();
    params.add(value);
  }

  public Iterator getAllVariables() {
    ArrayList ret = new ArrayList();
    int len = params==null ? 0 : params.size();
    for (int i=0; i<len; ++i)
      if (params.get(i) instanceof JavaClassCreator.VarAccess)
        ret.add(params.get(i));
    return ret.iterator();
  }

  public Object[] getParams() {
    return params == null ? null : params.toArray();
  }

  public int getParamCount() {
    return params==null ? 0 : params.size();
  }

  /** Accept the visitor. **/
  public Object jjtAccept(JamaicaParserVisitor visitor, Object data) throws Exception {
    return visitor.visit(this, data);
  }
}
