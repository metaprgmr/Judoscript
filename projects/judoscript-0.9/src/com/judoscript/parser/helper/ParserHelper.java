/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 03-17-2002  JH   Initial open source release.
 * 08-08-2002  JH   Added inFxnThrd and currentNS to support namespace.
 * 08-30-2002  JH   Added SgmlEvent, etc.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript.parser.helper;

import java.io.*;
import java.util.*;
import org.apache.commons.lang.StringUtils;
import com.judoscript.*;
import com.judoscript.bio.*;
import com.judoscript.db.*;
import com.judoscript.util.*;
import com.judoscript.parser.*;
import com.judoscript.gui.GuiListenerCollection;

/**
 * TODO: clean up the label mess --
 *       labels are now part of a loop statment and need no special treatment.
 */
public abstract class ParserHelper extends JudoUtil implements Consts
{
  HashSet forbiddenAntTasks = new HashSet();

  protected ParserHelper() {
    forbiddenAntTasks.add("ant");
    forbiddenAntTasks.add("anttask");
    forbiddenAntTasks.add("fail");
    forbiddenAntTasks.add("target");
  }

  public boolean checkAntTask(String name) {
    return forbiddenAntTasks.contains(name);
  }

  ////////////////////////////////////////////////////////////////////
  // Data members: used to communicate with jj.
  //

  public final static String eol = System.getProperty("line.separator", "\n");
  public final static Expr[] emptyExprs = new Expr[0];

  public Script script = new Script();
  public int curFileIndex = 0; // index into script.fileNames.
  protected MixtureParser mixtureParser = new MixtureParser();
  protected ParsingContext pc = null;
  protected Expr first = null;
  protected Expr[] rest = null;
  protected int[] ops = null;
  protected Stmt[] stmts = null;
  protected HashMap labels = null;
  protected boolean inClassDef = false;
  protected boolean inJavaCtor = false;
  protected boolean inJavax    = false;
  protected boolean inFxnThrd  = false;
  protected String  currentNS  = null;
  protected HashSet definedClasses = new HashSet();
  protected HashSet superCalls = new HashSet();
  protected HashSet javaxCalls = new HashSet();
  protected String encoding = null;

  private static final String[] disallowedAntNames = { "ant", "antcall", "target" };

  public static final boolean CheckSupportedAntTask(String name) {
    for (int i=disallowedAntNames.length-1; i>=0; --i)
      if (disallowedAntNames[i].equals(name)) return false;
    return true;
  }

  /**
   *  This stack is used for ()*, or rather, ()+ constructs.
   *  I believe jjtree uses the same mechanism, except that we
   *  can optimally utilize it for our grammar rules and nodes.
   *  
   *  The contents are typically String's, Expr's and Stmt's.
   *  For rules that use this stack, it should have a pointer
   *  (an int index) indicating its initial position, and at
   *  the end of the rule pop objects in the stack beyond the
   *  original position and construct whatever its return type.
   */
  protected Stack stack = new Stack();

  // release all resources used for parsing
  public void close() {
    cleanup();
    curFileIndex = 0;
    inClassDef = false;
    definedClasses.clear();
  }

  ////////////////////////////////////////////////////////////////////
  // User-Defined Class (including Java Extension) Methods
  //

  public void addFunction(Script script, Function fxn) throws ParseException {
    try { script.addFunction(fxn); } catch(Exception e) { throwParseException(e.getMessage()); }
  }

  public void addFunction(UserDefined.UserType ot, Function fxn) throws ParseException {
    try { ot.addFunction(fxn); } catch(Exception e) { throwParseException(e.getMessage()); }
  }

  public void addClassToScript(UserDefined.UserType ot) throws ParseException {
    try { script.addObjectType(ot); }
    catch(Exception e) { throwParseException(e.getMessage()); }
  }

  ////////////////////////////////////////////////////////////////////
  // Methods
  //

  public String getScriptBasePath() { return script.getScriptBasePath(); }

//  public void addFile(String fname) throws ParseException {
//    for (int i=script.fileNames.size()-1; i>=0; i--) {
//      String x = script.fileNames.elementAt(i).toString();
//      if (fname.equals(x))
//        throw new ParseException("File '" + fname + "' is being included more than once!");
//    }
//    script.fileNames.addElement(fname);
//  }

  protected final int markStack() { return stack.size(); }

