/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 03-17-2002  JH   Initial open source release.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript;

import com.judoscript.util.XMLWriter;


public class ExprColumn extends ExprAnyBase
{
  int    index;
  String name;

  public ExprColumn(String name) { this.name = name; index = -1;  }
  public ExprColumn(int idx)     { name = null;      index = idx; }

  public Variable eval() throws Throwable {
    ExprCollective row = RT.getTableDataSource().currentRow();
    if (name != null)
      return row.resolveVariable(name);
    return row.resolve(ConstInt.getInt(index));
  }

  public int getType() { return TYPE_UNKNOWN; }
  public boolean isNil() { return false; }
  public boolean isValue() { return true; }
  public boolean isReadOnly() { return true; }

  public void dump(XMLWriter out) {
    out.openTag("ExprColumn");
    out.tagAttr("index", (name==null) ? ""+index : name);
    out.endTagLn();
  }

} // end of class ExprColumn.
