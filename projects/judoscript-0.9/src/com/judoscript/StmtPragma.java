/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 02-01-2005  JH   Inception.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript;

import org.apache.commons.logging.*;

import com.judoscript.util.XMLWriter;


public class StmtPragma extends StmtBase
{
  public String   name;
  public Variable value;

  public StmtPragma(int line, String name, Variable value) {
    super(line);
    this.name = name;
    this.value = value;
  }

  public void exec() throws Throwable {
    String val;

    //
    // Logger pragmas -- refer to RT.java for logger organization.
    //
    if (name.equals("logger") || name.startsWith("logger.")) {
      val = value.getStringValue();

      int idx = name.lastIndexOf('.');
      if (idx > 0) {
        if (name.substring(idx).equalsIgnoreCase(".level"))
          name = name.substring(0, idx);
      }
      if (name.equals("logger.judo.all")) {
        RT.setAllLoggerLevel(val);
      } else { // name is "logger.xxx"
        Log logger;
        if (name.equals("logger")) {
          logger = RT.userLogger;
        } else {
          logger = LogFactory.getLog(name.substring(7)); // built-in and custom logs.
        }
        RT.setLoggerLevel(val, logger);
      }
      return;
    }

    //
    // Undefined variable access policy
    //
    if (name.startsWith("undefinedAccess")) {
      RT.setUndefinedAccessPolicy(name2level(value.getStringValue()));
      return;
    }

    //
    // Assertion policy
    //
    if (name.equals("assertAs")) {
      RT.setAssertAs(name2level(value.getStringValue()));
      return;
    }

    //
    // ... OTHER PRAGMAS
    //

  } // exec().

  public static int name2level(String name) {
		name = name.toLowerCase();
		if (name.equals("error"))    return ISSUE_LEVEL_ERROR;
		if (name.equals("info"))     return ISSUE_LEVEL_INFO;
		if (name.equals("debug"))    return ISSUE_LEVEL_DEBUG;
		if (name.startsWith("warn")) return ISSUE_LEVEL_WARN;
		return ISSUE_LEVEL_IGNORE;
  }

  public void dump(XMLWriter out) {
    out.simpleSingleTagLn("StmtPragma");
    // TODO: dump().
  }

} // end of class StmtPragma.