  protected final int countOnStack(int lastMark) { return (stack.size() - lastMark); }

  public static ExprCall newCall(Expr var, String fxn, Expr[] params, int[] types) {
    return new ExprCall(var, fxn, params, types, false);
  }
  public static ExprCall newCall(String var, String fxn, Expr[] params, int[] types) {
    return new ExprCall(var==null?null:new AccessVar(var), fxn, params, types, false);
  }
  public static ExprCall newCall(String var, String fxn, Expr[] params, int[] types, boolean expand) {
    return new ExprCall(var==null?null:new AccessVar(var), fxn, params, types, expand);
  }

  //public static NewJava newJava(Expr className, Arguments args, int line) {
  public static NewJava newJava(Expr className, Object args, int line) {
    NewJava nj;
    if (args != null && args instanceof Arguments) {
      Arguments a = (Arguments)args;
      nj = new NewJava(className, a.getParams(), a.getTypes());
    } else {
      nj = new NewJava(className, args, null);
    }
    nj.setLineNumber(line);
    return nj;
  }

  public static NewJava newJava(Expr className, int line) {
    NewJava nj = new NewJava(className);
    nj.setLineNumber(line);
    return nj;
  }

  protected final Expr handleSingleExpr(int lastMark) {
    return countOnStack(lastMark) == 0 ? null : (Expr)stack.pop();
  }

  protected final Expr handleSingleExpr() { return (Expr)stack.pop(); }

  /**
   *@return BlockSimple or Block.
   */
  protected BlockSimple handleBlock(int lastMark, String exceptionName, int begin) {
    int cnt = countOnStack(lastMark);
    if (cnt <= 0) return BlockSimple.EMPTY;
    int newSize = stack.size() - cnt;

    // count labels first
    int i;
    Object o;
    labels = null;
    int numLabels = cnt;
    boolean hasCatch = false;
    for (i=newSize; i<stack.size(); i++) {
      o = stack.elementAt(i);
      if ("catch".equals(o) || "finally".equals(o)) { hasCatch = true; continue; }
      if (o instanceof Stmt) --numLabels;
    }
    if (numLabels > 0) labels = new HashMap();
    stmts = new Stmt[cnt - numLabels];
    int idx = 0;
    try {
      if (!hasCatch) {
        for (i=newSize; i<stack.size(); i++) {
          o = stack.elementAt(i);
          if (o instanceof Stmt) {
            stmts[idx++] = (Stmt)o;
          } else { // a label: String or StmtSwitch.Case.
            labels.put(o, new Integer(idx));
          }
        }
        return new BlockSimple(begin,stmts,labels);
      } else {
        int segment = 0; // 0: code; 1: catch; 2: finally
        int codeLen = 0;
        int catchLen = 0;
        int finallyLen = 0;
        int catchStart = 0;
        int finallyStart = 0;
        cnt = 0;
        for (i=newSize; i<stack.size(); i++) {
          o = stack.elementAt(i);
          if (o instanceof Stmt) {
            stmts[idx++] = (Stmt)o;
            switch(segment) {
            case 0:  codeLen++; break;
            case 1:  catchLen++; break;
            case 2:  finallyLen++; break;
            }
            cnt++;
          } else if ("catch".equals(o)) {
            catchStart = cnt;
            segment = 1;
          } else if ("finally".equals(o)) {
            finallyStart = cnt;
            segment = 2;
          } else { // a label: String or StmtSwitch.Case.
            labels.put(o, new Integer(idx));
          }
        }
        return new Block(begin,stmts,labels,codeLen,catchStart,catchLen,finallyStart,finallyLen,exceptionName);
      }
    } finally {
      stack.setSize(newSize);
    }
  }

  protected final Object[] handleAny(int lastMark) {
    int cnt = countOnStack(lastMark);
    if (cnt <= 0) return null;
    Object[] oa = new Object[cnt];
    for (int i=oa.length-1; i>=0; i--)
      oa[i] = stack.pop();
    return oa;
  }

  protected final String[] handleStrings(int lastMark) {
    int cnt = countOnStack(lastMark);
    if (cnt <= 0) return null;
    String[] sa = new String[cnt];
    for (int i=sa.length-1; i>=0; i--)
      sa[i] = stack.pop().toString();
    return sa;
  }

