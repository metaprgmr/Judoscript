/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 03-17-2002  JH   Initial open source release.
 *                  See VersionInfo[] releases for change history.
 * 08-08-2004  JH   Moved to this package for user access.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript;

import org.apache.commons.lang.StringUtils;

/**
 * Keeps release information.
 *<p>
 * Maintains a constant array of all releases.
 */
public final class VersionInfo
{
  public static final String divider =
     "=================================================================";
  public static final String divider1 =
     ".................................................................";
  public static final String copyright =
     "Copyright 2001-2005 James Jianbo Huang, http://www.judoscript.com";
  public static final String jvmVersion =
     "Java Runtime Version: " + System.getProperty("java.runtime.version");

  public static void printHeader(String _title) {
    RT.logger.info(divider);
    RT.logger.info(_title);
    RT.logger.info(copyright);
    RT.logger.info(jvmVersion);
    RT.logger.info(divider);
  }

  public String PRODUCT_NAME;
  public String VERSION_TYPE;
  public int    MAJOR_VERSION;
  public int    MINOR_VERSION;
  public String SUFFIX;
  public String BUILD_DATE;
  public String JDK_VERSIONS_SUPPORTED;

  private VersionInfo(String name, String type, int major, int minor,
                      String suffix, String date, String jdk)
  {
    PRODUCT_NAME  = name;
    VERSION_TYPE  = type;
    MAJOR_VERSION = major;
    MINOR_VERSION = minor;
    SUFFIX        = suffix;
    BUILD_DATE    = date;
    JDK_VERSIONS_SUPPORTED = jdk;
  }

  public String getVersionID() {
    String s = "";
    if (StringUtils.isNotBlank(VERSION_TYPE)) s = VERSION_TYPE;
    return s+MAJOR_VERSION+"."+MINOR_VERSION+SUFFIX;
  }

  public String toString() {
    StringBuffer sb = new StringBuffer(PRODUCT_NAME);
    sb.append(" ");
    sb.append(getVersionID());
    sb.append(" ");
    sb.append(BUILD_DATE);
    sb.append(" (JDK");
    sb.append(JDK_VERSIONS_SUPPORTED);
    sb.append(")");
    return sb.toString();
  }

  public static VersionInfo latest() { return CURRENT; }
  public static String latestID()    { return CURRENT.getVersionID(); }

  //
  // RELEASE HISTORY
  //

  static final VersionInfo CURRENT =
  //new VersionInfo("JudoScript Language", "pre", 0, 1, "", "2001-11-11", "1.3+");
        // The very first public release. -- [HISTORY] November 2001
        //
        // Documentation:
        // a) Featured articles:
        //     Introduction to JudoScript    -- intro.htm_
        //     Uses of JudoScript            -- uses.htm_
        //     Java, Exceptions & EJB        -- java.htm_
        //     JDBC Scripting                -- jdbc.htm_
        //     XML Scripting                 -- xml.htm_
        //     Values & Data Structures      -- value_ds.htm_
        //     Function, Class, Thread, Eval -- fxn_cls_etc.htm_
        //     Miscellaneous Topics          -- misc.htm_
        //     Files, Directories & Archives -- file_dir.htm_
        //     Schedule, Executes, E-Mail    -- sched_exec_mail.htm_
        //     HTTP Fun & HTML Processing    -- http_html.htm_
        //     Build GUI Applications        -- gui.htm_
        // b) Language Specifications: some just brief (e.g. expresions).
        // c) Examples: all the code in the articles plus some more.
        //
        // TO-DO list:
        // a) Application features:
        //    * Advanced JDBC
        //    * Encryption
        //    * Sound and MIDI music
        //    * Graphics 2D & 3D; animation
        //    * Other enterprise features (LDAP, Windows Registry, Unix SysLog, ...)
        // b) Language features:
        //    * Error messages (included files, parser/runtime)
        //    * Precompiling; runtime configuration
        //    * Shell
        //    * GUI shell; studio w/debugger

  //new VersionInfo("JudoScript Language", "pre", 0, 1, "a", "2001-11-20", "1.3+");
        //  1  JDBC known drivers added PostgreSQL: "org.postgresql.Driver,postgressql.Driver".
        //  2  Make sure "with" clause is not mandatory in prepared statements.

