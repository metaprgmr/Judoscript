/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 03-17-2002  JH   Initial open source release.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript;

import java.util.StringTokenizer;
import com.judoscript.util.XMLWriter;


public class StmtSCP extends StmtBase
{
  public Expr host;
  public Expr username;
  public Expr password;
  public Expr cipher;
  public boolean toRemote  = false;
  public boolean recursive = false;
  public boolean verbose   = false;
  public Expr src;
  public Expr dest;

  public StmtSCP(int line, Expr host) {
    super(line);
    this.host = host;
  }

  public void exec() throws Throwable {
    String _host = host.getStringValue();
    int _port = 22;
    int idx = _host.indexOf(":");
    if (idx > 0) {
      try { _port = Integer.parseInt(_host.substring(idx+1)); } catch(Exception e) {}
      _host = _host.substring(0,idx).trim();
    }
    String _user = username.getStringValue();
    String _pass = password.getStringValue();
    Variable v = (cipher!=null) ? cipher.eval() : ValueSpecial.NIL;
    String _cipher = v.isNil() ? null : v.getStringValue();
    v = (dest!=null) ? dest.eval() : ValueSpecial.NIL;
    String _dest = v.getStringValue();
    StringTokenizer st = new StringTokenizer(src.getStringValue(),",");
    String[] _src = new String[st.countTokens()];
    for (int i=0; i<_src.length; ++i)
      _src[i] = st.nextToken();

    JudoUtil.scp(_host,_port,_user,_pass,_cipher,_src,_dest,recursive,toRemote,verbose);
  }

  public void dump(XMLWriter out) {
    out.simpleSingleTagLn("StmtSCP");
    // TODO: dump().
  }

} // end of class StmtSCP.