  /**
   * rest is set and returned.
   * On the stack is:  (Expr)+
   */
  protected final Expr[] handleExprs(int lastMark) {
    int cnt = countOnStack(lastMark);
    if (cnt <= 0) return null;

    int newSize = stack.size() - cnt;
    rest = new Expr[cnt];
    int idx = 0;
    for (int i=newSize; i<stack.size(); i++)
      rest[idx++] = reduce(stack.elementAt(i));
    stack.setSize(newSize);
    return rest;
  }

  /**
   * If true is returned, then first and rest are set.
   * On the stack is:  Expr, (Expr)+
   */
  protected final boolean handleMultiBase1(int lastMark) {
    int cnt = countOnStack(lastMark);
    if (cnt <= 1) return false;

    handleExprs(lastMark+1);
    first = (Expr)stack.pop();
    return true;
  }

  /**
   * If true is returned, then ops, first and rest are set.
   * On the stack is:  Expr, (Interger, Expr)*
   */
  protected final boolean handleMultiBase(int lastMark) {
    int cnt = countOnStack(lastMark);
    if (cnt <= 1) return false;

    --cnt;
    int newSize = stack.size() - cnt;
    cnt /= 2;
    ops = new int[cnt];
    rest = new Expr[cnt];

    int idx = 0;
    for (int i=newSize; i<stack.size(); i+=2) {
      ops[idx] = ((Integer)stack.elementAt(i)).intValue();
      rest[idx] = (Expr)stack.elementAt(i+1);
      ++idx;
    }
    stack.setSize(newSize);

    first = (Expr)stack.pop();
    return true;
  }

  /**
   * If true is returned, then stmts and labels are set.
   * On the stack are:  (String|Stmt)*
   */
  protected boolean handleStmts(int lastMark) {
    int cnt = countOnStack(lastMark);
    if (cnt <= 0) {
      stmts = null;
      return false;
    }

    // count labels first
    int newSize = stack.size() - cnt;
    int i;
    labels = null;
    int numLabels = cnt;
    for (i=newSize; i<stack.size(); i++) {
      if (stack.elementAt(i) instanceof Stmt)
        --numLabels;
    }
    if (numLabels > 0) labels = new HashMap();
    stmts = new Stmt[cnt - numLabels];
    int idx = 0;
    for (i=newSize; i<stack.size(); i++) {
      Object o = stack.elementAt(i);
      if (o instanceof Stmt) {
        if (o instanceof Expr)
          o = reduce(o);
        stmts[idx++] = (Stmt)o;
      } else {
        // a label can be a name (String), StmtSwitch.Case or StmtSwitch.Default.
        labels.put(o, new Integer(idx));
      }
    }
    stack.setSize(newSize);
    return true;
  }

  protected final AssociateList handleNamedExprs(int lastMark) {
    int cnt = countOnStack(lastMark);
    if (cnt <= 0) return null;
    AssociateList inits = new AssociateList();
    cnt /= 2;
    for (int i=cnt-1; i>=0; --i) {
      Expr expr = reduceIt();
      Object o = stack.pop();
      if (o instanceof Expr)
        o = reduce(o);
      inits.setAt(o, expr, i);
    }
    return inits;
  }

  protected final Expr handleMixture(Object[] oa) {
    return handleMixture(0, 0, oa);
  }

  protected final Expr handleMixture(int beginLine, int endLine, Object[] oa) {
    Object o = oa[0];
    if (oa.length == 1)
      return (o instanceof String) ? (Expr)toVariable((String)o) : (Expr)o;

    Expr lead = (o instanceof String) ? (Expr)toVariable((String)o) : reduce(o);
    Expr[] ea = new Expr[oa.length-1];
    for (int i=0; i<ea.length; i++) {
      o = oa[i+1];
      ea[i] = (o instanceof String) ? (Expr)toVariable((String)o) : reduce(o);
    }
    ExprMixture mix = new ExprMixture(lead, ea);
    mix.setLineNumbers(beginLine, endLine);
    return reduce(mix);
  }