  //new VersionInfo("JudoScript Language", "pre", 0, 2, "", "2001-12-15", "1.3+");
        //  1  BSF support (#23, p6)  -- public
        //  2  more HTML example (#24, p5)
        //  3  next method for date (#28, p5)
        //  4  added fileExists method for ZipArchive (#29, p5)
        //  5  Sys linked to xml packages (#30, p1)
        //  6  add Java bean property introspection (#30, p5)

  //new VersionInfo("JudoScript Language", "pre", 0, 3, "", "2002-2-7", "1.3+");
        //  1  add regex support (#3, p6) -- public
        //  2  added ftp support (#8, p6) -- public
        //  3  added database table dump (#21, p5) -- public
        //  4  fixed function calls in expression thows null exceptoin (#33, p1)
        //  5  fixed zip files with no folder entries failure (#34, p1)
        //  6  JDBC allow calls to PreparedStatement and QueryResult methods (#35,39, p6) -- public
        //  7  fixed add charset= support for email.
        //  8  added evalFile (#37, p6) -- public
        //  9  added chown/chgrp/chmod (#38, p6) -- public
        // 10  added TAR support (#40, p6) -- public
        // 11  added tableData support (#46, p6) -- public
        // 12  added printTable support (#47, p6) -- public

  //new VersionInfo("JudoScript Language", "pre", 0, 4, "", "2002-3-16", "1.3+");
        //  1  added generic Java package support (#48, p6)
        //  2  added SSH and SCP support (#50, p6)
        //  3  added countLines to list/ls commands (#51, p6)
        //  4  added support for init script ".judoscript" (#10, p6)
        //  5  added support for Windows Registry via com.ice.jni.registry. (#45, p6)
        //  6  added support for conditional include and const definition. (#53, p6)
        //  7  added support for encryption using JCE. (#1, #2, p6)
        //  8  added support for XSLT. (#?, p6)

  //new VersionInfo("JudoScript Language", "v", 0, 5, "", "2002-3-17", "1.3+");
        //  1  open source under LGPL (#20)! -- [HISTORY] March 2002
        //  2  added transform to copy command (#54)
        //  3  implemented a console debugger and the underlying debugging support.
        //  4  added Java code white-box testing feature.
        //  5  fixed a copy bug that '.tar.gz' sometimes does not work.

  //new VersionInfo("JudoScript Language", "v", 0, 6, "", "2002-8-4", "1.3+");
        //  1  changed package name from 'wws.judo' to 'com.judoscript'.
        //  2  added "ear" and "war" in addition to "jar" for FS commands.
        //  3  String contains() method also works with regex's.
        //  4  HTTP service, fixed the problem when the docroot parameter is null.
        //  5  Fixed a problem with tar files when a directory name is present.
        //  6  Added "hsqldb" to the known JDBC driver list.
        //  7  Added <!--> as a separate case than <!>; <!> includes <!--> only if
        //     the latter is not handled. Also fixed MarkupParser.java for such
        //     situations: <!--...>...-->.
        //  8  UserDefined, added method "copy(UserDefined)".
        //  9  Markup now has a selfClosed flag; MarkupParser parses <.../> as well.
        // 10  Added isA() method to all value objects. (see ExprAnyBase.java)
        // 11  Added "sgml" as a synonym to "html"; may later add case sensitivity.
        // 12  Added "isOdd" and "isEven" user methods to values.
        // 13  Added "fmt/formatRoman" and "parseIntRoman" user methods to numeric values.
        // 14  Added "isAlpha/isLetter", "isAlnum/isLetterOrDigit" and
        //     "isWhite/isWhitespace" user methods to character values.
        // 15  Added "exists" user methods to array.
        // 16  Added "createTreeOutput" system method

  //new VersionInfo("JudoScript Language", "v", 0, 7, "", "2002-11-23", "1.3+");
        //  0  Comprehensive documentation -- [HISTORY] August 2002
        //  1  Variable names no longer need $ in front!
        //  2  Changed the way static Java member usage and function alias declarations
        //     for Java static methods
        //  3  do ... as lines {}.

