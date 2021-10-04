/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 03-17-2002  JH   Initial open source release.
 * 01-12-2003  JH   Added support for both input and output blocks.
 * 07-20-2004  JH   Use text I/O only.
 * 10-27-2004  JH   Judo scripts can run directly w/o "java judo ".
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript;

import java.io.*;
import java.util.*;
import org.apache.commons.lang.StringUtils;
import com.judoscript.bio.UserDefined;
import com.judoscript.bio.JavaObject;
import com.judoscript.util.*;


public final class StmtExec extends StmtBase
{
  private static final String ANON_VAR_NAME = "??_??-/"; // anything.

  Expr cmd;
  Expr workDir = null;
  Object env = null;   // AssociateList or Expression for Properties
  Stmt pipeIn = null;  // used to be BlockSimple
  Stmt pipeOut = null; // used to be BlockSimple
  boolean inherit = true;
  boolean asis = true;
  boolean needSystemIn = false;
  String varName = ANON_VAR_NAME;

  public StmtExec(int lineNum) { super(lineNum); }

  public void setCmdLine(Expr cmd) { this.cmd = cmd; }
  public void setWorkingDir(Expr dir) { workDir = dir; }
  public void setEnv(AssociateList e) { env = e; }
  public void setEnv(Expr e) { env = e; }
  public void setPipeInPart(Stmt blk) { pipeIn = blk; }
  public void setPipeOutPart(Stmt blk) { pipeOut = blk; }
  public void setNoInherit() { inherit = false; }
  public void setAsIs(boolean set) { asis = set; }
  public void setNeedSystemIn() {
    needSystemIn = true;
    if (varName == null)
      varName = ANON_VAR_NAME;
  }
  public void setReturnVariable(String name) {
    if (name != null || varName == ANON_VAR_NAME)
      varName = name;
  }

  public void exec() throws RuntimeException, Throwable {
    int i;
    String cmdline = cmd.getStringValue().trim();
    if (StringUtils.isBlank(cmdline))
      return;

    String pwd = (workDir==null) ? RT.getCurrentDir().toString() : workDir.getStringValue();
    pwd = Lib.toOSPath(pwd);

    String k, v;
    Map envVars = new HashMap();
    if (inherit)
      envVars.putAll(Lib.getEnvVars());

    if (env != null) {
      if (env instanceof AssociateList) {
        AssociateList al = (AssociateList)env;
        for (i=0; i<al.size(); i++) {
          String n = (String)al.getKeyAt(i);
          v = ((Expr)al.getValueAt(i)).getStringValue();
          envVars.put(n, v);
        }
      } else { // Expr
        Variable var = ((Expr)env).eval();
        if (var instanceof UserDefined) {
          UserDefined ud = (UserDefined)var;
          Iterator en = ud.getKeys();
          for (i=0; en.hasNext(); i++) {
            k = en.next().toString();
            v = ud.get(k).toString();
            if (!asis)
              v = Lib.toOSPath(v);
            envVars.put(k, v);
          }
        } else if (var instanceof JavaObject) {
          try {
            Map map = (Map)var.getObjectValue();
            Iterator iter = map.keySet().iterator();
            for (i=0; iter.hasNext(); i++) {
              k = iter.next().toString();
              v = map.get(k).toString();
              if (!asis)
                v = Lib.toOSPath(v);
              envVars.put(k, v);
            }
          } catch(ClassCastException cce) {}
        }
      }
    }
    String[] e = new String[ envVars.size() ];
    Iterator iter = envVars.keySet().iterator();
    for (i=0; iter.hasNext(); i++) {
      k = (String)iter.next();
      e[i] = k + "=" + envVars.get(k);
    }

    boolean hasPipeIn = pipeIn != null;
    boolean hasPipeOut = pipeOut != null;
    ExecuteCmdline ec = new ExecuteCmdline(procCmdline(cmdline));
    int ret = ec.exec(e, pwd, needSystemIn ? RT.getIn() : null,
                      RT.getOut(), RT.getErr(),
                      (varName != null), -1, hasPipeIn, hasPipeOut);
    if (hasPipeOut) {
      RT.setPipeIn(ec.getInput());
      if (hasPipeIn) {
        RT.setPipeOut(ec.getOutput());
        new ExecOutThread(pipeOut).start(null, false);
        pipeIn.exec();
        ret = ec.waitOn();
        ec.getOutput().close();
        RT.clearPipeOut();
      } else {
        pipeOut.exec();
        ret = ec.waitOn();
        RT.clearPipeIn();
      }
    } else if (hasPipeIn) {
      RT.setPipeOut(ec.getOutput());
      pipeIn.exec();
      ret = ec.waitOn();
      ec.getOutput().close();
      RT.clearPipeOut();
    }

    if (varName != null)
      RT.setVariable(varName, new ConstInt(ret), 0);
  }

  String procCmdline(String cmdline) {
    if (!asis)
      cmdline = Lib.toOSPath(cmdline);

    if ((cmdline.indexOf('\n') > 0) && (cmdline.indexOf('\r') > 0)) {
      try {
        StringBuffer sb = new StringBuffer();
        BufferedReader br = new BufferedReader(new StringReader(cmdline));
        while (true) {
          String line = br.readLine();
          if (line == null)
            break;
          sb.append(line);
          sb.append(' ');
        }
        cmdline = sb.toString();
      } catch(Exception e) {} // never happens?
    }

    // handle direct judo script calls
    String ex = cmdline;
    int idx = cmdline.indexOf(' ');
    if (idx < 0)
      idx = cmdline.indexOf('\t');
    if (idx > 0)
      ex = cmdline.substring(0, idx);
    ex = ex.toLowerCase();
    idx = ex.lastIndexOf('.');
    if (idx > 0)
      ex = ex.substring(idx+1);
    if (ex.startsWith("jud") && ex.length() <= 4)
      cmdline = "java judo -q " + cmdline;

    return cmdline;
  }

  public void dump(XMLWriter out) {
    out.simpleSingleTagLn("StmtExec"); // TODO
  }

  static class ExecOutThread extends _Thread
  {
    public ExecOutThread(Stmt b) { super(b); }
    protected boolean endBlock() {
      try { RT.clearPipeIn(); } catch(Exception e) {}
      return false;
    }
  }

} // end of class StmtExec.

