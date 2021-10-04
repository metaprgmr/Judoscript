/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 04-19-2004  JH   Inception.
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript;

import java.io.*;
import java.util.zip.*;
import java.util.Iterator;
import com.judoscript.bio.*;
import com.judoscript.util.*;

public class ExprLines extends ExprAnyBase implements ExprCollective
{
  public Expr src;
  public Expr root = null; // must be a zip archive.
  public Expr filter = null;
  public boolean filterNegative = false;
  public Expr encoding = null;

  public int compareTo(Object o) { return 0; }

  public int      getType() { return TYPE_JAVA; }
  public String   getTypeName() { return "Lines"; }
  public Variable resolveVariable(String name) throws Throwable { return ValueSpecial.UNDEFINED; }
  public Variable resolveVariable(Variable idx) throws Throwable { return ValueSpecial.UNDEFINED; }
  public Variable invoke(String fxn, Expr[] params, int[] types) { return ValueSpecial.UNDEFINED; }
  public Variable cloneValue() { return this; }
  public Variable eval() throws Throwable { return this; }
  public Variable resolve(Variable idx) throws Exception { return null; }
  public Variable resolve(Variable[] dims) throws Exception { return null; }
  public Variable setVariable(Variable idx, Variable val, int type) throws Exception { return null; }
  public Variable setVariable(Variable[] dims, Variable val, int type) throws Exception { return null; }
  public Variable addVariable(Variable val, int type) throws Exception { return null; }
  public int      size() { return -1; }
  public void     close() {}
  public void     dump(XMLWriter out) {} // TODO

  public Iterator getIterator(int start, int to, int step, boolean upto, boolean backward) throws Throwable {
    Object o = null;
    String enc = encoding==null ? null : encoding.getStringValue();
    if (root == null) {
      o = IODevice.getInputStream(src.eval(), "Loop for Lines", enc);
    } else {
      o = root.eval();
      try {
        if (o instanceof ZipArchive) {
          o = ((ZipArchive)o).getBufferedReader(src.getStringValue(),
                encoding==null ? null : encoding.getStringValue());
        } else {
          ZipFile zf = new ZipFile(((Variable)o).getStringValue());
          o = zf.getInputStream(zf.getEntry(src.getStringValue()));
        }
      } catch(NullPointerException npe) { // entry does not exist.
        ExceptionRuntime.rte(RTERR_ILLEGAL_ARGUMENTS, "No such file in zip found.");
      }
    }
    BufferedReader br;
    if (o instanceof InputStream) {
      br = new BufferedReader(new InputStreamReader((InputStream)o, enc));
    } else if (o instanceof BufferedReader) {
      br = (BufferedReader)o;
    } else { // Reader
      br = new BufferedReader((Reader)o);
    }

    return RangeIterator.getIterator(new LinesIterator(br, filter, filterNegative), start, to, step, upto);
  }

  public Variable resolveRange(Variable low, Variable hi) throws Throwable {
    ExceptionRuntime.rte(RTERR_NO_SUCH_VALUE, getTypeName()+" has no ranged value.");
    return null;
  }

  // Its hasNext()/next() methods must be called alternatively.
  static class LinesIterator implements Iterator
  {
    BufferedReader br;
    Expr filter;
    boolean filterNegative;
    private String line;

    LinesIterator(BufferedReader br, Expr filter, boolean neg) {
      this.br = br;
      this.filter = filter;
      this.filterNegative = neg;
      line = null;
    }

    public boolean hasNext() {
      try {
        while (true) {
          line = br.readLine();
          if (line == null)
            return false;
          RT.setLocalVariable("$_", JudoUtil.toVariable(line), TYPE_STRING);
          if ((filter==null) || (filterNegative ^ !filter.getBoolValue()))
            return true;
        }
      } catch(Throwable e) {
        line = null;
        return false;
      }
    }

    public Object next() { return line; }
    public void remove() {}

    public void close() {
      try { br.close(); } catch(Exception e) {}
      line = null;
    }
  }

} // end of class ExprLines.

