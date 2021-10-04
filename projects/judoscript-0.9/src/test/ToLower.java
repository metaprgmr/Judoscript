/* JudoScript, The Scripting Solution for the Java Platform
 * Copyright (C) 2001-2002 James Huang, http://www.judoscript.com
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
 * 03-17-2002  JH   Initial open source release.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package test;

import java.io.*;


public class ToLower
{
  public static void main(String[] args) {
    try { 
      BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
      while (true) {
        String line = br.readLine();
        if (line == null) break;
        System.out.println(line.toLowerCase());
      }
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

}
