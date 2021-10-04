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
 * 03-17-2002  JH   Initial open source release.
 * 08-11-2002  JH   Added printJHList() to show the internal debug flags.
 * 05-20-2003  JH   Remove jh and related methods.
 *                  Move from com.judoscript.Main to this class.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


import java.io.*;
import java.util.*;
import com.judoscript.*;
import com.judoscript.util.UserClassLoader;
import com.judoscript.parser.ParseException;
import com.judoscript.parser.helper.ParserHelper;
import com.judoscript.util.XMLWriter;

public class judo
{
  public static void printHeader() {
    System.err.println(VersionInfo.divider);
    System.err.println(VersionInfo.latest());
    System.err.println(VersionInfo.copyright);
    System.err.println("Tell your friends. Enjoy USING Java!");
    System.err.println(VersionInfo.jvmVersion);
    Calendar c = new GregorianCalendar();
    boolean isMonday = c.get(Calendar.DAY_OF_WEEK) == 2;
    if (isMonday)
      System.err.println("Monday is a good time to check for the latest version.");
    System.err.println();
  }

  public static void main(String args[]) {
//    UserClassLoader.isSystemLoader = true;

    String fileName;
    boolean doCompile = false;
    boolean doDump = false;
    boolean doDebug = false;
    boolean quiet = false;
    String[] sa = null;
    Script script = null;

    try {
      String x;
      if (args.length == 0) {
        printHeader();
        System.err.println("Type in JudoScript code, finish by the 'end' command. ");
        System.err.println(VersionInfo.divider);
        System.err.println();
        script = ParserHelper.parse(null, null, System.in, null, 0, false);
        System.err.println(VersionInfo.divider1);
      } else {
        long startTime = System.currentTimeMillis();
        int progIdx = 0;
        x = args[0];
        if (x.startsWith("-")) {
          if (x.equals("-h") || "--help".equalsIgnoreCase(x)) {
            System.err.println("java judo [option] filename_or_code\n" +
                               "option is one of these:\n" +
                               " -h or --help:    this help screeen.\n" +
                               " -c or --compile: compile.\n" +
                               " -d or --dump:    dump.\n" +
                               " -q or --quiet:   quiet.\n" +
                               " -x or --exec:    commandline arguments as program.\n" +
                               " -l=level or --logger=level:\n" +
                               "     start the program with a given logging level.\n" +
                               "     Valid levels are: 'trace', 'debug', 'info', 'warn', 'error', 'fatal',\n" +
                               "     from most verbose to least.");
            System.exit(0);
          }
          progIdx = 1;
          doCompile = x.equals("-c") || "--compile".equalsIgnoreCase(x);
          doDump    = x.equals("-d") || "--dump".   equalsIgnoreCase(x);
          doDebug   = x.equals("-Y") || "--debug".  equalsIgnoreCase(x);
          quiet     = x.equals("-q") || "--quiet".  equalsIgnoreCase(x);
          if (x.equals("-x") || "--exec".equalsIgnoreCase(x)) {
            // concatenate everything on command line to form a program.
            if (progIdx < args.length) {
              StringBuffer sb = new StringBuffer();
              while (progIdx < args.length) {
                sb.append(args[progIdx++]);
                sb.append(" ");
              }
              sb.append(";");
              script = ParserHelper.parse(null, null, new StringReader(sb.toString()), null, 0, false);
            }
          }
          if (x.startsWith("-l")) {
            int idx = x.indexOf('=');
            if (idx > 0) {
              x = x.substring(idx+1);
              RT.setAllLoggerLevel(x);
              quiet = x.startsWith("warn") || x.equals("error") || x.equals("fatal");
            }
          }
        }
        if (progIdx >= args.length) { // choice 1.
          if (script == null)
            script = ParserHelper.parse(null, null, System.in, null, 0, false);
        } else {
          fileName = args[progIdx++];
          if (args.length > progIdx) {
            sa = new String[args.length-progIdx];
            System.arraycopy(args, progIdx, sa, 0, sa.length);
          }

          script = ParserHelper.parse(fileName, fileName, JudoUtil.findFile(fileName, null), null, 0, false);
          if (!quiet) {
            startTime = System.currentTimeMillis() - startTime;
            printHeader();
            RT.logger.info("Initialization and parsing: " + ((double)startTime/(double)1000) + " seconds.");
            RT.logger.info(VersionInfo.divider);
          }
        }
      }

      if (!doCompile) {
        if (doDump)
          script.dump(new XMLWriter(System.out));
        else if (!doDebug)
          script.start(sa, true);
        else { // for debug
          Debugger dbg = new Debugger() {
            protected void breakPoint(Stmt stmt) {
              System.err.println(stmt.getLineNumber());
              System.err.flush();
            }
            public void finish() {}
          };
          dbg.init(sa, script);
          dbg.setSingleStep(true);
          dbg.setStepInto(true);
          dbg.start();
        }
      }

    }
    catch (ParseException pe) {
      RT.logger.error("Parsing failed.", pe);
      System.exit(1);
    } catch (Exception e) {
      RT.logger.error("Failed to run.", e);
      System.exit(1);
    }
  }

} // end of class judo.