  //new VersionInfo("JudoScript Language", "v", 0, 8, "", "2002-12-23", "1.3+");
        //  1  Java extension support. -- [HISTORY] December 2002
        //  2  Added ${...} support for environment variables.
        //  3  Added user-defined custom built-in properties support.
        //  4  Move many built-in support into properties files.
        //  5  Usage mechanism.

  //new VersionInfo("JudoScript Language", "v", 0, 8, "a", "2003-6-7", "1.3+");
        //  1  Added COM/ActiveX support -- [HISTORY] December 2002
        //  2  Added "as tree" to "list" command.
        //  3  'exec' now accepts both input and output at the same time.
        //  4  Remote JudoScript engine -- [HISTORY] May 2003
        //  5  Enabled variables as keys in struct, which leads to many changes.

  //new VersionInfo("JudoScript Language", "v", 0, 9, "", "2003-7-15", "1.3+");
  //new VersionInfo("JudoScript Language", "v", 0, 9, "", "2003-8-11", "1.3+");
  //new VersionInfo("JudoScript Language", "v", 0, 9, "", "2003-8-24", "1.3+");
  //new VersionInfo("JudoScript Language", "v", 0, 9, "", "2003-8-27", "1.3+");
  //new VersionInfo("JudoScript Language", "v", 0, 9, "", "2003-8-28", "1.3+");
  //new VersionInfo("JudoScript Language", "v", 0, 9, "", "2003-9-1",  "1.3+");
  //new VersionInfo("JudoScript Language", "v", 0, 9, "", "2003-9-4",  "1.3+");
  //new VersionInfo("JudoScript Language", "v", 0, 9, "", "2003-11-11","1.3+"); // substring() fix
  //new VersionInfo("JudoScript Language", "v", 0, 9, "", "2003-12-31","1.3+"); // html/unicodeUn/Escape()
  //new VersionInfo("JudoScript Language", "v", 0, 9, "", "2004-1-7",  "1.3+"); // Fix jndi for weblogic/...
  //new VersionInfo("JudoScript Language", "v", 0, 9, "", "2004-1-12", "1.3+"); // Fix keywords
  //new VersionInfo("JudoScript Language", "v", 0, 9, "", "2004-1-24", "1.3+"); // Fix keywords
  //new VersionInfo("JudoScript Language", "v", 0, 9, "", "2004-1-28", "1.3+");
  //new VersionInfo("JudoScript Language", "v", 0, 9, "", "2004-2-3",  "1.3+");
  //new VersionInfo("JudoScript Language", "v", 0, 9, "", "2004-2-11", "1.3+");
  //new VersionInfo("JudoScript Language", "v", 0, 9, "", "2004-2-26", "1.3+");
  //new VersionInfo("JudoScript Language", "v", 0, 9, "", "2004-2-28", "1.3+");
  //new VersionInfo("JudoScript Language", "v", 0, 9, "", "2004-3-4",  "1.3+");
  //new VersionInfo("JudoScript Language", "v", 0, 9, "", "2004-3-9",  "1.3+"); // for x in ExprCollective
  //new VersionInfo("JudoScript Language", "v", 0, 9, "", "2004-3-10", "1.3+"); // OrderedMap: clone value
  //new VersionInfo("JudoScript Language", "v", 0, 9, "", "2004-3-11", "1.3+"); // "~/" in FS commands.
  //new VersionInfo("JudoScript Language", "v", 0, 9, "", "2004-3-15", "1.3+"); // "~/" in FS commands.
  //new VersionInfo("JudoScript Language", "v", 0, 9, "", "2004-3-27", "1.3+"); // "~/" in !include.
  //new VersionInfo("JudoScript Language", "v", 0, 9, "", "2004-3-28", "1.3+"); // var with multiple vars

  //new VersionInfo("JudoScript Language", "v", 0, 9, "", "2004-3-31", "1.3+"); // classloader; markupparser
        //  1  Major internal changes: [HISTORY]
        //     a) use thread-local storage for RuntimeContext.
        //     b) Change "Context" to "Frame" to avoid confusion.
        //  2  Use [..] for array literals and { a:expr } for struct literal.
        //     Support new Object() and new Array(), for JavaScript compatibility. [HISTORY]
        //     Added var/delete/typeof operators -- need to finish implementation.