  //
  // Pragmas, some apply to the parser, some apply to the script,
  // some apply at runtime.
  //
  public void handlePragma(int lineNo, String pragma, Variable val) {
    String x;
    Stmt stmt = null;
    try {
      if (pragma.equals("guiListener")) {
        x = val.getStringValue();
        StringTokenizer st = new StringTokenizer(x, ":");
        String nickname = (st.countTokens() > 2) ? st.nextToken() : null;
        String itf = st.nextToken();
        String cls = st.nextToken();
        GuiListenerCollection.registerImpl(nickname, itf, cls);
        return;
      } else if (pragma.equals("cryptoClass")) {
        script.setCryptoClassName(val.getStringValue());
        return;
      } else if (pragma.startsWith("logger.") || pragma.equals("logger")) {
        // See StmtPragma.java for details.
        stmt = new StmtPragma(lineNo, pragma, val);
      }
      stack.push(stmt);
    } catch(Throwable e) {
      RT.logger.error("Failed to handle pragma", e);
    }
  }

  public Stmt reduceBlock(BlockSimple blk) {
    if (blk == null) return null;
    Stmt[] stmts = blk.getStmts();
    if ((stmts!=null) && (stmts.length == 1))
      return stmts[0];
    return new StmtListStmt(stmts);
  }

  public final ExprBindVar newBindVar(int idx, String name, int type, String typeName, Expr expr) {
    return new ExprBindVar(idx, name, type, typeName, expr);
  }

  public final ExprOutBoundVar newOutBoundVar(int idx, String name, int type, Expr host, boolean inOut)
    throws ParseException
  {
    try {
      return new ExprOutBoundVar(idx, name, type, (ExprLValue)host, inOut);
    } catch(ClassCastException cce) {
      throwParseException("Outbound parameters must be valid variables or data members.");
      return null;
    }
  }

  public int getTargetID(String targetName) throws ParseException {
    if ("err".equals(targetName)) return RuntimeContext.PRINT_ERR;
    if ("out".equals(targetName)) return RuntimeContext.PRINT_OUT;
    if ("log".equals(targetName)) return RuntimeContext.PRINT_LOG;
    throwParseException("Invalid print target '" + targetName + "',",
                        "Valid targets are: 'out', 'err' and 'log'");
    return 0; // never reach.
  }

  public Expr nonAssignExpr(Object o) throws ParseException {
    if (o==null) return null;
    if (o instanceof ExprAssign)
      throwParseException("Assignment expression not allowed as conditions.");
    return reduce(o);
  }

  public final Expr reduceIt() { return reduce(stack.pop()); }

  public static Expr reduce(Object o) {
    if (o==null)
      return null;
    Expr e = (Expr)o;
    Stack stack = new Stack();
    Expr e1 = e.reduce(stack);
    if (stack.size() == 0)
      return e1;
    Expr[] ea = new Expr[stack.size()];
    for (int i=0; i<ea.length; i++)
      ea[i] = (Expr)stack.elementAt(i);
    return new ExprReduced(e1,ea);
  }

  public static Expr[] reduce(Expr[] ea) {
    int len = (ea==null) ? 0 : ea.length;
    for (int i=0; i<len; i++)
      ea[i] = reduce(ea[i]);
    return ea;
  }

  protected Expr handleForIn(int lineNum, AccessVar avar, Expr arr,
                             Expr from, Expr to, Expr step, boolean backward,
                             Expr[] forInit, Expr[] forNext, Stmt[] preStmts) {
    String arrVarName = genTempVarName();
    AccessVar arrVar  = new AccessVar(arrVarName);
    AccessVar idxVar  = new AccessVar(genTempVarName());
    AccessVar sizeVar = new AccessVar(genTempVarName());
    forInit[0] = ExprAssign.createLocalVar(lineNum,arrVar,arr);
    Expr length = reduce(new ExprPrimary( new Object[]{arrVarName,"length"} ));
    if (from == null) {
      if (backward) from = createSimpleArith("-",length,ConstInt.ONE);
      else from = ConstInt.ZERO;
    }
    forInit[1] = ExprAssign.createLocalVar(lineNum,idxVar,from);
    Expr end = null;
    if (backward) {
      if (to == null)
        end = ConstInt.ZERO;
      else
        end = new ExprConditional( createSimpleRelation("<",to,ConstInt.ZERO),
                                   ConstInt.ZERO,
                                   to );
    } else {
      end = length;
      if (to != null)
        end = new ExprConditional( createSimpleRelation("<",to,end),
                                   createSimpleArith("+",to,ConstInt.ONE),
                                   end );
    }
    forInit[2] = ExprAssign.createLocalVar(lineNum,sizeVar,end);
    forNext[0] = new ExprAssign(lineNum,
                                idxVar,
                                backward ? OP_MINUS_ASSIGN : OP_PLUS_ASSIGN,
                                (step==null) ? ConstInt.ONE : step);
    preStmts[0] = ExprAssign.createLocalVar(lineNum,avar,new AccessIndexed(arrVar,idxVar));
    return createSimpleRelation(backward?">=":"<",idxVar,sizeVar);
  }

