/* JudoScript, The Scripting Solution for the Java Platform
 * Copyright (C) 2001-2003 James Huang, http://www.judoscript.com
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
 * 05-18-2003  JH   Inception.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


import com.judoscript.VersionInfo;
import com.judoscript.util.RemoteExecClient;


public final class rjudo_server
{
  public static void main(String[] args) {
    VersionInfo.printHeader("Remote JudoScript Server");

    String[] remote_args = new String[args.length+1];
    System.arraycopy(args,0,remote_args,1,args.length);
    remote_args[0] = System.getProperty("remote_judo", "REMOTE_JUDO");

    //RemoteExecClient.progName = "rjudo_server";
    RemoteExecClient.main(remote_args);
  }
}

