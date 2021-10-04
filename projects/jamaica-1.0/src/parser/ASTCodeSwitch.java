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

import java.util.HashMap;
import com.judoscript.util.IntHashtable;

/**
 * Bytecode instructions for <code>tableswitch lookupswitch</code>.
 */
public class ASTCodeSwitch extends ASTCodeWithTextInt
{
  IntHashtable cases = new IntHashtable();

  HashMap caseLabelLines = new HashMap();
  int defaultLabelLineNum = 0;


  public ASTCodeSwitch(int id) {
    super(id);
  }

  public ASTCodeSwitch(JamaicaParser p, int id) {
    super(p, id);
  }

  public void setDefault(String label, int line) { setText(label, line); }
  public int getDefaultLabelLineNum() { return lineNum; }

  public void addCase(int val, String label, int line) {
    cases.put(val, label);
    caseLabelLines.put(label, new Integer(line));
  }
  public int getCaseLabelLineNum(String label) {
    try {
      return ((Integer)caseLabelLines.get(label)).intValue();
    } catch(Exception e) {
      return 0;
    }
  }

  /**
   *@return <code>Object[]{ int[], String[] }</code>.
   */
  public Object[] getCases() {
    int[]    values = cases.intKeysSorted();
    String[] labels = new String[cases.size()];

    for (int i=0; i<values.length; ++i)
      labels[i] = (String)cases.get(values[i]);

    return new Object[]{ values, labels };
  }

  public String getDefault() { return getText(); }

  /** Accept the visitor. **/
  public Object jjtAccept(JamaicaParserVisitor visitor, Object data) throws Exception {
    return visitor.visit(this, data);
  }
}