  public final void defineJavaPackages(String pkgName, String javaPkg) {
    JavaPackages.addPackageDef(pkgName,javaPkg);
  }

  public final void handleJavaPackages(int lineNo, String pkgId, String var)
                                      throws ParseException
  {
    Class c = JavaPackages.matchClass(pkgId, var.substring(1)); // const name.
    if (c != null) {
      try {
        script.setConst(var, new JavaObject(c, null));
        return;
      } catch(Exception e) {}
    }
    StringBuffer sb = new StringBuffer("No <");
    sb.append(pkgId);
    sb.append("> class found for ");
    sb.append(var.substring(1));
    sb.append(".");
    throw new ParseException(sb.toString());
  }

  public boolean evalConstToBool(Expr expr, boolean def) {
    try { return expr.getBoolValue(); } catch(Throwable e) { return def; }
  }

  public static final boolean isOctalChar(char ch) { return (ch >= '0') && (ch <= '7'); }

  public static String unquote(String s) { return unquote(s, 1); }

  public static String unquote(String s, int delimLen) {
    return unicodify(s.substring(delimLen, s.length()-delimLen));
  }

  public static String unicodify(String s) {
    StringBuffer sb = new StringBuffer();
    int len = s.length();
    for (int i=0; i<len; i++) {
      char ch = s.charAt(i);
      if (ch != '\\') {
        sb.append(ch);
        continue;
      }
      ch = s.charAt(++i);
      switch(ch) {
      case 'n':  sb.append('\n');  continue;
      case 't':  sb.append('\t');  continue;
      case 'b':  sb.append('\b');  continue;
      case 'r':  sb.append('\r');  continue;
      case 'f':  sb.append('\f');  continue;
      case '$':  sb.append("\\$"); continue;
      case '\\': sb.append('\\');  continue;
      case '\'': sb.append('\'');  continue;
      case '"':  sb.append('"');   continue;
      case 'u':  // unicode
        String unicodeCh = s.substring(++i,i+4);
        i += 4;
        try { sb.append((char)Integer.parseInt(unicodeCh,16)); }
        catch(Exception e) { /* should not happen. */ }
        continue;
      default:
        if (!Character.isDigit((char)ch)) {
          sb.append((char)ch);
          continue;
        }

        // octal escape sequences
        char ch1, ch2;
        int val;
        if (!isOctalChar(ch)) { // a dumb escape; just append
          sb.append(ch);
        } else if (ch <= '3') { // a '\377' sequence
          if (++i >= len) { // no more characters!
            sb.append(ch);
          } else {
            ch1 = s.charAt(i);
            if (++i >= len) { // no more characters! Has to have 2 more.
              sb.append(ch);
              --i; // roll back
            } else {
              ch2 = s.charAt(i);
              if (!isOctalChar(ch1) || !isOctalChar(ch2)) { // not a good sequence
                sb.append(ch);
                i -= 2; // roll back
              } else {
                val = (ch2 - '0') & 0x0FF;
                val |= ((ch1 - '0') & 0x0FF) << 3;
                val |= ((ch - '0') & 0x0FF) << 6;
                sb.append((char)val);
              }
            }
          }
        } else { // a '\7' or '\77' sequence
          if (++i >= len) { // is a '\7'.
            sb.append((char)((ch - '0') & 0x0FF));
          } else {
            ch1 = s.charAt(i);
            if (!isOctalChar(ch1)) { // is a '\7'.
              sb.append((char)((ch - '0') & 0x0FF));
              --i; // roll back
            } else {
              val = (ch1 - '0') & 0x0FF;
              val |= ((ch - '0') & 0x0FF) << 3;
              sb.append((char)val);
            }
          }
        }
      } // switch().
    }
    return sb.toString();
  }

  public static int parseInt(String x) {
    try { return Integer.parseInt(x); } catch(Exception e) { return 0; }
  }

  protected void cleanup() {
    first = null;
    rest = null;
    ops = null;
    stmts = null;
    labels = null;
  }

  public abstract void throwParseException(String msg, String hint) throws ParseException;

  public final void throwParseException(String msg) throws ParseException {
    throwParseException(msg, null);
  }

