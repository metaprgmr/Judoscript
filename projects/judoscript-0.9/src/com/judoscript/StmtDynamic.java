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

import java.io.File;
import java.io.StringReader;
import com.judoscript.parser.JudoParser;
import com.judoscript.util.Lib;
import com.judoscript.util.XMLWriter;


public class StmtDynamic extends StmtBase implements Consts
{
  Expr content;
  int  type;
  Expr[] args;
  Expr dir;

  public StmtDynamic(int line, int type, Expr content, Expr dir, Expr[] args) {
    super(line);
    this.type = type;
    this.content = content;
    this.dir = dir;
    this.args = args;
  }

  public void exec() throws Throwable {
    Script script;
    String x = content.getStringValue().trim();
    if (type == DYNAMIC_EVALFILE) {
      String path = null;
      if (dir != null) {
        path = dir.getStringValue();
        x = path + x;
      }
      script = JudoParser.parse(Lib.getFileName(x), new File(x), null, 0, false);
    } else {
      if (!x.endsWith(";"))
      	x += ";";
      script = JudoParser.parse("<string>", "./", new StringReader(x), null, 0, false);
    }

    switch (type) {
    case DYNAMIC_EVALEXTERNAL:
    case DYNAMIC_EVALFILE:
      script.startAllowException(RT.calcValuesAsStrings(args,true), RT.curCtxt());
      break;
      
    case DYNAMIC_EVAL:
      RT.getScript().takeDecls(script);
      script.exec(); // treating it as a simple block; all decls therein are ignored!
      break;
    }
  }

  public void dump(XMLWriter out) {
    // TODO: dump().
  }

} // end of class StmtDynamic.
