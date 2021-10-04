/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 08-09-2004  JH   Inception.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript;

import com.judoscript.util.XMLWriter;


public class StmtImport extends StmtBase
{
  String component;

  public StmtImport(String comp) {
    super(0);
    component = comp;
  }

  public void exec() throws Throwable {
    RT.getClasspath().addImport(component);
  }

  public void dump(XMLWriter out) {
    out.simpleSingleTagLn("StmtImport");
    // TODO: dump().
  }

} // end of class StmtSCP.
