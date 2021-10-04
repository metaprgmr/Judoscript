/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 04-25-2003  JH   Initial open source release.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript;

import java.io.*;
import java.util.*;
import com.judoscript.parser.JudoParser;


public class ScriptCache
{
  HashMap scripts = new HashMap(); // String => Script
  File defaultBase = null;


  public void setDefaultBase(String path) throws IllegalArgumentException {
    defaultBase = new File(path);
    if (!defaultBase.isDirectory())
      throw new IllegalArgumentException("Path '" + path + "' must be a directory.");
  }

  public boolean acceptsRelativePath() {
    return defaultBase != null;
  }

  public Script getScript(String path) throws Exception {
    File file = new File(path);
    if (!file.exists() && (defaultBase != null))
      file = new File(defaultBase, path);
    path = file.getPath();
    long modtime = file.lastModified();
    Script script = (Script)scripts.get(path);
    if (script != null && script.getLastModified() < modtime)
      script = null; // reload
    if (script == null) {
      synchronized(this) {
        script = (Script)scripts.get(path);
        if (script != null && script.getLastModified() < modtime)
          script = null; // reload
        if (script == null) {
          script = JudoParser.parse(file.getName(),
                                    file.getParent(),
                                    new FileReader(file),
																		null,
                                    modtime, false);
          scripts.put(path, script);
        }
      }
    }
    return script;
  }

} // end of class ScriptCache.

