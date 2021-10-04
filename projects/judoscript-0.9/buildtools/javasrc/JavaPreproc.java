package com.judoscript.buildtool;

/**
 * NOT DONE YET.
 *
 * Java files with #if/#ifdef/#ifndef macros.
 * Java source files are by convention with extension of ".prejava".
 *
 * Supported preprocessors are:
 *
 * #if expr     -- expr must be: name op 'value'
 *                 where name is a system property name,
 *                       op is '==', '!=', '>', '>=', '&lt;' and '&lt;='
 *                       'value' is a string, or null.
 * #ifdef name  -- same as #if name == null
 * #ifndef name -- same as #if name != null
 * #else
 * #endif
 *
 * Note, #if...#endif's can be nested.
 */
public class JavaPreproc
{
  public static void main(String[] args) {
    if (args.length < 2)
      System.err.println("JavaPreproc takes 2 parameters: the input file name and the output file name.");
    else
      try { preproc(br, pw); } catch(Exception e) { e.printStackTrace(); }
  }

  static void preproc(BufferedReader br, PrintWriter pw) throws IOException {
    BufferedReader br = new BufferedReader(new FileReader(args[0]));
    PrintWriter    pw = new PrintWriter(new FileWriter(args[1]));

    int lineNo = 0;
    while (true) {
      String line = br.readLine();
      if (line == null) break;
      ++lineNo;

      String x = line.trim();
      if (x.startsWith("#") {
        boolean flag;
        if (x.startsWith("#ifndef")) {
          flag = eval(x.substring(7) + "!=null", lineNo);
        } else if (x.startsWith("#ifdef")) {
          flag = eval(x.substring(6) + "==null", lineNo);
        } else if (x.startsWith("#if")) {
          flag = eval(x.substring(3) + "==null", lineNo);
        } else if (x.startsWith("#else")) {
          // TODO
        } else if (x.startsWith("#endif")) {
          // TODO
        } else {
          System.err.println("Unrecognized pragma at line #" + lineNo + ": " + x);
          continue;
        }


      } else {
      }
    }
    
    br.close();
    pw.close();
  }

} // end of class JavaPreproc.