  //new VersionInfo("JudoScript Language", "", 0, 9, "", "2004-8-23", "1.3+");
        //  1  Add import for Java.
        //  2  exec runs from pwd() by default. Can take multi-line command line.
        //  3  getScriptPath().
        //  4  for..in handles iterations, and add an interation count mechanism.
        //  5  For query, result can be an iteration.
        //  6  do..as-lines internally becomes for..in.
        //  7  $$local can be reused.
        //  8  ${::name}
        //  9  ${name} in !include
        // 10  Added last() and first() to Array/LinkedList.
        // 11  Ant scripting and AntJudoScriptTask. [HISTORY]
        // 12  ${abc.def} for system properties accesses.
        // 13  User classpath management. [HISTORY]
        // 14  exec runs synchronously by default; run asynchronously with "exec &&".

  //new VersionInfo("JudoScript Language", "", 0, 9, "", "2004-9-13", "1.3+");
        //  1  listFiles can now take remove/setFileTime/setReadOnly actions and also
        //     exec OS-command clause or {} for local processing.
        //     copy's transform clause is removed; use listFiles { ... }
        //  2  copy can now copy URL resources.
        //  3  Reduced mkdir and move/rename commands to system functions.

  //new VersionInfo("JudoScript Language", "", 0, 9, "", "2004-9-22", "1.3+");
        //  1  Allow dots in keys in Object initialization.
        //  2  Allow name-value pair initialization of new java.util.Map instances.
        //  3  #classpath.add() can take any array now.
        //  4  Support named bind parameters within prepared SQL statements.
        //  5  Added addToClasspath and limit clause in listFiles.
        //  6  Added closure-like syntax for annonymous function declaration.

  //new VersionInfo("JudoScript Language", "", 0, 9, "", "2004-11-3", "1.3+");
        //  1  Use db:: namespace for all SQL scripting statements/functions. [HISTORY]
        //  2  Use mail:: namespace for mail statements/functions.
        //  3  Use new com:: to obtain ActiveX controls.
        //  4  Use gui::events instead of guiEvents.
        //  5  Change eval/evalExternal/evalFile to systems functions and become
        //     eval, evalFile, evalSeparate and evalFileSeparate.
        //  6  Change SGML/XML events to be prefixed with ":".

  //new VersionInfo("JudoScript Language", "", 0, 9, "", "2004-11-9", "1.3+");
        //  1  Added and improved a number of methods to the simple (string) type:
        //     - Added splitWithMatches() and splitWithMatchesOnly() methods to
        //       return arrays of matches.
        //     - Added writeToFile() method.
        //     - startsWith() and endsWith() methods can take mutiple parameters,
        //       where any match will return true.
        //     - Fixed count().
        //     - trim() method can take a parameter.
        //  2  listFiles now has fileOnly as default.

  //new VersionInfo("JudoScript Language", "", 0, 9, "", "2004-11-14", "1.3+");
        //  1  Added do as jsp statement.
        //  2  For copy to archive command, added dupOk option to allow duplicate entries.
        //  3  Added saveProperties() system function.

  //new VersionInfo("JudoScript Language", "", 0, 9, "", "2004-11-18", "1.3+");
        //  1  Use statement for custom actions in listFiles (rather than anonymous function).
        //  2  Added isAsciiOnly() method to simple type.

  //new VersionInfo("JudoScript Language", "", 0, 9, "", "2004-11-27", "1.3+");
        //  1  Add wsdl:: support! [HISTORY]
        //  2  Allow Java primitive type arrays to be created without java:: prefix.
        //  3  Added escape/unescape methods to the simple type.

