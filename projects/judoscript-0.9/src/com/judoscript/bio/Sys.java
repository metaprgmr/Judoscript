/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 03-17-2002  JH   Initial open source release.
 * 08-04-2002  JH   Added createTreeOutput system function.
 * 08-10-2002  JH   Correct readLine() and readPipe() to return EOF rather than -1.
 * 08-12-2002  JH   Added $encoding to getFileAsString() and getGZipFileAsString().
 * 08-22-2002  JH   Added loadProperties() method.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript.bio;

import java.io.*;
import java.util.*;
import java.net.Socket;
import java.net.ServerSocket;
import org.apache.commons.lang.StringUtils;
import com.judoscript.*;
import com.judoscript.parser.*;
import com.judoscript.util.*;
import com.judoscript.user.HibernateLib;


public final class Sys extends ObjectInstance implements ShortcutInvokable
{
  public Sys() { super(); }

  public String getTypeName() { return "Sys"; }
  public void close() {}

  public Variable invoke(String fxn, Expr[] args, int[] javaTypes) throws Throwable
  {
    return invoke(getMethodOrdinal(fxn), fxn, args, javaTypes);
  }

  public Variable invoke(int ord, String fxn, Expr[] params, int[] javaTypes) throws Throwable
  {
    File file;
    String s = null;
    int i;
    boolean isPushd = false;
    double d1, d2;
    Variable v;
    Properties props;
    InputStream is = null;
    int len = (params==null) ? 0 : params.length;
    switch (ord) {
    case BIM_DATE:
    case BIM_SYS_TIME:
    case BIM_SYS_TIMETODAY:
      _Date d = new _Date();
      if (len > 0) {
        if (ord == BIM_DATE)
          d.init(params);
        else if (ord == BIM_SYS_TIME)
          d.initTime(params);
        else
          d.initTimeToday(params);
      }
      return d;

    case BIM_SYS_COOKIE:
      if (len < 2)
        ExceptionRuntime.rte(RTERR_ILLEGAL_ARGUMENTS, "Cookie needs a name and a value");
      return JudoUtil.toVariable(new Cookie(params[0].getStringValue(), params[1].getStringValue()));

    case BIM_PWD:   return RT.getCurrentDir();
    case BIM_POPD:  RT.popd(); break;
    case BIM_PUSHD: isPushd = true; // fall thru ...
    case BIM_CD:
      //
      // spec:   cd [ rel_dir | abs_dir ]
      //
      if (len == 0) {
        if (isPushd)
          break;
        return RT.setGlobalVariable(".", JudoUtil.toVariable(Lib.getHomeDir().replace('\\', '/')), 0);
      }

      for (i=0; i<len; ++i) {
        s = RT.getFilePath(params[i].getStringValue());
        file = new File(s);
        if (!file.exists())
          ExceptionRuntime.rte(RTERR_ILLEGAL_ARGUMENTS, "Cannot CD to '"+s+"': does not exist.");
        if (!file.isDirectory())
          ExceptionRuntime.rte(RTERR_ILLEGAL_ARGUMENTS, "Cannot CD to '"+s+"': not a directory.");
        if (isPushd)
          RT.pushd();
        RT.setCurrentDir(s);
      }
      return JudoUtil.toVariable(s);

    case BIM_CREATETEMPFILE:
      //
      // spec:   createTempFile <prefix> [ <suffix> ]
      //
      if (len < 1)
        ExceptionRuntime.rte(RTERR_ILLEGAL_ARGUMENTS,
        "createTempFile() takes a prefix (no less than three characters) and an optional suffix.");
      s = (len <= 1) ? null : params[1].getStringValue();
      File tfile = File.createTempFile(params[0].getStringValue(), s);
      return JudoUtil.toVariable(tfile.getAbsolutePath());

    case BIM_SYS_RAND:
      if (len == 0) return new ConstDouble(Math.random());
      boolean isInt = params[0].isInt();
      if (len == 1) {
        if (isInt)
          return ConstInt.getInt((long)(Math.floor(0.5+Math.random()*params[0].getLongValue())));
        else
          return new ConstDouble(Math.random()*params[0].getDoubleValue());
      } else {
        isInt &= params[1].isInt();
        if (isInt) {
          long low = params[0].getLongValue();
          long hi  = params[1].getLongValue();
          return ConstInt.getInt((long)(Math.floor(0.5+Math.random()*(hi-low))+low));
        } else {
          double low = params[0].getDoubleValue();
          double hi  = params[1].getDoubleValue();
          return new ConstDouble( Math.random() * (hi-low) + low );
        }
      }

    case BIM_SYS_GETENVVARS:
      return RT.getEnvVars();

    case BIM_SYS_GETENVVAR:
      if (len == 0) break;
      return JudoUtil.toVariable(RT.getEnvVar(params[0].getStringValue()));

    case BIM_SYS_GETIN:  return new JavaObject(RT.getIn());
    case BIM_SYS_GETOUT: return new JavaObject(RT.getOut());
    case BIM_SYS_GETERR: return new JavaObject(RT.getErr());
    case BIM_SYS_GETLOG: return new JavaObject(RT.getLog());
//    case BIM_SYS_GETINSTREAM:  return new JavaObject(RT.getInStream());
//    case BIM_SYS_GETOUTSTREAM: return new JavaObject(RT.getOutStream());

    case BIM_SYS_SETOUT:
    case BIM_SYS_SETERR:
    case BIM_SYS_SETLOG: setPrintWriter(ord, params); break;

    case BIM_SYS_SETIN:
      RT.setIn((len==0) ? null : new BufferedReader(new FileReader(params[0].getStringValue())));
      break;

    case BIM_OPENGZIPPEDFILE:
    case BIM_OPENGZIPPEDTEXTFILE:
    case BIM_OPENFILE:
    case BIM_OPENTEXTFILE:
    case BIM_OPENRANDOMACCESSFILE:
      if (len > 0) {
        s = params[0].getStringValue();
        char mode = 'r';
        String encoding = null;
        if (len > 1) {
          try { mode = params[1].getStringValue().charAt(0); } catch(Exception e) {}
          if (len > 2)
            encoding = params[2].getStringValue();
        }
        switch(ord) {
        case BIM_OPENFILE:
          return IODevice.create(s, (mode=='r')?IO_INPUTSTREAM:IO_OUTPUTSTREAM, mode=='a', null, encoding);
        case BIM_OPENTEXTFILE:
          return IODevice.create(s, 
                                 (mode=='r')?IO_TEXTINPUTFILE:IO_TEXTOUTPUTFILE,
                                 mode=='a', null, encoding);
        case BIM_OPENRANDOMACCESSFILE:
          return IODevice.create(s, IO_RANDOMACCESS, false, (mode=='r')?"r":"rw", null);
        case BIM_OPENGZIPPEDFILE:
          return IODevice.create(s, (mode=='r')?IO_GZIPPED_INPUTFILE:IO_GZIPPED_OUTPUTFILE,
                                 false, null, encoding);
        case BIM_OPENGZIPPEDTEXTFILE:
          return IODevice.create(s,
                                 (mode=='r')?IO_GZIPPED_TEXTINPUTFILE:IO_GZIPPED_TEXTOUTPUTFILE,
                                 false, null, encoding);
        }
      }
      break;

    case BIM_SYS_CONNECTMAILSERVER:
      try { RT.getClass("javax.mail.Transport"); }
      catch(ClassNotFoundException cnfe) {
        ExceptionRuntime.rte(RTERR_ENVIRONMENT_ERROR,
          "Package javax.mail is required in the classpath to send mail.");
      }
      if (len <= 0)
        ExceptionRuntime.rte(RTERR_ILLEGAL_ARGUMENTS, "Mail server name must be specified.");
      String server = params[0].getStringValue();
      String username = null;
      String password = null;
      if (len > 1)
        username = params[1].getStringValue();
      if (len > 2)
        password = params[2].getStringValue();
      if (RT.getSendMail() == null) {
        RT.setSendMail(new SendMail(server));
      } else if (server.equals(RT.getSendMail().mailServer)) {
        if (RT.getSendMail().isConnected())
          break;
      } else {
        try { RT.getSendMail().disconnect(); } catch(Exception e) {}
        RT.setSendMail(new SendMail(server));
      }
      RT.getSendMail().connect(username, password);
      break;

    case BIM_SYS_DISCONNECTMAILSERVER:
      try { RT.getSendMail().disconnect(); } catch(Exception e) {}
      break;

    case BIM_READLINE:
      s = RT.getIn().readLine();
      if (s==null)
        return ConstString.EOF;
      return JudoUtil.toVariable(s);

    case BIM_READPIPE:
      s = RT.getPipeIn().readLine();
      if (s==null)
        return ConstString.EOF;
      return JudoUtil.toVariable(s);

    case BIM_SYS_TIMERHANDLER:
      return JudoUtil.toVariable(RT.getGuiDefaultHandler());

    case BIM_SYS_SETGUILISTENER:
      for (i=1; i<len; ++i) {
        Variable var = params[0].eval();
        RT.getScript().attachGuiHandler(params[i].getStringValue(), var.getObjectValue());
      }
      break;

    case BIM_SYS_GETDEFAULTDATEFORMAT:
      return JudoUtil.toVariable(RT.getDefaultDateFormat().toPattern());

    case BIM_SYS_SETDEFAULTDATEFORMAT:
      if (len > 0)
        RT.setDefaultDateFormat(params[0].getStringValue());
      break;

    case BIM_SYS_COMPARE:
      if (len < 2) break;
      v = params[0].eval();
      Variable v1 = params[1].eval();
      if (v.isNumber()) {
        d1 = v.getDoubleValue();
        d2 = v1.getDoubleValue();
        if (d1 == d2)
          return ConstInt.ZERO;
        return (d1 > d2) ? ConstInt.ONE : ConstInt.MINUSONE;
      } else {
        Object o1 = v.getObjectValue();
        Object o2 = v1.getObjectValue();
        if (o1 instanceof Comparable)
          return ConstInt.getInt( ((Comparable)o1).compareTo(o2) );
        else
          return ConstInt.getInt( o1.toString().compareTo(o2.toString()) );
      }

    case BIM_SYS_LOCK:
    case BIM_SYS_UNLOCK:
      if (len >= 1)
        RT.getRootFrame().lock(params[0].getStringValue(), (ord==BIM_SYS_LOCK), RT.getContextName());
      break;

    case BIM_SYS_WAITFOR:
      if (len >= 1)
        RT.getRootFrame().waitFor(params[0].getStringValue(), RT.getContextName());
      break;

    case BIM_SYS_NOTIFY:
    case BIM_SYS_NOTIFYALL:
      if (len >= 1)
        RT.getRootFrame().notify(params[0].getStringValue(), (ord==BIM_SYS_NOTIFYALL), RT.getContextName());
      break;

    case BIM_CREATETREEOUTPUT:
      if (len < 1)
        break;
      PrintWriter[] pws = new PrintWriter[len];
      for (i=0; i<len; i++) {
        Object o = params[i].eval().getObjectValue();
        if (o instanceof PrintWriter)
          pws[i] = (PrintWriter)o;
        else if (o instanceof Writer)
          pws[i] = new PrintWriter((Writer)o);
        else if (o instanceof OutputStream)
          pws[i] = new PrintWriter((OutputStream)o);
        else
          pws[i] = new PrintWriter(new FileWriter(o.toString()));
      }
      return JudoUtil.toVariable(new TreePrintWriter(pws));

    case BIM_SYS_SECRET:
      if (len < 1)
        break;
      return new Secret(params[0], (len>1) ? params[1] : null);

    case BIM_SYS_ACCEPTHTTP:
      if (len < 1)
        break;
      v = params[0].eval();
      if (v instanceof JavaObject) {
        if (((JavaObject)v).object instanceof ServerSocket) {
          ServerSocket ss = (ServerSocket)((JavaObject)v).object;
          Socket skt = ss.accept();
          return new HttpService(skt, ss.getLocalPort());
        }
      }
      break;

    case BIM_SYS_HTTPGET:
    case BIM_SYS_HTTPPOST:
      if (len < 1)
        break;
      return new _HTTP(params[0].getStringValue(), (ord==BIM_SYS_HTTPGET)?"GET":"POST");

    case BIM_SYS_COPYSTREAM:
      if (len < 1)
        break;
      v = params[0].eval();
      OutputStream os = null;
      boolean doClose = (len <3) ? true : params[2].getBoolValue();
      if ((v instanceof JavaObject) && (((JavaObject)v).object instanceof InputStream)) {
        is = (InputStream)((JavaObject)v).object;
      } else {
        v = IODevice.create(v.getStringValue(), IODevice.IO_INPUTSTREAM, false, null, null);
        is = (InputStream)v.getObjectValue();
      }

      if (len > 1) {
        v = params[1].eval();
        if ((v instanceof JavaObject) && (((JavaObject)v).object instanceof OutputStream)) {
          os = (OutputStream)((JavaObject)v).object;
        } else {
          os = new FileOutputStream(v.getStringValue());
          doClose = true;
        }
      }

      try {
        Lib.copyStream(is, os, doClose);
        return ConstInt.TRUE;
      } catch(Exception e) {
        return ConstInt.FALSE;
      }

    case BIM_SYS_REGEX:
      if (len < 1) break;
      return RT.getRegexCompiler().compile(params);

    case BIM_SYS_SETCHARSET:
      s = (len<1) ? null : params[0].getStringValue();
      RT.setCharset(s);
      break;

    case BIM_SYS_GETCHARSET:
      return JudoUtil.toVariable(RT.getCharset());

    case BIM_SYS_ASSERT:
      i = RT.getAssertAs();
      if (i==ISSUE_LEVEL_IGNORE || len < 1)
        break;
      if (!params[0].getBoolValue()) {
        if (i == ISSUE_LEVEL_ERROR)
          ExceptionRuntime.assertFail((len<2) ? null : params[1].getStringValue());
        String msg = "Assertion failed" + (len<2 ? "" : ": " + params[1].getStringValue());
        int lineNum = RT.getLineNumber();
        if (lineNum > 0)
          msg += " at line #" + lineNum;
        switch (i) {
        case ISSUE_LEVEL_INFO:  RT.userLogger.info(msg);  break;
        case ISSUE_LEVEL_WARN:  RT.userLogger.warn(msg);  break;
        case ISSUE_LEVEL_DEBUG: RT.userLogger.debug(msg); break;
        }
      }
      break;
      
    case BIM_SYS_THISLINE:
      return ConstInt.getInt(RT.getLineNumber());

    case BIM_SYS_THISFILE:
      return JudoUtil.toVariable(RT.getSrcFileName());

    case BIM_SYS_ENCODE:
      if (len < 1)
        break;
      v = params[0].eval();
      byte[] ba = null;
      if (v instanceof JavaArray) {
        Object o = ((JavaArray)v).getObjectValue();
        if (o instanceof byte[])
          ba = (byte[])o;
      } else if (v instanceof ValueBase) {
        ba = v.getStringValue().getBytes();
      } else {
        ExceptionRuntime.rte(RTERR_ILLEGAL_ARGUMENTS, "encode() takes a byte array or a string.");
      }
      // got the encoding token and the bytes.
      String enc = (len > 1) ? params[1].getStringValue() : null;
      int offset = (len <= 2) ? 0 : (int)params[2].getLongValue();
      len = (len > 3) ? (int)params[3].getLongValue() : (ba.length-offset);
      if (StringUtils.isBlank(enc))            // use default character encoding
        enc = new String(ba, offset, len);
      else if (enc.equalsIgnoreCase("base64")) // base 64 encoding
        enc = new String(Lib.base64Encode(ba));
      else                                     // use s as character encoding
        enc = new String(ba, offset, len, enc);
      return JudoUtil.toVariable(enc);

    case BIM_SYS_SSH:
      if (len < 3)
        ExceptionRuntime.rte(RTERR_ILLEGAL_ARGUMENTS,
          "ssh() takes these parameters: host[:port], username, password [, cipher].");
      String cipher = (len >= 4) ? params[3].getStringValue() : null;
      s = params[0].getStringValue();
      i = s.indexOf(":");
      len = 22; // default port
      if (i>0) {
        try { len = Integer.parseInt(s.substring(i+1)); } catch(Exception e) {}
        s = s.substring(0, i);
      }
      return JudoUtil.getSSH(s, len,  params[1].getStringValue(), params[2].getStringValue(), cipher);

    case BIM_SYS_GETFUNCTIONS:
    case BIM_SYS_GETTHREADS:
      String[] sa = RT.getScript().getFunctionThreads(
                      (len<=0)?null:params[0].getStringValue(), (ord==BIM_SYS_GETFUNCTIONS) );
      return JudoUtil.arrayToJudoArray(sa);

    case BIM_MAX:
      if (len < 1)
        break;
      d1 = params[0].getDoubleValue();
      for (i=len-1; i>=0; --i) {
        d2 = params[i].getDoubleValue();
        if (d2 > d1) d1 = d2;
      }
      return new ConstDouble(d1);

    case BIM_MIN:
      if (len < 1)
        break;
      d1 = params[0].getDoubleValue();
      for (i=len-1; i>=0; --i) {
        d2 = params[i].getDoubleValue();
        if (d2 < d1) d1 = d2;
      }
      return new ConstDouble(d1);

/*
    case BIM_SYS_DIFF:
      if (len < 2) break;
      Variable lhs = params[0].eval();
      Variable rhs = params[1].eval();
      Set sl = null;
      Set sr = null;
      if (lhs instanceof UserDefined) {
        vl = ((UserDefined)lhs).getKeysAsSet();
      } else if (lhs instanceof _Set) {
        vl = ((_Set)lhs).cloneAllObjectsAsSet();
      } else if (lhs instanceof _Array) {
        vl = ((_Array)lhs).getAllObjectsAsSet();
      }
      return null; // TODO
*/

    case BIM_SYS_CONVERTTOVARIABLES:
      if (len < 1)
        break;
      boolean isLocal = (len == 1) ? false : params[1].getBoolValue();
      v1 = params[0].eval();
      String k;
      if (v1 instanceof UserDefined) {
        UserDefined ud = (UserDefined)v1;
        Iterator iter = ud.getKeys();
        while (iter.hasNext()) {
          k = iter.next().toString();
          setVariable(k, ud.resolveVariable(k), isLocal);
        }
      } else if (v1 instanceof JavaObject) {
        Object o = v1.getObjectValue();
        if (o instanceof Map) {
          Map m = (Map)o;
          Iterator iter = m.keySet().iterator();
          while (iter.hasNext()) {
            k = iter.next().toString();
            setVariable(k, JudoUtil.toVariable( m.get(k)), isLocal);
          }
        }
      }
      break;

    case BIM_SYS_SETVARIABLE:
      if (len < 2)
        break;
      isLocal = (len == 2) ? false : params[2].getBoolValue();
      setVariable(params[0].getStringValue(), params[1].eval(), isLocal);
      break;

    case BIM_SYS_GETVARIABLE:
      if (len < 1)
        break;
      return RT.resolveVariable(params[0].getStringValue());

    case BIM_SYS_EXIT:
      throw new ExceptionExit((len >= 1) ? params[0].getStringValue() : "0");

    case BIM_SYS_GETSCRIPTPATH:
      return JudoUtil.toVariable(RT.getScript().getScriptPath());

    case BIM_SYS_LOOPINDEX:
      return ConstInt.getInt(RT.curLoopIndex(len < 1 ? 0 : (int)params[0].getLongValue()));
 
    case BIM_SYS_ECHO:
      if (len < 0)
        RT.echoOff();
      else
        RT.echoOn(params[0].getStringValue());
      break;

    case BIM_SYS_BOOLEAN:
    case BIM_SYS_BYTE:
    case BIM_SYS_CHAR:
    case BIM_SYS_SHORT:
    case BIM_SYS_INT:
    case BIM_SYS_LONG:
    case BIM_SYS_FLOAT:
    case BIM_SYS_DOUBLE:
      if (len < 1)
        break;
      v = params[0].eval();
      if (v instanceof ConstInt) {
        switch (ord) {
        case BIM_SYS_BOOLEAN: len = JAVA_BOOLEAN; break;
        case BIM_SYS_BYTE:    len = JAVA_BYTE;    break;
        case BIM_SYS_SHORT:   len = JAVA_SHORT;   break;
        case BIM_SYS_INT:     len = JAVA_INT;     break;
        case BIM_SYS_LONG:    len = JAVA_LONG;    break;
        case BIM_SYS_FLOAT:   len = JAVA_FLOAT;   break;
        case BIM_SYS_DOUBLE:  len = JAVA_DOUBLE;  break;
        case BIM_SYS_CHAR:    len = JAVA_CHAR;    break;
        }
        v.setJavaPrimitiveType(len);
        return v;
      } else if ((ord==BIM_SYS_CHAR) && (v instanceof ConstString)) {
        v.setJavaPrimitiveType(JAVA_CHAR);
        return v;
      }
      ExceptionRuntime.rte(RTERR_ILLEGAL_ARGUMENTS, "Can't cast \"" + v.getStringValue() +
                           "\" to a Java primitive type.");

    case BIM_SYS_ANTCALL:
      if (len >= 1)
        AntJudoScriptTask.antcall(params);
      break;

    case BIM_SYS_EVAL:
    case BIM_SYS_EVALSEPARATE:
    case BIM_SYS_EVALFILE:
    case BIM_SYS_EVALFILESEPARATE:
      if (len <= 0)
        break;

      String x = params[0].getStringValue().trim();
      Script script;

      if (ord == BIM_SYS_EVAL || ord == BIM_SYS_EVALSEPARATE) {
        script = JudoParser.parse("<string>", "./", new StringReader(x), null, 0, false);
      } else {
        script = JudoParser.parse(Lib.getFileName(x), new File(x), null, 0, false);
      }

      Expr[] args = null;
      if (len > 1) {
        args = new Expr[len-1];
        System.arraycopy(params, 1, args, 0, len-1);
      }

      if (ord == BIM_SYS_EVALSEPARATE || ord == BIM_SYS_EVALFILESEPARATE) { // in a new context
        return JudoUtil.toVariable(script.startAllowException(args, RT.curCtxt()));
      } else { // in context
        RT.getScript().takeDecls(script);
        try {
          script.exec(args); // treating it as a simple block; all decls therein are ignored!
        } finally {
          script.restoreLocalDataSource();
        }
      }
      break;

    case BIM_SYS_DB_DISCONNECT:
      Expr acc = (len < 1) ? new AccessVar(DEFAULT_CONNECTION_NAME) : params[0];
      v = acc.eval();
      v.invoke("close", null, null);
      break;

    case BIM_NEVEREMPTY:
      if (len < 0)
        break;
      v = params[0].eval();
      if (v.isNil())
        return len > 1 ? params[1].eval() : ConstString.SPACE;
      return v;

    case BIM_HIB_ADD_CLASS:
      for (i=0; i<len; ++i)
        HibernateLib.addClass(params[i].getObjectValue());
      break;

    case BIM_HIB_ADD_RESOURCE:
      for (i=0; i<len; ++i)
        HibernateLib.addResource(params[i].getStringValue());
      break;

    default: return super.invoke(ord,fxn,params);
    }

    return ValueSpecial.UNDEFINED;
  }

