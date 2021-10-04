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

package com.judoscript.jamaica;

import java.io.*;
import com.judoscript.jamaica.parser.*;

public class Main
{
  static final String build = "2004-12-20";
  static final String version =
    "=====================================================================\n" +
    "Jamaica (JVM Macro Assembler) Version 1.0 [build:" + build + "]\n" +
    "Copyright 2004 James Jianbo Huang, http://www.judoscript.com/jamaica/\n" +
    "=====================================================================";

  public static void main(String args[]) {
    if (args.length != 1) {
      System.out.println(version + "\nSpecify a source file.");
      return;
    } 
    String fileName = args[0];
    System.out.println(version + "\nReading from file " + fileName + " . . .");

    int idx = fileName.lastIndexOf('/');
    if (idx < 0)
      idx = fileName.lastIndexOf('\\');
    String srcFileName = (idx >= 0) ? fileName.substring(idx+1) : fileName;

    // Parse the source --
    try {
      JamaicaParser parser = new JamaicaParser(new FileInputStream(fileName));
      ASTInterfaceDeclaration n = parser.CompilationUnit();

      // Do it --
      if (System.getProperty("dump") != null) {
        System.err.println("From file " + fileName);
        dump(n);
      } else if (System.getProperty("verify") != null) {
        verify(n);
        System.err.println("The class is verified Jamaica-OK.");
      } else
        create(n, srcFileName);
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

  static void create(ASTInterfaceDeclaration n, String srcFileName) throws Exception {
    // Create the class contents --
    JamaicaCreateVisitor visitor = n.createClass(srcFileName);

    // Write the class file --
    String fileName = visitor.getClassRootName() + ".class";
    FileOutputStream fos = new FileOutputStream(fileName);
    fos.write(visitor.getClassBytes());
    fos.close();
    System.err.println("Successfully created '" + fileName + "'.");
  }

  static void dump(ASTInterfaceDeclaration n) throws Exception {
    n.jjtAccept(new JamaicaDumpVisitor(), null);
  }

  static void verify(ASTInterfaceDeclaration n) throws Exception {
    n.jjtAccept(new JamaicaVerifyVisitor(), null);
  }
}