  //new VersionInfo("JudoScript Language", "", 0, 9, "", "2004-12-30", "1.3+");
        //  1  Added to simple type leftOf() and rightOf() methods.
        //  2  Added range operator for arrays, and strings can be accessed like arrays
        //     for their characters.
        //  3  Added 'upto' option in addition to 'to' for for-from and for-in statements.
        //  4  Member init list now takes expressions, such as static Java fields, as keys.
  //new VersionInfo("JudoScript Language", "", 0, 9, "", "2005-1-30", "1.3+");
        //  1  Hibernate scripting! [HISTORY]
        //  2  Added function annotation.
        //  3  Made all Serializable.
        //  4  Added getApacheContext() function.
  //new VersionInfo("JudoScript Language", "", 0, 9, "", "2005-2-23", "1.3+");
        //  1  Added robust logging using commons-logging, supporting JDK1.4, Log4J and SimpleLog.
        //  2  Majorly boosted JUSP technology. [HISTORY]
        //  3  Added embedded functions (as function variables pointing to global anon functions).
        //  4  Added !pragma undefinedAccessPolicy.
        //  5  Added !pragma assertAs.
        //  6  Added $$context and $$annotation for user functions.
        //  7  User-defined class constructors now takes regular parameters.
        //     (Named initialization still valid.)
        //  8  Added the ":=" copy operator.
  //new VersionInfo("JudoScript Language", "", 0, 9, "", "2005-3-11", "1.3+");
        //  1  Added a few new methods to JuspContext.
        //  2  Support :xxx options for exec. Added "asis" option not to
        //     convert command line and env var values to OS style.
        //  3  eval/File now takes parameters.
  //new VersionInfo("JudoScript Language", "", 0, 9, "", "2005-3-12", "1.3+");
        //  1  Fixed a minor (but definitive) JuSP bug.
  //new VersionInfo("JudoScript Language", "", 0, 9, "", "2005-3-16", "1.3+");
        //  1  Have db:: statements take an expression rather than a name for the connection object.
        //  2  Fixed a db::sql bug.
        //  3  Extend db:: statements to use expression for the connection object.
  //new VersionInfo("JudoScript Language", "", 0, 9, "", "2005-3-21", "1.3+");
        //  1  Fixed db:: statements with using expression for the connection object.
        //  2  judo.java: if -l=warn/error/fatal, -q is on automatically.
        //  3  Use local variable to hold the mail server object.
  //new VersionInfo("JudoScript Language", "", 0, 9, "", "2005-4-6", "1.3+");
        //  1  Added pushd and popd commands.
        //  2  Can do 'Abc'.toLower();
        //  3  Added loadProperties(file, eval); where eval can handle ${} replacement in values.
        //  4  Added these methods to String: chomp(), replaceTags(), linesToArray() and writeToZip().
        //  5  copyStream() can take null for output stream.
        //  6  Added support for (x, y, ..) = array; and var (x, y, ..) = array;
        //  7  Added `..` and ``..`` expression to take shell executable output as string or array.
        //  8  Added "copy .. into .. as .." for single file archiving.
        //  9  Added openSocket() function.
  //new VersionInfo("JudoScript Language", "", 0, 9, "", "2005-5-1", "1.3+");
        //  1  Added getOne() method to Set object.
        //  2  Added exponential expression (weird Java doesn't have that!)
  //new VersionInfo("JudoScript Language", "", 0, 9, "", "2005-5-3", "1.3+");
        //  1  Fixed the evalSeparate/evalFileSeparate to use #args rather than $$args.
  //new VersionInfo("JudoScript Language", "", 0, 9, "", "2005-6-14", "1.3+");
        //  1  Support <xyz-abc> tas in SGML/XML processing.
        //  2  In new com::'{...}', fixed the bug of not unquoting the string literal.
        //  3  Use com.apache.commons.lang as much as possible.
        //  4  Use com.apache.commons.lang.StringPrintWriter and removed our own.

  //new VersionInfo("JudoScript Language", "", 0, 9, "", "2005-7-25", "1.3+");
        //  1  Added string.toAbsoluteUrl() method.
        //  2  Added String[] getGroups() method to java.util.regex.Matcher (in bio/JavaObject).
        //  3  Fixed the else part of onSuccess-else.
        //  4  Fixed assertion (which used to write to RT.logger rather than RT.userLogger.)
        //  5  Removed RuntimeGlobalContext.log and use RT.logger for log manipulation.
        //  6  Fixed CC/BCC for send mail!

  new VersionInfo("JudoScript Language", "", 0, 9, "", "2005-7-31", "1.3+");
        //  1  Enabled string.toAbsoluteUrl() method.
        //  2  Fixed a MarkupParser bug, and enhance to have $$parser available to code,
        //     and it now has rushToTag() method..
        //  3  In the usage block, "args" can be an array now.

        //  ?  Added JDBC spy support. -- TODO

} // end of class VersionInfo.