  ////////////////////////////////////////////////////////////////////
  // Helpers
  //
  static void setVariable(String name, Object val, boolean isLocal) throws Throwable {
    if (StringUtils.isBlank(name))
      return;
    char ch = name.charAt(0);
    if (!Character.isJavaIdentifierStart(ch) && (ch!='$'))
      return;
    Variable v = (val instanceof Variable) ? (Variable)val : JudoUtil.toVariable(val);
    if (isLocal)
      RT.setLocalVariable(name, v, v.getJavaPrimitiveType());
    else
      RT.setVariable(name, v, v.getJavaPrimitiveType());
  }

  /**
   * <u>Category</u>: General
   *<p>
   * Waits <em>dur</em> milliseconds on object <em>o</em> since the <em>starttime</em>.
   *<p>
   * If <em>interruptable</em> is false, when interrupted, it goes back
   * to sleep until the duration of rest has elapsed. Otherwise, it
   * returns immediately.
   */
  public final static void wait(long starttime, long dur, boolean interruptable) {
    while (true) {
      long lapsed = System.currentTimeMillis() - starttime;
      if (lapsed >= dur)
        return;
      try { Thread.sleep(dur - lapsed); }
      catch(InterruptedException ie) { if (interruptable) break; }
    }
  }

  void setPrintWriter(int choice, Expr[] args) throws Throwable {
    Object output = null;
    boolean append = false;
    int len = (args==null) ? 0 : args.length;
    if (len > 0) {
      output = args[0].getObjectValue();
      append = (len <= 1) ? false : args[1].getBoolValue();
    }
    if (output == null) {
      switch(choice) {
      case BIM_SYS_SETOUT: RT.setOut(null); break;
      case BIM_SYS_SETERR: RT.setErr(null); break;
      case BIM_SYS_SETLOG: RT.setLog(null); break;
      }
    } else {
      LinePrintWriter pw = JudoUtil.toLinePrintWriter(output, append);
      switch(choice) {
      case BIM_SYS_SETOUT: RT.setOut(pw); break;
      case BIM_SYS_SETERR: RT.setErr(pw); break;
      case BIM_SYS_SETLOG: RT.setLog(pw); break;
      }
    }
  }

} // end of class Sys.