  public final void throwParseException(Exception ex) throws ParseException {
    throwParseException(ex.getMessage(), null);
  }


  // Used by SGML processing statements:
  protected static final int SGML_BEFORE      = 1;
  protected static final int SGML_AFTER       = 2;
  protected static final int SGML_TAG         = 3;
  protected static final int SGML_DEFAULT_TAG = 4;
  protected static final int SGML_TEXT        = 5;
  protected static final int SGML_SPECIAL     = 6;

  public static class SgmlEvent {
    public int     type;
    public String  name;
    public boolean isEnd = false;
    public int     lineNum;
  };

  /** stmts is expected to be processed, which can be null */
  public void addSgmlHandler(SgmlEvent evt, StmtHtml hndlr) {
    switch(evt.type) {
    case SGML_BEFORE:      hndlr.setInit(stmts); break;
    case SGML_AFTER:       hndlr.setFinish(stmts); break;
    case SGML_TAG:         hndlr.addTagHandler(evt.lineNum, evt.isEnd, evt.name, stmts); break;
    case SGML_DEFAULT_TAG: hndlr.setDefaultTagHandler(evt.lineNum, stmts); break;
    case SGML_TEXT:        hndlr.setTextHandler(evt.lineNum, stmts); break;
    case SGML_SPECIAL:     hndlr.setSpecialTagHandler(evt.lineNum, evt.name, stmts); break;
    }
  }

  static NamedReader getReader(String name, Object src) throws IOException {
    boolean preproc = false;
    if (name != null) {
      String ext = StringUtils.defaultString(Lib.getFileExt(name)).toLowerCase();
      preproc = ext.equals("java") || ext.equals("jav") ||
                ext.equals("c") || ext.equals("cpp") || ext.equals("c++");
    }
    if (preproc) {
      if (src instanceof Reader)      return new JavaReader((Reader)src, name);
      if (src instanceof InputStream) return new JavaReader((InputStream)src, name);
      if (src instanceof String)      return new JavaReader((String)src);
      if (src instanceof File)        return new JavaReader((File)src);
    } else {
      if (src instanceof NamedReader) return (NamedReader)src;
      if (src instanceof Reader)      return new NamedReader((Reader)src, name);
      if (src instanceof InputStream) return new NamedReader((InputStream)src, name);
      if (src instanceof String)      return new NamedReader((String)src);
      if (src instanceof File)        return new NamedReader((File)src);
    }
    return null;
  }

  public static Script parse(String name, String path, InputStream is, String enc, long lastMod, boolean dbg)
    throws ParseException, IOException
  {
    RT.logger.debug("Parsing '" + name + "' (path: " + path + ").");
    NamedReader nr = getReader(name, is);
    JudoCharStream jcs = new JudoCharStream(nr);
    JudoParser parser = new JudoParser(jcs);
    parser.encoding = enc;
    Script script = parser.CompilationUnit(name,path,lastMod,dbg);
    script.setScriptPath(nr.getName());
    return script;
  }

  public static Script parse(String name, File f, String enc, long lastMod, boolean dbg)
    throws ParseException, IOException
  {
    RT.logger.debug("Parsing '" + name + "' (path: " + f.getAbsolutePath() + ").");
    NamedReader nr = getReader(name, f);
    JudoCharStream jcs = new JudoCharStream(nr);
    JudoParser parser = new JudoParser(jcs);
    parser.encoding = enc;
    Script script = parser.CompilationUnit(name, f.getParent(), lastMod, dbg);
    script.setScriptPath(nr.getName());
    return script;
  }

  public static Script parse(String name, String path, Reader r, String enc, long lastMod, boolean dbg)
    throws ParseException, IOException
  {
    RT.logger.debug("Parsing '" + name + "' (path: " + path + ").");
    NamedReader nr = getReader(name, r);
    JudoCharStream jcs = new JudoCharStream(nr);
    JudoParser parser = new JudoParser(jcs);
    parser.encoding = enc;
    Script script = parser.CompilationUnit(name, path, lastMod, dbg);
    script.setScriptPath(nr.getName());
    return script;
  }

  public static HashMap parseUsage(String fileName) {
    try {
      return new JudoParser(new JudoCharStream(new FileReader(fileName))).getUsage(fileName, ".");
    } catch(Exception e) {
      return null;
    }
  }

} // end of class ParserHelper.
